package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class WorkoutProgressTest {
    private val state = ProgramUiState(initialized = true, currentWeek = 2, currentDay = 3, authority = ProgramAuthority.V72)
    private val session = SessionResolver.resolve(2, 3, ProgramAuthority.V72)

    @Test fun sessionStatusHasFourStatesAndDoesNotNeedSetTaps() {
        assertEquals(SessionStatus.NOT_STARTED, WorkoutProgress.status(null, showingWorkout = false))
        val started = WorkoutProgress.start(state)
        assertEquals(SessionStatus.ACTIVE, WorkoutProgress.status(started, showingWorkout = true))
        assertEquals(SessionStatus.RESUMABLE, WorkoutProgress.status(started, showingWorkout = false))
        assertEquals(SessionStatus.COMPLETED, WorkoutProgress.status(WorkoutProgress.markSessionComplete(started), showingWorkout = true))
    }

    @Test fun mutationsDoNotKeepPerSetOrResultFields() {
        val started = WorkoutProgress.start(state).copy(
            movementIndex = 4,
            effortIndex = 3,
            completedEffortIds = setOf("d3_bench:1"),
            undoMovementIndex = 1,
            undoEffortIndex = 1,
            undoComplete = false,
            undoCompletedEffortIds = setOf("stale"),
        )
        val normalized = WorkoutProgress.normalize(started, session)
        assertEquals(0, normalized.movementIndex)
        assertEquals(1, normalized.effortIndex)
        assertTrue(normalized.completedEffortIds.isEmpty())
        assertFalse(normalized.canUndo)
        val completed = WorkoutProgress.markSessionComplete(normalized)
        assertTrue(completed.complete)
        assertTrue(completed.completedEffortIds.isEmpty())
        assertEquals(null, completed.restTimerEndEpochMillis)
        val row = session.rows.first { it.alternatives.isNotEmpty() }
        val substituted = WorkoutProgress.selectSubstitution(completed, session, row.id, row.alternatives.first())
        assertTrue(substituted.completedEffortIds.isEmpty())
        assertEquals(row.alternatives.first(), substituted.selectedSubstitutions[row.id])
    }

    @Test fun unknownSubstitutionIsRejected() {
        val started = WorkoutProgress.start(state)
        val row = session.rows.first { it.alternatives.isNotEmpty() }
        val failed = runCatching { WorkoutProgress.selectSubstitution(started, session, row.id, "Trap-bar deadlift") }
        assertTrue(failed.exceptionOrNull() is IllegalArgumentException)
    }

    @Test fun storeSourceDoesNotPersistProhibitedResults() {
        val source = storeSource()
        listOf(
            "bodyweight", "calories", "readiness", "sleep", "heartRate", "hrv",
            "photo", "Health Connect", "analytics", "cloudSync", "paceResult", "rirActual",
        ).forEach { prohibited ->
            assertFalse(source.contains(prohibited, ignoreCase = true))
        }
        assertTrue(source.contains("remove(KEY_COMPLETED)"))
        assertTrue(source.contains("putBoolean(KEY_HAS_UNDO, false)"))
        assertFalse(source.contains("putStringSet(KEY_COMPLETED"))
        assertFalse(source.contains("putStringSet(KEY_UNDO_COMPLETED"))
        val workoutKeys = source.substringAfter("WORKOUT_KEYS").substringAfter("listOf(").substringBefore(")")
        assertFalse(workoutKeys.contains("ENGAGED"))
        assertTrue(workoutKeys.contains("KEY_COMPLETED"))
    }

    @Test fun v72SessionTextDoesNotCarryV71OrEightDayLabels() {
        (1..32).forEach { week ->
            (1..7).forEach { day ->
                val resolved = SessionResolver.resolve(week, day, ProgramAuthority.V72)
                val text = buildString {
                    append(resolved.title)
                    resolved.rows.forEach { row ->
                        append(row.title).append(row.prescription).append(row.note.orEmpty())
                        row.alternatives.forEach(::append)
                    }
                }
                assertFalse("W$week D$day", text.contains("v7.1", ignoreCase = true))
                assertFalse("W$week D$day", text.contains("eight-day", ignoreCase = true) || text.contains("8-day", ignoreCase = true) || text.contains("Day 8"))
                assertTrue(resolved.day in 1..7)
            }
        }
    }

    private fun storeSource(): String {
        val relative = "src/main/java/com/jk/operatorperformance/state/ProgramStateStore.kt"
        return listOf(File(relative), File("app/$relative"), File("../$relative")).first { it.isFile }.readText()
    }
}
