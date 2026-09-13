package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.SessionReference

object WorkoutProgress {
    fun start(state: ProgramUiState): ActiveWorkoutState = ActiveWorkoutState(
        week = state.currentWeek,
        day = state.currentDay,
        speedVariant = state.speedVariant,
        engineVariant = state.engineVariant,
        deloadMode = state.deloadMode,
    )

    fun normalize(progress: ActiveWorkoutState, session: SessionReference): ActiveWorkoutState {
        require(session.rows.isNotEmpty()) { "Workout session must contain at least one row." }
        if (progress.complete) return progress.copy(
            movementIndex = session.rows.lastIndex,
            effortIndex = session.rows.last().effortCount,
        )
        val movement = progress.movementIndex.coerceIn(0, session.rows.lastIndex)
        val effort = progress.effortIndex.coerceIn(1, session.rows[movement].effortCount)
        return progress.copy(movementIndex = movement, effortIndex = effort)
    }

    fun advance(progress: ActiveWorkoutState, session: SessionReference): ActiveWorkoutState {
        val current = normalize(progress, session)
        if (current.complete) return current

        val row = session.rows[current.movementIndex]
        val previousMovement = current.movementIndex
        val previousEffort = current.effortIndex
        val previousComplete = current.complete

        return when {
            current.effortIndex < row.effortCount -> current.copy(
                effortIndex = current.effortIndex + 1,
                undoMovementIndex = previousMovement,
                undoEffortIndex = previousEffort,
                undoComplete = previousComplete,
            )
            current.movementIndex < session.rows.lastIndex -> current.copy(
                movementIndex = current.movementIndex + 1,
                effortIndex = 1,
                undoMovementIndex = previousMovement,
                undoEffortIndex = previousEffort,
                undoComplete = previousComplete,
            )
            else -> current.copy(
                complete = true,
                undoMovementIndex = previousMovement,
                undoEffortIndex = previousEffort,
                undoComplete = previousComplete,
            )
        }
    }

    fun undo(progress: ActiveWorkoutState): ActiveWorkoutState {
        if (!progress.canUndo) return progress
        return progress.copy(
            movementIndex = requireNotNull(progress.undoMovementIndex),
            effortIndex = requireNotNull(progress.undoEffortIndex),
            complete = requireNotNull(progress.undoComplete),
            undoMovementIndex = null,
            undoEffortIndex = null,
            undoComplete = null,
        )
    }
}
