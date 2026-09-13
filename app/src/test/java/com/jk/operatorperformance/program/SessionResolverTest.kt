package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionResolverTest {
    @Test
    fun weekOneSpeedUsesBaselineAndNoMachineFinisher() {
        val session = SessionResolver.resolve(1, 3, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, deload = false)
        assertEquals(1, session.rows.size)
        assertEquals("d3_baseline_300m", session.rows.single().id)
        assertTrue(session.rows.single().prescription.contains("300 m"))
        assertFalse(session.rows.any { it.id == "d3_machine_power_finisher" })
    }

    @Test
    fun postBaselineAccelerationAppendsCanonicalMachineHierarchyWhenInventoryIsUnconfigured() {
        val session = SessionResolver.resolve(2, 3, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, deload = false)
        assertEquals(listOf("d3_acceleration", "d3_machine_power_finisher"), session.rows.map { it.id })
        val finisher = session.rows.last()
        assertEquals("6 × 10 s hard / 80 s easy", finisher.prescription)
        assertEquals(
            listOf("Air/fan bike", "Stationary cycle ergometer", "Row ergometer", "Ski ergometer"),
            finisher.alternatives,
        )
    }

    @Test
    fun configuredMachineInventoryResolvesHighestRankedAvailableModality() {
        val session = SessionResolver.resolve(
            week = 2,
            day = 3,
            speedVariant = SpeedVariant.ACCELERATION,
            engineVariant = EngineVariant.THRESHOLD,
            deload = false,
            machineInventoryConfigured = true,
            availableMachineModalities = setOf(MachineModality.ROW_ERG, MachineModality.SKI_ERG),
        )
        val finisher = session.rows.single { it.id == "d3_machine_power_finisher" }
        assertEquals("Optional machine-power finisher — Row ergometer", finisher.title)
        assertTrue(finisher.alternatives.isEmpty())
    }

    @Test
    fun configuredMachineInventoryOmitsFinisherWhenNoAuthorizedMachineIsAvailable() {
        val session = SessionResolver.resolve(
            week = 2,
            day = 3,
            speedVariant = SpeedVariant.MAX_VELOCITY,
            engineVariant = EngineVariant.THRESHOLD,
            deload = false,
            machineInventoryConfigured = true,
            availableMachineModalities = emptySet(),
        )
        assertEquals(listOf("d3_max_velocity"), session.rows.map { it.id })
        assertFalse(session.rows.any { it.id == "d3_machine_power_finisher" })
    }

    @Test
    fun specialEnduranceNeverAppendsMachineFinisher() {
        val session = SessionResolver.resolve(
            4,
            3,
            SpeedVariant.SPECIAL_ENDURANCE,
            EngineVariant.THRESHOLD,
            deload = false,
            machineInventoryConfigured = true,
            availableMachineModalities = MachineModality.entries.toSet(),
        )
        assertEquals(1, session.rows.size)
        assertEquals("d3_special_endurance", session.rows.single().id)
        assertTrue(session.rows.single().prescription.contains("3 × 200 m"))
        assertTrue(session.rows.single().prescription.contains("2 × 300 m"))
    }

    @Test
    fun weekOneEngineBaselineReplacesNormalEngineReference() {
        val session = SessionResolver.resolve(1, 6, SpeedVariant.ACCELERATION, EngineVariant.VO2, deload = false)
        assertTrue(session.rows.any { it.id == "d6_baseline_15mi" })
        assertFalse(session.rows.any { it.id == "d6_engine_vo2" })
    }

    @Test
    fun manualEngineSelectionRepeatsWithoutCalendarInference() {
        val week2 = SessionResolver.resolve(2, 6, SpeedVariant.ACCELERATION, EngineVariant.VO2, deload = false)
        val week7 = SessionResolver.resolve(7, 6, SpeedVariant.ACCELERATION, EngineVariant.VO2, deload = false)
        assertTrue(week2.rows.any { it.id == "d6_engine_vo2" })
        assertTrue(week7.rows.any { it.id == "d6_engine_vo2" })
    }

    @Test
    fun deloadOmitsZeroSetRowsAndSuppressesSpeedWork() {
        val day2 = SessionResolver.resolve(5, 2, SpeedVariant.MAX_VELOCITY, EngineVariant.THRESHOLD, deload = true)
        assertFalse(day2.rows.any { it.id == "d2_abs" })
        assertEquals("1 × 10–12 @ 1 RIR", day2.rows.single { it.id == "d2_triceps" }.prescription)

        val day3 = SessionResolver.resolve(
            5,
            3,
            SpeedVariant.MAX_VELOCITY,
            EngineVariant.THRESHOLD,
            deload = true,
            machineInventoryConfigured = true,
            availableMachineModalities = MachineModality.entries.toSet(),
        )
        assertEquals(listOf("d3_deload"), day3.rows.map { it.id })
        assertFalse(day3.rows.any { it.id == "d3_machine_power_finisher" })
    }

    @Test
    fun daySevenIsRestReference() {
        val session = SessionResolver.resolve(3, 7, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, deload = false)
        assertEquals(1, session.rows.size)
        assertEquals(SessionRowKind.REST, session.rows.single().kind)
        assertTrue(session.rows.single().prescription.contains("No lifting"))
        assertTrue(session.rows.single().prescription.contains("no running", ignoreCase = true))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unpopulatedWeeksCannotResolve() {
        SessionResolver.resolve(9, 1, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, deload = false)
    }
}
