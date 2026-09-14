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
import com.jk.operatorperformance.program.MachineModality
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ToolsIntegrationUiTest {
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
    fun machineInventoryPersistsAndControlsDayThreeFinisherReference() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("machine_STATIONARY_CYCLE"))
        composeRule.onNodeWithTag("machine_STATIONARY_CYCLE").performClick()
        composeRule.waitForIdle()

        val configured = ProgramStateStore(context).load()
        assertTrue(configured.machineInventoryConfigured)
        assertEquals(setOf(MachineModality.STATIONARY_CYCLE), configured.availableMachineModalities)

        composeRule.onNodeWithTag("nav_PROGRAM").performClick()
        composeRule.onNodeWithTag("program_week_2").performClick()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_day_3"))
        composeRule.onNodeWithTag("program_day_3").performClick()
        composeRule.onNodeWithText("Optional machine-power finisher — Stationary cycle ergometer").assertIsDisplayed()
        composeRule.onNodeWithTag("reference_dialog").performScrollToNode(hasText("CLOSE"))
        composeRule.onNodeWithText("CLOSE").performClick()

        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("machine_none_available"))
        composeRule.onNodeWithTag("machine_none_available").performClick()
        composeRule.waitForIdle()
        assertTrue(ProgramStateStore(context).load().availableMachineModalities.isEmpty())

        composeRule.onNodeWithTag("nav_PROGRAM").performClick()
        composeRule.onNodeWithTag("program_week_2").performClick()
        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_day_3"))
        composeRule.onNodeWithTag("program_day_3").performClick()
        composeRule.onNodeWithText("Optional machine-power finisher", substring = true).assertDoesNotExist()
    }

    @Test
    fun passFourUtilitiesAreReachableFromTools() {
        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("plate_math_tool"))
        composeRule.onNodeWithTag("plate_math_tool").assertIsDisplayed()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("program_notes_tool"))
        composeRule.onNodeWithTag("program_notes_tool").assertIsDisplayed()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("data_controls_tool"))
        composeRule.onNodeWithTag("data_controls_tool").assertIsDisplayed()
    }
}
