package com.example.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.media.PlaybackManager
import com.example.core.util.NetworkMonitor
import com.example.data.repository.MusicRepository
import com.example.domain.model.SearchResults
import com.example.domain.model.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(
        val results: SearchResults,
        val favoriteIds: Set<String>
    ) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

enum class SearchScope(val displayName: String, val code: String) {
    ALL("All Tracks", "ALL"),
    YOUTUBE_MUSIC("Music Stream", "YOUTUBE_MUSIC"),
    YOUTUBE("Video Audio", "YOUTUBE"),
    UPLOADED("Uploaded", "UPLOADED")
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val repository: MusicRepository,
    private val playbackManager: PlaybackManager,
    private val networkMonitor: NetworkMonitor? = null
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _searchScope = MutableStateFlow(SearchScope.ALL)
    val searchScope: StateFlow<SearchScope> = _searchScope.asStateFlow()

    val isOnline: StateFlow<Boolean> = networkMonitor?.isOnline
        ?: MutableStateFlow(true).asStateFlow()

    val searchHistory: StateFlow<List<String>> = repository.getSearchHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchTrigger = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        _query.debounce(200).distinctUntilChanged(),
        _searchTrigger,
        _searchScope,
        repository.getFavorites()
    ) { debouncedQuery, immediateQuery, scope, favorites ->
        val effectiveQuery = immediateQuery.ifEmpty { debouncedQuery }
        Triple(effectiveQuery, scope, favorites.map { it.id }.toSet())
    }.flatMapLatest { (q, scope, favIds) ->
        if (q.isBlank()) {
            flowOf<SearchUiState>(SearchUiState.Idle)
        } else {
            flow<SearchUiState> {
                emit(SearchUiState.Loading)
                repository.searchWithScope(q, scope.code).collect { result ->
                    val state = result.fold(
                        onSuccess = { SearchUiState.Success(results = it, favoriteIds = favIds) },
                        onFailure = { SearchUiState.Error(it.message ?: "No songs found") }
                    )
                    emit(state)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState.Idle
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        _searchTrigger.value = ""
    }

    fun submitSearch(searchQuery: String) {
        val q = searchQuery.trim()
        if (q.isNotEmpty()) {
            _query.value = q
            _searchTrigger.value = q
            viewModelScope.launch {
                repository.addSearchQuery(q)
            }
        }
    }

    fun setSearchScope(scope: SearchScope) {
        _searchScope.value = scope
        if (_query.value.isNotBlank()) {
            _searchTrigger.value = _query.value
        }
    }

    fun deleteHistoryItem(item: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(item)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
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
                return SearchViewModel(repository, playbackManager, networkMonitor) as T
            }
        }
    }
}
