package com.jk.operatorperformance.ui

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jk.operatorperformance.R
import com.jk.operatorperformance.program.ProgramBlock

internal val LegendAccent = Color(LegendPalette.Brass)

internal val LegendColors = darkColorScheme(
    primary = LegendAccent,
    onPrimary = Color(LegendPalette.OnBrass),
    primaryContainer = Color(LegendPalette.BrassContainer),
    onPrimaryContainer = Color(LegendPalette.OnBrassContainer),
    secondary = Color(LegendPalette.OnSurfaceVariant),
    onSecondary = Color(LegendPalette.OnBrass),
    secondaryContainer = Color(LegendPalette.BrassContainer),
    onSecondaryContainer = Color(LegendPalette.OnBrassContainer),
    tertiary = LegendAccent,
    onTertiary = Color(LegendPalette.OnBrass),
    tertiaryContainer = Color(LegendPalette.BrassContainer),
    onTertiaryContainer = Color(LegendPalette.OnBrassContainer),
    background = Color(LegendPalette.Background),
    onBackground = Color(LegendPalette.OnSurface),
    surface = Color(LegendPalette.Surface),
    onSurface = Color(LegendPalette.OnSurface),
    surfaceVariant = Color(LegendPalette.SurfaceVariant),
    onSurfaceVariant = Color(LegendPalette.OnSurfaceVariant),
    surfaceTint = Color(LegendPalette.Surface),
    inverseSurface = Color(LegendPalette.InverseSurface),
    inverseOnSurface = Color(LegendPalette.InverseOnSurface),
    error = Color(LegendPalette.Warning),
    onError = Color(LegendPalette.OnWarning),
    errorContainer = Color(LegendPalette.ErrorContainer),
    onErrorContainer = Color(LegendPalette.OnErrorContainer),
    outline = Color(LegendPalette.Outline),
    outlineVariant = Color(LegendPalette.OutlineVariant),
    scrim = Color(LegendPalette.Background),
    surfaceBright = Color(LegendPalette.SurfaceContainerHighest),
    surfaceDim = Color(LegendPalette.Background),
    surfaceContainerLowest = Color(LegendPalette.SurfaceContainerLowest),
    surfaceContainerLow = Color(LegendPalette.SurfaceContainerLow),
    surfaceContainer = Color(LegendPalette.SurfaceContainer),
    surfaceContainerHigh = Color(LegendPalette.SurfaceContainerHigh),
    surfaceContainerHighest = Color(LegendPalette.SurfaceContainerHighest),
)

@OptIn(ExperimentalTextApi::class)
private fun garamond(weight: FontWeight, style: FontStyle = FontStyle.Normal) = Font(
    resId = if (style == FontStyle.Italic) R.font.eb_garamond_italic else R.font.eb_garamond,
    weight = weight,
    style = style,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/** EB Garamond, bundled offline. Titles, day names, and small-cap labels. */
internal val LegendSerif = FontFamily(
    garamond(FontWeight.Normal),
    garamond(FontWeight.Medium),
    garamond(FontWeight.SemiBold),
    garamond(FontWeight.Bold),
    garamond(FontWeight.Normal, FontStyle.Italic),
    garamond(FontWeight.Medium, FontStyle.Italic),
    garamond(FontWeight.SemiBold, FontStyle.Italic),
    garamond(FontWeight.Bold, FontStyle.Italic),
)

internal const val LegendSmallCaps = "smcp, c2sc"

internal fun legendTypography(): Typography {
    val base = Typography()
    fun TextStyle.serif(weight: FontWeight = FontWeight.Normal) = copy(
        fontFamily = LegendSerif,
        fontWeight = weight,
        letterSpacing = 0.sp,
        fontFeatureSettings = null,
    )
    fun TextStyle.smallCaps() = copy(
        fontFamily = LegendSerif,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.4.sp,
        fontFeatureSettings = LegendSmallCaps,
    )
    return base.copy(
        displayLarge = base.displayLarge.serif(),
        displayMedium = base.displayMedium.serif(),
        displaySmall = base.displaySmall.serif(),
        headlineLarge = base.headlineLarge.serif(),
        headlineMedium = base.headlineMedium.serif(),
        headlineSmall = base.headlineSmall.serif(),
        titleLarge = base.titleLarge.serif(),
        titleMedium = base.titleMedium.serif(),
        titleSmall = base.titleSmall.serif(),
        labelLarge = base.labelLarge.smallCaps(),
        labelMedium = base.labelMedium.smallCaps(),
        labelSmall = base.labelSmall.smallCaps(),
    )
}

@Composable
internal fun technicalStyle(style: TextStyle = MaterialTheme.typography.bodyLarge): TextStyle =
    style.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
        fontFeatureSettings = null,
    )

@Composable
internal fun legendButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = Color(LegendPalette.DisabledContainer),
    disabledContentColor = Color(LegendPalette.DisabledContent),
)

internal fun phaseAccent(@Suppress("UNUSED_PARAMETER") phase: ProgramBlock): Color = LegendAccent
