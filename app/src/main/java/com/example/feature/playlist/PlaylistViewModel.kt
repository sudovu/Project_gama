package com.example.feature.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.media.PlaybackManager
import com.example.data.repository.MusicRepository
import com.example.domain.model.Playlist
import com.example.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PlaylistUiState {
    data object Loading : PlaylistUiState
    data class Success(
        val playlist: Playlist,
        val favoriteIds: Set<String>
    ) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
}

class PlaylistViewModel(
    private val playlistId: String,
    private val repository: MusicRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _rawState = MutableStateFlow<PlaylistUiState>(PlaylistUiState.Loading)

    val uiState: StateFlow<PlaylistUiState> = combine(
        _rawState,
        repository.getFavorites()
    ) { raw, favorites ->
        when (raw) {
            is PlaylistUiState.Success -> {
                raw.copy(favoriteIds = favorites.map { it.id }.toSet())
            }
            else -> raw
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistUiState.Loading
    )

    init {
        loadPlaylist()
    }

    fun loadPlaylist() {
        _rawState.value = PlaylistUiState.Loading
        viewModelScope.launch {
            repository.getPlaylist(playlistId).collect { result ->
                result.fold(
                    onSuccess = { playlist ->
                        _rawState.value = PlaylistUiState.Success(playlist = playlist, favoriteIds = emptySet())
                    },
                    onFailure = { error ->
                        _rawState.value = PlaylistUiState.Error(error.message ?: "Failed to load playlist")
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

    fun removeTrack(trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
            loadPlaylist()
        }
    }

    fun deletePlaylist(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            onComplete()
        }
    }

    companion object {
        fun provideFactory(
            playlistId: String,
            repository: MusicRepository,
            playbackManager: PlaybackManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlaylistViewModel(playlistId, repository, playbackManager) as T
            }
        }
    }
}
