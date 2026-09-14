package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessProgramTest {
    @Test
    fun weekEightSpeedIsTrainingNotAssessmentAndStaysWithinFixedCap() {
        val week8 = OperatorProgramV56.conditioningForWeek(8)
        assertEquals(450, week8.speed.sumOf { it.qualifyingSprintMeters })
        val text = week8.speed.joinToString(" ") { it.title + " " + it.prescription }
        assertTrue(text.contains("300 m"))
        assertTrue(text.contains("training, not a test", ignoreCase = true))
        assertFalse(text.contains("baseline", ignoreCase = true))
        assertFalse(text.contains("time trial", ignoreCase = true))
    }

    @Test
    fun computedWeekExportStillMatchesCanonicalPopulationBoundary() {
        val lines = ProgramExport.programWeeksCsv().lineSequence().filter { it.isNotBlank() }.toList()
        assertEquals(33, lines.size)
        assertTrue(lines[1].startsWith("1,Block 1,POPULATED,"))
        assertEquals("9,Block 2,UNPOPULATED,—,—,—", lines[9])
        assertEquals("32,Block 4,UNPOPULATED,—,—,—", lines[32])
    }

    @Test
    fun conditioningExportContainsAllFixedWeekRows() {
        val export = ProgramExport.conditioningRowsCsv()
        assertTrue(export.contains("w1_accel"))
        assertTrue(export.contains("w8_special_endurance_300"))
        assertTrue(export.contains("w8_engine"))
    }
}
