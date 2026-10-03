package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.ProgramAuthority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramStateStorePersistenceTest {
    @Test fun freshInstallStaysUninitializedOnV72() {
        val (prefs, store) = store()
        val loaded = store.load()
        assertFalse(loaded.initialized)
        assertEquals(1, loaded.currentWeek)
        assertEquals(1, loaded.currentDay)
        assertEquals(ProgramAuthority.V72, loaded.authority)
        assertNull(loaded.retainedV71Week)
        assertEquals(AuthorityMigration.CURRENT_REVISION, prefs.snapshot()["v72MigrationRevision"])
        assertEquals(loaded, store.load())
    }

    @Test fun unstartedInitializedDay1ActivatesV72() {
        val (prefs, store) = store()
        prefs.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 5)
            putInt("currentDay", 1)
            putInt("engagedAbsoluteDay", 1)
        }
        val loaded = store.load()
        assertEquals(5, loaded.currentWeek)
        assertEquals(1, loaded.currentDay)
        assertEquals(ProgramAuthority.V72, loaded.authority)
        assertNull(loaded.retainedV71Week)
        assertTrue(loaded.v72ActivationProven)
    }

    @Test fun activeDay1RetainsV71() {
        val (prefs, store) = store()
        prefs.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 1)
            putInt("currentDay", 1)
            putBoolean("workoutActive", true)
            putInt("workoutSchema", 5)
        }
        val loaded = store.load()
        assertEquals(1, loaded.currentWeek)
        assertEquals(1, loaded.currentDay)
        assertEquals(ProgramAuthority.V71, loaded.authority)
        assertEquals(1, loaded.retainedV71Week)
        assertTrue(store.loadWorkout() != null)
    }

    @Test fun completedDay1BeforePointerAdvanceRetainsV71() {
        val (prefs, store) = store()
        prefs.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 6)
            putInt("currentDay", 1)
            putBoolean("workoutActive", true)
            putInt("workoutSchema", 5)
            putBoolean("complete", true)
            putInt("engagedAbsoluteDay", 36)
        }
        val loaded = store.load()
        assertEquals(6, loaded.currentWeek)
        assertEquals(1, loaded.currentDay)
        assertEquals(ProgramAuthority.V71, loaded.authority)
        assertEquals(6, loaded.retainedV71Week)
        assertEquals(true, store.loadWorkout()?.complete)
    }

    @Test fun resetDay1RetainsV71() {
        val (prefs, store) = store()
        prefs.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 6)
            putInt("currentDay", 1)
            putInt("engagedAbsoluteDay", 36)
        }
        val loaded = store.load()
        assertEquals(6, loaded.currentWeek)
        assertEquals(1, loaded.currentDay)
        assertEquals(ProgramAuthority.V71, loaded.authority)
        assertEquals(6, loaded.retainedV71Week)
        assertNull(store.loadWorkout())
    }

    @Test fun day2OrLaterRetainsV71() {
        val (prefs, store) = store()
        prefs.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 6)
            putInt("currentDay", 2)
        }
        val loaded = store.load()
        assertEquals(ProgramAuthority.V71, loaded.authority)
        assertEquals(6, loaded.retainedV71Week)
        assertEquals(2, loaded.currentDay)
    }

    @Test fun alreadyMigratedLegacyDay1IsRepairedOnce() {
        val (prefs, store) = store()
        prefs.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 5)
            putInt("currentDay", 1)
            putString("authorityVersion", ProgramAuthority.V72.name)
            putBoolean("v72MigrationApplied", true)
        }
        val repaired = store.load()
        assertEquals(ProgramAuthority.V71, repaired.authority)
        assertEquals(5, repaired.retainedV71Week)
        assertEquals(1, repaired.currentDay)
        assertEquals(AuthorityMigration.CURRENT_REVISION, prefs.snapshot()["v72MigrationRevision"])
        val again = store.load()
        assertEquals(repaired, again)
    }

    @Test fun activeV71Week32DoesNotCreateWeek33() {
        val (prefs, store) = store()
        prefs.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 32)
            putInt("currentDay", 6)
            putString("authorityVersion", ProgramAuthority.V71.name)
            putInt("retainedV71Week", 32)
            putBoolean("v72MigrationApplied", true)
            putBoolean("workoutActive", true)
            putInt("workoutSchema", 5)
            putInt("workoutWeek", 32)
            putInt("workoutDay", 6)
        }
        val loaded = store.load()
        assertEquals(32, loaded.currentWeek)
        assertEquals(6, loaded.currentDay)
        assertEquals(ProgramAuthority.V71, loaded.authority)
        assertEquals(32, loaded.retainedV71Week)
        val end = loaded.copy(currentDay = 7)
        assertEquals(end, end.advanceDay())
    }

    @Test fun manualRestTimerRoundTripsAndResetClearsIt() {
        val (prefs, store) = store()
        val end = 1_700_000_000_000L
        val state = ProgramUiState(
            initialized = true,
            currentWeek = 2,
            currentDay = 3,
            authority = ProgramAuthority.V72,
            v72ActivationProven = true,
            restTimerSeconds = 90,
            utilityRestTimerEndEpochMillis = end,
        )
        store.save(state)
        val reloaded = ProgramStateStore(prefs, MemoryPrefs()).load()
        assertEquals(90, reloaded.restTimerSeconds)
        assertEquals(end, reloaded.utilityRestTimerEndEpochMillis)

        val session = WorkoutProgress.startManualRest(WorkoutProgress.start(state), 120, end)
        store.saveWorkout(session.copy(selectedSubstitutions = mapOf("d2_squat" to "Smith squat")))
        val workout = ProgramStateStore(prefs, MemoryPrefs()).loadWorkout()
        assertEquals(end + 120_000L, workout?.restTimerEndEpochMillis)
        assertEquals("Smith squat", workout?.selectedSubstitutions?.get("d2_squat"))

        store.saveWorkout(WorkoutProgress.skipRest(workout!!))
        assertNull(store.loadWorkout()?.restTimerEndEpochMillis)
        assertFalse(prefs.snapshot().containsKey("workoutTimerEnd"))

        store.save(reloaded.copy(restTimerSeconds = null, utilityRestTimerEndEpochMillis = null))
        val cleared = store.load()
        assertNull(cleared.restTimerSeconds)
        assertNull(cleared.utilityRestTimerEndEpochMillis)
        assertFalse(prefs.snapshot().containsKey("manualRestTimerSeconds"))
        assertFalse(prefs.snapshot().containsKey("utilityRestTimerEndEpochMillis"))
    }

    @Test fun activeSessionResetKeepsEngagementAndDropsTheTimer() {
        val (prefs, store) = store()
        val state = ProgramUiState(initialized = true, currentWeek = 4, currentDay = 1, authority = ProgramAuthority.V72, v72ActivationProven = true)
        store.save(state.copy(utilityRestTimerEndEpochMillis = 1_800_000_000_000L, restTimerSeconds = 60))
        store.saveWorkout(WorkoutProgress.startManualRest(WorkoutProgress.start(state), 90, 1_800_000_000_000L))
        assertEquals(22, prefs.snapshot()["engagedAbsoluteDay"])
        store.clearWorkout()
        assertNull(store.loadWorkout())
        assertEquals(22, prefs.snapshot()["engagedAbsoluteDay"])
        assertFalse(prefs.snapshot().containsKey("workoutTimerEnd"))
        assertFalse(prefs.snapshot().containsKey("workoutActive"))
        assertEquals(1_800_000_000_000L, store.load().utilityRestTimerEndEpochMillis)
        assertEquals(60, store.load().restTimerSeconds)
    }

    @Test fun completedSessionDropsTimerEffortsAndResultFields() {
        val (prefs, store) = store()
        val state = ProgramUiState(initialized = true, currentWeek = 1, currentDay = 1, authority = ProgramAuthority.V72, v72ActivationProven = true)
        store.save(state)
        val dirty = WorkoutProgress.start(state).copy(
            completedEffortIds = setOf("d1_bench:1"),
            restTimerEndEpochMillis = 1_700_000_100_000L,
            undoMovementIndex = 0,
            undoEffortIndex = 1,
            undoComplete = false,
            undoCompletedEffortIds = setOf("stale"),
        )
        store.saveWorkout(WorkoutProgress.markSessionComplete(dirty))
        val loaded = store.loadWorkout()
        assertEquals(true, loaded?.complete)
        assertTrue(loaded!!.completedEffortIds.isEmpty())
        assertNull(loaded.restTimerEndEpochMillis)
        assertFalse(loaded.canUndo)
        val keys = prefs.snapshot().keys
        listOf(
            "completedEfforts", "workoutTimerEnd", "undoCompleted", "undoMovement",
            "bodyweight", "calories", "rirActual", "paceResult", "reps", "load",
        ).forEach { prohibited ->
            assertFalse(prohibited, keys.contains(prohibited))
        }
    }

    private fun store(): Pair<MemoryPrefs, ProgramStateStore> {
        val prefs = MemoryPrefs()
        return prefs to ProgramStateStore(prefs, MemoryPrefs())
    }
}

internal class MemoryPrefs : PrefsStore {
    private val values = linkedMapOf<String, Any>()

    fun snapshot(): Map<String, Any> = values.toMap()

    override fun contains(key: String) = values.containsKey(key)
    override fun getBoolean(key: String, default: Boolean) = values[key] as? Boolean ?: default
    override fun getInt(key: String, default: Int) = values[key] as? Int ?: default
    override fun getLong(key: String, default: Long) = values[key] as? Long ?: default
    override fun getString(key: String, default: String?) = if (values.containsKey(key)) values[key] as String? else default
    override fun getStringSet(key: String, default: Set<String>): Set<String> {
        val stored = values[key] as? Set<*> ?: return default
        return stored.filterIsInstance<String>().toSet()
    }

    override fun edit(block: PrefsEditor.() -> Unit) {
        object : PrefsEditor {
            override fun putBoolean(key: String, value: Boolean) { values[key] = value }
            override fun putInt(key: String, value: Int) { values[key] = value }
            override fun putLong(key: String, value: Long) { values[key] = value }
            override fun putString(key: String, value: String) { values[key] = value }
            override fun putStringSet(key: String, value: Set<String>) { values[key] = value.toSet() }
            override fun remove(key: String) { values.remove(key) }
        }.block()
    }
}
