package com.example.core.di

import android.content.Context
import com.example.core.media.PlaybackManager
import com.example.core.util.NetworkMonitor
import com.example.data.local.GammaDatabase
import com.example.data.provider.MusicProvider
import com.example.data.provider.YouTubeProvider
import com.example.data.repository.MusicRepository

class GammaContainer(context: Context) {
    val database: GammaDatabase = GammaDatabase.getInstance(context)
    val networkMonitor: NetworkMonitor = NetworkMonitor(context)
    val musicProvider: MusicProvider = YouTubeProvider()
    val youtubeProvider: YouTubeProvider = musicProvider as YouTubeProvider
    val musicRepository: MusicRepository = MusicRepository(
        provider = musicProvider,
        favoriteDao = database.favoriteDao(),
        recentPlaybackDao = database.recentPlaybackDao(),
        playlistDao = database.playlistDao(),
        searchHistoryDao = database.searchHistoryDao(),
        uploadedTrackDao = database.uploadedTrackDao(),
        discoveryCacheDao = database.discoveryCacheDao()
    )
    val playbackManager: PlaybackManager = PlaybackManager(context)

    init {
        playbackManager.setCandidateProvider {
            musicRepository.getAllCandidateTracks()
        }
    }
}
