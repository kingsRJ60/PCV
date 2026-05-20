package com.pcv.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NavyDeep      = Color(0xFF080D1A)
val NavySurface   = Color(0xFF101729)
val NavyCard      = Color(0xFF182035)
val NavyBorder    = Color(0xFF263050)
val CyanPrimary   = Color(0xFF00D4FF)
val CyanContainer = Color(0xFF003B47)
val CyanOnContainer = Color(0xFF80EEFF)
val AmberAccent   = Color(0xFFFFB300)
val RedAlert      = Color(0xFFFF4F4F)
val GreenActive   = Color(0xFF00E676)
val TextPrimary   = Color(0xFFE8EEF8)
val TextSecondary = Color(0xFF8899BB)

private val PCVColorScheme = darkColorScheme(
    primary             = CyanPrimary,
    onPrimary           = Color(0xFF002B35),
    primaryContainer    = CyanContainer,
    onPrimaryContainer  = CyanOnContainer,
    secondary           = AmberAccent,
    onSecondary         = Color(0xFF2A1E00),
    secondaryContainer  = Color(0xFF3A2E00),
    onSecondaryContainer = Color(0xFFFFDF80),
    tertiary            = GreenActive,
    onTertiary          = Color(0xFF00200F),
    error               = RedAlert,
    onError             = Color(0xFF3B0000),
    background          = NavyDeep,
    onBackground        = TextPrimary,
    surface             = NavySurface,
    onSurface           = TextPrimary,
    surfaceVariant      = NavyCard,
    onSurfaceVariant    = TextSecondary,
    outline             = NavyBorder,
    outlineVariant      = Color(0xFF1C2840)
)

@Composable
fun PCVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PCVColorScheme,
        content     = content
    )
}
