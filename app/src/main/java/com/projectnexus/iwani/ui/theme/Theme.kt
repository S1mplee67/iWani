package com.projectnexus.iwani.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class AppFontStyle(val labelEn: String, val labelTl: String, val fontFamily: FontFamily) {
    GEOMETRIC(
        "Geometric Condensed",
        "Geometric Condensed",
        FontFamily(
            Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Normal),
            Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Medium),
            Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold),
            Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Black)
        )
    ),
    EDITORIAL(
        "Editorial Serif",
        "Editorial Serif",
        FontFamily.Serif
    ),
    CYBER_MONO(
        "Cyber Monospace",
        "Cyber Monospace",
        FontFamily.Monospace
    ),
    HUMANIST(
        "Rounded Casual",
        "Rounded Casual",
        FontFamily(
            Font(DeviceFontFamilyName("casual"), FontWeight.Normal),
            Font(DeviceFontFamilyName("casual"), FontWeight.Bold)
        )
    )
}

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

fun buildAppTypography(fontStyle: AppFontStyle, scale: Float = 1.0f): Typography {
    val ff = fontStyle.fontFamily
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = ff, fontSize = (57 * scale).sp, letterSpacing = 0.5.sp),
        displayMedium = base.displayMedium.copy(fontFamily = ff, fontSize = (45 * scale).sp, letterSpacing = 0.5.sp),
        displaySmall = base.displaySmall.copy(fontFamily = ff, fontSize = (36 * scale).sp, letterSpacing = 0.4.sp),
        headlineLarge = base.headlineLarge.copy(fontFamily = ff, fontSize = (32 * scale).sp, letterSpacing = 0.4.sp),
        headlineMedium = base.headlineMedium.copy(fontFamily = ff, fontSize = (28 * scale).sp, letterSpacing = 0.35.sp),
        headlineSmall = base.headlineSmall.copy(fontFamily = ff, fontSize = (24 * scale).sp, letterSpacing = 0.3.sp),
        titleLarge = base.titleLarge.copy(fontFamily = ff, fontSize = (22 * scale).sp, letterSpacing = 0.3.sp),
        titleMedium = base.titleMedium.copy(fontFamily = ff, fontSize = (16 * scale).sp, letterSpacing = 0.25.sp),
        titleSmall = base.titleSmall.copy(fontFamily = ff, fontSize = (14 * scale).sp, letterSpacing = 0.2.sp),
        bodyLarge = base.bodyLarge.copy(fontFamily = ff, fontSize = (16 * scale).sp, letterSpacing = 0.25.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = ff, fontSize = (14 * scale).sp, letterSpacing = 0.2.sp),
        bodySmall = base.bodySmall.copy(fontFamily = ff, fontSize = (12 * scale).sp, letterSpacing = 0.2.sp),
        labelLarge = base.labelLarge.copy(fontFamily = ff, fontSize = (14 * scale).sp, letterSpacing = 0.3.sp),
        labelMedium = base.labelMedium.copy(fontFamily = ff, fontSize = (12 * scale).sp, letterSpacing = 0.3.sp),
        labelSmall = base.labelSmall.copy(fontFamily = ff, fontSize = (11 * scale).sp, letterSpacing = 0.3.sp)
    )
}

@Composable
fun IWaniTheme(
    isDarkTheme: Boolean = true,
    fontStyle: AppFontStyle = AppFontStyle.GEOMETRIC,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) IcctDarkColorScheme else ClassicLightColorScheme,
        typography = buildAppTypography(fontStyle, fontScale),
        content = content
    )
}
