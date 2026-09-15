package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.After
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
        resetDisplay()
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("operator_v54_reference_state", Context.MODE_PRIVATE).edit().clear().commit()
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    @After
    fun restoreDisplay() = resetDisplay()

    @Test
    fun focusModeExposesContextMovementSetPrescriptionAndPrimaryAction() {
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()

        val context = composeRule.onNodeWithTag("workout_context").fetchSemanticsNode()
        val movement = composeRule.onNodeWithTag("workout_movement").fetchSemanticsNode()
        val effort = composeRule.onNodeWithTag("workout_effort").fetchSemanticsNode()
        val prescription = composeRule.onNodeWithTag("workout_prescription").fetchSemanticsNode()
        val primary = composeRule.onNodeWithTag("set_done").fetchSemanticsNode()

        assertTrue(context.config[SemanticsProperties.Text].joinToString(" ") { it.text }.contains("WEEK 1 • DAY 1"))
        assertTrue(movement.config[SemanticsProperties.Text].joinToString(" ") { it.text }.contains("Back squat or front squat"))
        assertTrue(effort.config[SemanticsProperties.Text].joinToString(" ") { it.text }.contains("SET 1 / 4"))
        assertTrue(prescription.config[SemanticsProperties.Text].joinToString(" ") { it.text }.contains("4 × 5–8 @ 2–3 RIR"))
        assertTrue(primary.config.contains(SemanticsActions.OnClick))
        assertTrue("Primary workout target must be at least 48 dp high", primary.boundsInRoot.height >= 48f)
    }

    @Test
    fun independentRestTimerPublishesTalkBackTimerState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = ProgramStateStore(context)
        store.save(
            store.load().copy(
                restTimerSeconds = 120,
                utilityRestTimerEndEpochMillis = System.currentTimeMillis() + 120_000L,
            ),
        )
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("nav_TOOLS").performClick()
        composeRule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("utility_rest_countdown"))

        val config = composeRule.onNodeWithTag("utility_rest_countdown").fetchSemanticsNode().config
        assertEquals(listOf("Rest timer countdown"), config[SemanticsProperties.ContentDescription])
        assertTrue(config[SemanticsProperties.StateDescription].startsWith("Rest timer remaining "))
        assertEquals(LiveRegionMode.Polite, config[SemanticsProperties.LiveRegion])
    }

    @Test
    fun maximumFontScaleKeepsPrimaryWorkoutControlReachable() {
        configureDisplay(widthPx = 360, heightPx = 640, densityDpi = 160, fontScale = 2.0f)
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()

        composeRule.onNodeWithTag("set_done").assertIsDisplayed()
        composeRule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag("return_today"))
        composeRule.onNodeWithTag("return_today").assertIsDisplayed()
    }

    @Test
    fun maximumFontScaleKeepsProgramSearchAndQuickJumpReachable() {
        configureDisplay(widthPx = 360, heightPx = 640, densityDpi = 160, fontScale = 2.0f)
        composeRule.onNodeWithTag("nav_PROGRAM").performClick()

        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_search"))
        composeRule.onNodeWithTag("program_search").assertIsDisplayed()
        val search = composeRule.onNodeWithTag("program_search").fetchSemanticsNode()
        assertTrue("Program search must retain a large physical target", search.boundsInRoot.height >= 48f)

        composeRule.onNodeWithTag("program_screen").performScrollToNode(hasTestTag("program_jump_week_1"))
        composeRule.onNodeWithTag("program_jump_week_1").assertIsDisplayed()
    }

    @Test
    fun workoutResetControlMeetsPhysicalTouchTargetMinimum() {
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("effort_d1_squat_1"))
        composeRule.onNodeWithTag("effort_d1_squat_1").performClick()
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("reset_today_workout"))

        val reset = composeRule.onNodeWithTag("reset_today_workout").fetchSemanticsNode()
        assertTrue(reset.config.contains(SemanticsActions.OnClick))
        assertTrue("Workout reset target must be at least 48 dp high", reset.boundsInRoot.height >= 48f)
    }

    @Test
    fun navigationPrimaryTargetsRemainReachable() {
        listOf("nav_TODAY", "nav_WEEK", "nav_PROGRAM", "nav_TOOLS").forEach { tag ->
            composeRule.onNodeWithTag(tag).assertIsDisplayed()
            assertTrue(composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.height >= 48f)
        }
    }

    private fun configureDisplay(widthPx: Int, heightPx: Int, densityDpi: Int, fontScale: Float) {
        shell("wm size ${widthPx}x$heightPx")
        shell("wm density $densityDpi")
        shell("settings put system font_scale $fontScale")
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()
    }

    private fun resetDisplay() {
        shell("wm size reset")
        shell("wm density reset")
        shell("settings put system font_scale 1.0")
    }

    private fun shell(command: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).close()
    }
}
