package com.example.feature.artist

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.GammaAlbumCard
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
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    viewModel: ArtistViewModel,
    currentPlayingTrackId: String?,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onTrackClick: (Track, List<Track>) -> Unit,
    onAlbumClick: (String) -> Unit,
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
                        modifier = Modifier.testTag("artist_back_button")
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
                is ArtistUiState.Loading -> {
                    GammaLoadingSkeleton()
                }
                is ArtistUiState.Error -> {
                    GammaErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadArtist() }
                    )
                }
                is ArtistUiState.Success -> {
                    val artist = state.artist
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("artist_content_list"),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Panoramic Header
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                GammaArtwork(
                                    url = artist.artworkUrl,
                                    contentDescription = "${artist.name} photo",
                                    modifier = Modifier.size(140.dp),
                                    shape = CircleShape,
                                    hasGlowBorder = true
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.displayMedium,
                                    color = GammaTextPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = artist.monthlyListeners,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GammaPrimary
                                )

                                if (artist.bio.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = artist.bio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GammaTextSecondary,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Action buttons
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    GammaPrimaryButton(
                                        text = "Play All",
                                        icon = Icons.Default.PlayArrow,
                                        onClick = { viewModel.playAll(artist.topTracks, shuffle = false) },
                                        testTag = "artist_play_all_button"
                                    )

                                    GammaSecondaryButton(
                                        text = "Shuffle",
                                        icon = Icons.Default.Shuffle,
                                        onClick = { viewModel.playAll(artist.topTracks, shuffle = true) },
                                        testTag = "artist_shuffle_button"
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                            }
                        }

                        // Top Tracks
                        if (artist.topTracks.isNotEmpty()) {
                            item {
                                GammaSectionHeader(
                                    category = "Popular Signals",
                                    title = "Top Tracks"
                                )
                            }

                            items(artist.topTracks, key = { it.id }) { track ->
                                val isFav = state.favoriteIds.contains(track.id)
                                val isCurr = track.id == currentPlayingTrackId
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    GammaTrackRow(
                                        track = track.copy(isFavorite = isFav),
                                        isCurrentTrack = isCurr,
                                        isPlaying = isPlaying && isCurr,
                                        onClick = { onTrackClick(track, artist.topTracks) },
                                        onFavoriteToggle = { viewModel.toggleFavorite(track) }
                                    )
                                }
                            }
                        }

                        // Albums
                        if (artist.albums.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                GammaSectionHeader(
                                    category = "Discography",
                                    title = "Albums & Horizons"
                                )
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(artist.albums, key = { it.id }) { album ->
                                        GammaAlbumCard(
                                            album = album,
                                            onClick = { onAlbumClick(album.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
