package com.jk.operatorperformance

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import org.junit.After
import org.junit.Assert.assertEquals
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
    private val lifecycle get() = LifecycleTestSupport(rule)

    @Before fun reset() {
        clearPrefs()
        lifecycle.recreate("start_v72")
    }

    @After fun restoreDeviceSettings() {
        lifecycle.fontScale(1f)
        shell("wm density reset")
        val physicalDensity = Regex("Physical density: (\\d+)").find(shell("wm density"))?.groupValues?.get(1)?.toInt()
        if (physicalDensity != null) lifecycle.configuration { it.densityDpi == physicalDensity }
        shell("wm size reset")
        lifecycle.configuration { true }
        shell("cmd uimode night yes")
        lifecycle.configuration { it.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES }
        shell("cmd connectivity airplane-mode disable")
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
    }

    @Test fun destinationsLongestSessionRestAndWeek32() {
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("today_header").assertTextContains("Week 1 · I · Draft · Week 1 of 8", substring = true)
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        assertPrimaryAction("start_resume_workout")
        rule.onNodeWithTag("today_screen").performTouchInput {
            swipeUp(startY = bottom * 0.8f, endY = bottom * 0.2f)
        }
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        rule.onAllNodesWithTag("set_done").assertCountEquals(0)
        rule.onAllNodesWithTag("load_field").assertCountEquals(0)
        rule.onAllNodesWithTag("rep_field").assertCountEquals(0)
        rule.onAllNodesWithTag("result_field").assertCountEquals(0)
        rule.onAllNodesWithText("LOG SET", substring = true).assertCountEquals(0)
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

        seedRetainedWeek32()
        rule.onNodeWithTag("nav_PROGRAM").performClick()
        rule.onNodeWithTag("program_jump_week_32").performScrollTo().performClick()
        revealOnScreen("program_screen", "program_day_32_7")
        rule.onNodeWithTag("program_day_32_7").performClick()
        rule.onNodeWithTag("set_current_day").performClick()
        rule.onNodeWithTag("confirm_set_current").performClick()
        rule.onAllNodesWithTag("next_week").assertCountEquals(0)
        rule.onNodeWithTag("nav_WEEK").performClick()
        revealOnScreen("week_screen", "week_day_7")
        rule.onNodeWithTag("week_day_7").performClick()
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("day_reference_scroll").fetchSemanticsNodes().isNotEmpty()
        }
        revealOnScreen("day_reference_scroll", "day_prescription_d7_rest")
        rule.onNodeWithTag("day_prescription_d7_rest").assertTextContains("Walking and ordinary daily activity only", substring = true)
        rule.onNodeWithText("CLOSE").performClick()
    }

    @Test fun narrowAndEnlargedDisplayKeepsResumeAndRestVisible() {
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
        shell("wm density 560")
        lifecycle.configuration { it.densityDpi == 560 }
        shell("wm size 720x1280")
        lifecycle.configuration { it.screenWidthDp in 200..220 }
        shell("cmd uimode night yes")
        lifecycle.configuration { it.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES }
        // START may be below the fold at this density; scroll before asserting visibility.
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithTag("start_resume_workout").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        assertPrimaryAction("start_resume_workout")
        for (destination in listOf("TODAY", "WEEK", "PROGRAM", "TOOLS")) {
            rule.onNodeWithTag("nav_$destination").assertIsDisplayed()
                .assertContentDescriptionEquals(destination)
            assertTouchTarget("nav_$destination")
        }
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
        lifecycle.configuration { it.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES }
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("start_resume_workout").performClick()
        rule.onNodeWithTag("workout_status").assertTextContains("SESSION ACTIVE", substring = true)
        revealInWorkout("start_rest_90")
        rule.onNodeWithTag("start_rest_90").performClick()
        rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
        revealInWorkout("return_today")
        rule.onNodeWithTag("return_today").performClick()
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_LANDSCAPE)
        rule.onNodeWithTag("start_resume_workout").assertIsDisplayed()
        rule.onNodeWithTag("nav_WEEK").performClick()
        rule.onNodeWithTag("week_screen").assertIsDisplayed()
        rule.onNodeWithTag("nav_PROGRAM").performClick()
        rule.onNodeWithTag("program_screen").assertIsDisplayed()
        rule.onNodeWithTag("nav_TOOLS").performClick()
        revealOnScreen("tools_screen", "rest_choice_60")
        rule.onNodeWithTag("rest_choice_60").performClick()
        val timerPrefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)
        assertEquals("Selected rest duration was not persisted", 60, timerPrefs.getInt("manualRestTimerSeconds", 0))
        revealOnScreen("tools_screen", "rest_timer_start")
        rule.onNodeWithTag("rest_timer_start").performClick()
        assertTrue("Timer start was not persisted: ${timerPrefs.all}",
            timerPrefs.getLong("utilityRestTimerEndEpochMillis", 0L) > System.currentTimeMillis())
        assertOverlaps("tools_screen", "utility_rest_countdown")
        lifecycle.recreate("tools_screen")
        rule.onNodeWithTag("nav_TODAY").performClick()
        rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME", substring = true)
    }

    @Test fun airplaneModeSearchReadsLocalSessions() {
        shell("cmd connectivity airplane-mode enable")
        val airplane = shell("settings get global airplane_mode_on").trim()
        assertTrue("airplane_mode_on=$airplane", airplane == "1")
        lifecycle.configuration { true }
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("nav_PROGRAM").performClick()
        rule.onNodeWithTag("program_search").performTextInput("Behind-body cable curl")
        hideIme()
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("program_search_open_first").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("program_search_open_first").performScrollTo().performClick()
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("day_reference_scroll").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("CLOSE").performClick()
        rule.onNodeWithTag("program_search_clear").performScrollTo().performClick()
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("program_search_open_first").fetchSemanticsNodes().isEmpty()
        }
        seedRetainedWeek32()
        rule.onNodeWithTag("nav_PROGRAM").performClick()
        rule.onNodeWithTag("program_search").performTextReplacement("week 32 day 2")
        hideIme()
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("program_search_open_first").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("program_search_open_first").performScrollTo().performClick()
        rule.waitUntil(8_000) {
            rule.onAllNodesWithTag("day_reference_scroll").fetchSemanticsNodes().isNotEmpty()
        }
        revealOnScreen("day_reference_scroll", "day_prescription_w32_d2")
        rule.onNodeWithTag("day_prescription_w32_d2").assertTextContains("very easy bike", substring = true)
        rule.onAllNodesWithTag("program_week_33").assertCountEquals(0)
        rule.onNodeWithText("CLOSE").performClick()
    }

    @Test fun coachingNotesAndLegacyExportAreReachable() {
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("nav_TOOLS").performClick()
        revealOnScreen("tools_screen", "program_notes_toggle")
        rule.onNodeWithTag("program_notes_toggle").performClick()
        rule.onNodeWithText("RIR means", substring = true).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Two consecutive unattained", substring = true).performScrollTo().assertIsDisplayed()
        revealOnScreen("tools_screen", "sprint_reference_150")
        rule.onNodeWithText("23.0 s").assertIsDisplayed()
        revealOnScreen("tools_screen", "sprint_reference_300")
        rule.onNodeWithText("46.0 s").assertIsDisplayed()
        rule.onNodeWithText("These 300 m entries", substring = true).performScrollTo().assertIsDisplayed()
        revealOnScreen("tools_screen", "legacy_export_scope")
        rule.onNodeWithTag("legacy_export_scope").assertTextContains("Excludes current program authority, pointer, session status, substitutions, and timers.", substring = true)
        revealOnScreen("tools_screen", "export_data")
        rule.onNodeWithTag("export_data").assertTextContains("EXPORT LEGACY DATA")
    }

    private fun revealInWorkout(tag: String) = revealOnScreen("workout_mode", tag)

    private fun assertOverlaps(screenTag: String, targetTag: String) {
        // These screens are lazy lists. Resolve the tagged child directly rather
        // than guessing how many physical-pixel swipes fit a landscape viewport.
        rule.onNodeWithTag(screenTag).performScrollToNode(hasTestTag(targetTag))
        rule.onNodeWithTag(targetTag).assertIsDisplayed()
        val screen = rule.onNodeWithTag(screenTag).fetchSemanticsNode()
        val target = rule.onNodeWithTag(targetTag).fetchSemanticsNode()
        assertTrue(
            "$targetTag is not visible inside $screenTag",
            target.boundsInRoot.height > 8f && target.boundsInRoot.overlaps(screen.boundsInRoot),
        )
    }

    private fun revealOnScreen(screenTag: String, targetTag: String) {
        if (screenTag == "day_reference_scroll") {
            rule.onNodeWithTag(targetTag).performScrollTo().assertIsDisplayed()
        } else {
            rule.onNodeWithTag(screenTag).performScrollToNode(hasTestTag(targetTag))
            rule.onNodeWithTag(targetTag).assertIsDisplayed()
        }
    }

    private fun seedRetainedWeek32() {
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .putBoolean("v71Initialized", true)
            .putInt("currentWeek", 32)
            .putInt("currentDay", 1)
            .putString("authorityVersion", "V72")
            .putBoolean("v72MigrationApplied", true)
            .putInt("v72MigrationRevision", 2)
            .putBoolean("v72ActivationProven", true)
            .putBoolean("v74MigrationApplied", true)
            .putInt("v74ActivationWeek", 33)
            .putStringSet("historicalWeekAuthorities", (1..32).map { "$it:V72" }.toSet())
            .commit()
        lifecycle.recreate("today_header")
    }

    private fun longestSession() = (1..32).flatMap { week ->
        (1..7).map { day -> SessionResolver.resolve(week, day, ProgramAuthority.V74) }
    }.maxBy { session ->
        session.rows.sumOf { row ->
            row.title.length + row.prescription.length + (row.note?.length ?: 0) + row.alternatives.sumOf { it.length }
        }
    }

    private fun assertPrimaryAction(tag: String) {
        val node = rule.onNodeWithTag(tag).fetchSemanticsNode()
        val density = rule.activity.resources.displayMetrics.density
        val heightDp = node.boundsInRoot.height / density
        val widthDp = node.boundsInRoot.width / density
        assertTrue("$tag height ${heightDp}dp", heightDp >= 56f)
        assertTrue("$tag width ${widthDp}dp", widthDp >= 56f)
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
