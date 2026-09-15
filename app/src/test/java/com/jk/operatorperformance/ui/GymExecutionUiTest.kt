package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SpeedVariant
import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.WorkoutProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GymExecutionUiTest {
    @Test
    fun weekOneSpeedSurfacesFreshnessAndBaselineExecutionGuidance() {
        val session = SessionResolver.resolve(week = 1, day = 3, deload = false)
        val guidance = sessionExecutionGuidance(session)

        assertEquals(2, guidance.size)
        assertTrue(guidance[0].contains("performed fresh"))
        assertTrue(guidance[0].contains("Do not perform heavy resistance work"))
        assertTrue(guidance[1].contains("one timed 300 m"))
        assertTrue(guidance[1].contains("Do not perform the machine-power finisher"))
    }

    @Test
    fun foundationReferenceLabelsOnlyAppearOnWeeksTwoThroughEightSpeedAndEngine() {
        val state = ProgramUiState(
            currentWeek = 4,
            currentDay = 3,
            speedVariant = SpeedVariant.MAX_VELOCITY,
            engineVariant = EngineVariant.VO2,
        )
        val speed = SessionResolver.resolve(4, 3, false, speedVariant = state.speedVariant, engineVariant = state.engineVariant)
        val engine = SessionResolver.resolve(4, 6, false, speedVariant = state.speedVariant, engineVariant = state.engineVariant)
        val lower = SessionResolver.resolve(4, 1, false, speedVariant = state.speedVariant, engineVariant = state.engineVariant)
        val laterSpeed = SessionResolver.resolve(9, 3, false, speedVariant = state.speedVariant, engineVariant = state.engineVariant)

        assertEquals("FOUNDATION SPEED • ${SpeedVariant.MAX_VELOCITY.label} • MANUAL REFERENCE", foundationReferenceLabel(speed, state))
        assertEquals("FOUNDATION ENGINE • ${EngineVariant.VO2.label} • MANUAL REFERENCE", foundationReferenceLabel(engine, state))
        assertNull(foundationReferenceLabel(lower, state))
        assertNull(foundationReferenceLabel(laterSpeed, state))
    }

    @Test
    fun workoutMapDistinguishesCompleteCurrentAndUpcomingRows() {
        val session = SessionResolver.resolve(week = 1, day = 1, deload = false)
        val first = session.rows.first()
        val completedFirst = (1..first.effortCount).mapTo(linkedSetOf()) { WorkoutProgress.effortId(first.id, it) }
        val progress = ActiveWorkoutState(
            week = 1,
            day = 1,
            deloadMode = false,
            movementIndex = 1,
            effortIndex = 1,
            completedEffortIds = completedFirst,
        )

        assertEquals(WorkoutMapVisualState.COMPLETE, workoutMapState(progress, session, 0))
        assertEquals(WorkoutMapVisualState.CURRENT, workoutMapState(progress, session, 1))
        assertEquals(WorkoutMapVisualState.UPCOMING, workoutMapState(progress, session, 2))
    }

    @Test
    fun nextMovementPreviewSkipsCompletedRowsAndRespectsSelectedAlternative() {
        val session = SessionResolver.resolve(week = 1, day = 1, deload = false)
        val current = session.rows[0]
        val next = session.rows[1]
        val alternative = next.alternatives.first()
        val progress = ActiveWorkoutState(
            week = 1,
            day = 1,
            deloadMode = false,
            movementIndex = 0,
            effortIndex = 1,
            selectedSubstitutions = mapOf(next.id to alternative),
        )

        assertEquals(alternative, nextIncompleteMovementTitle(progress, session))
        assertTrue(!rowIsComplete(progress, current))
    }
}
