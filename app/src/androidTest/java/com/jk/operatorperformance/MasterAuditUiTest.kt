package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MasterAuditUiTest {
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
    fun conditioningAlternativeUsesAlternativePrescriptionAndDistinctHierarchy() {
        composeRule.onNodeWithTag("today_screen")
            .performScrollToNode(hasTestTag("conditioning_work_d1_easy_aerobic"))
        composeRule.onNodeWithTag("conditioning_work_d1_easy_aerobic").assertIsDisplayed()
        composeRule.onNodeWithTag("today_alt_d1_easy_aerobic").performClick()
        composeRule.onNodeWithText("Air/fan bike").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(
            "25:00 continuous at RPE 3–4/10 with full-sentence speech. Use a smooth sustainable cadence; fan resistance self-scales. No intervals, no sprint finish, no added running.",
        ).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun optionalMachineFinisherSkipsAsOneBlockAndMovesToRequiredCooldown() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        store.save(store.load().withCurrentPointer(1, 3))
        store.saveWorkout(
            ActiveWorkoutState(
                week = 1,
                day = 3,
                deloadMode = false,
                movementIndex = 2,
                effortIndex = 1,
                completedEffortIds = buildSet {
                    add("d3_speed_warmup#1")
                    (1..6).forEach { add("w1_accel#$it") }
                },
            ),
        )
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithText("Optional machine-power finisher — Air/fan bike").assertIsDisplayed()
        composeRule.onNodeWithTag("skip_optional").performClick()
        composeRule.onNodeWithText("Speed cooldown").assertIsDisplayed()
        composeRule.onNodeWithText("COOLDOWN 1 OF 1").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_primary").assertIsDisplayed()
    }

    @Test
    fun focusRestStateFitsCompactScreenAtFontScale13WithoutScrolling() {
        configureDisplay(widthPx = 360, heightPx = 640, densityDpi = 160, fontScale = 1.3f)
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("workout_primary").performClick()
        composeRule.onNodeWithTag("workout_rest").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_rest_add_30").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_rest_skip").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("workout_primary").assertIsDisplayed()
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
}
