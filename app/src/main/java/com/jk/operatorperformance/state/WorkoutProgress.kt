package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRow
import java.util.Locale

/**
 * A saved workout whose authority is not the authority of its week is not shown on Today.
 * A null authority is kept; [WorkoutProgress.normalize] still rejects a real mismatch.
 */
fun workoutForToday(state: ProgramUiState, workout: ActiveWorkoutState?): ActiveWorkoutState? {
    if (workout == null) return null
    val savedAuthority = workout.authority
    if (savedAuthority == com.jk.operatorperformance.program.ProgramAuthority.V74 && workout.week != state.currentWeek) return null
    if (savedAuthority == null) return workout
    return if (savedAuthority == state.authorityForWeek(workout.week)) workout else null
}

enum class SessionStatus {
    NOT_STARTED,
    ACTIVE,
    RESUMABLE,
    COMPLETED,
}

/**
 * Ticks are local completion marks for the active workout. An id is "<row.id>#<n>".
 * They record that a set was marked done, never a load, rep count, RIR, time, or result.
 * Ticks clear only when the workout is cleared.
 */
object WorkoutProgress {
    fun effortId(rowId: String, index: Int): String = "$rowId#$index"

    fun effortWord(label: String): String =
        label.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }

    fun toggleEffort(progress: ActiveWorkoutState, row: SessionRow, index: Int): ActiveWorkoutState {
        require(index in 1..row.effortCount) { "Effort $index is outside 1..${row.effortCount} for ${row.id}." }
        val id = effortId(row.id, index)
        val next = if (id in progress.completedEffortIds) progress.completedEffortIds - id else progress.completedEffortIds + id
        return progress.copy(completedEffortIds = next)
    }

    fun completedEffortCount(progress: ActiveWorkoutState, row: SessionRow): Int =
        (1..row.effortCount).count { effortId(row.id, it) in progress.completedEffortIds }

    fun isRowComplete(progress: ActiveWorkoutState, row: SessionRow): Boolean =
        completedEffortCount(progress, row) == row.effortCount

    fun retainedEffortIds(ids: Set<String>, session: SessionReference): Set<String> {
        val rows = session.rows.associateBy { it.id }
        return ids.mapNotNullTo(linkedSetOf()) { id ->
            val split = id.lastIndexOf('#')
            if (split <= 0 || split == id.lastIndex) return@mapNotNullTo null
            val row = rows[id.substring(0, split)] ?: return@mapNotNullTo null
            val n = id.substring(split + 1).toIntOrNull() ?: return@mapNotNullTo null
            if (n in 1..row.effortCount) effortId(row.id, n) else null
        }
    }

    fun start(state: ProgramUiState): ActiveWorkoutState = ActiveWorkoutState(
        week = state.currentWeek,
        day = state.currentDay,
        authority = state.authorityForWeek(state.currentWeek),
    )

    fun status(workout: ActiveWorkoutState?, showingWorkout: Boolean): SessionStatus = when {
        workout == null -> SessionStatus.NOT_STARTED
        workout.complete -> SessionStatus.COMPLETED
        showingWorkout -> SessionStatus.ACTIVE
        else -> SessionStatus.RESUMABLE
    }

    fun normalize(progress: ActiveWorkoutState, session: SessionReference): ActiveWorkoutState {
        require(progress.authority == null || progress.authority == session.authority) { "Saved session authority does not match reference." }
        require(session.rows.isNotEmpty()) { "Workout session must contain at least one row." }
        val validSubstitutions = progress.selectedSubstitutions.filter { (rowId, choice) ->
            session.rows.any { row -> row.id == rowId && choice in row.alternatives }
        }
        return progress.copy(
            authority = progress.authority ?: session.authority,
            movementIndex = 0,
            effortIndex = 1,
            selectedSubstitutions = validSubstitutions,
            completedEffortIds = retainedEffortIds(progress.completedEffortIds, session),
            undoMovementIndex = null,
            undoEffortIndex = null,
            undoComplete = null,
            undoRestTimerEndEpochMillis = null,
            undoCompletedEffortIds = null,
        )
    }

    fun markSessionComplete(progress: ActiveWorkoutState): ActiveWorkoutState =
        progress.copy(complete = true, restTimerEndEpochMillis = null)

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

    fun startManualRest(progress: ActiveWorkoutState, seconds: Int, nowMillis: Long): ActiveWorkoutState {
        require(seconds in 30..600)
        return progress.copy(restTimerEndEpochMillis = nowMillis + seconds.toLong() * 1_000L)
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
}
