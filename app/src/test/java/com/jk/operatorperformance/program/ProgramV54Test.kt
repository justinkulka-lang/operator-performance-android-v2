package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramV54Test {
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
    fun onlyWeeksOneThroughEightArePopulated() {
        (1..8).forEach { assertTrue(OperatorProgramV54.weekOrNull(it) != null) }
        (9..32).forEach { assertNull(OperatorProgramV54.weekOrNull(it)) }
    }

    @Test
    fun weekOneIsBaselineAndFirstPostBaselineDefaultsAreOneTimeReferences() {
        val week1 = OperatorProgramV54.weekOrNull(1)!!
        assertTrue(week1.speedBaseline)
        assertTrue(week1.engineBaseline)
        OperatorProgramV54.weeks.drop(1).forEach {
            assertFalse(it.speedBaseline)
            assertFalse(it.engineBaseline)
        }
        assertEquals(SpeedVariant.ACCELERATION, OperatorProgramV54.firstPostBaselineSpeedDefault)
        assertEquals(EngineVariant.THRESHOLD, OperatorProgramV54.firstPostBaselineEngineDefault)
    }

    @Test
    fun sprintBoundaryIsExact() {
        assertEquals(SprintVolumeStatus.ALLOWED, ProgramAudit.sprintVolumeStatus(500))
        assertEquals(SprintVolumeStatus.WARNING, ProgramAudit.sprintVolumeStatus(501))
        assertEquals(SprintVolumeStatus.WARNING, ProgramAudit.sprintVolumeStatus(600))
        assertEquals(SprintVolumeStatus.PROHIBITED, ProgramAudit.sprintVolumeStatus(601))
    }

    @Test
    fun machineFinisherMatchesAuthorityAndSpecialEnduranceOmitsIt() {
        val finisher = OperatorProgramV54.machinePowerFinisher
        assertEquals(6, finisher.efforts)
        assertEquals(10, finisher.hardSeconds)
        assertEquals(80, finisher.easySeconds)
        assertEquals(
            listOf(MachineModality.AIR_FAN_BIKE, MachineModality.STATIONARY_CYCLE, MachineModality.ROW_ERG, MachineModality.SKI_ERG),
            finisher.rankedModalities,
        )
        assertTrue(OperatorProgramV54.speedOptions.first { it.variant == SpeedVariant.ACCELERATION }.includesMachinePowerFinisher)
        assertTrue(OperatorProgramV54.speedOptions.first { it.variant == SpeedVariant.MAX_VELOCITY }.includesMachinePowerFinisher)
        assertFalse(OperatorProgramV54.speedOptions.first { it.variant == SpeedVariant.SPECIAL_ENDURANCE }.includesMachinePowerFinisher)
    }

    @Test
    fun canonicalProgramContainsNoSledMovementOrAlternative() {
        val resistanceText = OperatorProgramV54.resistanceSlots.joinToString(" ") { slot ->
            slot.movement + " " + slot.alternatives.joinToString(" ") { it.name }
        }
        assertFalse(resistanceText.contains("sled", ignoreCase = true))
    }
}
