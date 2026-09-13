package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.program.SpeedVariant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutProgressTest {
    private val session = SessionReference(
        week = 2,
        day = 2,
        title = "Upper A",
        rows = listOf(
            SessionRow("a", "Bench", "2 × 5", SessionRowKind.RESISTANCE, effortCount = 2),
            SessionRow("b", "Row", "1 × 8", SessionRowKind.RESISTANCE, effortCount = 1),
        ),
        deload = false,
    )

    @Test
    fun startCopiesOnlyAuthorizedReferenceState() {
        val progress = WorkoutProgress.start(
            ProgramUiState(
                currentWeek = 2,
                currentDay = 2,
                speedVariant = SpeedVariant.MAX_VELOCITY,
                engineVariant = EngineVariant.VO2,
                deloadMode = true,
            ),
        )
        assertEquals(2, progress.week)
        assertEquals(2, progress.day)
        assertEquals(0, progress.movementIndex)
        assertEquals(1, progress.effortIndex)
        assertFalse(progress.complete)
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
