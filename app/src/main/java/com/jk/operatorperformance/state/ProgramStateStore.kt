package com.jk.operatorperformance.state

import android.content.Context
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
) {
    init {
        require(currentWeek in 1..32) { "currentWeek must be 1–32." }
        require(currentDay in 1..7) { "currentDay must be 1–7." }
        require(restTimerSeconds == null || restTimerSeconds > 0)
        require(utilityRestTimerEndEpochMillis == null || utilityRestTimerEndEpochMillis > 0L)
        require(retainedV71Week == null || retainedV71Week in 1..32)
    }
    fun authorityForWeek(week: Int): ProgramAuthority =
        if (retainedV71Week != null && week == retainedV71Week) ProgramAuthority.V71 else ProgramAuthority.V72

    fun startV71() = copy(initialized=true,currentWeek=2,currentDay=3)
    fun startV72() = copy(initialized=true, currentWeek=1, currentDay=1, authority=ProgramAuthority.V72, retainedV71Week=null)
    fun advanceDay(): ProgramUiState = when {
        currentDay < 7 -> copy(currentDay = currentDay + 1)
        currentWeek < 32 -> {
            val leavingRetainedWeek = retainedV71Week == currentWeek
            copy(
                currentWeek = currentWeek + 1,
                currentDay = 1,
                authority = if (leavingRetainedWeek) ProgramAuthority.V72 else authority,
                retainedV71Week = if (leavingRetainedWeek) null else retainedV71Week,
            )
        }
        else -> this
    }
    fun withCurrentPointer(week:Int,day:Int):ProgramUiState {
        require(week in 1..32); require(day in 1..7)
        val stillRetained = retainedV71Week == week
        return copy(
            currentWeek = week,
            currentDay = day,
            authority = if (stillRetained) ProgramAuthority.V71 else ProgramAuthority.V72,
            retainedV71Week = if (stillRetained) retainedV71Week else null,
        )
    }
    fun resetToV72Start() = copy(initialized = true, currentWeek = 1, currentDay = 1, authority = ProgramAuthority.V72, retainedV71Week = null)
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
            return state.copy(currentWeek = 1, currentDay = 1, authority = ProgramAuthority.V72, retainedV71Week = null)
        }
        val provenUnstarted = state.currentDay == 1 && !workoutActive && evidence == WeekStartEvidence.NOT_STARTED
        return if (provenUnstarted) {
            state.copy(authority = ProgramAuthority.V72, retainedV71Week = null)
        } else {
            state.copy(authority = ProgramAuthority.V71, retainedV71Week = state.currentWeek)
        }
    }

    fun evidenceFor(recordPresent: Boolean, engagedAbsoluteDay: Int, week: Int): WeekStartEvidence {
        if (!recordPresent) return WeekStartEvidence.UNKNOWN
        val weekStart = (week - 1) * 7 + 1
        return if (engagedAbsoluteDay >= weekStart) WeekStartEvidence.ENGAGED else WeekStartEvidence.NOT_STARTED
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

class ProgramStateStore(context:Context) {
    private val prefs=context.getSharedPreferences(PREFS_NAME,Context.MODE_PRIVATE)
    private val legacyPrefs=context.getSharedPreferences(LEGACY_PREFS_NAME,Context.MODE_PRIVATE)

    fun load():ProgramUiState {
        if (!prefs.contains(KEY_INITIALIZED) && legacyPrefs.getBoolean(LEGACY_KEY_INITIALIZED,false)) {
            val oldWeek=legacyPrefs.getInt(KEY_CYCLE,2).coerceIn(1,32)
            val oldDay=legacyPrefs.getInt(KEY_DAY,3).coerceIn(1,8)
            val pointer=if(oldDay==8) ProgramUiState(true,(oldWeek+1).coerceAtMost(32),1)
                else ProgramUiState(true,oldWeek,oldDay.coerceIn(1,7))
            val legacyEvidence=if(oldDay==8 && oldWeek<32) WeekStartEvidence.NOT_STARTED else WeekStartEvidence.UNKNOWN
            val migrated=AuthorityMigration.apply(pointer, workoutActive=false, legacyEvidence)
            save(migrated)
            return migrated
        }
        val stored = ProgramUiState(
            initialized=prefs.getBoolean(KEY_INITIALIZED,false),
            currentWeek=prefs.getInt(KEY_CYCLE,1).coerceIn(1,32),
            currentDay=prefs.getInt(KEY_DAY,1).coerceIn(1,7),
            restTimerSeconds=prefs.getInt(KEY_REST,0).takeIf { it>0 },
            utilityRestTimerEndEpochMillis=prefs.getLong(KEY_TIMER_END,0L).takeIf { it>0L },
            authority=runCatching { ProgramAuthority.valueOf(prefs.getString(KEY_AUTHORITY, ProgramAuthority.V72.name).orEmpty()) }.getOrDefault(ProgramAuthority.V72),
            retainedV71Week=prefs.getInt(KEY_RETAINED_V71_WEEK, 0).takeIf { it in 1..32 },
        )
        if (prefs.getBoolean(KEY_V72_MIGRATED, false)) return stored
        val workoutActive = prefs.getBoolean(KEY_WORKOUT_ACTIVE, false) && prefs.getInt(KEY_WORKOUT_SCHEMA, 0) == WORKOUT_SCHEMA
        val evidence = AuthorityMigration.evidenceFor(
            recordPresent = prefs.contains(KEY_ENGAGED_ABSOLUTE_DAY),
            engagedAbsoluteDay = prefs.getInt(KEY_ENGAGED_ABSOLUTE_DAY, 0),
            week = stored.currentWeek,
        )
        val migrated = AuthorityMigration.apply(stored, workoutActive, evidence)
        save(migrated)
        return migrated
    }
    fun save(state:ProgramUiState) {
        val e=prefs.edit().putBoolean(KEY_INITIALIZED,state.initialized).putInt(KEY_CYCLE,state.currentWeek).putInt(KEY_DAY,state.currentDay)
            .putString(KEY_AUTHORITY, state.authority.name)
            .putBoolean(KEY_V72_MIGRATED, true)
        if (state.retainedV71Week != null) e.putInt(KEY_RETAINED_V71_WEEK, state.retainedV71Week) else e.remove(KEY_RETAINED_V71_WEEK)
        state.restTimerSeconds?.let { e.putInt(KEY_REST,it) } ?: e.remove(KEY_REST)
        state.utilityRestTimerEndEpochMillis?.let { e.putLong(KEY_TIMER_END,it) } ?: e.remove(KEY_TIMER_END)
        e.apply()
    }
    fun loadWorkout():ActiveWorkoutState? {
        if(!prefs.getBoolean(KEY_WORKOUT_ACTIVE,false) || prefs.getInt(KEY_WORKOUT_SCHEMA,0)!=WORKOUT_SCHEMA) return null
        val hasUndo=prefs.getBoolean(KEY_HAS_UNDO,false)
        return ActiveWorkoutState(
            week=prefs.getInt(KEY_WORKOUT_WEEK,1).coerceIn(1,32),day=prefs.getInt(KEY_WORKOUT_DAY,1).coerceIn(1,7),
            movementIndex=prefs.getInt(KEY_MOVEMENT,0).coerceAtLeast(0),effortIndex=prefs.getInt(KEY_EFFORT,1).coerceAtLeast(1),
            complete=prefs.getBoolean(KEY_COMPLETE,false),selectedSubstitutions=decodeSubstitutions(prefs.getStringSet(KEY_SUBSTITUTIONS,emptySet()).orEmpty()),
            completedEffortIds=emptySet(),restTimerEndEpochMillis=prefs.getLong(KEY_WORKOUT_TIMER,0L).takeIf { it>0L },
            undoMovementIndex=if(hasUndo)prefs.getInt(KEY_UNDO_MOVEMENT,0) else null,undoEffortIndex=if(hasUndo)prefs.getInt(KEY_UNDO_EFFORT,1) else null,
            undoComplete=if(hasUndo)prefs.getBoolean(KEY_UNDO_COMPLETE,false) else null,undoRestTimerEndEpochMillis=if(hasUndo)prefs.getLong(KEY_UNDO_TIMER,0L).takeIf { it>0L } else null,
            undoCompletedEffortIds=if(hasUndo)prefs.getStringSet(KEY_UNDO_COMPLETED,emptySet()).orEmpty().toSet() else null)
    }
    fun markSessionEngaged(week: Int, day: Int) {
        require(week in 1..32)
        require(day in 1..7)
        val absolute = (week - 1) * 7 + day
        val existing = prefs.getInt(KEY_ENGAGED_ABSOLUTE_DAY, 0)
        if (!prefs.contains(KEY_ENGAGED_ABSOLUTE_DAY) || absolute > existing) {
            prefs.edit { putInt(KEY_ENGAGED_ABSOLUTE_DAY, absolute) }
        }
    }

    fun clearSessionStatus() {
        prefs.edit { remove(KEY_ENGAGED_ABSOLUTE_DAY) }
    }

    fun saveWorkout(s:ActiveWorkoutState) {
        markSessionEngaged(s.week, s.day)
        prefs.edit {
            putBoolean(KEY_WORKOUT_ACTIVE, true)
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
    }
    fun clearWorkout() {
        val editor=prefs.edit()
        WORKOUT_KEYS.forEach(editor::remove)
        editor.apply()
    }
    private fun encodeSubstitutions(v:Map<String,String>)=v.mapTo(linkedSetOf()) { (k,x) -> k+SEP+x }
    private fun decodeSubstitutions(v:Set<String>)=v.mapNotNull { x -> x.indexOf(SEP).takeIf { it>0 && it<x.lastIndex }?.let { x.substring(0,it) to x.substring(it+SEP.length) } }.toMap()
    companion object {
        private const val PREFS_NAME="ares_v71_reference_state"
        private const val LEGACY_PREFS_NAME="ares_v70_reference_state"
        private const val LEGACY_KEY_INITIALIZED="v70Initialized"
        private const val KEY_INITIALIZED="v71Initialized"; private const val KEY_CYCLE="currentWeek"; private const val KEY_DAY="currentDay"
        private const val KEY_AUTHORITY="authorityVersion"; private const val KEY_RETAINED_V71_WEEK="retainedV71Week"; private const val KEY_V72_MIGRATED="v72MigrationApplied"
        private const val KEY_ENGAGED_ABSOLUTE_DAY="engagedAbsoluteDay"
        private const val KEY_REST="manualRestTimerSeconds"; private const val KEY_TIMER_END="utilityRestTimerEndEpochMillis"
        private const val WORKOUT_SCHEMA=5; private const val KEY_WORKOUT_ACTIVE="workoutActive"; private const val KEY_WORKOUT_SCHEMA="workoutSchema"
        private const val KEY_WORKOUT_WEEK="workoutWeek"; private const val KEY_WORKOUT_DAY="workoutDay"; private const val KEY_MOVEMENT="movementIndex"
        private const val KEY_EFFORT="effortIndex"; private const val KEY_COMPLETE="complete"; private const val KEY_SUBSTITUTIONS="substitutions"
        private const val KEY_COMPLETED="completedEfforts"; private const val KEY_WORKOUT_TIMER="workoutTimerEnd"; private const val KEY_HAS_UNDO="hasUndo"
        private const val KEY_UNDO_MOVEMENT="undoMovement"; private const val KEY_UNDO_EFFORT="undoEffort"; private const val KEY_UNDO_COMPLETE="undoComplete"
        private const val KEY_UNDO_TIMER="undoTimer"; private const val KEY_UNDO_COMPLETED="undoCompleted"; private const val SEP="\u001F"
        private val WORKOUT_KEYS=listOf(KEY_WORKOUT_ACTIVE,KEY_WORKOUT_SCHEMA,KEY_WORKOUT_WEEK,KEY_WORKOUT_DAY,KEY_MOVEMENT,KEY_EFFORT,KEY_COMPLETE,KEY_SUBSTITUTIONS,KEY_COMPLETED,KEY_WORKOUT_TIMER,KEY_HAS_UNDO,KEY_UNDO_MOVEMENT,KEY_UNDO_EFFORT,KEY_UNDO_COMPLETE,KEY_UNDO_TIMER,KEY_UNDO_COMPLETED)
    }
}
