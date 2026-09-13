package com.jk.operatorperformance.state

import android.content.Context
import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.OperatorProgramV54
import com.jk.operatorperformance.program.SpeedVariant

data class ProgramUiState(
    val currentWeek: Int = 1,
    val currentDay: Int = 1,
    val speedVariant: SpeedVariant = OperatorProgramV54.firstPostBaselineSpeedDefault,
    val engineVariant: EngineVariant = OperatorProgramV54.firstPostBaselineEngineDefault,
    val deloadMode: Boolean = false,
) {
    init {
        require(currentWeek in 1..8) { "currentWeek must be 1–8." }
        require(currentDay in 1..7) { "currentDay must be 1–7." }
    }

    fun advanceDay(): ProgramUiState = when {
        currentDay < 7 -> copy(currentDay = currentDay + 1)
        currentWeek < 8 -> copy(currentWeek = currentWeek + 1, currentDay = 1)
        else -> this
    }

    fun withCurrentPointer(week: Int, day: Int): ProgramUiState {
        require(week in 1..8) { "Only populated Weeks 1–8 may be current." }
        require(day in 1..7) { "Day must be 1–7." }
        return copy(currentWeek = week, currentDay = day)
    }
}

data class ActiveWorkoutState(
    val week: Int,
    val day: Int,
    val speedVariant: SpeedVariant,
    val engineVariant: EngineVariant,
    val deloadMode: Boolean,
    val movementIndex: Int = 0,
    val effortIndex: Int = 1,
    val complete: Boolean = false,
    val undoMovementIndex: Int? = null,
    val undoEffortIndex: Int? = null,
    val undoComplete: Boolean? = null,
) {
    init {
        require(week in 1..8) { "Workout week must be 1–8." }
        require(day in 1..7) { "Workout day must be 1–7." }
        require(movementIndex >= 0) { "movementIndex must be non-negative." }
        require(effortIndex >= 1) { "effortIndex must be at least 1." }
        val undoParts = listOf(undoMovementIndex, undoEffortIndex, undoComplete)
        require(undoParts.all { it == null } || undoParts.none { it == null }) {
            "Undo state must be fully present or fully absent."
        }
    }

    val canUndo: Boolean
        get() = undoMovementIndex != null

    fun matchesPointer(state: ProgramUiState): Boolean =
        week == state.currentWeek && day == state.currentDay
}

class ProgramStateStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): ProgramUiState {
        val week = prefs.getInt(KEY_WEEK, 1).coerceIn(1, 8)
        val day = prefs.getInt(KEY_DAY, 1).coerceIn(1, 7)
        val speed = enumOrDefault(
            prefs.getString(KEY_SPEED, null),
            OperatorProgramV54.firstPostBaselineSpeedDefault,
        )
        val engine = enumOrDefault(
            prefs.getString(KEY_ENGINE, null),
            OperatorProgramV54.firstPostBaselineEngineDefault,
        )
        return ProgramUiState(
            currentWeek = week,
            currentDay = day,
            speedVariant = speed,
            engineVariant = engine,
            deloadMode = prefs.getBoolean(KEY_DELOAD, false),
        )
    }

    fun save(state: ProgramUiState) {
        prefs.edit()
            .putInt(KEY_WEEK, state.currentWeek)
            .putInt(KEY_DAY, state.currentDay)
            .putString(KEY_SPEED, state.speedVariant.name)
            .putString(KEY_ENGINE, state.engineVariant.name)
            .putBoolean(KEY_DELOAD, state.deloadMode)
            .apply()
    }

    fun loadWorkout(): ActiveWorkoutState? {
        if (!prefs.getBoolean(KEY_WORKOUT_ACTIVE, false)) return null
        val hasUndo = prefs.getBoolean(KEY_WORKOUT_HAS_UNDO, false)
        return ActiveWorkoutState(
            week = prefs.getInt(KEY_WORKOUT_WEEK, 1).coerceIn(1, 8),
            day = prefs.getInt(KEY_WORKOUT_DAY, 1).coerceIn(1, 7),
            speedVariant = enumOrDefault(
                prefs.getString(KEY_WORKOUT_SPEED, null),
                OperatorProgramV54.firstPostBaselineSpeedDefault,
            ),
            engineVariant = enumOrDefault(
                prefs.getString(KEY_WORKOUT_ENGINE, null),
                OperatorProgramV54.firstPostBaselineEngineDefault,
            ),
            deloadMode = prefs.getBoolean(KEY_WORKOUT_DELOAD, false),
            movementIndex = prefs.getInt(KEY_WORKOUT_MOVEMENT, 0).coerceAtLeast(0),
            effortIndex = prefs.getInt(KEY_WORKOUT_EFFORT, 1).coerceAtLeast(1),
            complete = prefs.getBoolean(KEY_WORKOUT_COMPLETE, false),
            undoMovementIndex = if (hasUndo) prefs.getInt(KEY_WORKOUT_UNDO_MOVEMENT, 0).coerceAtLeast(0) else null,
            undoEffortIndex = if (hasUndo) prefs.getInt(KEY_WORKOUT_UNDO_EFFORT, 1).coerceAtLeast(1) else null,
            undoComplete = if (hasUndo) prefs.getBoolean(KEY_WORKOUT_UNDO_COMPLETE, false) else null,
        )
    }

    fun saveWorkout(state: ActiveWorkoutState) {
        val editor = prefs.edit()
            .putBoolean(KEY_WORKOUT_ACTIVE, true)
            .putInt(KEY_WORKOUT_WEEK, state.week)
            .putInt(KEY_WORKOUT_DAY, state.day)
            .putString(KEY_WORKOUT_SPEED, state.speedVariant.name)
            .putString(KEY_WORKOUT_ENGINE, state.engineVariant.name)
            .putBoolean(KEY_WORKOUT_DELOAD, state.deloadMode)
            .putInt(KEY_WORKOUT_MOVEMENT, state.movementIndex)
            .putInt(KEY_WORKOUT_EFFORT, state.effortIndex)
            .putBoolean(KEY_WORKOUT_COMPLETE, state.complete)
            .putBoolean(KEY_WORKOUT_HAS_UNDO, state.canUndo)
        if (state.canUndo) {
            editor
                .putInt(KEY_WORKOUT_UNDO_MOVEMENT, requireNotNull(state.undoMovementIndex))
                .putInt(KEY_WORKOUT_UNDO_EFFORT, requireNotNull(state.undoEffortIndex))
                .putBoolean(KEY_WORKOUT_UNDO_COMPLETE, requireNotNull(state.undoComplete))
        } else {
            editor
                .remove(KEY_WORKOUT_UNDO_MOVEMENT)
                .remove(KEY_WORKOUT_UNDO_EFFORT)
                .remove(KEY_WORKOUT_UNDO_COMPLETE)
        }
        editor.apply()
    }

    fun clearWorkout() {
        prefs.edit()
            .remove(KEY_WORKOUT_ACTIVE)
            .remove(KEY_WORKOUT_WEEK)
            .remove(KEY_WORKOUT_DAY)
            .remove(KEY_WORKOUT_SPEED)
            .remove(KEY_WORKOUT_ENGINE)
            .remove(KEY_WORKOUT_DELOAD)
            .remove(KEY_WORKOUT_MOVEMENT)
            .remove(KEY_WORKOUT_EFFORT)
            .remove(KEY_WORKOUT_COMPLETE)
            .remove(KEY_WORKOUT_HAS_UNDO)
            .remove(KEY_WORKOUT_UNDO_MOVEMENT)
            .remove(KEY_WORKOUT_UNDO_EFFORT)
            .remove(KEY_WORKOUT_UNDO_COMPLETE)
            .apply()
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == raw } ?: fallback

    companion object {
        private const val PREFS_NAME = "operator_v54_reference_state"
        private const val KEY_WEEK = "currentWeek"
        private const val KEY_DAY = "currentDay"
        private const val KEY_SPEED = "manualSpeedVariant"
        private const val KEY_ENGINE = "manualEngineVariant"
        private const val KEY_DELOAD = "manualDeloadMode"

        private const val KEY_WORKOUT_ACTIVE = "workoutActive"
        private const val KEY_WORKOUT_WEEK = "workoutWeek"
        private const val KEY_WORKOUT_DAY = "workoutDay"
        private const val KEY_WORKOUT_SPEED = "workoutSpeedVariant"
        private const val KEY_WORKOUT_ENGINE = "workoutEngineVariant"
        private const val KEY_WORKOUT_DELOAD = "workoutDeloadMode"
        private const val KEY_WORKOUT_MOVEMENT = "workoutMovementIndex"
        private const val KEY_WORKOUT_EFFORT = "workoutEffortIndex"
        private const val KEY_WORKOUT_COMPLETE = "workoutComplete"
        private const val KEY_WORKOUT_HAS_UNDO = "workoutHasUndo"
        private const val KEY_WORKOUT_UNDO_MOVEMENT = "workoutUndoMovementIndex"
        private const val KEY_WORKOUT_UNDO_EFFORT = "workoutUndoEffortIndex"
        private const val KEY_WORKOUT_UNDO_COMPLETE = "workoutUndoComplete"
    }
}
