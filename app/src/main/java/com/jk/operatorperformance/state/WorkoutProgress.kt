package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRowKind

object WorkoutProgress {
    fun start(state: ProgramUiState): ActiveWorkoutState = ActiveWorkoutState(
        week = state.currentWeek,
        day = state.currentDay,
        speedVariant = state.speedVariant,
        engineVariant = state.engineVariant,
        deloadMode = state.deloadMode,
        machineInventoryConfigured = state.machineInventoryConfigured,
        availableMachineModalities = state.availableMachineModalities,
    )

    fun normalize(progress: ActiveWorkoutState, session: SessionReference): ActiveWorkoutState {
        require(session.rows.isNotEmpty()) { "Workout session must contain at least one row." }
        val validSubstitutions = progress.selectedSubstitutions.filter { (rowId, choice) ->
            session.rows.any { row -> row.id == rowId && choice in row.alternatives }
        }
        if (progress.complete) return progress.copy(
            movementIndex = session.rows.lastIndex,
            effortIndex = session.rows.last().effortCount,
            selectedSubstitutions = validSubstitutions,
            restTimerEndEpochMillis = null,
        )
        val movement = progress.movementIndex.coerceIn(0, session.rows.lastIndex)
        val effort = progress.effortIndex.coerceIn(1, session.rows[movement].effortCount)
        return progress.copy(
            movementIndex = movement,
            effortIndex = effort,
            selectedSubstitutions = validSubstitutions,
        )
    }

    fun advance(
        progress: ActiveWorkoutState,
        session: SessionReference,
        timerDurationSeconds: Int? = null,
        nowMillis: Long = System.currentTimeMillis(),
    ): ActiveWorkoutState {
        require(timerDurationSeconds == null || timerDurationSeconds > 0) {
            "timerDurationSeconds must be positive when configured."
        }
        val current = normalize(progress, session)
        if (current.complete) return current

        val row = session.rows[current.movementIndex]
        val previousMovement = current.movementIndex
        val previousEffort = current.effortIndex
        val previousComplete = current.complete
        val previousRestTimerEnd = current.restTimerEndEpochMillis

        val advanced = when {
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

        val autoRestEnd = if (
            !advanced.complete &&
            row.kind == SessionRowKind.RESISTANCE &&
            timerDurationSeconds != null
        ) {
            nowMillis + timerDurationSeconds.toLong() * 1_000L
        } else {
            null
        }

        return advanced.copy(
            restTimerEndEpochMillis = autoRestEnd,
            undoRestTimerEndEpochMillis = previousRestTimerEnd,
        )
    }

    fun undo(progress: ActiveWorkoutState): ActiveWorkoutState {
        if (!progress.canUndo) return progress
        return progress.copy(
            movementIndex = requireNotNull(progress.undoMovementIndex),
            effortIndex = requireNotNull(progress.undoEffortIndex),
            complete = requireNotNull(progress.undoComplete),
            restTimerEndEpochMillis = progress.undoRestTimerEndEpochMillis,
            undoMovementIndex = null,
            undoEffortIndex = null,
            undoComplete = null,
            undoRestTimerEndEpochMillis = null,
        )
    }

    fun skipRest(progress: ActiveWorkoutState): ActiveWorkoutState =
        progress.copy(restTimerEndEpochMillis = null)

    fun clearExpiredRest(progress: ActiveWorkoutState, nowMillis: Long): ActiveWorkoutState {
        val end = progress.restTimerEndEpochMillis ?: return progress
        return if (end <= nowMillis) progress.copy(restTimerEndEpochMillis = null) else progress
    }

    fun remainingRestSeconds(progress: ActiveWorkoutState, nowMillis: Long): Int {
        val end = progress.restTimerEndEpochMillis ?: return 0
        val remainingMillis = (end - nowMillis).coerceAtLeast(0L)
        return ((remainingMillis + 999L) / 1_000L).toInt()
    }

    fun selectSubstitution(
        progress: ActiveWorkoutState,
        session: SessionReference,
        rowId: String,
        alternative: String?,
    ): ActiveWorkoutState {
        val row = session.rows.singleOrNull { it.id == rowId }
            ?: error("Unknown workout row: $rowId")
        if (alternative != null) {
            require(alternative in row.alternatives) {
                "Substitution is not authorized for row $rowId: $alternative"
            }
        }
        val next = progress.selectedSubstitutions.toMutableMap()
        if (alternative == null) next.remove(rowId) else next[rowId] = alternative
        return progress.copy(selectedSubstitutions = next.toMap())
    }
}
