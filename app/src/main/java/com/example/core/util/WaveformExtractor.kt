package com.example.core.util

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import com.example.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.sin

object WaveformExtractor {

    private const val BAR_COUNT = 100
    const val MAX_DISK_CACHE_BYTES = 50L * 1024L * 1024L // 50 MB
    const val TARGET_DISK_CACHE_BYTES = 40L * 1024L * 1024L // 40 MB
    private val memoryCache = ConcurrentHashMap<String, FloatArray>()

    suspend fun getWaveform(context: Context, track: Track): FloatArray = withContext(Dispatchers.Default) {
        val cacheKey = track.id.ifBlank { track.youtubeVideoId.ifBlank { track.title } }
        memoryCache[cacheKey]?.let { return@withContext it }

        // Check disk cache
        val diskCacheDir = File(context.cacheDir, "waveforms").apply { mkdirs() }
        val diskFile = File(diskCacheDir, "${cacheKey.replace(Regex("[^a-zA-Z0-9_-]"), "_")}.bin")
        if (diskFile.exists() && diskFile.length() == (BAR_COUNT * 4).toLong()) {
            try {
                val bytes = diskFile.readBytes()
                diskFile.setLastModified(System.currentTimeMillis())
                val buffer = ByteBuffer.wrap(bytes)
                val peaks = FloatArray(BAR_COUNT)
                for (i in 0 until BAR_COUNT) {
                    peaks[i] = buffer.float
                }
                memoryCache[cacheKey] = peaks
                return@withContext peaks
            } catch (_: Exception) {}
        }

        // Try extracting from local file if available
        val peaks: FloatArray = if (track.localAudioUri.isNotBlank()) {
            extractFromLocalAudio(track.localAudioUri) ?: generateRealisticWaveform(track)
        } else {
            generateRealisticWaveform(track)
        }

        // Save to memory and disk cache
        memoryCache[cacheKey] = peaks
        try {
            val buffer = ByteBuffer.allocate(BAR_COUNT * 4)
            for (p in peaks) {
                buffer.putFloat(p)
            }
            diskFile.writeBytes(buffer.array())
            diskFile.setLastModified(System.currentTimeMillis())
            trimDiskCacheIfNeeded(diskCacheDir)
        } catch (_: Exception) {}

        peaks
    }

    fun trimDiskCacheIfNeeded(dir: File, maxBytes: Long = MAX_DISK_CACHE_BYTES, targetBytes: Long = TARGET_DISK_CACHE_BYTES) {
        if (!dir.exists() || !dir.isDirectory) return
        val files = dir.listFiles { file -> file.extension == "bin" } ?: return
        var totalSize = files.sumOf { it.length() }
        if (totalSize > maxBytes) {
            val sorted = files.sortedBy { it.lastModified() }
            for (file in sorted) {
                if (totalSize <= targetBytes) break
                val len = file.length()
                if (file.delete()) {
                    totalSize -= len
                }
            }
        }
    }

    fun getDiskCacheSizeBytes(context: Context): Long {
        val diskCacheDir = File(context.cacheDir, "waveforms")
        if (!diskCacheDir.exists()) return 0L
        return diskCacheDir.listFiles { f -> f.extension == "bin" }?.sumOf { it.length() } ?: 0L
    }

    fun clearDiskCache(context: Context): Boolean {
        memoryCache.clear()
        val diskCacheDir = File(context.cacheDir, "waveforms")
        return diskCacheDir.deleteRecursively()
    }

    private fun extractFromLocalAudio(pathOrUri: String): FloatArray? {
        val extractor = MediaExtractor()
        return try {
            if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
                val uri = android.net.Uri.parse(pathOrUri)
                // Fallback to file extraction if scheme matches
                val file = File(uri.path ?: "")
                if (file.exists()) {
                    extractor.setDataSource(file.absolutePath)
                } else {
                    return null
                }
            } else {
                val file = File(pathOrUri)
                if (!file.exists()) return null
                extractor.setDataSource(file.absolutePath)
            }

            var audioTrackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    break
                }
            }

            if (audioTrackIndex < 0) return null
            extractor.selectTrack(audioTrackIndex)

            // Approximate sample peaks across file
            val peaks = FloatArray(BAR_COUNT) { 0.15f }
            val buffer = ByteBuffer.allocate(16384)
            var sampleIndex = 0

            while (extractor.readSampleData(buffer, 0) > 0 && sampleIndex < 1000) {
                val barIndex = (sampleIndex * BAR_COUNT / 1000).coerceIn(0, BAR_COUNT - 1)
                var maxSample = 0f
                val sampleSize = extractor.sampleSize.toInt()
                for (b in 0 until sampleSize.coerceAtMost(512) step 2) {
                    if (b + 1 < buffer.capacity()) {
                        val sample = abs(buffer.getShort(b).toFloat() / 32768f)
                        if (sample > maxSample) maxSample = sample
                    }
                }
                if (maxSample > peaks[barIndex]) {
                    peaks[barIndex] = maxSample.coerceIn(0.12f, 1.0f)
                }
                sampleIndex++
                extractor.advance()
            }

            // Normalize and smooth peaks
            normalizePeaks(peaks)
        } catch (_: Throwable) {
            null
        } finally {
            try {
                extractor.release()
            } catch (_: Throwable) {}
        }
    }

    /**
     * Generates a realistic, dynamic SoundCloud-style envelope curve based on the track's seed,
     * structure, and duration:
     * - Intro build-up (0-15%)
     * - Verse 1 rhythm (15-35%)
     * - Chorus 1 peak swell (35-50%)
     * - Bridge / breakdown (50-65%)
     * - Climactic Chorus 2 (65-85%)
     * - Outro fade-down (85-100%)
     */
    fun generateRealisticWaveform(track: Track): FloatArray {
        val peaks = FloatArray(BAR_COUNT)
        val seed = (track.id.hashCode() xor track.title.hashCode()).toLong()
        val random = java.util.Random(seed)

        for (i in 0 until BAR_COUNT) {
            val progress = i.toFloat() / BAR_COUNT

            // Macro song structure envelope
            val structure = when {
                progress < 0.12f -> 0.25f + (progress / 0.12f) * 0.45f // Intro ramp
                progress in 0.12f..0.32f -> 0.50f + 0.20f * sin(progress * 25f) // Verse 1
                progress in 0.32f..0.52f -> 0.75f + 0.22f * sin(progress * 30f) // Chorus 1 (dynamic high)
                progress in 0.52f..0.66f -> 0.40f + 0.15f * sin(progress * 15f) // Breakdown
                progress in 0.66f..0.86f -> 0.85f + 0.15f * sin(progress * 35f) // Climax Chorus 2
                else -> 0.70f * (1f - (progress - 0.86f) / 0.14f).coerceAtLeast(0.15f) // Outro
            }

            // Micro transients & rhythmic pulse
            val transient = (random.nextFloat() * 0.28f) - 0.14f
            val rhythmicBeat = if (i % 4 == 0) 0.18f else 0.0f

            val rawVal = (structure + transient + rhythmicBeat).coerceIn(0.12f, 1.0f)
            peaks[i] = rawVal
        }

        // Apply 3-point smoothing
        for (i in 1 until BAR_COUNT - 1) {
            peaks[i] = (peaks[i - 1] * 0.2f + peaks[i] * 0.6f + peaks[i + 1] * 0.2f).coerceIn(0.12f, 1.0f)
        }

        return peaks
    }

    private fun normalizePeaks(peaks: FloatArray): FloatArray {
        val max = peaks.maxOrNull()?.coerceAtLeast(0.1f) ?: 1.0f
        for (i in peaks.indices) {
            peaks[i] = ((peaks[i] / max) * 0.9f + 0.1f).coerceIn(0.12f, 1.0f)
        }
        return peaks
    }
}
