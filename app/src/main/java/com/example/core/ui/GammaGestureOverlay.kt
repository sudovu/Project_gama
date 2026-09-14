package com.example.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GammaFrequencyBrush
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaSurfaceGlass
import com.example.ui.theme.GammaTextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ActiveGestureHud {
    NONE,
    SKIP_BACK_5,
    SKIP_FORWARD_5,
    HOLD_REWIND,
    HOLD_2X_SPEED
}

/**
 * Gesture overlay wrapper for the GAMMA Hero Player / Video stage.
 * Provides double-tap to skip 5 seconds, long-press to scrub rewind or activate 2x fast-forward speed,
 * and animated feedback badges.
 */
@Composable
fun GammaGestureOverlay(
    onSkipBackward5: () -> Unit,
    onSkipForward5: () -> Unit,
    onContinuousRewind: () -> Unit,
    onFastForwardStart: () -> Unit,
    onFastForwardEnd: () -> Unit,
    onSingleTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    var activeHud by remember { mutableStateOf(ActiveGestureHud.NONE) }
    val hudScale = remember { Animatable(1.0f) }

    // Auto-dismiss transient skip HUDs
    LaunchedEffect(activeHud) {
        if (activeHud == ActiveGestureHud.SKIP_BACK_5 || activeHud == ActiveGestureHud.SKIP_FORWARD_5) {
            hudScale.snapTo(0.8f)
            hudScale.animateTo(1.05f, tween(120))
            hudScale.animateTo(1.0f, tween(80))
            delay(650)
            activeHud = ActiveGestureHud.NONE
        }
    }

    Box(
        modifier = modifier
            .testTag("gamma_gesture_overlay")
            .pointerInput(Unit) {
                var lastTapTime = 0L
                var lastTapX = 0f

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downTime = System.currentTimeMillis()
                    val downX = down.position.x
                    val width = size.width

                    var isLongPress = false
                    var continuousJob: kotlinx.coroutines.Job? = null

                    // Watch for long press
                    val longPressTimeout = 320L
                    scope.launch {
                        delay(longPressTimeout)
                        if (!down.isConsumed) {
                            isLongPress = true
                            if (downX < width * 0.42f) {
                                // Hold Rewind
                                activeHud = ActiveGestureHud.HOLD_REWIND
                                continuousJob = scope.launch {
                                    while (activeHud == ActiveGestureHud.HOLD_REWIND) {
                                        onContinuousRewind()
                                        delay(260)
                                    }
                                }
                            } else if (downX > width * 0.58f) {
                                // Hold 2X Speed
                                activeHud = ActiveGestureHud.HOLD_2X_SPEED
                                onFastForwardStart()
                            }
                        }
                    }

                    val upOrCancel: PointerInputChange? = waitForUpOrCancellation()
                    continuousJob?.cancel()

                    if (isLongPress) {
                        if (activeHud == ActiveGestureHud.HOLD_2X_SPEED) {
                            onFastForwardEnd()
                        }
                        activeHud = ActiveGestureHud.NONE
                    } else if (upOrCancel != null) {
                        val tapTime = System.currentTimeMillis()
                        val diff = tapTime - lastTapTime
                        val distanceX = kotlin.math.abs(downX - lastTapX)

                        if (diff < 320 && distanceX < width * 0.35f) {
                            // Double Tap detected!
                            if (downX < width * 0.48f) {
                                activeHud = ActiveGestureHud.SKIP_BACK_5
                                onSkipBackward5()
                            } else {
                                activeHud = ActiveGestureHud.SKIP_FORWARD_5
                                onSkipForward5()
                            }
                            lastTapTime = 0L
                        } else {
                            // Single tap candidate - wait briefly to see if another tap arrives
                            lastTapTime = tapTime
                            lastTapX = downX
                            scope.launch {
                                delay(320)
                                if (lastTapTime == tapTime && activeHud == ActiveGestureHud.NONE) {
                                    onSingleTap()
                                }
                            }
                        }
                    } else {
                        // Cancelled
                        if (activeHud == ActiveGestureHud.HOLD_2X_SPEED) {
                            onFastForwardEnd()
                        }
                        activeHud = ActiveGestureHud.NONE
                    }
                }
            }
    ) {
        // Child content (Hero Artwork or YouTube Player)
        content()

        // HUD: Left Skip -5s
        AnimatedVisibility(
            visible = activeHud == ActiveGestureHud.SKIP_BACK_5,
            enter = fadeIn(tween(80)) + scaleIn(tween(100)),
            exit = fadeOut(tween(180)) + scaleOut(tween(180)),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(hudScale.value)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, GammaPrimary, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Replay5,
                        contentDescription = "Skip back 5s",
                        tint = GammaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "-5s",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = GammaTextPrimary
                    )
                }
            }
        }

        // HUD: Right Skip +5s
        AnimatedVisibility(
            visible = activeHud == ActiveGestureHud.SKIP_FORWARD_5,
            enter = fadeIn(tween(80)) + scaleIn(tween(100)),
            exit = fadeOut(tween(180)) + scaleOut(tween(180)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(hudScale.value)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(1.dp, GammaPrimary, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+5s",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = GammaTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Filled.Forward5,
                        contentDescription = "Skip forward 5s",
                        tint = GammaPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // HUD: Hold 2X Speed
        AnimatedVisibility(
            visible = activeHud == ActiveGestureHud.HOLD_2X_SPEED,
            enter = fadeIn(tween(100)) + scaleIn(tween(100)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.9f))
                    .border(1.5.dp, GammaFrequencyBrush, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FastForward,
                        contentDescription = "2x speed",
                        tint = GammaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚡ 2X SPEED",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = GammaPrimary
                    )
                }
            }
        }

        // HUD: Hold Rewind
        AnimatedVisibility(
            visible = activeHud == ActiveGestureHud.HOLD_REWIND,
            enter = fadeIn(tween(100)) + scaleIn(tween(100)),
            exit = fadeOut(tween(150)) + scaleOut(tween(150)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.9f))
                    .border(1.5.dp, GammaSecondary, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FastRewind,
                        contentDescription = "Rewinding",
                        tint = GammaSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⏪ REWINDING...",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = GammaSecondary
                    )
                }
            }
        }
    }
}
