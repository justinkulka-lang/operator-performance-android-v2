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

    private inline fun <reified T : Enum<T>> enumOrDefault(raw: String?, fallback: T): T =
        enumValues<T>().firstOrNull { it.name == raw } ?: fallback

    companion object {
        private const val PREFS_NAME = "operator_v54_reference_state"
        private const val KEY_WEEK = "currentWeek"
        private const val KEY_DAY = "currentDay"
        private const val KEY_SPEED = "manualSpeedVariant"
        private const val KEY_ENGINE = "manualEngineVariant"
        private const val KEY_DELOAD = "manualDeloadMode"
    }
}
