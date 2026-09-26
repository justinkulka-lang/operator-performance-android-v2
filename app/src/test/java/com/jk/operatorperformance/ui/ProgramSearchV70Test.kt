package com.jk.operatorperformance.ui

import com.jk.operatorperformance.state.ProgramUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramSearchV70Test {
    @Test fun cycleAndDayTokensResolve() {
        val hits=searchProgram("C28 D3 final 300",ProgramUiState(initialized=true))
        assertEquals(1,hits.size)
        assertEquals(28,hits.single().session.cycle)
        assertEquals(3,hits.single().session.day)
    }
    @Test fun noCycle29Exists() { assertTrue(searchProgram("C29",ProgramUiState(initialized=true)).isEmpty()) }
}
