package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramV56Test {
    @Test
    fun authorityInvariantsPass() {
        assertEquals(emptyList<String>(), ProgramAudit.validate())
    }

    @Test
    fun exactNormalAndDeloadTotalsMatchAuthority() {
        assertEquals(148.0, ProgramAudit.credits(deload = false).values.sum(), 0.0)
        assertEquals(117.5, ProgramAudit.credits(deload = true).values.sum(), 0.0)
    }

    @Test
    fun authorityIdentityMatchesApprovedDocument() {
        assertEquals("5.6", OperatorProgramV56.AUTHORITY_VERSION)
        assertEquals("7c220f57ac02bdb53fbbafb5b740219b03737ad848d7ce171d6b4b46ec4f8139", OperatorProgramV56.AUTHORITY_SHA256)
    }

    @Test
    fun onlyWeeksOneThroughEightArePopulated() {
        (1..8).forEach { assertTrue(OperatorProgramV56.weekOrNull(it) != null) }
        (9..32).forEach { assertNull(OperatorProgramV56.weekOrNull(it)) }
    }

    @Test
    fun everyResistanceMovementHasExactlyOneAlternativeAndPrescribedRest() {
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            assertEquals(1, slot.alternatives.size)
            assertEquals(1, slot.alternatives.single().rank)
            assertTrue(slot.restSeconds in setOf(90, 120, 180))
        }
    }

    @Test
    fun allEightWeeksHaveFixedSpeedAndEngineWorkWithoutTests() {
        assertEquals((1..8).toList(), OperatorProgramV56.conditioningByWeek.map { it.week })
        OperatorProgramV56.conditioningByWeek.forEach { week ->
            assertTrue(week.speed.isNotEmpty())
            assertTrue(week.engine.isNotEmpty())
            val text = (week.speed + week.engine).joinToString(" ") { it.title + " " + it.prescription }
            assertFalse(text.contains("baseline", ignoreCase = true))
            assertFalse(text.contains("time trial", ignoreCase = true))
        }
    }

    @Test
    fun fixedSpeedScheduleStaysAtOrBelowFiveHundredQualifyingMeters() {
        OperatorProgramV56.conditioningByWeek.forEach { week ->
            val meters = week.speed.sumOf { it.qualifyingSprintMeters }
            assertTrue("Week ${week.week} has $meters m", meters <= 500)
        }
    }

    @Test
    fun sprintBoundaryGuardRemainsExact() {
        assertEquals(SprintVolumeStatus.ALLOWED, ProgramAudit.sprintVolumeStatus(500))
        assertEquals(SprintVolumeStatus.WARNING, ProgramAudit.sprintVolumeStatus(501))
        assertEquals(SprintVolumeStatus.WARNING, ProgramAudit.sprintVolumeStatus(600))
        assertEquals(SprintVolumeStatus.PROHIBITED, ProgramAudit.sprintVolumeStatus(601))
    }

    @Test
    fun machineFinisherMatchesV56Authority() {
        val finisher = OperatorProgramV56.machinePowerFinisher
        assertEquals(6, finisher.efforts)
        assertEquals(10, finisher.hardSeconds)
        assertEquals(80, finisher.easySeconds)
        assertEquals(MachineModality.AIR_FAN_BIKE, finisher.primary)
        assertEquals(MachineModality.STATIONARY_CYCLE, finisher.alternative)
    }

    @Test
    fun exactConditioningExecutionRulesArePresent() {
        val day1 = OperatorProgramV56.day1EasyAerobic.prescription
        assertTrue(day1.contains("lowest resistance"))
        assertTrue(day1.contains(">90 rpm"))
        assertTrue(day1.contains("increase resistance one machine level"))
        assertTrue(day1.contains("cadence falls <80 rpm"))
        assertTrue(day1.contains("No intervals, no sprint finish, no added running"))

        assertTrue(OperatorProgramV56.speedStopRule.contains("flat track or measured flat turf/straightaway"))
        assertTrue(OperatorProgramV56.speedStopRule.contains("No incline sprinting"))
        assertTrue(OperatorProgramV56.speedStopRule.contains("no machine substitute", ignoreCase = true))
        assertEquals("5:00 easy walk after the last sprint/finisher.", OperatorProgramV56.speedCooldownPrescription)
        assertTrue(OperatorProgramV56.engineWarmupPrescription.contains("40 s easy walk/jog"))

        assertEquals("Threshold intervals", OperatorProgramV56.conditioningForWeek(1).engine.single().title)
        assertTrue(OperatorProgramV56.conditioningForWeek(3).speed.single().prescription.contains("fly zone fast and smooth"))
        assertTrue(OperatorProgramV56.conditioningForWeek(5).speed.single().prescription.contains("relaxed-fast, preserve mechanics"))
        assertTrue(OperatorProgramV56.conditioningForWeek(6).speed.single().prescription.contains("even effort, preserve mechanics"))
        assertTrue(OperatorProgramV56.conditioningForWeek(7).speed.single().prescription.contains("controlled first half, hold form home"))
        assertEquals("Goal-pace intervals", OperatorProgramV56.conditioningForWeek(8).engine.single().title)
    }

    @Test
    fun canonicalProgramContainsNoSledOrRuckMovement() {
        val resistanceText = OperatorProgramV56.resistanceSlots.joinToString(" ") { slot ->
            slot.movement + " " + slot.alternatives.single().name
        }
        assertFalse(resistanceText.contains("sled", ignoreCase = true))
        assertFalse(resistanceText.contains("ruck", ignoreCase = true))
    }
}
