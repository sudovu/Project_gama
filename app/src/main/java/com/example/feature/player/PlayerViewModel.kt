package com.example.feature.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.media.PlaybackManager
import com.example.data.repository.MusicRepository
import com.example.domain.model.PlaybackState
import com.example.domain.model.Track
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class PlayerSheetType {
    NONE,
    QUEUE,
    SPECS,
    EQUALIZER
}

data class PlayerUiState(
    val playbackState: PlaybackState = PlaybackState(),
    val isFavorite: Boolean = false,
    val sheetType: PlayerSheetType = PlayerSheetType.NONE
)

class PlayerViewModel(
    val playbackManager: PlaybackManager,
    private val repository: MusicRepository
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = combine(
        playbackManager.playbackState,
        repository.getFavorites()
    ) { state, favorites ->
        val track = state.currentTrack
        val isFav = if (track != null) favorites.any { it.id == track.id } else false
        PlayerUiState(
            playbackState = state,
            isFavorite = isFav
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    fun togglePlayPause() = playbackManager.togglePlayPause()

    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)

    fun skipNext() = playbackManager.skipNext()

    fun skipPrevious() = playbackManager.skipPrevious()

    fun toggleShuffle() = playbackManager.toggleShuffle()

    fun cycleRepeatMode() = playbackManager.cycleRepeatMode()

    fun removeFromQueue(index: Int) = playbackManager.removeFromQueue(index)

    fun clearQueue() = playbackManager.clearQueue()

    fun toggleSmartQueue(enabled: Boolean? = null) = playbackManager.toggleSmartQueue(enabled)

    fun appendSmartRecommendations(count: Int = 3) = playbackManager.appendSmartRecommendations(count)

    fun setEqualizerBand(index: Int, gainDb: Float) = playbackManager.setEqualizerBand(index, gainDb)

    fun setTuningPreset(preset: com.example.domain.model.TuningPreset) = playbackManager.setTuningPreset(preset)

    fun toggleEqualizer(enabled: Boolean? = null) = playbackManager.toggleEqualizer(enabled)

    fun setMasterGain(gain: Float) = playbackManager.setMasterGain(gain)

    fun jumpToQueueIndex(index: Int) {
        val q = playbackManager.playbackState.value.queue
        if (index in q.indices) {
            playbackManager.playTrack(q[index], q)
        }
    }

    fun toggleFavorite() {
        val current = playbackManager.playbackState.value.currentTrack ?: return
        viewModelScope.launch {
            repository.toggleFavorite(current)
        }
    }

    fun downloadCurrentTrack() {
        val current = playbackManager.playbackState.value.currentTrack ?: return
        playbackManager.downloadTrack(current)
    }

    fun downloadTrack(track: Track) {
        playbackManager.downloadTrack(track)
    }

    companion object {
        fun provideFactory(
            playbackManager: PlaybackManager,
            repository: MusicRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerViewModel(playbackManager, repository) as T
            }
        }
    }
}
