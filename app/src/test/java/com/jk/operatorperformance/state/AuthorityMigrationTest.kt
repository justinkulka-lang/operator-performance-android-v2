package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.ProgramAuthority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthorityMigrationTest {
    @Test fun freshInstallDoesNotUseWeek2Day3() {
        val migrated = AuthorityMigration.apply(ProgramUiState(), workoutActive = false)
        assertEquals(false, migrated.initialized)
        assertEquals(1, migrated.currentWeek)
        assertEquals(1, migrated.currentDay)
        assertEquals(ProgramAuthority.V72, migrated.authority)
        assertNull(migrated.retainedV71Week)
    }

    @Test fun day1WithoutSessionEvidenceRetainsV71() {
        val migrated = AuthorityMigration.apply(
            ProgramUiState(initialized = true, currentWeek = 5, currentDay = 1),
            workoutActive = false,
            evidence = WeekStartEvidence.UNKNOWN,
        )
        assertEquals(5, migrated.currentWeek)
        assertEquals(1, migrated.currentDay)
        assertEquals(ProgramAuthority.V71, migrated.authority)
        assertEquals(5, migrated.retainedV71Week)
    }

    @Test fun resetAfterCompletionOnDay1RetainsV71() {
        val migrated = AuthorityMigration.apply(
            ProgramUiState(initialized = true, currentWeek = 5, currentDay = 1),
            workoutActive = false,
            evidence = WeekStartEvidence.ENGAGED,
        )
        assertEquals(ProgramAuthority.V71, migrated.authority)
        assertEquals(5, migrated.retainedV71Week)
    }

    @Test fun provenUnstartedDay1ActivatesV72() {
        val migrated = AuthorityMigration.apply(
            ProgramUiState(initialized = true, currentWeek = 5, currentDay = 1),
            workoutActive = false,
            evidence = WeekStartEvidence.NOT_STARTED,
        )
        assertEquals(5, migrated.currentWeek)
        assertEquals(1, migrated.currentDay)
        assertEquals(ProgramAuthority.V72, migrated.authority)
        assertNull(migrated.retainedV71Week)
    }

    @Test fun laterDayRetainsEvenWhenEvidenceSaysNotStarted() {
        val migrated = AuthorityMigration.apply(
            ProgramUiState(initialized = true, currentWeek = 5, currentDay = 2),
            workoutActive = false,
            evidence = WeekStartEvidence.NOT_STARTED,
        )
        assertEquals(ProgramAuthority.V71, migrated.authority)
        assertEquals(5, migrated.retainedV71Week)
    }

    @Test fun sessionEvidenceDoesNotTreatAMissingRecordAsUnstarted() {
        assertEquals(WeekStartEvidence.UNKNOWN, AuthorityMigration.evidenceFor(recordPresent = false, engagedAbsoluteDay = 0, week = 1))
        assertEquals(WeekStartEvidence.ENGAGED, AuthorityMigration.evidenceFor(recordPresent = true, engagedAbsoluteDay = 1, week = 1))
        assertEquals(WeekStartEvidence.NOT_STARTED, AuthorityMigration.evidenceFor(recordPresent = true, engagedAbsoluteDay = 7, week = 2))
        assertEquals(WeekStartEvidence.ENGAGED, AuthorityMigration.evidenceFor(recordPresent = true, engagedAbsoluteDay = 8, week = 2))
    }

    @Test fun activeMidweekRetainsV71UntilTheNextDay1() {
        val migrated = AuthorityMigration.apply(ProgramUiState(initialized = true, currentWeek = 2, currentDay = 3), workoutActive = false)
        assertEquals(ProgramAuthority.V71, migrated.authority)
        assertEquals(2, migrated.retainedV71Week)
        val finished = migrated.copy(currentDay = 7).advanceDay()
        assertEquals(3, finished.currentWeek)
        assertEquals(1, finished.currentDay)
        assertEquals(ProgramAuthority.V72, finished.authority)
        assertNull(finished.retainedV71Week)
        assertEquals(ProgramAuthority.V72, finished.authorityForWeek(2))
        assertEquals(ProgramAuthority.V72, finished.authorityForWeek(3))
    }

    @Test fun activeWorkoutRetainsTheWeekEvenOnDay1() {
        val migrated = AuthorityMigration.apply(ProgramUiState(initialized = true, currentWeek = 4, currentDay = 1), workoutActive = true)
        assertEquals(4, migrated.retainedV71Week)
        assertEquals(ProgramAuthority.V71, migrated.authorityForWeek(4))
        assertEquals(ProgramAuthority.V72, migrated.authorityForWeek(5))
    }

    @Test fun week32DoesNotCreateWeek33() {
        val end = AuthorityMigration.apply(ProgramUiState(initialized = true, currentWeek = 32, currentDay = 7), workoutActive = false)
        assertEquals(end, end.advanceDay())
        assertEquals(32, end.currentWeek)
        assertEquals(ProgramAuthority.V71, end.authorityForWeek(32))
    }

    @Test fun migrationIsIdempotent() {
        val once = AuthorityMigration.apply(ProgramUiState(initialized = true, currentWeek = 9, currentDay = 6), workoutActive = true)
        val twice = AuthorityMigration.apply(once, workoutActive = true)
        assertEquals(once.retainedV71Week, twice.retainedV71Week)
        assertEquals(once.authority, twice.authority)
    }

    @Test fun explicitStartIsWeek1Day1() {
        val started = ProgramUiState().startV72()
        assertEquals(ProgramUiState(initialized = true, currentWeek = 1, currentDay = 1, authority = ProgramAuthority.V72), started)
    }

    @Test fun repeatedApplyIsStableForEveryEvidenceCase() {
        val cases = listOf(
            Triple(ProgramUiState(), false, WeekStartEvidence.UNKNOWN),
            Triple(ProgramUiState(initialized = true, currentWeek = 5, currentDay = 1), false, WeekStartEvidence.UNKNOWN),
            Triple(ProgramUiState(initialized = true, currentWeek = 5, currentDay = 1), false, WeekStartEvidence.NOT_STARTED),
            Triple(ProgramUiState(initialized = true, currentWeek = 5, currentDay = 1), false, WeekStartEvidence.ENGAGED),
            Triple(ProgramUiState(initialized = true, currentWeek = 5, currentDay = 1), true, WeekStartEvidence.NOT_STARTED),
            Triple(ProgramUiState(initialized = true, currentWeek = 6, currentDay = 4), false, WeekStartEvidence.UNKNOWN),
            Triple(ProgramUiState(initialized = true, currentWeek = 32, currentDay = 7), false, WeekStartEvidence.ENGAGED),
        )
        cases.forEach { (state, active, evidence) ->
            val once = AuthorityMigration.apply(state, active, evidence)
            val twice = AuthorityMigration.apply(once, active, evidence)
            assertEquals(once, twice)
        }
    }

    @Test fun engagedDay1DoesNotBecomeV72OnTheSameDay() {
        val retained = AuthorityMigration.apply(
            ProgramUiState(initialized = true, currentWeek = 8, currentDay = 1),
            workoutActive = false,
            evidence = WeekStartEvidence.ENGAGED,
        )
        assertEquals(ProgramAuthority.V71, retained.authority)
        assertEquals(1, retained.currentDay)
        assertEquals(8, retained.retainedV71Week)
        val nextWeek = retained.copy(currentDay = 7).advanceDay()
        assertEquals(9, nextWeek.currentWeek)
        assertEquals(1, nextWeek.currentDay)
        assertEquals(ProgramAuthority.V72, nextWeek.authority)
        assertNull(nextWeek.retainedV71Week)
    }

    @Test fun v72Week32DoesNotCreateWeek33() {
        val end = ProgramUiState(initialized = true, currentWeek = 32, currentDay = 7, authority = ProgramAuthority.V72)
        assertEquals(end, end.advanceDay())
    }
}
