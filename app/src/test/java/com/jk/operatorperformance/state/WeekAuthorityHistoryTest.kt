package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Engaged week authority must be adopted from the state [ProgramStateStore] returns.
 * Today drops a saved workout whose authority is not the authority of that week.
 */
class WeekAuthorityHistoryTest {
    @Test fun engageThenAdvanceWithReturnedStateKeepsThatWeekInHistory() {
        val (_, store) = store()
        store.save(ProgramUiState().startV73().withCurrentPointer(17, 7))
        val engaged = store.markSessionEngaged(17, 7)
        store.save(engaged.advanceDay())
        val loaded = store.load()
        assertEquals(ProgramAuthority.V73, loaded.historicalAuthorities[17])
        assertEquals(18, loaded.currentWeek)
        assertEquals(1, loaded.currentDay)
    }

    @Test fun saveWorkoutReturnsStateWhoseHistoryIncludesTheEngagedWeek() {
        val (_, store) = store()
        val state = ProgramUiState().startV73().withCurrentPointer(17, 4)
        store.save(state)
        val returned = store.saveWorkout(WorkoutProgress.start(store.load()))
        assertEquals(ProgramAuthority.V73, returned.historicalAuthorities[17])
        assertEquals(ProgramAuthority.V73, store.load().historicalAuthorities[17])
    }

    @Test fun mismatchedSavedWorkoutIsExcludedFromTodayAndClearedFromStorage() {
        val (_, store) = store()
        val state = ProgramUiState().startV73().withCurrentPointer(17, 2)
        store.save(state)
        val mismatched = WorkoutProgress.start(state).copy(authority = ProgramAuthority.V72)
        store.saveWorkout(mismatched)
        val loaded = store.load()
        val saved = store.loadWorkout()
        assertEquals(ProgramAuthority.V72, saved?.authority)
        assertNull(workoutForToday(loaded, saved))
        if (saved != null && workoutForToday(loaded, saved) == null) store.clearWorkout()
        assertNull(store.loadWorkout())
        assertThrows(IllegalArgumentException::class.java) {
            WorkoutProgress.normalize(mismatched, SessionResolver.resolve(17, 2, ProgramAuthority.V73))
        }
    }

    private fun store(): Pair<MemoryPrefs, ProgramStateStore> {
        val prefs = MemoryPrefs()
        return prefs to ProgramStateStore(prefs, MemoryPrefs())
    }
}
