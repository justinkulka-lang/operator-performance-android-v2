package com.jk.operatorperformance

import android.content.Context
import android.content.pm.ActivityInfo
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
 * Exercises the reference UI, including a real font-scale change.
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
        rule.onNodeWithTag("start_v72").performClick()
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
        rule.onNodeWithTag("program_search_status").assertIsDisplayed()
        rule.onNodeWithTag("program_search_clear").performScrollTo().performClick()
        rule.onAllNodesWithTag("program_week_33").assertCountEquals(0)

        val longest = longestSession()
        rule.onNodeWithTag("program_week_${longest.week}").performScrollTo().performClick()
        rule.onNodeWithTag("program_day_${longest.week}_${longest.day}").performScrollTo().performClick()
        val last = longest.rows.last()
        rule.onNodeWithTag("day_prescription_${last.id}").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("CLOSE").performClick()

        rule.onNodeWithTag("program_week_32").performScrollTo().performClick()
        rule.onNodeWithTag("program_day_32_7").performScrollTo().performClick()
        rule.onNodeWithTag("set_current_day").performClick()
        rule.onNodeWithTag("confirm_set_current").performClick()
        rule.onAllNodesWithTag("next_week").assertCountEquals(0)
        rule.onNodeWithTag("nav_WEEK").performClick()
        rule.onNodeWithTag("week_day_7").performScrollTo().performClick()
        rule.onNodeWithText("Walking and ordinary daily activity only. No lifting and no running.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("CLOSE").performClick()
    }

    @Test fun enlargedFontKeepsTheLongestSessionAndStartReachable() {
        shell("settings put system font_scale 1.5")
        rule.activityRule.scenario.recreate()
        val scale = rule.activity.resources.configuration.fontScale
        assertTrue("fontScale=$scale", scale >= 1.4f)
        rule.onNodeWithTag("start_v72").assertIsDisplayed()
        assertTouchTarget("start_v72")
        rule.onNodeWithTag("start_v72").performClick()
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        val longest = longestSession()
        rule.onNodeWithTag("nav_PROGRAM").performClick()
        rule.onNodeWithTag("program_week_${longest.week}").performScrollTo().performClick()
        rule.onNodeWithTag("program_day_${longest.week}_${longest.day}").performScrollTo().performClick()
        rule.onNodeWithTag("day_prescription_${longest.rows.last().id}").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("CLOSE").performClick()
        rule.onNodeWithTag("nav_TODAY").performClick()
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
    }

    @Test fun narrowDarkRotationAirplaneAndSessionResume() {
        shell("wm density 560")
        shell("wm size 720x1280")
        shell("cmd uimode night yes")
        shell("cmd connectivity airplane-mode enable")
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("start_v72").performClick()
        rule.onNodeWithTag("start_resume_workout").performClick()
        rule.onNodeWithTag("workout_mode").assertIsDisplayed()
        rule.onAllNodesWithTag("set_done").assertCountEquals(0)
        rule.onNodeWithTag("workout_status").assertTextContains("SESSION ACTIVE", substring = true)
        rule.onNodeWithTag("start_rest_120").performScrollTo().performClick()
        rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
        rule.onNodeWithTag("return_today").performClick()
        rule.onNodeWithTag("saved_workout_status").assertIsDisplayed()
        rule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        rule.waitForIdle()
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        rule.onNodeWithTag("nav_TOOLS").performClick()
        rule.onNodeWithTag("rest_timer_tool").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("2:00").performScrollTo().performClick()
        rule.onNodeWithTag("rest_timer_start").performScrollTo().performClick()
        rule.onNodeWithTag("utility_rest_countdown").assertIsDisplayed()
        rule.onNodeWithTag("nav_WEEK").performClick()
        rule.onNodeWithTag("week_screen").assertIsDisplayed()
        rule.onNodeWithTag("nav_PROGRAM").performClick()
        rule.onNodeWithTag("program_screen").assertIsDisplayed()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("nav_TODAY").performClick()
        rule.onNodeWithTag("saved_workout_status").assertIsDisplayed()
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
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

    private fun shell(command: String): String {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.executeShellCommand(command).use { descriptor ->
            return FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
        }
    }
}
