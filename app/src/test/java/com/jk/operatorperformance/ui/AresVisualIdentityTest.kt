package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.AresProgramV72
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class AresVisualIdentityTest {
    @Test fun phasePurposeCopyStaysNeutral() {
        val ares = AresProgramV72.blocks.first { it.godName == "ARES" }
        val zeus = AresProgramV72.blocks.first { it.godName == "ZEUS" }
        assertEquals("SPECIFIC DEVELOPMENT", ares.name)
        assertEquals(17, ares.startWeek)
        assertEquals(24, ares.endWeek)
        assertEquals(
            "Special endurance, 1.5-mile pace development, and aerobic capacity while retaining hypertrophy volume.",
            ares.purpose,
        )
        assertEquals("INTEGRATION / PEAK", zeus.name)
        assertEquals(25, zeus.startWeek)
        assertEquals(32, zeus.endWeek)
        assertEquals(
            "Late-phase consolidation, fatigue management, and performance expression.",
            zeus.purpose,
        )
        listOf(ares.purpose, zeus.purpose).forEach { purpose ->
            val lower = purpose.lowercase()
            assertFalse(lower.contains("final test"))
            assertFalse(lower.contains("goal-pace"))
            assertFalse(lower.contains("taper"))
            assertFalse(lower.contains("event"))
        }
    }

    @Test fun fieldLabelsLeadWithThePlainPhaseName() {
        val labels = AresProgramV72.blocks.map(::phaseFieldLabel)
        assertEquals(
            listOf(
                "Phase 1 · Foundation",
                "Phase 2 · Development",
                "Phase 3 · Specific development",
                "Phase 4 · Integration",
            ),
            labels,
        )
    }

    @Test fun paletteMeetsWcagAa() {
        val textPairs = listOf(
            AresPalette.OnSurface to AresPalette.Background,
            AresPalette.OnSurface to AresPalette.Surface,
            AresPalette.OnSurface to AresPalette.SurfaceContainerHighest,
            AresPalette.OnSurfaceVariant to AresPalette.Surface,
            AresPalette.OnSurfaceVariant to AresPalette.SurfaceVariant,
            AresPalette.Brass to AresPalette.Background,
            AresPalette.Brass to AresPalette.Surface,
            AresPalette.OnBrass to AresPalette.Brass,
            AresPalette.OnBrassContainer to AresPalette.BrassContainer,
            AresPalette.Warning to AresPalette.Background,
            AresPalette.Warning to AresPalette.Surface,
            AresPalette.OnWarning to AresPalette.Warning,
            AresPalette.OnErrorContainer to AresPalette.ErrorContainer,
            AresPalette.DisabledContent to AresPalette.DisabledContainer,
            AresPalette.InverseOnSurface to AresPalette.InverseSurface,
        )
        textPairs.forEach { (foreground, background) ->
            assertTrue(
                "text ${foreground.toHex()} on ${background.toHex()} is ${contrast(foreground, background)}",
                contrast(foreground, background) >= 4.5,
            )
        }
        val controlPairs = listOf(
            AresPalette.Outline to AresPalette.Surface,
            AresPalette.Outline to AresPalette.Background,
            AresPalette.OutlineVariant to AresPalette.Surface,
            AresPalette.Brass to AresPalette.SurfaceVariant,
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
