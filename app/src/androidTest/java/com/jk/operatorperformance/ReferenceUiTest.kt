package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.SpeedVariant
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReferenceUiTest {
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
    fun freshInstallLaunchesDirectlyToTodayWithCompleteWeekOneDayOneReference() {
        composeRule.onNodeWithTag("today_screen").assertIsDisplayed()
        composeRule.onNodeWithText("WEEK 1 • DAY 1").assertIsDisplayed()
        composeRule.onNodeWithText("Lower A + easy aerobic").assertExists()
        composeRule.onNodeWithText("Back squat or front squat").assertExists()
        composeRule.onNodeWithText("4 × 5–8 @ 2–3 RIR").assertExists()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasText("Low-eccentric aerobic finisher"))
        composeRule.onNodeWithText("Low-eccentric aerobic finisher").assertExists()
        composeRule.onNodeWithText("20–25 min easy").assertExists()
        composeRule.onNodeWithTag("nav_TODAY").assertExists()
        composeRule.onNodeWithTag("nav_WEEK").assertExists()
        composeRule.onNodeWithTag("nav_PROGRAM").assertExists()
        composeRule.onNodeWithTag("nav_TOOLS").assertExists()
    }

    @Test
    fun weekBrowseShowsSevenDaysAndDoesNotMoveCurrentPointer() {
        composeRule.onNodeWithTag("nav_WEEK").performClick()
        composeRule.onNodeWithTag("week_day_1").assertExists()
        composeRule.onNodeWithText("CURRENT").assertExists()
        composeRule.onNodeWithTag("week_screen").performScrollToNode(hasTestTag("week_day_7"))
        composeRule.onNodeWithTag("week_day_7").assertExists()
        composeRule.onNodeWithTag("week_screen").performScrollToNode(hasTestTag("week_day_3"))
        composeRule.onNodeWithTag("week_day_3").performClick()
        composeRule.onNodeWithText("300 m baseline").assertExists()
        composeRule.onNodeWithText("CLOSE").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithText("WEEK 1 • DAY 1").assertIsDisplayed()
    }

    @Test
    fun programBrowsesPopulatedWeeksShowsPhaseStatusAndChangesPointerOnlyAfterConfirmation() {
        composeRule.onNodeWithTag("nav_PROGRAM").performClick()
        composeRule.onNodeWithTag("program_week_2").performClick()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasText("NEXT WEEK • WEEK 2"))
        composeRule.onNodeWithText("NEXT WEEK • WEEK 2").assertExists()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("current_phase"))
        composeRule.onNodeWithText("CURRENT PHASE • Block 1 — Accumulation • Weeks 1–8 • POPULATED").assertExists()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("next_phase"))
        composeRule.onNodeWithText("NEXT PHASE • Block 2 • Weeks 9–16 • UNPOPULATED / NOT PROGRAMMED").assertExists()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("phase_17_24"))
        composeRule.onNodeWithTag("phase_17_24").assertExists()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("phase_25_32"))
        composeRule.onNodeWithTag("phase_25_32").assertExists()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_day_3"))
        composeRule.onNodeWithTag("program_day_3").performClick()
        composeRule.onNodeWithText("ACCELERATION").assertExists()
        composeRule.onNodeWithText("Optional machine-power finisher").assertExists()
        composeRule.onNodeWithTag("set_current_button").performClick()
        composeRule.onNodeWithTag("confirm_set_current").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithText("WEEK 2 • DAY 3").assertIsDisplayed()
        composeRule.onNodeWithText("ACCELERATION").assertExists()
    }

    @Test
    fun workoutModeAdvancesSetsSupportsUndoAndExposesWorkoutMap() {
        startWorkout()
        composeRule.onNodeWithTag("workout_mode").assertIsDisplayed()
        composeRule.onNodeWithText("Back squat or front squat").assertIsDisplayed()
        composeRule.onNodeWithText("SET 1 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_primary").performClick()
        composeRule.onNodeWithText("SET 2 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_undo").performClick()
        composeRule.onNodeWithText("SET 1 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_map_button").performClick()
        composeRule.onNodeWithTag("workout_map_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("CURRENT").assertExists()
    }

    @Test
    fun workoutPointerRestoresAfterActivityRecreationWithoutPerformanceEntry() {
        startWorkout()
        composeRule.onNodeWithTag("workout_primary").performClick()
        composeRule.onNodeWithText("SET 2 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_return_today").performClick()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithText("RESUME WORKOUT").assertIsDisplayed()
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithText("SET 2 OF 4").assertIsDisplayed()
    }

    @Test
    fun substitutionSelectionPersistsForActiveWorkoutAndCanReturnToCanonical() {
        startWorkout()
        composeRule.onNodeWithTag("workout_substitutions").performClick()
        composeRule.onNodeWithTag("substitution_0").performClick()
        composeRule.onNodeWithTag("selected_substitution").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_return_today").performClick()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("selected_substitution").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_substitutions").performClick()
        composeRule.onNodeWithTag("substitution_canonical").performClick()
        composeRule.onNodeWithTag("selected_substitution").assertDoesNotExist()
        composeRule.onNodeWithText("Back squat or front squat").assertIsDisplayed()
    }

    @Test
    fun configuredRestTimerRunsIndependentlyAndRestoresWorkoutTimerWithUndo() {
        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("rest_timer_input").performTextReplacement("120")
        composeRule.onNodeWithTag("rest_timer_save").performClick()
        composeRule.onNodeWithTag("rest_timer_start").performClick()
        composeRule.onNodeWithTag("utility_rest_countdown").assertIsDisplayed()
        composeRule.onNodeWithTag("rest_timer_cancel").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        startWorkout()
        composeRule.onNodeWithTag("workout_primary").performClick()
        composeRule.onNodeWithTag("workout_rest").assertIsDisplayed()
        composeRule.onNodeWithText("SET 2 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_return_today").performClick()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("workout_rest").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_undo").performClick()
        composeRule.onNodeWithText("SET 1 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_rest").assertDoesNotExist()
    }

    @Test
    fun workoutReadyFits360x640DpAtFontScale13WithoutFocusScroll() {
        configureDisplay(widthPx = 360, heightPx = 640, densityDpi = 160, fontScale = 1.3f)
        startWorkout()
        composeRule.onNodeWithTag("workout_context").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_movement").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_effort").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_prescription").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_substitutions").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_primary").assertIsDisplayed()
        val primaryHeight = composeRule.onNodeWithTag("workout_primary").fetchSemanticsNode().boundsInRoot.height
        assertTrue("Primary workout target must remain at least 48 dp high", primaryHeight >= 48f)
    }

    @Test
    fun workoutPrimaryControlRemainsReachableAtMaximumFontScale() {
        configureDisplay(widthPx = 360, heightPx = 640, densityDpi = 160, fontScale = 2.0f)
        startWorkout()
        composeRule.onNodeWithTag("workout_primary").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("workout_primary").performClick()
        composeRule.onNodeWithText("SET 2 OF 4").assertExists()
    }

    @Test
    fun authorizedReferenceStatePersistsWithoutPerformanceData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        val expected = ProgramUiState(currentWeek = 6, currentDay = 6, speedVariant = SpeedVariant.MAX_VELOCITY, engineVariant = EngineVariant.VO2, deloadMode = true, restTimerSeconds = 75, utilityRestTimerEndEpochMillis = 123_456_789L)
        store.save(expected)
        assertEquals(expected, ProgramStateStore(context).load())
    }

    private fun startWorkout() {
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.waitForIdle()
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
