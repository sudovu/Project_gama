package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GammaDarkColorScheme = darkColorScheme(
    primary = GammaPrimary,
    onPrimary = GammaBackground,
    primaryContainer = GammaSurfaceHighlight,
    onPrimaryContainer = GammaPrimary,
    secondary = GammaSecondary,
    onSecondary = GammaTextPrimary,
    tertiary = GammaTertiary,
    background = GammaBackground,
    onBackground = GammaTextPrimary,
    surface = GammaSurface,
    onSurface = GammaTextPrimary,
    surfaceVariant = GammaSurfaceElevated,
    onSurfaceVariant = GammaTextSecondary,
    outline = GammaDivider,
    error = GammaError
)

private val GammaLightColorScheme = lightColorScheme(
    primary = GammaPrimaryVariant,
    onPrimary = GammaLightSurface,
    primaryContainer = GammaLightSurfaceElevated,
    onPrimaryContainer = GammaPrimaryVariant,
    secondary = GammaSecondary,
    onSecondary = GammaLightSurface,
    tertiary = GammaTertiary,
    background = GammaLightBackground,
    onBackground = GammaLightTextPrimary,
    surface = GammaLightSurface,
    onSurface = GammaLightTextPrimary,
    surfaceVariant = GammaLightSurfaceElevated,
    onSurfaceVariant = GammaLightTextSecondary,
    outline = GammaLightDivider,
    error = GammaError
)

@Composable
fun GammaTheme(
    preset: GammaThemePreset = GammaThemeManager.selectedThemePreset,
    darkTheme: Boolean = preset.isDark,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = preset.primaryColor,
            onPrimary = preset.backgroundColor,
            primaryContainer = preset.surfaceHighlightColor,
            onPrimaryContainer = preset.primaryColor,
            secondary = preset.secondaryColor,
            onSecondary = preset.textPrimaryColor,
            tertiary = preset.tertiaryColor,
            background = preset.backgroundColor,
            onBackground = preset.textPrimaryColor,
            surface = preset.surfaceColor,
            onSurface = preset.textPrimaryColor,
            surfaceVariant = preset.surfaceElevatedColor,
            onSurfaceVariant = preset.textSecondaryColor,
            outline = preset.dividerColor,
            error = GammaError
        )
    } else {
        lightColorScheme(
            primary = preset.primaryColor,
            onPrimary = preset.surfaceColor,
            primaryContainer = preset.surfaceElevatedColor,
            onPrimaryContainer = preset.primaryColor,
            secondary = preset.secondaryColor,
            onSecondary = preset.surfaceColor,
            tertiary = preset.tertiaryColor,
            background = preset.backgroundColor,
            onBackground = preset.textPrimaryColor,
            surface = preset.surfaceColor,
            onSurface = preset.textPrimaryColor,
            surfaceVariant = preset.surfaceElevatedColor,
            onSurfaceVariant = preset.textSecondaryColor,
            outline = preset.dividerColor,
            error = GammaError
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    GammaTheme(darkTheme = darkTheme, content = content)
}
