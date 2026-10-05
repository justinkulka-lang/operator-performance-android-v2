package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramV72
import com.jk.operatorperformance.program.ProgramBlock

internal fun phaseFieldLabel(phase: ProgramBlock): String {
    val index = ProgramV72.blocks.indexOfFirst { it.startWeek == phase.startWeek }.let { if (it < 0) 1 else it + 1 }
    return "Phase $index · ${phase.name}"
}

internal object LegendPalette {
    const val Background = 0xFF12110EL
    const val Surface = 0xFF1C1A17L
    const val SurfaceVariant = 0xFF2A2723L
    const val SurfaceContainerLowest = 0xFF0E0D0BL
    const val SurfaceContainerLow = 0xFF161410L
    const val SurfaceContainer = 0xFF1C1A17L
    const val SurfaceContainerHigh = 0xFF24211DL
    const val SurfaceContainerHighest = 0xFF2E2A26L
    const val OnSurface = 0xFFE8E2D6L
    const val OnSurfaceVariant = 0xFFC2BAA8L
    const val Brass = 0xFFC4A574L
    const val OnBrass = 0xFF1A140CL
    const val BrassContainer = 0xFF3A3228L
    const val OnBrassContainer = 0xFFE8DCC8L
    const val Warning = 0xFFE7B2A8L
    const val OnWarning = 0xFF2A1210L
    const val ErrorContainer = 0xFF3A2422L
    const val OnErrorContainer = 0xFFF0D4CEL
    const val DisabledContainer = 0xFF3F382FL
    const val DisabledContent = 0xFFE8E2D6L
    const val Outline = 0xFFA89880L
    const val OutlineVariant = 0xFF8C7E6AL
    const val InverseSurface = 0xFFE8E2D6L
    const val InverseOnSurface = 0xFF1A140CL
}
