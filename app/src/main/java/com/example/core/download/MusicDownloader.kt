package com.example.core.download

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(val progress: Float) : DownloadStatus()
    data class Completed(val file: File) : DownloadStatus()
    data class Failed(val error: String) : DownloadStatus()
}

class MusicDownloader(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
) {

    companion object {
        private const val TAG = "MusicDownloader"
        const val MIN_VALID_AUDIO_BYTES = 200_000L // Real audio tracks are >= 200 KB
    }

    private val _downloadStatuses = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val downloadStatuses: StateFlow<Map<String, DownloadStatus>> = _downloadStatuses.asStateFlow()

    private val localFiles = ConcurrentHashMap<String, File>()

    init {
        scanExistingDownloads()
    }

    private fun getStorageDir(): File {
        val external = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val dir = if (external != null && external.canWrite()) {
            File(external, "GAMA")
        } else {
            File(context.filesDir, "downloads")
        }
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getTrackFile(trackId: String): File {
        val sanitized = trackId.replace(Regex("[^a-zA-Z0-9_-]"), "_")
        return File(getStorageDir(), "$sanitized.mp3")
    }

    fun isValidAudioFile(file: File): Boolean {
        if (!file.exists() || file.length() < MIN_VALID_AUDIO_BYTES) return false
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(32)
                val read = input.read(header)
                if (read < 4) return false
                val headerStr = String(header, 0, read, Charsets.ISO_8859_1).lowercase()
                // Reject HTML / XML / JSON
                if (headerStr.startsWith("<!do") ||
                    headerStr.startsWith("<htm") ||
                    headerStr.startsWith("<?xm") ||
                    headerStr.startsWith("{") ||
                    headerStr.contains("<html") ||
                    headerStr.contains("cloudflare")
                ) {
                    return false
                }
                // Valid MP3 (ID3v2: 0x49 0x44 0x33 or MPEG sync word: 11 bits set 0xFF 0xEx/0xFx)
                if (header[0] == 0x49.toByte() && header[1] == 0x44.toByte() && header[2] == 0x33.toByte()) return true
                if ((header[0].toInt() and 0xFF) == 0xFF && (header[1].toInt() and 0xE0) == 0xE0) return true
                // Valid M4A / MP4 ("ftyp" starting at offset 4)
                if (read >= 8 && header[4] == 0x66.toByte() && header[5] == 0x74.toByte() && header[6] == 0x79.toByte() && header[7] == 0x70.toByte()) return true
                // Valid WebM / Matroska (0x1A, 0x45, 0xDF, 0xA3)
                if (header[0] == 0x1A.toByte() && header[1] == 0x45.toByte() && (header[2].toInt() and 0xFF) == 0xDF && (header[3].toInt() and 0xFF) == 0xA3) return true
                // Valid Ogg (OggS: 0x4F, 0x67, 0x67, 0x53)
                if (header[0] == 0x4F.toByte() && header[1] == 0x67.toByte() && header[2] == 0x67.toByte() && header[3] == 0x53.toByte()) return true

                // Generic binary audio if large enough and no HTML markers found
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isTrackDownloaded(trackId: String): Boolean {
        val file = localFiles[trackId] ?: getTrackFile(trackId)
        return isValidAudioFile(file)
    }

    fun getDownloadedFile(trackId: String): File? {
        val file = localFiles[trackId] ?: getTrackFile(trackId)
        return if (isValidAudioFile(file)) file else null
    }

    fun scanExistingDownloads(): Map<String, File> {
        val dir = getStorageDir()
        val files = dir.listFiles { _, name -> name.endsWith(".mp3") } ?: emptyArray()
        for (f in files) {
            val trackId = f.nameWithoutExtension
            if (isValidAudioFile(f)) {
                localFiles[trackId] = f
            } else {
                Log.w(TAG, "Deleting invalid/corrupt audio file: ${f.name} (${f.length()} bytes)")
                try {
                    f.delete()
                } catch (_: Exception) {}
                localFiles.remove(trackId)
            }
        }
        return localFiles
    }

    suspend fun downloadTrack(
        track: Track,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        val targetFile = getTrackFile(track.id)

        if (isValidAudioFile(targetFile)) {
            localFiles[track.id] = targetFile
            _downloadStatuses.update { it + (track.id to DownloadStatus.Completed(targetFile)) }
            onProgress(1f)
            return@withContext Result.success(targetFile)
        }

        _downloadStatuses.update { it + (track.id to DownloadStatus.Downloading(0.05f)) }
        onProgress(0.05f)

        try {
            val downloadUrl = if (track.streamUrl.isNotBlank()) {
                track.streamUrl
            } else if (track.youtubeVideoId.isNotBlank()) {
                resolveYouTubeMp3Url(track.youtubeVideoId) { p ->
                    _downloadStatuses.update { it + (track.id to DownloadStatus.Downloading(p)) }
                    onProgress(p)
                }
            } else {
                throw Exception("No valid audio source found for track: ${track.title}")
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = "Download HTTP failure: ${response.code}"
                _downloadStatuses.update { it + (track.id to DownloadStatus.Failed(err)) }
                return@withContext Result.failure(Exception(err))
            }

            val contentType = response.header("Content-Type", "")?.lowercase() ?: ""
            if (contentType.contains("text/html") ||
                contentType.contains("text/plain") ||
                contentType.contains("application/json") ||
                (!contentType.contains("audio") && !contentType.contains("octet-stream") && !contentType.contains("video"))
            ) {
                val err = "Server returned non-audio content: $contentType"
                Log.e(TAG, err)
                _downloadStatuses.update { it + (track.id to DownloadStatus.Failed(err)) }
                return@withContext Result.failure(Exception(err))
            }

            val body = response.body ?: throw Exception("Empty response body from server")
            val rawLength = body.contentLength()
            val totalBytes = if (rawLength > 10_000L) rawLength else (track.durationSeconds.toLong() * 16_000L).coerceAtLeast(1_500_000L)

            val tempFile = File(getStorageDir(), "${targetFile.name}.tmp")
            if (tempFile.exists()) tempFile.delete()

            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastReportedProgress = 0.35f

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val rawP = (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                        val progress = 0.35f + (rawP * 0.65f)
                        if (progress - lastReportedProgress >= 0.05f || progress >= 1f) {
                            lastReportedProgress = progress
                            _downloadStatuses.update { it + (track.id to DownloadStatus.Downloading(progress)) }
                            onProgress(progress)
                        }
                    }
                    output.flush()
                }
            }

            if (isValidAudioFile(tempFile)) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
                localFiles[track.id] = targetFile
                _downloadStatuses.update { it + (track.id to DownloadStatus.Completed(targetFile)) }
                onProgress(1f)
                Log.d(TAG, "Track ${track.title} successfully downloaded (${targetFile.length()} bytes)")
                Result.success(targetFile)
            } else {
                val byteCount = tempFile.length()
                tempFile.delete()
                val err = "Downloaded file was invalid or incomplete ($byteCount bytes)"
                Log.e(TAG, err)
                _downloadStatuses.update { it + (track.id to DownloadStatus.Failed(err)) }
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for track ${track.id}: ${e.message}", e)
            _downloadStatuses.update { it + (track.id to DownloadStatus.Failed(e.message ?: "Download failed")) }
            Result.failure(e)
        }
    }

    private suspend fun resolveYouTubeMp3Url(videoId: String, onProgress: (Float) -> Unit): String = withContext(Dispatchers.IO) {
        val invidiousInstances = listOf(
            "https://yt.chocolatemoo53.com",
            "https://invidious.nerdvpn.de",
            "https://inv.nadeko.net",
            "https://iv.ggtyler.dev",
            "https://yewtu.be",
            "https://invidious.jing.rocks"
        )

        // Engine 1: Invidious Adaptive Formats with Proxied Audio Stream
        for (instance in invidiousInstances) {
            try {
                onProgress(0.12f)
                val apiReq = Request.Builder()
                    .url("$instance/api/v1/videos/$videoId")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()

                val apiResp = client.newCall(apiReq).execute()
                if (apiResp.isSuccessful) {
                    val apiBody = apiResp.body?.string() ?: ""
                    if (apiBody.isNotEmpty()) {
                        val json = JSONObject(apiBody)
                        val formats = json.optJSONArray("adaptiveFormats")
                        if (formats != null && formats.length() > 0) {
                            var bestUrl: String? = null
                            var bestIsMp4 = false
                            var bestBitrate = 0

                            for (i in 0 until formats.length()) {
                                val fmt = formats.optJSONObject(i) ?: continue
                                val type = fmt.optString("type", "")
                                if (type.startsWith("audio/")) {
                                    val streamUrl = fmt.optString("url")
                                    val bitrate = fmt.optInt("bitrate", 0)
                                    val isMp4 = type.contains("mp4")
                                    if (streamUrl.isNotBlank()) {
                                        if (bestUrl == null || (isMp4 && !bestIsMp4) || (isMp4 == bestIsMp4 && bitrate > bestBitrate)) {
                                            bestUrl = streamUrl
                                            bestIsMp4 = isMp4
                                            bestBitrate = bitrate
                                        }
                                    }
                                }
                            }

                            if (bestUrl != null) {
                                // Proxy the stream through the Invidious instance to avoid 403 IP binding
                                val uri = Uri.parse(bestUrl)
                                val proxiedUrl = "$instance${uri.path}?${uri.query}"

                                // Test proxied stream via quick HEAD request
                                val testReq = Request.Builder()
                                    .url(proxiedUrl)
                                    .head()
                                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                    .build()

                                val testResp = client.newCall(testReq).execute()
                                val ct = testResp.header("Content-Type", "")?.lowercase() ?: ""
                                if (testResp.isSuccessful && !ct.contains("text/html") && (ct.contains("audio") || ct.contains("octet-stream") || ct.contains("video"))) {
                                    Log.d(TAG, "Resolved working proxied audio stream via $instance: $ct")
                                    onProgress(0.25f)
                                    return@withContext proxiedUrl
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Invidious stream extraction on $instance failed: ${e.message}")
            }
        }

        // Engine 2: Verified direct itag=140 stream (only if Content-Type is verified audio)
        for (instance in invidiousInstances) {
            try {
                onProgress(0.18f)
                val directAudioUrl = "$instance/latest_version?id=$videoId&itag=140"
                val testReq = Request.Builder()
                    .url(directAudioUrl)
                    .head()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()

                val testResp = client.newCall(testReq).execute()
                val ct = testResp.header("Content-Type", "")?.lowercase() ?: ""
                if (testResp.isSuccessful && !ct.contains("text/html") && (ct.contains("audio") || ct.contains("octet-stream"))) {
                    val streamUrl = testResp.header("Location") ?: directAudioUrl
                    Log.d(TAG, "Resolved verified direct audio via $instance: $streamUrl")
                    onProgress(0.25f)
                    return@withContext streamUrl
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct itag probe on $instance failed: ${e.message}")
            }
        }

        throw Exception("Could not find a valid audio stream for this track. Please check network and try again.")
    }

    fun deleteDownload(trackId: String): Boolean {
        val file = localFiles[trackId] ?: getTrackFile(trackId)
        localFiles.remove(trackId)
        _downloadStatuses.update { it - trackId }
        return if (file.exists()) file.delete() else false
    }
}
