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

class MasterAuditUiTest {
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
    fun phaseTwoCheckpointIsReferenceOnlyAndNoResultFieldExists() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ProgramStateStore(context).save(ProgramUiState(currentWeek = 16, currentDay = 3))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("HERMES • DEVELOPMENT").assertIsDisplayed()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_w16_checkpoint"))
        composeRule.onNodeWithText("300 M CHECKPOINT").assertExists()
        composeRule.onNodeWithText("One timed 300 m after normal sprint build-ups. Result is not stored.").assertExists()
        composeRule.onNodeWithText("ENTER RESULT", substring = true).assertDoesNotExist()
    }

    @Test
    fun weekTwentyFourCheckpointAndWeekThirtyTwoFinalsArePopulated() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        store.save(ProgramUiState(currentWeek = 24, currentDay = 6))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ARES • SPECIFIC DEVELOPMENT").assertIsDisplayed()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_w24_checkpoint"))
        composeRule.onNodeWithText("1.5-MILE CHECKPOINT").assertExists()

        store.save(ProgramUiState(currentWeek = 32, currentDay = 6))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("ZEUS • INTEGRATION / PEAK").assertIsDisplayed()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_w32_final15"))
        composeRule.onNodeWithText("FINAL 1.5-MILE TEST").assertExists()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_d6_incline"))
        composeRule.onNodeWithTag("prescription_d6_incline").assertExists()
    }

    @Test
    fun manualDeloadSuppressesSprintSessionAndKeepsReferenceFirstFlow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        ProgramStateStore(context).save(ProgramUiState(currentWeek = 21, currentDay = 3, deloadMode = true))
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("DELOAD MODE").assertIsDisplayed()
        composeRule.onNodeWithText("Speed deload").assertExists()
        composeRule.onNodeWithText("SPECIAL ENDURANCE").assertDoesNotExist()
    }

    @Test
    fun toolsExposeAllFourFinisherEquipmentOptions() {
        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("machine_inventory_tool"))
        composeRule.onNodeWithTag("machine_inventory_tool").assertIsDisplayed()
        listOf("AIR_FAN_BIKE", "STATIONARY_CYCLE", "ROW_ERG", "SKI_ERG").forEach { modality ->
            composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("machine_$modality"))
            composeRule.onNodeWithTag("machine_$modality").assertExists()
        }
    }
}
