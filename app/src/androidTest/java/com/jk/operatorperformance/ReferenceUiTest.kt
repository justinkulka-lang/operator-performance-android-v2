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
        context.getSharedPreferences("operator_v54_reference_state", Context.MODE_PRIVATE).edit().clear().commit()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @After
    fun restoreDisplay() {
        resetDisplayConfiguration()
    }

    @Test
    fun freshInstallLaunchesToExactWeekOneDayOneWorkout() {
        composeRule.onNodeWithTag("today_screen").assertIsDisplayed()
        composeRule.onNodeWithText("WEEK 1 • DAY 1").assertIsDisplayed()
        composeRule.onNodeWithText("Lower A + easy aerobic").assertExists()
        composeRule.onNodeWithText("Back squat").assertExists()
        composeRule.onNodeWithText("4 × 5–8 @ 2–3 RIR • REST 3:00").assertExists()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasText("Stationary cycle — easy aerobic"))
        composeRule.onNodeWithText("Stationary cycle — easy aerobic").assertExists()
        composeRule.onNodeWithText("25:00 continuous • 80–90 rpm • RPE 3–4/10 • full-sentence speech. Increase resistance only enough to hold the prescribed cadence at the prescribed effort.").assertExists()
        composeRule.onNodeWithTag("nav_TODAY").assertExists()
        composeRule.onNodeWithTag("nav_WEEK").assertExists()
        composeRule.onNodeWithTag("nav_PROGRAM").assertExists()
        composeRule.onNodeWithTag("nav_TOOLS").assertExists()
    }

    @Test
    fun todayCardsSupportDirectCompletionAlternativeAndIncompleteAdvanceConfirmation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)

        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("effort_d1_squat_1"))
        composeRule.onNodeWithTag("effort_d1_squat_1").performClick()
        composeRule.waitForIdle()
        assertTrue(store.loadWorkout()?.completedEffortIds?.contains("d1_squat#1") == true)

        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("today_alt_d1_squat"))
        composeRule.onNodeWithTag("today_alt_d1_squat").performClick()
        composeRule.onNodeWithText("Safety-bar squat").assertExists()
        assertEquals("Safety-bar squat", store.loadWorkout()?.selectedSubstitutions?.get("d1_squat"))

        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("complete_day"))
        composeRule.onNodeWithTag("complete_day").performClick()
        composeRule.onNodeWithText("Advance with unfinished work?").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm_incomplete_advance").performClick()
        composeRule.onNodeWithText("WEEK 1 • DAY 2").assertIsDisplayed()
        assertTrue(store.loadWorkout() == null)
    }

    @Test
    fun weekBrowseShowsFullSpeedWorkoutWithoutMovingPointer() {
        composeRule.onNodeWithTag("nav_WEEK").performClick()
        composeRule.onNodeWithTag("week_day_1").assertExists()
        composeRule.onNodeWithText("CURRENT").assertExists()
        composeRule.onNodeWithTag("week_screen").performScrollToNode(hasTestTag("week_day_3"))
        composeRule.onNodeWithTag("week_day_3").performClick()
        composeRule.onNodeWithText("Speed warm-up").assertExists()
        composeRule.onNodeWithText("Acceleration").assertExists()
        composeRule.onNodeWithText("Optional machine-power finisher — Air/fan bike").assertExists()
        composeRule.onNodeWithTag("reference_dialog").performScrollToNode(hasText("CLOSE"))
        composeRule.onNodeWithText("CLOSE").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithText("WEEK 1 • DAY 1").assertIsDisplayed()
    }

    @Test
    fun programBrowsesFixedWeekTwoAndChangesPointerOnlyAfterConfirmation() {
        composeRule.onNodeWithTag("nav_PROGRAM").performClick()
        composeRule.onNodeWithTag("program_week_2").performClick()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_day_3"))
        composeRule.onNodeWithTag("program_day_3").performClick()
        composeRule.onNodeWithText("Acceleration").assertExists()
        composeRule.onNodeWithTag("reference_dialog").performScrollToNode(hasTestTag("set_current_button"))
        composeRule.onNodeWithTag("set_current_button").performClick()
        composeRule.onNodeWithTag("confirm_set_current").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithText("WEEK 2 • DAY 3").assertIsDisplayed()
        composeRule.onNodeWithText("Acceleration").assertExists()
    }

    @Test
    fun workoutModeCompletesSetsStartsPrescribedRestAndSupportsUndo() {
        startWorkout()
        composeRule.onNodeWithTag("workout_mode").assertIsDisplayed()
        composeRule.onNodeWithText("Back squat").assertIsDisplayed()
        composeRule.onNodeWithText("SET 1 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_primary").performClick()
        composeRule.onNodeWithText("SET 2 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_rest").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_rest_add_30").performClick()
        composeRule.onNodeWithTag("workout_undo").performClick()
        composeRule.onNodeWithText("SET 1 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_rest").assertDoesNotExist()
    }

    @Test
    fun completedSetCheckmarkAndPointerRestoreAfterActivityRecreation() {
        startWorkout()
        composeRule.onNodeWithTag("workout_primary").performClick()
        composeRule.onNodeWithTag("workout_return_today").performClick()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("effort_d1_squat_1"))
        composeRule.onNodeWithTag("effort_d1_squat_1").assertExists()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithText("RESUME WORKOUT").assertIsDisplayed()
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithText("SET 2 OF 4").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_rest").assertIsDisplayed()
    }

    @Test
    fun singleAlternativeSelectionPersistsAndCanReturnToPrimary() {
        startWorkout()
        composeRule.onNodeWithTag("workout_substitutions").performClick()
        composeRule.onNodeWithTag("substitution_0").performClick()
        composeRule.onNodeWithText("Safety-bar squat").assertIsDisplayed()
        composeRule.onNodeWithTag("selected_substitution").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_return_today").performClick()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithText("Safety-bar squat").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_substitutions").performClick()
        composeRule.onNodeWithTag("substitution_canonical").performClick()
        composeRule.onNodeWithTag("selected_substitution").assertDoesNotExist()
        composeRule.onNodeWithText("Back squat").assertIsDisplayed()
    }

    @Test
    fun utilityRestTimerRemainsIndependentFromWorkoutRest() {
        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("rest_timer_input").performTextReplacement("75")
        composeRule.onNodeWithTag("rest_timer_save").performClick()
        composeRule.onNodeWithTag("rest_timer_start").performClick()
        composeRule.onNodeWithTag("utility_rest_countdown").assertIsDisplayed()
        composeRule.onNodeWithTag("rest_timer_cancel").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        startWorkout()
        composeRule.onNodeWithTag("workout_primary").performClick()
        composeRule.onNodeWithTag("workout_rest").assertIsDisplayed()
        composeRule.onNodeWithText("SET 2 OF 4").assertIsDisplayed()
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
    }

    @Test
    fun authorizedReferenceStatePersistsWithoutPerformanceData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        val expected = ProgramUiState(
            currentWeek = 6,
            currentDay = 6,
            deloadMode = true,
            restTimerSeconds = 75,
            utilityRestTimerEndEpochMillis = 123_456_789L,
        )
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
