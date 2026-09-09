package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.ui.theme.GammaGlowCyan
import com.example.ui.theme.GammaPrimary
import com.example.ui.theme.GammaSurfaceElevated
import com.example.ui.theme.GammaSurfaceHighlight

@Composable
fun GammaArtwork(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    hasGlowBorder: Boolean = false
) {
    val borderModifier = if (hasGlowBorder) {
        Modifier.border(1.dp, GammaGlowCyan, shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(borderModifier)
            .background(GammaSurfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        if (url.isNotBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(GammaSurfaceHighlight)
                    )
                },
                error = {
                    ArtworkFallback()
                }
            )
        } else {
            ArtworkFallback()
        }
    }
}

@Composable
private fun ArtworkFallback() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GammaSurfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.GraphicEq,
            contentDescription = null,
            tint = GammaPrimary.copy(alpha = 0.6f)
        )
    }
}
