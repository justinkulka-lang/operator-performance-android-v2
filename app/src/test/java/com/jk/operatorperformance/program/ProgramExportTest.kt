package com.jk.operatorperformance.program

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramExportTest {
    @Test
    fun computedProgramExportsComeFromCanonicalRows() {
        val reports = File("build/reports").apply { mkdirs() }
        val matrix = File(reports, "program-matrix.csv")
        val rows = File(reports, "program-resistance-rows.csv")
        val weeks = File(reports, "program-weeks-1-32.csv")
        matrix.writeText(ProgramExport.programMatrixCsv())
        rows.writeText(ProgramExport.resistanceRowsCsv())
        weeks.writeText(ProgramExport.programWeeksCsv())

        assertTrue(matrix.readText().contains("Quads,13.0,2,10.0,2"))
        assertTrue(rows.readText().contains("d5_deadlift"))
        assertTrue(rows.readText().contains("Trap-bar deadlift"))

        val weekLines = weeks.readLines()
        assertEquals(33, weekLines.size)
        assertEquals(
            "1,Block 1,POPULATED,\"13 / 11.5 / 11.5 / 12 / 12 / 11 / 15 / 11.5 / 12 / 10 / 9 / 9.5 / 10\",148.0,1.667",
            weekLines[1],
        )
        assertEquals(
            "8,Block 1,POPULATED,\"13 / 11.5 / 11.5 / 12 / 12 / 11 / 15 / 11.5 / 12 / 10 / 9 / 9.5 / 10\",148.0,1.667",
            weekLines[8],
        )
        assertEquals("9,Block 2,UNPOPULATED,—,—,—", weekLines[9])
        assertEquals("32,Block 4,UNPOPULATED,—,—,—", weekLines[32])
    }
}
