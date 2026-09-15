package com.jk.operatorperformance.ui

import com.jk.operatorperformance.state.ProgramUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramSearchTest {
    @Test
    fun compactWeekDayQueryResolvesExactApprovedSession() {
        val hits = searchProgram("W17 D3", ProgramUiState())

        assertEquals(1, hits.size)
        assertEquals(17, hits.single().session.week)
        assertEquals(3, hits.single().session.day)
    }

    @Test
    fun exerciseTextFindsCanonicalSessionContent() {
        val hits = searchProgram("safety-bar squat", ProgramUiState())

        assertTrue(hits.isNotEmpty())
        assertTrue(hits.any { it.matchSnippet.contains("squat", ignoreCase = true) })
    }

    @Test
    fun searchNeverCreatesUnauthorizedWeek33() {
        assertTrue(searchProgram("W33", ProgramUiState()).isEmpty())
    }

    @Test
    fun normalizationSupportsCompactJumpSyntax() {
        assertEquals("week 24 day 6", normalizeProgramQuery("W24 D6"))
    }
}
