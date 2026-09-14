package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessProgramTest {
    @Test
    fun allWeeksAreCanonicalAndNoWeekBeyondThirtyTwoExists() {
        val lines = ProgramExport.programWeeksCsv().lineSequence().filter { it.isNotBlank() }.toList()
        assertEquals(33, lines.size)
        assertTrue(lines[1].contains("HEPHAESTUS"))
        assertTrue(lines[9].contains("HERMES"))
        assertTrue(lines[17].contains("ARES"))
        assertTrue(lines[25].contains("ZEUS"))
        assertTrue(lines[32].contains("POPULATED_REALIZATION_TAPER"))
        assertFalse(OperatorProgramV56.weeks.any { it.week > 32 })
    }

    @Test
    fun conditioningExportContainsCriticalAuthorityRows() {
        val export = ProgramExport.conditioningRowsCsv()
        assertTrue(export.contains("w1_300_baseline"))
        assertTrue(export.contains("w10_vo2"))
        assertTrue(export.contains("w16_checkpoint"))
        assertTrue(export.contains("w21_special"))
        assertTrue(export.contains("w24_checkpoint"))
        assertTrue(export.contains("w28_goal"))
        assertTrue(export.contains("w32_final300"))
        assertTrue(export.contains("w32_final15"))
    }

    @Test
    fun normalWeeksRetainHypertrophyChassisAndWeekThirtyTwoIsOnlyTaperException() {
        (1..31).forEach { week ->
            OperatorProgramV56.resistanceSlots.forEach { slot ->
                val session = SessionResolver.resolve(week, slot.day, deload = false)
                assertEquals("Week $week ${slot.id}", slot.workingSets, session.rows.single { it.id == slot.id }.effortCount)
            }
        }
        val w32 = SessionResolver.resolve(32, 1, deload = false)
        assertEquals(2, w32.rows.single { it.id == "d1_squat" }.effortCount)
    }

    @Test
    fun checkpointAndFinalResultsAreReferenceOnly() {
        val text = listOf(16, 24, 32).flatMap { week ->
            val p = OperatorProgramV56.conditioningForWeek(week)
            p.speed + p.engine
        }.joinToString(" ") { it.prescription }
        assertTrue(text.contains("not stored"))
        assertFalse(text.contains("enter your time", ignoreCase = true))
        assertFalse(text.contains("save result", ignoreCase = true))
    }
}
