package com.jworks.eigojourney.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// EigoJourney glass palette — translucent layers on near-black
private val GlassColorScheme = darkColorScheme(
    primary = Color(0xFF4A90D9),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A90D9).copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFFE8E8ED),

    secondary = Color(0xFF43A047),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF43A047).copy(alpha = 0.12f),
    onSecondaryContainer = Color(0xFFE8E8ED),

    tertiary = Color(0xFFFFD54F),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFFD54F).copy(alpha = 0.10f),
    onTertiaryContainer = Color(0xFFE8E8ED),

    error = Color(0xFFE05555),
    onError = Color.White,
    errorContainer = Color(0xFFE05555).copy(alpha = 0.12f),
    onErrorContainer = Color(0xFFE8E8ED),

    background = Color(0xFF050508),
    onBackground = Color(0xFFE8E8ED),

    surface = Color(0xFF0A0A10),
    onSurface = Color(0xFFE8E8ED),
    surfaceVariant = Color.White.copy(alpha = 0.06f),
    onSurfaceVariant = Color(0xFFA0A0B0),

    outline = Color.White.copy(alpha = 0.10f),
    outlineVariant = Color(0xFF4A90D9).copy(alpha = 0.28f),

    inverseSurface = Color(0xFFE8E8ED),
    inverseOnSurface = Color(0xFF050508),
    inversePrimary = Color(0xFF3A7BD5),

    surfaceTint = Color(0xFF4A90D9),
)

@Composable
fun EigoJourneyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GlassColorScheme,
        content = content
    )
}
