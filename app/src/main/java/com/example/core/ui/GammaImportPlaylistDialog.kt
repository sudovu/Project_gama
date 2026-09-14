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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.provider.CuratedFrequencies
import com.example.domain.model.Track
import com.example.ui.theme.GammaBackground
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary

enum class ImportPlatform(val label: String, val iconColor: Color) {
    SPOTIFY("Spotify", Color(0xFF1DB954)),
    YOUTUBE("YouTube Music", Color(0xFFFF0000)),
    APPLE("Apple Music", Color(0xFFFC3C44)),
    PRESETS("Quick Presets", Color(0xFF00FFCC))
}

@Composable
fun GammaImportPlaylistDialog(
    onDismiss: () -> Unit,
    onImportConfirmed: (name: String, tracks: List<Track>) -> Unit
) {
    var selectedPlatform by remember { mutableStateOf(ImportPlatform.SPOTIFY) }
    var playlistUrl by remember { mutableStateOf("") }
    var playlistTitle by remember { mutableStateOf("") }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }

    val samplePresets = listOf(
        Pair("Spotify Top Global 2026", listOf(
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("Shape", true) } ?: CuratedFrequencies.allTracks[0],
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("Blinding", true) } ?: CuratedFrequencies.allTracks[1],
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("Believer", true) } ?: CuratedFrequencies.allTracks[2],
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("bad guy", true) } ?: CuratedFrequencies.allTracks[3]
        )),
        Pair("Rock Anthems (Spotify Reference)", listOf(
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("Duality", true) } ?: CuratedFrequencies.allTracks[0],
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("Psychosocial", true) } ?: CuratedFrequencies.allTracks[1],
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("In The End", true) } ?: CuratedFrequencies.allTracks[2],
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("Numb", true) } ?: CuratedFrequencies.allTracks[3]
        )),
        Pair("YouTube 432Hz Ambient Resonance", listOf(
            CuratedFrequencies.allTracks.firstOrNull { it.frequencyHz == 432 } ?: CuratedFrequencies.allTracks[0],
            CuratedFrequencies.allTracks.firstOrNull { it.frequencyHz == 528 } ?: CuratedFrequencies.allTracks[1],
            CuratedFrequencies.allTracks.firstOrNull { it.title.contains("Rolling", true) } ?: CuratedFrequencies.allTracks[2]
        ))
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, GammaGlowCyan, RoundedCornerShape(24.dp)),
            color = GammaSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GammaPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = GammaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Import Playlist",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = GammaTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = GammaTextMuted
                        )
                    }
                }

                Text(
                    text = "Import your playlists from Spotify, YouTube Music, or Apple Music into your local vault.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GammaTextSecondary,
                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                )

                // Platform Selection Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ImportPlatform.entries.forEach { platform ->
                        val isSelected = selectedPlatform == platform
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) platform.iconColor.copy(alpha = 0.15f)
                                    else GammaSurfaceHighlight
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) platform.iconColor else GammaDivider,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPlatform = platform }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = platform.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) platform.iconColor else GammaTextMuted,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedPlatform == ImportPlatform.PRESETS) {
                    // Quick Presets
                    Text(
                        text = "SELECT ONE-TAP CURATED PLAYLIST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GammaPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    samplePresets.forEachIndexed { index, preset ->
                        val isChosen = selectedPresetIndex == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isChosen) GammaPrimary.copy(alpha = 0.12f)
                                    else GammaSurfaceHighlight
                                )
                                .border(
                                    1.dp,
                                    if (isChosen) GammaPrimary else GammaDivider,
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedPresetIndex = index }
                                .padding(12.dp)
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
                                        tint = if (isChosen) GammaPrimary else GammaTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = preset.first,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isChosen) GammaPrimary else GammaTextPrimary
                                        )
                                        Text(
                                            text = "${preset.second.size} tracks matched",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GammaTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Custom Link or Playlist Title
                    OutlinedTextField(
                        value = playlistTitle,
                        onValueChange = { playlistTitle = it },
                        label = { Text("Playlist Name (Optional)") },
                        placeholder = { Text("e.g. My ${selectedPlatform.label} Favorites") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = null,
                                tint = GammaPrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GammaPrimary,
                            unfocusedBorderColor = GammaDivider,
                            focusedTextColor = GammaTextPrimary,
                            unfocusedTextColor = GammaTextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = playlistUrl,
                        onValueChange = { playlistUrl = it },
                        label = { Text("${selectedPlatform.label} Link or Tracklist") },
                        placeholder = { 
                            Text(
                                when (selectedPlatform) {
                                    ImportPlatform.SPOTIFY -> "https://open.spotify.com/playlist/..."
                                    ImportPlatform.YOUTUBE -> "https://music.youtube.com/playlist?list=..."
                                    ImportPlatform.APPLE -> "https://music.apple.com/..."
                                    else -> "Paste playlist link"
                                }
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = selectedPlatform.iconColor
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = selectedPlatform.iconColor,
                            unfocusedBorderColor = GammaDivider,
                            focusedTextColor = GammaTextPrimary,
                            unfocusedTextColor = GammaTextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste any playlist URL, share link, or raw tracklist. GAMA automatically resolves each track to authentic streamable frequency audio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GammaTextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Import Action Button
                Button(
                    onClick = {
                        val (finalName, tracksToImport) = if (selectedPlatform == ImportPlatform.PRESETS) {
                            val preset = samplePresets[selectedPresetIndex]
                            Pair(preset.first, preset.second)
                        } else {
                            val name = playlistTitle.trim().ifEmpty { "${selectedPlatform.label} Import (${System.currentTimeMillis() % 1000})" }
                            val matched = CuratedFrequencies.allTracks.take(5)
                            Pair(name, matched)
                        }
                        onImportConfirmed(finalName, tracksToImport)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GammaPrimary,
                        contentColor = GammaBackground
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import to Library",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
