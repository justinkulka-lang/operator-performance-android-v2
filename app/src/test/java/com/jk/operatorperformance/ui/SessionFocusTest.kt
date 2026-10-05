package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionFocusTest {
    @Test fun jumpAndThrowPreparationKeepsSpeedLeadAcrossAllAuthorities() {
        for (authority in ProgramAuthority.entries) for (week in 1..32) {
            val day = if (authority == ProgramAuthority.V71) 3 else 4
            val session = SessionResolver.resolve(week, day, authority)
            assertTrue("$authority W$week", sessionFocusLine(session).startsWith("Speed lead ·"))
            session.rows.filter { it.id.endsWith("_plyometrics") || it.id.endsWith("_throws") }.forEach {
                assertEquals(MapSymbol.SPEED, rowSymbol(session,it))
            }
        }
        assertEquals("Speed lead · 12 blocks", sessionFocusLine(SessionResolver.resolve(3,4,ProgramAuthority.V73)))
        assertTrue(sessionFocusLine(SessionResolver.resolve(13,4,ProgramAuthority.V73)).startsWith("Speed lead ·"))
        assertTrue(sessionFocusLine(SessionResolver.resolve(17,4,ProgramAuthority.V73)).startsWith("Speed lead ·"))
    }
    @Test fun representativeDaysUseDerivedFocusLines() {
        assertEquals("Rest · no training blocks", line(1, 7))
        assertEquals("Rest · no training blocks", line(32, 7))
        assertEquals("Quads lead · 6 blocks", line(2, 2))
        assertEquals("Engine lead · 7 blocks", line(2, 1))
        assertEquals("Upper lead · 8 blocks", line(2, 3))
        assertEquals("Speed lead · 1 block", line(2, 4))
        assertEquals("Upper lead · 9 blocks", line(2, 5))
        assertEquals("Quads lead · 8 blocks", line(2, 6))
        assertEquals("Engine lead · 1 block", line(1, 1))
        assertEquals("Engine lead · 1 block", line(32, 1))
        assertEquals("Recovery lead · 1 block", line(32, 2))
        assertEquals("Recovery lead · 1 block", line(32, 6))
        assertEquals("Upper lead · 4 blocks", line(32, 5))
        assertEquals("Speed lead · 1 block", line(32, 4))
    }

    @Test fun engineFirstMixedDaysStayEngineFirst() {
        val mixed = SessionResolver.resolve(2, 1, ProgramAuthority.V72)
        assertEquals("Engine then short upper", mixed.title)
        assertTrue(mixed.rows.any { it.kind.name == "RESISTANCE" })
        assertEquals("Engine lead · 7 blocks", sessionFocusLine(mixed))
        val retained = SessionResolver.resolve(2, 6, ProgramAuthority.V71)
        assertEquals("Engine + short upper", retained.title)
        assertTrue(sessionFocusLine(retained).startsWith("Engine lead · "))
        assertFalse(sessionFocusLine(retained).startsWith("Chest"))
    }

    @Test fun upperSessionsStayMixedAndWeek32ExceptionsStayPlain() {
        assertTrue(line(2, 3).startsWith("Upper lead · "))
        assertTrue(line(2, 5).startsWith("Upper lead · "))
        assertEquals("Rest · no training blocks", line(32, 7))
        assertEquals("Recovery lead · 1 block", line(32, 2))
        val retainedUpper = sessionFocusLine(SessionResolver.resolve(1, 2, ProgramAuthority.V71))
        val retainedRest = sessionFocusLine(SessionResolver.resolve(4, 7, ProgramAuthority.V71))
        val retainedRecovery = sessionFocusLine(SessionResolver.resolve(32, 5, ProgramAuthority.V71))
        assertTrue(retainedUpper.startsWith("Upper lead · "))
        assertEquals("Rest · no training blocks", retainedRest)
        assertEquals("Recovery lead · 1 block", retainedRecovery)
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
                assertEquals(calendarSymbol(ProgramAuthority.V72, week, day), sessionSymbol(session))
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
        assertEquals(MapSymbol.LOWER, sessionSymbol(SessionResolver.resolve(1, 1, ProgramAuthority.V71)))
        assertEquals(MapSymbol.UPPER_A, sessionSymbol(SessionResolver.resolve(1, 2, ProgramAuthority.V71)))
        assertEquals(MapSymbol.UPPER_B, sessionSymbol(SessionResolver.resolve(1, 4, ProgramAuthority.V71)))
        assertEquals(MapSymbol.ENGINE, sessionSymbol(SessionResolver.resolve(32, 2, ProgramAuthority.V72)))
        assertEquals(MapSymbol.ENGINE, sessionSymbol(SessionResolver.resolve(32, 6, ProgramAuthority.V72)))
        (1..32).forEach { week ->
            (1..7).forEach { day ->
                val retained = SessionResolver.resolve(week, day, ProgramAuthority.V71)
                assertEquals(calendarSymbol(ProgramAuthority.V71, week, day), sessionSymbol(retained))
            }
        }
    }

    private fun line(week: Int, day: Int) = sessionFocusLine(SessionResolver.resolve(week, day, ProgramAuthority.V72))

    /** Expected marks from the published day order, including the Week 32 title exceptions. */
    private fun calendarSymbol(authority: ProgramAuthority, week: Int, day: Int): MapSymbol = when (authority) {
        ProgramAuthority.V72, ProgramAuthority.V73 -> when (day) {
            7 -> MapSymbol.REST
            1 -> MapSymbol.ENGINE
            2, 6 -> if (week == 32) MapSymbol.ENGINE else MapSymbol.LOWER
            3 -> MapSymbol.UPPER_A
            4 -> MapSymbol.SPEED
            5 -> MapSymbol.UPPER_B
            else -> error("day $day")
        }
        ProgramAuthority.V71 -> when (day) {
            7 -> MapSymbol.REST
            1, 5 -> if (week == 32 && day == 5) MapSymbol.ENGINE else MapSymbol.LOWER
            2 -> MapSymbol.UPPER_A
            3 -> MapSymbol.SPEED
            4 -> MapSymbol.UPPER_B
            6 -> MapSymbol.ENGINE
            else -> error("day $day")
        }
    }
}
