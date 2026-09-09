package com.example.core.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary

@Composable
fun GammaUploadMusicDialog(
    onDismiss: () -> Unit,
    onUploadConfirmed: (
        title: String,
        artist: String,
        frequencyHz: Int,
        genre: String,
        youtubeUrlOrId: String,
        localAudioUri: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Personal Orbit") }
    var youtubeUrl by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableIntStateOf(432) }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAudioName by remember { mutableStateOf("") }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedAudioUri = uri
            val path = uri.lastPathSegment ?: "Local Audio Track"
            val cleanName = path.substringAfterLast("/").substringBeforeLast(".")
            selectedAudioName = cleanName
            if (title.isBlank()) {
                title = cleanName
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GammaSurfaceElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = GammaPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Upload Music",
                    style = MaterialTheme.typography.titleLarge,
                    color = GammaTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Uploaded music will be stored locally on your device, discoverable on your Discover feed, and available in your Library.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GammaTextSecondary
                )

                // Pick Local File Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GammaSurfaceHighlight)
                        .border(1.dp, GammaDivider, RoundedCornerShape(12.dp))
                        .clickable { audioPicker.launch("audio/*") }
                        .padding(12.dp)
                        .testTag("pick_audio_file_button")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(GammaPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = GammaSurfaceElevated,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (selectedAudioName.isNotEmpty()) selectedAudioName else "Select Audio File from Device",
                                style = MaterialTheme.typography.titleMedium,
                                color = GammaTextPrimary,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (selectedAudioUri != null) "File attached" else "Tap to import MP3, WAV, FLAC, AAC",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (selectedAudioUri != null) GammaPrimary else GammaTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Track Title *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GammaTextPrimary,
                        unfocusedTextColor = GammaTextPrimary,
                        focusedBorderColor = GammaPrimary,
                        unfocusedBorderColor = GammaDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("upload_title_input")
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GammaTextPrimary,
                        unfocusedTextColor = GammaTextPrimary,
                        focusedBorderColor = GammaPrimary,
                        unfocusedBorderColor = GammaDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("upload_artist_input")
                )

                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text("Genre (e.g. Pop, Acoustic, Rock)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GammaTextPrimary,
                        unfocusedTextColor = GammaTextPrimary,
                        focusedBorderColor = GammaPrimary,
                        unfocusedBorderColor = GammaDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("upload_genre_input")
                )

                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("YouTube / YouTube Music URL (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GammaTextPrimary,
                        unfocusedTextColor = GammaTextPrimary,
                        focusedBorderColor = GammaPrimary,
                        unfocusedBorderColor = GammaDivider
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("upload_youtube_input")
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onUploadConfirmed(
                            title,
                            artist.ifEmpty { "Community Creator" },
                            selectedFrequency,
                            genre.ifEmpty { "Custom" },
                            youtubeUrl,
                            selectedAudioUri?.toString() ?: ""
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("upload_confirm_button")
            ) {
                Text("Add to Library", color = GammaPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GammaTextSecondary)
            }
        }
    )
}
