package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.ProgramAuthority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Week authority recorded at engagement must survive a later save of an older in-memory state.
 * Today must not apply a saved session or pointer that names a different authority.
 */
class WeekAuthorityHistoryTest {
    @Test fun recordedWeekAuthoritySurvivesLaterSaveThatOmitsIt() {
        val (prefs, store) = store()
        val frozen = ProgramUiState(
            initialized = true,
            currentWeek = 12,
            currentDay = 4,
            authority = ProgramAuthority.V72,
            v72ActivationProven = true,
            v73MigrationApplied = true,
            v73ActivationWeek = 10,
            historicalAuthorities = mapOf(12 to ProgramAuthority.V72),
        )
        store.save(frozen, replaceHistoricalAuthorities = true)
        val stale = frozen.copy(historicalAuthorities = emptyMap(), restTimerSeconds = 90)
        store.save(stale)

        val loaded = store.load()
        assertEquals(ProgramAuthority.V72, loaded.historicalAuthorities[12])
        assertEquals(ProgramAuthority.V72, loaded.authorityForWeek(12))
        assertEquals(90, loaded.restTimerSeconds)
        assertEquals(ProgramAuthority.V73, loaded.authorityForWeek(10))
    }

    @Test fun engagementThenStaleAdvanceKeepsTheWeekJustRecorded() {
        val (_, store) = store()
        val start = ProgramUiState().startV73().withCurrentPointer(17, 7)
        store.save(start, replaceHistoricalAuthorities = true)
        val memory = store.load()
        assertTrue(17 !in memory.historicalAuthorities)

        val engaged = store.markSessionEngaged(17, 7)
        assertEquals(ProgramAuthority.V73, engaged.historicalAuthorities[17])
        store.save(memory.advanceDay())

        val loaded = store.load()
        assertEquals(18, loaded.currentWeek)
        assertEquals(1, loaded.currentDay)
        assertEquals(ProgramAuthority.V73, loaded.historicalAuthorities[17])
        assertEquals(ProgramAuthority.V73, loaded.authorityForWeek(17))
        assertEquals(ProgramAuthority.V73, loaded.authority)
    }

    @Test fun explicitResetReplacesHistoricalAuthorities() {
        val (_, store) = store()
        store.save(
            ProgramUiState(
                initialized = true,
                currentWeek = 12,
                currentDay = 4,
                authority = ProgramAuthority.V72,
                v72ActivationProven = true,
                v73MigrationApplied = true,
                v73ActivationWeek = 10,
                historicalAuthorities = mapOf(12 to ProgramAuthority.V72),
                restTimerSeconds = 90,
                utilityRestTimerEndEpochMillis = 123456L,
            ),
            replaceHistoricalAuthorities = true,
        )
        store.save(store.load().resetToV73Start(), replaceHistoricalAuthorities = true)
        val reset = store.load()
        assertTrue(reset.historicalAuthorities.isEmpty())
        assertEquals(1, reset.currentWeek)
        assertEquals(ProgramAuthority.V73, reset.authorityForWeek(12))
        assertEquals(90, reset.restTimerSeconds)
        assertEquals(123456L, reset.utilityRestTimerEndEpochMillis)
    }

    @Test fun todayGuardDetectsSavedSessionAndPointerMismatchesOnly() {
        val week = ProgramUiState().startV73().withCurrentPointer(17, 2)
        val matching = WorkoutProgress.start(week)
        assertNull(todayAuthorityMismatch(week, matching))
        assertNull(todayAuthorityMismatch(week, null))

        val saved = todayAuthorityMismatch(week, matching.copy(authority = ProgramAuthority.V72))
        assertEquals(ProgramAuthority.V73, saved?.weekAuthority)
        assertEquals(ProgramAuthority.V72, saved?.savedAuthority)
        assertNull(saved?.pointerAuthority)

        val otherDay = todayAuthorityMismatch(week, matching.copy(day = 3, authority = ProgramAuthority.V72))
        assertNull(otherDay)

        val pointer = week.copy(authority = ProgramAuthority.V72)
        val pointerMismatch = todayAuthorityMismatch(pointer, null)
        assertEquals(ProgramAuthority.V72, pointerMismatch?.pointerAuthority)
        assertEquals(ProgramAuthority.V73, pointerMismatch?.weekAuthority)
        assertNull(pointerMismatch?.savedAuthority)
    }

    private fun store(): Pair<MemoryPrefs, ProgramStateStore> {
        val prefs = MemoryPrefs()
        return prefs to ProgramStateStore(prefs, MemoryPrefs())
    }
}
