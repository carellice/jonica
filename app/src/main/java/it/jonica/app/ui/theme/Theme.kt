package it.jonica.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import it.jonica.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Olive = Color(0xFF3E5A2C)
val OliveLight = Color(0xFF7FA65A)
val OliveDeep = Color(0xFF5C8240)
val TerracottaDeep = Color(0xFFB0532F)
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

val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

private val AppTypography = Typography().let { t ->
    fun TextStyle.m() = copy(fontFamily = Manrope)
    Typography(
        displayLarge = t.displayLarge.m(),
        displayMedium = t.displayMedium.m(),
        displaySmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.8).sp),
        headlineLarge = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.6).sp),
        headlineMedium = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.5).sp),
        headlineSmall = TextStyle(fontFamily = Manrope, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.3).sp),
        titleLarge = t.titleLarge.m().copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
        titleMedium = t.titleMedium.m().copy(fontWeight = FontWeight.Bold),
        titleSmall = t.titleSmall.m().copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = t.bodyLarge.m(),
        bodyMedium = t.bodyMedium.m(),
        bodySmall = t.bodySmall.m(),
        labelLarge = t.labelLarge.m().copy(fontWeight = FontWeight.SemiBold),
        labelMedium = t.labelMedium.m().copy(fontWeight = FontWeight.SemiBold),
        labelSmall = t.labelSmall.m().copy(fontWeight = FontWeight.SemiBold),
    )
}

/** Colori di categoria leggibili anche su sfondo scuro: in dark mode vengono schiariti. */
@Composable
fun Color.readable(): Color =
    if (isSystemInDarkTheme()) androidx.compose.ui.graphics.lerp(this, Color.White, 0.45f) else this

@Composable
fun JonicaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        typography = AppTypography,
    ) {
        // senza Surface il colore del testo di default sarebbe nero: in tema scuro diventerebbe illeggibile
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onBackground,
        ) {
            androidx.compose.material3.ProvideTextStyle(MaterialTheme.typography.bodyMedium, content)
        }
    }
}
