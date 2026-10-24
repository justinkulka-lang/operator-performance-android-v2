package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.Rule
import org.junit.Test

/**
 * A saved workout whose authority is not the week's authority must not crash Today.
 * Storage is cleared from a side effect after the screen is composed.
 */
class TodayAuthorityMismatchUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun mismatchedSavedWorkoutRendersTodayAndClearsStorage() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE).edit().clear()
            .putBoolean("v71Initialized", true)
            .putInt("currentWeek", 17)
            .putInt("currentDay", 2)
            .putString("authorityVersion", "V73")
            .putBoolean("v72MigrationApplied", true)
            .putInt("v72MigrationRevision", 2)
            .putBoolean("v72ActivationProven", true)
            .putBoolean("v73MigrationApplied", true)
            .putInt("v73ActivationWeek", 1)
            .putBoolean("workoutActive", true)
            .putInt("workoutSchema", 5)
            .putInt("workoutWeek", 17)
            .putInt("workoutDay", 2)
            .putString("workoutAuthority", "V72")
            .commit()
        LifecycleTestSupport(rule).recreate("today_header")
        rule.onNodeWithTag("today_header").assertTextContains("v7.3", substring = true)
        rule.onNodeWithTag("today_screen").assertIsDisplayed()
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        val store = ProgramStateStore(context)
        rule.waitUntil(10_000) { store.loadWorkout() == null }
        rule.onNodeWithTag("today_screen").assertIsDisplayed()
    }
}
