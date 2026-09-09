package com.example.core.media

import com.example.domain.model.SmartQueueProfile
import com.example.domain.model.Track
import kotlin.math.abs

object SmartQueueEngine {

    fun analyzePlayedTracks(playedTracks: List<Track>): SmartQueueProfile {
        if (playedTracks.isEmpty()) {
            return SmartQueueProfile(
                dominantGenre = "All",
                totalAnalyzed = 0
            )
        }

        val recentTracks = playedTracks.takeLast(20)
        val genreScoreMap = mutableMapOf<String, Int>()

        recentTracks.forEachIndexed { index, track ->
            val weight = 1 + (index * 3) / recentTracks.size.coerceAtLeast(1)
            val cleanGenre = track.genre.trim()
            if (cleanGenre.isNotEmpty()) {
                genreScoreMap[cleanGenre] = (genreScoreMap[cleanGenre] ?: 0) + weight
            }
        }

        val topGenre = genreScoreMap.maxByOrNull { it.value }?.key ?: playedTracks.last().genre

        return SmartQueueProfile(
            dominantGenre = topGenre,
            genreBreakdown = genreScoreMap,
            totalAnalyzed = playedTracks.size
        )
    }

    fun scoreCandidate(
        candidate: Track,
        profile: SmartQueueProfile,
        currentQueue: List<Track>,
        playedIds: Set<String>
    ): Double {
        if (currentQueue.any { it.id == candidate.id || (it.youtubeVideoId.isNotEmpty() && it.youtubeVideoId == candidate.youtubeVideoId) }) {
            return -1000.0
        }

        var score = 0.0

        val candidateGenre = candidate.genre.trim()
        if (candidateGenre.equals(profile.dominantGenre, ignoreCase = true)) {
            score += 100.0
        } else if (candidateGenre.contains(profile.dominantGenre, ignoreCase = true) ||
            profile.dominantGenre.contains(candidateGenre, ignoreCase = true)
        ) {
            score += 65.0
        } else {
            val genreMatchWeight = profile.genreBreakdown[candidateGenre]
            if (genreMatchWeight != null) {
                score += 40.0 + (genreMatchWeight * 5.0).coerceAtMost(25.0)
            } else {
                val candidateTokens = candidateGenre.lowercase().split(" ", "-", "/")
                val dominantTokens = profile.dominantGenre.lowercase().split(" ", "-", "/")
                if (candidateTokens.any { token -> token.length > 3 && dominantTokens.contains(token) }) {
                    score += 30.0
                }
            }
        }

        // Artist affinity boost if artist is in current queue
        if (currentQueue.any { it.artist.equals(candidate.artist, ignoreCase = true) }) {
            score += 25.0
        }

        if (playedIds.contains(candidate.id)) {
            score -= 40.0
        }

        val jitter = (abs(candidate.id.hashCode() % 100) / 20.0)
        score += jitter

        return score
    }

    fun generateRecommendations(
        playedTracks: List<Track>,
        candidatePool: List<Track>,
        currentQueue: List<Track>,
        count: Int = 3
    ): List<Track> {
        if (candidatePool.isEmpty()) return emptyList()

        val profile = analyzePlayedTracks(playedTracks)
        val playedIds = playedTracks.map { it.id }.toSet()
        val currentQueueIds = currentQueue.map { it.id }.toSet()

        val scoredCandidates = candidatePool
            .filter { candidate ->
                candidate.id !in currentQueueIds &&
                        (candidate.youtubeVideoId.isEmpty() || currentQueue.none { it.youtubeVideoId == candidate.youtubeVideoId })
            }
            .map { candidate ->
                val score = scoreCandidate(candidate, profile, currentQueue, playedIds)
                candidate to score
            }
            .filter { it.second > -500.0 }
            .sortedByDescending { it.second }

        val selected = scoredCandidates.take(count).map { (track, _) ->
            track.copy(source = "smart_recommendation")
        }

        if (selected.isEmpty()) {
            val fallback = candidatePool
                .filter { it.id != currentQueue.lastOrNull()?.id }
                .take(count)
                .map { it.copy(source = "smart_recommendation") }
            return fallback
        }

        return selected
    }
}
