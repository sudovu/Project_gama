package com.example.data.provider

import com.example.domain.model.Album
import com.example.domain.model.Artist
import com.example.domain.model.DiscoverFeed
import com.example.domain.model.Playlist
import com.example.domain.model.SearchResults
import com.example.domain.model.Track

class MockMusicProvider : MusicProvider {
    override val id: String = "mock_frequency"
    override val displayName: String = "GAMA Offline Catalogue"

    override fun getPlaybackCapability(): PlaybackCapability {
        return PlaybackCapability(
            complianceMode = ComplianceMode.OFFLINE_SYNTHETIC,
            canSeek = true,
            canShuffle = true,
            canRepeat = true,
            hasVisualFeedback = true,
            termsNotice = "Authentic offline hit songs catalogue."
        )
    }

    override suspend fun getDiscoverFeed(): Result<DiscoverFeed> {
        return Result.success(
            DiscoverFeed(
                quickPicks = CuratedFrequencies.allTracks.take(4),
                trendingTracks = CuratedFrequencies.allTracks,
                featuredPlaylists = CuratedFrequencies.playlists,
                featuredAlbums = CuratedFrequencies.albums,
                featuredArtists = CuratedFrequencies.artists,
                moodPlaylists = emptyMap()
            )
        )
    }

    override suspend fun search(query: String): Result<SearchResults> {
        val tracks = CuratedFrequencies.allTracks.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
        val artists = CuratedFrequencies.artists.filter { it.name.contains(query, ignoreCase = true) }
        val albums = CuratedFrequencies.albums.filter { it.title.contains(query, ignoreCase = true) }
        val playlists = CuratedFrequencies.playlists.filter { it.title.contains(query, ignoreCase = true) }

        return Result.success(
            SearchResults(
                query = query,
                tracks = tracks,
                artists = artists,
                albums = albums,
                playlists = playlists
            )
        )
    }

    override suspend fun getTrack(trackId: String): Result<Track> {
        val trk = CuratedFrequencies.allTracks.find { it.id == trackId }
        return if (trk != null) Result.success(trk) else Result.failure(NoSuchElementException("Track not found"))
    }

    override suspend fun getArtist(artistId: String): Result<Artist> {
        val art = CuratedFrequencies.artists.find { it.id == artistId }
        return if (art != null) Result.success(art) else Result.failure(NoSuchElementException("Artist not found"))
    }

    override suspend fun getAlbum(albumId: String): Result<Album> {
        val alb = CuratedFrequencies.albums.find { it.id == albumId }
        return if (alb != null) Result.success(alb) else Result.failure(NoSuchElementException("Album not found"))
    }

    override suspend fun getPlaylist(playlistId: String): Result<Playlist> {
        val pl = CuratedFrequencies.playlists.find { it.id == playlistId }
        return if (pl != null) Result.success(pl) else Result.failure(NoSuchElementException("Playlist not found"))
    }
}
