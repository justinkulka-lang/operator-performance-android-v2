package com.jk.operatorperformance

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class SetCompletionUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val lifecycle get() = LifecycleTestSupport(rule)
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val prefs get() = context.getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)
    private val squat: SessionRow = SessionResolver.resolve(1, 2, ProgramAuthority.V74, 1, 1).rows.single { it.title == "Back squat" }

    @Before fun prepare() {
        lifecycle.fontScale(1f)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
        prefs.edit().clear().commit()
        lifecycle.recreate("start_v72")
    }

    @After fun restore() {
        lifecycle.fontScale(1f)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
    }

    @Test fun ticksSurviveActivityRecreationAndStoreReload() {
        seedDay(2)
        rule.onNodeWithTag("start_resume_workout").performClick()
        lifecycle.screen("workout_mode")
        tapEffort(1)
        tapEffort(2)
        val chip = rule.onNodeWithTag("effort_${squat.id}_1").assertIsDisplayed().fetchSemanticsNode()
        var density = 1f
        lifecycle.withActivity { density = it.resources.displayMetrics.density }
        assertTrue(chip.boundsInRoot.width / density >= 48f)
        assertTrue(chip.boundsInRoot.height / density >= 48f)
        assertEffort(1, done = true)
        assertEffort(2, done = true)
        assertEffort(3, done = false)
        lifecycle.recreate("workout_mode")
        val reloaded = ProgramStateStore(context).loadWorkout()
        assertEquals(setOf("${squat.id}#1", "${squat.id}#2"), reloaded!!.completedEffortIds)
        assertNull(reloaded.restTimerEndEpochMillis)
        assertEquals(1, ProgramStateStore(context).load().currentWeek)
        assertEquals(2, ProgramStateStore(context).load().currentDay)
        assertEffort(1, done = true)
        assertEffort(2, done = true)
        assertEffort(4, done = false)
        rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag("return_today"))
        rule.onNodeWithTag("return_today").performClick()
        lifecycle.screen("today_screen")
        rule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("today_effort_progress_${squat.id}"))
        rule.onNodeWithTag("today_effort_progress_${squat.id}").assertTextContains("2/4")
    }

    @Test fun rowShowsCompleteAfterItsLastTick() {
        seedDay(2)
        rule.onNodeWithTag("start_resume_workout").performClick()
        lifecycle.screen("workout_mode")
        tapEffort(1)
        tapEffort(2)
        tapEffort(3)
        rule.onAllNodesWithTag("workout_row_complete_${squat.id}").assertCountEquals(0)
        tapEffort(4)
        assertEffort(4, done = true)
        rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag("workout_row_complete_${squat.id}"))
        rule.onNodeWithTag("workout_row_complete_${squat.id}").assertTextContains("COMPLETE")
    }

    @Test fun completeDayShowsThatDaysSealAndNoneOnTheCurrentDay() {
        seedDay(1)
        rule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("complete_day"))
        rule.onNodeWithTag("complete_day").performClick()
        lifecycle.screen("today_screen")
        val store = ProgramStateStore(context)
        assertEquals(2, store.load().currentDay)
        assertEquals(setOf("1:1"), store.sealedDays())
        assertNull(store.loadWorkout())
        rule.onNodeWithTag("nav_WEEK").performClick()
        lifecycle.screen("week_screen")
        rule.onNodeWithTag("week_screen").performScrollToNode(hasTestTag("week_seal_1"))
        rule.onNodeWithContentDescription("Day 1 sealed").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("Day 2 sealed").assertCountEquals(0)
    }

    @Test fun capturesMidCompleteAndWeekSealsAtBothFontScales() {
        assertEquals(4, squat.effortCount)
        for (scale in listOf(1f, 2f)) {
            seedDay(2)
            lifecycle.fontScale(scale)
            val suffix = if (scale == 1f) "font1" else "font2"
            rule.onNodeWithTag("start_resume_workout").performClick()
            lifecycle.screen("workout_mode")
            tapEffort(1)
            tapEffort(2)
            assertEffort(2, done = true)
            capture("workout-row-mid-$suffix")
            tapEffort(3)
            tapEffort(4)
            rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag("workout_row_complete_${squat.id}"))
            rule.onNodeWithTag("workout_row_complete_${squat.id}").assertTextContains("COMPLETE")
            capture("workout-row-complete-$suffix")
            rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag("return_today"))
            rule.onNodeWithTag("return_today").performClick()
            lifecycle.screen("today_screen")
            rule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("complete_day"))
            rule.onNodeWithTag("complete_day").performClick()
            rule.onNodeWithTag("confirm_incomplete_advance").performClick()
            lifecycle.screen("today_screen")
            rule.onNodeWithTag("nav_WEEK").performClick()
            lifecycle.screen("week_screen")
            rule.onNodeWithTag("week_screen").performScrollToNode(hasTestTag("week_seal_2"))
            rule.onNodeWithContentDescription("Day 2 sealed").assertIsDisplayed()
            rule.onAllNodesWithContentDescription("Day 3 sealed").assertCountEquals(0)
            capture("week-seals-$suffix")
        }
    }

    private fun seedDay(day: Int) {
        prefs.edit().clear().commit()
        ProgramStateStore(context).save(ProgramUiState().startV74().copy(currentDay = day))
        lifecycle.recreate("today_header")
    }

    private fun showEffort(index: Int) {
        val tag = "effort_${squat.id}_$index"
        rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag(tag))
    }

    private fun assertEffort(index: Int, done: Boolean) {
        val description = "Set $index of ${squat.effortCount}, ${if (done) "done" else "not done"}"
        showEffort(index)
        rule.onNode(hasTestTag("effort_${squat.id}_$index") and hasContentDescription(description))
            .assertIsDisplayed()
    }

    private fun tapEffort(index: Int) {
        showEffort(index)
        rule.onNodeWithTag("effort_${squat.id}_$index").performClick()
    }

    private fun capture(name: String) {
        rule.waitForIdle()
        val directory = File(requireNotNull(InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")))
        directory.mkdirs()
        val image = rule.onRoot().captureToImage().asAndroidBitmap()
        File(directory, "$name.png").outputStream().use { assertTrue(image.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        image.recycle()
    }
}
