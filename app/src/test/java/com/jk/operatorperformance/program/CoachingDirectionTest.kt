package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachingDirectionTest {
    @Test
    fun week1SpeedUsesOnlyAuthorizedWarmupAndBaselineRows() {
        val session = SessionResolver.resolve(week = 1, day = 3, deload = false)

        assertEquals(listOf("d3_speed_warmup", "w1_300_baseline"), session.rows.map { it.id })
        val warmup = session.rows.first()
        assertTrue(warmup.note.orEmpty().contains("No heavy resistance work in the same session"))
        assertTrue(warmup.note.orEmpty().contains("sprint quality is the priority stimulus"))
        assertFalse(session.rows.any { it.id.contains("cooldown", ignoreCase = true) })
    }

    @Test
    fun engineDoesNotSynthesizeWarmupOrCooldownRows() {
        val session = SessionResolver.resolve(week = 1, day = 6, deload = false)

        assertEquals("w1_15_baseline", session.rows.first().id)
        assertFalse(session.rows.any { it.id == "d6_engine_warmup" || it.id == "d6_engine_cooldown" })
        assertTrue(session.rows.drop(1).any { it.kind == SessionRowKind.RESISTANCE })
    }

    @Test
    fun programNotesRestoreApprovedDirectCoachingGuidance() {
        assertTrue(ProgramNotesV56.resistanceProgression.any { it.contains("reps before load") })
        assertTrue(ProgramNotesV56.foundationSpeedSelection.any { it.contains("two quality ACCELERATION exposures") })
        assertTrue(ProgramNotesV56.foundationEngineSelection.any { it.contains("5 × 1000 m") })
        assertTrue(ProgramNotesV56.deload.any { it.contains("5–10%") })
        assertTrue(ProgramNotesV56.deload.any { it.contains("6–8 weeks") })
        assertTrue(ProgramNotesV56.execution.any { it.contains("controlled, strong-but-repeatable 1.5-mile") })
    }

    @Test
    fun authorityLimitsMakeMissingNormativeValuesExplicitInsteadOfInventingThem() {
        val text = ProgramNotesV56.authorityLimits.joinToString(" ")
        assertTrue(text.contains("does not specify an exact count, distance, or percentage progression"))
        assertTrue(text.contains("does not provide a numeric recovery duration"))
        assertTrue(text.contains("must not invent"))
    }
}
