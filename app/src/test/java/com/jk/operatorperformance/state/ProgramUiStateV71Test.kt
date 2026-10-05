package com.jk.operatorperformance.state

import org.junit.Assert.*
import org.junit.Test

class ProgramUiStateV71Test {
    @Test fun v71RequiresExplicitStart() {
        val initial=ProgramUiState()
        assertFalse(initial.initialized)
        assertEquals(ProgramUiState(initialized=true,currentWeek=2,currentDay=3),initial.startV71())
    }
    @Test fun pointerUsesSevenDayWeeksAndStopsAtEnd() {
        assertEquals(ProgramUiState(initialized=true,currentWeek=2,currentDay=1),ProgramUiState(true,1,7).advanceDay())
        val end=ProgramUiState(true,32,7)
        assertEquals(end,end.advanceDay())
    }
    @Test fun invalidPointersAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { ProgramUiState(currentWeek=33) }
        assertThrows(IllegalArgumentException::class.java) { ProgramUiState(currentDay=8) }
    }
}
