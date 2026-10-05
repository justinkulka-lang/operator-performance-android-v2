package com.jk.operatorperformance.program

import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.WorkoutProgress
import org.junit.Assert.*
import org.junit.Test

class V72MixedSpeedReferenceTest {
    @Test fun mixedWeeksResolveOrderedGroupsWithExactDoseAndTotals() {
        mapOf(13 to 3, 20 to 3, 28 to 2, 30 to 2).forEach { (week, flies) ->
            val plan = ProgramV72.conditioningForWeek(week).speed.single()
            assertEquals(4 + flies, plan.effortCount)
            assertEquals(120 + flies * 20, plan.qualifyingSprintMeters)
            val rows = SessionResolver.resolve(week, 4, ProgramAuthority.V72).rows
            val groups = rows.filter { it.id in setOf("w${week}_d4_acceleration", "w${week}_d4_fly") }
            assertEquals(listOf("Acceleration", "Fly"), groups.map { it.title })
            assertEquals(listOf(4, flies), groups.map { it.effortCount })
            val suffix = if (week == 30) "; full recovery; no jumps" else "; full recovery"
            assertEquals(listOf("4 × 30 m acceleration$suffix", "$flies × 20 m fly$suffix"), groups.map { it.prescription })
            assertEquals(120 + flies * 20, groups[0].effortCount * 30 + groups[1].effortCount * 20)
            assertTrue(groups.all { it.note.orEmpty().contains(ProgramV72.stopRule) })
            assertFalse(groups.any { it.note.orEmpty().contains("WORKLOAD WARNING") })
            assertFalse(rows.any { it.id == "w${week}_d4" })
            assertEquals("Cooldown", rows.last().title)
            if (week == 30) assertFalse(rows.any { it.id.endsWith("plyometrics") })
            else assertTrue(rows.any { it.id.endsWith("plyometrics") })
        }
    }

    @Test fun unaffectedWeeksKeepOriginalSpeedRowAndCount() {
        (1..32).filterNot { it in setOf(13, 20, 28, 30) }.forEach { week ->
            val plan = ProgramV72.conditioningForWeek(week).speed.single()
            val row = SessionResolver.resolve(week, 4, ProgramAuthority.V72).rows.single { it.id == plan.id }
            assertEquals(plan.prescription, row.prescription)
            assertEquals(plan.effortCount, row.effortCount)
            val oldCount = Regex("^(\\d+) ×").find(plan.prescription)?.groupValues?.get(1)?.toInt() ?: 1
            assertEquals(oldCount, plan.effortCount)
        }
    }

    @Test fun existingSessionStatusSurvivesReferenceGroupChangeWithoutEffortState() {
        listOf(13, 20, 28, 30).forEach { week ->
            val session = SessionResolver.resolve(week, 4, ProgramAuthority.V72)
            listOf(false, true).forEach { completed ->
                val saved = ActiveWorkoutState(week = week, day = 4, complete = completed,
                    completedEffortIds = setOf("w${week}_d4:1"), restTimerEndEpochMillis = 123456L)
                val normalized = WorkoutProgress.normalize(saved, session)
                assertEquals(completed, normalized.complete)
                assertEquals(week, normalized.week)
                assertEquals(4, normalized.day)
                assertEquals(saved.restTimerEndEpochMillis, normalized.restTimerEndEpochMillis)
                assertTrue(normalized.completedEffortIds.isEmpty())
            }
        }
    }
}
