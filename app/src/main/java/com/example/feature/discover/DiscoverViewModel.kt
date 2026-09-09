package com.example.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.media.PlaybackManager
import com.example.core.util.NetworkMonitor
import com.example.data.repository.MusicRepository
import com.example.domain.model.DiscoverFeed
import com.example.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DiscoverUiState {
    data object Loading : DiscoverUiState
    data class Success(
        val feed: DiscoverFeed,
        val recentTracks: List<Track>,
        val favoriteIds: Set<String>,
        val selectedMood: String = "All"
    ) : DiscoverUiState
    data class Error(val message: String) : DiscoverUiState
}

class DiscoverViewModel(
    private val repository: MusicRepository,
    private val playbackManager: PlaybackManager,
    private val networkMonitor: NetworkMonitor? = null
) : ViewModel() {

    private val _selectedMood = MutableStateFlow("All")
    val selectedMood: StateFlow<String> = _selectedMood.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor?.isOnline
        ?: MutableStateFlow(true).asStateFlow()

    private val _rawState = MutableStateFlow<DiscoverUiState>(DiscoverUiState.Loading)

    val uiState: StateFlow<DiscoverUiState> = combine(
        _rawState,
        repository.getFavorites(),
        repository.getRecentTracks(),
        _selectedMood
    ) { raw, favorites, recent, mood ->
        when (raw) {
            is DiscoverUiState.Success -> {
                val favIds = favorites.map { it.id }.toSet()
                raw.copy(
                    favoriteIds = favIds,
                    recentTracks = recent,
                    selectedMood = mood
                )
            }
            else -> raw
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DiscoverUiState.Loading
    )

    init {
        loadFeed()
    }

    fun loadFeed() {
        _rawState.value = DiscoverUiState.Loading
        viewModelScope.launch {
            repository.getDiscoverFeed().collect { result ->
                result.fold(
                    onSuccess = { feed ->
                        _rawState.value = DiscoverUiState.Success(
                            feed = feed,
                            recentTracks = emptyList(),
                            favoriteIds = emptySet()
                        )
                    },
                    onFailure = { error ->
                        _rawState.value = DiscoverUiState.Error(
                            error.message ?: "Failed to load music feed"
                        )
                    }
                )
            }
        }
    }

    fun selectMood(mood: String) {
        _selectedMood.value = mood
    }

    fun uploadTrack(
        title: String,
        artist: String,
        frequencyHz: Int,
        genre: String,
        youtubeUrlOrId: String,
        localAudioUri: String
    ) {
        viewModelScope.launch {
            val uploaded = repository.uploadTrack(
                title = title,
                artist = artist,
                frequencyHz = frequencyHz,
                genre = genre,
                youtubeUrlOrId = youtubeUrlOrId,
                localAudioUri = localAudioUri
            )
            // Immediately refresh feed so the uploaded track is 100% discoverable
            loadFeed()
        }
    }

    fun playTrack(track: Track, queue: List<Track>) {
        playbackManager.playTrack(track, queue)
        viewModelScope.launch {
            repository.recordRecent(track)
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track)
        }
    }

    companion object {
        fun provideFactory(
            repository: MusicRepository,
            playbackManager: PlaybackManager,
            networkMonitor: NetworkMonitor? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DiscoverViewModel(repository, playbackManager, networkMonitor) as T
            }
        }
    }
}
