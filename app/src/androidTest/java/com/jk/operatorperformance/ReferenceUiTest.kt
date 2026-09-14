package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReferenceUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("operator_v54_reference_state", Context.MODE_PRIVATE).edit().clear().commit()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @Test
    fun freshInstallLaunchesDirectlyToAresTodayWithCompleteWeekOneDayOne() {
        composeRule.onNodeWithTag("today_screen").assertIsDisplayed()
        composeRule.onNodeWithText("ARES PERFORMANCE").assertIsDisplayed()
        composeRule.onNodeWithText("HEPHAESTUS • FOUNDATION").assertIsDisplayed()
        composeRule.onNodeWithText("WEEK 1 • DAY 1").assertIsDisplayed()
        composeRule.onNodeWithText("Lower A + easy aerobic").assertIsDisplayed()
        composeRule.onNodeWithTag("row_d1_squat").assertExists()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_d1_easy_aerobic"))
        composeRule.onNodeWithTag("row_d1_easy_aerobic").assertExists()
        composeRule.onNodeWithTag("nav_TODAY").assertExists()
        composeRule.onNodeWithTag("nav_WEEK").assertExists()
        composeRule.onNodeWithTag("nav_PROGRAM").assertExists()
        composeRule.onNodeWithTag("nav_TOOLS").assertExists()
    }

    @Test
    fun weekShowsAllSevenDaysWithoutChangingCurrentPointer() {
        composeRule.onNodeWithTag("nav_WEEK").performClick()
        (1..7).forEach { day ->
            composeRule.onNodeWithTag("week_screen").performScrollToNode(hasTestTag("week_day_$day"))
            composeRule.onNodeWithTag("week_day_$day").assertExists()
        }
        composeRule.onNodeWithTag("week_screen").performScrollToNode(hasTestTag("week_day_3"))
        composeRule.onNodeWithTag("week_day_3").performClick()
        composeRule.onNodeWithText("300 M BASELINE").assertExists()
        composeRule.onNodeWithText("CLOSE").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithText("WEEK 1 • DAY 1").assertIsDisplayed()
    }

    @Test
    fun programDisplaysGreekPhasesNextPhaseStatusAndThirtyTwoWeekBoundary() {
        composeRule.onNodeWithTag("nav_PROGRAM").performClick()
        composeRule.onNodeWithText("HEPHAESTUS — FOUNDATION").assertExists()
        composeRule.onNodeWithText("HERMES — DEVELOPMENT").assertExists()
        composeRule.onNodeWithText("ARES — SPECIFIC DEVELOPMENT").assertExists()
        composeRule.onNodeWithText("ZEUS — INTEGRATION / PEAK").assertExists()
        composeRule.onNodeWithTag("next_phase_status").assertExists()
        composeRule.onNodeWithText("NEXT PHASE • HERMES — DEVELOPMENT • POPULATED / APPROVED").assertExists()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_week_32"))
        composeRule.onNodeWithTag("program_week_32").performClick()
        composeRule.onNodeWithText("ZEUS • INTEGRATION / PEAK").assertExists()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_day_32_3"))
        composeRule.onNodeWithTag("program_day_32_3").performClick()
        composeRule.onNodeWithText("FINAL 300 M TEST").assertExists()
    }

    @Test
    fun currentPointerCanNavigateToWeekThirtyTwoWithoutPerformanceInput() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        store.save(ProgramUiState(currentWeek = 32, currentDay = 3))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("WEEK 32 • DAY 3").assertIsDisplayed()
        composeRule.onNodeWithText("ZEUS • INTEGRATION / PEAK").assertIsDisplayed()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_w32_final300"))
        composeRule.onNodeWithTag("row_w32_final300").assertExists()
        composeRule.onNodeWithText("FINAL 300 M TEST").assertExists()
    }

    @Test
    fun workoutModeAdvancesBinarySetStateWithoutLoadRepsOrRirEntry() {
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("workout_mode").assertIsDisplayed()
        composeRule.onNodeWithText("Back squat or front squat").assertIsDisplayed()
        composeRule.onNodeWithTag("set_done").performClick()
        composeRule.onNodeWithText("SET 2 / 4").assertExists()
        composeRule.onNodeWithTag("undo_workout").performClick()
        composeRule.onNodeWithText("SET 1 / 4").assertExists()
    }
}
