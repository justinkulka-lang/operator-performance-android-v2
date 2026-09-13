package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.SpeedVariant
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgramUiStateTest {
    @Test
    fun freshStateIsWeekOneDayOneWithAuthorizedReferenceDefaults() {
        val state = ProgramUiState()
        assertEquals(1, state.currentWeek)
        assertEquals(1, state.currentDay)
        assertEquals(SpeedVariant.ACCELERATION, state.speedVariant)
        assertEquals(EngineVariant.THRESHOLD, state.engineVariant)
    }

    @Test
    fun advanceDayMovesWithinWeek() {
        val next = ProgramUiState(currentWeek = 2, currentDay = 4).advanceDay()
        assertEquals(2, next.currentWeek)
        assertEquals(5, next.currentDay)
    }

    @Test
    fun advancingDaySevenMovesToNextPopulatedWeek() {
        val next = ProgramUiState(currentWeek = 3, currentDay = 7).advanceDay()
        assertEquals(4, next.currentWeek)
        assertEquals(1, next.currentDay)
    }

    @Test
    fun blockOneBoundaryNeverAdvancesIntoUnpopulatedWeekNine() {
        val state = ProgramUiState(currentWeek = 8, currentDay = 7)
        assertEquals(state, state.advanceDay())
    }

    @Test
    fun manualPointerChangeAcceptsOnlyPopulatedWeeks() {
        val changed = ProgramUiState().withCurrentPointer(8, 7)
        assertEquals(8, changed.currentWeek)
        assertEquals(7, changed.currentDay)
    }
}
