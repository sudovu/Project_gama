package com.example.core.util

import android.content.Context
import com.example.domain.model.Track
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.UUID

/**
 * High-performance parser and cache manager for direct audio/video stream URLs,
 * live radio endpoints, and M3U/M3U8 playlists.
 */
object StreamPlaylistParser {

    private const val STREAM_SOURCE = "stream"
    private const val CACHE_SUBDIR = "stream_playlists"

    /**
     * Parses a direct stream URL, M3U/M3U8 playlist URL, or raw M3U text content.
     * Returns a list of playable Tracks with intelligent metadata fallback.
     */
    fun parseStreamOrPlaylist(
        input: String,
        fallbackTitle: String = "",
        fallbackArtist: String = "",
        cacheDir: File? = null
    ): List<Track> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return emptyList()

        val tracks = when {
            // Raw M3U content directly pasted
            trimmed.contains("#EXTM3U") || (trimmed.contains("#EXTINF") && trimmed.contains("\n")) -> {
                parseM3uContent(trimmed, baseUrl = null, fallbackTitle = fallbackTitle, fallbackArtist = fallbackArtist)
            }

            // Remote URL
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("rtsp://", ignoreCase = true) -> {
                if (isM3uUrl(trimmed)) {
                    val fetched = fetchRemoteText(trimmed)
                    if (fetched != null && (fetched.contains("#EXTM3U") || fetched.contains("#EXTINF"))) {
                        parseM3uContent(fetched, baseUrl = trimmed, fallbackTitle = fallbackTitle, fallbackArtist = fallbackArtist)
                    } else {
                        // If fetching fails or it's a single HLS master stream (.m3u8), treat as direct stream
                        listOf(createDirectStreamTrack(trimmed, fallbackTitle, fallbackArtist))
                    }
                } else {
                    listOf(createDirectStreamTrack(trimmed, fallbackTitle, fallbackArtist))
                }
            }

            // Fallback for single line URL without scheme
            trimmed.contains(".") && !trimmed.contains("\n") -> {
                val urlWithScheme = "https://$trimmed"
                listOf(createDirectStreamTrack(urlWithScheme, fallbackTitle, fallbackArtist))
            }

            else -> emptyList()
        }

        // Cache playlist if name provided and cacheDir available
        if (tracks.isNotEmpty() && cacheDir != null) {
            val playlistName = fallbackTitle.ifBlank { "Stream Playlist" }
            saveToCache(cacheDir, playlistName, tracks)
        }

        return tracks
    }

    /**
     * Parses standard M3U/M3U8 text lines.
     */
    fun parseM3uContent(
        content: String,
        baseUrl: String? = null,
        fallbackTitle: String = "",
        fallbackArtist: String = ""
    ): List<Track> {
        val tracks = mutableListOf<Track>()
        val lines = content.lines()

        var currentTitle = fallbackTitle
        var currentArtist = fallbackArtist
        var currentDuration = 0

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                // Format: #EXTINF:<duration>,<artist> - <title> OR #EXTINF:<duration>,<title>
                val info = line.substringAfter("#EXTINF:").trim()
                val commaIdx = info.indexOf(',')
                if (commaIdx != -1) {
                    val durStr = info.substring(0, commaIdx).trim()
                    currentDuration = durStr.toIntOrNull()?.coerceAtLeast(0) ?: 0

                    val titleAndArtist = info.substring(commaIdx + 1).trim()
                    if (titleAndArtist.contains(" - ")) {
                        val parts = titleAndArtist.split(" - ", limit = 2)
                        currentArtist = parts[0].trim()
                        currentTitle = parts[1].trim()
                    } else {
                        currentTitle = titleAndArtist
                        if (currentArtist.isBlank()) currentArtist = fallbackArtist.ifBlank { "Direct Stream" }
                    }
                }
            } else if (!line.startsWith("#")) {
                // URL line
                val resolvedUrl = resolveUrl(baseUrl, line)
                val finalTitle = if (currentTitle.isNotBlank()) currentTitle else extractTitleFromUrl(resolvedUrl)
                val finalArtist = if (currentArtist.isNotBlank()) currentArtist else extractArtistFromUrl(resolvedUrl)

                val trackId = "stream_" + UUID.nameUUIDFromBytes(resolvedUrl.toByteArray()).toString().take(12)
                tracks.add(
                    Track(
                        id = trackId,
                        title = finalTitle,
                        artist = finalArtist,
                        durationSeconds = currentDuration,
                        streamUrl = resolvedUrl,
                        source = STREAM_SOURCE,
                        codec = extractCodecFromUrl(resolvedUrl),
                        frequencyHz = 432
                    )
                )

                // Reset per-track state
                currentTitle = ""
                currentArtist = ""
                currentDuration = 0
            }
        }

        return tracks
    }

    /**
     * Creates a standalone Track entity from a direct stream or live broadcast URL.
     */
    fun createDirectStreamTrack(
        url: String,
        fallbackTitle: String = "",
        fallbackArtist: String = ""
    ): Track {
        val finalTitle = if (fallbackTitle.isNotBlank()) fallbackTitle else extractTitleFromUrl(url)
        val finalArtist = if (fallbackArtist.isNotBlank()) fallbackArtist else extractArtistFromUrl(url)
        val trackId = "stream_" + UUID.nameUUIDFromBytes(url.toByteArray()).toString().take(12)

        return Track(
            id = trackId,
            title = finalTitle,
            artist = finalArtist,
            durationSeconds = 0, // Live / continuous stream
            streamUrl = url,
            source = STREAM_SOURCE,
            codec = extractCodecFromUrl(url),
            frequencyHz = 432
        )
    }

    private fun isM3uUrl(url: String): Boolean {
        val clean = url.substringBefore('?').lowercase()
        return clean.endsWith(".m3u") || clean.endsWith(".m3u8")
    }

    private fun extractTitleFromUrl(url: String): String {
        return try {
            val path = URI(url).path
            val lastSegment = path.substringAfterLast('/').substringBeforeLast('.')
            if (lastSegment.isNotBlank() && lastSegment != "/") {
                lastSegment.replace('_', ' ').replace('-', ' ').trim()
            } else {
                URI(url).host ?: "Live Stream"
            }
        } catch (_: Exception) {
            "Live Stream"
        }
    }

    private fun extractArtistFromUrl(url: String): String {
        return try {
            val host = URI(url).host
            host?.removePrefix("www.") ?: "Direct Stream"
        } catch (_: Exception) {
            "Direct Stream"
        }
    }

    private fun extractCodecFromUrl(url: String): String {
        val clean = url.substringBefore('?').lowercase()
        return when {
            clean.endsWith(".m3u8") -> "HLS"
            clean.endsWith(".flac") -> "FLAC"
            clean.endsWith(".wav") -> "WAV"
            clean.endsWith(".aac") -> "AAC"
            clean.endsWith(".ogg") -> "OGG"
            clean.endsWith(".opus") -> "OPUS"
            clean.endsWith(".mp4") || clean.endsWith(".m4a") -> "AAC"
            clean.endsWith(".mp3") -> "MP3"
            clean.startsWith("rtsp://") -> "RTSP"
            else -> "STREAM"
        }
    }

    /**
     * Resolves local file paths or remote URLs, handling both relative filesystem entries
     * and HTTP/HTTPS/RTSP base URLs.
     */
    fun resolveUrl(baseUrl: String?, relativeOrAbsolute: String): String {
        val trimmed = relativeOrAbsolute.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("rtsp://", ignoreCase = true) ||
            trimmed.startsWith("content://", ignoreCase = true) ||
            trimmed.startsWith("file://", ignoreCase = true)
        ) {
            return trimmed
        }

        // Direct absolute path on Unix/Android or Windows
        if (trimmed.startsWith("/") || trimmed.matches(Regex("^[a-zA-Z]:[/\\\\].*"))) {
            return trimmed
        }

        if (baseUrl.isNullOrBlank()) return trimmed

        return try {
            if (baseUrl.startsWith("http://", ignoreCase = true) ||
                baseUrl.startsWith("https://", ignoreCase = true) ||
                baseUrl.startsWith("rtsp://", ignoreCase = true)
            ) {
                URI(baseUrl).resolve(trimmed).toString()
            } else if (baseUrl.startsWith("file://", ignoreCase = true)) {
                val fileUri = URI(baseUrl)
                val parent = File(fileUri.path).parentFile
                if (parent != null) {
                    val resolved = File(parent, trimmed)
                    "file://${resolved.absolutePath.replace('\\', '/')}"
                } else {
                    fileUri.resolve(trimmed).toString()
                }
            } else {
                // Local filesystem path
                val parent = File(baseUrl).parentFile
                if (parent != null) {
                    File(parent, trimmed).absolutePath
                } else {
                    trimmed
                }
            }
        } catch (_: Exception) {
            trimmed
        }
    }

    /**
     * Parses an M3U or M3U8 playlist file from local storage.
     */
    fun parseM3uFile(
        file: File,
        fallbackTitle: String = "",
        fallbackArtist: String = ""
    ): List<Track> {
        if (!file.exists() || !file.isFile) return emptyList()
        val text = file.readText(Charsets.UTF_8)
        val title = fallbackTitle.ifBlank { file.nameWithoutExtension.replace('_', ' ') }
        return parseM3uContent(
            content = text,
            baseUrl = file.absolutePath,
            fallbackTitle = title,
            fallbackArtist = fallbackArtist
        )
    }

    /**
     * Parses an M3U or M3U8 playlist from an InputStream (e.g. Android ContentResolver).
     */
    fun parseM3uInputStream(
        inputStream: java.io.InputStream,
        baseUriOrPath: String? = null,
        fallbackTitle: String = "",
        fallbackArtist: String = ""
    ): List<Track> {
        val text = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return parseM3uContent(
            content = text,
            baseUrl = baseUriOrPath,
            fallbackTitle = fallbackTitle,
            fallbackArtist = fallbackArtist
        )
    }

    /**
     * Fetches live ICY metadata (e.g. Shoutcast/Icecast StreamTitle) from a streaming URL.
     */
    fun fetchIcyStreamMetadata(streamUrl: String): String? {
        if (!streamUrl.startsWith("http://", ignoreCase = true) && !streamUrl.startsWith("https://", ignoreCase = true)) {
            return null
        }
        return try {
            val url = URL(streamUrl)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3500
            conn.readTimeout = 4000
            conn.setRequestProperty("Icy-MetaData", "1")
            conn.setRequestProperty("User-Agent", "GAMA/2.3 (Android; Cybernetic Live Audio Engine)")
            conn.connect()

            val icyName = conn.getHeaderField("icy-name")
            val metaInt = conn.getHeaderField("icy-metaint")?.toIntOrNull() ?: 0

            var streamTitle: String? = null
            if (metaInt in 1..65536) {
                conn.inputStream.use { stream ->
                    var skipped = 0L
                    while (skipped < metaInt) {
                        val skip = stream.skip(metaInt - skipped)
                        if (skip <= 0) break
                        skipped += skip
                    }
                    val metaLengthByte = stream.read()
                    if (metaLengthByte > 0) {
                        val metaLength = metaLengthByte * 16
                        val metaBytes = ByteArray(metaLength)
                        var read = 0
                        while (read < metaLength) {
                            val r = stream.read(metaBytes, read, metaLength - read)
                            if (r <= 0) break
                            read += r
                        }
                        val metaString = String(metaBytes, 0, read, Charsets.UTF_8)
                        val titleMatch = Regex("StreamTitle='([^']*)';").find(metaString)
                        streamTitle = titleMatch?.groupValues?.getOrNull(1)?.trim()
                    }
                }
            } else {
                conn.disconnect()
            }
            if (!streamTitle.isNullOrBlank()) streamTitle else icyName?.trim()
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchRemoteText(urlStr: String): String? {
        return try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 7000
            conn.setRequestProperty("User-Agent", "GAMA/2.3 (Android; Cybernetic Audio Engine)")
            if (conn.responseCode in 200..299) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    // ==========================================
    // STREAM FAVORITES & RECENT HISTORY PERSISTENCE
    // ==========================================
    private const val PREFS_STREAMS = "gama_streams_store"
    private const val KEY_FAVORITES = "favorite_streams_json"
    private const val KEY_RECENTS = "recent_streams_json"
    private const val MAX_RECENTS = 25

    fun getFavoriteStreams(context: Context): List<Track> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_STREAMS, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
            deserializeTrackList(jsonStr)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun isStreamFavorite(context: Context, streamUrl: String): Boolean {
        if (streamUrl.isBlank()) return false
        val favorites = getFavoriteStreams(context)
        return favorites.any { it.streamUrl.equals(streamUrl, ignoreCase = true) }
    }

    fun toggleFavoriteStream(context: Context, track: Track): Boolean {
        if (track.streamUrl.isBlank()) return false
        return try {
            val prefs = context.getSharedPreferences(PREFS_STREAMS, Context.MODE_PRIVATE)
            val current = getFavoriteStreams(context).toMutableList()
            val index = current.indexOfFirst { it.streamUrl.equals(track.streamUrl, ignoreCase = true) }
            val isNowFavorite = if (index != -1) {
                current.removeAt(index)
                false
            } else {
                current.add(0, track)
                true
            }
            prefs.edit().putString(KEY_FAVORITES, serializeTrackList(current)).apply()
            isNowFavorite
        } catch (_: Exception) {
            false
        }
    }

    fun getRecentStreams(context: Context): List<Track> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_STREAMS, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_RECENTS, null) ?: return emptyList()
            deserializeTrackList(jsonStr)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun recordRecentStream(context: Context, track: Track) {
        if (track.streamUrl.isBlank()) return
        try {
            val prefs = context.getSharedPreferences(PREFS_STREAMS, Context.MODE_PRIVATE)
            val current = getRecentStreams(context).toMutableList()
            current.removeAll { it.streamUrl.equals(track.streamUrl, ignoreCase = true) }
            current.add(0, track)
            if (current.size > MAX_RECENTS) {
                current.subList(MAX_RECENTS, current.size).clear()
            }
            prefs.edit().putString(KEY_RECENTS, serializeTrackList(current)).apply()
        } catch (_: Exception) {}
    }

    fun clearRecentStreams(context: Context) {
        try {
            context.getSharedPreferences(PREFS_STREAMS, Context.MODE_PRIVATE)
                .edit().remove(KEY_RECENTS).apply()
        } catch (_: Exception) {}
    }

    fun serializeTrackList(tracks: List<Track>): String {
        val array = JSONArray()
        for (t in tracks) {
            array.put(
                JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("artist", t.artist)
                    put("durationSeconds", t.durationSeconds)
                    put("streamUrl", t.streamUrl)
                    put("codec", t.codec)
                    put("source", t.source)
                }
            )
        }
        return array.toString()
    }

    fun deserializeTrackList(jsonStr: String): List<Track> {
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<Track>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val streamUrl = obj.optString("streamUrl", "")
                if (streamUrl.isNotBlank()) {
                    list.add(
                        Track(
                            id = obj.optString("id", "stream_" + UUID.randomUUID().toString().take(8)),
                            title = obj.optString("title", "Live Stream"),
                            artist = obj.optString("artist", "Stream"),
                            durationSeconds = obj.optInt("durationSeconds", 0),
                            streamUrl = streamUrl,
                            codec = obj.optString("codec", extractCodecFromUrl(streamUrl)),
                            source = STREAM_SOURCE,
                            frequencyHz = 432
                        )
                    )
                }
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    // ==========================================
    // DISK CACHE PERSISTENCE
    // ==========================================

    fun saveToCache(cacheDir: File, playlistName: String, tracks: List<Track>) {
        try {
            val dir = File(cacheDir, CACHE_SUBDIR).apply { mkdirs() }
            val sanitized = playlistName.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(40)
            val file = File(dir, "${sanitized}.json")

            val jsonArray = JSONArray()
            for (t in tracks) {
                jsonArray.put(
                    JSONObject().apply {
                        put("id", t.id)
                        put("title", t.title)
                        put("artist", t.artist)
                        put("durationSeconds", t.durationSeconds)
                        put("streamUrl", t.streamUrl)
                        put("codec", t.codec)
                        put("source", t.source)
                    }
                )
            }
            val root = JSONObject().apply {
                put("playlistName", playlistName)
                put("updatedAt", System.currentTimeMillis())
                put("tracks", jsonArray)
            }
            file.writeText(root.toString())
        } catch (e: Exception) {
            // Non-critical cache write failure
        }
    }

    fun loadCachedPlaylists(cacheDir: File): Map<String, List<Track>> {
        val result = mutableMapOf<String, List<Track>>()
        val dir = File(cacheDir, CACHE_SUBDIR)
        if (!dir.exists()) return result

        val files = dir.listFiles { f -> f.extension == "json" } ?: return result
        for (file in files) {
            try {
                val json = JSONObject(file.readText())
                val name = json.optString("playlistName", file.nameWithoutExtension)
                val array = json.optJSONArray("tracks") ?: continue
                val trackList = mutableListOf<Track>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    trackList.add(
                        Track(
                            id = obj.optString("id", UUID.randomUUID().toString().take(12)),
                            title = obj.optString("title", "Unknown Stream"),
                            artist = obj.optString("artist", "Stream"),
                            durationSeconds = obj.optInt("durationSeconds", 0),
                            streamUrl = obj.optString("streamUrl", ""),
                            codec = obj.optString("codec", "STREAM"),
                            source = STREAM_SOURCE,
                            frequencyHz = 432
                        )
                    )
                }
                if (trackList.isNotEmpty()) {
                    result[name] = trackList
                }
            } catch (_: Exception) {
                // Ignore corrupt single cache file
            }
        }
        return result
    }
}
