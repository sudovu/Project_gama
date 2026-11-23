package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.media.PlaybackManager
import com.example.ui.theme.GammaBackground
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaFrequencyBrush
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurface
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary
import com.example.ui.theme.GammaThemeManager
import com.example.ui.theme.GammaThemePreset

object GammaCommandPaletteManager {
    var isVisible by mutableStateOf(false)

    fun open() {
        isVisible = true
    }

    fun close() {
        isVisible = false
    }

    fun toggle() {
        isVisible = !isVisible
    }
}

data class PaletteCommand(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: String,
    val icon: ImageVector,
    val shortcutHint: String? = null,
    val onExecute: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GammaCommandPalette(
    playbackManager: PlaybackManager,
    onNavigateToPlayer: () -> Unit = {},
    onDismiss: () -> Unit = { GammaCommandPaletteManager.close() }
) {
    if (!GammaCommandPaletteManager.isVisible) return

    var query by remember { mutableStateOf("") }
    val playbackState = playbackManager.playbackState.value

    val allCommands = remember(playbackState) {
        listOf(
            PaletteCommand(
                id = "play_pause",
                title = if (playbackState.isPlaying) "Pause Playback" else "Resume Playback",
                subtitle = playbackState.currentTrack?.title ?: "Toggle active audio engine",
                category = "Playback",
                icon = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                shortcutHint = "Space"
            ) {
                playbackManager.togglePlayPause()
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "skip_next",
                title = "Skip to Next Track",
                subtitle = "Gapless transition to next queued item",
                category = "Playback",
                icon = Icons.Default.SkipNext,
                shortcutHint = "Ctrl+Right"
            ) {
                playbackManager.skipNext()
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "skip_prev",
                title = "Skip to Previous Track",
                subtitle = "Replay current or previous track",
                category = "Playback",
                icon = Icons.Default.SkipPrevious,
                shortcutHint = "Ctrl+Left"
            ) {
                playbackManager.skipPrevious()
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "toggle_audio_only",
                title = if (playbackState.isAudioOnlyMode) "Switch to Video Mode" else "Switch to Audio-Only Mode",
                subtitle = "Background audio streaming continuity without reload",
                category = "Playback",
                icon = if (playbackState.isAudioOnlyMode) Icons.Default.Videocam else Icons.Default.Headphones
            ) {
                playbackManager.toggleAudioOnlyMode()
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "toggle_shuffle",
                title = if (playbackState.isShuffle) "Turn Shuffle Off" else "Turn Shuffle On",
                subtitle = "Algorithmic queue randomization",
                category = "Queue",
                icon = Icons.Default.Shuffle
            ) {
                playbackManager.toggleShuffle()
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "clear_queue",
                title = "Clear Up-Next Queue",
                subtitle = "Empty remaining queue tracks",
                category = "Queue",
                icon = Icons.Default.QueueMusic
            ) {
                playbackManager.clearQueue()
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "open_player",
                title = "Open Fullscreen Studio Player",
                subtitle = "Access 16-band spectrum visualizer and controls",
                category = "Studio",
                icon = Icons.Default.Tune
            ) {
                GammaCommandPaletteManager.close()
                onNavigateToPlayer()
            },
            PaletteCommand(
                id = "sleep_15",
                title = "Set Sleep Timer: 15 Minutes",
                subtitle = "Gradual audio fade-out after 15 min",
                category = "Sleep Timer",
                icon = Icons.Default.Bedtime
            ) {
                playbackManager.startSleepTimer(15)
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "sleep_30",
                title = "Set Sleep Timer: 30 Minutes",
                subtitle = "Gradual audio fade-out after 30 min",
                category = "Sleep Timer",
                icon = Icons.Default.Bedtime
            ) {
                playbackManager.startSleepTimer(30)
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "theme_cosmic_cyan",
                title = "Theme: Cosmic Cyan (Default)",
                subtitle = "Deep space cyan & electric violet accents",
                category = "Themes",
                icon = Icons.Default.Palette
            ) {
                GammaThemeManager.selectTheme(GammaThemePreset.COSMIC_CYAN)
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "theme_solfeggio",
                title = "Theme: Solfeggio Violet",
                subtitle = "Radiant purple & electric magenta cyberpunk resonance",
                category = "Themes",
                icon = Icons.Default.Palette
            ) {
                GammaThemeManager.selectTheme(GammaThemePreset.SOLFEGGIO_VIOLET)
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "theme_solar_amber",
                title = "Theme: Solar Amber",
                subtitle = "Warm molten gold, acoustic wood & amber harmonics",
                category = "Themes",
                icon = Icons.Default.Palette
            ) {
                GammaThemeManager.selectTheme(GammaThemePreset.SOLAR_AMBER)
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "theme_emerald_zen",
                title = "Theme: Emerald Zen",
                subtitle = "Natural organic tuning, healing jade & vibrant mint",
                category = "Themes",
                icon = Icons.Default.Palette
            ) {
                GammaThemeManager.selectTheme(GammaThemePreset.EMERALD_ZEN)
                GammaCommandPaletteManager.close()
            },
            PaletteCommand(
                id = "theme_solar_daybreak",
                title = "Theme: Solar Daybreak",
                subtitle = "Clean daytime layout with sapphire accents",
                category = "Themes",
                icon = Icons.Default.Palette
            ) {
                GammaThemeManager.selectTheme(GammaThemePreset.SOLAR_DAYBREAK)
                GammaCommandPaletteManager.close()
            }
        )
    }

    val filteredCommands = remember(query, allCommands) {
        if (query.isBlank()) {
            allCommands
        } else {
            allCommands.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.subtitle.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(20.dp))
            .background(GammaSurfaceElevated)
            .border(1.5.dp, GammaFrequencyBrush, RoundedCornerShape(20.dp))
            .testTag("gamma_command_palette_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GammaPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = GammaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "COMMAND PALETTE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            ),
                            color = GammaPrimary
                        )
                        Text(
                            text = "Press Ctrl+K or type to launch actions",
                            style = MaterialTheme.typography.bodySmall,
                            color = GammaTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = GammaTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Query Input Box
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text(
                        text = "Search actions, themes, equalizer, timers...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GammaTextMuted
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("command_palette_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GammaPrimary,
                    unfocusedBorderColor = GammaDivider,
                    focusedTextColor = GammaTextPrimary,
                    unfocusedTextColor = GammaTextPrimary,
                    cursorColor = GammaPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Commands List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredCommands, key = { it.id }) { cmd ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(GammaSurface)
                            .clickable { cmd.onExecute() }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GammaSurfaceHighlight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cmd.icon,
                                    contentDescription = null,
                                    tint = GammaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = cmd.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = GammaTextPrimary
                                )
                                Text(
                                    text = cmd.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GammaTextMuted
                                )
                            }
                        }

                        if (cmd.shortcutHint != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GammaBackground)
                                    .border(1.dp, GammaDivider, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = cmd.shortcutHint,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = GammaTextSecondary
                                )
                            }
                        }
                    }
                }

                if (filteredCommands.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No commands matching \"$query\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GammaTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
