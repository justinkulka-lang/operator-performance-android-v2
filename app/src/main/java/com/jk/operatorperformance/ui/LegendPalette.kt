package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramV72
import com.jk.operatorperformance.program.ProgramBlock

internal fun phaseFieldLabel(phase: ProgramBlock): String {
    val index = ProgramV72.blocks.indexOfFirst { it.startWeek == phase.startWeek }.let { if (it < 0) 1 else it + 1 }
    return "Phase $index · ${phase.name}"
}

internal object LegendPalette {
    const val Background = 0xFF15110DL
    const val Surface = 0xFF211A14L
    const val SurfaceVariant = 0xFF32281FL
    const val SurfaceContainerLowest = 0xFF100C0AL
    const val SurfaceContainerLow = 0xFF1A1510L
    const val SurfaceContainer = 0xFF211A14L
    const val SurfaceContainerHigh = 0xFF2C241CL
    const val SurfaceContainerHighest = 0xFF34281FL
    const val OnSurface = 0xFFE8DFCEL
    const val OnSurfaceVariant = 0xFFB5A892L
    const val Brass = 0xFFB08D57L
    const val OnBrass = 0xFF15110DL
    const val BrassContainer = 0xFF2C241CL
    const val OnBrassContainer = 0xFFE8DFCEL
    /** Current-day band and current phase-marker fill only. */
    const val Oxblood = 0xFF6E2424L
    /** Non-text marks only. */
    const val Olive = 0xFF6F7250L
    const val Warning = 0xFFE7B2A8L
    const val OnWarning = 0xFF2A1210L
    const val ErrorContainer = 0xFF3A2422L
    const val OnErrorContainer = 0xFFF0D4CEL
    const val DisabledContainer = 0xFF3F382FL
    const val DisabledContent = 0xFFE8DFCEL
    const val Outline = 0xFFB08D57L
    const val OutlineVariant = 0xFFB08D57L
    const val InverseSurface = 0xFFE8DFCEL
    const val InverseOnSurface = 0xFF15110DL
}
