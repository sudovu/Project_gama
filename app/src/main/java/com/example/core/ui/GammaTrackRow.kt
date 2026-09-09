package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Track
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary

@Composable
fun GammaTrackRow(
    track: Track,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onDownloadClick: (() -> Unit)? = null,
    isDownloaded: Boolean = track.isDownloaded,
    isDownloading: Boolean = false,
    onMoreOptionsClick: (() -> Unit)? = null
) {
    val bg = if (isCurrentTrack) GammaSurfaceHighlight else GammaSurfaceElevated.copy(alpha = 0.6f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("track_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artwork
        GammaArtwork(
            url = track.artworkUrl,
            contentDescription = "${track.title} artwork",
            modifier = Modifier.size(52.dp),
            hasGlowBorder = isCurrentTrack
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Info
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isCurrentTrack) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(GammaPrimary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrentTrack) GammaPrimary else GammaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GammaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = " • ${track.genre}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GammaPrimary.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )

                // Source Badge
                if (track.isUploaded) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GammaSecondary.copy(alpha = 0.25f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "UPLOADED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GammaSecondary
                        )
                    }
                } else if (track.source == "youtube_music") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF0000).copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "YT MUSIC",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4E45)
                        )
                    }
                } else if (track.source == "youtube") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFF0000).copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "YOUTUBE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5252)
                        )
                    }
                }

                // Offline Downloaded Badge
                if (isDownloaded || track.isDownloaded || track.localAudioUri.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GammaSecondary.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "OFFLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GammaSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Duration
        Text(
            text = track.formattedDuration,
            style = MaterialTheme.typography.bodyMedium,
            color = GammaTextMuted
        )

        // Download Action / Status
        if (isDownloading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp),
                strokeWidth = 2.dp,
                color = GammaPrimary
            )
        } else if (isDownloaded || track.isDownloaded || track.localAudioUri.isNotBlank()) {
            IconButton(
                onClick = { onDownloadClick?.invoke() },
                modifier = Modifier.testTag("downloaded_indicator_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.DownloadDone,
                    contentDescription = "Downloaded offline",
                    tint = GammaSecondary
                )
            }
        } else if (onDownloadClick != null) {
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier.testTag("download_button_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download locally",
                    tint = GammaTextMuted
                )
            }
        }

        // Favorite Button
        IconButton(
            onClick = onFavoriteToggle,
            modifier = Modifier.testTag("favorite_button_${track.id}")
        ) {
            Icon(
                imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (track.isFavorite) "Remove from favorites" else "Add to favorites",
                tint = if (track.isFavorite) GammaPrimary else GammaTextMuted
            )
        }

        // More options
        if (onMoreOptionsClick != null) {
            IconButton(
                onClick = onMoreOptionsClick,
                modifier = Modifier.testTag("more_button_${track.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = GammaTextMuted
                )
            }
        }
    }
}
