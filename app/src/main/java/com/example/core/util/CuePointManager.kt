package com.example.core.util

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class CuePoint(
    val id: String = "cue_" + java.util.UUID.randomUUID().toString().take(8),
    val trackId: String,
    val positionMs: Long,
    val label: String
) {
    val timestampMs: Long
        get() = positionMs
}

/**
 * Manages persistent cue points dropped by the user on tracks for precise navigation.
 */
object CuePointManager {

    private const val PREFS_CUES = "gama_cue_points_store"

    fun formatTimestamp(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        val s = totalSec % 60
        return if (h > 0) {
            String.format("%02d:%02d:%02d", h, m, s)
        } else {
            String.format("%02d:%02d", m, s)
        }
    }

    fun getCuePoints(context: Context, trackId: String): List<CuePoint> {
        if (trackId.isBlank()) return emptyList()
        return try {
            val prefs = context.getSharedPreferences(PREFS_CUES, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString("cues_$trackId", null) ?: return emptyList()
            deserializeCuePoints(jsonStr)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addCuePoint(context: Context, trackId: String, positionMs: Long, label: String): CuePoint {
        val cue = CuePoint(
            id = "cue_" + java.util.UUID.randomUUID().toString().take(8),
            trackId = trackId,
            positionMs = positionMs,
            label = label.ifBlank { "Cue @ ${formatTimestamp(positionMs)}" }
        )
        try {
            val prefs = context.getSharedPreferences(PREFS_CUES, Context.MODE_PRIVATE)
            val current = getCuePoints(context, trackId).toMutableList()
            current.add(cue)
            current.sortBy { it.positionMs }
            prefs.edit().putString("cues_$trackId", serializeCuePoints(current)).apply()
        } catch (_: Exception) {}
        return cue
    }

    fun deleteCuePoint(context: Context, trackId: String, cueId: String) {
        try {
            val prefs = context.getSharedPreferences(PREFS_CUES, Context.MODE_PRIVATE)
            val current = getCuePoints(context, trackId).filterNot { it.id == cueId }
            prefs.edit().putString("cues_$trackId", serializeCuePoints(current)).apply()
        } catch (_: Exception) {}
    }

    fun serializeCuePoints(cues: List<CuePoint>): String {
        val array = JSONArray()
        for (c in cues) {
            array.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("trackId", c.trackId)
                    put("positionMs", c.positionMs)
                    put("label", c.label)
                }
            )
        }
        return array.toString()
    }

    fun deserializeCuePoints(jsonStr: String): List<CuePoint> {
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<CuePoint>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CuePoint(
                        id = obj.optString("id", UUID.randomUUID().toString().take(8)),
                        trackId = obj.optString("trackId", ""),
                        positionMs = obj.optLong("positionMs", 0L),
                        label = obj.optString("label", "Cue")
                    )
                )
            }
            list.sortedBy { it.positionMs }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun formatCueTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format("%d:%02d", m, s)
    }
}
