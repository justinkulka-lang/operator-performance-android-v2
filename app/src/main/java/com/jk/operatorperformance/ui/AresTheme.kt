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

internal val AresBrass = Color(AresPalette.Brass)

internal val AresDarkColors = darkColorScheme(
    primary = AresBrass,
    onPrimary = Color(AresPalette.OnBrass),
    primaryContainer = Color(AresPalette.BrassContainer),
    onPrimaryContainer = Color(AresPalette.OnBrassContainer),
    secondary = Color(AresPalette.OnSurfaceVariant),
    onSecondary = Color(AresPalette.OnBrass),
    secondaryContainer = Color(AresPalette.BrassContainer),
    onSecondaryContainer = Color(AresPalette.OnBrassContainer),
    tertiary = AresBrass,
    onTertiary = Color(AresPalette.OnBrass),
    tertiaryContainer = Color(AresPalette.BrassContainer),
    onTertiaryContainer = Color(AresPalette.OnBrassContainer),
    background = Color(AresPalette.Background),
    onBackground = Color(AresPalette.OnSurface),
    surface = Color(AresPalette.Surface),
    onSurface = Color(AresPalette.OnSurface),
    surfaceVariant = Color(AresPalette.SurfaceVariant),
    onSurfaceVariant = Color(AresPalette.OnSurfaceVariant),
    surfaceTint = Color(AresPalette.Surface),
    inverseSurface = Color(AresPalette.InverseSurface),
    inverseOnSurface = Color(AresPalette.InverseOnSurface),
    error = Color(AresPalette.Warning),
    onError = Color(AresPalette.OnWarning),
    errorContainer = Color(AresPalette.ErrorContainer),
    onErrorContainer = Color(AresPalette.OnErrorContainer),
    outline = Color(AresPalette.Outline),
    outlineVariant = Color(AresPalette.OutlineVariant),
    scrim = Color(AresPalette.Background),
    surfaceBright = Color(AresPalette.SurfaceContainerHighest),
    surfaceDim = Color(AresPalette.Background),
    surfaceContainerLowest = Color(AresPalette.SurfaceContainerLowest),
    surfaceContainerLow = Color(AresPalette.SurfaceContainerLow),
    surfaceContainer = Color(AresPalette.SurfaceContainer),
    surfaceContainerHigh = Color(AresPalette.SurfaceContainerHigh),
    surfaceContainerHighest = Color(AresPalette.SurfaceContainerHighest),
)

private val AresCondensed = FontFamily(
    androidx.compose.ui.text.font.Typeface(
        Typeface.create("sans-serif-condensed", Typeface.NORMAL),
    ),
)

internal fun aresTypography(): Typography {
    val base = Typography()
    fun TextStyle.condensed(weight: FontWeight = FontWeight.Medium) = copy(
        fontFamily = AresCondensed,
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
internal fun aresButtonColors() = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = Color(AresPalette.DisabledContainer),
    disabledContentColor = Color(AresPalette.DisabledContent),
)

internal fun phaseAccent(@Suppress("UNUSED_PARAMETER") phase: ProgramBlock): Color = AresBrass
