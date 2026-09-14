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
import com.jk.operatorperformance.program.MachineModality
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
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
        context.getSharedPreferences("operator_v54_reference_state", Context.MODE_PRIVATE).edit().clear().commit()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @Test
    fun machineInventoryPersistsAndControlsOptionalFinisher() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        store.save(ProgramUiState(currentWeek = 9, currentDay = 3))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("machine_ROW_ERG"))
        composeRule.onNodeWithTag("machine_ROW_ERG").performClick()
        composeRule.waitForIdle()
        val configured = store.load()
        assertTrue(configured.machineInventoryConfigured)
        assertEquals(setOf(MachineModality.ROW_ERG), configured.availableMachineModalities)

        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_d3_machine_power_finisher"))
        composeRule.onNodeWithText("Optional machine-power finisher — Row ergometer").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("machine_none_available"))
        composeRule.onNodeWithTag("machine_none_available").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithTag("row_d3_machine_power_finisher").assertDoesNotExist()
    }

    @Test
    fun approvedUtilitiesAreReachableFromTools() {
        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("phase1_reference_selectors"))
        composeRule.onNodeWithTag("phase1_reference_selectors").assertIsDisplayed()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("rest_timer_tool"))
        composeRule.onNodeWithTag("rest_timer_tool").assertIsDisplayed()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("plate_math_tool"))
        composeRule.onNodeWithTag("plate_math_tool").assertIsDisplayed()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("program_notes_tool"))
        composeRule.onNodeWithTag("program_notes_tool").assertIsDisplayed()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("data_controls_tool"))
        composeRule.onNodeWithTag("data_controls_tool").assertIsDisplayed()
    }
}
