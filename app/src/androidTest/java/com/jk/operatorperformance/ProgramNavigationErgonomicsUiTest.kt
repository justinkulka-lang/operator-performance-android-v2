package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProgramNavigationErgonomicsUiTest {
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
    fun programSearchJumpsDirectlyToApprovedSessionReference() {
        composeRule.onNodeWithTag("nav_PROGRAM").performClick()
        composeRule.onNodeWithTag("program_search").performTextInput("W17 D3")
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_search_result_17_3"))
        composeRule.onNodeWithTag("program_search_result_17_3").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("set_current_day").assertIsDisplayed()
    }

    @Test
    fun resetWorkoutRequiresConfirmationAndClearsOnlyExecutionState() {
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("effort_d1_squat_1"))
        composeRule.onNodeWithTag("effort_d1_squat_1").performClick()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("saved_workout_status"))
        composeRule.onNodeWithTag("saved_workout_status").assertIsDisplayed()

        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("reset_today_workout"))
        val reset = composeRule.onNodeWithTag("reset_today_workout")
        assertTrue(reset.fetchSemanticsNode().boundsInRoot.height >= 48f)
        reset.performClick()
        composeRule.onNodeWithTag("confirm_reset_workout").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("saved_workout_status").assertDoesNotExist()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
    }

    @Test
    fun buildIntegrityPanelIsAvailableWithoutNetworkOrSigningSetup() {
        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("build_integrity_card"))
        composeRule.onNodeWithTag("build_integrity_card").assertIsDisplayed()
        composeRule.onNodeWithTag("authority_sha").assertIsDisplayed()
    }
}
