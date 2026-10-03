package com.jk.operatorperformance.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LegacyExportTest {
    @Test fun exportOmitsV72SessionStateAndDoesNotInventResults() {
        val json = renderLocalExport(
            currentReferenceState = mapOf(
                "currentWeek" to 4,
                "currentDay" to 2,
                "plateUnitSystem" to "LB",
            ),
            legacyTraining = mapOf("week_index" to 1, "session_index" to 0),
            legacyPerformanceReferences = emptyMap(),
            legacyHistoricalBenchmarks = emptyMap(),
            exportedAtMillis = 1_700_000_000_000L,
        )
        assertTrue(json.contains("\"current_reference_state\""))
        assertTrue(json.contains("\"legacy_training\""))
        assertTrue(json.contains("\"legacy_performance_references\""))
        assertTrue(json.contains("\"legacy_historical_benchmarks\""))
        assertTrue(json.contains("\"plateUnitSystem\""))
        listOf(
            "ares_v71_reference_state",
            "engagedAbsoluteDay",
            "workoutTimerEnd",
            "completedEfforts",
            "substitutions",
            "v72MigrationApplied",
            "authorityVersion",
            "bodyweight",
            "calories",
            "rirActual",
            "paceResult",
            "manualRestTimerSeconds",
        ).forEach { prohibited ->
            assertFalse(prohibited, json.contains(prohibited))
        }
    }

    @Test fun sessionStoreValuesAreNotCopiedIntoTheExport() {
        val sessionStore = mapOf(
            "engagedAbsoluteDay" to 22,
            "workoutTimerEnd" to 1_700_000_000_000L,
            "bodyweight" to 180,
            "calories" to 500,
            "paceResult" to "6:40",
            "completedEfforts" to setOf("d1_bench:1"),
        )
        val json = renderLocalExport(
            currentReferenceState = mapOf("currentWeek" to 1),
            legacyTraining = emptyMap(),
            legacyPerformanceReferences = emptyMap(),
            legacyHistoricalBenchmarks = emptyMap(),
            exportedAtMillis = 1_700_000_000_000L,
        )
        sessionStore.keys.forEach { key ->
            assertFalse(key, json.contains(key))
        }
        assertFalse(json.contains("180"))
        assertFalse(json.contains("6:40"))
    }

    @Test fun exporterSourceDoesNotReadTheV72SessionStore() {
        val source = sourceText("src/main/java/com/jk/operatorperformance/state/LegacyDataManager.kt")
        assertTrue(source.contains("operator_v54_reference_state"))
        assertTrue(source.contains("operator_performance_state"))
        assertFalse(source.contains("getSharedPreferences(\"ares_v71_reference_state\""))
        assertFalse(source.contains("engagedAbsoluteDay"))
        assertFalse(source.contains("workoutTimerEnd"))
        listOf("bodyweight", "calories", "rirActual", "paceResult").forEach { prohibited ->
            assertFalse(prohibited, source.contains(prohibited))
        }
    }

    private fun sourceText(relative: String): String =
        listOf(File(relative), File("app/$relative"), File("../$relative")).first { it.isFile }.readText()
}
