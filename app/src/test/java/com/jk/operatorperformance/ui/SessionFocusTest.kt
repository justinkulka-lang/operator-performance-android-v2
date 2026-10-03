package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionFocusTest {
    @Test fun representativeDaysUseDerivedFocusLines() {
        assertEquals("Rest · no training blocks", line(1, 7))
        assertEquals("Rest · no training blocks", line(32, 7))
        assertEquals("Quads lead · 6 blocks", line(2, 2))
        assertEquals("Chest lead · 7 blocks", line(2, 1))
        assertEquals("Chest lead · 8 blocks", line(2, 3))
        assertEquals("Speed lead · 1 block", line(2, 4))
        assertEquals("Delts lead · 9 blocks", line(2, 5))
        assertEquals("Quads lead · 8 blocks", line(2, 6))
        assertEquals("Engine lead · 1 block", line(1, 1))
        assertEquals("Engine lead · 1 block", line(32, 1))
        assertEquals("Recovery lead · 1 block", line(32, 2))
        assertEquals("Speed lead · 1 block", line(32, 4))
    }

    @Test fun everyWeekHasAPlainFocusLineAndASymbol() {
        (1..32).forEach { week ->
            (1..7).forEach { day ->
                val session = SessionResolver.resolve(week, day, ProgramAuthority.V72)
                val focus = sessionFocusLine(session)
                assertTrue(focus.contains("·"))
                assertFalse(focus.contains("prescribed"))
                if (day == 7) {
                    assertEquals("Rest · no training blocks", focus)
                    assertEquals(MapSymbol.REST, sessionSymbol(session))
                } else {
                    assertTrue(focus.contains("lead"))
                }
                assertEquals(sessionSymbol(session), sessionSymbol(session))
            }
        }
    }

    @Test fun daySymbolsFollowTheSessionTitle() {
        assertEquals(MapSymbol.ENGINE, sessionSymbol(SessionResolver.resolve(2, 1, ProgramAuthority.V72)))
        assertEquals(MapSymbol.LOWER, sessionSymbol(SessionResolver.resolve(2, 2, ProgramAuthority.V72)))
        assertEquals(MapSymbol.UPPER_A, sessionSymbol(SessionResolver.resolve(2, 3, ProgramAuthority.V72)))
        assertEquals(MapSymbol.SPEED, sessionSymbol(SessionResolver.resolve(2, 4, ProgramAuthority.V72)))
        assertEquals(MapSymbol.UPPER_B, sessionSymbol(SessionResolver.resolve(2, 5, ProgramAuthority.V72)))
        assertEquals(MapSymbol.LOWER, sessionSymbol(SessionResolver.resolve(2, 6, ProgramAuthority.V72)))
        assertEquals(MapSymbol.ENGINE, sessionSymbol(SessionResolver.resolve(32, 1, ProgramAuthority.V72)))
        assertEquals(MapSymbol.UPPER_B, sessionSymbol(SessionResolver.resolve(32, 5, ProgramAuthority.V72)))
        assertEquals(MapSymbol.SPEED, sessionSymbol(SessionResolver.resolve(1, 3, ProgramAuthority.V71)))
        assertEquals(MapSymbol.ENGINE, sessionSymbol(SessionResolver.resolve(1, 6, ProgramAuthority.V71)))
    }

    private fun line(week: Int, day: Int) = sessionFocusLine(SessionResolver.resolve(week, day, ProgramAuthority.V72))
}
