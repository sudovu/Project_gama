package com.example.core.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File

data class ExtractedAudioMetadata(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val durationMs: Long = 0L,
    val codec: String = "",
    val bitrateKbps: Int = 0,
    val sampleRateHz: Int = 0,
    val bitDepth: Int = 16,
    val audioQualityBadge: String = "",
    val embeddedPicture: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExtractedAudioMetadata) return false
        return title == other.title &&
                artist == other.artist &&
                album == other.album &&
                durationMs == other.durationMs &&
                codec == other.codec &&
                bitrateKbps == other.bitrateKbps &&
                sampleRateHz == other.sampleRateHz &&
                bitDepth == other.bitDepth
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + codec.hashCode()
        result = 31 * result + bitrateKbps
        return result
    }
}

object AudioMetadataExtractor {

    fun extract(context: Context, uri: Uri): ExtractedAudioMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            parseRetriever(retriever, uri.lastPathSegment ?: "")
        } catch (_: Exception) {
            val fallbackName = uri.lastPathSegment?.substringAfterLast("/")?.substringBeforeLast(".") ?: "Local Track"
            ExtractedAudioMetadata(title = fallbackName, codec = inferCodecFromPath(uri.path ?: ""))
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    fun extractFromFile(file: File): ExtractedAudioMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            parseRetriever(retriever, file.name)
        } catch (_: Exception) {
            val fallbackName = file.nameWithoutExtension
            ExtractedAudioMetadata(title = fallbackName, codec = inferCodecFromPath(file.name))
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    private fun parseRetriever(retriever: MediaMetadataRetriever, fileName: String): ExtractedAudioMetadata {
        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.trim()
            ?.ifEmpty { null } ?: fileName.substringAfterLast("/").substringBeforeLast(".")
        val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)?.trim() ?: ""
        val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.trim() ?: ""
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        val bitrate = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0) / 1000
        val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: ""
        
        var sampleRate = 0
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 0
            } catch (_: Throwable) {}
        }

        val codec = formatCodec(mimeType, fileName)
        val bitDepth = if (codec.contains("FLAC") || codec.contains("WAV")) {
            if (bitrate > 1411 || sampleRate > 48000) 24 else 16
        } else {
            16
        }

        val picture = try {
            retriever.embeddedPicture
        } catch (_: Exception) {
            null
        }

        val badge = computeQualityBadge(codec, bitrate, sampleRate, bitDepth)

        return ExtractedAudioMetadata(
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            codec = codec,
            bitrateKbps = bitrate,
            sampleRateHz = sampleRate,
            bitDepth = bitDepth,
            audioQualityBadge = badge,
            embeddedPicture = picture
        )
    }

    fun computeQualityBadge(codec: String, bitrateKbps: Int, sampleRateHz: Int, bitDepth: Int): String {
        return when {
            codec.equals("FLAC", ignoreCase = true) -> {
                val sampleRateStr = if (sampleRateHz > 0) "${sampleRateHz / 1000}kHz" else "Lossless"
                "FLAC • ${bitDepth}-bit/$sampleRateStr"
            }
            codec.equals("WAV", ignoreCase = true) -> {
                val sampleRateStr = if (sampleRateHz > 0) "${sampleRateHz / 1000}kHz" else "PCM"
                "WAV • ${bitDepth}-bit/$sampleRateStr"
            }
            bitrateKbps >= 320 -> {
                "HQ AUDIO • ${bitrateKbps} kbps"
            }
            bitrateKbps in 192..319 -> {
                "$codec • ${bitrateKbps} kbps"
            }
            codec.isNotBlank() -> {
                if (bitrateKbps > 0) "$codec • ${bitrateKbps} kbps" else codec
            }
            else -> "STEREO 432Hz"
        }
    }

    private fun formatCodec(mimeType: String, fileName: String): String {
        return when {
            mimeType.contains("flac", ignoreCase = true) || fileName.endsWith(".flac", ignoreCase = true) -> "FLAC"
            mimeType.contains("wav", ignoreCase = true) || fileName.endsWith(".wav", ignoreCase = true) -> "WAV"
            mimeType.contains("ogg", ignoreCase = true) || fileName.endsWith(".ogg", ignoreCase = true) -> "OGG"
            mimeType.contains("mp4", ignoreCase = true) || fileName.endsWith(".m4a", ignoreCase = true) || fileName.endsWith(".aac", ignoreCase = true) -> "AAC"
            mimeType.contains("mpeg", ignoreCase = true) || fileName.endsWith(".mp3", ignoreCase = true) -> "MP3"
            mimeType.contains("opus", ignoreCase = true) || fileName.endsWith(".opus", ignoreCase = true) -> "OPUS"
            else -> inferCodecFromPath(fileName)
        }
    }

    private fun inferCodecFromPath(path: String): String {
        val ext = path.substringAfterLast(".", "").uppercase()
        return if (ext.isNotBlank() && ext.length in 2..4) ext else "AUDIO"
    }
}
