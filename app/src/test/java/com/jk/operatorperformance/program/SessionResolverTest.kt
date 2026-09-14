package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionResolverTest {
    @Test
    fun weekOneUsesBaselineAndNoMachineFinisher() {
        val speed = SessionResolver.resolve(1, 3, deload = false, machineInventoryConfigured = true, availableMachineModalities = MachineModality.entries.toSet())
        assertTrue(speed.rows.any { it.id == "w1_300_baseline" })
        assertFalse(speed.rows.any { it.id == "d3_machine_power_finisher" })
        assertEquals("HEPHAESTUS", speed.phase.godName)

        val engine = SessionResolver.resolve(1, 6, deload = false)
        assertTrue(engine.rows.any { it.id == "w1_15_baseline" })
        assertTrue(engine.rows.indexOfFirst { it.id == "w1_15_baseline" } < engine.rows.indexOfFirst { it.id == "d6_incline" })
    }

    @Test
    fun foundationWeeksTwoThroughEightUseManualReferences() {
        val defaultSpeed = SessionResolver.resolve(2, 3, deload = false)
        assertTrue(defaultSpeed.rows.any { it.title == "ACCELERATION" })
        assertTrue(defaultSpeed.rows.any { it.id == "d3_machine_power_finisher" })

        val maxv = SessionResolver.resolve(6, 3, deload = false, speedVariant = SpeedVariant.MAX_VELOCITY)
        assertTrue(maxv.rows.any { it.title == "MAX VELOCITY" })
        assertTrue(maxv.rows.any { it.id == "d3_machine_power_finisher" })

        val special = SessionResolver.resolve(8, 3, deload = false, speedVariant = SpeedVariant.SPECIAL_ENDURANCE)
        assertTrue(special.rows.any { it.title == "SPECIAL ENDURANCE" })
        assertFalse(special.rows.any { it.id == "d3_machine_power_finisher" })

        val vo2 = SessionResolver.resolve(4, 6, deload = false, engineVariant = EngineVariant.VO2)
        assertTrue(vo2.rows.any { it.title == "VO2" })
        assertTrue(vo2.rows.first().id == "d6_engine_warmup")
        assertTrue(vo2.rows.indexOfFirst { it.title == "VO2" } < vo2.rows.indexOfFirst { it.id == "d6_incline" })
    }

    @Test
    fun populatedDevelopmentSpecificAndPeakWeeksResolveExactConditioning() {
        val w10 = SessionResolver.resolve(10, 3, deload = false)
        assertEquals("HERMES", w10.phase.godName)
        assertTrue(w10.rows.any { it.id == "w10_maxv" })

        val w21 = SessionResolver.resolve(21, 3, deload = false)
        assertEquals("ARES", w21.phase.godName)
        assertTrue(w21.rows.any { it.id == "w21_special" && it.prescription.contains("2 × 300 m") })

        val w26 = SessionResolver.resolve(26, 6, deload = false)
        assertEquals("ZEUS", w26.phase.godName)
        assertTrue(w26.rows.any { it.id == "w26_goal" && it.prescription.contains("6 × 400 m") })
    }

    @Test
    fun fullMachineHierarchyResolvesFirstAvailableAuthorizedOption() {
        val session = SessionResolver.resolve(
            week = 9,
            day = 3,
            deload = false,
            machineInventoryConfigured = true,
            availableMachineModalities = setOf(MachineModality.ROW_ERG, MachineModality.SKI_ERG),
        )
        val finisher = session.rows.single { it.id == "d3_machine_power_finisher" }
        assertEquals("Optional machine-power finisher — Row ergometer", finisher.title)
        assertEquals(6, finisher.effortCount)
        assertEquals(80, finisher.restSeconds)
    }

    @Test
    fun specialEnduranceCheckpointAndFinalTestsNeverAppendFinisher() {
        listOf(13, 19, 21, 23, 26, 28, 29, 16, 32).forEach { week ->
            val session = SessionResolver.resolve(
                week = week,
                day = 3,
                deload = false,
                machineInventoryConfigured = true,
                availableMachineModalities = MachineModality.entries.toSet(),
            )
            assertFalse("Week $week", session.rows.any { it.id == "d3_machine_power_finisher" })
        }
    }

    @Test
    fun dayOneUsesWeekSpecificAerobicPrescription() {
        val w9 = SessionResolver.resolve(9, 1, deload = false)
        assertTrue(w9.rows.last().id == "w9_d1")
        assertTrue(w9.rows.last().prescription.contains("25 min easy"))

        val w17 = SessionResolver.resolve(17, 1, deload = false)
        val run = w17.rows.last()
        assertEquals("w17_d1", run.id)
        assertTrue(run.prescription.contains("20 min easy continuous run"))
        assertTrue(run.note.orEmpty().contains("110%"))
        assertTrue(run.alternatives.single().contains("Stationary bike"))
    }

    @Test
    fun weekThirtyTwoUsesExactTaperSetsAndRirFloors() {
        val d1 = SessionResolver.resolve(32, 1, deload = false)
        assertEquals(2, d1.rows.single { it.id == "d1_squat" }.effortCount)
        assertEquals(1, d1.rows.single { it.id == "d1_rdl" }.effortCount)
        assertTrue(d1.rows.single { it.id == "d1_squat" }.note.orEmpty().contains("at least 3 RIR"))

        val d2 = SessionResolver.resolve(32, 2, deload = false)
        assertEquals(4, d2.rows.single { it.id == "d2_bench" }.effortCount)
        assertTrue(d2.rows.single { it.id == "d2_bench" }.note.orEmpty().contains("at least 1 RIR"))

        val d6 = SessionResolver.resolve(32, 6, deload = false)
        assertTrue(d6.rows.any { it.id == "w32_final15" })
        assertEquals(1, d6.rows.single { it.id == "d6_incline" }.effortCount)
        assertTrue(d6.rows.single { it.id == "d6_incline" }.note.orEmpty().contains("at least 2 RIR"))
    }

    @Test
    fun manualDeloadOverridesHardConditioningWithoutInventingPerformanceEntry() {
        val d3 = SessionResolver.resolve(21, 3, deload = true)
        assertEquals(listOf("d3_deload"), d3.rows.map { it.id })
        assertFalse(d3.rows.joinToString(" ") { it.prescription }.contains("300 m"))

        val d6 = SessionResolver.resolve(29, 6, deload = true)
        assertEquals("d6_deload_engine", d6.rows.first().id)
        assertTrue(d6.rows.first().prescription.contains("Keep running easy"))
        assertTrue(d6.rows.indexOfFirst { it.id == "d6_deload_engine" } < d6.rows.indexOfFirst { it.id == "d6_incline" })
    }

    @Test
    fun allThirtyTwoWeeksResolveSevenDays() {
        (1..32).forEach { week ->
            val split = SessionResolver.splitForWeek(week, deload = false)
            assertEquals("Week $week", 7, split.size)
            assertEquals((1..7).toList(), split.map { it.day })
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun weekThirtyThreeCannotResolve() {
        SessionResolver.resolve(33, 1, deload = false)
    }
}
