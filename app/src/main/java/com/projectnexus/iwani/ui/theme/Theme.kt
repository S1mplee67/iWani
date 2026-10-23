package com.projectnexus.iwani.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IcctDarkColorScheme = darkColorScheme(
    primary = IcctBlue,
    onPrimary = Color.White,
    primaryContainer = DarkSurfaceHigh,
    secondary = IcctGold,
    onSecondary = Color.Black,
    error = EmergencyRed,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val ClassicLightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    secondary = IcctGold,
    error = EmergencyRed,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

object IWaniColors {
    fun background(isDark: Boolean) = if (isDark) DarkBackground else Color(0xFFF8FAFC)
    fun surface(isDark: Boolean) = if (isDark) DarkSurfaceGlass else Color.White
    fun border(isDark: Boolean) = if (isDark) DarkSurfaceBorder else Color(0xFFE2E8F0)
    fun textPrimary(isDark: Boolean) = if (isDark) TextPrimary else Color(0xFF0F172A)
    fun textSecondary(isDark: Boolean) = if (isDark) TextSecondary else Color(0xFF64748B)
    fun accent(isDark: Boolean) = if (isDark) IcctGold else TealPrimary
    fun primary(isDark: Boolean) = if (isDark) IcctBlue else TealPrimary
}

@Composable
fun IWaniTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) IcctDarkColorScheme else ClassicLightColorScheme,
        content = content
    )
}
