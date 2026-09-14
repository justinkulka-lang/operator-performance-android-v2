package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.MachineModality
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutProgressTest {
    private val session = SessionReference(
        week = 2,
        day = 2,
        title = "Upper A",
        rows = listOf(
            SessionRow(
                id = "a",
                title = "Bench",
                prescription = "2 × 5 • REST 1:30",
                kind = SessionRowKind.RESISTANCE,
                alternatives = listOf("Dumbbell bench"),
                effortCount = 2,
                restSeconds = 90,
            ),
            SessionRow(
                id = "b",
                title = "Row",
                prescription = "1 × 8 • REST 1:30",
                kind = SessionRowKind.RESISTANCE,
                effortCount = 1,
                restSeconds = 90,
            ),
        ),
        deload = false,
    )

    @Test
    fun startSnapshotsOnlyAuthorizedReferenceStateIncludingEquipmentInventory() {
        val progress = WorkoutProgress.start(
            ProgramUiState(
                currentWeek = 2,
                currentDay = 2,
                deloadMode = true,
                restTimerSeconds = 90,
                machineInventoryConfigured = true,
                availableMachineModalities = setOf(MachineModality.ROW_ERG),
            ),
        )
        assertEquals(2, progress.week)
        assertEquals(2, progress.day)
        assertTrue(progress.deloadMode)
        assertTrue(progress.machineInventoryConfigured)
        assertEquals(setOf(MachineModality.ROW_ERG), progress.availableMachineModalities)
        assertEquals(0, progress.movementIndex)
        assertEquals(1, progress.effortIndex)
        assertTrue(progress.completedEffortIds.isEmpty())
        assertFalse(progress.complete)
        assertNull(progress.restTimerEndEpochMillis)
    }

    @Test
    fun setDoneMarksCompletionAndAdvancesSetThenMovementThenCompletes() {
        var progress = ActiveWorkoutState(week = 2, day = 2, deloadMode = false)
        progress = WorkoutProgress.advance(progress, session, nowMillis = 10_000L)
        assertEquals(setOf("a#1"), progress.completedEffortIds)
        assertEquals(0, progress.movementIndex)
        assertEquals(2, progress.effortIndex)

        progress = WorkoutProgress.skipRest(progress)
        progress = WorkoutProgress.advance(progress, session, nowMillis = 20_000L)
        assertEquals(setOf("a#1", "a#2"), progress.completedEffortIds)
        assertEquals(1, progress.movementIndex)
        assertEquals(1, progress.effortIndex)

        progress = WorkoutProgress.advance(progress, session, nowMillis = 30_000L)
        assertTrue(progress.complete)
        assertEquals(3, WorkoutProgress.completedEfforts(progress, session))
    }

    @Test
    fun prescribedRowRestStartsAfterNonFinalSetAndUndoRestoresCompletionAndTimer() {
        val start = ActiveWorkoutState(week = 2, day = 2, deloadMode = false)
        val advanced = WorkoutProgress.advance(start, session, nowMillis = 10_000L)
        assertEquals(100_000L, advanced.restTimerEndEpochMillis)
        assertEquals(90, WorkoutProgress.remainingRestSeconds(advanced, 10_000L))
        assertTrue("a#1" in advanced.completedEffortIds)

        val extended = WorkoutProgress.addRestSeconds(advanced, 30)
        assertEquals(130_000L, extended.restTimerEndEpochMillis)

        val undone = WorkoutProgress.undo(extended)
        assertEquals(0, undone.movementIndex)
        assertEquals(1, undone.effortIndex)
        assertFalse("a#1" in undone.completedEffortIds)
        assertNull(undone.restTimerEndEpochMillis)
        assertFalse(undone.canUndo)
    }

    @Test
    fun finalSetOfMovementDoesNotStartRestUnlessExplicitlyRequired() {
        val beforeFinalBenchSet = ActiveWorkoutState(
            week = 2,
            day = 2,
            deloadMode = false,
            movementIndex = 0,
            effortIndex = 2,
            completedEffortIds = setOf("a#1"),
        )
        val advanced = WorkoutProgress.advance(beforeFinalBenchSet, session, nowMillis = 10_000L)
        assertEquals(1, advanced.movementIndex)
        assertNull(advanced.restTimerEndEpochMillis)
    }

    @Test
    fun restAfterLastEffortSupportsWeekEightStyleBetweenSegmentRecovery() {
        val special = SessionReference(
            week = 8,
            day = 3,
            title = "Speed",
            rows = listOf(
                SessionRow("x", "300 m", "1 × 300 m", SessionRowKind.CONDITIONING, effortCount = 1, restSeconds = 720, restAfterLastEffort = true),
                SessionRow("y", "150 m", "1 × 150 m", SessionRowKind.CONDITIONING, effortCount = 1),
            ),
            deload = false,
        )
        val advanced = WorkoutProgress.advance(ActiveWorkoutState(8, 3, false), special, nowMillis = 1_000L)
        assertEquals(721_000L, advanced.restTimerEndEpochMillis)
        assertEquals(1, advanced.movementIndex)
    }

    @Test
    fun todayStyleToggleCanMarkAndUnmarkAnIndividualSet() {
        val start = ActiveWorkoutState(week = 2, day = 2, deloadMode = false)
        val checked = WorkoutProgress.toggleEffort(start, session, "a", 1, nowMillis = 10_000L)
        assertTrue(WorkoutProgress.isEffortComplete(checked, "a", 1))
        assertEquals(2, checked.effortIndex)
        val unchecked = WorkoutProgress.toggleEffort(checked, session, "a", 1, nowMillis = 20_000L)
        assertFalse(WorkoutProgress.isEffortComplete(unchecked, "a", 1))
        assertEquals(1, unchecked.effortIndex)
    }

    @Test
    fun substitutionIsSessionScopedAndMustBeAuthorizedForCanonicalRow() {
        val start = ActiveWorkoutState(week = 2, day = 2, deloadMode = false)
        val substituted = WorkoutProgress.selectSubstitution(start, session, "a", "Dumbbell bench")
        assertEquals("Dumbbell bench", substituted.selectedSubstitutions["a"])
        val canonical = WorkoutProgress.selectSubstitution(substituted, session, "a", null)
        assertFalse(canonical.selectedSubstitutions.containsKey("a"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unlistedSubstitutionIsRejected() {
        WorkoutProgress.selectSubstitution(
            ActiveWorkoutState(week = 2, day = 2, deloadMode = false),
            session,
            "a",
            "Invented movement",
        )
    }
}
