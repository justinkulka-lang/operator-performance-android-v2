package com.jk.operatorperformance.program

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramExportTest {
    @Test
    fun computedProgramExportsComeFromCanonicalRows() {
        val reports = File("build/reports").apply { mkdirs() }
        val matrix = File(reports, "program-matrix.csv")
        val rows = File(reports, "program-resistance-rows.csv")
        matrix.writeText(ProgramExport.programMatrixCsv())
        rows.writeText(ProgramExport.resistanceRowsCsv())

        assertTrue(matrix.readText().contains("Quads,13.0,2,10.0,2"))
        assertTrue(rows.readText().contains("d5_deadlift"))
        assertTrue(rows.readText().contains("Trap-bar deadlift"))
    }
}
