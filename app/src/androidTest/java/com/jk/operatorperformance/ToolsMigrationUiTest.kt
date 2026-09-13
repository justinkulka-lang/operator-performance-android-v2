package com.jk.operatorperformance

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jk.operatorperformance.state.LegacyDataManager
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToolsMigrationUiTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun clearStores() {
        listOf(
            "operator_v54_reference_state",
            "operator_performance_state",
            "operator_performance_references",
            "operator_performance_benchmarks",
        ).forEach { context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().commit() }
    }

    @Test
    fun legacyZeroBasedPointerMapsToWeekAndDayWithoutUsingLegacyPerformanceData() {
        context.getSharedPreferences("operator_performance_state", Context.MODE_PRIVATE).edit()
            .putInt("week_index", 2)
            .putInt("session_index", 4)
            .putString("session_history", "legacy-performance-data")
            .commit()

        LegacyDataManager(context).migratePointerIfNeeded()
        val state = ProgramStateStore(context).load()
        assertEquals(3, state.currentWeek)
        assertEquals(5, state.currentDay)
        assertTrue(LegacyDataManager(context).hasLegacyTrackingData())
    }

    @Test
    fun ambiguousLegacyPointerDefaultsSafelyToWeekOneDayOne() {
        context.getSharedPreferences("operator_performance_state", Context.MODE_PRIVATE).edit()
            .putInt("week_index", 99)
            .putInt("session_index", -1)
            .commit()

        LegacyDataManager(context).migratePointerIfNeeded()
        val state = ProgramStateStore(context).load()
        assertEquals(1, state.currentWeek)
        assertEquals(1, state.currentDay)
    }

    @Test
    fun exportContainsRetainedLegacyDataAndConfirmedDeletionClearsOnlyLegacyNamespaces() {
        val legacyTraining = context.getSharedPreferences("operator_performance_state", Context.MODE_PRIVATE)
        legacyTraining.edit().putString("session_history", "legacy-row").commit()
        LegacyDataManager(context).migratePointerIfNeeded()
        ProgramStateStore(context).save(ProgramStateStore(context).load().withCurrentPointer(4, 6))

        val manager = LegacyDataManager(context)
        val export = manager.buildLocalExport(exportedAtMillis = 1234L)
        assertTrue(export.contains("legacy-row"))
        assertTrue(export.contains("current_reference_state"))
        assertTrue(manager.deleteLegacyTrackingData())
        assertFalse(manager.hasLegacyTrackingData())
        val state = ProgramStateStore(context).load()
        assertEquals(4, state.currentWeek)
        assertEquals(6, state.currentDay)
    }
}
