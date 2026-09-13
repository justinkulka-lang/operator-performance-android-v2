package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsConfiguration
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AccessibilityUiTest {
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
    fun workoutReadyExposesContextMovementSetPrescriptionAndPrimaryActionInOrder() {
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.waitForIdle()

        val contextNode = composeRule.onNodeWithTag("workout_context").fetchSemanticsNode()
        val movementNode = composeRule.onNodeWithTag("workout_movement").fetchSemanticsNode()
        val effortNode = composeRule.onNodeWithTag("workout_effort").fetchSemanticsNode()
        val prescriptionNode = composeRule.onNodeWithTag("workout_prescription").fetchSemanticsNode()
        val primaryNode = composeRule.onNodeWithTag("workout_primary").fetchSemanticsNode()

        assertTrue(semanticsText(contextNode.config).contains("WEEK 1 • DAY 1"))
        assertTrue(semanticsText(movementNode.config).contains("Back squat or front squat"))
        assertTrue(semanticsText(effortNode.config).contains("SET 1 OF 4"))
        assertTrue(semanticsText(prescriptionNode.config).contains("4 × 5–8 @ 2–3 RIR"))
        assertTrue(primaryNode.config.contains(SemanticsActions.OnClick))

        val tops = listOf(
            contextNode.boundsInRoot.top,
            movementNode.boundsInRoot.top,
            effortNode.boundsInRoot.top,
            prescriptionNode.boundsInRoot.top,
            primaryNode.boundsInRoot.top,
        )
        assertTrue("READY semantics must follow the visual reading order", tops.zipWithNext().all { (a, b) -> a <= b })
    }

    @Test
    fun restTimerExposesLiveTimerStateForAssistiveTechnology() {
        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("rest_timer_input").performTextReplacement("120")
        composeRule.onNodeWithTag("rest_timer_save").performClick()
        composeRule.onNodeWithTag("rest_timer_start").performClick()
        composeRule.waitForIdle()

        val config = composeRule.onNodeWithTag("utility_rest_countdown").fetchSemanticsNode().config
        assertEquals(listOf("Rest timer countdown"), config[SemanticsProperties.ContentDescription])
        assertTrue(config[SemanticsProperties.StateDescription].startsWith("Rest timer remaining "))
        assertEquals(LiveRegionMode.Polite, config[SemanticsProperties.LiveRegion])
    }

    private fun semanticsText(config: SemanticsConfiguration): String =
        config[SemanticsProperties.Text].joinToString(" ") { it.text }
}
