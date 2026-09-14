package com.jk.operatorperformance

import android.content.Context
import android.os.ParcelFileDescriptor
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
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ReleaseReadinessUiTest {
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
    fun airplaneModeStillProvidesCompleteOfflineProgramReference() {
        assertEquals("1", shellOutput("settings get global airplane_mode_on"))
        composeRule.onNodeWithTag("today_screen").assertIsDisplayed()
        composeRule.onNodeWithText("ARES PERFORMANCE").assertIsDisplayed()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("workout_mode").assertIsDisplayed()
        composeRule.onNodeWithTag("set_done").assertIsDisplayed()
    }

    @Test
    fun binaryFocusUndoAndReturnRemainReachable() {
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("set_done").performClick()
        composeRule.onNodeWithTag("undo_workout").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("return_today").performScrollTo().assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("today_screen").assertIsDisplayed()
    }

    @Test
    fun finalProgramDayDoesNotSynthesizeWeekThirtyThree() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        store.save(ProgramUiState(currentWeek = 32, currentDay = 7))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("WEEK 32 • DAY 7").assertIsDisplayed()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasText("ARES PERFORMANCE • 32-WEEK PROGRAM COMPLETE"))
        composeRule.onNodeWithText("ARES PERFORMANCE • 32-WEEK PROGRAM COMPLETE").assertIsDisplayed()
        composeRule.onNodeWithText("WEEK 33", substring = true).assertDoesNotExist()
    }

    private fun shellOutput(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText().trim() }
    }
}
