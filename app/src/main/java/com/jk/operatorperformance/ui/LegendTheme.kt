package com.jk.operatorperformance.ui

import android.graphics.Typeface
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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

private val LegendCondensed = FontFamily(
    androidx.compose.ui.text.font.Typeface(
        Typeface.create("sans-serif-condensed", Typeface.NORMAL),
    ),
)

internal fun legendTypography(): Typography {
    val base = Typography()
    fun TextStyle.condensed(weight: FontWeight = FontWeight.Medium) = copy(
        fontFamily = LegendCondensed,
        fontWeight = weight,
        letterSpacing = 0.2.sp,
    )
    return base.copy(
        displayLarge = base.displayLarge.condensed(FontWeight.Medium),
        displayMedium = base.displayMedium.condensed(FontWeight.Medium),
        displaySmall = base.displaySmall.condensed(FontWeight.Medium),
        headlineLarge = base.headlineLarge.condensed(FontWeight.Medium),
        headlineMedium = base.headlineMedium.condensed(FontWeight.Medium),
        headlineSmall = base.headlineSmall.condensed(FontWeight.Medium),
        titleLarge = base.titleLarge.condensed(FontWeight.Medium),
        titleMedium = base.titleMedium.condensed(FontWeight.Medium),
        titleSmall = base.titleSmall.condensed(FontWeight.Medium),
        labelLarge = base.labelLarge.condensed(FontWeight.Medium),
        labelMedium = base.labelMedium.condensed(FontWeight.Medium),
        labelSmall = base.labelSmall.condensed(FontWeight.Medium),
    )
}

@Composable
internal fun technicalStyle(style: TextStyle = MaterialTheme.typography.bodyLarge): TextStyle =
    style.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)

@Composable
internal fun legendButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = Color(LegendPalette.DisabledContainer),
    disabledContentColor = Color(LegendPalette.DisabledContent),
)

internal fun phaseAccent(@Suppress("UNUSED_PARAMETER") phase: ProgramBlock): Color = LegendAccent
