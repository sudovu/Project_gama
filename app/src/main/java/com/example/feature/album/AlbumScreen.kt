package com.example.feature.album

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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel,
    currentPlayingTrackId: String?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GammaBackground,
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("album_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GammaTextPrimary
                        )
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
                is AlbumUiState.Loading -> {
                    GammaLoadingSkeleton()
                }
                is AlbumUiState.Error -> {
                    GammaErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadAlbum() }
                    )
                }
                is AlbumUiState.Success -> {
                    val album = state.album
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("album_content_list"),
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
                                    url = album.artworkUrl,
                                    contentDescription = "${album.title} cover",
                                    modifier = Modifier.size(190.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    hasGlowBorder = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = GammaTextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${album.artist} • ${album.releaseYear} • ${album.genre}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GammaTextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GammaPrimaryButton(
                                        text = "Play",
                                        icon = Icons.Default.PlayArrow,
                                        onClick = { viewModel.playAll(album.tracks, shuffle = false) },
                                        testTag = "album_play_button"
                                    )

                                    GammaSecondaryButton(
                                        text = "Shuffle",
                                        icon = Icons.Default.Shuffle,
                                        onClick = { viewModel.playAll(album.tracks, shuffle = true) },
                                        testTag = "album_shuffle_button"
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        item {
                            GammaSectionHeader(
                                category = "Tracklist",
                                title = "${album.tracks.size} Signals"
                            )
                        }

                        items(album.tracks, key = { it.id }) { track ->
                            val isFav = state.favoriteIds.contains(track.id)
                            val isCurr = track.id == currentPlayingTrackId
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            ) {
                                GammaTrackRow(
                                    track = track.copy(isFavorite = isFav),
                                    isCurrentTrack = isCurr,
                                    isPlaying = isPlaying && isCurr,
                                    onClick = { onTrackClick(track, album.tracks) },
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
