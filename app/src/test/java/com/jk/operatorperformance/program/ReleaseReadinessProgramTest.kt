package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessProgramTest {
    @Test
    fun specialEnduranceSurfacesRequiredSixHundredMeterWarning() {
        val session = SessionResolver.resolve(
            week = 2,
            day = 3,
            speedVariant = SpeedVariant.SPECIAL_ENDURANCE,
            engineVariant = EngineVariant.THRESHOLD,
            deload = false,
        )
        val row = session.rows.single { it.id == "d3_special_endurance" }
        val note = assertNotNull(row.note).let { row.note.orEmpty() }
        assertTrue(note.contains("SPRINT VOLUME WARNING"))
        assertTrue(note.contains("600 m exactly is permitted with a warning"))
        assertTrue(note.contains("do not exceed 600 m"))
    }

    @Test
    fun computedWeekExportStillMatchesCanonicalPopulationBoundary() {
        val lines = ProgramExport.programWeeksCsv().lineSequence().filter { it.isNotBlank() }.toList()
        assertEquals(33, lines.size)
        assertTrue(lines[1].startsWith("1,Block 1,POPULATED,"))
        assertEquals("9,Block 2,UNPOPULATED,—,—,—", lines[9])
        assertEquals("32,Block 4,UNPOPULATED,—,—,—", lines[32])
    }
}
