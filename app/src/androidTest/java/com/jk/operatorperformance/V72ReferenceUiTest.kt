package com.jk.operatorperformance

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.FileInputStream

/**
 * Reference navigation on the emulator. Font scaling is covered by [V72FontScaleUiTest].
 * Passing this class is not a Galaxy S26 Ultra acceptance run.
 */
class V72ReferenceUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun reset() {
        clearPrefs()
        rule.activityRule.scenario.recreate()
    }

    @After fun restoreDeviceSettings() {
        shell("settings put system font_scale 1.0")
        shell("wm density reset")
        shell("wm size reset")
        shell("cmd uimode night yes")
        shell("cmd connectivity airplane-mode disable")
        rule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    @Test fun destinationsLongestSessionRestAndWeek32() {
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("today_header").assertTextContains("v7.2", substring = true)
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        assertTouchTarget("start_resume_workout")
        listOf("WEEK", "PROGRAM", "TOOLS", "TODAY").forEach { destination ->
            rule.onNodeWithTag("nav_$destination").performClick()
            assertTouchTarget("nav_$destination")
        }
        rule.onNodeWithTag("today_screen").assertIsDisplayed()
        rule.onAllNodesWithText("DAY 8", substring = true).assertCountEquals(0)
        rule.onAllNodesWithText("eight-day", substring = true).assertCountEquals(0)

        rule.onNodeWithTag("nav_PROGRAM").performClick()
        rule.onNodeWithTag("program_search").performTextInput("squat")
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("program_search_clear").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("program_search_status").assertIsDisplayed()
        rule.onNodeWithTag("program_search_clear").performScrollTo().performClick()
        rule.onAllNodesWithTag("program_week_33").assertCountEquals(0)

        val longest = longestSession()
        rule.onNodeWithTag("program_search").performTextInput("week ${longest.week} day ${longest.day}")
        hideIme()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("program_search_open_first").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("program_search_open_first").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("day_reference_scroll").fetchSemanticsNodes().isNotEmpty()
        }
        val last = longest.rows.last()
        revealOnScreen("day_reference_scroll", "day_prescription_${last.id}")
        rule.onNodeWithTag("day_prescription_${last.id}").assertIsDisplayed()
        rule.onNodeWithText("CLOSE").performClick()
        rule.onNodeWithTag("program_search_clear").performScrollTo().performClick()

        rule.onNodeWithTag("program_jump_week_32").performScrollTo().performClick()
        revealOnScreen("program_screen", "program_day_32_7")
        rule.onNodeWithTag("program_day_32_7").performClick()
        rule.onNodeWithTag("set_current_day").performClick()
        rule.onNodeWithTag("confirm_set_current").performClick()
        rule.onAllNodesWithTag("next_week").assertCountEquals(0)
        rule.onNodeWithTag("nav_WEEK").performClick()
        revealOnScreen("week_screen", "week_day_7")
        rule.onNodeWithTag("week_day_7").performClick()
        rule.onNodeWithText("Walking and ordinary daily activity only. No lifting and no running.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("CLOSE").performClick()
    }

    @Test fun narrowAndEnlargedDisplayKeepsResumeAndRestVisible() {
        shell("wm density 560")
        shell("wm size 720x1280")
        shell("cmd uimode night yes")
        rule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        Thread.sleep(1_000)
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("start_resume_workout").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        assertTouchTarget("start_resume_workout")
        rule.onNodeWithTag("start_resume_workout").performClick()
        rule.onNodeWithTag("workout_mode").assertIsDisplayed()
        rule.onAllNodesWithTag("set_done").assertCountEquals(0)
        revealInWorkout("start_rest_120")
        rule.onNodeWithTag("start_rest_120").performClick()
        rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
        revealInWorkout("return_today")
        rule.onNodeWithTag("return_today").performClick()
        rule.waitUntil(45_000) {
            rule.onAllNodesWithTag("today_screen").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
        assertOverlaps("today_screen", "saved_workout_status")
        rule.onNodeWithTag("nav_TOOLS").performClick()
        rule.waitUntil(20_000) {
            rule.onAllNodesWithTag("tools_screen").fetchSemanticsNodes().isNotEmpty()
        }
        assertOverlaps("tools_screen", "rest_timer_tool")
    }

    @Test fun rotationAirplaneResumeAndNavigation() {
        shell("cmd uimode night yes")
        shell("cmd connectivity airplane-mode enable")
        Thread.sleep(1_000)
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("start_resume_workout").performClick()
        rule.onNodeWithTag("workout_status").assertTextContains("SESSION ACTIVE", substring = true)
        revealInWorkout("start_rest_90")
        rule.onNodeWithTag("start_rest_90").performClick()
        rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
        revealInWorkout("return_today")
        rule.onNodeWithTag("return_today").performClick()
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
        rule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        rule.waitForIdle()
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        rule.onNodeWithTag("nav_WEEK").performClick()
        rule.onNodeWithTag("week_screen").assertIsDisplayed()
        rule.onNodeWithTag("nav_PROGRAM").performClick()
        rule.onNodeWithTag("program_screen").assertIsDisplayed()
        rule.onNodeWithTag("nav_TOOLS").performClick()
        revealOnScreen("tools_screen", "rest_choice_60")
        rule.onNodeWithTag("rest_choice_60").performClick()
        revealOnScreen("tools_screen", "rest_timer_start")
        rule.onNodeWithTag("rest_timer_start").performClick()
        assertOverlaps("tools_screen", "utility_rest_countdown")
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("nav_TODAY").performClick()
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
    }

    private fun revealInWorkout(tag: String) = revealOnScreen("workout_mode", tag)

    private fun assertOverlaps(screenTag: String, targetTag: String) {
        repeat(2) { pass ->
            repeat(10) {
                val screen = rule.onAllNodesWithTag(screenTag).fetchSemanticsNodes().firstOrNull()
                val nodes = rule.onAllNodesWithTag(targetTag).fetchSemanticsNodes()
                if (screen != null && nodes.any { it.boundsInRoot.height > 8f && it.boundsInRoot.overlaps(screen.boundsInRoot) }) {
                    return
                }
                if (screen != null) {
                    rule.onNodeWithTag(screenTag).performTouchInput {
                        if (pass == 0) swipeUp(startY = bottom * 0.8f, endY = top + 80f)
                        else swipeDown(startY = top + 80f, endY = bottom * 0.8f)
                    }
                }
                rule.waitForIdle()
            }
        }
        throw AssertionError("$targetTag is not visible inside $screenTag")
    }

    private fun revealOnScreen(screenTag: String, targetTag: String) {
        repeat(2) { pass ->
            repeat(8) {
                if (rule.onAllNodesWithTag(targetTag).fetchSemanticsNodes().isNotEmpty()) {
                    rule.onNodeWithTag(targetTag).performScrollTo()
                    return
                }
                rule.onNodeWithTag(screenTag).performTouchInput {
                    if (pass == 0) swipeUp(startY = bottom * 0.8f, endY = top + 80f)
                    else swipeDown(startY = top + 80f, endY = bottom * 0.8f)
                }
                rule.waitForIdle()
            }
        }
    }

    private fun longestSession() = (1..32).flatMap { week ->
        (1..7).map { day -> SessionResolver.resolve(week, day, ProgramAuthority.V72) }
    }.maxBy { session ->
        session.rows.sumOf { row ->
            row.title.length + row.prescription.length + (row.note?.length ?: 0) + row.alternatives.sumOf { it.length }
        }
    }

    private fun assertTouchTarget(tag: String) {
        val node = rule.onNodeWithTag(tag).fetchSemanticsNode()
        val density = rule.activity.resources.displayMetrics.density
        val heightDp = node.boundsInRoot.height / density
        val widthDp = node.boundsInRoot.width / density
        assertTrue("$tag height ${heightDp}dp", heightDp >= 48f)
        assertTrue("$tag width ${widthDp}dp", widthDp >= 48f)
    }

    private fun clearPrefs() {
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    private fun hideIme() {
        rule.activityRule.scenario.onActivity { activity ->
            activity.getSystemService(InputMethodManager::class.java)
                ?.hideSoftInputFromWindow(activity.window.decorView.windowToken, 0)
        }
        rule.waitForIdle()
    }

    private fun shell(command: String): String {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.executeShellCommand(command).use { descriptor ->
            return FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
        }
    }
}
