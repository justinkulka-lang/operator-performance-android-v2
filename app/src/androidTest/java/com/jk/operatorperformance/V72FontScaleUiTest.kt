package com.jk.operatorperformance

import android.content.Context
import android.content.res.Configuration
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.io.FileInputStream

/**
 * Launches the activity only after the process font scale is 1.5.
 * The system setting is changed as well. This is an emulator check, not a Galaxy S26 Ultra pass.
 */
class V72FontScaleUiTest {
    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule val ruleChain: TestRule = RuleChain.outerRule(FontScaleRule(1.5f)).around(composeRule)

    @After fun restoreFontScale() {
        FontScaleRule(1.0f).applyScale()
    }

    @Test fun enlargedFontKeepsTheLongestSessionAndStartReachable() {
        val scale = composeRule.activity.resources.configuration.fontScale
        val systemScale = shell("settings get system font_scale").trim()
        assertTrue("activity fontScale=$scale system=$systemScale", scale >= 1.4f)
        composeRule.onNodeWithTag("start_v72").performScrollTo().assertIsDisplayed()
        val heightDp = composeRule.onNodeWithTag("start_v72").fetchSemanticsNode().boundsInRoot.height /
            composeRule.activity.resources.displayMetrics.density
        assertTrue("start height ${heightDp}dp", heightDp >= 48f)
        composeRule.onNodeWithTag("start_v72").performClick()
        composeRule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        val longest = (1..32).flatMap { week ->
            (1..7).map { day -> SessionResolver.resolve(week, day, ProgramAuthority.V72) }
        }.maxBy { session ->
            session.rows.sumOf { row ->
                row.title.length + row.prescription.length + (row.note?.length ?: 0) + row.alternatives.sumOf { it.length }
            }
        }
        composeRule.onNodeWithTag("nav_PROGRAM").performClick()
        composeRule.onNodeWithTag("program_search").performTextReplacement("week ${longest.week} day ${longest.day}")
        composeRule.activityRule.scenario.onActivity { activity ->
            val manager = activity.getSystemService(InputMethodManager::class.java)
            manager?.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("program_search_open_first").performScrollTo().performClick()
        composeRule.onNodeWithTag("day_prescription_${longest.rows.last().id}").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("CLOSE").performClick()
        composeRule.onNodeWithTag("nav_TODAY").performClick()
        composeRule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
    }

    private fun revealSearchResult(week: Int, day: Int) {
        val tag = "program_search_result_${week}_${day}"
        repeat(6) {
            if (composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()) {
                composeRule.onNodeWithTag(tag).performScrollTo()
                return
            }
            composeRule.onNodeWithTag("program_screen").performTouchInput {
                swipeUp(startY = bottom * 0.8f, endY = top + 80f)
            }
            composeRule.waitForIdle()
        }
    }

    private fun shell(command: String): String {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.executeShellCommand(command).use { descriptor ->
            return FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
        }
    }
}

private class FontScaleRule(private val scale: Float) : TestRule {
    override fun apply(base: Statement, description: Description): Statement = object : Statement() {
        override fun evaluate() {
            ApplicationProvider.getApplicationContext<Context>()
                .getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
            applyScale()
            base.evaluate()
        }
    }

    fun applyScale() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.executeShellCommand("settings put system font_scale $scale").use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
        }
        val context = instrumentation.targetContext
        val config = Configuration(context.resources.configuration)
        config.fontScale = scale
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
