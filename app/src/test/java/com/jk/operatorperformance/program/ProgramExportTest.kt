package com.jk.operatorperformance.program

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramExportTest {
    @Test
    fun computedProgramExportsComeFromCanonicalV57Rows() {
        val reports = File("build/reports").apply { mkdirs() }
        val matrix = File(reports, "program-matrix.csv")
        val rows = File(reports, "program-resistance-rows.csv")
        val weeks = File(reports, "program-weeks-1-32.csv")
        val conditioning = File(reports, "program-conditioning-weeks-1-32.csv")
        val phases = File(reports, "program-phases.csv")
        matrix.writeText(ProgramExport.programMatrixCsv())
        rows.writeText(ProgramExport.resistanceRowsCsv())
        weeks.writeText(ProgramExport.programWeeksCsv())
        conditioning.writeText(ProgramExport.conditioningRowsCsv())
        phases.writeText(ProgramExport.phaseReferenceCsv())

        assertTrue(matrix.readText().contains("Quads,13.0,2,10.0,2"))
        assertTrue(rows.readText().contains("d5_deadlift"))
        assertTrue(rows.readText().contains("1:Trap-bar deadlift"))
        assertTrue(rows.readText().contains("Rest ~2–3+ min"))

        val conditioningText = conditioning.readText()
        assertTrue(conditioningText.contains("w1_300_baseline"))
        assertTrue(conditioningText.contains("w16_checkpoint"))
        assertTrue(conditioningText.contains("w24_checkpoint"))
        assertTrue(conditioningText.contains("w32_final300"))
        assertTrue(conditioningText.contains("w32_final15"))
        assertTrue(conditioningText.contains("MANUAL_REFERENCE_DEFAULT"))

        val weekLines = weeks.readLines()
        assertEquals(33, weekLines.size)
        assertTrue(weekLines[1].startsWith("1,\"FOUNDATION\",\"HEPHAESTUS\",POPULATED_NORMAL,"))
        assertTrue(weekLines[9].startsWith("9,\"DEVELOPMENT\",\"HERMES\",POPULATED_NORMAL,"))
        assertTrue(weekLines[17].startsWith("17,\"SPECIFIC DEVELOPMENT\",\"ARES\",POPULATED_NORMAL,"))
        assertTrue(weekLines[25].startsWith("25,\"INTEGRATION / PEAK\",\"ZEUS\",POPULATED_NORMAL,"))
        assertTrue(weekLines[32].startsWith("32,\"INTEGRATION / PEAK\",\"ZEUS\",POPULATED_REALIZATION_TAPER,"))

        val phaseText = phases.readText()
        assertTrue(phaseText.contains("\"HEPHAESTUS\""))
        assertTrue(phaseText.contains("\"HERMES\""))
        assertTrue(phaseText.contains("\"ARES\""))
        assertTrue(phaseText.contains("\"ZEUS\""))
    }
}
