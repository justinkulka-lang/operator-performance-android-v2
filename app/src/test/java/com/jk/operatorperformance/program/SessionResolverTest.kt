package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionResolverTest {
    @Test
    fun weekOneSpeedIsFullWorkoutNotTestAndIncludesCooldown() {
        val session = SessionResolver.resolve(1, 3, deload = false)
        assertEquals(
            listOf("d3_speed_warmup", "w1_accel", "d3_machine_power_finisher", "d3_speed_cooldown"),
            session.rows.map { it.id },
        )
        assertTrue(session.rows.single { it.id == "w1_accel" }.prescription.contains("6 × 30 m"))
        assertTrue(session.rows.single { it.id == "w1_accel" }.note.orEmpty().contains("No incline sprinting"))
        assertEquals("5:00 easy walk after the last sprint/finisher.", session.rows.last().prescription)
        assertFalse(session.rows.joinToString(" ") { it.prescription }.contains("baseline", ignoreCase = true))
    }

    @Test
    fun unconfiguredEquipmentShowsOnlyApprovedFinisherPrimaryAndAlternative() {
        val session = SessionResolver.resolve(2, 3, deload = false)
        val finisher = session.rows.single { it.id == "d3_machine_power_finisher" }
        assertEquals("Optional machine-power finisher — Air/fan bike", finisher.title)
        assertEquals(listOf("Stationary cycle ergometer"), finisher.alternatives)
        assertEquals(6, finisher.effortCount)
        assertEquals(80, finisher.restSeconds)
        assertTrue(finisher.prescription.contains("maximal but smooth"))
        assertTrue(finisher.prescription.contains("very easy"))
        assertTrue(finisher.note.orEmpty().contains("light-to-moderate resistance"))
    }

    @Test
    fun configuredMachineInventoryUsesCycleWhenAirBikeUnavailable() {
        val session = SessionResolver.resolve(
            week = 2,
            day = 3,
            deload = false,
            machineInventoryConfigured = true,
            availableMachineModalities = setOf(MachineModality.STATIONARY_CYCLE, MachineModality.ROW_ERG),
        )
        val finisher = session.rows.single { it.id == "d3_machine_power_finisher" }
        assertEquals("Optional machine-power finisher — Stationary cycle ergometer", finisher.title)
        assertTrue(finisher.alternatives.isEmpty())
    }

    @Test
    fun configuredInventoryOmitsFinisherWhenNeitherApprovedMachineExists() {
        val session = SessionResolver.resolve(
            week = 2,
            day = 3,
            deload = false,
            machineInventoryConfigured = true,
            availableMachineModalities = setOf(MachineModality.ROW_ERG, MachineModality.SKI_ERG),
        )
        assertFalse(session.rows.any { it.id == "d3_machine_power_finisher" })
        assertEquals("d3_speed_cooldown", session.rows.last().id)
    }

    @Test
    fun speedEnduranceWeeksNeverAppendMachineFinisher() {
        val session = SessionResolver.resolve(
            week = 5,
            day = 3,
            deload = false,
            machineInventoryConfigured = true,
            availableMachineModalities = MachineModality.entries.toSet(),
        )
        assertTrue(session.rows.any { it.id == "w5_speed_endurance" })
        assertFalse(session.rows.any { it.id == "d3_machine_power_finisher" })
        assertEquals("d3_speed_cooldown", session.rows.last().id)
    }

    @Test
    fun weekOneEngineIsFixedWorkoutAndPrecedesShortUpper() {
        val session = SessionResolver.resolve(1, 6, deload = false)
        assertEquals("d6_engine_warmup", session.rows.first().id)
        assertEquals("w1_engine", session.rows[1].id)
        assertEquals("d6_engine_cooldown", session.rows[2].id)
        assertEquals("d6_incline", session.rows[3].id)
        assertEquals("Threshold intervals", session.rows[1].title)
        assertTrue(session.rows[1].prescription.contains("Treadmill 0%"))
        assertTrue(session.rows[1].prescription.contains("RECOVERY 2:00 EASY WALK/JOG"))
        assertTrue(session.rows[2].note.orEmpty().contains("5–10 min transition"))
        assertFalse(session.rows.joinToString(" ") { it.prescription }.contains("time trial", ignoreCase = true))
    }

    @Test
    fun eachWeekResolvesItsOwnFixedEngineSession() {
        val week2 = SessionResolver.resolve(2, 6, deload = false)
        val week7 = SessionResolver.resolve(7, 6, deload = false)
        assertTrue(week2.rows.any { it.id == "w2_engine" && it.prescription.contains("8.5 mph") })
        assertTrue(week7.rows.any { it.id == "w7_engine" && it.prescription.contains("9.0 mph") })
        assertEquals("VO₂ / goal-pace intervals", week7.rows.single { it.id == "w7_engine" }.title)
    }

    @Test
    fun deloadUsesFixedReducedPrescriptionsAndOmitsZeroSetRows() {
        val day2 = SessionResolver.resolve(5, 2, deload = true)
        assertFalse(day2.rows.any { it.id == "d2_abs" })
        assertTrue(day2.rows.single { it.id == "d2_triceps" }.prescription.startsWith("1 × 10–12 @ 1 RIR"))

        val day3 = SessionResolver.resolve(
            week = 5,
            day = 3,
            deload = true,
            machineInventoryConfigured = true,
            availableMachineModalities = MachineModality.entries.toSet(),
        )
        assertEquals(
            listOf("d3_deload_warmup", "d3_deload_relaxed_accel", "d3_speed_cooldown"),
            day3.rows.map { it.id },
        )
        assertFalse(day3.rows.any { it.id == "d3_machine_power_finisher" })
    }

    @Test
    fun dayOneAerobicIsSpecificAndHasOneAlternative() {
        val row = SessionResolver.resolve(1, 1, deload = false).rows.single { it.id == "d1_easy_aerobic" }
        assertTrue(row.prescription.contains("25:00"))
        assertTrue(row.prescription.contains("80–90 rpm"))
        assertTrue(row.prescription.contains("lowest resistance"))
        assertTrue(row.prescription.contains("increase resistance one machine level"))
        assertTrue(row.prescription.contains("decrease one level"))
        assertTrue(row.prescription.contains("No intervals, no sprint finish, no added running"))
        assertEquals(1, row.alternatives.size)
        assertTrue(row.alternatives.single().startsWith("Air/fan bike — 25:00"))
    }

    @Test
    fun daySevenIsRestReference() {
        val session = SessionResolver.resolve(3, 7, deload = false)
        assertEquals(1, session.rows.size)
        assertEquals(SessionRowKind.REST, session.rows.single().kind)
        assertTrue(session.rows.single().prescription.contains("No lifting"))
        assertTrue(session.rows.single().prescription.contains("no running", ignoreCase = true))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unpopulatedWeeksCannotResolve() {
        SessionResolver.resolve(9, 1, deload = false)
    }
}
