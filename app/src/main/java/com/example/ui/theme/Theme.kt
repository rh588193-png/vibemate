package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val VibeDarkColorScheme = darkColorScheme(
    primary = VibePurple,
    onPrimary = VibeWhite,
    primaryContainer = VibeDeepViolet,
    onPrimaryContainer = VibeSoftLavender,
    secondary = VibeElectricBlue,
    onSecondary = VibeDarkInk,
    secondaryContainer = VibeCobalt,
    onSecondaryContainer = VibeIceBlue,
    tertiary = VibeElectricBlue,
    onTertiary = VibeDarkInk,
    background = VibeObsidian,
    onBackground = VibeWhite,
    surface = VibeCarbon,
    onSurface = VibeWhite,
    surfaceVariant = VibeSurfaceElevated,
    onSurfaceVariant = VibeMutedSlate,
    outline = VibeSurfaceBorder,
    error = VibeRose,
    onError = VibeWhite
)

private val ColorTokensLightVariant = androidx.compose.ui.graphics.Color(0xFFEBEFF9)

private val VibeLightColorScheme = lightColorScheme(
    primary = VibeDeepViolet,
    onPrimary = VibeWhite,
    primaryContainer = VibeSoftLavender,
    onPrimaryContainer = VibeDarkInk,
    secondary = VibeCobalt,
    onSecondary = VibeWhite,
    secondaryContainer = VibeIceBlue,
    onSecondaryContainer = VibeDarkInk,
    tertiary = VibeElectricBlue,
    onTertiary = VibeDarkInk,
    background = VibeLightSurface,
    onBackground = VibeDarkInk,
    surface = VibeLightCard,
    onSurface = VibeDarkInk,
    surfaceVariant = ColorTokensLightVariant,
    onSurfaceVariant = VibeMutedSlate,
    outline = VibeLightBorder,
    error = VibeRose,
    onError = VibeWhite
)

val VibeShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun VIBEMATETheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) VibeDarkColorScheme else VibeLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = VibeShapes,
        content = content
    )
}
