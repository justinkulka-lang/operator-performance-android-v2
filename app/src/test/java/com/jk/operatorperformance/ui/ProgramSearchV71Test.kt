package com.jk.operatorperformance.ui

import com.jk.operatorperformance.state.ProgramUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramSearchV71Test {
    @Test fun weekAndDayTokensResolve() {
        val hits=searchProgram("W32 D3 final 300",ProgramUiState(initialized=true))
        assertEquals(1,hits.size)
        assertEquals(32,hits.single().session.week)
        assertEquals(3,hits.single().session.day)
    }
    @Test fun noWeek33Exists() { assertTrue(searchProgram("W33",ProgramUiState(initialized=true)).isEmpty()) }
}
