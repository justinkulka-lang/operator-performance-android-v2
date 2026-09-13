package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.MachineModality
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.program.SpeedVariant
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
                "a",
                "Bench",
                "2 × 5",
                SessionRowKind.RESISTANCE,
                alternatives = listOf("Dumbbell bench"),
                effortCount = 2,
            ),
            SessionRow("b", "Row", "1 × 8", SessionRowKind.RESISTANCE, effortCount = 1),
        ),
        deload = false,
    )

    @Test
    fun startSnapshotsOnlyAuthorizedReferenceStateIncludingEquipmentInventory() {
        val progress = WorkoutProgress.start(
            ProgramUiState(
                currentWeek = 2,
                currentDay = 2,
                speedVariant = SpeedVariant.MAX_VELOCITY,
                engineVariant = EngineVariant.VO2,
                deloadMode = true,
                restTimerSeconds = 90,
                machineInventoryConfigured = true,
                availableMachineModalities = setOf(MachineModality.ROW_ERG),
            ),
        )
        assertEquals(2, progress.week)
        assertEquals(2, progress.day)
        assertTrue(progress.machineInventoryConfigured)
        assertEquals(setOf(MachineModality.ROW_ERG), progress.availableMachineModalities)
        assertEquals(0, progress.movementIndex)
        assertEquals(1, progress.effortIndex)
        assertFalse(progress.complete)
        assertNull(progress.restTimerEndEpochMillis)
    }

    @Test
    fun setDoneAdvancesSetThenMovementThenCompletes() {
        var progress = ActiveWorkoutState(2, 2, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, false)
        progress = WorkoutProgress.advance(progress, session)
        assertEquals(0, progress.movementIndex)
        assertEquals(2, progress.effortIndex)

        progress = WorkoutProgress.advance(progress, session)
        assertEquals(1, progress.movementIndex)
        assertEquals(1, progress.effortIndex)

        progress = WorkoutProgress.advance(progress, session)
        assertTrue(progress.complete)
    }

    @Test
    fun configuredRestStartsAfterNonFinalResistanceSetAndUndoRestoresTimerState() {
        val start = ActiveWorkoutState(2, 2, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, false)
        val advanced = WorkoutProgress.advance(
            progress = start,
            session = session,
            timerDurationSeconds = 90,
            nowMillis = 10_000L,
        )
        assertEquals(100_000L, advanced.restTimerEndEpochMillis)
        assertEquals(90, WorkoutProgress.remainingRestSeconds(advanced, 10_000L))

        val undone = WorkoutProgress.undo(advanced)
        assertEquals(0, undone.movementIndex)
        assertEquals(1, undone.effortIndex)
        assertNull(undone.restTimerEndEpochMillis)
        assertFalse(undone.canUndo)
    }

    @Test
    fun finalSetNeverStartsUnnecessaryRestCountdown() {
        val finalSet = ActiveWorkoutState(
            2,
            2,
            SpeedVariant.ACCELERATION,
            EngineVariant.THRESHOLD,
            false,
            movementIndex = 1,
            effortIndex = 1,
        )
        val complete = WorkoutProgress.advance(
            progress = finalSet,
            session = session,
            timerDurationSeconds = 90,
            nowMillis = 10_000L,
        )
        assertTrue(complete.complete)
        assertNull(complete.restTimerEndEpochMillis)
    }

    @Test
    fun substitutionIsSessionScopedAndMustBeAuthorizedForCanonicalRow() {
        val start = ActiveWorkoutState(2, 2, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, false)
        val substituted = WorkoutProgress.selectSubstitution(
            progress = start,
            session = session,
            rowId = "a",
            alternative = "Dumbbell bench",
        )
        assertEquals("Dumbbell bench", substituted.selectedSubstitutions["a"])

        val canonical = WorkoutProgress.selectSubstitution(substituted, session, "a", null)
        assertFalse(canonical.selectedSubstitutions.containsKey("a"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unlistedSubstitutionIsRejected() {
        WorkoutProgress.selectSubstitution(
            ActiveWorkoutState(2, 2, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, false),
            session,
            "a",
            "Invented movement",
        )
    }

    @Test
    fun undoRestoresImmediatelyPreviousPointer() {
        val start = ActiveWorkoutState(2, 2, SpeedVariant.ACCELERATION, EngineVariant.THRESHOLD, false)
        val advanced = WorkoutProgress.advance(start, session)
        val undone = WorkoutProgress.undo(advanced)
        assertEquals(0, undone.movementIndex)
        assertEquals(1, undone.effortIndex)
        assertFalse(undone.complete)
        assertFalse(undone.canUndo)
    }
}
