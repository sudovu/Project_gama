package com.example.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary
import com.example.ui.theme.GammaTextSecondary
import kotlin.math.roundToInt

@Composable
fun GammaSoundCloudWaveformScrubber(
    peaks: FloatArray,
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    heightDp: Int = 54
) {
    val density = LocalDensity.current
    val safeDuration = durationMs.coerceAtLeast(1000L)
    val currentOnSeek by rememberUpdatedState(onSeek)

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val actualProgress = (positionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val displayProgress = if (isDragging) dragProgress else actualProgress
    val displayMs = if (isDragging) (dragProgress * safeDuration).toLong() else positionMs

    val horizontalPaddingDp = 12.dp
    val horizontalPaddingPx = with(density) { horizontalPaddingDp.toPx() }
    var canvasWidthPx by remember { mutableFloatStateOf(0f) }

    val playedBrush = Brush.verticalGradient(
        colors = listOf(GammaSecondary, GammaPrimary)
    )
    val unplayedColor = GammaSurfaceHighlight.copy(alpha = 0.85f)
    val unplayedBorderColor = GammaDivider.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("soundcloud_waveform_scrubber")
    ) {
        // Floating Time Indicator Bubble on Drag
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            if (isDragging) {
                val availableWidth = (canvasWidthPx - 2 * horizontalPaddingPx).coerceAtLeast(1f)
                val bubbleWidthDp = 54.dp
                val bubbleWidthPx = with(density) { bubbleWidthDp.toPx() }
                val targetOffset = (horizontalPaddingPx + displayProgress * availableWidth - (bubbleWidthPx / 2f))
                    .coerceIn(horizontalPaddingPx, (canvasWidthPx - horizontalPaddingPx - bubbleWidthPx).coerceAtLeast(0f))

                Box(
                    modifier = Modifier
                        .offset { IntOffset(targetOffset.roundToInt(), 0) }
                        .clip(RoundedCornerShape(6.dp))
                        .background(GammaSurfaceElevated)
                        .border(1.dp, GammaPrimary, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formatTimecode(displayMs),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        color = GammaPrimary
                    )
                }
            }
        }

        // 60fps Waveform Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .pointerInput(safeDuration) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val totalW = size.width.toFloat()
                        canvasWidthPx = totalW
                        val availW = (totalW - 2 * horizontalPaddingPx).coerceAtLeast(1f)

                        isDragging = true
                        dragProgress = ((down.position.x - horizontalPaddingPx) / availW).coerceIn(0f, 1f)
                        down.consume()

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: break
                            if (change.pressed) {
                                dragProgress = ((change.position.x - horizontalPaddingPx) / availW).coerceIn(0f, 1f)
                                change.consume()
                            } else {
                                val targetMs = (dragProgress * safeDuration).toLong()
                                currentOnSeek(targetMs)
                                isDragging = false
                                break
                            }
                        }
                    }
                }
        ) {
            canvasWidthPx = size.width
            val totalW = size.width
            val totalH = size.height
            val availW = (totalW - 2 * horizontalPaddingPx).coerceAtLeast(1f)

            val barCount = peaks.size.coerceAtLeast(20)
            val barSpacing = 2.dp.toPx()
            val barWidth = ((availW - (barSpacing * (barCount - 1))) / barCount).coerceAtLeast(1.5f)
            val midY = totalH * 0.5f

            for (i in 0 until barCount) {
                val barProgress = i.toFloat() / barCount
                val isPlayed = barProgress <= displayProgress

                val peak = peaks.getOrElse(i) { 0.2f }.coerceIn(0.12f, 1.0f)
                val barHeight = (totalH * 0.90f * peak).coerceAtLeast(4.dp.toPx())
                val top = midY - (barHeight * 0.65f) // Asymmetric SoundCloud balance (more height above baseline)

                val x = horizontalPaddingPx + i * (barWidth + barSpacing)

                if (isPlayed) {
                    // Played bar with vibrant gradient & slight glow
                    drawRoundRect(
                        brush = playedBrush,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                } else {
                    // Unplayed bar with muted cyber surface
                    drawRoundRect(
                        color = unplayedColor,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            // Scrubber Needle Playhead
            val playheadX = horizontalPaddingPx + (displayProgress * availW)
            drawLine(
                brush = Brush.verticalGradient(listOf(Color.White, GammaPrimary)),
                start = Offset(playheadX, 0f),
                end = Offset(playheadX, totalH),
                strokeWidth = 2.dp.toPx()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPaddingDp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTimecode(displayMs),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                ),
                color = if (isDragging) GammaPrimary else GammaTextSecondary
            )

            Text(
                text = formatTimecode(safeDuration),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                ),
                color = GammaTextMuted
            )
        }
    }
}

private fun formatTimecode(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60
    val secs = totalSec % 60
    return "%d:%02d".format(mins, secs)
}
