package com.example.domain.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val albumTitle: String = "",
    val albumId: String = "",
    val durationSeconds: Int = 0,
    val artworkUrl: String = "",
    val youtubeVideoId: String = "",
    val streamUrl: String = "",
    val localAudioUri: String = "",
    val genre: String = "Electronic",
    val frequencyHz: Int = 432,
    val playCount: Long = 0L,
    val isFavorite: Boolean = false,
    val isUploaded: Boolean = false,
    val source: String = "curated",
    val isDownloaded: Boolean = false,
    val dailyRank: Int = 0,
    val chartTrend: String = ""
) {
    val formattedDuration: String
        get() {
            val minutes = durationSeconds / 60
            val seconds = durationSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Artist(
    val id: String,
    val name: String,
    val bio: String = "",
    val artworkUrl: String = "",
    val headerUrl: String = "",
    val genres: List<String> = emptyList(),
    val monthlyListeners: String = "",
    val topTracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList()
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val artworkUrl: String = "",
    val releaseYear: String = "",
    val genre: String = "",
    val trackCount: Int = 0,
    val tracks: List<Track> = emptyList()
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val artworkUrl: String = "",
    val trackCount: Int = 0,
    val isUserCreated: Boolean = false,
    val tracks: List<Track> = emptyList()
)

data class DiscoverFeed(
    val quickPicks: List<Track> = emptyList(),
    val trendingTracks: List<Track> = emptyList(),
    val uploadedTracks: List<Track> = emptyList(),
    val featuredPlaylists: List<Playlist> = emptyList(),
    val featuredAlbums: List<Album> = emptyList(),
    val featuredArtists: List<Artist> = emptyList(),
    val moodPlaylists: Map<String, List<Track>> = emptyMap()
)

data class SearchResults(
    val query: String = "",
    val tracks: List<Track> = emptyList(),
    val uploadedTracks: List<Track> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val searchScope: String = "ALL"
) {
    val isEmpty: Boolean
        get() = tracks.isEmpty() && uploadedTracks.isEmpty() && artists.isEmpty() && albums.isEmpty() && playlists.isEmpty()
}

enum class RepeatMode { OFF, ONE, ALL }

enum class TuningPreset(val displayName: String) {
    CUSTOM("Custom"),
    FLAT("Flat"),
    BASS_BOOST("Bass Boost"),
    VOCAL_CLARITY("Vocal & Clarity"),
    TREBLE_BOOST("Treble Boost"),
    LOFI_CHILL("Lo-Fi Warmth"),
    ELECTRONIC("Electronic / Dance"),
    SYNTHWAVE("Synthwave Smile"),
    ACOUSTIC("Acoustic / Warm")
}

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val label: String,
    val gainDb: Float = 0f
)

data class EqualizerSettings(
    val isEnabled: Boolean = true,
    val currentPreset: TuningPreset = TuningPreset.FLAT,
    val bands: List<EqualizerBand> = defaultBands(),
    val bassBoost: Float = 0f,
    val masterGain: Float = 1.0f
) {
    companion object {
        fun defaultBands(): List<EqualizerBand> = listOf(
            EqualizerBand(0, 60, "60 Hz", 0f),
            EqualizerBand(1, 230, "230 Hz", 0f),
            EqualizerBand(2, 910, "910 Hz", 0f),
            EqualizerBand(3, 3600, "3.6 kHz", 0f),
            EqualizerBand(4, 14000, "14 kHz", 0f)
        )

        fun presetGains(preset: TuningPreset): List<Float> = when (preset) {
            TuningPreset.CUSTOM -> listOf(0f, 0f, 0f, 0f, 0f)
            TuningPreset.FLAT -> listOf(0f, 0f, 0f, 0f, 0f)
            TuningPreset.BASS_BOOST -> listOf(7.5f, 4.5f, 0f, -1.5f, -2.5f)
            TuningPreset.VOCAL_CLARITY -> listOf(-3.0f, -1.0f, 4.5f, 7.0f, 3.0f)
            TuningPreset.TREBLE_BOOST -> listOf(-2.5f, 0f, 1.5f, 5.5f, 8.5f)
            TuningPreset.LOFI_CHILL -> listOf(4.5f, 6.0f, 2.0f, -3.5f, -7.5f)
            TuningPreset.ELECTRONIC -> listOf(6.0f, 3.0f, -1.0f, 3.5f, 5.0f)
            TuningPreset.SYNTHWAVE -> listOf(7.0f, 2.5f, -2.5f, 4.0f, 6.5f)
            TuningPreset.ACOUSTIC -> listOf(3.5f, 5.0f, 3.0f, 1.0f, -2.0f)
        }
    }
}

data class SmartQueueProfile(
    val dominantGenre: String = "All",
    val genreBreakdown: Map<String, Int> = emptyMap(),
    val totalAnalyzed: Int = 0
) {
    val displaySummary: String
        get() = if (totalAnalyzed > 0) {
            "$dominantGenre • Smart Queue Active"
        } else {
            "Listening Session Active"
        }
}

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = -1,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val visualizerBands: FloatArray = FloatArray(16) { 0.15f },
    val errorMessage: String? = null,
    val isSmartQueueEnabled: Boolean = true,
    val smartQueueProfile: SmartQueueProfile = SmartQueueProfile(),
    val playedTracksHistory: List<Track> = emptyList(),
    val equalizerSettings: EqualizerSettings = EqualizerSettings(),
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadingTrackId: String? = null,
    val sleepTimerRemainingSeconds: Long? = null,
    val sleepTimerInitialSeconds: Long? = null,
    val isSleepTimerActive: Boolean = false,
    val isSleepTimerEndOfTrack: Boolean = false,
    val playbackSpeed: Float = 1.0f
) {
    val progress: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val formattedPosition: String
        get() {
            val totalSeconds = (positionMs / 1000).toInt()
            val m = totalSeconds / 60
            val s = totalSeconds % 60
            return "%d:%02d".format(m, s)
        }

    val formattedDuration: String
        get() {
            val totalSeconds = (durationMs / 1000).toInt()
            val m = totalSeconds / 60
            val s = totalSeconds % 60
            return "%d:%02d".format(m, s)
        }

    val formattedSleepTimer: String
        get() {
            val secs = sleepTimerRemainingSeconds ?: 0L
            val m = secs / 60
            val s = secs % 60
            return "%d:%02d".format(m, s)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlaybackState

        if (currentTrack != other.currentTrack) return false
        if (isPlaying != other.isPlaying) return false
        if (isBuffering != other.isBuffering) return false
        if (positionMs != other.positionMs) return false
        if (durationMs != other.durationMs) return false
        if (queue != other.queue) return false
        if (queueIndex != other.queueIndex) return false
        if (isShuffle != other.isShuffle) return false
        if (repeatMode != other.repeatMode) return false
        if (!visualizerBands.contentEquals(other.visualizerBands)) return false
        if (errorMessage != other.errorMessage) return false
        if (isSmartQueueEnabled != other.isSmartQueueEnabled) return false
        if (smartQueueProfile != other.smartQueueProfile) return false
        if (playedTracksHistory != other.playedTracksHistory) return false
        if (equalizerSettings != other.equalizerSettings) return false
        if (isDownloading != other.isDownloading) return false
        if (downloadProgress != other.downloadProgress) return false
        if (downloadingTrackId != other.downloadingTrackId) return false
        if (sleepTimerRemainingSeconds != other.sleepTimerRemainingSeconds) return false
        if (sleepTimerInitialSeconds != other.sleepTimerInitialSeconds) return false
        if (isSleepTimerActive != other.isSleepTimerActive) return false
        if (isSleepTimerEndOfTrack != other.isSleepTimerEndOfTrack) return false
        if (playbackSpeed != other.playbackSpeed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = currentTrack?.hashCode() ?: 0
        result = 31 * result + isPlaying.hashCode()
        result = 31 * result + isBuffering.hashCode()
        result = 31 * result + positionMs.hashCode()
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + queue.hashCode()
        result = 31 * result + queueIndex
        result = 31 * result + isShuffle.hashCode()
        result = 31 * result + repeatMode.hashCode()
        result = 31 * result + visualizerBands.contentHashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + isSmartQueueEnabled.hashCode()
        result = 31 * result + smartQueueProfile.hashCode()
        result = 31 * result + playedTracksHistory.hashCode()
        result = 31 * result + equalizerSettings.hashCode()
        result = 31 * result + isDownloading.hashCode()
        result = 31 * result + downloadProgress.hashCode()
        result = 31 * result + (downloadingTrackId?.hashCode() ?: 0)
        result = 31 * result + (sleepTimerRemainingSeconds?.hashCode() ?: 0)
        result = 31 * result + (sleepTimerInitialSeconds?.hashCode() ?: 0)
        result = 31 * result + isSleepTimerActive.hashCode()
        result = 31 * result + isSleepTimerEndOfTrack.hashCode()
        result = 31 * result + playbackSpeed.hashCode()
        return result
    }
}
