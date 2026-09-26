package com.jk.operatorperformance.state

import org.junit.Assert.*
import org.junit.Test

class ProgramUiStateV70Test {
    @Test fun v70RequiresExplicitStart() {
        val initial=ProgramUiState()
        assertFalse(initial.initialized)
        assertEquals(ProgramUiState(initialized=true,currentCycle=1,currentDay=1),initial.startV70())
    }
    @Test fun pointerUsesEightDayCyclesAndStopsAtEnd() {
        assertEquals(ProgramUiState(initialized=true,currentCycle=2,currentDay=1),ProgramUiState(true,1,8).advanceDay())
        val end=ProgramUiState(true,28,8)
        assertEquals(end,end.advanceDay())
    }
    @Test fun invalidPointersAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { ProgramUiState(currentCycle=29) }
        assertThrows(IllegalArgumentException::class.java) { ProgramUiState(currentDay=9) }
    }
}
