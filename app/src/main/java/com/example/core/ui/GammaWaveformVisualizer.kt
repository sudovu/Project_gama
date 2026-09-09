package com.example.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSecondary
import com.example.ui.theme.GammaTertiary

@Composable
fun GammaWaveformVisualizer(
    bands: FloatArray,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    val barBrush = Brush.verticalGradient(
        colors = listOf(GammaTertiary, GammaSecondary, GammaPrimary)
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val count = bands.size.coerceAtLeast(1)
        val spacing = 4.dp.toPx()
        val totalSpacing = spacing * (count - 1)
        val barWidth = ((size.width - totalSpacing) / count).coerceAtLeast(2.dp.toPx())
        val maxHeight = size.height

        for (i in 0 until count) {
            val normalized = if (isPlaying) bands.getOrElse(i) { 0.15f }.coerceIn(0.08f, 1.0f) else 0.08f
            val barH = (maxHeight * normalized).coerceAtLeast(4.dp.toPx())
            val x = i * (barWidth + spacing)
            val y = maxHeight - barH

            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}
