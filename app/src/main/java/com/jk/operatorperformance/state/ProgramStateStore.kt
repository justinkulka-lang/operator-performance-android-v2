package com.jk.operatorperformance.state

import android.content.Context

data class ProgramUiState(
    val initialized: Boolean = false,
    val currentWeek: Int = 1,
    val currentDay: Int = 1,
    val restTimerSeconds: Int? = null,
    val utilityRestTimerEndEpochMillis: Long? = null,
) {
    init {
        require(currentWeek in 1..32) { "currentWeek must be 1–32." }
        require(currentDay in 1..7) { "currentDay must be 1–7." }
        require(restTimerSeconds == null || restTimerSeconds > 0)
        require(utilityRestTimerEndEpochMillis == null || utilityRestTimerEndEpochMillis > 0L)
    }
    fun startV71() = copy(initialized=true,currentWeek=2,currentDay=3)
    fun advanceDay():ProgramUiState = when {
        currentDay < 7 -> copy(currentDay=currentDay+1)
        currentWeek < 32 -> copy(currentWeek=currentWeek+1,currentDay=1)
        else -> this
    }
    fun withCurrentPointer(week:Int,day:Int):ProgramUiState {
        require(week in 1..32); require(day in 1..7)
        return copy(currentWeek=week,currentDay=day)
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
            val migrated=if(oldDay==8) ProgramUiState(true,(oldWeek+1).coerceAtMost(32),1)
                else ProgramUiState(true,oldWeek,oldDay.coerceIn(1,7))
            save(migrated)
            return migrated
        }
        return ProgramUiState(
            initialized=prefs.getBoolean(KEY_INITIALIZED,false),
            currentWeek=prefs.getInt(KEY_CYCLE,2).coerceIn(1,32),
            currentDay=prefs.getInt(KEY_DAY,3).coerceIn(1,7),
            restTimerSeconds=prefs.getInt(KEY_REST,0).takeIf { it>0 },
            utilityRestTimerEndEpochMillis=prefs.getLong(KEY_TIMER_END,0L).takeIf { it>0L },
        )
    }
    fun save(state:ProgramUiState) {
        val e=prefs.edit().putBoolean(KEY_INITIALIZED,state.initialized).putInt(KEY_CYCLE,state.currentWeek).putInt(KEY_DAY,state.currentDay)
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
            completedEffortIds=prefs.getStringSet(KEY_COMPLETED,emptySet()).orEmpty().toSet(),restTimerEndEpochMillis=prefs.getLong(KEY_WORKOUT_TIMER,0L).takeIf { it>0L },
            undoMovementIndex=if(hasUndo)prefs.getInt(KEY_UNDO_MOVEMENT,0) else null,undoEffortIndex=if(hasUndo)prefs.getInt(KEY_UNDO_EFFORT,1) else null,
            undoComplete=if(hasUndo)prefs.getBoolean(KEY_UNDO_COMPLETE,false) else null,undoRestTimerEndEpochMillis=if(hasUndo)prefs.getLong(KEY_UNDO_TIMER,0L).takeIf { it>0L } else null,
            undoCompletedEffortIds=if(hasUndo)prefs.getStringSet(KEY_UNDO_COMPLETED,emptySet()).orEmpty().toSet() else null)
    }
    fun saveWorkout(s:ActiveWorkoutState) {
        val e=prefs.edit().putBoolean(KEY_WORKOUT_ACTIVE,true).putInt(KEY_WORKOUT_SCHEMA,WORKOUT_SCHEMA).putInt(KEY_WORKOUT_WEEK,s.week).putInt(KEY_WORKOUT_DAY,s.day)
            .putInt(KEY_MOVEMENT,s.movementIndex).putInt(KEY_EFFORT,s.effortIndex).putBoolean(KEY_COMPLETE,s.complete)
            .putStringSet(KEY_SUBSTITUTIONS,encodeSubstitutions(s.selectedSubstitutions)).putStringSet(KEY_COMPLETED,s.completedEffortIds).putBoolean(KEY_HAS_UNDO,s.canUndo)
        s.restTimerEndEpochMillis?.let { e.putLong(KEY_WORKOUT_TIMER,it) } ?: e.remove(KEY_WORKOUT_TIMER)
        if(s.canUndo) {
            e.putInt(KEY_UNDO_MOVEMENT,requireNotNull(s.undoMovementIndex)).putInt(KEY_UNDO_EFFORT,requireNotNull(s.undoEffortIndex))
                .putBoolean(KEY_UNDO_COMPLETE,requireNotNull(s.undoComplete)).putStringSet(KEY_UNDO_COMPLETED,requireNotNull(s.undoCompletedEffortIds))
            s.undoRestTimerEndEpochMillis?.let { e.putLong(KEY_UNDO_TIMER,it) } ?: e.remove(KEY_UNDO_TIMER)
        } else listOf(KEY_UNDO_MOVEMENT,KEY_UNDO_EFFORT,KEY_UNDO_COMPLETE,KEY_UNDO_COMPLETED,KEY_UNDO_TIMER).forEach(e::remove)
        e.apply()
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
