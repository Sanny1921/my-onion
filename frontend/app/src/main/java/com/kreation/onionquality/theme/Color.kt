package com.kreation.onionquality.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val PrimaryBackground = Color(0xFFFFF7FC)
val SoftPink = Color(0xFFF9D8EE)
val PrimaryMagenta = Color(0xFFD92C8B)
val DeepMagenta = Color(0xFF8E1E63)
val DeepPlum = Color(0xFF5A1242)
val DarkPlum = Color(0xFF1E0D18)
val White = Color(0xFFFFFFFF)
val SecondaryText = Color(0xFF6F5A67)
val BorderColor = Color(0xFFE8D5E1)

// Semantic colors
val SemanticGood = Color(0xFF4CAF50)
val SemanticUrs = Color(0xFFF4C430)
val SemanticSprouted = Color(0xFF9C4DCC)
val SemanticDamaged = Color(0xFFF28C28)
val SemanticRotten = Color(0xFFE53935)

val LightColorScheme = lightColorScheme(
    primary = PrimaryMagenta,
    onPrimary = White,
    primaryContainer = SoftPink,
    onPrimaryContainer = DeepPlum,
    secondary = DeepMagenta,
    onSecondary = White,
    background = PrimaryBackground,
    onBackground = DarkPlum,
    surface = White,
    onSurface = DarkPlum,
    surfaceVariant = SoftPink,
    onSurfaceVariant = SecondaryText,
    outline = BorderColor
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryMagenta,
    onPrimary = White,
    primaryContainer = DeepPlum,
    onPrimaryContainer = SoftPink,
    secondary = SoftPink,
    onSecondary = DarkPlum,
    background = DarkPlum,
    onBackground = White,
    surface = Color(0xFF2C1E26), // slightly lighter than dark plum
    onSurface = White,
    surfaceVariant = DeepPlum,
    onSurfaceVariant = SecondaryText,
    outline = BorderColor
)
