package it.jonica.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Olive = Color(0xFF3E5A2C)
val OliveLight = Color(0xFF7FA65A)
val Terracotta = Color(0xFFC8643B)
val Mustard = Color(0xFFE3A93B)
val Cream = Color(0xFFFBF6EC)
val Ink = Color(0xFF23241C)
val Sky = Color(0xFF3E6B8C)

private val Light = lightColorScheme(
    primary = Olive,
    onPrimary = Cream,
    primaryContainer = Color(0xFFDDE8CC),
    onPrimaryContainer = Color(0xFF1C2D10),
    secondary = Terracotta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF8DCCD),
    onSecondaryContainer = Color(0xFF4A1F0C),
    tertiary = Mustard,
    tertiaryContainer = Color(0xFFFBE8BF),
    onTertiaryContainer = Color(0xFF3F2C00),
    background = Cream,
    onBackground = Ink,
    surface = Color(0xFFFFFCF5),
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1EBDD),
    onSurfaceVariant = Color(0xFF5B5A4C),
    surfaceContainer = Color(0xFFF5EFE2),
    surfaceContainerHigh = Color(0xFFEFE8D8),
    outline = Color(0xFFD8D0BC),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFA9CC85),
    onPrimary = Color(0xFF1C2D10),
    primaryContainer = Color(0xFF2E4420),
    onPrimaryContainer = Color(0xFFDDE8CC),
    secondary = Color(0xFFF0A07C),
    onSecondary = Color(0xFF4A1F0C),
    secondaryContainer = Color(0xFF6A3018),
    onSecondaryContainer = Color(0xFFF8DCCD),
    tertiary = Color(0xFFF2C46A),
    tertiaryContainer = Color(0xFF5A4206),
    onTertiaryContainer = Color(0xFFFBE8BF),
    background = Color(0xFF15170F),
    onBackground = Color(0xFFEDE8DA),
    surface = Color(0xFF1C1E15),
    onSurface = Color(0xFFEDE8DA),
    surfaceVariant = Color(0xFF2A2C22),
    onSurfaceVariant = Color(0xFFC6C2B0),
    surfaceContainer = Color(0xFF22241A),
    surfaceContainerHigh = Color(0xFF2B2D22),
    outline = Color(0xFF45473A),
)

private val Display = FontFamily.Serif

private val AppTypography = Typography().let { t ->
    t.copy(
        displaySmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 40.sp),
        headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
        headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
        headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
        titleLarge = t.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

@Composable
fun JonicaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = AppTypography,
        content = content,
    )
}
