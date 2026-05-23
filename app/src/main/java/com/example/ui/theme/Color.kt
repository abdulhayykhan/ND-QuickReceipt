package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val BgColor = Color(0xFFFEF7FF)
val OnBgColor = Color(0xFF1D1B20)
val PrimaryColor = Color(0xFF6750A4)
val SecondaryTextColor = Color(0xFF49454F)
val BorderColor = Color(0xFFCAC4D0)
val SurfaceColor = Color(0xFFF3EDF7)
val SuccessBgColor = Color(0xFFD1E8CF)
val SuccessTextColor = Color(0xFF0A2F07)

val OutlineColor = Color(0xFF79747E)
val WhiteBg = Color(0xFFFFFFFF)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    background = BgColor,
    onBackground = OnBgColor,
    surface = SurfaceColor,
    onSurface = OnBgColor,
    outline = OutlineColor,
    secondary = SecondaryTextColor
)

val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    background = BgColor,
    onBackground = OnBgColor,
    surface = SurfaceColor,
    surfaceVariant = SurfaceColor,
    onSurface = OnBgColor,
    outline = OutlineColor,
    outlineVariant = BorderColor,
    secondary = SecondaryTextColor,
    onPrimary = WhiteBg
)
