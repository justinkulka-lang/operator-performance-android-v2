package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import org.junit.Assert.*
import org.junit.Test

class V73AuthorityMigrationTest {
    private fun old(week: Int, day: Int) = ProgramUiState(initialized=true,currentWeek=week,currentDay=day,
        authority=ProgramAuthority.V72,v72ActivationProven=true)
    @Test fun freshInstallAndPositiveDay1ActivateWithoutInventingCompletion() {
        val fresh = V73AuthorityMigration.apply(ProgramUiState(),false,WeekStartEvidence.UNKNOWN)
        assertFalse(fresh.initialized); assertEquals(ProgramAuthority.V73,fresh.authorityForWeek(1))
        val unstarted = V73AuthorityMigration.apply(old(6,1),false,WeekStartEvidence.NOT_STARTED)
        assertEquals(6,unstarted.v73ActivationWeek); assertEquals(ProgramAuthority.V73,unstarted.authority)
        assertEquals(ProgramAuthority.V72,unstarted.authorityForWeek(5))
    }
    @Test fun activeCompleteClearedAndAmbiguousWeeksRemainHistorical() {
        for (evidence in WeekStartEvidence.entries) for (active in listOf(false,true)) {
            if (!active && evidence == WeekStartEvidence.NOT_STARTED) continue
            val state = V73AuthorityMigration.apply(old(6,1),active,evidence)
            assertEquals(ProgramAuthority.V72,state.authorityForWeek(6))
            assertEquals(7,state.v73ActivationWeek)
            assertEquals(ProgramAuthority.V73,state.authorityForWeek(7))
        }
        assertEquals(ProgramAuthority.V72,V73AuthorityMigration.apply(old(6,3),false,WeekStartEvidence.NOT_STARTED).authority)
    }
    @Test fun advancementActivatesAndHistoricalBrowsingNeverRewritesWeeks() {
        val migrated = V73AuthorityMigration.apply(old(6,7),true,WeekStartEvidence.ENGAGED)
        val next = migrated.advanceDay()
        assertEquals(7,next.currentWeek); assertEquals(1,next.currentDay); assertEquals(ProgramAuthority.V73,next.authority)
        val history = next.historicalAuthorities
        val moved = next.withCurrentPointer(6,4)
        assertEquals(history,moved.historicalAuthorities)
        assertEquals(ProgramAuthority.V72,moved.authority)
        assertEquals(ProgramAuthority.V73,moved.withCurrentPointer(7,1).authority)
    }
    @Test fun pointerMovedBackCannotReplaceLaterEngagedWeeks() {
        val migrated=V73AuthorityMigration.apply(old(3,1),false,WeekStartEvidence.ENGAGED,10)
        assertEquals(11,migrated.v73ActivationWeek)
        assertEquals(ProgramAuthority.V72,migrated.authorityForWeek(10))
        assertEquals(ProgramAuthority.V73,migrated.authorityForWeek(11))
    }
    @Test fun engagedWeek32TerminatesWithoutWeek33OrRetroactiveReplacement() {
        val end = V73AuthorityMigration.apply(old(32,7),false,WeekStartEvidence.ENGAGED)
        assertNull(end.v73ActivationWeek); assertEquals(end,end.advanceDay())
        assertEquals(ProgramAuthority.V72,end.authorityForWeek(32))
    }
    @Test fun retainedV71WeekAndPreviouslyKnownAuthoritiesPersist() {
        val state = old(4,3).copy(authority=ProgramAuthority.V71,retainedV71Week=4,
            historicalAuthorities=mapOf(2 to ProgramAuthority.V71))
        val migrated = V73AuthorityMigration.apply(state,true,WeekStartEvidence.ENGAGED)
        assertEquals(ProgramAuthority.V71,migrated.authorityForWeek(2))
        assertEquals(ProgramAuthority.V71,migrated.authorityForWeek(4))
        assertEquals(ProgramAuthority.V73,migrated.authorityForWeek(5))
    }
    @Test fun migrationAndSavedSessionRoundTripKeepCompletionSubstitutionAndTimer() {
        val prefs = MemoryPrefs(); val store = ProgramStateStore(prefs,MemoryPrefs())
        store.save(old(13,4))
        // Seed historical schema-5 data exactly as an older build would write it.
        prefs.edit {
            putBoolean("workoutActive",true); putInt("workoutSchema",5)
            putInt("workoutWeek",13);putInt("workoutDay",4);putBoolean("complete",true)
            putLong("workoutTimerEnd",123456L);putInt("engagedAbsoluteDay",88)
        }
        val migrated = store.load(); val saved=store.loadWorkout()!!
        assertEquals(ProgramAuthority.V72,saved.authority); assertTrue(saved.complete)
        assertEquals(123456L,saved.restTimerEndEpochMillis)
        assertEquals(ProgramAuthority.V72,migrated.authorityForWeek(13))
        assertEquals(14,migrated.v73ActivationWeek)
        assertEquals(migrated,ProgramStateStore(prefs,MemoryPrefs()).load())
        val session=SessionResolver.resolve(13,4,ProgramAuthority.V72)
        assertTrue(WorkoutProgress.normalize(saved,session).complete)
        store.clearWorkout(); assertEquals(migrated,store.load())
    }
    @Test fun newAuthorityRejectsLegacySessionAndMovedSlotsNeverTransferSubstitution() {
        val state=old(7,4).copy(authority=ProgramAuthority.V73,v73ActivationWeek=7,v73MigrationApplied=true)
        val saved=WorkoutProgress.start(state).copy(selectedSubstitutions=mapOf("d1_fly" to "Pec deck"))
        val normalized=WorkoutProgress.normalize(saved,SessionResolver.resolve(7,4,ProgramAuthority.V73))
        assertFalse(normalized.complete);assertTrue(normalized.selectedSubstitutions.isEmpty())
        assertThrows(IllegalArgumentException::class.java) { WorkoutProgress.normalize(saved,SessionResolver.resolve(7,4,ProgramAuthority.V72)) }
    }
    @Test fun resetIsExplicitAndClearsHistoryWhilePreservingUtilitySettings() {
        val migrated=V73AuthorityMigration.apply(old(5,3),false,WeekStartEvidence.UNKNOWN).copy(restTimerSeconds=90,utilityRestTimerEndEpochMillis=123456L)
        val reset=migrated.resetToV73Start()
        assertEquals(1,reset.currentWeek);assertEquals(ProgramAuthority.V73,reset.authority)
        assertTrue(reset.historicalAuthorities.isEmpty());assertNull(reset.retainedV71Week)
        assertEquals(90,reset.restTimerSeconds);assertEquals(123456L,reset.utilityRestTimerEndEpochMillis)
    }
    @Test fun completedNewWeekKeepsItsAuthorityAcrossRestartAndPointerChanges() {
        val prefs=MemoryPrefs();val store=ProgramStateStore(prefs,MemoryPrefs())
        store.save(ProgramUiState().startV73().withCurrentPointer(17,4))
        val state=store.load();store.saveWorkout(WorkoutProgress.markSessionComplete(WorkoutProgress.start(state)))
        val restart=ProgramStateStore(prefs,MemoryPrefs())
        assertEquals(ProgramAuthority.V73,restart.loadWorkout()!!.authority)
        assertTrue(restart.loadWorkout()!!.complete)
        assertEquals(ProgramAuthority.V73,restart.load().historicalAuthorities[17])
    }
}
