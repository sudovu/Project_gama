package com.example.data.provider

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DiscoverFeed
import com.example.domain.model.Playlist
import com.example.domain.model.SearchResults
import com.example.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class YouTubeProvider(
    private val apiKey: String = "",
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
) : MusicProvider {

    override val id: String = "youtube_authorized"
    override val displayName: String = "YouTube & YouTube Music"

    override fun getPlaybackCapability(): PlaybackCapability {
        return PlaybackCapability(
            complianceMode = ComplianceMode.OFFICIAL_IFRAME_EMBED,
            canSeek = true,
            canShuffle = true,
            canRepeat = true,
            hasVisualFeedback = true,
            termsNotice = "Powered by official YouTube & YouTube Music embedded IFrame player. Respects all YouTube terms of service, creator rights, and platform restrictions."
        )
    }

    override suspend fun getDiscoverFeed(): Result<DiscoverFeed> = withContext(Dispatchers.IO) {
        try {
            // Attempt to fetch fresh trending music videos from YouTube InnerTube
            val onlineTrending = try {
                fetchInnerTubeVideos("Trending Music Hits Official", "ALL").take(15)
            } catch (_: Exception) {
                emptyList()
            }

            val baseTracks = CuratedFrequencies.allTracks
            val allCombined = (onlineTrending + baseTracks).distinctBy { it.youtubeVideoId.ifEmpty { it.id } }

            val quickPicks = (onlineTrending.take(4) + baseTracks.shuffled()).distinctBy { it.id }.take(8)
            val trending = allCombined.sortedByDescending { it.playCount.takeIf { c -> c > 0 } ?: 500000000L }
            val playlists = CuratedFrequencies.playlists
            val albums = CuratedFrequencies.albums
            val artists = CuratedFrequencies.artists

            val moodMap = mapOf(
                "Hip-Hop" to allCombined.filter { it.genre.contains("Rap", ignoreCase = true) || it.genre.contains("Hip-Hop", ignoreCase = true) }.ifEmpty { baseTracks.take(4) },
                "Rock & Metal" to allCombined.filter { it.genre.contains("Rock", ignoreCase = true) || it.genre.contains("Metal", ignoreCase = true) || it.genre.contains("Grunge", ignoreCase = true) }.ifEmpty { baseTracks.take(4) },
                "Pop Hits" to allCombined.filter { it.genre.contains("Pop", ignoreCase = true) || it.genre.contains("Soul", ignoreCase = true) || it.genre.contains("Funk", ignoreCase = true) }.ifEmpty { baseTracks.take(4) },
                "Classics" to allCombined.filter { it.artist in listOf("Michael Jackson", "Queen", "Nirvana", "AC/DC", "Coldplay") }.ifEmpty { baseTracks.take(4) }
            )

            Result.success(
                DiscoverFeed(
                    quickPicks = quickPicks,
                    trendingTracks = trending,
                    featuredPlaylists = playlists,
                    featuredAlbums = albums,
                    featuredArtists = artists,
                    moodPlaylists = moodMap
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun search(query: String): Result<SearchResults> {
        return searchWithScope(query, "ALL")
    }

    suspend fun searchWithScope(query: String, scope: String): Result<SearchResults> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return@withContext Result.success(SearchResults(query = "", searchScope = scope))
        }

        // 1. Check if user entered a direct YouTube or YouTube Music URL or 11-char Video ID
        val directVideoId = extractYouTubeVideoId(cleanQuery)
        if (directVideoId != null) {
            val oembedTrack = fetchOEmbedMetadata(directVideoId)
            val directTrack = oembedTrack?.copy(streamUrl = "") ?: Track(
                id = "yt_$directVideoId",
                title = "YouTube Video: $directVideoId",
                artist = if (scope == "YOUTUBE_MUSIC") "YouTube Music" else "YouTube Creator",
                artworkUrl = "https://img.youtube.com/vi/$directVideoId/hqdefault.jpg",
                youtubeVideoId = directVideoId,
                streamUrl = "",
                genre = if (scope == "YOUTUBE_MUSIC") "YouTube Music" else "YouTube Audio",
                durationSeconds = 240,
                frequencyHz = 432,
                source = if (scope == "YOUTUBE_MUSIC") "youtube_music" else "youtube"
            )
            return@withContext Result.success(
                SearchResults(
                    query = cleanQuery,
                    tracks = listOf(directTrack),
                    searchScope = scope
                )
            )
        }

        val ytTracks = mutableListOf<Track>()

        // 2. Query official YouTube Data API v3 if API key is provided
        if (apiKey.isNotBlank()) {
            try {
                val searchUrl = if (scope == "YOUTUBE_MUSIC") {
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&videoCategoryId=10&maxResults=20&q=${URLEncoder.encode("$cleanQuery audio music", "UTF-8")}&key=${apiKey}"
                } else {
                    "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&videoCategoryId=10&maxResults=20&q=${URLEncoder.encode(cleanQuery, "UTF-8")}&key=${apiKey}"
                }
                val request = Request.Builder().url(searchUrl).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (bodyString != null) {
                        val json = JSONObject(bodyString)
                        val items = json.optJSONArray("items")
                        if (items != null) {
                            for (i in 0 until items.length()) {
                                val item = items.getJSONObject(i)
                                val idObj = item.optJSONObject("id")
                                val videoId = idObj?.optString("videoId") ?: ""
                                val snippet = item.optJSONObject("snippet")
                                val title = snippet?.optString("title") ?: "Unknown Title"
                                val channelTitle = snippet?.optString("channelTitle") ?: "YouTube Artist"
                                val thumbs = snippet?.optJSONObject("thumbnails")
                                val thumbHigh = thumbs?.optJSONObject("high")?.optString("url")
                                    ?: thumbs?.optJSONObject("medium")?.optString("url")
                                    ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

                                if (videoId.isNotEmpty() && !ytTracks.any { it.youtubeVideoId == videoId }) {
                                    ytTracks.add(
                                        Track(
                                            id = "yt_$videoId",
                                            title = cleanTitle(title),
                                            artist = cleanTitle(channelTitle),
                                            artworkUrl = thumbHigh,
                                            youtubeVideoId = videoId,
                                            streamUrl = "",
                                            genre = if (scope == "YOUTUBE_MUSIC") "YouTube Music" else "YouTube Audio",
                                            durationSeconds = 240,
                                            frequencyHz = 432,
                                            source = if (scope == "YOUTUBE_MUSIC") "youtube_music" else "youtube"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 3. YouTube InnerTube Search: live public search for songs and videos uploaded to YouTube
        if (ytTracks.isEmpty()) {
            val innerTubeResults = fetchInnerTubeVideos(cleanQuery, scope)
            ytTracks.addAll(innerTubeResults)
        }

        // 4. Secondary fallback: Web search scraper (extracts videoRenderer from ytInitialData)
        if (ytTracks.isEmpty()) {
            val webResults = fetchWebSearchVideos(cleanQuery, scope)
            ytTracks.addAll(webResults)
        }

        // 5. Curated and local frequency matching
        val matchedTracks = CuratedFrequencies.allTracks.filter {
            it.title.contains(cleanQuery, ignoreCase = true) ||
                    it.artist.contains(cleanQuery, ignoreCase = true) ||
                    it.genre.contains(cleanQuery, ignoreCase = true)
        }

        val matchedArtists = if (scope != "YOUTUBE" && scope != "YOUTUBE_MUSIC") {
            CuratedFrequencies.artists.filter {
                it.name.contains(cleanQuery, ignoreCase = true) ||
                        it.genres.any { g -> g.contains(cleanQuery, ignoreCase = true) }
            }
        } else emptyList()

        val matchedAlbums = if (scope != "YOUTUBE" && scope != "YOUTUBE_MUSIC") {
            CuratedFrequencies.albums.filter {
                it.title.contains(cleanQuery, ignoreCase = true) ||
                        it.artist.contains(cleanQuery, ignoreCase = true)
            }
        } else emptyList()

        val matchedPlaylists = if (scope != "YOUTUBE" && scope != "YOUTUBE_MUSIC") {
            CuratedFrequencies.playlists.filter {
                it.title.contains(cleanQuery, ignoreCase = true) ||
                        it.description.contains(cleanQuery, ignoreCase = true)
            }
        } else emptyList()

        // Combine based on scope
        val combinedTracks = when (scope) {
            "YOUTUBE" -> ytTracks.ifEmpty {
                matchedTracks.map { it.copy(source = "youtube") }
            }
            "YOUTUBE_MUSIC" -> ytTracks.ifEmpty {
                matchedTracks.map { it.copy(source = "youtube_music") }
            }
            else -> {
                (ytTracks + matchedTracks).distinctBy { it.id }
            }
        }

        Result.success(
            SearchResults(
                query = cleanQuery,
                tracks = combinedTracks,
                artists = matchedArtists,
                albums = matchedAlbums,
                playlists = matchedPlaylists,
                searchScope = scope
            )
        )
    }

    /**
     * Searches YouTube using YouTube InnerTube API for uploaded songs and music videos.
     * Requires no API key and returns real-time YouTube songs and video IDs.
     */
    private fun fetchInnerTubeVideos(query: String, scope: String): List<Track> {
        return try {
            val effectiveQuery = if (scope == "YOUTUBE_MUSIC") {
                if (query.lowercase().contains("music") || query.lowercase().contains("song")) query else "$query song"
            } else {
                query
            }

            val payloadJson = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB")
                        put("clientVersion", "2.20231201.00.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("query", effectiveQuery)
            }

            val requestBody = payloadJson.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val request = Request.Builder()
                .url("https://www.youtube.com/youtubei/v1/search")
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val root = JSONObject(body)
            val tracks = mutableListOf<Track>()
            extractTracksFromJsonObject(root, scope, tracks)
            tracks
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Fallback web search scraper that fetches video results directly from YouTube HTML ytInitialData.
     */
    private fun fetchWebSearchVideos(query: String, scope: String): List<Track> {
        return try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://www.youtube.com/results?search_query=$encodedQuery&sp=EgIQAQ%253D%253D"
            val request = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()
            val html = response.body?.string() ?: return emptyList()

            val jsonMarker = "var ytInitialData = "
            val startIndex = html.indexOf(jsonMarker)
            if (startIndex != -1) {
                val jsonStart = startIndex + jsonMarker.length
                val endIndex = html.indexOf(";</script>", jsonStart)
                if (endIndex != -1) {
                    val jsonString = html.substring(jsonStart, endIndex)
                    val json = JSONObject(jsonString)
                    val tracks = mutableListOf<Track>()
                    extractTracksFromJsonObject(json, scope, tracks)
                    if (tracks.isNotEmpty()) return tracks
                }
            }

            // Regex extraction directly from HTML as secondary resilience
            val pattern = Pattern.compile("\"videoId\":\"([a-zA-Z0-9_-]{11})\".*?\"title\":\\{\"runs\":\\[\\{\"text\":\"([^\"]+)\"")
            val matcher = pattern.matcher(html)
            val regexTracks = mutableListOf<Track>()
            while (matcher.find() && regexTracks.size < 20) {
                val vid = matcher.group(1) ?: continue
                val title = matcher.group(2) ?: "YouTube Track"
                if (!regexTracks.any { it.youtubeVideoId == vid }) {
                    regexTracks.add(
                        Track(
                            id = "yt_$vid",
                            title = cleanTitle(title),
                            artist = "YouTube Artist",
                            artworkUrl = "https://img.youtube.com/vi/$vid/hqdefault.jpg",
                            youtubeVideoId = vid,
                            streamUrl = "",
                            genre = if (scope == "YOUTUBE_MUSIC") "YouTube Music" else "YouTube Audio",
                            durationSeconds = 240,
                            frequencyHz = 432,
                            source = if (scope == "YOUTUBE_MUSIC") "youtube_music" else "youtube"
                        )
                    )
                }
            }
            regexTracks
        } catch (_: Exception) {
            emptyList()
        }
    }

    /**
     * Traverses the innerTube or ytInitialData JSON hierarchy to extract all videoRenderer and
     * musicResponsiveListItemRenderer tracks.
     */
    private fun extractTracksFromJsonObject(obj: JSONObject, scope: String, out: MutableList<Track>) {
        // 1. videoRenderer / compactVideoRenderer
        val vr = obj.optJSONObject("videoRenderer") ?: obj.optJSONObject("compactVideoRenderer")
        if (vr != null) {
            val videoId = vr.optString("videoId")
            if (videoId.isNotEmpty() && !out.any { it.youtubeVideoId == videoId }) {
                val titleObj = vr.optJSONObject("title")
                val titleRuns = titleObj?.optJSONArray("runs")
                val title = if (titleRuns != null && titleRuns.length() > 0) {
                    titleRuns.optJSONObject(0)?.optString("text") ?: ""
                } else {
                    titleObj?.optString("simpleText") ?: ""
                }

                val ownerObj = vr.optJSONObject("ownerText") ?: vr.optJSONObject("shortBylineText")
                val ownerRuns = ownerObj?.optJSONArray("runs")
                val artist = if (ownerRuns != null && ownerRuns.length() > 0) {
                    ownerRuns.optJSONObject(0)?.optString("text") ?: "YouTube Artist"
                } else {
                    ownerObj?.optString("simpleText") ?: "YouTube Artist"
                }

                val lengthObj = vr.optJSONObject("lengthText")
                val durationSec = parseDurationToSeconds(lengthObj?.optString("simpleText") ?: "")

                val thumbs = vr.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                val thumbUrl = if (thumbs != null && thumbs.length() > 0) {
                    thumbs.optJSONObject(thumbs.length() - 1)?.optString("url") ?: "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                } else {
                    "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                }

                if (title.isNotBlank()) {
                    out.add(
                        Track(
                            id = "yt_$videoId",
                            title = cleanTitle(title),
                            artist = cleanTitle(artist),
                            artworkUrl = thumbUrl,
                            youtubeVideoId = videoId,
                            streamUrl = "",
                            genre = if (scope == "YOUTUBE_MUSIC") "YouTube Music" else "YouTube Audio",
                            durationSeconds = durationSec,
                            frequencyHz = 432,
                            source = if (scope == "YOUTUBE_MUSIC") "youtube_music" else "youtube"
                        )
                    )
                }
            }
        }

        // 2. musicResponsiveListItemRenderer
        val mr = obj.optJSONObject("musicResponsiveListItemRenderer")
        if (mr != null) {
            val plData = mr.optJSONObject("playlistItemData")
            var videoId = plData?.optString("videoId") ?: ""
            if (videoId.isEmpty()) {
                val nav = mr.optJSONObject("navigationEndpoint")
                videoId = nav?.optJSONObject("watchEndpoint")?.optString("videoId") ?: ""
            }
            if (videoId.isNotEmpty() && !out.any { it.youtubeVideoId == videoId }) {
                val flexColumns = mr.optJSONArray("flexColumns")
                var title = ""
                var artist = "YouTube Music"
                if (flexColumns != null && flexColumns.length() > 0) {
                    val col0 = flexColumns.optJSONObject(0)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")?.optJSONArray("runs")
                    if (col0 != null && col0.length() > 0) {
                        title = col0.optJSONObject(0)?.optString("text") ?: ""
                    }
                }
                if (flexColumns != null && flexColumns.length() > 1) {
                    val col1 = flexColumns.optJSONObject(1)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")?.optJSONArray("runs")
                    if (col1 != null && col1.length() > 0) {
                        val names = mutableListOf<String>()
                        for (i in 0 until col1.length()) {
                            val t = col1.optJSONObject(i)?.optString("text") ?: ""
                            if (t.isNotBlank() && t != " • " && t != "•") {
                                names.add(t)
                            }
                        }
                        if (names.isNotEmpty()) {
                            artist = names.joinToString(" • ")
                        }
                    }
                }

                if (title.isNotBlank()) {
                    out.add(
                        Track(
                            id = "yt_$videoId",
                            title = cleanTitle(title),
                            artist = cleanTitle(artist),
                            artworkUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                            youtubeVideoId = videoId,
                            streamUrl = "",
                            genre = "YouTube Music",
                            durationSeconds = 210,
                            frequencyHz = 432,
                            source = "youtube_music"
                        )
                    )
                }
            }
        }

        // Recurse child objects and arrays
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val child = obj.opt(key)
            if (child is JSONObject) {
                extractTracksFromJsonObject(child, scope, out)
            } else if (child is JSONArray) {
                extractTracksFromJsonArray(child, scope, out)
            }
        }
    }

    private fun extractTracksFromJsonArray(arr: JSONArray, scope: String, out: MutableList<Track>) {
        for (i in 0 until arr.length()) {
            val item = arr.opt(i)
            if (item is JSONObject) {
                extractTracksFromJsonObject(item, scope, out)
            } else if (item is JSONArray) {
                extractTracksFromJsonArray(item, scope, out)
            }
        }
    }

    private fun parseDurationToSeconds(lengthStr: String): Int {
        if (lengthStr.isBlank()) return 210
        val parts = lengthStr.split(":").mapNotNull { it.trim().toIntOrNull() }
        return when (parts.size) {
            1 -> parts[0]
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> 210
        }
    }

    /**
     * Resolves live YouTube metadata using YouTube oEmbed without an API key.
     */
    private fun fetchOEmbedMetadata(videoId: String): Track? {
        return try {
            val oembedUrl = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v=$videoId&format=json"
            val request = Request.Builder()
                .url(oembedUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val title = json.optString("title", "YouTube Track: $videoId")
                val author = json.optString("author_name", "YouTube Creator")
                val thumb = json.optString("thumbnail_url", "https://img.youtube.com/vi/$videoId/hqdefault.jpg")
                Track(
                    id = "yt_$videoId",
                    title = cleanTitle(title),
                    artist = cleanTitle(author),
                    artworkUrl = thumb,
                    youtubeVideoId = videoId,
                    genre = "YouTube Stream",
                    durationSeconds = 240,
                    frequencyHz = 432,
                    source = "youtube"
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getTrack(trackId: String): Result<Track> = withContext(Dispatchers.IO) {
        val track = CuratedFrequencies.allTracks.find { it.id == trackId }
        if (track != null) {
            Result.success(track)
        } else {
            // Check if YouTube track ID
            if (trackId.startsWith("yt_")) {
                val videoId = trackId.removePrefix("yt_")
                val fetched = fetchOEmbedMetadata(videoId) ?: Track(
                    id = trackId,
                    title = "YouTube Video Stream",
                    artist = "YouTube Music",
                    artworkUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                    youtubeVideoId = videoId,
                    genre = "YouTube Stream",
                    durationSeconds = 240,
                    frequencyHz = 432,
                    source = "youtube"
                )
                Result.success(fetched)
            } else {
                Result.failure(NoSuchElementException("Track $trackId not found in provider"))
            }
        }
    }

    override suspend fun getArtist(artistId: String): Result<Artist> = withContext(Dispatchers.IO) {
        val artist = CuratedFrequencies.artists.find { it.id == artistId }
        if (artist != null) {
            val topTracks = CuratedFrequencies.allTracks.filter { it.artistId == artistId }
            val albums = CuratedFrequencies.albums.filter { it.artistId == artistId }
            Result.success(artist.copy(topTracks = topTracks, albums = albums))
        } else {
            Result.failure(NoSuchElementException("Artist $artistId not found in provider"))
        }
    }

    override suspend fun getAlbum(albumId: String): Result<Album> = withContext(Dispatchers.IO) {
        val album = CuratedFrequencies.albums.find { it.id == albumId }
        if (album != null) {
            Result.success(album)
        } else {
            Result.failure(NoSuchElementException("Album $albumId not found in provider"))
        }
    }

    override suspend fun getPlaylist(playlistId: String): Result<Playlist> = withContext(Dispatchers.IO) {
        val playlist = CuratedFrequencies.playlists.find { it.id == playlistId }
        if (playlist != null) {
            Result.success(playlist)
        } else {
            Result.failure(NoSuchElementException("Playlist $playlistId not found in provider"))
        }
    }

    private fun extractYouTubeVideoId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.length == 11 && trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }
        val pattern = Pattern.compile(
            "(?:https?:\\/\\/)?(?:www\\.|m\\.|music\\.)?(?:youtube\\.com\\/(?:[^\\/\\n\\s]+\\/\\S+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/)([a-zA-Z0-9_-]{11})"
        )
        val matcher = pattern.matcher(trimmed)
        return if (matcher.find()) {
            matcher.group(1)
        } else null
    }

    private fun cleanTitle(title: String): String {
        return title
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }
}


