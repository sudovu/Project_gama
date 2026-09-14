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

    fun isCollectionOrMix(title: String, durationSeconds: Int = 0): Boolean {
        // Single songs are typically 1.5 - 7.5 minutes (90s - 450s). Anything over 8 minutes (480s) is a collection/mix/album.
        if (durationSeconds > 480 || (durationSeconds in 1..70)) return true

        val t = title.lowercase()
        val collectionKeywords = listOf(
            "full album", "album mix", "playlist", "collection", "compilation",
            "jukebox", "non stop", "nonstop", "discography", "mashup",
            "1 hour", "2 hour", "3 hour", "hours", "mix 20", "video mix",
            "top 10", "top 20", "top 30", "top 40", "top 50", "top 100",
            "best songs of", "greatest hits mix", "vol.", "vol ", "volume ",
            "best of", "megamix"
        )
        if (collectionKeywords.any { t.contains(it) }) return true
        if (t.contains("mix") && (durationSeconds > 360 || durationSeconds == 0)) return true
        if (t.contains("greatest hits") && (durationSeconds > 360 || durationSeconds == 0)) return true
        return false
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
        if (isCollectionOrMix(candidate.title, candidate.durationSeconds)) {
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
        val singlePool = candidatePool.filter { !isCollectionOrMix(it.title, it.durationSeconds) }
        if (singlePool.isEmpty()) return emptyList()

        val profile = analyzePlayedTracks(playedTracks)
        val playedIds = playedTracks.map { it.id }.toSet()
        val currentQueueIds = currentQueue.map { it.id }.toSet()

        val scoredCandidates = singlePool
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
            val fallback = singlePool
                .filter { it.id != currentQueue.lastOrNull()?.id }
                .take(count)
                .map { it.copy(source = "smart_recommendation") }
            return fallback
        }

        return selected
    }

    /**
     * Generates endless recommendations strictly aligned with the target track's genre and artist.
     * Guaranteed to return high-quality single songs and never collections or mixes.
     */
    fun generateGenreRecommendations(
        targetTrack: Track?,
        playedTracks: List<Track>,
        candidatePool: List<Track>,
        currentQueue: List<Track>,
        count: Int = 5
    ): List<Track> {
        val singlePool = candidatePool.filter { !isCollectionOrMix(it.title, it.durationSeconds) }
        if (singlePool.isEmpty()) return emptyList()

        val activeGenre = targetTrack?.genre?.trim().orEmpty()
        val activeArtist = targetTrack?.artist?.trim().orEmpty()
        val currentQueueIds = currentQueue.map { it.id }.toSet()
        val currentVideoIds = currentQueue.mapNotNull { it.youtubeVideoId.takeIf { v -> v.isNotEmpty() } }.toSet()
        val playedIds = playedTracks.map { it.id }.toSet()

        // 1. Unplayed candidates matching the target genre or artist
        val genreMatches = singlePool
            .filter { candidate ->
                candidate.id !in currentQueueIds &&
                (candidate.youtubeVideoId.isEmpty() || candidate.youtubeVideoId !in currentVideoIds) &&
                (
                    (activeGenre.isNotEmpty() && (
                        candidate.genre.contains(activeGenre, ignoreCase = true) ||
                        activeGenre.contains(candidate.genre, ignoreCase = true)
                    )) ||
                    (activeArtist.isNotEmpty() && candidate.artist.equals(activeArtist, ignoreCase = true))
                )
            }
            .sortedBy { if (playedIds.contains(it.id)) 1 else 0 }

        if (genreMatches.isNotEmpty()) {
            return genreMatches.take(count).map { it.copy(source = "genre_recommendation") }
        }

        // 2. Fallback to standard smart profile recommendations
        val generalRecs = generateRecommendations(playedTracks, singlePool, currentQueue, count)
        if (generalRecs.isNotEmpty()) {
            return generalRecs
        }

        // 3. Absolute fallback: never allow empty return when candidate pool exists (loop/recycle single songs)
        return singlePool
            .filter { it.id != currentQueue.lastOrNull()?.id }
            .shuffled()
            .take(count)
            .map { it.copy(source = "genre_recommendation") }
    }
}
