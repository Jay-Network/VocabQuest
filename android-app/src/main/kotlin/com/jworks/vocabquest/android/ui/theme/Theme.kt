package com.jworks.vocabquest.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// EigoQuest palette: scholarly blue + energetic green
val Blue = Color(0xFF4A90D9)
val BlueDark = Color(0xFF3A7BD5)
val Green = Color(0xFF43A047)
val GreenDark = Color(0xFF2E7D32)
val Gold = Color(0xFFFFD54F)
val GoldDark = Color(0xFFFFC107)
val Cream = Color(0xFFF5F5F0)
val DarkBg = Color(0xFF1C1C2E)

private val LightColors = lightColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    secondary = Green,
    onSecondary = Color.White,
    tertiary = Gold,
    onTertiary = Color.Black,
    background = Cream,
    onBackground = Color(0xFF1C1B1F),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColors = darkColorScheme(
    primary = BlueDark,
    onPrimary = Color.White,
    secondary = GreenDark,
    onSecondary = Color.White,
    tertiary = GoldDark,
    onTertiary = Color.Black,
    background = DarkBg,
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
)

@Composable
fun EigoQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
