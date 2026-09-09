package com.example.feature.player

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.FavoriteBorder
import com.example.domain.model.TuningPreset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.GammaArtwork
import com.example.core.ui.GammaWaveformVisualizer
import com.example.domain.model.RepeatMode as DomainRepeatMode
import com.example.domain.model.Track
import com.example.ui.theme.GammaAuraBrush
import com.example.ui.theme.GammaBackground
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaFrequencyBrush
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaGlowViolet
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaPrimaryVariant
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurface
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GammaPlayerScreen(
    viewModel: PlayerViewModel,
    onCollapseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playback = uiState.playbackState
    val track = playback.currentTrack

    var showQueueSheet by remember { mutableStateOf(false) }
    var showSpecsSheet by remember { mutableStateOf(false) }
    var showEqualizerSheet by remember { mutableStateOf(false) }
    val queueSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val specsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val equalizerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Breathing artwork pulse when playing
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPositionMs by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GammaBackground)
    ) {
        // Deep ambient glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(GammaGlowViolet, GammaBackground),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation & Frequency Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapseClick,
                    modifier = Modifier.testTag("player_collapse_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = GammaTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Frequency badge
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GammaSurfaceElevated)
                        .border(1.dp, GammaGlowCyan, CircleShape)
                        .clickable { showSpecsSheet = true }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(GammaPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${track?.genre ?: "Music"} • NOW PLAYING",
                            style = MaterialTheme.typography.labelSmall,
                            color = GammaPrimary
                        )
                    }
                }

                IconButton(
                    onClick = { showSpecsSheet = true },
                    modifier = Modifier.testTag("player_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Track specs",
                        tint = GammaTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hero Artwork or YouTube Video Area
            val isYouTubePlaying = track?.let { it.youtubeVideoId.isNotEmpty() && it.localAudioUri.isEmpty() } ?: false
            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 210.dp)
                    .scale(if (playback.isPlaying) pulseScale else 1.0f),
                contentAlignment = Alignment.Center
            ) {
                if (!isYouTubePlaying) {
                    // Background aura ring
                    Box(
                        modifier = Modifier
                            .size(width = 280.dp, height = 190.dp)
                            .background(GammaGlowCyan.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    )

                    GammaArtwork(
                        url = track?.artworkUrl ?: "",
                        contentDescription = "${track?.title} cover",
                        modifier = Modifier.size(210.dp),
                        shape = RoundedCornerShape(20.dp),
                        hasGlowBorder = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Visual Frequency Equalizer Bars
            GammaWaveformVisualizer(
                bands = playback.visualizerBands,
                isPlaying = playback.isPlaying,
                modifier = Modifier.padding(horizontal = 12.dp),
                height = 40.dp
            )

            // Track Title & Favorite Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "No Signal Active",
                        style = MaterialTheme.typography.displayMedium,
                        color = GammaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track?.artist ?: "GAMMA Frequency Hub",
                        style = MaterialTheme.typography.titleMedium,
                        color = GammaTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Download Button
                    val isDownloading = playback.isDownloading && playback.downloadingTrackId == track?.id
                    val isDownloaded = track?.isDownloaded == true || (track != null && viewModel.playbackManager.isTrackDownloaded(track.id))
                    IconButton(
                        onClick = { viewModel.downloadCurrentTrack() },
                        modifier = Modifier.testTag("player_download_button")
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = { playback.downloadProgress },
                                modifier = Modifier.size(24.dp),
                                color = GammaPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Filled.DownloadDone else Icons.Default.Download,
                                contentDescription = if (isDownloaded) "Downloaded locally" else "Download locally",
                                tint = if (isDownloaded) GammaPrimary else GammaTextMuted,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        modifier = Modifier.testTag("player_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (uiState.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (uiState.isFavorite) GammaPrimary else GammaTextMuted,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Seekbar
            Column(modifier = Modifier.fillMaxWidth()) {
                val currentMs = if (isUserSeeking) seekPositionMs.toLong() else playback.positionMs
                val maxMs = playback.durationMs.coerceAtLeast(1000L)
                val sliderValue = currentMs.toFloat().coerceIn(0f, maxMs.toFloat())

                Slider(
                    value = sliderValue,
                    onValueChange = {
                        isUserSeeking = true
                        seekPositionMs = it
                    },
                    onValueChangeFinished = {
                        viewModel.seekTo(seekPositionMs.toLong())
                        isUserSeeking = false
                    },
                    valueRange = 0f..maxMs.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = GammaPrimary,
                        activeTrackColor = GammaPrimary,
                        inactiveTrackColor = GammaDivider
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playback_slider")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimeMs(currentMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = GammaTextMuted
                    )
                    Text(
                        text = formatTimeMs(maxMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = GammaTextMuted
                    )
                }
            }

            // Control Deck (Shuffle, Prev, Hero Play/Pause, Next, Repeat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = { viewModel.toggleShuffle() },
                    modifier = Modifier.testTag("player_shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playback.isShuffle) GammaPrimary else GammaTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Skip Previous
                IconButton(
                    onClick = { viewModel.skipPrevious() },
                    modifier = Modifier.testTag("player_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous signal",
                        tint = GammaTextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Hero Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GammaFrequencyBrush)
                        .clickable { viewModel.togglePlayPause() }
                        .testTag("player_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (playback.isBuffering) {
                        CircularProgressIndicator(
                            color = GammaBackground,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (playback.isPlaying) "Pause" else "Play",
                            tint = GammaBackground,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Skip Next
                IconButton(
                    onClick = { viewModel.skipNext() },
                    modifier = Modifier.testTag("player_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next signal",
                        tint = GammaTextPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Repeat Mode
                IconButton(
                    onClick = { viewModel.cycleRepeatMode() },
                    modifier = Modifier.testTag("player_repeat_button")
                ) {
                    val (icon, tint) = when (playback.repeatMode) {
                        DomainRepeatMode.OFF -> Pair(Icons.Default.Repeat, GammaTextMuted)
                        DomainRepeatMode.ALL -> Pair(Icons.Default.Repeat, GammaPrimary)
                        DomainRepeatMode.ONE -> Pair(Icons.Default.RepeatOne, GammaPrimary)
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Repeat mode",
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bottom Actions (Smart Queue & Equalizer Buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
            ) {
                // Smart Queue Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (playback.isSmartQueueEnabled) GammaSurfaceElevated else GammaSurface)
                        .border(
                            width = 1.dp,
                            brush = if (playback.isSmartQueueEnabled) GammaFrequencyBrush else Brush.linearGradient(listOf(GammaDivider, GammaDivider)),
                            shape = CircleShape
                        )
                        .clickable { showQueueSheet = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("open_queue_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (playback.isSmartQueueEnabled) Icons.Default.AutoAwesome else Icons.Default.QueueMusic,
                            contentDescription = null,
                            tint = if (playback.isSmartQueueEnabled) GammaPrimary else GammaTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (playback.isSmartQueueEnabled) "Smart Queue (${playback.queue.size})" else "Queue (${playback.queue.size})",
                            style = MaterialTheme.typography.bodySmall,
                            color = GammaTextPrimary
                        )
                        if (playback.isSmartQueueEnabled && playback.smartQueueProfile.totalAnalyzed > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(GammaPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = playback.smartQueueProfile.dominantGenre,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GammaPrimary
                                )
                            }
                        }
                    }
                }

                // Equalizer Tuning Button
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (playback.equalizerSettings.isEnabled) GammaSurfaceElevated else GammaSurface)
                        .border(
                            width = 1.dp,
                            brush = if (playback.equalizerSettings.isEnabled) GammaFrequencyBrush else Brush.linearGradient(listOf(GammaDivider, GammaDivider)),
                            shape = CircleShape
                        )
                        .clickable { showEqualizerSheet = true }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("open_equalizer_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Equalizer Tuning",
                            tint = if (playback.equalizerSettings.isEnabled) GammaPrimary else GammaTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "EQ: ${playback.equalizerSettings.currentPreset.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GammaTextPrimary
                        )
                    }
                }
            }
        }
    }

    // Queue Bottom Sheet
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = queueSheetState,
            containerColor = GammaSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GammaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart Queue",
                            style = MaterialTheme.typography.titleLarge,
                            color = GammaTextPrimary
                        )
                    }
                    Text(
                        text = "Clear Queue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GammaPrimary,
                        modifier = Modifier
                            .clickable { viewModel.clearQueue() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Smart Queue Mode & Resonance Affinity Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GammaSurfaceHighlight)
                        .border(1.dp, GammaFrequencyBrush, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Append Recommendations",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GammaTextPrimary
                                )
                                Text(
                                    text = if (playback.isSmartQueueEnabled) {
                                        "Matches your session: ${playback.smartQueueProfile.displaySummary}"
                                    } else {
                                        "Smart queue paused"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GammaTextSecondary
                                )
                            }
                            Switch(
                                checked = playback.isSmartQueueEnabled,
                                onCheckedChange = { viewModel.toggleSmartQueue(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GammaPrimary,
                                    checkedTrackColor = GammaPrimaryVariant
                                )
                            )
                        }

                        if (playback.isSmartQueueEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GammaPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = playback.smartQueueProfile.dominantGenre,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GammaPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GammaSecondary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${playback.smartQueueProfile.totalAnalyzed} Tracks",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GammaSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GammaPrimary)
                                        .clickable { viewModel.appendSmartRecommendations(3) }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = GammaBackground,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "+ Append 3",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GammaBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    itemsIndexed(playback.queue) { index, queueTrack ->
                        val isCurr = index == playback.queueIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurr) GammaSurfaceHighlight else Color.Transparent)
                                .clickable { viewModel.jumpToQueueIndex(index) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurr) GammaPrimary else GammaTextMuted,
                                modifier = Modifier.width(24.dp)
                            )
                            GammaArtwork(
                                url = queueTrack.artworkUrl,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = queueTrack.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isCurr) GammaPrimary else GammaTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = queueTrack.artist,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GammaTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (queueTrack.source == "smart_recommendation") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "✦ Auto-Rec",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GammaPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = queueTrack.genre,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GammaTextMuted
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.removeFromQueue(index) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove from queue",
                                    tint = GammaTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Specs Bottom Sheet
    if (showSpecsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSpecsSheet = false },
            sheetState = specsSheetState,
            containerColor = GammaSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Track Information",
                    style = MaterialTheme.typography.titleLarge,
                    color = GammaTextPrimary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GammaBackground)
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SpecRow("Genre", track?.genre ?: "Audio")
                        SpecRow("Audio Architecture", "5-Band DSP Equalizer")
                        SpecRow("Playback Mechanism", "YouTube Authorized IFrame Embed")
                        SpecRow("Terms & Rights", "Official Player • Policy Compliant")
                        SpecRow("Media ID", track?.youtubeVideoId ?: "local_track")
                    }
                }

                Text(
                    text = "GAMA delivers high-fidelity audio playback with customizable 5-band real-time equalization and smart recommendations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GammaTextMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }

    // Equalizer Bottom Sheet
    if (showEqualizerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showEqualizerSheet = false },
            sheetState = equalizerSheetState,
            containerColor = GammaSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Header with EQ bypass switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = GammaPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "GAMA Equalizer & Tuning",
                                style = MaterialTheme.typography.titleLarge,
                                color = GammaTextPrimary
                            )
                            Text(
                                text = "5-Band Real-Time DSP Audio Processing",
                                style = MaterialTheme.typography.bodySmall,
                                color = GammaTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = playback.equalizerSettings.isEnabled,
                        onCheckedChange = { viewModel.toggleEqualizer(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GammaPrimary,
                            checkedTrackColor = GammaPrimaryVariant
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Carousel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TUNING PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = GammaTextMuted,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Active: ${playback.equalizerSettings.currentPreset.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = GammaPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(TuningPreset.values()) { preset ->
                        val isSelected = playback.equalizerSettings.currentPreset == preset
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) GammaPrimary else GammaSurfaceHighlight)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) GammaPrimary else GammaDivider,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.setTuningPreset(preset) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = preset.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) GammaBackground else GammaTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5 Equalizer Bands
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GammaBackground)
                        .border(1.dp, GammaFrequencyBrush, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        playback.equalizerSettings.bands.forEach { band ->
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = band.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GammaTextPrimary
                                    )
                                    val sign = if (band.gainDb > 0f) "+" else ""
                                    Text(
                                        text = "$sign%.1f dB".format(band.gainDb),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (band.gainDb != 0f) GammaPrimary else GammaTextMuted
                                    )
                                }
                                Slider(
                                    value = band.gainDb,
                                    onValueChange = { newDb ->
                                        viewModel.setEqualizerBand(band.index, newDb)
                                    },
                                    valueRange = -12f..12f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = GammaPrimary,
                                        activeTrackColor = GammaPrimary,
                                        inactiveTrackColor = GammaSurfaceHighlight
                                    ),
                                    enabled = playback.equalizerSettings.isEnabled
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Master Pre-Amp Gain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Master Tuning Gain",
                        style = MaterialTheme.typography.bodySmall,
                        color = GammaTextSecondary
                    )
                    Text(
                        text = "%.1fx".format(playback.equalizerSettings.masterGain),
                        style = MaterialTheme.typography.labelSmall,
                        color = GammaSecondary
                    )
                }
                Slider(
                    value = playback.equalizerSettings.masterGain,
                    onValueChange = { viewModel.setMasterGain(it) },
                    valueRange = 0.5f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = GammaSecondary,
                        activeTrackColor = GammaSecondary,
                        inactiveTrackColor = GammaSurfaceHighlight
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = GammaTextSecondary)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = GammaPrimary)
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
