package com.example.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaFrequencyBrush
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Professional curved waveform seekbar for the GAMMA Frequency Player.
 * Replaces standard flat sliders with dynamic harmonic audio waveforms,
 * glowing cyber thumb, scrub preview tooltips, and interactive touch controls.
 */
@Composable
fun GammaCurvedWaveformSeekbar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    visualizerBands: FloatArray = FloatArray(16) { 0.1f },
    barCount: Int = 46
) {
    val density = LocalDensity.current
    val safeDuration = durationMs.coerceAtLeast(1000L)

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val actualProgress = (positionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val displayProgress = if (isDragging) dragProgress else actualProgress
    val displayMs = (displayProgress * safeDuration).toLong()

    val animatedThumbRadius by animateFloatAsState(
        targetValue = if (isDragging) 8f else 5.5f,
        animationSpec = tween(durationMillis = 150),
        label = "thumbRadius"
    )

    // Pre-calculate harmonic wave profile for smooth curvature
    val waveformEnvelopes = remember(barCount) {
        FloatArray(barCount) { i ->
            val norm = i.toFloat() / (barCount - 1).coerceAtLeast(1)
            // Symmetrical sinusoidal curve combined with musical harmonics
            val baseSine = sin(norm * PI.toFloat())
            val harmonic1 = sin(norm * PI.toFloat() * 3f) * 0.18f
            val harmonic2 = cos(norm * PI.toFloat() * 5f) * 0.12f
            (0.22f + 0.78f * (baseSine + harmonic1 + harmonic2)).coerceIn(0.12f, 1.0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("curved_waveform_seekbar"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Floating scrub tooltip bubble when user is actively dragging
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isDragging,
                enter = fadeIn(tween(100)),
                exit = fadeOut(tween(100))
            ) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (displayProgress * 800).toInt().coerceIn(0, 700),
                                y = 0
                            )
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(GammaSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatTimeMs(displayMs),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = GammaPrimary
                    )
                }
            }
        }

        // Canvas Audio Waveform
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .pointerInput(safeDuration) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetMs = (ratio * safeDuration).toLong()
                        onSeek(targetMs)
                    }
                }
                .pointerInput(safeDuration) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        },
                        onDragEnd = {
                            isDragging = false
                            val targetMs = (dragProgress * safeDuration).toLong()
                            onSeek(targetMs)
                        },
                        onDragCancel = {
                            isDragging = false
                        }
                    )
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2f

            val totalSpacing = canvasWidth / barCount
            val barWidth = (totalSpacing * 0.58f).coerceAtLeast(2f)

            val currentHeadX = displayProgress * canvasWidth

            // Unplayed baseline track
            drawLine(
                color = GammaDivider.copy(alpha = 0.4f),
                start = Offset(0f, centerY),
                end = Offset(canvasWidth, centerY),
                strokeWidth = 1.5f
            )

            // Played glowing line
            if (currentHeadX > 0f) {
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GammaPrimary, GammaSecondary),
                        startX = 0f,
                        endX = currentHeadX
                    ),
                    start = Offset(0f, centerY),
                    end = Offset(currentHeadX, centerY),
                    strokeWidth = 2.5f
                )
            }

            // Draw harmonic waveform bars
            for (i in 0 until barCount) {
                val barCenterX = (i + 0.5f) * totalSpacing
                val isPlayed = barCenterX <= currentHeadX

                // Harmonic amplitude with subtle visualizer breathing
                val baseHeightFactor = waveformEnvelopes.getOrElse(i) { 0.5f }
                val bandIndex = ((i.toFloat() / barCount) * visualizerBands.size).toInt().coerceIn(0, visualizerBands.lastIndex)
                val livePulse = visualizerBands[bandIndex] * 0.35f
                val barTotalHeight = (canvasHeight * (baseHeightFactor * 0.72f + livePulse)).coerceIn(6f, canvasHeight * 0.95f)

                val barTop = centerY - barTotalHeight / 2f
                val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

                if (isPlayed) {
                    // Vibrant played bar gradient
                    val barColor = if (currentHeadX > 0f) {
                        val ratio = (barCenterX / currentHeadX).coerceIn(0f, 1f)
                        if (ratio < 0.5f) GammaPrimary else GammaSecondary
                    } else {
                        GammaPrimary
                    }
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barCenterX - barWidth / 2f, barTop),
                        size = Size(barWidth, barTotalHeight),
                        cornerRadius = cornerRadius
                    )
                } else {
                    // Muted unplayed bar
                    drawRoundRect(
                        color = Color(0xFF2A2838),
                        topLeft = Offset(barCenterX - barWidth / 2f, barTop),
                        size = Size(barWidth, barTotalHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }

            // Glowing cyber thumb
            val thumbRadiusPx = with(density) { animatedThumbRadius.dp.toPx() }
            val glowRadiusPx = thumbRadiusPx * 2.2f

            // Outer glow aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GammaGlowCyan.copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(currentHeadX, centerY),
                    radius = glowRadiusPx
                ),
                radius = glowRadiusPx,
                center = Offset(currentHeadX, centerY)
            )

            // Solid Primary Outer Ring
            drawCircle(
                color = GammaPrimary,
                radius = thumbRadiusPx,
                center = Offset(currentHeadX, centerY)
            )

            // Inner White Hot Core Dot
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx * 0.45f,
                center = Offset(currentHeadX, centerY)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time labels (Current vs Duration / Remaining)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTimeMs(displayMs),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = if (isDragging) GammaPrimary else GammaTextMuted
            )

            val remainingMs = (safeDuration - displayMs).coerceAtLeast(0L)
            Text(
                text = "-${formatTimeMs(remainingMs)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.5.sp
                ),
                color = GammaTextMuted
            )
        }
    }
}

private fun formatTimeMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
