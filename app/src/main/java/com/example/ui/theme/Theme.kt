package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AnqeznyLightColorScheme = lightColorScheme(
    primary = AnqeznyCrimson,
    onPrimary = Color.White,
    primaryContainer = AnqeznyCrimsonContainer,
    onPrimaryContainer = AnqeznyCrimsonDark,
    secondary = AnqeznyCharcoal,
    onSecondary = Color.White,
    secondaryContainer = AnqeznySurfaceVariant,
    onSecondaryContainer = AnqeznyInk,
    tertiary = AnqeznyAmber,
    onTertiary = Color.White,
    tertiaryContainer = AnqeznyAmberContainer,
    onTertiaryContainer = AnqeznyInk,
    background = AnqeznyBackground,
    onBackground = AnqeznyInk,
    surface = AnqeznySurface,
    onSurface = AnqeznyInk,
    surfaceVariant = AnqeznySurfaceVariant,
    onSurfaceVariant = AnqeznyInkSecondary,
    outline = AnqeznyOutline,
    error = AnqeznyCrimson,
    onError = Color.White
)

private val AnqeznyShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun AnqeznyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AnqeznyLightColorScheme,
        typography = AnqeznyTypography,
        shapes = AnqeznyShapes,
        content = content
    )
}
