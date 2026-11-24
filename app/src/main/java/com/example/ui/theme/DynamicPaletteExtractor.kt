package com.example.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Palette extraction utility.
 * Note: Under the Strict Color Theme Lock rule, automatic artwork-driven theme shifts during playback
 * are prohibited. Themes and color harmonics change strictly on explicit user selection.
 */
object DynamicPaletteExtractor {

    fun extractDominantColor(bitmap: Bitmap?): Color? {
        if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) return null

        return try {
            val scaled = Bitmap.createScaledBitmap(bitmap, 32, 32, false)
            val pixels = IntArray(32 * 32)
            scaled.getPixels(pixels, 0, 32, 0, 0, 32, 32)

            val hsv = FloatArray(3)
            val buckets = Array(12) { mutableListOf<Int>() }

            for (pixel in pixels) {
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF

                android.graphics.Color.RGBToHSV(r, g, b, hsv)
                val sat = hsv[1]
                val value = hsv[2]

                // Filter out extreme darks and washed-out grayscale
                val isTooDark = value < 0.20f
                val isWashedOut = sat < 0.20f
                if (!isTooDark && !isWashedOut) {
                    val hueBin = ((hsv[0] % 360f) / 30f).toInt().coerceIn(0, 11)
                    buckets[hueBin].add(pixel)
                }
            }

            var bestBucket: List<Int>? = null
            var bestScore = -1f

            for (bucket in buckets) {
                if (bucket.isEmpty()) continue
                var totalSat = 0f
                var totalVal = 0f
                for (p in bucket) {
                    val r = (p shr 16) and 0xFF
                    val g = (p shr 8) and 0xFF
                    val b = p and 0xFF
                    android.graphics.Color.RGBToHSV(r, g, b, hsv)
                    totalSat += hsv[1]
                    totalVal += hsv[2]
                }
                val avgSat = totalSat / bucket.size
                val avgVal = totalVal / bucket.size
                val score = bucket.size * avgSat * avgVal
                if (score > bestScore) {
                    bestScore = score
                    bestBucket = bucket
                }
            }

            val winner = bestBucket
            if (winner != null && winner.isNotEmpty()) {
                var sumR = 0L
                var sumG = 0L
                var sumB = 0L
                for (p in winner) {
                    sumR += (p shr 16) and 0xFF
                    sumG += (p shr 8) and 0xFF
                    sumB += p and 0xFF
                }
                val avgR = (sumR / winner.size).toInt().coerceIn(0, 255)
                val avgG = (sumG / winner.size).toInt().coerceIn(0, 255)
                val avgB = (sumB / winner.size).toInt().coerceIn(0, 255)
                Color(avgR, avgG, avgB)
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun extractFromUrlOrPath(context: Context, source: String): Color? = withContext(Dispatchers.IO) {
        if (source.isBlank()) return@withContext null

        try {
            val bitmap: Bitmap? = if (source.startsWith("http://") || source.startsWith("https://")) {
                val url = URL(source)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                connection.doInput = true
                try {
                    connection.connect()
                    connection.inputStream.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } finally {
                    try { connection.disconnect() } catch (_: Throwable) {}
                }
            } else if (source.startsWith("content://") || source.startsWith("file://")) {
                val uri = android.net.Uri.parse(source)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } else {
                val file = java.io.File(source)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else null
            }

            extractDominantColor(bitmap)
        } catch (_: Throwable) {
            null
        }
    }
}
