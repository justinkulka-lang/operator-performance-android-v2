package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SetCompletionTest {
    @Test fun tickAndUntick() {
        val (state, session) = dayTwo()
        val squat = squat(session)
        val timed = WorkoutProgress.start(state).copy(restTimerEndEpochMillis = 50_000L)
        val ticked = WorkoutProgress.toggleEffort(timed, squat, 2)
        assertEquals(setOf(WorkoutProgress.effortId(squat.id, 2)), ticked.completedEffortIds)
        assertEquals(50_000L, ticked.restTimerEndEpochMillis)
        val unticked = WorkoutProgress.toggleEffort(ticked, squat, 2)
        assertTrue(unticked.completedEffortIds.isEmpty())
        assertEquals(50_000L, unticked.restTimerEndEpochMillis)
        assertEquals("Set", WorkoutProgress.effortWord(squat.effortLabel))
        assertEquals("Warm-up", WorkoutProgress.effortWord("WARM-UP"))
    }

    @Test fun rowCompletesOnlyWhenEveryTickIsDone() {
        val (state, session) = dayTwo()
        val squat = squat(session)
        assertEquals(4, squat.effortCount)
        var progress = WorkoutProgress.start(state)
        assertFalse(WorkoutProgress.isRowComplete(progress, squat))
        for (index in 1 until squat.effortCount) {
            progress = WorkoutProgress.toggleEffort(progress, squat, index)
            assertEquals(index, WorkoutProgress.completedEffortCount(progress, squat))
            assertFalse(WorkoutProgress.isRowComplete(progress, squat))
        }
        progress = WorkoutProgress.toggleEffort(progress, squat, squat.effortCount)
        assertTrue(WorkoutProgress.isRowComplete(progress, squat))
        progress = WorkoutProgress.toggleEffort(progress, squat, 2)
        assertFalse(WorkoutProgress.isRowComplete(progress, squat))
        assertEquals(3, WorkoutProgress.completedEffortCount(progress, squat))
    }

    @Test fun saveAndLoadRoundTripKeepsTicks() {
        val (prefs, store) = store()
        val (state, session) = dayTwo()
        store.save(state.copy(restTimerSeconds = 90, utilityRestTimerEndEpochMillis = 1_800_000_000_000L))
        val squat = squat(session)
        val saved = WorkoutProgress.start(state).copy(
            completedEffortIds = setOf(WorkoutProgress.effortId(squat.id, 1), WorkoutProgress.effortId(squat.id, 2)),
            selectedSubstitutions = mapOf(squat.id to squat.alternatives.first()),
            restTimerEndEpochMillis = 1_700_000_111_000L,
        )
        store.saveWorkout(saved)
        val loaded = store.loadWorkout()
        assertEquals(saved.completedEffortIds, loaded!!.completedEffortIds)
        assertEquals(squat.alternatives.first(), loaded.selectedSubstitutions[squat.id])
        assertEquals(1_700_000_111_000L, loaded.restTimerEndEpochMillis)
        assertEquals(ProgramAuthority.V74, loaded.authority)
        assertEquals(1, loaded.week)
        assertEquals(2, loaded.day)
        assertFalse(loaded.complete)
        val pointer = store.load()
        assertEquals(1, pointer.currentWeek)
        assertEquals(2, pointer.currentDay)
        assertEquals(ProgramAuthority.V74, pointer.authorityForWeek(1))
        assertEquals(90, pointer.restTimerSeconds)
        assertEquals(1_800_000_000_000L, pointer.utilityRestTimerEndEpochMillis)
        assertTrue(prefs.snapshot().containsKey("completedEfforts"))
    }

    @Test fun savedWorkoutFrom0420WithoutCompletedEffortsLoadsIntact() {
        val (prefs, store) = store()
        val state = ProgramUiState().startV74().copy(
            currentDay = 2,
            restTimerSeconds = 90,
            utilityRestTimerEndEpochMillis = 1_800_000_000_000L,
        )
        store.save(state)
        prefs.edit {
            putBoolean("workoutActive", true)
            putString("workoutAuthority", "V74")
            putInt("workoutSchema", 5)
            putInt("workoutWeek", 1)
            putInt("workoutDay", 2)
            putInt("movementIndex", 4)
            putInt("effortIndex", 2)
            putBoolean("complete", false)
            putStringSet("substitutions", setOf("v74_b1_s01\u001FSmith squat"))
            putLong("workoutTimerEnd", 1_700_000_111_000L)
            putBoolean("hasUndo", false)
            remove("completedEfforts")
        }
        assertFalse(prefs.contains("completedEfforts"))
        val loaded = store.loadWorkout()
        assertTrue(loaded!!.completedEffortIds.isEmpty())
        assertEquals(1, loaded.week)
        assertEquals(2, loaded.day)
        assertEquals(4, loaded.movementIndex)
        assertEquals(2, loaded.effortIndex)
        assertFalse(loaded.complete)
        assertEquals(ProgramAuthority.V74, loaded.authority)
        assertEquals("Smith squat", loaded.selectedSubstitutions["v74_b1_s01"])
        assertEquals(1_700_000_111_000L, loaded.restTimerEndEpochMillis)
        assertFalse(loaded.canUndo)
        val pointer = store.load()
        assertEquals(1, pointer.currentWeek)
        assertEquals(2, pointer.currentDay)
        assertEquals(ProgramAuthority.V74, pointer.authorityForWeek(1))
        assertEquals(1, pointer.v74Block)
        assertEquals(1, pointer.v74BlockWeek)
        assertEquals(90, pointer.restTimerSeconds)
        assertEquals(1_800_000_000_000L, pointer.utilityRestTimerEndEpochMillis)
        assertFalse(prefs.contains("completedEfforts"))
    }

    @Test fun substitutionAndTimerActionsKeepTicks() {
        val (state, session) = dayTwo()
        val squat = squat(session)
        val ticks = setOf(WorkoutProgress.effortId(squat.id, 1), WorkoutProgress.effortId(squat.id, 3))
        var progress = WorkoutProgress.start(state).copy(completedEffortIds = ticks)
        val alternative = squat.alternatives.first()
        progress = WorkoutProgress.selectSubstitution(progress, session, squat.id, alternative)
        assertEquals(ticks, progress.completedEffortIds)
        assertEquals(alternative, progress.selectedSubstitutions[squat.id])
        progress = WorkoutProgress.selectSubstitution(progress, session, squat.id, null)
        assertEquals(ticks, progress.completedEffortIds)
        assertFalse(progress.selectedSubstitutions.containsKey(squat.id))
        progress = WorkoutProgress.startManualRest(progress, 90, 1_000_000L)
        assertEquals(ticks, progress.completedEffortIds)
        assertEquals(1_090_000L, progress.restTimerEndEpochMillis)
        progress = WorkoutProgress.addRestSeconds(progress, 30)
        assertEquals(ticks, progress.completedEffortIds)
        assertEquals(1_120_000L, progress.restTimerEndEpochMillis)
        progress = WorkoutProgress.skipRest(progress)
        assertEquals(ticks, progress.completedEffortIds)
        assertNull(progress.restTimerEndEpochMillis)
        progress = WorkoutProgress.startManualRest(progress, 60, 5_000L)
        progress = WorkoutProgress.clearExpiredRest(progress, 5_000L + 1_000L)
        assertEquals(ticks, progress.completedEffortIds)
        assertEquals(65_000L, progress.restTimerEndEpochMillis)
        progress = WorkoutProgress.clearExpiredRest(progress, 65_000L)
        assertEquals(ticks, progress.completedEffortIds)
        assertNull(progress.restTimerEndEpochMillis)
    }

    @Test fun completeDayClearsTicksAndAddsExactlyOneSeal() {
        val (prefs, store) = store()
        val (state, session) = dayTwo()
        store.save(state)
        val squat = squat(session)
        val ticked = WorkoutProgress.toggleEffort(
            WorkoutProgress.toggleEffort(WorkoutProgress.start(state), squat, 1),
            squat,
            4,
        ).copy(restTimerEndEpochMillis = 1_700_000_111_000L)
        store.saveWorkout(ticked)
        store.recordDaySeal(1, 2)
        val next = store.completeDay(store.load())
        assertEquals(1, next.currentWeek)
        assertEquals(3, next.currentDay)
        assertNull(store.loadWorkout())
        assertEquals(setOf("1:2"), store.sealedDays())
        assertFalse(prefs.contains("completedEfforts"))
        assertFalse(prefs.contains("workoutActive"))
        assertFalse(prefs.contains("workoutTimerEnd"))
    }

    @Test fun setAsCurrentAndConsolidateAddNoSeal() {
        val (prefs, store) = store()
        store.save(ProgramUiState().startAtPosition(1, 5, 1))
        prefs.edit { putInt("engagedAbsoluteDay", 28) }
        val loaded = store.load()
        assertEquals(5, loaded.currentWeek)
        assertEquals(1, loaded.currentDay)
        val evidence = store.evidenceForWeek(5)
        assertEquals(WeekStartEvidence.NOT_STARTED, evidence)
        assertTrue(loaded.canConsolidate(workoutActive = false, evidence))
        assertTrue(store.sealedDays().isEmpty())
        store.clearWorkout()
        store.save(loaded.withCurrentPointer(5, 4))
        assertTrue(store.sealedDays().isEmpty())
        store.save(store.load().withCurrentPointer(5, 1))
        val consolidated = store.load().consolidateNow(workoutActive = false, evidence = store.evidenceForWeek(5))
        store.save(consolidated)
        assertTrue(store.sealedDays().isEmpty())
        assertEquals(8, store.load().v74BlockWeek)
        store.recordDaySeal(5, 1)
        store.save(store.load().withCurrentPointer(5, 3))
        assertEquals(setOf("5:1"), store.sealedDays())
    }

    @Test fun sealsPruneWhenTheWeekAdvances() {
        val (_, store) = store()
        store.save(ProgramUiState().startV74().copy(currentDay = 6))
        store.recordDaySeal(1, 2)
        val sameWeek = store.completeDay(store.load())
        assertEquals(1, sameWeek.currentWeek)
        assertEquals(7, sameWeek.currentDay)
        assertEquals(setOf("1:2", "1:6"), store.sealedDays())
        val nextWeek = store.completeDay(store.load())
        assertEquals(2, nextWeek.currentWeek)
        assertEquals(1, nextWeek.currentDay)
        assertTrue(store.sealedDays().isEmpty())
    }

    @Test fun resetClearsSealsAndClearWorkoutDoesNot() {
        val (prefs, store) = store()
        val state = ProgramUiState().startV74()
        store.save(state)
        store.recordDaySeal(1, 1)
        store.recordDaySeal(1, 2)
        store.saveWorkout(WorkoutProgress.start(state))
        store.clearWorkout()
        assertNull(store.loadWorkout())
        assertEquals(setOf("1:1", "1:2"), store.sealedDays())
        store.clearSessionStatus()
        store.clearSealedDays()
        store.save(store.load().resetToV74Start())
        assertTrue(store.sealedDays().isEmpty())
        assertFalse(prefs.contains("sealedDays"))
        val app = source("src/main/java/com/jk/operatorperformance/ui/OperatorPerformanceApp.kt")
        val tools = source("src/main/java/com/jk/operatorperformance/ui/ToolsExtraUi.kt")
        assertEquals(2, Regex("clearSealedDays\\(\\)").findAll(app).count())
        assertEquals(2, Regex("clearSealedDays\\(\\)").findAll(tools).count())
        assertTrue(app.contains("stateStore.completeDay(state)"))
    }

    @Test fun normalizeDropsOnlyInvalidIds() {
        val (state, session) = dayTwo()
        val squat = squat(session)
        val warmUp = session.rows.first { it.effortLabel == "WARM-UP" }
        val kept = setOf(
            WorkoutProgress.effortId(squat.id, 1),
            WorkoutProgress.effortId(squat.id, 2),
            WorkoutProgress.effortId(warmUp.id, 1),
        )
        val progress = WorkoutProgress.start(state).copy(
            completedEffortIds = kept + setOf(
                WorkoutProgress.effortId(squat.id, squat.effortCount + 1),
                WorkoutProgress.effortId(warmUp.id, warmUp.effortCount + 1),
                "missing-row#1",
                "d3_bench:1",
                "${squat.id}#0",
                "${squat.id}#01",
            ),
            restTimerEndEpochMillis = 4_321L,
            selectedSubstitutions = mapOf(squat.id to squat.alternatives.first()),
            undoMovementIndex = 1,
            undoEffortIndex = 1,
            undoComplete = false,
            undoCompletedEffortIds = setOf("stale"),
        )
        val normalized = WorkoutProgress.normalize(progress, session)
        assertEquals(kept, normalized.completedEffortIds)
        assertEquals(4_321L, normalized.restTimerEndEpochMillis)
        assertEquals(squat.alternatives.first(), normalized.selectedSubstitutions[squat.id])
        assertEquals(ProgramAuthority.V74, normalized.authority)
        assertFalse(normalized.canUndo)
    }

    private fun dayTwo(): Pair<ProgramUiState, com.jk.operatorperformance.program.SessionReference> {
        val state = ProgramUiState().startV74().copy(currentDay = 2)
        val session = SessionResolver.resolve(
            state.currentWeek,
            state.currentDay,
            ProgramAuthority.V74,
            state.v74Block,
            state.v74BlockWeek,
        )
        return state to session
    }

    private fun squat(session: com.jk.operatorperformance.program.SessionReference): SessionRow =
        session.rows.single { it.title == "Back squat" }

    private fun store(): Pair<MemoryPrefs, ProgramStateStore> {
        val prefs = MemoryPrefs()
        return prefs to ProgramStateStore(prefs, MemoryPrefs())
    }

    private fun source(relative: String): String =
        listOf(File(relative), File("app/$relative"), File("../$relative")).first { it.isFile }.readText()
}
