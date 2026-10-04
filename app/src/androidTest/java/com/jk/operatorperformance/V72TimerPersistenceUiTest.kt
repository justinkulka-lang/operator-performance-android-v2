package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Timer and pointer survival. [timerSurvivesActivityRecreate] runs in the normal suite.
 * Host orchestration runs [seedsManualRestTimer] and then [timerAndPointerSurviveProcessDeath]
 * around force-stop or reboot. Those two methods skip unless persistenceMode is set.
 */
class V72TimerPersistenceUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun prepare() {
        if (mode() == "verify") {
            rule.activityRule.scenario.recreate()
            return
        }
        clearPrefs()
        rule.activityRule.scenario.recreate()
    }

    @Test fun timerSurvivesActivityRecreate() {
        if (mode() != "isolated") return
        seedSessionAndTimers()
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        revealOnScreen("tools_screen", "utility_rest_countdown")
        rule.onNodeWithTag("utility_rest_countdown").assertIsDisplayed()
        rule.onNodeWithTag("nav_TODAY").performClick()
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
        rule.onNodeWithTag("start_resume_workout").performClick()
        rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
        val prefs = prefs()
        assertTrue(prefs.getLong("workoutTimerEnd", 0L) > 0L)
        assertTrue(prefs.getLong("utilityRestTimerEndEpochMillis", 0L) > 0L)
        assertEquals(1, prefs.getInt("currentWeek", 0))
        assertEquals(1, prefs.getInt("currentDay", 0))
        assertFalse(prefs.contains("completedEfforts"))
    }

    @Test fun seedsManualRestTimer() {
        if (mode() != "seed") return
        seedSessionAndTimers()
        val prefs = prefs()
        assertTrue(prefs.getLong("workoutTimerEnd", 0L) > System.currentTimeMillis())
        assertTrue(prefs.getLong("utilityRestTimerEndEpochMillis", 0L) > System.currentTimeMillis())
        assertEquals(60, prefs.getInt("manualRestTimerSeconds", 0))
        assertEquals(1, prefs.getInt("engagedAbsoluteDay", 0))
        assertTrue(prefs.getString("authorityVersion", "") in setOf("V72", "V73"))
    }

    @Test fun timerAndPointerSurviveProcessDeath() {
        if (mode() != "verify") return
        val prefs = prefs()
        assertTrue(prefs.getBoolean("v71Initialized", false))
        assertEquals(1, prefs.getInt("currentWeek", 0))
        assertEquals(1, prefs.getInt("currentDay", 0))
        assertTrue(prefs.getString("authorityVersion", "") in setOf("V72", "V73"))
        assertEquals(1, prefs.getInt("engagedAbsoluteDay", 0))
        assertTrue(prefs.getLong("workoutTimerEnd", 0L) > 0L)
        assertTrue(prefs.getLong("utilityRestTimerEndEpochMillis", 0L) > 0L)
        assertEquals(60, prefs.getInt("manualRestTimerSeconds", 0))
        assertFalse(prefs.contains("completedEfforts"))
        assertFalse(prefs.contains("bodyweight"))
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
        val workoutEnd = prefs.getLong("workoutTimerEnd", 0L)
        if (workoutEnd > System.currentTimeMillis()) {
            rule.onNodeWithTag("start_resume_workout").performClick()
            rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
        }
    }

    private fun seedSessionAndTimers() {
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("start_resume_workout").performClick()
        revealOnScreen("workout_mode", "start_rest_180")
        rule.onNodeWithTag("start_rest_180").performClick()
        rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
        revealOnScreen("workout_mode", "return_today")
        rule.onNodeWithTag("return_today").performClick()
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
        rule.onNodeWithTag("nav_TOOLS").performClick()
        revealOnScreen("tools_screen", "rest_choice_60")
        rule.onNodeWithTag("rest_choice_60").performClick()
        revealOnScreen("tools_screen", "rest_timer_start")
        rule.onNodeWithTag("rest_timer_start").performClick()
        revealOnScreen("tools_screen", "utility_rest_countdown")
        rule.onNodeWithTag("utility_rest_countdown").assertIsDisplayed()
    }

    private fun revealOnScreen(screenTag: String, targetTag: String) {
        repeat(2) { pass ->
            repeat(8) {
                if (rule.onAllNodesWithTag(targetTag).fetchSemanticsNodes().isNotEmpty()) {
                    rule.onNodeWithTag(targetTag).performScrollTo()
                    return
                }
                rule.onNodeWithTag(screenTag).performTouchInput {
                    if (pass == 0) swipeUp(startY = bottom * 0.8f, endY = top + 80f)
                    else swipeDown(startY = top + 80f, endY = bottom * 0.8f)
                }
                rule.waitForIdle()
            }
        }
    }

    private fun mode(): String = InstrumentationRegistry.getArguments().getString("persistenceMode") ?: "isolated"

    private fun prefs() = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)

    private fun clearPrefs() {
        prefs().edit().clear().commit()
    }
}
