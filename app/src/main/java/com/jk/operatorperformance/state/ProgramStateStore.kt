package com.jk.operatorperformance.state

import android.content.Context
import com.jk.operatorperformance.program.MachineModality

data class ProgramUiState(
    val currentWeek: Int = 1,
    val currentDay: Int = 1,
    val deloadMode: Boolean = false,
    val restTimerSeconds: Int? = null,
    val utilityRestTimerEndEpochMillis: Long? = null,
    val machineInventoryConfigured: Boolean = false,
    val availableMachineModalities: Set<MachineModality> = emptySet(),
) {
    init {
        require(currentWeek in 1..8) { "currentWeek must be 1–8." }
        require(currentDay in 1..7) { "currentDay must be 1–7." }
        require(restTimerSeconds == null || restTimerSeconds > 0) {
            "restTimerSeconds must be positive when configured."
        }
        require(utilityRestTimerEndEpochMillis == null || utilityRestTimerEndEpochMillis > 0L) {
            "utilityRestTimerEndEpochMillis must be positive when active."
        }
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
    val deloadMode: Boolean,
    val machineInventoryConfigured: Boolean = false,
    val availableMachineModalities: Set<MachineModality> = emptySet(),
    val movementIndex: Int = 0,
    val effortIndex: Int = 1,
    val complete: Boolean = false,
    val selectedSubstitutions: Map<String, String> = emptyMap(),
    val completedEffortIds: Set<String> = emptySet(),
    val restTimerEndEpochMillis: Long? = null,
    val undoMovementIndex: Int? = null,
    val undoEffortIndex: Int? = null,
    val undoComplete: Boolean? = null,
    val undoRestTimerEndEpochMillis: Long? = null,
    val undoCompletedEffortIds: Set<String>? = null,
) {
    init {
        require(week in 1..8) { "Workout week must be 1–8." }
        require(day in 1..7) { "Workout day must be 1–7." }
        require(movementIndex >= 0) { "movementIndex must be non-negative." }
        require(effortIndex >= 1) { "effortIndex must be at least 1." }
        require(restTimerEndEpochMillis == null || restTimerEndEpochMillis > 0L) {
            "restTimerEndEpochMillis must be positive when active."
        }
        require(selectedSubstitutions.keys.none { it.isBlank() }) { "Substitution row ids must be non-blank." }
        require(selectedSubstitutions.values.none { it.isBlank() }) { "Substitution choices must be non-blank." }
        require(completedEffortIds.none { it.isBlank() }) { "Completed effort ids must be non-blank." }
        val pointerUndoParts = listOf(undoMovementIndex, undoEffortIndex, undoComplete)
        val pointerUndoPresent = pointerUndoParts.none { it == null }
        require(pointerUndoParts.all { it == null } || pointerUndoPresent) {
            "Undo pointer state must be fully present or fully absent."
        }
        require(pointerUndoPresent == (undoCompletedEffortIds != null)) {
            "Undo completion state must match undo pointer state."
        }
    }

    val canUndo: Boolean get() = undoMovementIndex != null

    fun matchesPointer(state: ProgramUiState): Boolean =
        week == state.currentWeek && day == state.currentDay
}

class ProgramStateStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): ProgramUiState = ProgramUiState(
        currentWeek = prefs.getInt(KEY_WEEK, 1).coerceIn(1, 8),
        currentDay = prefs.getInt(KEY_DAY, 1).coerceIn(1, 7),
        deloadMode = prefs.getBoolean(KEY_DELOAD, false),
        restTimerSeconds = prefs.getInt(KEY_REST_TIMER_SECONDS, 0).takeIf { it > 0 },
        utilityRestTimerEndEpochMillis = prefs.getLong(KEY_UTILITY_REST_TIMER_END, 0L).takeIf { it > 0L },
        machineInventoryConfigured = prefs.getBoolean(KEY_MACHINE_INVENTORY_CONFIGURED, false),
        availableMachineModalities = decodeMachineModalities(
            prefs.getStringSet(KEY_MACHINE_MODALITIES, emptySet()).orEmpty(),
        ),
    )

    fun save(state: ProgramUiState) {
        val editor = prefs.edit()
            .putInt(KEY_WEEK, state.currentWeek)
            .putInt(KEY_DAY, state.currentDay)
            .putBoolean(KEY_DELOAD, state.deloadMode)
            .putBoolean(KEY_MACHINE_INVENTORY_CONFIGURED, state.machineInventoryConfigured)
            .putStringSet(KEY_MACHINE_MODALITIES, state.availableMachineModalities.mapTo(linkedSetOf()) { it.name })
            .remove(KEY_LEGACY_SPEED)
            .remove(KEY_LEGACY_ENGINE)
        state.restTimerSeconds?.let { editor.putInt(KEY_REST_TIMER_SECONDS, it) }
            ?: editor.remove(KEY_REST_TIMER_SECONDS)
        state.utilityRestTimerEndEpochMillis?.let { editor.putLong(KEY_UTILITY_REST_TIMER_END, it) }
            ?: editor.remove(KEY_UTILITY_REST_TIMER_END)
        editor.apply()
    }

    fun loadWorkout(): ActiveWorkoutState? {
        if (!prefs.getBoolean(KEY_WORKOUT_ACTIVE, false)) return null
        if (prefs.getInt(KEY_WORKOUT_SCHEMA, 0) != WORKOUT_SCHEMA_VERSION) {
            clearWorkout()
            return null
        }
        val hasUndo = prefs.getBoolean(KEY_WORKOUT_HAS_UNDO, false)
        return ActiveWorkoutState(
            week = prefs.getInt(KEY_WORKOUT_WEEK, 1).coerceIn(1, 8),
            day = prefs.getInt(KEY_WORKOUT_DAY, 1).coerceIn(1, 7),
            deloadMode = prefs.getBoolean(KEY_WORKOUT_DELOAD, false),
            machineInventoryConfigured = prefs.getBoolean(KEY_WORKOUT_MACHINE_INVENTORY_CONFIGURED, false),
            availableMachineModalities = decodeMachineModalities(
                prefs.getStringSet(KEY_WORKOUT_MACHINE_MODALITIES, emptySet()).orEmpty(),
            ),
            movementIndex = prefs.getInt(KEY_WORKOUT_MOVEMENT, 0).coerceAtLeast(0),
            effortIndex = prefs.getInt(KEY_WORKOUT_EFFORT, 1).coerceAtLeast(1),
            complete = prefs.getBoolean(KEY_WORKOUT_COMPLETE, false),
            selectedSubstitutions = decodeSubstitutions(
                prefs.getStringSet(KEY_WORKOUT_SUBSTITUTIONS, emptySet()).orEmpty(),
            ),
            completedEffortIds = prefs.getStringSet(KEY_WORKOUT_COMPLETED_EFFORTS, emptySet()).orEmpty().toSet(),
            restTimerEndEpochMillis = prefs.getLong(KEY_WORKOUT_REST_TIMER_END, 0L).takeIf { it > 0L },
            undoMovementIndex = if (hasUndo) prefs.getInt(KEY_WORKOUT_UNDO_MOVEMENT, 0).coerceAtLeast(0) else null,
            undoEffortIndex = if (hasUndo) prefs.getInt(KEY_WORKOUT_UNDO_EFFORT, 1).coerceAtLeast(1) else null,
            undoComplete = if (hasUndo) prefs.getBoolean(KEY_WORKOUT_UNDO_COMPLETE, false) else null,
            undoRestTimerEndEpochMillis = if (hasUndo) prefs.getLong(KEY_WORKOUT_UNDO_REST_TIMER_END, 0L).takeIf { it > 0L } else null,
            undoCompletedEffortIds = if (hasUndo) {
                prefs.getStringSet(KEY_WORKOUT_UNDO_COMPLETED_EFFORTS, emptySet()).orEmpty().toSet()
            } else null,
        )
    }

    fun saveWorkout(state: ActiveWorkoutState) {
        val editor = prefs.edit()
            .putBoolean(KEY_WORKOUT_ACTIVE, true)
            .putInt(KEY_WORKOUT_SCHEMA, WORKOUT_SCHEMA_VERSION)
            .putInt(KEY_WORKOUT_WEEK, state.week)
            .putInt(KEY_WORKOUT_DAY, state.day)
            .putBoolean(KEY_WORKOUT_DELOAD, state.deloadMode)
            .putBoolean(KEY_WORKOUT_MACHINE_INVENTORY_CONFIGURED, state.machineInventoryConfigured)
            .putStringSet(KEY_WORKOUT_MACHINE_MODALITIES, state.availableMachineModalities.mapTo(linkedSetOf()) { it.name })
            .putInt(KEY_WORKOUT_MOVEMENT, state.movementIndex)
            .putInt(KEY_WORKOUT_EFFORT, state.effortIndex)
            .putBoolean(KEY_WORKOUT_COMPLETE, state.complete)
            .putStringSet(KEY_WORKOUT_SUBSTITUTIONS, encodeSubstitutions(state.selectedSubstitutions))
            .putStringSet(KEY_WORKOUT_COMPLETED_EFFORTS, state.completedEffortIds)
            .putBoolean(KEY_WORKOUT_HAS_UNDO, state.canUndo)
            .remove(KEY_LEGACY_WORKOUT_SPEED)
            .remove(KEY_LEGACY_WORKOUT_ENGINE)
        state.restTimerEndEpochMillis?.let { editor.putLong(KEY_WORKOUT_REST_TIMER_END, it) }
            ?: editor.remove(KEY_WORKOUT_REST_TIMER_END)
        if (state.canUndo) {
            editor
                .putInt(KEY_WORKOUT_UNDO_MOVEMENT, requireNotNull(state.undoMovementIndex))
                .putInt(KEY_WORKOUT_UNDO_EFFORT, requireNotNull(state.undoEffortIndex))
                .putBoolean(KEY_WORKOUT_UNDO_COMPLETE, requireNotNull(state.undoComplete))
                .putStringSet(KEY_WORKOUT_UNDO_COMPLETED_EFFORTS, requireNotNull(state.undoCompletedEffortIds))
            state.undoRestTimerEndEpochMillis?.let { editor.putLong(KEY_WORKOUT_UNDO_REST_TIMER_END, it) }
                ?: editor.remove(KEY_WORKOUT_UNDO_REST_TIMER_END)
        } else {
            editor
                .remove(KEY_WORKOUT_UNDO_MOVEMENT)
                .remove(KEY_WORKOUT_UNDO_EFFORT)
                .remove(KEY_WORKOUT_UNDO_COMPLETE)
                .remove(KEY_WORKOUT_UNDO_REST_TIMER_END)
                .remove(KEY_WORKOUT_UNDO_COMPLETED_EFFORTS)
        }
        editor.apply()
    }

    fun clearWorkout() {
        prefs.edit()
            .remove(KEY_WORKOUT_ACTIVE)
            .remove(KEY_WORKOUT_SCHEMA)
            .remove(KEY_WORKOUT_WEEK)
            .remove(KEY_WORKOUT_DAY)
            .remove(KEY_WORKOUT_DELOAD)
            .remove(KEY_WORKOUT_MACHINE_INVENTORY_CONFIGURED)
            .remove(KEY_WORKOUT_MACHINE_MODALITIES)
            .remove(KEY_WORKOUT_MOVEMENT)
            .remove(KEY_WORKOUT_EFFORT)
            .remove(KEY_WORKOUT_COMPLETE)
            .remove(KEY_WORKOUT_SUBSTITUTIONS)
            .remove(KEY_WORKOUT_COMPLETED_EFFORTS)
            .remove(KEY_WORKOUT_REST_TIMER_END)
            .remove(KEY_WORKOUT_HAS_UNDO)
            .remove(KEY_WORKOUT_UNDO_MOVEMENT)
            .remove(KEY_WORKOUT_UNDO_EFFORT)
            .remove(KEY_WORKOUT_UNDO_COMPLETE)
            .remove(KEY_WORKOUT_UNDO_REST_TIMER_END)
            .remove(KEY_WORKOUT_UNDO_COMPLETED_EFFORTS)
            .remove(KEY_LEGACY_WORKOUT_SPEED)
            .remove(KEY_LEGACY_WORKOUT_ENGINE)
            .apply()
    }

    private fun encodeSubstitutions(substitutions: Map<String, String>): Set<String> =
        substitutions.mapTo(linkedSetOf()) { (rowId, choice) -> rowId + SUBSTITUTION_SEPARATOR + choice }

    private fun decodeSubstitutions(encoded: Set<String>): Map<String, String> =
        encoded.mapNotNull { item ->
            val separatorIndex = item.indexOf(SUBSTITUTION_SEPARATOR)
            if (separatorIndex <= 0 || separatorIndex >= item.lastIndex) return@mapNotNull null
            item.substring(0, separatorIndex) to item.substring(separatorIndex + SUBSTITUTION_SEPARATOR.length)
        }.toMap()

    private fun decodeMachineModalities(encoded: Set<String>): Set<MachineModality> =
        encoded.mapNotNullTo(linkedSetOf()) { raw -> MachineModality.entries.firstOrNull { it.name == raw } }

    companion object {
        // Preserve the historical preference namespace so the v0.34 -> current pointer migration remains compatible.
        private const val PREFS_NAME = "operator_v54_reference_state"
        private const val KEY_WEEK = "currentWeek"
        private const val KEY_DAY = "currentDay"
        private const val KEY_DELOAD = "manualDeloadMode"
        private const val KEY_REST_TIMER_SECONDS = "manualRestTimerSeconds"
        private const val KEY_UTILITY_REST_TIMER_END = "utilityRestTimerEndEpochMillis"
        private const val KEY_MACHINE_INVENTORY_CONFIGURED = "machineInventoryConfigured"
        private const val KEY_MACHINE_MODALITIES = "availableMachineModalities"
        private val KEY_LEGACY_SPEED = listOf("manual", "Speed", "Variant").joinToString("")
        private val KEY_LEGACY_ENGINE = listOf("manual", "Engine", "Variant").joinToString("")

        private const val WORKOUT_SCHEMA_VERSION = 2
        private const val KEY_WORKOUT_ACTIVE = "workoutActive"
        private const val KEY_WORKOUT_SCHEMA = "workoutSchemaVersion"
        private const val KEY_WORKOUT_WEEK = "workoutWeek"
        private const val KEY_WORKOUT_DAY = "workoutDay"
        private const val KEY_WORKOUT_DELOAD = "workoutDeloadMode"
        private const val KEY_WORKOUT_MACHINE_INVENTORY_CONFIGURED = "workoutMachineInventoryConfigured"
        private const val KEY_WORKOUT_MACHINE_MODALITIES = "workoutAvailableMachineModalities"
        private const val KEY_WORKOUT_MOVEMENT = "workoutMovementIndex"
        private const val KEY_WORKOUT_EFFORT = "workoutEffortIndex"
        private const val KEY_WORKOUT_COMPLETE = "workoutComplete"
        private const val KEY_WORKOUT_SUBSTITUTIONS = "workoutSubstitutions"
        private const val KEY_WORKOUT_COMPLETED_EFFORTS = "workoutCompletedEffortIds"
        private const val KEY_WORKOUT_REST_TIMER_END = "workoutRestTimerEndEpochMillis"
        private const val KEY_WORKOUT_HAS_UNDO = "workoutHasUndo"
        private const val KEY_WORKOUT_UNDO_MOVEMENT = "workoutUndoMovementIndex"
        private const val KEY_WORKOUT_UNDO_EFFORT = "workoutUndoEffortIndex"
        private const val KEY_WORKOUT_UNDO_COMPLETE = "workoutUndoComplete"
        private const val KEY_WORKOUT_UNDO_REST_TIMER_END = "workoutUndoRestTimerEndEpochMillis"
        private const val KEY_WORKOUT_UNDO_COMPLETED_EFFORTS = "workoutUndoCompletedEffortIds"
        private val KEY_LEGACY_WORKOUT_SPEED = listOf("workout", "Speed", "Variant").joinToString("")
        private val KEY_LEGACY_WORKOUT_ENGINE = listOf("workout", "Engine", "Variant").joinToString("")
        private const val SUBSTITUTION_SEPARATOR = "\u001F"
    }
}
