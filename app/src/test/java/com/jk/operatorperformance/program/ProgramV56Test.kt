package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramV56Test {
    @Test
    fun authorityAndAllProgramInvariantsPass() {
        assertEquals(emptyList<String>(), ProgramAudit.validate())
        assertEquals("5.7", OperatorProgramV56.AUTHORITY_VERSION)
        assertEquals("867c468ba616a22185b871c649d8c026be5c1c61b3a7f690edac5b6169fc3d0a", OperatorProgramV56.AUTHORITY_SHA256)
        assertEquals("ARES PERFORMANCE", OperatorProgramV56.PROGRAM_NAME)
    }

    @Test
    fun allThirtyTwoWeeksAndFourGreekLabeledPhasesArePopulated() {
        assertEquals((1..32).toList(), OperatorProgramV56.weeks.map { it.week })
        (1..32).forEach { assertNotNull(OperatorProgramV56.weekOrNull(it)) }
        assertEquals(listOf("HEPHAESTUS", "HERMES", "ARES", "ZEUS"), OperatorProgramV56.blocks.map { it.godName })
        assertEquals(listOf("FOUNDATION", "DEVELOPMENT", "SPECIFIC DEVELOPMENT", "INTEGRATION / PEAK"), OperatorProgramV56.blocks.map { it.name })
    }

    @Test
    fun hypertrophyChassisMatchesAuthority() {
        assertEquals(148.0, ProgramAudit.credits(false).values.sum(), 0.0)
        assertEquals(117.5, ProgramAudit.credits(true).values.sum(), 0.0)
        ProgramAudit.exposures(false).values.forEach { assertTrue(it >= 2) }
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            assertTrue(slot.alternatives.size in 2..4)
            assertEquals((1..slot.alternatives.size).toList(), slot.alternatives.map { it.rank })
            assertTrue(slot.restGuidance.contains("Rest ~"))
        }
    }

    @Test
    fun exactTestsAndCheckpointsAreInApprovedWeeksAndNeverImplyStorage() {
        val w1 = OperatorProgramV56.conditioningForWeek(1)
        assertEquals("300 M BASELINE", w1.speed.single().title)
        assertEquals("1.5-MILE BASELINE", w1.engine.single().title)
        assertTrue(w1.speed.single().prescription.contains("does not store"))
        assertTrue(w1.engine.single().prescription.contains("does not store"))

        val w16 = OperatorProgramV56.conditioningForWeek(16)
        assertEquals("300 M CHECKPOINT", w16.speed.single().title)
        assertTrue(w16.speed.single().prescription.contains("not stored"))

        val w24 = OperatorProgramV56.conditioningForWeek(24)
        assertEquals("1.5-MILE CHECKPOINT", w24.engine.single().title)
        assertTrue(w24.engine.single().prescription.contains("not stored"))

        val w32 = OperatorProgramV56.conditioningForWeek(32)
        assertEquals("FINAL 300 M TEST", w32.speed.single().title)
        assertEquals("FINAL 1.5-MILE TEST", w32.engine.single().title)
        assertTrue(w32.speed.single().prescription.contains("not stored"))
        assertTrue(w32.engine.single().prescription.contains("not stored"))
    }

    @Test
    fun manualFoundationReferencesStayManualAndLaterWeeksAreFixed() {
        val accel = OperatorProgramV56.conditioningForWeek(2, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD)
        val maxv = OperatorProgramV56.conditioningForWeek(2, SpeedVariant.MAX_VELOCITY, EngineVariant.VO2)
        assertEquals("ACCELERATION", accel.speed.single().title)
        assertEquals("THRESHOLD", accel.engine.single().title)
        assertEquals("MAX VELOCITY", maxv.speed.single().title)
        assertEquals("VO2", maxv.engine.single().title)

        val w10a = OperatorProgramV56.conditioningForWeek(10, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD)
        val w10b = OperatorProgramV56.conditioningForWeek(10, SpeedVariant.SPECIAL_ENDURANCE, EngineVariant.VO2)
        assertEquals(w10a, w10b)
        assertEquals("MAX VELOCITY", w10a.speed.single().title)
        assertEquals("VO2", w10a.engine.single().title)
    }

    @Test
    fun sprintCapAndMachineFinisherRulesAreExact() {
        (1..32).forEach { week ->
            val meters = OperatorProgramV56.conditioningForWeek(week).speed.sumOf { it.qualifyingSprintMeters }
            assertTrue("Week $week has $meters m", meters <= 600)
            if (meters > 500) assertEquals(SprintVolumeStatus.WARNING, ProgramAudit.sprintVolumeStatus(meters))
        }
        assertEquals(SprintVolumeStatus.ALLOWED, ProgramAudit.sprintVolumeStatus(500))
        assertEquals(SprintVolumeStatus.WARNING, ProgramAudit.sprintVolumeStatus(600))
        assertEquals(SprintVolumeStatus.PROHIBITED, ProgramAudit.sprintVolumeStatus(601))

        val finisher = OperatorProgramV56.machinePowerFinisher
        assertEquals(6, finisher.efforts)
        assertEquals(10, finisher.hardSeconds)
        assertEquals(80, finisher.easySeconds)
        assertEquals(
            listOf(MachineModality.AIR_FAN_BIKE, MachineModality.STATIONARY_CYCLE, MachineModality.ROW_ERG, MachineModality.SKI_ERG),
            finisher.hierarchy,
        )
    }

    @Test
    fun weekThirtyTwoTaperIsDeterministic() {
        val expected = mapOf(
            "d1_squat" to 2, "d1_rdl" to 1, "d1_leg_press" to 1, "d1_leg_curl" to 1, "d1_calf" to 2, "d1_abs" to 1,
            "d5_deadlift" to 2, "d5_hip" to 1, "d5_bss" to 1, "d5_extension" to 1, "d5_calf" to 2, "d5_abs" to 1, "d5_wrist" to 1,
            "d6_incline" to 1, "d6_fly" to 1, "d6_pushdown" to 1, "d6_wrist" to 1, "d6_calf" to 1, "d6_abs" to 1,
        )
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            assertEquals(expected[slot.id] ?: slot.workingSets, OperatorProgramV56.week32Sets(slot.id, slot.workingSets))
        }
        assertEquals(3, OperatorProgramV56.week32RirFloor(1))
        assertEquals(1, OperatorProgramV56.week32RirFloor(2))
        assertEquals(1, OperatorProgramV56.week32RirFloor(4))
        assertEquals(3, OperatorProgramV56.week32RirFloor(5))
        assertEquals(2, OperatorProgramV56.week32RirFloor(6))
    }

    @Test
    fun canonicalProgramContainsNoSledOrRuckPrescription() {
        val text = buildString {
            OperatorProgramV56.resistanceSlots.forEach { append(it.movement).append(' ').append(it.alternatives.joinToString { alt -> alt.name }) }
            (1..32).forEach { week ->
                val plan = OperatorProgramV56.conditioningForWeek(week)
                (plan.d1 + plan.speed + plan.engine).forEach { append(it.title).append(' ').append(it.prescription) }
            }
        }
        assertFalse(text.contains("sled", ignoreCase = true))
        assertFalse(text.contains("ruck", ignoreCase = true))
    }
}
