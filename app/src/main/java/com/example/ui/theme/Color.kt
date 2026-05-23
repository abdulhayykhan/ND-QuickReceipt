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
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1D1B20),
    surfaceVariant = Color(0xFF49454F),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41)
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
