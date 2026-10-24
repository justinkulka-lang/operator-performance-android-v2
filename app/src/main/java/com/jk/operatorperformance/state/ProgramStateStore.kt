package com.jk.operatorperformance.state

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.jk.operatorperformance.program.ProgramAuthority

data class ProgramUiState(
    val initialized: Boolean = false,
    val currentWeek: Int = 1,
    val currentDay: Int = 1,
    val restTimerSeconds: Int? = null,
    val utilityRestTimerEndEpochMillis: Long? = null,
    val authority: ProgramAuthority = ProgramAuthority.V72,
    val retainedV71Week: Int? = null,
    val v72ActivationProven: Boolean = false,
    val v73ActivationWeek: Int? = null,
    val v73MigrationApplied: Boolean = false,
    val historicalAuthorities: Map<Int, ProgramAuthority> = emptyMap(),
) {
    init {
        require(currentWeek in 1..32) { "currentWeek must be 1–32." }
        require(currentDay in 1..7) { "currentDay must be 1–7." }
        require(restTimerSeconds == null || restTimerSeconds > 0)
        require(utilityRestTimerEndEpochMillis == null || utilityRestTimerEndEpochMillis > 0L)
        require(retainedV71Week == null || retainedV71Week in 1..32)
        require(v73ActivationWeek == null || v73ActivationWeek in 1..32)
        require(historicalAuthorities.keys.all { it in 1..32 })
    }
    fun authorityForWeek(week: Int): ProgramAuthority {
        require(week in 1..32)
        return historicalAuthorities[week]
            ?: if (retainedV71Week == week) ProgramAuthority.V71
            else if (v73ActivationWeek != null && week >= v73ActivationWeek) ProgramAuthority.V73
            else ProgramAuthority.V72
    }
    fun startV73() = copy(initialized = true, currentWeek = 1, currentDay = 1,
        authority = ProgramAuthority.V73, retainedV71Week = null, v72ActivationProven = true,
        v73ActivationWeek = 1, v73MigrationApplied = true, historicalAuthorities = emptyMap())
    fun resetToV73Start() = startV73()

    fun startV71() = copy(initialized=true,currentWeek=2,currentDay=3)
    fun startV72() = copy(
        initialized = true,
        currentWeek = 1,
        currentDay = 1,
        authority = ProgramAuthority.V72,
        retainedV71Week = null,
        v72ActivationProven = true,
    )
    fun advanceDay(): ProgramUiState = when {
        currentDay < 7 -> copy(currentDay = currentDay + 1)
        currentWeek < 32 && v73MigrationApplied -> copy(currentWeek = currentWeek + 1,
            currentDay = 1, authority = authorityForWeek(currentWeek + 1))
        currentWeek < 32 -> {
            val leavingRetainedWeek = retainedV71Week == currentWeek
            copy(
                currentWeek = currentWeek + 1,
                currentDay = 1,
                authority = if (leavingRetainedWeek) ProgramAuthority.V72 else authority,
                retainedV71Week = if (leavingRetainedWeek) null else retainedV71Week,
                v72ActivationProven = if (leavingRetainedWeek) true else v72ActivationProven,
            )
        }
        else -> this
    }
    fun withCurrentPointer(week:Int,day:Int):ProgramUiState {
        require(week in 1..32); require(day in 1..7)
        if (v73MigrationApplied) return copy(currentWeek = week, currentDay = day,
            authority = authorityForWeek(week))
        val stillRetained = retainedV71Week == week
        return copy(
            currentWeek = week,
            currentDay = day,
            authority = if (stillRetained) ProgramAuthority.V71 else ProgramAuthority.V72,
            retainedV71Week = if (stillRetained) retainedV71Week else null,
        )
    }
    fun resetToV72Start() = copy(
        initialized = true,
        currentWeek = 1,
        currentDay = 1,
        authority = ProgramAuthority.V72,
        retainedV71Week = null,
        v72ActivationProven = true,
    )
}

enum class WeekStartEvidence {
    /** No durable session-status record. Absence is not proof the week never started. */
    UNKNOWN,
    /** A recorded session status shows this week has not been started. */
    NOT_STARTED,
    /** A workout was started or a day in this week was completed, even if that workout was later cleared. */
    ENGAGED,
}

object AuthorityMigration {
    const val CURRENT_REVISION = 2

    /**
     * An initialized Day 1 with no active workout is not treated as unstarted.
     * clearWorkout() deletes the workout and leaves the pointer in place, so that
     * pair cannot be distinguished from a week that never began.
     * v7.2 is activated immediately only when the install is uninitialized, or when
     * a session-status record positively shows the current Day 1 was never started.
     */
    fun apply(
        state: ProgramUiState,
        workoutActive: Boolean,
        evidence: WeekStartEvidence = WeekStartEvidence.UNKNOWN,
    ): ProgramUiState {
        if (!state.initialized) {
            return state.copy(
                currentWeek = 1,
                currentDay = 1,
                authority = ProgramAuthority.V72,
                retainedV71Week = null,
                v72ActivationProven = false,
            )
        }
        val provenUnstarted = state.currentDay == 1 && !workoutActive && evidence == WeekStartEvidence.NOT_STARTED
        return if (provenUnstarted) {
            state.copy(authority = ProgramAuthority.V72, retainedV71Week = null, v72ActivationProven = true)
        } else {
            state.copy(authority = ProgramAuthority.V71, retainedV71Week = state.currentWeek, v72ActivationProven = false)
        }
    }

    /**
     * One-time repair for installs that already set v72MigrationApplied before revision 2.
     * The earlier Day 1 heuristic activated v7.2 whenever Day 1 had no active workout.
     * That shape is restored to v7.1 unless a session-status record proves the week
     * has not started, or this build has already recorded an explicit v7.2 activation.
     * An active v7.1 week, including Week 32, is left in place. No week after 32 is created.
     */
    fun reconcileAlreadyMigrated(
        state: ProgramUiState,
        workoutActive: Boolean,
        evidence: WeekStartEvidence,
    ): ProgramUiState {
        if (!state.initialized) {
            return state.copy(
                currentWeek = 1,
                currentDay = 1,
                authority = ProgramAuthority.V72,
                retainedV71Week = null,
                v72ActivationProven = false,
            )
        }
        if (state.v72ActivationProven) return state
        if (state.retainedV71Week != null && state.currentWeek == state.retainedV71Week) {
            return state.copy(authority = ProgramAuthority.V71, v72ActivationProven = false)
        }
        val provenUnstarted = state.currentDay == 1 && !workoutActive && evidence == WeekStartEvidence.NOT_STARTED
        if (provenUnstarted) {
            return state.copy(authority = ProgramAuthority.V72, retainedV71Week = null, v72ActivationProven = true)
        }
        if (state.currentDay == 1 && !workoutActive) {
            return state.copy(
                authority = ProgramAuthority.V71,
                retainedV71Week = state.currentWeek,
                v72ActivationProven = false,
            )
        }
        if (state.authority == ProgramAuthority.V71) {
            return state.copy(retainedV71Week = state.currentWeek, v72ActivationProven = false)
        }
        return state
    }

    fun evidenceFor(recordPresent: Boolean, engagedAbsoluteDay: Int, week: Int): WeekStartEvidence {
        if (!recordPresent) return WeekStartEvidence.UNKNOWN
        val weekStart = (week - 1) * 7 + 1
        return if (engagedAbsoluteDay >= weekStart) WeekStartEvidence.ENGAGED else WeekStartEvidence.NOT_STARTED
    }
}

/** Retain historical weeks; activate only at a positively unstarted Day 1 or normal next-week boundary. */
object V73AuthorityMigration {
    fun apply(state: ProgramUiState, workoutActive: Boolean, evidence: WeekStartEvidence, latestEngagedWeek: Int? = null): ProgramUiState {
        if (state.v73MigrationApplied) return state
        if (!state.initialized) return state.copy(authority = ProgramAuthority.V73,
            currentWeek = 1, currentDay = 1, v73ActivationWeek = 1,
            v73MigrationApplied = true, historicalAuthorities = emptyMap())
        val unstarted = state.currentDay == 1 && !workoutActive && evidence == WeekStartEvidence.NOT_STARTED
        val lastRetained = maxOf(if (unstarted) state.currentWeek - 1 else state.currentWeek, latestEngagedWeek ?: 0)
        val history = (1..lastRetained).associateWith { state.authorityForWeek(it) }
        val activation = (lastRetained + 1).takeIf { it <= 32 }
        return state.copy(authority = if (unstarted) ProgramAuthority.V73 else state.authorityForWeek(state.currentWeek),
            v73ActivationWeek = activation, v73MigrationApplied = true, historicalAuthorities = history)
    }
}

data class ActiveWorkoutState(
    val week:Int,
    val day:Int,
    val movementIndex:Int=0,
    val effortIndex:Int=1,
    val complete:Boolean=false,
    val selectedSubstitutions:Map<String,String> = emptyMap(),
    val completedEffortIds:Set<String> = emptySet(),
    val restTimerEndEpochMillis:Long?=null,
    val undoMovementIndex:Int?=null,
    val undoEffortIndex:Int?=null,
    val undoComplete:Boolean?=null,
    val undoRestTimerEndEpochMillis:Long?=null,
    val undoCompletedEffortIds:Set<String>?=null,
    val authority: ProgramAuthority? = null,
) {
    init {
        require(week in 1..32); require(day in 1..7); require(movementIndex>=0); require(effortIndex>=1)
        require(restTimerEndEpochMillis==null || restTimerEndEpochMillis>0L)
        require(selectedSubstitutions.keys.none { it.isBlank() }); require(selectedSubstitutions.values.none { it.isBlank() })
        require(completedEffortIds.none { it.isBlank() })
        val undo = listOf(undoMovementIndex,undoEffortIndex,undoComplete)
        require(undo.all { it==null } || undo.none { it==null })
        require((undo.none { it==null }) == (undoCompletedEffortIds!=null))
    }
    val canUndo:Boolean get() = undoMovementIndex!=null
    fun matchesPointer(state:ProgramUiState)=week==state.currentWeek && day==state.currentDay
}

internal interface PrefsStore {
    fun contains(key: String): Boolean
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getInt(key: String, default: Int): Int
    fun getLong(key: String, default: Long): Long
    fun getString(key: String, default: String?): String?
    fun getStringSet(key: String, default: Set<String>): Set<String>
    fun edit(block: PrefsEditor.() -> Unit)
}

internal interface PrefsEditor {
    fun putBoolean(key: String, value: Boolean)
    fun putInt(key: String, value: Int)
    fun putLong(key: String, value: Long)
    fun putString(key: String, value: String)
    fun putStringSet(key: String, value: Set<String>)
    fun remove(key: String)
}

internal class SharedPrefsStore(private val prefs: SharedPreferences) : PrefsStore {
    override fun contains(key: String) = prefs.contains(key)
    override fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    override fun getInt(key: String, default: Int) = prefs.getInt(key, default)
    override fun getLong(key: String, default: Long) = prefs.getLong(key, default)
    override fun getString(key: String, default: String?) = prefs.getString(key, default)
    override fun getStringSet(key: String, default: Set<String>) = prefs.getStringSet(key, default)?.toSet() ?: default
    override fun edit(block: PrefsEditor.() -> Unit) {
        prefs.edit(commit = true) { SharedPrefsEditor(this).block() }
    }
}

private class SharedPrefsEditor(private val editor: SharedPreferences.Editor) : PrefsEditor {
    override fun putBoolean(key: String, value: Boolean) { editor.putBoolean(key, value) }
    override fun putInt(key: String, value: Int) { editor.putInt(key, value) }
    override fun putLong(key: String, value: Long) { editor.putLong(key, value) }
    override fun putString(key: String, value: String) { editor.putString(key, value) }
    override fun putStringSet(key: String, value: Set<String>) { editor.putStringSet(key, value.toMutableSet()) }
    override fun remove(key: String) { editor.remove(key) }
}

class ProgramStateStore internal constructor(
    private val prefs: PrefsStore,
    private val legacyPrefs: PrefsStore,
    private val targetAuthority: ProgramAuthority = ProgramAuthority.V73,
) {
    constructor(context: Context) : this(
        SharedPrefsStore(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)),
        SharedPrefsStore(context.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)),
    )

    fun load(): ProgramUiState {
        if (!prefs.contains(KEY_INITIALIZED) && legacyPrefs.getBoolean(LEGACY_KEY_INITIALIZED, false)) {
            val oldWeek = legacyPrefs.getInt(KEY_CYCLE, 2).coerceIn(1, 32)
            val oldDay = legacyPrefs.getInt(KEY_DAY, 3).coerceIn(1, 8)
            val pointer = if (oldDay == 8) ProgramUiState(true, (oldWeek + 1).coerceAtMost(32), 1)
            else ProgramUiState(true, oldWeek, oldDay.coerceIn(1, 7))
            val legacyEvidence = if (oldDay == 8 && oldWeek < 32) WeekStartEvidence.NOT_STARTED else WeekStartEvidence.UNKNOWN
            val migrated = AuthorityMigration.apply(pointer, workoutActive = false, legacyEvidence)
            return migrateV73(migrated, false, legacyEvidence)
        }
        val stored = readState()
        val revision = prefs.getInt(KEY_MIGRATION_REVISION, 0)
        if (prefs.getBoolean(KEY_V72_MIGRATED, false) && revision >= AuthorityMigration.CURRENT_REVISION) {
            return migrateV73(stored, prefs.getBoolean(KEY_WORKOUT_ACTIVE, false), currentEvidence(stored.currentWeek))
        }
        val workoutActive = prefs.getBoolean(KEY_WORKOUT_ACTIVE, false) && prefs.getInt(KEY_WORKOUT_SCHEMA, 0) == WORKOUT_SCHEMA
        val evidence = AuthorityMigration.evidenceFor(
            recordPresent = prefs.contains(KEY_ENGAGED_ABSOLUTE_DAY),
            engagedAbsoluteDay = prefs.getInt(KEY_ENGAGED_ABSOLUTE_DAY, 0),
            week = stored.currentWeek,
        )
        val migrated = if (prefs.getBoolean(KEY_V72_MIGRATED, false)) {
            AuthorityMigration.reconcileAlreadyMigrated(stored, workoutActive, evidence)
        } else {
            AuthorityMigration.apply(stored, workoutActive, evidence)
        }
        return migrateV73(migrated, workoutActive, evidence)
    }

    private fun currentEvidence(week: Int) = AuthorityMigration.evidenceFor(
        prefs.contains(KEY_ENGAGED_ABSOLUTE_DAY), prefs.getInt(KEY_ENGAGED_ABSOLUTE_DAY, 0), week)

    private fun migrateV73(state: ProgramUiState, workoutActive: Boolean, evidence: WeekStartEvidence): ProgramUiState {
        val migrated = if (targetAuthority == ProgramAuthority.V73) V73AuthorityMigration.apply(state, workoutActive, evidence,
            prefs.getInt(KEY_ENGAGED_ABSOLUTE_DAY, 0).takeIf { it in 1..224 }?.let { (it - 1) / 7 + 1 }) else state
        if (migrated != state || !prefs.contains(KEY_MIGRATION_REVISION)) save(migrated, replaceHistoricalAuthorities = true)
        else if (targetAuthority != ProgramAuthority.V73) save(migrated, replaceHistoricalAuthorities = true)
        return migrated
    }

    /**
     * Ordinary saves keep week-authority assignments that [state] does not mention.
     * A later pointer, timer, or workout save must not erase a week recorded earlier.
     * Reset and migration pass [replaceHistoricalAuthorities] so an explicit clear is still possible.
     */
    fun save(state: ProgramUiState, replaceHistoricalAuthorities: Boolean = false) {
        val history = if (replaceHistoricalAuthorities) state.historicalAuthorities
            else readState().historicalAuthorities + state.historicalAuthorities
        prefs.edit {
            putBoolean(KEY_INITIALIZED, state.initialized)
            putInt(KEY_CYCLE, state.currentWeek)
            putInt(KEY_DAY, state.currentDay)
            putString(KEY_AUTHORITY, state.authority.name)
            putBoolean(KEY_V73_MIGRATED, state.v73MigrationApplied)
            if (state.v73ActivationWeek != null) putInt(KEY_V73_ACTIVATION_WEEK, state.v73ActivationWeek) else remove(KEY_V73_ACTIVATION_WEEK)
            putStringSet(KEY_HISTORY, history.mapTo(linkedSetOf()) { (week, authority) -> "$week:${authority.name}" })
            putBoolean(KEY_V72_MIGRATED, true)
            putInt(KEY_MIGRATION_REVISION, AuthorityMigration.CURRENT_REVISION)
            putBoolean(KEY_V72_ACTIVATION_PROVEN, state.v72ActivationProven)
            if (state.retainedV71Week != null) putInt(KEY_RETAINED_V71_WEEK, state.retainedV71Week) else remove(KEY_RETAINED_V71_WEEK)
            if (state.restTimerSeconds != null) putInt(KEY_REST, state.restTimerSeconds) else remove(KEY_REST)
            if (state.utilityRestTimerEndEpochMillis != null) putLong(KEY_TIMER_END, state.utilityRestTimerEndEpochMillis) else remove(KEY_TIMER_END)
        }
    }

    fun loadWorkout(): ActiveWorkoutState? {
        if (!prefs.getBoolean(KEY_WORKOUT_ACTIVE, false) || prefs.getInt(KEY_WORKOUT_SCHEMA, 0) != WORKOUT_SCHEMA) return null
        val hasUndo = prefs.getBoolean(KEY_HAS_UNDO, false)
        return ActiveWorkoutState(
            authority = prefs.getString(KEY_WORKOUT_AUTHORITY, null)?.let { runCatching { ProgramAuthority.valueOf(it) }.getOrNull() }
                ?: load().authorityForWeek(prefs.getInt(KEY_WORKOUT_WEEK, 1).coerceIn(1,32)),
            week = prefs.getInt(KEY_WORKOUT_WEEK, 1).coerceIn(1, 32),
            day = prefs.getInt(KEY_WORKOUT_DAY, 1).coerceIn(1, 7),
            movementIndex = prefs.getInt(KEY_MOVEMENT, 0).coerceAtLeast(0),
            effortIndex = prefs.getInt(KEY_EFFORT, 1).coerceAtLeast(1),
            complete = prefs.getBoolean(KEY_COMPLETE, false),
            selectedSubstitutions = decodeSubstitutions(prefs.getStringSet(KEY_SUBSTITUTIONS, emptySet())),
            completedEffortIds = emptySet(),
            restTimerEndEpochMillis = prefs.getLong(KEY_WORKOUT_TIMER, 0L).takeIf { it > 0L },
            undoMovementIndex = if (hasUndo) prefs.getInt(KEY_UNDO_MOVEMENT, 0) else null,
            undoEffortIndex = if (hasUndo) prefs.getInt(KEY_UNDO_EFFORT, 1) else null,
            undoComplete = if (hasUndo) prefs.getBoolean(KEY_UNDO_COMPLETE, false) else null,
            undoRestTimerEndEpochMillis = if (hasUndo) prefs.getLong(KEY_UNDO_TIMER, 0L).takeIf { it > 0L } else null,
            undoCompletedEffortIds = if (hasUndo) prefs.getStringSet(KEY_UNDO_COMPLETED, emptySet()).toSet() else null,
        )
    }

    fun markSessionEngaged(week: Int, day: Int): ProgramUiState {
        require(week in 1..32)
        require(day in 1..7)
        val state = load()
        val recorded = if (state.v73MigrationApplied && week !in state.historicalAuthorities) {
            state.copy(historicalAuthorities = state.historicalAuthorities + (week to state.authorityForWeek(week)))
        } else state
        if (recorded != state) save(recorded)
        val absolute = (week - 1) * 7 + day
        val existing = prefs.getInt(KEY_ENGAGED_ABSOLUTE_DAY, 0)
        if (!prefs.contains(KEY_ENGAGED_ABSOLUTE_DAY) || absolute > existing) {
            prefs.edit { putInt(KEY_ENGAGED_ABSOLUTE_DAY, absolute) }
        }
        return recorded
    }

    fun clearSessionStatus() {
        prefs.edit { remove(KEY_ENGAGED_ABSOLUTE_DAY) }
    }

    fun saveWorkout(s: ActiveWorkoutState): ProgramUiState {
        val recorded = markSessionEngaged(s.week, s.day)
        prefs.edit {
            putBoolean(KEY_WORKOUT_ACTIVE, true)
            putString(KEY_WORKOUT_AUTHORITY, (s.authority ?: load().authorityForWeek(s.week)).name)
            putInt(KEY_WORKOUT_SCHEMA, WORKOUT_SCHEMA)
            putInt(KEY_WORKOUT_WEEK, s.week)
            putInt(KEY_WORKOUT_DAY, s.day)
            putInt(KEY_MOVEMENT, 0)
            putInt(KEY_EFFORT, 1)
            putBoolean(KEY_COMPLETE, s.complete)
            putStringSet(KEY_SUBSTITUTIONS, encodeSubstitutions(s.selectedSubstitutions))
            remove(KEY_COMPLETED)
            putBoolean(KEY_HAS_UNDO, false)
            if (s.restTimerEndEpochMillis != null) putLong(KEY_WORKOUT_TIMER, s.restTimerEndEpochMillis) else remove(KEY_WORKOUT_TIMER)
            listOf(KEY_UNDO_MOVEMENT, KEY_UNDO_EFFORT, KEY_UNDO_COMPLETE, KEY_UNDO_COMPLETED, KEY_UNDO_TIMER).forEach(::remove)
        }
        return recorded
    }

    fun clearWorkout() {
        prefs.edit {
            WORKOUT_KEYS.forEach(::remove)
        }
    }

    private fun readState(): ProgramUiState = ProgramUiState(
        initialized = prefs.getBoolean(KEY_INITIALIZED, false),
        currentWeek = prefs.getInt(KEY_CYCLE, 1).coerceIn(1, 32),
        currentDay = prefs.getInt(KEY_DAY, 1).coerceIn(1, 7),
        restTimerSeconds = prefs.getInt(KEY_REST, 0).takeIf { it > 0 },
        utilityRestTimerEndEpochMillis = prefs.getLong(KEY_TIMER_END, 0L).takeIf { it > 0L },
        authority = runCatching {
            ProgramAuthority.valueOf(prefs.getString(KEY_AUTHORITY, ProgramAuthority.V72.name).orEmpty())
        }.getOrDefault(ProgramAuthority.V72),
        retainedV71Week = prefs.getInt(KEY_RETAINED_V71_WEEK, 0).takeIf { it in 1..32 },
        v72ActivationProven = prefs.getBoolean(KEY_V72_ACTIVATION_PROVEN, false),
        v73MigrationApplied = prefs.getBoolean(KEY_V73_MIGRATED, false),
        v73ActivationWeek = prefs.getInt(KEY_V73_ACTIVATION_WEEK, 0).takeIf { it in 1..32 },
        historicalAuthorities = prefs.getStringSet(KEY_HISTORY, emptySet()).mapNotNull { entry ->
            val parts = entry.split(':')
            val week = parts.firstOrNull()?.toIntOrNull()?.takeIf { it in 1..32 }
            val authority = parts.getOrNull(1)?.let { runCatching { ProgramAuthority.valueOf(it) }.getOrNull() }
            if (week != null && authority != null) week to authority else null
        }.toMap(),
    )

    private fun encodeSubstitutions(v: Map<String, String>) = v.mapTo(linkedSetOf()) { (k, x) -> k + SEP + x }
    private fun decodeSubstitutions(v: Set<String>) = v.mapNotNull { x ->
        x.indexOf(SEP).takeIf { it > 0 && it < x.lastIndex }?.let { x.substring(0, it) to x.substring(it + SEP.length) }
    }.toMap()

    companion object {
        private const val PREFS_NAME = "ares_v71_reference_state"
        private const val LEGACY_PREFS_NAME = "ares_v70_reference_state"
        private const val LEGACY_KEY_INITIALIZED = "v70Initialized"
        private const val KEY_INITIALIZED = "v71Initialized"
        private const val KEY_CYCLE = "currentWeek"
        private const val KEY_DAY = "currentDay"
        private const val KEY_AUTHORITY = "authorityVersion"
        private const val KEY_V73_MIGRATED = "v73MigrationApplied"
        private const val KEY_V73_ACTIVATION_WEEK = "v73ActivationWeek"
        private const val KEY_HISTORY = "historicalWeekAuthorities"
        private const val KEY_WORKOUT_AUTHORITY = "workoutAuthority"
        private const val KEY_RETAINED_V71_WEEK = "retainedV71Week"
        private const val KEY_V72_MIGRATED = "v72MigrationApplied"
        private const val KEY_MIGRATION_REVISION = "v72MigrationRevision"
        private const val KEY_V72_ACTIVATION_PROVEN = "v72ActivationProven"
        private const val KEY_ENGAGED_ABSOLUTE_DAY = "engagedAbsoluteDay"
        private const val KEY_REST = "manualRestTimerSeconds"
        private const val KEY_TIMER_END = "utilityRestTimerEndEpochMillis"
        private const val WORKOUT_SCHEMA = 5
        private const val KEY_WORKOUT_ACTIVE = "workoutActive"
        private const val KEY_WORKOUT_SCHEMA = "workoutSchema"
        private const val KEY_WORKOUT_WEEK = "workoutWeek"
        private const val KEY_WORKOUT_DAY = "workoutDay"
        private const val KEY_MOVEMENT = "movementIndex"
        private const val KEY_EFFORT = "effortIndex"
        private const val KEY_COMPLETE = "complete"
        private const val KEY_SUBSTITUTIONS = "substitutions"
        private const val KEY_COMPLETED = "completedEfforts"
        private const val KEY_WORKOUT_TIMER = "workoutTimerEnd"
        private const val KEY_HAS_UNDO = "hasUndo"
        private const val KEY_UNDO_MOVEMENT = "undoMovement"
        private const val KEY_UNDO_EFFORT = "undoEffort"
        private const val KEY_UNDO_COMPLETE = "undoComplete"
        private const val KEY_UNDO_TIMER = "undoTimer"
        private const val KEY_UNDO_COMPLETED = "undoCompleted"
        private const val SEP = "\u001F"
        private val WORKOUT_KEYS = listOf(
            KEY_WORKOUT_ACTIVE, KEY_WORKOUT_SCHEMA, KEY_WORKOUT_WEEK, KEY_WORKOUT_DAY, KEY_MOVEMENT, KEY_EFFORT,
            KEY_COMPLETE, KEY_WORKOUT_AUTHORITY, KEY_SUBSTITUTIONS, KEY_COMPLETED, KEY_WORKOUT_TIMER, KEY_HAS_UNDO, KEY_UNDO_MOVEMENT,
            KEY_UNDO_EFFORT, KEY_UNDO_COMPLETE, KEY_UNDO_TIMER, KEY_UNDO_COMPLETED,
        )
    }
}
