package com.jk.operatorperformance.state

import android.content.Context

class LegacyDataManager(context: Context) {
    private val app = context.applicationContext
    private val currentPrefs = app.getSharedPreferences(CURRENT_PREFS, Context.MODE_PRIVATE)
    private val legacyTraining = app.getSharedPreferences(LEGACY_TRAINING_PREFS, Context.MODE_PRIVATE)
    private val legacyReferences = app.getSharedPreferences(LEGACY_REFERENCE_PREFS, Context.MODE_PRIVATE)
    private val legacyBenchmarks = app.getSharedPreferences(LEGACY_BENCHMARK_PREFS, Context.MODE_PRIVATE)

    fun migratePointerIfNeeded() {
        if (currentPrefs.getBoolean(KEY_MIGRATION_COMPLETE, false)) return
        val editor = currentPrefs.edit()
        if (!currentPrefs.contains(KEY_CURRENT_WEEK) || !currentPrefs.contains(KEY_CURRENT_DAY)) {
            val oldWeek = legacyTraining.getInt(LEGACY_WEEK_INDEX, Int.MIN_VALUE)
            val oldSession = legacyTraining.getInt(LEGACY_SESSION_INDEX, Int.MIN_VALUE)
            if (oldWeek in 0..7 && oldSession in 0..6) {
                editor.putInt(KEY_CURRENT_WEEK, oldWeek + 1)
                editor.putInt(KEY_CURRENT_DAY, oldSession + 1)
                editor.putString(KEY_MIGRATION_RESULT, "MAPPED_POINTER")
            } else {
                editor.putInt(KEY_CURRENT_WEEK, 1)
                editor.putInt(KEY_CURRENT_DAY, 1)
                editor.putString(KEY_MIGRATION_RESULT, "DEFAULTED_POINTER")
            }
        } else {
            editor.putString(KEY_MIGRATION_RESULT, "CURRENT_POINTER_PRESERVED")
        }
        editor.putBoolean(KEY_MIGRATION_COMPLETE, true).commit()
    }

    fun hasLegacyTrackingData(): Boolean = legacyNamespaces().values.any { it.isNotEmpty() }

    fun deleteLegacyTrackingData(): Boolean {
        val results = listOf(
            legacyTraining.edit().clear().commit(),
            legacyReferences.edit().clear().commit(),
            legacyBenchmarks.edit().clear().commit(),
        )
        return results.all { it }
    }

    fun buildLocalExport(exportedAtMillis: Long = System.currentTimeMillis()): String {
        require(exportedAtMillis > 0L)
        return renderLocalExport(
            currentReferenceState = currentPrefs.all,
            legacyTraining = legacyTraining.all,
            legacyPerformanceReferences = legacyReferences.all,
            legacyHistoricalBenchmarks = legacyBenchmarks.all,
            exportedAtMillis = exportedAtMillis,
        )
    }

    private fun legacyNamespaces(): Map<String, Map<String, *>> = linkedMapOf(
        "legacy_training" to legacyTraining.all,
        "legacy_performance_references" to legacyReferences.all,
        "legacy_historical_benchmarks" to legacyBenchmarks.all,
    )

    companion object {
        // operator_v54_reference_state keeps the v5.4 pointer migration and the plate calculator.
        // The three legacy_* stores are retained v5.7 tracking the user can delete.
        // None of these is the v7.2 session store (ares_v71_reference_state), and this export does not read it.
        private const val CURRENT_PREFS = "operator_v54_reference_state"
        private const val LEGACY_TRAINING_PREFS = "operator_performance_state"
        private const val LEGACY_REFERENCE_PREFS = "operator_performance_references"
        private const val LEGACY_BENCHMARK_PREFS = "operator_performance_benchmarks"
        private const val LEGACY_WEEK_INDEX = "week_index"
        private const val LEGACY_SESSION_INDEX = "session_index"
        private const val KEY_CURRENT_WEEK = "currentWeek"
        private const val KEY_CURRENT_DAY = "currentDay"
        private const val KEY_MIGRATION_COMPLETE = "migrationV54Complete"
        private const val KEY_MIGRATION_RESULT = "migrationV54Result"
    }
}

/**
 * Tools export copies four historical preference namespaces and does not create training results.
 * It does not read ares_v71_reference_state, so the current pointer, session status, substitutions,
 * manual timers, and authority activation are not written into the file.
 * operator_v54_reference_state remains because older builds stored the pointer migration there and
 * the plate calculator still uses that same file. The three legacy namespaces remain so a user can
 * keep a local copy of retained v5.7 tracking before deleting it.
 */
internal fun renderLocalExport(
    currentReferenceState: Map<String, Any?>,
    legacyTraining: Map<String, Any?>,
    legacyPerformanceReferences: Map<String, Any?>,
    legacyHistoricalBenchmarks: Map<String, Any?>,
    exportedAtMillis: Long,
): String {
    require(exportedAtMillis > 0L)
    val namespaces = linkedMapOf(
        "current_reference_state" to currentReferenceState,
        "legacy_training" to legacyTraining,
        "legacy_performance_references" to legacyPerformanceReferences,
        "legacy_historical_benchmarks" to legacyHistoricalBenchmarks,
    )
    return buildString {
        append("{\n  \"format_version\": 1,\n")
        append("  \"authority\": \"LEGEND v7.2\",\n")
        append("  \"exported_at_millis\": ").append(exportedAtMillis).append(",\n")
        append("  \"namespaces\": {")
        namespaces.entries.forEachIndexed { namespaceIndex, (namespace, values) ->
            if (namespaceIndex > 0) append(',')
            append("\n    ").append(localExportQuoted(namespace)).append(": {")
            values.toSortedMap().entries.forEachIndexed { index, (key, value) ->
                if (index > 0) append(',')
                append("\n      ").append(localExportQuoted(key)).append(": ").append(localExportJsonValue(value))
            }
            if (values.isNotEmpty()) append('\n')
            append("    }")
        }
        append("\n  }\n}\n")
    }
}

private fun localExportJsonValue(value: Any?): String = when (value) {
    is String -> localExportQuoted(value)
    is Boolean, is Int, is Long -> value.toString()
    is Float -> if (value.isFinite()) value.toString() else localExportQuoted(value.toString())
    is Set<*> -> value.filterIsInstance<String>().sorted().joinToString(prefix = "[", postfix = "]") { localExportQuoted(it) }
    null -> "null"
    else -> localExportQuoted(value.toString())
}

private fun localExportQuoted(value: String): String = buildString {
    append('"')
    value.forEach { c ->
        when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (c.code < 0x20) append("\\u%04x".format(c.code)) else append(c)
        }
    }
    append('"')
}
