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

    fun getTrackMetaKey(track: Track): String {
        val normTitle = track.title
            .replace(Regex("\\s*[\\[\\(].*?[\\]\\)]"), "")
            .replace(Regex("[^a-zA-Z0-9]"), "")
            .lowercase()
            .trim()
        val normArtist = track.artist
            .replace(Regex("[^a-zA-Z0-9]"), "")
            .lowercase()
            .trim()
        return if (normTitle.isNotEmpty()) "${normArtist}_$normTitle" else ""
    }

    fun areTracksEqual(a: Track?, b: Track?): Boolean {
        if (a == null || b == null) return false
        if (a.id.isNotBlank() && a.id == b.id) return true
        if (a.youtubeVideoId.isNotBlank() && a.youtubeVideoId == b.youtubeVideoId) return true
        if (a.localAudioUri.isNotBlank() && a.localAudioUri == b.localAudioUri) return true
        val metaA = getTrackMetaKey(a)
        val metaB = getTrackMetaKey(b)
        return metaA.isNotEmpty() && metaA == metaB
    }

    fun deduplicateTracks(tracks: List<Track>): List<Track> {
        val seenIds = mutableSetOf<String>()
        val seenYtIds = mutableSetOf<String>()
        val seenUris = mutableSetOf<String>()
        val seenMetaKeys = mutableSetOf<String>()
        val result = ArrayList<Track>(tracks.size)

        for (t in tracks) {
            val id = t.id.trim()
            val ytId = t.youtubeVideoId.trim()
            val uri = t.localAudioUri.trim()
            val metaKey = getTrackMetaKey(t)

            val isDuplicate = (id.isNotEmpty() && id in seenIds) ||
                    (ytId.isNotEmpty() && ytId in seenYtIds) ||
                    (uri.isNotEmpty() && uri in seenUris) ||
                    (metaKey.isNotEmpty() && metaKey in seenMetaKeys)

            if (!isDuplicate) {
                if (id.isNotEmpty()) seenIds.add(id)
                if (ytId.isNotEmpty()) seenYtIds.add(ytId)
                if (uri.isNotEmpty()) seenUris.add(uri)
                if (metaKey.isNotEmpty()) seenMetaKeys.add(metaKey)
                result.add(t)
            }
        }
        return result
    }

    fun scoreCandidate(
        candidate: Track,
        profile: SmartQueueProfile,
        currentQueue: List<Track>,
        playedIds: Set<String>
    ): Double {
        val candidateMeta = getTrackMetaKey(candidate)
        if (currentQueue.any { 
            it.id == candidate.id || 
            (it.youtubeVideoId.isNotEmpty() && it.youtubeVideoId == candidate.youtubeVideoId) ||
            (it.localAudioUri.isNotEmpty() && it.localAudioUri == candidate.localAudioUri) ||
            (candidateMeta.isNotEmpty() && getTrackMetaKey(it) == candidateMeta)
        }) {
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
        val currentQueueIds = currentQueue.map { it.id }.filter { it.isNotEmpty() }.toSet()
        val currentVideoIds = currentQueue.map { it.youtubeVideoId }.filter { it.isNotEmpty() }.toSet()
        val currentMetaKeys = currentQueue.map { getTrackMetaKey(it) }.filter { it.isNotEmpty() }.toSet()

        fun isCandidateInQueue(c: Track): Boolean {
            val meta = getTrackMetaKey(c)
            return (c.id.isNotEmpty() && c.id in currentQueueIds) ||
                    (c.youtubeVideoId.isNotEmpty() && c.youtubeVideoId in currentVideoIds) ||
                    (meta.isNotEmpty() && meta in currentMetaKeys)
        }

        val scoredCandidates = singlePool
            .filter { candidate -> !isCandidateInQueue(candidate) }
            .map { candidate ->
                val score = scoreCandidate(candidate, profile, currentQueue, playedIds)
                candidate to score
            }
            .filter { it.second > -500.0 }
            .sortedByDescending { it.second }

        val selected = deduplicateTracks(scoredCandidates.take(count).map { (track, _) ->
            track.copy(source = "smart_recommendation")
        })

        if (selected.isEmpty()) {
            val unqueued = singlePool.filter { !isCandidateInQueue(it) }
            return deduplicateTracks(unqueued).take(count).map { it.copy(source = "smart_recommendation") }
        }

        return selected
    }

    /**
     * Generates endless recommendations strictly aligned with the target track's genre and artist.
     * Guaranteed to return high-quality single songs and never collections or mixes, and never repeat songs in the queue.
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

        val currentQueueIds = currentQueue.map { it.id }.filter { it.isNotEmpty() }.toSet()
        val currentVideoIds = currentQueue.map { it.youtubeVideoId }.filter { it.isNotEmpty() }.toSet()
        val currentUris = currentQueue.map { it.localAudioUri }.filter { it.isNotEmpty() }.toSet()
        val currentMetaKeys = currentQueue.map { getTrackMetaKey(it) }.filter { it.isNotEmpty() }.toSet()

        val playedIds = playedTracks.map { it.id }.filter { it.isNotEmpty() }.toSet()
        val playedVideoIds = playedTracks.map { it.youtubeVideoId }.filter { it.isNotEmpty() }.toSet()
        val playedMetaKeys = playedTracks.map { getTrackMetaKey(it) }.filter { it.isNotEmpty() }.toSet()

        fun isCandidateInQueue(c: Track): Boolean {
            val meta = getTrackMetaKey(c)
            return (c.id.isNotEmpty() && c.id in currentQueueIds) ||
                    (c.youtubeVideoId.isNotEmpty() && c.youtubeVideoId in currentVideoIds) ||
                    (c.localAudioUri.isNotEmpty() && c.localAudioUri in currentUris) ||
                    (meta.isNotEmpty() && meta in currentMetaKeys)
        }

        fun isCandidatePlayed(c: Track): Boolean {
            val meta = getTrackMetaKey(c)
            return (c.id.isNotEmpty() && c.id in playedIds) ||
                    (c.youtubeVideoId.isNotEmpty() && c.youtubeVideoId in playedVideoIds) ||
                    (meta.isNotEmpty() && meta in playedMetaKeys)
        }

        // 1. Unplayed candidates matching the target genre or artist NOT already in the queue
        val genreMatches = singlePool
            .filter { candidate ->
                !isCandidateInQueue(candidate) &&
                (
                    (activeGenre.isNotEmpty() && (
                        candidate.genre.contains(activeGenre, ignoreCase = true) ||
                        activeGenre.contains(candidate.genre, ignoreCase = true)
                    )) ||
                    (activeArtist.isNotEmpty() && candidate.artist.equals(activeArtist, ignoreCase = true))
                )
            }
            .sortedBy { if (isCandidatePlayed(it)) 1 else 0 }

        if (genreMatches.isNotEmpty()) {
            return deduplicateTracks(genreMatches).take(count).map { it.copy(source = "genre_recommendation") }
        }

        // 2. Fallback to standard smart profile recommendations NOT in queue
        val generalRecs = generateRecommendations(playedTracks, singlePool, currentQueue, count)
        if (generalRecs.isNotEmpty()) {
            return deduplicateTracks(generalRecs)
        }

        // 3. Fallback: only pick candidates that are NOT currently in the queue
        val unqueued = singlePool.filter { !isCandidateInQueue(it) }
        if (unqueued.isNotEmpty()) {
            return deduplicateTracks(unqueued).shuffled().take(count).map { it.copy(source = "genre_recommendation") }
        }

        // Strictly avoid repeating songs when every candidate is already in queue
        return emptyList()
    }
}
