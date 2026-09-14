package com.example.feature.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.ui.GammaFilterChip
import com.example.core.ui.GammaSectionHeader
import com.example.ui.theme.GammaAuraBrush
import com.example.ui.theme.GammaBackground
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.GammaPrimaryVariant
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.lazy.items
import com.example.core.media.PlaybackManager
import com.example.domain.model.EqualizerSettings
import com.example.domain.model.TuningPreset
import com.example.core.taste.TastePreferenceManager
import com.example.ui.theme.GammaThemeManager
import com.example.ui.theme.GammaThemePreset
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    playbackManager: PlaybackManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val playbackState = playbackManager?.playbackState?.collectAsStateWithLifecycle()?.value
    val eqSettings = playbackState?.equalizerSettings ?: EqualizerSettings()

    var selectedFrequency by remember { mutableIntStateOf(432) }
    var spatialAudioEnabled by remember { mutableStateOf(true) }
    var visualizerPulseEnabled by remember { mutableStateOf(true) }

    val tasteManager = remember { TastePreferenceManager.getInstance(context) }
    val isSpotifyLinked by tasteManager.isSpotifyLinked.collectAsStateWithLifecycle()
    val spotifyUser by tasteManager.spotifyUsername.collectAsStateWithLifecycle()
    val spotifyTastes by tasteManager.spotifyTastes.collectAsStateWithLifecycle()
    val isGoogleLinked by tasteManager.isGoogleLinked.collectAsStateWithLifecycle()
    val googleEmail by tasteManager.googleEmail.collectAsStateWithLifecycle()
    val youtubeTastes by tasteManager.youtubeTastes.collectAsStateWithLifecycle()

    val developerName = "VHUWON MATHERS"
    val developerEmail = "vhuwonmathers@gmail.com"
    val developerUsername = "sudovu"
    val developerPhone = "9869367788"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GammaBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GammaPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "About & Developer",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = GammaTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GammaAuraBrush)
                .testTag("settings_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // ==========================================
            // APPLICATION ARCHITECTURE & ABOUT
            // ==========================================
            item {
                Text(
                    text = "APPLICATION SPECIFICATIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GammaPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(GammaSurfaceElevated)
                        .border(1.dp, GammaDivider, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GammaPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = GammaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "GAMA Audio Engine",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GammaTextPrimary
                                )
                                Text(
                                    text = "Version 1.2.0 (Official Release)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GammaPrimary
                                )
                            }
                        }

                        Text(
                            text = "Engineered with precision by Bhuwan Gautam (sudovu). Powered by Kotlin, Jetpack Compose, Room local metadata persistence, 5-band real-time DSP audio equalizer, and the official YouTube embedded playback bridge.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GammaTextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // ==========================================
            // THEME & VISUAL PALETTE SELECTION
            // ==========================================
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = GammaPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "THEME & COLOR HARMONICS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GammaPrimary
                        )
                    }
                    Text(
                        text = GammaThemeManager.selectedThemePreset.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GammaTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GammaThemePreset.entries.forEach { preset ->
                        val isSelected = GammaThemeManager.selectedThemePreset == preset
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) GammaPrimary.copy(alpha = 0.12f)
                                    else GammaSurfaceElevated
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) GammaPrimary else GammaDivider,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    GammaThemeManager.selectTheme(preset, context)
                                    Toast.makeText(
                                        context,
                                        "Theme applied: ${preset.title}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(14.dp)
                                .testTag("theme_preset_${preset.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = preset.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isSelected) GammaPrimary else GammaTextPrimary
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(GammaPrimary.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.sp
                                                    ),
                                                    color = GammaPrimary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = GammaTextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Color preview swatches
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(preset.primaryColor)
                                            .border(1.dp, GammaDivider, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(preset.secondaryColor)
                                            .border(1.dp, GammaDivider, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(preset.accentColor)
                                            .border(1.dp, GammaDivider, CircleShape)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(preset.backgroundColor)
                                            .border(1.dp, GammaDivider, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // RESONANCE TUNING & AUDIO CALIBRATION
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIO EQUALIZER & DSP HARMONICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GammaPrimary
                    )
                    Switch(
                        checked = eqSettings.isEnabled,
                        onCheckedChange = {
                            playbackManager?.toggleEqualizer(it)
                            Toast.makeText(context, if (it) "Hardware DSP Equalizer Enabled" else "Equalizer Bypassed", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GammaPrimary,
                            checkedTrackColor = GammaPrimaryVariant
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(GammaSurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (eqSettings.isEnabled) GammaPrimary.copy(alpha = 0.5f) else GammaDivider,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Active Sound Signature",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GammaTextPrimary
                                )
                                Text(
                                    text = "Preset: ${eqSettings.currentPreset.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GammaPrimary
                                )
                            }
                        }

                        // Presets Row
                        val presetMap = listOf(
                            Pair("Balanced", TuningPreset.FLAT),
                            Pair("Bass Boost", TuningPreset.BASS_BOOST),
                            Pair("Vocal Focus", TuningPreset.VOCAL_CLARITY),
                            Pair("Synthwave", TuningPreset.SYNTHWAVE),
                            Pair("Electronic", TuningPreset.ELECTRONIC)
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(presetMap) { (label, preset) ->
                                val isSelected = eqSettings.currentPreset == preset
                                GammaFilterChip(
                                    text = label,
                                    selected = isSelected,
                                    onClick = {
                                        playbackManager?.setTuningPreset(preset)
                                        Toast.makeText(context, "Applied $label profile", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                        // 5-Band Interactive Equalizer Sliders
                        Text(
                            text = "5-BAND FREQUENCY SHAPING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GammaTextSecondary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            eqSettings.bands.forEach { band ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = band.label,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = GammaTextPrimary
                                        )
                                        val sign = if (band.gainDb > 0f) "+" else ""
                                        Text(
                                            text = "$sign%.1f dB".format(band.gainDb),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (band.gainDb != 0f) GammaPrimary else GammaTextMuted
                                        )
                                    }
                                    Slider(
                                        value = band.gainDb,
                                        onValueChange = { newDb ->
                                            playbackManager?.setEqualizerBand(band.index, newDb)
                                        },
                                        valueRange = -12f..12f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = GammaPrimary,
                                            activeTrackColor = GammaPrimary,
                                            inactiveTrackColor = GammaSurfaceHighlight
                                        ),
                                        enabled = eqSettings.isEnabled
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // AUDIO & VISUAL ENGINE SWITCHES
            // ==========================================
            item {
                Text(
                    text = "AUDIO & VISUAL ENGINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GammaPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(GammaSurfaceElevated)
                        .border(1.dp, GammaDivider, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Spatial Zero-Phase Matrix",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = GammaTextPrimary
                                )
                                Text(
                                    text = "Widens stereo soundfield for deep headphone listening.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GammaTextSecondary
                                )
                            }
                            Switch(
                                checked = spatialAudioEnabled,
                                onCheckedChange = {
                                    spatialAudioEnabled = it
                                    playbackManager?.setSpatialAudioEnabled(it)
                                    Toast.makeText(context, if (it) "Spatial Audio Activated" else "Spatial Audio Disabled", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GammaBackground,
                                    checkedTrackColor = GammaPrimary,
                                    uncheckedThumbColor = GammaTextMuted,
                                    uncheckedTrackColor = GammaDivider
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Dynamic Frequency Spectrum",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = GammaTextPrimary
                                )
                                Text(
                                    text = "Renders real-time audio waveform equalizer on player.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GammaTextSecondary
                                )
                            }
                            Switch(
                                checked = visualizerPulseEnabled,
                                onCheckedChange = { visualizerPulseEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = GammaBackground,
                                    checkedTrackColor = GammaPrimary,
                                    uncheckedThumbColor = GammaTextMuted,
                                    uncheckedTrackColor = GammaDivider
                                )
                            )
                        }
                    }
                }
            }

            // ==========================================
            // STREAMING & TASTE INTEGRATIONS (Spotify & Google / YouTube Music)
            // ==========================================
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONNECTED ACCOUNTS & MUSIC TASTE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GammaPrimary
                    )
                    if (isSpotifyLinked || isGoogleLinked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GammaPrimary.copy(alpha = 0.15f))
                                .clickable {
                                    tasteManager.syncAllTastes()
                                    Toast.makeText(context, "Music tastes synchronized with Discover feed!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("sync_all_tastes_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    tint = GammaPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SYNC TASTES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = GammaPrimary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Spotify Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(GammaSurfaceElevated)
                            .border(
                                width = if (isSpotifyLinked) 1.5.dp else 1.dp,
                                color = if (isSpotifyLinked) Color(0xFF1DB954) else GammaDivider,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(16.dp)
                            .testTag("spotify_integration_card")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                            .background(Color(0xFF1DB954).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = "Spotify",
                                            tint = Color(0xFF1DB954),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Spotify",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GammaTextPrimary
                                        )
                                        Text(
                                            text = if (isSpotifyLinked) spotifyUser.ifEmpty { "Connected User" } else "Not Connected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSpotifyLinked) Color(0xFF1DB954) else GammaTextMuted
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSpotifyLinked) Color(0xFF1DB954).copy(alpha = 0.2f)
                                            else GammaSurfaceHighlight
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (isSpotifyLinked) "LINKED" else "UNLINKED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (isSpotifyLinked) Color(0xFF1DB954) else GammaTextMuted
                                    )
                                }
                            }

                            if (isSpotifyLinked) {
                                Text(
                                    text = "Referenced Taste Profile:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = GammaTextSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    spotifyTastes.take(3).forEach { taste ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF1DB954).copy(alpha = 0.12f))
                                                .border(1.dp, Color(0xFF1DB954).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = taste,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color(0xFF1DB954)
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            tasteManager.syncAllTastes()
                                            Toast.makeText(context, "Spotify tastes updated & applied to Discover!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1DB954))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sync Taste", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1DB954))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            tasteManager.unlinkSpotify()
                                            Toast.makeText(context, "Spotify unlinked", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Unlink", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF4D4D))
                                    }
                                }
                            } else {
                                Text(
                                    text = "Connect your Spotify account to analyze listening history, top genres, and adapt GAMA soundwave recommendations.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GammaTextSecondary
                                )
                                Button(
                                    onClick = {
                                        tasteManager.linkSpotify("Spotify User", listOf("Rock & Metal", "Alternative Rock", "Cyberpunk", "Nu Metal"))
                                        Toast.makeText(context, "Spotify account linked! Taste profile synced.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Link Spotify Account", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                                }
                            }
                        }
                    }

                    // Google & YouTube Music Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(GammaSurfaceElevated)
                            .border(
                                width = if (isGoogleLinked) 1.5.dp else 1.dp,
                                color = if (isGoogleLinked) Color(0xFFFF0000) else GammaDivider,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(16.dp)
                            .testTag("google_integration_card")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                            .background(Color(0xFFFF0000).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sensors,
                                            contentDescription = "YouTube Music",
                                            tint = Color(0xFFFF0000),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Google & YouTube Music",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GammaTextPrimary
                                        )
                                        Text(
                                            text = if (isGoogleLinked) googleEmail.ifEmpty { "Connected Account" } else "Not Connected",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isGoogleLinked) Color(0xFFFF4D4D) else GammaTextMuted
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isGoogleLinked) Color(0xFFFF0000).copy(alpha = 0.2f)
                                            else GammaSurfaceHighlight
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = if (isGoogleLinked) "LINKED" else "UNLINKED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (isGoogleLinked) Color(0xFFFF4D4D) else GammaTextMuted
                                    )
                                }
                            }

                            if (isGoogleLinked) {
                                Text(
                                    text = "Referenced Taste Profile:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = GammaTextSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    youtubeTastes.take(3).forEach { taste ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFF0000).copy(alpha = 0.12f))
                                                .border(1.dp, Color(0xFFFF0000).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = taste,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = Color(0xFFFF4D4D)
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            tasteManager.syncAllTastes()
                                            Toast.makeText(context, "YouTube Music tastes updated & applied to Discover!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFF4D4D))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sync Taste", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF4D4D))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            tasteManager.unlinkGoogle()
                                            Toast.makeText(context, "Google unlinked", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Unlink", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF4D4D))
                                    }
                                }
                            } else {
                                Text(
                                    text = "Connect your Google account to sync your YouTube Music playlists, artist preferences, and frequency resonances.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GammaTextSecondary
                                )
                                Button(
                                    onClick = {
                                        tasteManager.linkGoogle("user@gmail.com", listOf("432Hz Ambient", "Progressive Metal", "Synthwave", "Heavy Drums"))
                                        Toast.makeText(context, "Google account linked! Taste profile synced.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Link Google Account", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // PROVIDER COMPLIANCE & ATTRIBUTION
            // ==========================================
            item {
                Text(
                    text = "PROVIDER COMPLIANCE & ATTRIBUTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GammaPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(GammaSurfaceElevated)
                        .border(1.dp, GammaDivider, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GammaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Official YouTube IFrame Embed Integration",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GammaTextPrimary
                            )
                        }

                        Text(
                            text = "GAMMA operates under official YouTube developer terms. Playback is rendered through the official YouTube embedded player without scraping, ripping, or modifying audio streams, respecting creator rights and platform guidelines.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GammaTextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Designed & Built by Bhuwan Gautam (sudovu)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = GammaPrimary
                        )
                    }
                }
            }

            // ==========================================
            // DEVELOPED BY - HERO SHOWCASE SECTION (PLACED AT BOTTOM)
            // ==========================================
            item {
                AboutDeveloperSection(
                    developerName = developerName,
                    developerUsername = developerUsername,
                    developerEmail = developerEmail,
                    developerPhone = developerPhone,
                    context = context,
                    clipboardManager = clipboardManager
                )
            }
        }
    }
}

@Composable
private fun AboutDeveloperSection(
    developerName: String,
    developerUsername: String,
    developerEmail: String,
    developerPhone: String,
    context: Context,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    Text(
        text = "ABOUT THE CREATOR",
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold
        ),
        color = GammaPrimary
    )
    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GammaPrimary, GammaSecondary, Color(0xFF38BDF8))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        GammaSurfaceHighlight,
                        GammaSurfaceElevated
                    )
                )
            )
            .padding(20.dp)
            .testTag("about_developer_card")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // "DEVELOPED BY" Tag Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(GammaPrimary.copy(alpha = 0.15f))
                    .border(1.dp, GammaPrimary.copy(alpha = 0.4f), RoundedCornerShape(30.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = GammaPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DEVELOPED BY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        ),
                        color = GammaPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Permanent Profile Picture with glowing dual neon rings
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.sweepGradient(
                            colors = listOf(
                                GammaPrimary,
                                GammaSecondary,
                                Color(0xFF38BDF8),
                                GammaPrimary
                            )
                        )
                    )
                    .padding(3.5.dp)
                    .testTag("profile_avatar_permanent")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_vhuwon_profile),
                    contentDescription = "VHUWON MATHERS Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(GammaBackground),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Developer Name (Bold & High Contrast)
            Text(
                text = developerName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                color = GammaTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Username Pill Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(GammaSurfaceElevated)
                    .border(1.dp, GammaDivider, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Verified Developer",
                    tint = GammaPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "@$developerUsername",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GammaPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lead Software Engineer & Android Architect",
                style = MaterialTheme.typography.bodyMedium,
                color = GammaTextSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Detailed Contact Information Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Email Contact Tile
                DeveloperContactCard(
                    icon = Icons.Default.Email,
                    iconTint = GammaPrimary,
                    label = "OFFICIAL EMAIL",
                    value = developerEmail,
                    subtitle = "Direct Developer Correspondence",
                    actionLabel = "Send Mail",
                    actionIcon = Icons.Default.OpenInNew,
                    onClick = {
                        copyAndLaunch(
                            context = context,
                            clipboardManager = clipboardManager,
                            label = "Email",
                            text = developerEmail,
                            intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$developerEmail")
                            }
                        )
                    },
                    testTag = "about_email_card"
                )

                // Phone Contact Tile
                DeveloperContactCard(
                    icon = Icons.Default.Phone,
                    iconTint = GammaSecondary,
                    label = "PHONE NUMBER",
                    value = developerPhone,
                    subtitle = "+977 $developerPhone",
                    actionLabel = "Call",
                    actionIcon = Icons.Default.Call,
                    onClick = {
                        copyAndLaunch(
                            context = context,
                            clipboardManager = clipboardManager,
                            label = "Phone",
                            text = developerPhone,
                            intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$developerPhone")
                            }
                        )
                    },
                    testTag = "about_phone_card"
                )

                // Username / GitHub Handle Tile
                DeveloperContactCard(
                    icon = Icons.Default.Person,
                    iconTint = Color(0xFFF59E0B),
                    label = "USERNAME",
                    value = developerUsername,
                    subtitle = "GitHub & Cosmic Identity",
                    actionLabel = "Copy",
                    actionIcon = Icons.Default.ContentCopy,
                    onClick = {
                        clipboardManager.setText(AnnotatedString(developerUsername))
                        Toast.makeText(context, "Username copied: $developerUsername", Toast.LENGTH_SHORT).show()
                    },
                    testTag = "about_username_card"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val fullDetails = """
                            Developer: $developerName
                            Username: @$developerUsername
                            Email: $developerEmail
                            Phone: $developerPhone
                            Project: GAMMA Frequency Audio
                        """.trimIndent()
                        clipboardManager.setText(AnnotatedString(fullDetails))
                        Toast.makeText(context, "All developer details copied!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GammaPrimary,
                        contentColor = GammaBackground
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_developer_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Copy Details",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                OutlinedButton(
                    onClick = {
                        copyAndLaunch(
                            context = context,
                            clipboardManager = clipboardManager,
                            label = "Email",
                            text = developerEmail,
                            intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$developerEmail?subject=Hello%20Bhuwan%20(GAMMA%20App)")
                            }
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GammaPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GammaPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Contact",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DeveloperContactCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subtitle: String? = null,
    actionLabel: String,
    actionIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GammaSurfaceElevated)
            .border(1.dp, GammaDivider.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GammaTextMuted
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = GammaTextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = GammaTextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GammaSurfaceHighlight)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = actionLabel,
                        tint = GammaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GammaPrimary
                    )
                }
            }
        }
    }
}

private fun copyAndLaunch(
    context: Context,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    label: String,
    text: String,
    intent: Intent
) {
    clipboardManager.setText(AnnotatedString(text))
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "$label copied: $text", Toast.LENGTH_SHORT).show()
    }
}
