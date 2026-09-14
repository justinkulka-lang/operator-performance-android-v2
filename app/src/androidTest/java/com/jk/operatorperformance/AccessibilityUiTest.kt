package com.jk.operatorperformance

import android.content.Context
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
import org.junit.After
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
    fun primaryWorkoutActionMeetsMinimumTouchTargetAndCoreContextIsReadable() {
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithText("WEEK 1 • DAY 1 • Lower A + easy aerobic").assertIsDisplayed()
        composeRule.onNodeWithText("Back squat or front squat").assertIsDisplayed()
        composeRule.onNodeWithText("SET 1 / 4").assertIsDisplayed()
        composeRule.onNodeWithText("4 × 5–8 @ 2–3 RIR").assertIsDisplayed()
        val target = composeRule.onNodeWithTag("set_done").fetchSemanticsNode().boundsInRoot
        assertTrue("Primary workout target must be at least 48 dp high", target.height >= 48f)
    }

    @Test
    fun maximumFontScaleKeepsPrimaryWorkoutControlReachable() {
        configureDisplay(widthPx = 360, heightPx = 640, densityDpi = 160, fontScale = 2.0f)
        composeRule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("start_resume_workout"))
        composeRule.onNodeWithTag("start_resume_workout").performClick()
        composeRule.onNodeWithTag("set_done").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("return_today").performScrollTo().assertIsDisplayed()
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
