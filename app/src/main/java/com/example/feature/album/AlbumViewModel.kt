package com.example.feature.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.media.PlaybackManager
import com.example.data.repository.MusicRepository
import com.example.domain.model.Album
import com.example.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AlbumUiState {
    data object Loading : AlbumUiState
    data class Success(
        val album: Album,
        val favoriteIds: Set<String>
    ) : AlbumUiState
    data class Error(val message: String) : AlbumUiState
}

class AlbumViewModel(
    private val albumId: String,
    private val repository: MusicRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _rawState = MutableStateFlow<AlbumUiState>(AlbumUiState.Loading)

    val uiState: StateFlow<AlbumUiState> = combine(
        _rawState,
        repository.getFavorites()
    ) { raw, favorites ->
        when (raw) {
            is AlbumUiState.Success -> {
                raw.copy(favoriteIds = favorites.map { it.id }.toSet())
            }
            else -> raw
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlbumUiState.Loading
    )

    init {
        loadAlbum()
    }

    fun loadAlbum() {
        _rawState.value = AlbumUiState.Loading
        viewModelScope.launch {
            repository.getAlbum(albumId).collect { result ->
                result.fold(
                    onSuccess = { album ->
                        _rawState.value = AlbumUiState.Success(album = album, favoriteIds = emptySet())
                    },
                    onFailure = { error ->
                        _rawState.value = AlbumUiState.Error(error.message ?: "Failed to load album")
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
            albumId: String,
            repository: MusicRepository,
            playbackManager: PlaybackManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AlbumViewModel(albumId, repository, playbackManager) as T
            }
        }
    }
}
