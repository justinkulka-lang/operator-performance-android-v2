package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.SessionReference

enum class SessionStatus {
    NOT_STARTED,
    ACTIVE,
    RESUMABLE,
    COMPLETED,
}

/**
 * Session status only. Set and effort taps are not stored.
 * Authorized local fields are the pointer, substitutions, and a manual rest timer.
 */
object WorkoutProgress {
    fun start(state: ProgramUiState): ActiveWorkoutState = ActiveWorkoutState(
        week = state.currentWeek,
        day = state.currentDay,
    )

    fun status(workout: ActiveWorkoutState?, showingWorkout: Boolean): SessionStatus = when {
        workout == null -> SessionStatus.NOT_STARTED
        workout.complete -> SessionStatus.COMPLETED
        showingWorkout -> SessionStatus.ACTIVE
        else -> SessionStatus.RESUMABLE
    }

    fun normalize(progress: ActiveWorkoutState, session: SessionReference): ActiveWorkoutState {
        require(session.rows.isNotEmpty()) { "Workout session must contain at least one row." }
        val validSubstitutions = progress.selectedSubstitutions.filter { (rowId, choice) ->
            session.rows.any { row -> row.id == rowId && choice in row.alternatives }
        }
        return progress.copy(
            movementIndex = 0,
            effortIndex = 1,
            selectedSubstitutions = validSubstitutions,
            completedEffortIds = emptySet(),
            undoMovementIndex = null,
            undoEffortIndex = null,
            undoComplete = null,
            undoRestTimerEndEpochMillis = null,
            undoCompletedEffortIds = null,
        )
    }

    fun markSessionComplete(progress: ActiveWorkoutState): ActiveWorkoutState =
        progress.copy(complete = true, completedEffortIds = emptySet(), restTimerEndEpochMillis = null)

    fun selectSubstitution(
        progress: ActiveWorkoutState,
        session: SessionReference,
        rowId: String,
        alternative: String?,
    ): ActiveWorkoutState {
        val row = session.rows.singleOrNull { it.id == rowId } ?: error("Unknown workout row: $rowId")
        if (alternative != null) {
            require(alternative in row.alternatives) { "Substitution is not authorized for row $rowId: $alternative" }
        }
        val next = progress.selectedSubstitutions.toMutableMap()
        if (alternative == null) next.remove(rowId) else next[rowId] = alternative
        return progress.copy(selectedSubstitutions = next.toMap(), completedEffortIds = emptySet())
    }

    fun startManualRest(progress: ActiveWorkoutState, seconds: Int, nowMillis: Long): ActiveWorkoutState {
        require(seconds in 30..600)
        return progress.copy(
            restTimerEndEpochMillis = nowMillis + seconds.toLong() * 1_000L,
            completedEffortIds = emptySet(),
        )
    }

    fun skipRest(progress: ActiveWorkoutState): ActiveWorkoutState =
        progress.copy(restTimerEndEpochMillis = null, completedEffortIds = emptySet())

    fun addRestSeconds(progress: ActiveWorkoutState, seconds: Int): ActiveWorkoutState {
        require(seconds > 0)
        val end = progress.restTimerEndEpochMillis ?: return progress
        return progress.copy(restTimerEndEpochMillis = end + seconds.toLong() * 1_000L, completedEffortIds = emptySet())
    }

    fun clearExpiredRest(progress: ActiveWorkoutState, nowMillis: Long): ActiveWorkoutState {
        val end = progress.restTimerEndEpochMillis ?: return progress
        return if (end <= nowMillis) progress.copy(restTimerEndEpochMillis = null) else progress
    }

    fun remainingRestSeconds(progress: ActiveWorkoutState, nowMillis: Long): Int {
        val end = progress.restTimerEndEpochMillis ?: return 0
        val remainingMillis = (end - nowMillis).coerceAtLeast(0L)
        return ((remainingMillis + 999L) / 1_000L).toInt()
    }
}
