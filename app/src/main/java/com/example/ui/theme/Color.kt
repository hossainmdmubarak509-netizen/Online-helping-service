package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette
val PrimaryNavy = Color(0xFF1E3A8A)
val PrimaryNavyLight = Color(0xFF3B82F6)
val PrimaryDarkNavy = Color(0xFF0F172A)

val SecondaryTeal = Color(0xFF0D9488)
val SecondaryTealLight = Color(0xFF14B8A6)

val AccentGold = Color(0xFFF59E0B)
val AccentPurple = Color(0xFF7C3AED)

// Status colors
val StatusPendingBg = Color(0xFFFEF3C7)
val StatusPendingText = Color(0xFFB45309)
val StatusApprovedBg = Color(0xFFD1FAE5)
val StatusApprovedText = Color(0xFF065F46)
val StatusProcessingBg = Color(0xFFDBEAFE)
val StatusProcessingText = Color(0xFF1E40AF)
val StatusRejectedBg = Color(0xFFFEE2E2)
val StatusRejectedText = Color(0xFF991B1B)

// Surfaces
val SurfaceLight = Color(0xFFF8FAFC)
val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceDarkCard = Color(0xFF1E293B)
val SurfaceDarkBg = Color(0xFF0F172A)

// Telecom Operator brand hints
val ColorGP = Color(0xFF0072CE)
val ColorBanglalink = Color(0xFFE84E0F)
val ColorRobi = Color(0xFFED1C24)
val ColorAirtel = Color(0xFFD81E05)
val ColorTeletalk = Color(0xFF008A3B)
val ColorSkitto = Color(0xFFFF5200)

val LightColors = androidx.compose.material3.lightColorScheme(
    primary = PrimaryNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = AccentPurple,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Color(0xFF0F172A),
    surface = SurfaceCard,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = StatusRejectedText,
    errorContainer = StatusRejectedBg,
    onError = Color.White
)

val DarkColors = androidx.compose.material3.darkColorScheme(
    primary = PrimaryNavyLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = SecondaryTealLight,
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = Color(0xFFA78BFA),
    onTertiary = Color(0xFF2E1065),
    background = SurfaceDarkBg,
    onBackground = Color(0xFFF8FAFC),
    surface = SurfaceDarkCard,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D),
    onError = Color.White
)
