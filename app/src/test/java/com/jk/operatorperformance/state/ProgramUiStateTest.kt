package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.SpeedVariant
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgramUiStateTest {
    @Test
    fun freshStateIsWeekOneDayOneWithApprovedReferenceDefaults() {
        val state = ProgramUiState()
        assertEquals(1, state.currentWeek)
        assertEquals(1, state.currentDay)
        assertEquals(SpeedVariant.ACCELERATION, state.speedVariant)
        assertEquals(EngineVariant.THRESHOLD, state.engineVariant)
    }

    @Test
    fun advanceDayMovesWithinAndAcrossAllPopulatedWeeks() {
        assertEquals(ProgramUiState(currentWeek = 2, currentDay = 5), ProgramUiState(currentWeek = 2, currentDay = 4).advanceDay())
        assertEquals(ProgramUiState(currentWeek = 9, currentDay = 1), ProgramUiState(currentWeek = 8, currentDay = 7).advanceDay())
        assertEquals(ProgramUiState(currentWeek = 17, currentDay = 1), ProgramUiState(currentWeek = 16, currentDay = 7).advanceDay())
        assertEquals(ProgramUiState(currentWeek = 25, currentDay = 1), ProgramUiState(currentWeek = 24, currentDay = 7).advanceDay())
    }

    @Test
    fun weekThirtyTwoDaySevenIsTerminal() {
        val state = ProgramUiState(currentWeek = 32, currentDay = 7)
        assertEquals(state, state.advanceDay())
    }

    @Test
    fun manualPointerAcceptsAnyPopulatedWeekThroughThirtyTwo() {
        val changed = ProgramUiState().withCurrentPointer(32, 6)
        assertEquals(32, changed.currentWeek)
        assertEquals(6, changed.currentDay)
    }

    @Test(expected = IllegalArgumentException::class)
    fun weekThirtyThreeIsRejected() {
        ProgramUiState().withCurrentPointer(33, 1)
    }
}
