package com.example.data.provider

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DiscoverFeed
import com.example.domain.model.Playlist
import com.example.domain.model.SearchResults
import com.example.domain.model.Track

enum class ComplianceMode {
    OFFICIAL_IFRAME_EMBED,
    AUTHORIZED_DIRECT_STREAM,
    OFFLINE_SYNTHETIC
}

data class PlaybackCapability(
    val complianceMode: ComplianceMode,
    val canSeek: Boolean = true,
    val canShuffle: Boolean = true,
    val canRepeat: Boolean = true,
    val hasVisualFeedback: Boolean = true,
    val termsNotice: String = "Compliant with official YouTube terms via embedded IFrame player."
)

interface MusicProvider {
    val id: String
    val displayName: String
    suspend fun getDiscoverFeed(): Result<DiscoverFeed>
    suspend fun search(query: String): Result<SearchResults>
    suspend fun getTrack(trackId: String): Result<Track>
    suspend fun getArtist(artistId: String): Result<Artist>
    suspend fun getAlbum(albumId: String): Result<Album>
    suspend fun getPlaylist(playlistId: String): Result<Playlist>
    fun getPlaybackCapability(): PlaybackCapability
}
