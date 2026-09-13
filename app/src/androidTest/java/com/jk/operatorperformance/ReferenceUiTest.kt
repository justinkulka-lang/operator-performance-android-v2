package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.SpeedVariant
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReferenceUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("operator_v54_reference_state", Context.MODE_PRIVATE)
            .edit().clear().commit()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
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
    fun authorizedReferenceStatePersistsWithoutPerformanceData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        val expected = ProgramUiState(
            currentWeek = 6,
            currentDay = 6,
            speedVariant = SpeedVariant.MAX_VELOCITY,
            engineVariant = EngineVariant.VO2,
            deloadMode = true,
        )
        store.save(expected)
        assertEquals(expected, ProgramStateStore(context).load())
    }
}
