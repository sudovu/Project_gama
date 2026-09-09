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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalWifiConnectedNoInternet4
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.util.NetworkMonitor
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary

@Composable
fun GammaInternetBanner(
    isOnline: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOnline) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(GammaSurfaceElevated)
                .border(1.dp, GammaPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("internet_access_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(GammaSurfaceHighlight, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "No internet",
                            tint = GammaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Offline Orbit",
                            style = MaterialTheme.typography.titleMedium,
                            color = GammaTextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Internet required for YouTube & YouTube Music",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GammaTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GammaPrimary)
                        .clickable(onClick = onOpenSettings)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("network_settings_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = GammaSurfaceElevated,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Connect",
                            style = MaterialTheme.typography.labelSmall,
                            color = GammaSurfaceElevated
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GammaInternetAccessDialog(
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GammaSurfaceElevated,
        icon = {
            Icon(
                imageVector = Icons.Default.SignalWifiConnectedNoInternet4,
                contentDescription = null,
                tint = GammaPrimary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Internet Access Required",
                style = MaterialTheme.typography.titleLarge,
                color = GammaTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "To search and stream authorized audio frequencies from YouTube & YouTube Music, GAMMA requires an active internet connection (Wi-Fi or Mobile Data).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GammaTextSecondary
                )
                Text(
                    text = "You can still browse and play all your uploaded music and offline frequencies in your Orbit while disconnected.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GammaTextMuted
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    NetworkMonitor.openNetworkSettings(context)
                    onDismiss()
                },
                modifier = Modifier.testTag("dialog_open_settings_button")
            ) {
                Text("Open Network Settings", color = GammaPrimary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onRetry()
                    onDismiss()
                }
            ) {
                Text("Check Connection", color = GammaTextSecondary)
            }
        }
    )
}
