package com.jk.operatorperformance.program

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SprintWarningTest {
    @Test
    fun everyBuiltInSixHundredMeterSpeedSessionSurfacesMandatoryWarning() {
        listOf(19, 21, 26, 28).forEach { week ->
            val session = SessionResolver.resolve(week, 3, deload = false)
            val text = session.rows.joinToString(" ") { it.note.orEmpty() }
            assertTrue("Week $week must display the >500–600 m warning", text.contains("SPRINT VOLUME WARNING"))
            assertTrue(text.contains("Do not exceed 600 m"))
        }
    }

    @Test
    fun foundationSpecialEnduranceReferenceAlsoSurfacesWarning() {
        val session = SessionResolver.resolve(
            week = 2,
            day = 3,
            deload = false,
            speedVariant = SpeedVariant.SPECIAL_ENDURANCE,
        )
        assertTrue(session.rows.joinToString(" ") { it.note.orEmpty() }.contains("SPRINT VOLUME WARNING"))
    }

    @Test
    fun fiveHundredMeterSessionDoesNotShowCapWarning() {
        val session = SessionResolver.resolve(23, 3, deload = false)
        assertFalse(session.rows.joinToString(" ") { it.note.orEmpty() }.contains("SPRINT VOLUME WARNING"))
    }
}
