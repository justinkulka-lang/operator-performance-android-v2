package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramV72
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class LegendVisualIdentityTest {
    @Test fun phasePurposeCopyStaysNeutral() {
        val blocks = ProgramV72.blocks
        assertEquals(listOf("Draft", "Build", "Backstop", "Live"), blocks.map { it.name })
        assertEquals(listOf("Draft", "Build", "Backstop", "Live"), blocks.map { it.godName })
        assertEquals(listOf(1 to 8, 9 to 16, 17 to 24, 25 to 32), blocks.map { it.startWeek to it.endWeek })
        assertEquals(
            listOf(
                "Foundation, movement quality, and early work capacity.",
                "Progressive strength, hypertrophy, and conditioning development.",
                "Sustained strength, fatigue control, and repeatable work capacity.",
                "Advanced conditioning, maintained hypertrophy, and practical performance expression.",
            ),
            blocks.map { it.purpose },
        )
        assertEquals("LEGEND", ProgramV72.PROGRAM_NAME)
        assertEquals("Built to hold.", ProgramV72.PROGRAM_TAGLINE)
        blocks.forEach { block ->
            val lower = "${block.name} ${block.purpose}".lowercase()
            assertFalse(lower.contains("final test"))
            assertFalse(lower.contains("goal-pace"))
            assertFalse(lower.contains("taper"))
            assertFalse(lower.contains("event"))
        }
    }

    @Test fun fieldLabelsLeadWithThePlainPhaseName() {
        val labels = ProgramV72.blocks.map(::phaseFieldLabel)
        assertEquals(
            listOf(
                "Phase 1 · Draft",
                "Phase 2 · Build",
                "Phase 3 · Backstop",
                "Phase 4 · Live",
            ),
            labels,
        )
    }

    @Test fun paletteMeetsWcagAa() {
        val textPairs = listOf(
            LegendPalette.OnSurface to LegendPalette.Background,
            LegendPalette.OnSurface to LegendPalette.Surface,
            LegendPalette.OnSurface to LegendPalette.SurfaceContainerHighest,
            LegendPalette.OnSurfaceVariant to LegendPalette.Surface,
            LegendPalette.OnSurfaceVariant to LegendPalette.SurfaceVariant,
            LegendPalette.OnSurface to LegendPalette.SurfaceContainerHigh,
            LegendPalette.OnSurfaceVariant to LegendPalette.SurfaceContainerHigh,
            LegendPalette.Brass to LegendPalette.SurfaceContainerHigh,
            LegendPalette.Brass to LegendPalette.Background,
            LegendPalette.Brass to LegendPalette.Surface,
            LegendPalette.OnBrass to LegendPalette.Brass,
            LegendPalette.OnBrassContainer to LegendPalette.BrassContainer,
            LegendPalette.Warning to LegendPalette.Background,
            LegendPalette.Warning to LegendPalette.Surface,
            LegendPalette.OnWarning to LegendPalette.Warning,
            LegendPalette.OnErrorContainer to LegendPalette.ErrorContainer,
            LegendPalette.DisabledContent to LegendPalette.DisabledContainer,
            LegendPalette.InverseOnSurface to LegendPalette.InverseSurface,
        )
        textPairs.forEach { (foreground, background) ->
            assertTrue(
                "text ${foreground.toHex()} on ${background.toHex()} is ${contrast(foreground, background)}",
                contrast(foreground, background) >= 4.5,
            )
        }
        val controlPairs = listOf(
            LegendPalette.Outline to LegendPalette.Surface,
            LegendPalette.Outline to LegendPalette.Background,
            LegendPalette.OutlineVariant to LegendPalette.Surface,
            LegendPalette.Brass to LegendPalette.SurfaceVariant,
        )
        controlPairs.forEach { (foreground, background) ->
            assertTrue(
                "control ${foreground.toHex()} on ${background.toHex()} is ${contrast(foreground, background)}",
                contrast(foreground, background) >= 3.0,
            )
        }
    }
}

private fun Long.toHex(): String = "0x${toString(16)}"

private fun contrast(foreground: Long, background: Long): Double {
    val lighter = maxOf(luminance(foreground), luminance(background))
    val darker = minOf(luminance(foreground), luminance(background))
    return (lighter + 0.05) / (darker + 0.05)
}

private fun luminance(color: Long): Double {
    fun channel(shift: Int): Double {
        val value = ((color shr shift) and 0xFF).toInt() / 255.0
        return if (value <= 0.04045) value / 12.92 else ((value + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
}
