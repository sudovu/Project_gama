package com.example.feature.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.media.PlaybackManager
import com.example.data.repository.MusicRepository
import com.example.domain.model.Artist
import com.example.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ArtistUiState {
    data object Loading : ArtistUiState
    data class Success(
        val artist: Artist,
        val favoriteIds: Set<String>
    ) : ArtistUiState
    data class Error(val message: String) : ArtistUiState
}

class ArtistViewModel(
    private val artistId: String,
    private val repository: MusicRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _rawState = MutableStateFlow<ArtistUiState>(ArtistUiState.Loading)

    val uiState: StateFlow<ArtistUiState> = combine(
        _rawState,
        repository.getFavorites()
    ) { raw, favorites ->
        when (raw) {
            is ArtistUiState.Success -> {
                raw.copy(favoriteIds = favorites.map { it.id }.toSet())
            }
            else -> raw
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArtistUiState.Loading
    )

    init {
        loadArtist()
    }

    fun loadArtist() {
        _rawState.value = ArtistUiState.Loading
        viewModelScope.launch {
            repository.getArtist(artistId).collect { result ->
                result.fold(
                    onSuccess = { artist ->
                        _rawState.value = ArtistUiState.Success(artist = artist, favoriteIds = emptySet())
                    },
                    onFailure = { error ->
                        _rawState.value = ArtistUiState.Error(error.message ?: "Failed to load artist")
                    }
                )
            }
        }
    }

    fun playAll(tracks: List<Track>, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        val queue = if (shuffle) tracks.shuffled() else tracks
        playbackManager.playTrack(queue.first(), queue)
        viewModelScope.launch {
            repository.recordPlayback(queue.first())
        }
    }

    fun playTrack(track: Track, queue: List<Track>) {
        playbackManager.playTrack(track, queue)
        viewModelScope.launch {
            repository.recordPlayback(track)
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track)
        }
    }

    companion object {
        fun provideFactory(
            artistId: String,
            repository: MusicRepository,
            playbackManager: PlaybackManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ArtistViewModel(artistId, repository, playbackManager) as T
            }
        }
    }
}
