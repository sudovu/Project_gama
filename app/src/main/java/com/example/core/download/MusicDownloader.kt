package com.example.core.download

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) {

    companion object {
        private const val TAG = "MusicDownloader"
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

    fun isTrackDownloaded(trackId: String): Boolean {
        val file = localFiles[trackId] ?: getTrackFile(trackId)
        return file.exists() && file.length() > 1024L
    }

    fun getDownloadedFile(trackId: String): File? {
        val file = localFiles[trackId] ?: getTrackFile(trackId)
        return if (file.exists() && file.length() > 1024L) file else null
    }

    fun scanExistingDownloads(): Map<String, File> {
        val dir = getStorageDir()
        val files = dir.listFiles { _, name -> name.endsWith(".mp3") } ?: emptyArray()
        for (f in files) {
            val trackId = f.nameWithoutExtension
            if (f.length() > 1024L) {
                localFiles[trackId] = f
            }
        }
        return localFiles
    }

    suspend fun downloadTrack(
        track: Track,
        onProgress: (Float) -> Unit = {}
    ): Result<File> = withContext(Dispatchers.IO) {
        val targetFile = getTrackFile(track.id)

        if (targetFile.exists() && targetFile.length() > 1024L) {
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
                val err = "Download HTTP failure: " + response.code
                _downloadStatuses.update { it + (track.id to DownloadStatus.Failed(err)) }
                return@withContext Result.failure(Exception(err))
            }

            val body = response.body ?: throw Exception("Empty response body from server")
            val totalBytes = body.contentLength().coerceAtLeast(1L)

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

            if (tempFile.length() > 0) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
                localFiles[track.id] = targetFile
                _downloadStatuses.update { it + (track.id to DownloadStatus.Completed(targetFile)) }
                onProgress(1f)
                Log.d(TAG, "Track " + track.title + " successfully downloaded to " + targetFile.absolutePath)
                Result.success(targetFile)
            } else {
                tempFile.delete()
                val err = "Downloaded file was 0 bytes"
                _downloadStatuses.update { it + (track.id to DownloadStatus.Failed(err)) }
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for track " + track.id + ": " + e.message, e)
            _downloadStatuses.update { it + (track.id to DownloadStatus.Failed(e.message ?: "Download failed")) }
            Result.failure(e)
        }
    }

    private suspend fun resolveYouTubeMp3Url(videoId: String, onProgress: (Float) -> Unit): String = withContext(Dispatchers.IO) {
        val initUrl = "https://loader.to/ajax/download.php?format=mp3&url=https://www.youtube.com/watch?v=$videoId"
        val initRequest = Request.Builder()
            .url(initUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .build()

        val initResponse = client.newCall(initRequest).execute()
        if (!initResponse.isSuccessful) {
            throw Exception("MP3 converter service error: ${initResponse.code}")
        }

        val initBody = initResponse.body?.string() ?: throw Exception("Empty converter response")
        val initJson = JSONObject(initBody)
        val progressUrl = initJson.optString("progress_url")
        if (progressUrl.isBlank()) {
            val directUrl = initJson.optString("url")
            if (directUrl.isNotBlank()) return@withContext directUrl
            throw Exception("Could not initiate MP3 conversion")
        }

        for (i in 0 until 18) {
            delay(1500L)
            onProgress(0.08f + (i * 0.015f))

            val pollRequest = Request.Builder()
                .url(progressUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            try {
                val pollResponse = client.newCall(pollRequest).execute()
                if (pollResponse.isSuccessful) {
                    val pollBody = pollResponse.body?.string() ?: ""
                    if (pollBody.isNotEmpty()) {
                        val pollJson = JSONObject(pollBody)
                        val text = pollJson.optString("text")
                        if (text.contains("not supported", ignoreCase = true)) {
                            throw Exception(text)
                        }
                        val downloadUrl = pollJson.optString("download_url")
                        if (downloadUrl.isNotBlank()) {
                            return@withContext downloadUrl
                        }
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("not supported") == true) throw e
            }
        }

        throw Exception("Conversion timed out. Please try again.")
    }

    fun deleteDownload(trackId: String): Boolean {
        val file = localFiles[trackId] ?: getTrackFile(trackId)
        localFiles.remove(trackId)
        _downloadStatuses.update { it - trackId }
        return if (file.exists()) file.delete() else false
    }
}