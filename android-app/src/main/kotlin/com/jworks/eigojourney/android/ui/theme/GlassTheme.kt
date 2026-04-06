package com.jworks.eigojourney.android.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Glass design system — translucent layers on near-black
object Glass {
    // Background
    val Background = Color(0xFF050508)
    val Surface = Color(0xFF0A0A10)
    val SurfaceOverlay = Color(0xFF0A0A10)

    // Brand accent (EigoJourney blue)
    val Accent = Color(0xFF4A90D9)
    val AccentDim = Color(0xFF4A90D9).copy(alpha = 0.28f)
    val AccentGlow = Color(0xFF4A90D9).copy(alpha = 0.12f)

    // Glass surfaces
    val CardGradient = Brush.linearGradient(
        listOf(Color.White.copy(0.10f), Color.White.copy(0.04f))
    )
    val CardGradientBright = Brush.linearGradient(
        listOf(Color.White.copy(0.14f), Color.White.copy(0.06f))
    )
    val CardBorder = Color.White.copy(0.08f)
    val CardBorderAccent = Color(0xFF4A90D9).copy(0.28f)

    // Text
    val TextPrimary = Color(0xFFE8E8ED)
    val TextSecondary = Color(0xFFA0A0B0)
    val TextDim = Color(0xFF707080)
    val TextAccent = Color(0xFF6AABF0)

    // Semantic
    val Success = Color(0xFF43A047)
    val SuccessGlow = Color(0xFF43A047).copy(0.15f)
    val Error = Color(0xFFE05555)
    val ErrorGlow = Color(0xFFE05555).copy(0.15f)
    val Gold = Color(0xFFFFD54F)
    val GoldGlow = Color(0xFFFFD54F).copy(0.12f)

    // Progress
    val ProgressTrack = Color.White.copy(0.06f)
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accentBorder: Boolean = false,
    borderColor: Color? = null,
    cornerRadius: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border = borderColor
        ?: if (accentBorder) Glass.CardBorderAccent else Glass.CardBorder

    Box(
        modifier = modifier
            .clip(shape)
            .background(Glass.CardGradient, shape)
            .border(1.dp, border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GlassCardBright(
    modifier: Modifier = Modifier,
    accentBorder: Boolean = true,
    cornerRadius: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val border = if (accentBorder) Glass.CardBorderAccent else Glass.CardBorder

    Box(
        modifier = modifier
            .clip(shape)
            .background(Glass.CardGradientBright, shape)
            .border(1.dp, border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content
    )
}
