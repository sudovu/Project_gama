package com.example.feature.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.GammaArtwork
import com.example.core.ui.GammaErrorState
import com.example.core.ui.GammaLoadingSkeleton
import com.example.core.ui.GammaPrimaryButton
import com.example.core.ui.GammaSecondaryButton
import com.example.core.ui.GammaSectionHeader
import com.example.core.ui.GammaTrackRow
import com.example.domain.model.Track
import com.example.ui.theme.GammaAuraBrush
import com.example.ui.theme.GammaBackground
import com.example.ui.theme.GammaError
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel,
    currentPlayingTrackId: String?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GammaBackground,
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("playlist_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GammaTextPrimary
                        )
                    }
                },
                actions = {
                    if ((uiState as? PlaylistUiState.Success)?.playlist?.isUserCreated == true) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("playlist_delete_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Playlist",
                                tint = GammaError
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GammaAuraBrush)
        ) {
            when (val state = uiState) {
                is PlaylistUiState.Loading -> {
                    GammaLoadingSkeleton()
                }
                is PlaylistUiState.Error -> {
                    GammaErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadPlaylist() }
                    )
                }
                is PlaylistUiState.Success -> {
                    val playlist = state.playlist

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("playlist_content_list"),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GammaArtwork(
                                    url = playlist.artworkUrl,
                                    contentDescription = "${playlist.title} cover",
                                    modifier = Modifier.size(190.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    hasGlowBorder = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = playlist.title,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = GammaTextPrimary
                                )

                                if (playlist.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = playlist.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GammaTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                if (playlist.tracks.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GammaPrimaryButton(
                                            text = "Play",
                                            icon = Icons.Default.PlayArrow,
                                            onClick = { viewModel.playAll(playlist.tracks, shuffle = false) },
                                            testTag = "playlist_play_button"
                                        )

                                        GammaSecondaryButton(
                                            text = "Shuffle",
                                            icon = Icons.Default.Shuffle,
                                            onClick = { viewModel.playAll(playlist.tracks, shuffle = true) },
                                            testTag = "playlist_shuffle_button"
                                        )

                                        GammaSecondaryButton(
                                            text = "Download All",
                                            icon = Icons.Default.Download,
                                            onClick = { viewModel.downloadAll(playlist.tracks) },
                                            testTag = "playlist_download_all_button"
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        item {
                            GammaSectionHeader(
                                category = "Soundwave Queue",
                                title = "${playlist.tracks.size} Signals"
                            )
                        }

                        if (playlist.tracks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "This playlist has no signals yet. Add tracks from Discover or Search!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GammaTextSecondary
                                    )
                                }
                            }
                        } else {
                            items(playlist.tracks, key = { it.id }) { track ->
                                val isFav = state.favoriteIds.contains(track.id)
                                val isCurr = track.id == currentPlayingTrackId
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    GammaTrackRow(
                                        track = track.copy(isFavorite = isFav),
                                        isCurrentTrack = isCurr,
                                        isPlaying = isPlaying && isCurr,
                                        onClick = { onTrackClick(track, playlist.tracks) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(track) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = GammaSurfaceElevated,
            title = {
                Text(
                    text = "Delete Playlist?",
                    style = MaterialTheme.typography.titleLarge,
                    color = GammaTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this playlist? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GammaTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deletePlaylist(onComplete = onBackClick)
                    }
                ) {
                    Text("Delete", color = GammaError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = GammaTextPrimary)
                }
            }
        )
    }
}
