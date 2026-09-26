package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.SessionReference

object WorkoutProgress {
    fun start(state: ProgramUiState): ActiveWorkoutState = ActiveWorkoutState(
        cycle = state.currentCycle,
        day = state.currentDay,
    )

    fun effortId(rowId: String, effortIndex: Int): String = "$rowId#$effortIndex"

    fun totalEfforts(session: SessionReference): Int =
        session.rows.filterNot { it.optional }.sumOf { it.effortCount }

    fun completedEfforts(progress: ActiveWorkoutState, session: SessionReference): Int {
        val required = requiredEffortIds(session)
        return progress.completedEffortIds.count { it in required }
    }

    fun isEffortComplete(progress: ActiveWorkoutState, rowId: String, effortIndex: Int): Boolean =
        effortId(rowId, effortIndex) in progress.completedEffortIds

    fun normalize(progress: ActiveWorkoutState, session: SessionReference): ActiveWorkoutState {
        require(session.rows.isNotEmpty()) { "Workout session must contain at least one row." }
        val validSubstitutions = progress.selectedSubstitutions.filter { (rowId, choice) ->
            session.rows.any { row -> row.id == rowId && choice in row.alternatives }
        }
        val validEffortIds = validEffortIds(session)
        val completed = progress.completedEffortIds.filterTo(linkedSetOf()) { it in validEffortIds }
        val ordered = positions(session)
        val required = ordered.filterNot { (rowIndex, _) -> session.rows[rowIndex].optional }
        val allRequiredComplete = required.all { (rowIndex, effortIndex) ->
            effortId(session.rows[rowIndex].id, effortIndex) in completed
        }
        if (allRequiredComplete) {
            val finalRequired = required.lastOrNull() ?: ordered.last()
            return progress.copy(
                movementIndex = finalRequired.first,
                effortIndex = finalRequired.second,
                complete = true,
                selectedSubstitutions = validSubstitutions,
                completedEffortIds = completed,
                restTimerEndEpochMillis = null,
            )
        }

        val movement = progress.movementIndex.coerceIn(0, session.rows.lastIndex)
        val effort = progress.effortIndex.coerceIn(1, session.rows[movement].effortCount)
        val currentId = effortId(session.rows[movement].id, effort)
        val target = if (currentId !in completed) {
            movement to effort
        } else {
            ordered.first { (rowIndex, effortIndex) ->
                effortId(session.rows[rowIndex].id, effortIndex) !in completed
            }
        }
        return progress.copy(
            movementIndex = target.first,
            effortIndex = target.second,
            complete = false,
            selectedSubstitutions = validSubstitutions,
            completedEffortIds = completed,
        )
    }

    fun advance(
        progress: ActiveWorkoutState,
        session: SessionReference,
        nowMillis: Long = System.currentTimeMillis(),
    ): ActiveWorkoutState {
        val current = normalize(progress, session)
        if (current.complete) return current

        val row = session.rows[current.movementIndex]
        val currentId = effortId(row.id, current.effortIndex)
        val completed = current.completedEffortIds + currentId
        val ordered = positions(session)
        val currentPositionIndex = ordered.indexOf(current.movementIndex to current.effortIndex)
        val next = ordered.drop(currentPositionIndex + 1).firstOrNull { (rowIndex, effortIndex) ->
            effortId(session.rows[rowIndex].id, effortIndex) !in completed
        } ?: ordered.take(currentPositionIndex).firstOrNull { (rowIndex, effortIndex) ->
            effortId(session.rows[rowIndex].id, effortIndex) !in completed
        }
        val finished = requiredEffortIds(session).all { it in completed }
        val shouldRest = !finished && row.restSeconds != null &&
            (current.effortIndex < row.effortCount || row.restAfterLastEffort)
        val restEnd = if (shouldRest) nowMillis + requireNotNull(row.restSeconds).toLong() * 1_000L else null

        val seeded = current.copy(
            movementIndex = next?.first ?: current.movementIndex,
            effortIndex = next?.second ?: current.effortIndex,
            complete = finished,
            completedEffortIds = completed,
            restTimerEndEpochMillis = restEnd,
            undoMovementIndex = current.movementIndex,
            undoEffortIndex = current.effortIndex,
            undoComplete = current.complete,
            undoRestTimerEndEpochMillis = current.restTimerEndEpochMillis,
            undoCompletedEffortIds = current.completedEffortIds,
        )
        return if (finished) normalize(seeded, session) else seeded
    }

    fun skipOptional(progress: ActiveWorkoutState, session: SessionReference): ActiveWorkoutState {
        val current = normalize(progress, session)
        if (current.complete) return current
        val row = session.rows[current.movementIndex]
        require(row.optional) { "Only an optional workout row may be skipped." }
        val skippedIds = (1..row.effortCount).mapTo(linkedSetOf()) { effortId(row.id, it) }
        val skipped = current.copy(
            completedEffortIds = current.completedEffortIds + skippedIds,
            restTimerEndEpochMillis = null,
            undoMovementIndex = current.movementIndex,
            undoEffortIndex = current.effortIndex,
            undoComplete = current.complete,
            undoRestTimerEndEpochMillis = current.restTimerEndEpochMillis,
            undoCompletedEffortIds = current.completedEffortIds,
        )
        return normalize(skipped, session)
    }

    fun toggleEffort(
        progress: ActiveWorkoutState,
        session: SessionReference,
        rowId: String,
        effortIndex: Int,
        nowMillis: Long = System.currentTimeMillis(),
    ): ActiveWorkoutState {
        val normalized = normalize(progress, session)
        val rowIndex = session.rows.indexOfFirst { it.id == rowId }
        require(rowIndex >= 0) { "Unknown workout row: $rowId" }
        val row = session.rows[rowIndex]
        require(effortIndex in 1..row.effortCount) { "Invalid effort index for $rowId: $effortIndex" }
        val id = effortId(rowId, effortIndex)
        val wasComplete = id in normalized.completedEffortIds
        val nextCompleted = normalized.completedEffortIds.toMutableSet().apply {
            if (wasComplete) remove(id) else add(id)
        }.toSet()
        val pointerSeed = if (wasComplete) {
            normalized.copy(movementIndex = rowIndex, effortIndex = effortIndex, complete = false)
        } else normalized
        val toggled = pointerSeed.copy(
            completedEffortIds = nextCompleted,
            restTimerEndEpochMillis = null,
            undoMovementIndex = normalized.movementIndex,
            undoEffortIndex = normalized.effortIndex,
            undoComplete = normalized.complete,
            undoRestTimerEndEpochMillis = normalized.restTimerEndEpochMillis,
            undoCompletedEffortIds = normalized.completedEffortIds,
        )
        val result = normalize(toggled, session)
        val wasCurrent = normalized.movementIndex == rowIndex && normalized.effortIndex == effortIndex
        if (wasComplete || !wasCurrent || result.complete || row.restSeconds == null) return result
        val shouldRest = effortIndex < row.effortCount || row.restAfterLastEffort
        return if (shouldRest) {
            result.copy(restTimerEndEpochMillis = nowMillis + row.restSeconds.toLong() * 1_000L)
        } else result
    }

    fun undo(progress: ActiveWorkoutState): ActiveWorkoutState {
        if (!progress.canUndo) return progress
        return progress.copy(
            movementIndex = requireNotNull(progress.undoMovementIndex),
            effortIndex = requireNotNull(progress.undoEffortIndex),
            complete = requireNotNull(progress.undoComplete),
            completedEffortIds = requireNotNull(progress.undoCompletedEffortIds),
            restTimerEndEpochMillis = progress.undoRestTimerEndEpochMillis,
            undoMovementIndex = null,
            undoEffortIndex = null,
            undoComplete = null,
            undoRestTimerEndEpochMillis = null,
            undoCompletedEffortIds = null,
        )
    }

    fun skipRest(progress: ActiveWorkoutState): ActiveWorkoutState =
        progress.copy(restTimerEndEpochMillis = null)

    fun addRestSeconds(progress: ActiveWorkoutState, seconds: Int): ActiveWorkoutState {
        require(seconds > 0)
        val end = progress.restTimerEndEpochMillis ?: return progress
        return progress.copy(restTimerEndEpochMillis = end + seconds.toLong() * 1_000L)
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
        return progress.copy(selectedSubstitutions = next.toMap())
    }

    private fun positions(session: SessionReference): List<Pair<Int, Int>> = buildList {
        session.rows.forEachIndexed { rowIndex, row ->
            (1..row.effortCount).forEach { effortIndex -> add(rowIndex to effortIndex) }
        }
    }

    private fun validEffortIds(session: SessionReference): Set<String> = buildSet {
        session.rows.forEach { row ->
            (1..row.effortCount).forEach { effortIndex -> add(effortId(row.id, effortIndex)) }
        }
    }

    private fun requiredEffortIds(session: SessionReference): Set<String> = buildSet {
        session.rows.filterNot { it.optional }.forEach { row ->
            (1..row.effortCount).forEach { effortIndex -> add(effortId(row.id, effortIndex)) }
        }
    }
}
