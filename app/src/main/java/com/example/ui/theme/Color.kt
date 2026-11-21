package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// VIBEMATE Brand Palette: Purple, Electric Blue, Obsidian Black, Crisp White
val VibeObsidian = Color(0xFF07070C)
val VibeCarbon = Color(0xFF0F101A)
val VibeSurfaceElevated = Color(0xFF171828)
val VibeSurfaceBorder = Color(0xFF282A42)

val VibePurple = Color(0xFF8B5CF6)
val VibeDeepViolet = Color(0xFF6D28D9)
val VibeSoftLavender = Color(0xFFC4B5FD)

val VibeElectricBlue = Color(0xFF00D2FF)
val VibeCobalt = Color(0xFF2563EB)
val VibeIceBlue = Color(0xFFBAE6FD)

val VibeWhite = Color(0xFFFFFFFF)
val VibeOffWhite = Color(0xFFF8FAFC)
val VibeMutedSlate = Color(0xFF94A3B8)
val VibeDarkInk = Color(0xFF0B0B14)
val VibeLightCard = Color(0xFFFFFFFF)
val VibeLightSurface = Color(0xFFF3F4FA)
val VibeLightBorder = Color(0xFFE2E5F1)

val VibeRose = Color(0xFFFB3768)
val VibeEmerald = Color(0xFF10B981)
val VibeAmber = Color(0xFFF59E0B)

object VibeGradients {
    val PrismBrand = Brush.linearGradient(
        colors = listOf(VibePurple, VibeCobalt, VibeElectricBlue)
    )
    val DiagonalPurpleElectric = Brush.linearGradient(
        colors = listOf(VibeDeepViolet, VibePurple, VibeElectricBlue)
    )
    val ObsidianBackdrop = Brush.verticalGradient(
        colors = listOf(VibeObsidian, VibeCarbon, VibeObsidian)
    )
    val DarkScrim = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color(0xCC07070C), Color(0xF207070C))
    )
}
