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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GammaDivider
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaTextMuted
import com.example.ui.theme.GammaTextPrimary

/**
 * Professional continuous curved seekbar for the GAMMA Frequency Player.
 * Replaces jagged wavy up-down bars with an ultra-sleek, continuous aerodynamic
 * curved arc track ("curve only"), luminous cyber thumb, and rock-solid gesture physics.
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
        targetValue = if (isDragging) 9f else 6f,
        animationSpec = tween(durationMillis = 150),
        label = "thumbRadius"
    )

    // Track width and padding
    val horizontalPaddingDp = 16.dp
    val horizontalPaddingPx = with(density) { horizontalPaddingDp.toPx() }
    val arcHeightDp = 10.dp
    val arcHeightPx = with(density) { arcHeightDp.toPx() }
    val trackStrokeDp = 5.dp
    val trackStrokePx = with(density) { trackStrokeDp.toPx() }

    var canvasWidthPx by remember { mutableFloatStateOf(0f) }

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
                .height(24.dp)
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isDragging,
                enter = fadeIn(tween(100)),
                exit = fadeOut(tween(100))
            ) {
                val tooltipWidthPx = with(density) { 54.dp.toPx() }
                val availableTrackW = (canvasWidthPx - 2 * horizontalPaddingPx).coerceAtLeast(1f)
                val thumbCenterXPx = horizontalPaddingPx + displayProgress * availableTrackW
                val targetXPx = (thumbCenterXPx - tooltipWidthPx / 2f).coerceIn(
                    0f,
                    (canvasWidthPx - tooltipWidthPx).coerceAtLeast(0f)
                )

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = targetXPx.toInt(), y = 0) }
                        .clip(RoundedCornerShape(8.dp))
                        .background(GammaSurfaceElevated)
                        .border(1.dp, GammaPrimary.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
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

        // Continuous Curved Arc Canvas Track with Unified Rock-Solid Gestures
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(safeDuration) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val w = size.width.toFloat()
                        canvasWidthPx = w
                        val pad = horizontalPaddingPx
                        val trackW = (w - 2 * pad).coerceAtLeast(1f)

                        isDragging = true
                        var currentX = down.position.x
                        var p = ((currentX - pad) / trackW).coerceIn(0f, 1f)
                        dragProgress = p
                        down.consume()

                        val pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                            if (!change.pressed) {
                                change.consume()
                                break
                            }
                            currentX = change.position.x
                            p = ((currentX - pad) / trackW).coerceIn(0f, 1f)
                            dragProgress = p
                            change.consume()
                        }
                        isDragging = false
                        val targetMs = (dragProgress * safeDuration).toLong()
                        onSeek(targetMs)
                    }
                }
        ) {
            val canvasWidth = size.width
            canvasWidthPx = canvasWidth
            val canvasHeight = size.height

            val x0 = horizontalPaddingPx
            val x1 = canvasWidth - horizontalPaddingPx
            val trackW = (x1 - x0).coerceAtLeast(1f)
            val xc = (x0 + x1) / 2f

            // Baseline and apex of curve
            val yBase = canvasHeight * 0.72f
            val yc = yBase - 2f * arcHeightPx

            // Quadratic bezier curve point calculator
            fun getCurvePoint(t: Float): Offset {
                val clampedT = t.coerceIn(0f, 1f)
                val xt = x0 + clampedT * trackW
                val yt = yBase - 4f * arcHeightPx * clampedT * (1f - clampedT)
                return Offset(xt, yt)
            }

            // 1. Unplayed Curved Track (subtle obsidian glow with rounded ends)
            val unplayedPath = Path().apply {
                moveTo(x0, yBase)
                quadraticBezierTo(xc, yc, x1, yBase)
            }
            drawPath(
                path = unplayedPath,
                color = Color(0xFF232136),
                style = Stroke(width = trackStrokePx, cap = StrokeCap.Round)
            )

            // 2. Played Curved Track (vibrant cyan to magenta gradient along curve)
            if (displayProgress > 0.002f) {
                val qcx = x0 + displayProgress * (xc - x0)
                val qcy = yBase + displayProgress * (yc - yBase)
                val endPoint = getCurvePoint(displayProgress)

                val playedPath = Path().apply {
                    moveTo(x0, yBase)
                    quadraticBezierTo(qcx, qcy, endPoint.x, endPoint.y)
                }

                // Ambient glow stroke under played path
                drawPath(
                    path = playedPath,
                    color = GammaGlowCyan.copy(alpha = 0.22f),
                    style = Stroke(width = trackStrokePx * 2.2f, cap = StrokeCap.Round)
                )

                // Main vibrant gradient stroke
                drawPath(
                    path = playedPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(GammaPrimary, GammaSecondary),
                        startX = x0,
                        endX = x1
                    ),
                    style = Stroke(width = trackStrokePx, cap = StrokeCap.Round)
                )
            }

            // 3. Cyber Glowing Thumb Orb following curve (x(t), y(t))
            val currentThumbPos = getCurvePoint(displayProgress)
            val thumbRadiusPx = with(density) { animatedThumbRadius.dp.toPx() }
            val glowRadiusPx = thumbRadiusPx * 2.4f

            // Outer cyan aura glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GammaGlowCyan.copy(alpha = 0.55f), Color.Transparent),
                    center = currentThumbPos,
                    radius = glowRadiusPx
                ),
                radius = glowRadiusPx,
                center = currentThumbPos
            )

            // Solid Primary Outer Ring
            drawCircle(
                color = GammaPrimary,
                radius = thumbRadiusPx,
                center = currentThumbPos
            )

            // Inner White Hot Core Dot
            drawCircle(
                color = Color.White,
                radius = thumbRadiusPx * 0.42f,
                center = currentThumbPos
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Time labels (Current vs Duration / Remaining)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPaddingDp),
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
