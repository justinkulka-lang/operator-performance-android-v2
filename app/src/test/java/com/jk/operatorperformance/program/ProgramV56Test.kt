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
        assertEquals("c48ae8c6b0585d809758d682abc872510645dca864992e36f837396c328ecf03", OperatorProgramV56.AUTHORITY_SHA256)
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
    fun canonicalProgramContainsNoSledOrRuckMovement() {
        val resistanceText = OperatorProgramV56.resistanceSlots.joinToString(" ") { slot ->
            slot.movement + " " + slot.alternatives.single().name
        }
        assertFalse(resistanceText.contains("sled", ignoreCase = true))
        assertFalse(resistanceText.contains("ruck", ignoreCase = true))
    }
}
