package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.di.GammaContainer
import com.example.core.media.CompliantYouTubeHost
import com.example.core.ui.GammaMiniPlayer
import com.example.feature.album.AlbumScreen
import com.example.feature.album.AlbumViewModel
import com.example.feature.artist.ArtistScreen
import com.example.feature.artist.ArtistViewModel
import com.example.feature.discover.DiscoverScreen
import com.example.feature.discover.DiscoverViewModel
import com.example.feature.library.LibraryScreen
import com.example.feature.library.LibraryViewModel
import com.example.feature.player.GammaPlayerScreen
import com.example.feature.player.PlayerViewModel
import com.example.feature.playlist.PlaylistScreen
import com.example.feature.playlist.PlaylistViewModel
import com.example.feature.profile.ProfileSettingsScreen
import com.example.feature.search.SearchScreen
import com.example.feature.search.SearchViewModel
import com.example.ui.theme.GammaBackground
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceGlass
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary

sealed class Screen(val route: String, val title: String, val testTag: String) {
    data object Discover : Screen("discover", "Discover", "tab_discover")
    data object Search : Screen("search", "Search", "tab_search")
    data object Library : Screen("library", "Library", "tab_library")
    data object Settings : Screen("settings", "About", "tab_settings")
    data object Player : Screen("player", "Player", "screen_player")
}

@Composable
fun GammaApp(
    container: GammaContainer = rememberGammaContainer()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playbackState by container.playbackManager.playbackState.collectAsStateWithLifecycle()
    val isPlayerScreen = currentRoute == Screen.Player.route

    val bottomNavItems = listOf(
        Pair(Screen.Discover, Icons.Default.Sensors),
        Pair(Screen.Search, Icons.Default.Search),
        Pair(Screen.Library, Icons.Default.QueueMusic),
        Pair(Screen.Settings, Icons.Default.Person)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = GammaBackground,
            contentWindowInsets = WindowInsets.systemBars,
            bottomBar = {
                if (!isPlayerScreen) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GammaBackground)
                    ) {
                        // Floating docked mini-player
                        GammaMiniPlayer(
                            playbackState = playbackState,
                            onExpandClick = {
                                navController.navigate(Screen.Player.route)
                            },
                            onPlayPauseClick = {
                                container.playbackManager.togglePlayPause()
                            },
                            onNextClick = {
                                container.playbackManager.skipNext()
                            }
                        )

                        // Bottom Navigation Bar
                        NavigationBar(
                            containerColor = GammaSurfaceElevated,
                            contentColor = GammaTextPrimary,
                            tonalElevation = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .border(1.dp, GammaDivider.copy(alpha = 0.5f))
                        ) {
                            bottomNavItems.forEach { (screen, icon) ->
                                val selected = currentRoute == screen.route
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = screen.title,
                                            tint = if (selected) GammaPrimary else GammaTextMuted,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected) GammaPrimary else GammaTextMuted,
                                            fontSize = 10.sp
                                        )
                                    },
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = GammaSurfaceElevated
                                    ),
                                    modifier = Modifier.testTag(screen.testTag)
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Discover.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isPlayerScreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
            ) {
                composable(Screen.Discover.route) {
                    val viewModel: DiscoverViewModel = viewModel(
                        factory = DiscoverViewModel.provideFactory(
                            container.musicRepository,
                            container.playbackManager,
                            container.networkMonitor
                        )
                    )
                    DiscoverScreen(
                        viewModel = viewModel,
                        currentPlayingTrackId = playbackState.currentTrack?.id,
                        isPlaying = playbackState.isPlaying,
                        onTrackClick = { track, queue ->
                            viewModel.playTrack(track, queue)
                        },
                        onArtistClick = { artistId ->
                            navController.navigate("artist/$artistId")
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate("album/$albumId")
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate("playlist/$playlistId")
                        }
                    )
                }

                composable(Screen.Search.route) {
                    val viewModel: SearchViewModel = viewModel(
                        factory = SearchViewModel.provideFactory(
                            container.musicRepository,
                            container.playbackManager,
                            container.networkMonitor
                        )
                    )
                    SearchScreen(
                        viewModel = viewModel,
                        currentPlayingTrackId = playbackState.currentTrack?.id,
                        isPlaying = playbackState.isPlaying,
                        onTrackClick = { track, queue ->
                            viewModel.playTrack(track, queue)
                        },
                        onArtistClick = { artistId ->
                            navController.navigate("artist/$artistId")
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate("album/$albumId")
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate("playlist/$playlistId")
                        }
                    )
                }

                composable(Screen.Library.route) {
                    val viewModel: LibraryViewModel = viewModel(
                        factory = LibraryViewModel.provideFactory(
                            container.musicRepository,
                            container.playbackManager
                        )
                    )
                    LibraryScreen(
                        viewModel = viewModel,
                        currentPlayingTrackId = playbackState.currentTrack?.id,
                        isPlaying = playbackState.isPlaying,
                        onTrackClick = { track, queue ->
                            viewModel.playTrack(track, queue)
                        },
                        onPlaylistClick = { playlistId ->
                            navController.navigate("playlist/$playlistId")
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    ProfileSettingsScreen()
                }

                composable("artist/{artistId}", arguments = listOf(navArgument("artistId") { type = NavType.StringType })) { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getString("artistId") ?: ""
                    val viewModel: ArtistViewModel = viewModel(
                        key = "artist_$artistId",
                        factory = ArtistViewModel.provideFactory(
                            artistId,
                            container.musicRepository,
                            container.playbackManager
                        )
                    )
                    ArtistScreen(
                        viewModel = viewModel,
                        currentPlayingTrackId = playbackState.currentTrack?.id,
                        isPlaying = playbackState.isPlaying,
                        onBackClick = { navController.popBackStack() },
                        onTrackClick = { track, queue ->
                            viewModel.playTrack(track, queue)
                        },
                        onAlbumClick = { albumId ->
                            navController.navigate("album/$albumId")
                        }
                    )
                }

                composable("album/{albumId}", arguments = listOf(navArgument("albumId") { type = NavType.StringType })) { backStackEntry ->
                    val albumId = backStackEntry.arguments?.getString("albumId") ?: ""
                    val viewModel: AlbumViewModel = viewModel(
                        key = "album_$albumId",
                        factory = AlbumViewModel.provideFactory(
                            albumId,
                            container.musicRepository,
                            container.playbackManager
                        )
                    )
                    AlbumScreen(
                        viewModel = viewModel,
                        currentPlayingTrackId = playbackState.currentTrack?.id,
                        isPlaying = playbackState.isPlaying,
                        onBackClick = { navController.popBackStack() },
                        onTrackClick = { track, queue ->
                            viewModel.playTrack(track, queue)
                        }
                    )
                }

                composable("playlist/{playlistId}", arguments = listOf(navArgument("playlistId") { type = NavType.StringType })) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId") ?: ""
                    val viewModel: PlaylistViewModel = viewModel(
                        key = "playlist_$playlistId",
                        factory = PlaylistViewModel.provideFactory(
                            playlistId,
                            container.musicRepository,
                            container.playbackManager
                        )
                    )
                    PlaylistScreen(
                        viewModel = viewModel,
                        currentPlayingTrackId = playbackState.currentTrack?.id,
                        isPlaying = playbackState.isPlaying,
                        onBackClick = { navController.popBackStack() },
                        onTrackClick = { track, queue ->
                            viewModel.playTrack(track, queue)
                        }
                    )
                }

                composable(Screen.Player.route) {
                    val viewModel: PlayerViewModel = viewModel(
                        factory = PlayerViewModel.provideFactory(
                            container.playbackManager,
                            container.musicRepository
                        )
                    )
                    GammaPlayerScreen(
                        viewModel = viewModel,
                        onCollapseClick = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Discover.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }

        // Persistent hardware-accelerated authentic YouTube player (visible at alpha 1.0f)
        val isYouTubeTrack = playbackState.currentTrack?.let {
            it.youtubeVideoId.isNotEmpty() && it.localAudioUri.isEmpty()
        } ?: false

        if (isYouTubeTrack) {
            val videoWidth by animateDpAsState(
                targetValue = if (isPlayerScreen) 320.dp else 220.dp,
                label = "videoWidth"
            )
            val videoHeight by animateDpAsState(
                targetValue = if (isPlayerScreen) 210.dp else 130.dp,
                label = "videoHeight"
            )
            val cornerRadius by animateDpAsState(
                targetValue = if (isPlayerScreen) 20.dp else 12.dp,
                label = "cornerRadius"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = if (isPlayerScreen) 86.dp else 0.dp,
                        bottom = if (isPlayerScreen) 0.dp else 148.dp,
                        end = if (isPlayerScreen) 0.dp else 14.dp
                    ),
                contentAlignment = if (isPlayerScreen) Alignment.TopCenter else Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(width = videoWidth, height = videoHeight)
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(Color.Black)
                        .border(1.5.dp, GammaGlowCyan, RoundedCornerShape(cornerRadius))
                        .clickable(enabled = !isPlayerScreen) {
                            navController.navigate(Screen.Player.route)
                        }
                ) {
                    CompliantYouTubeHost(
                        playbackManager = container.playbackManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun rememberGammaContainer(): GammaContainer {
    val context = LocalContext.current.applicationContext
    return remember { GammaContainer(context) }
}
