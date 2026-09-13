package com.jk.operatorperformance

import android.content.Context
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReleaseReadinessUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetState() {
        resetDisplayConfiguration()
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("operator_v54_reference_state", Context.MODE_PRIVATE)
            .edit().clear().commit()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @After
    fun restoreDisplay() {
        resetDisplayConfiguration()
    }

    @Test
    fun airplaneModeAcceptanceStillProvidesTodayAndWorkoutReference() {
        assertEquals("1", shellOutput("settings get global airplane_mode_on"))
        composeRule.onNodeWithTag("today_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("workout_context").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_prescription").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_primary").assertIsDisplayed()
    }

    @Test
    fun essentialNavigationTimerSubstitutionUndoAndCompletionRemainReachableAtMaxFontScale() {
        configureDisplay(widthPx = 360, heightPx = 640, densityDpi = 160, fontScale = 2.0f)

        composeRule.onNodeWithTag("nav_TODAY").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_WEEK").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_PROGRAM").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_TOOLS").assertIsDisplayed()

        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("workout_substitutions").performScrollTo().performClick()
        composeRule.onNodeWithTag("substitution_0").performClick()
        composeRule.onNodeWithTag("workout_primary").performScrollTo().performClick()
        composeRule.onNodeWithTag("workout_undo").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("workout_return_today").performScrollTo().performClick()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        store.save(store.load().copy(restTimerSeconds = 60))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("rest_timer_start"))
        composeRule.onNodeWithTag("rest_timer_start").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("utility_rest_countdown"))
        composeRule.onNodeWithTag("utility_rest_countdown").assertIsDisplayed()
        composeRule.onNodeWithTag("rest_timer_cancel").performScrollTo().performClick()

        store.clearWorkout()
        store.save(store.load().withCurrentPointer(1, 7))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("workout_primary").performScrollTo().performClick()
        composeRule.onNodeWithTag("workout_complete").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("today_screen").assertIsDisplayed()
    }

    private fun configureDisplay(widthPx: Int, heightPx: Int, densityDpi: Int, fontScale: Float) {
        shell("wm size ${widthPx}x$heightPx")
        shell("wm density $densityDpi")
        shell("settings put system font_scale $fontScale")
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    private fun resetDisplayConfiguration() {
        shell("wm size reset")
        shell("wm density reset")
        shell("settings put system font_scale 1.0")
    }

    private fun shell(command: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).close()
    }

    private fun shellOutput(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor)
            .bufferedReader()
            .use { it.readText().trim() }
    }
}
