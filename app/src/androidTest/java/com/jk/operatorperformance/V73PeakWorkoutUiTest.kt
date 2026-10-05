package com.jk.operatorperformance

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Peak reference execution on the emulator, never physical-device certification. */
class V73PeakWorkoutUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val lifecycle get() = LifecycleTestSupport(rule)
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val store get() = ProgramStateStore(context)

    @Before fun prepare() {
        lifecycle.fontScale(1f)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
        context.getSharedPreferences("ares_v71_reference_state",Context.MODE_PRIVATE).edit().clear().commit()
        lifecycle.recreate("start_v72")
        lifecycle.fontScale(2f)
    }

    @After fun restore() {
        lifecycle.fontScale(1f)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
        context.getSharedPreferences("ares_v71_reference_state",Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun peakLowerThirtySetsRemainsUsableAndResumesAtFontTwo() = verifyPeak(2,30)
    @Test fun peakSpeedThrowsAndUpperCTwentySixSetsRemainUsableAtFontTwo() = verifyPeak(4,26)

    private fun verifyPeak(day: Int, expectedSets: Int) {
        val session = SessionResolver.resolve(17,day,ProgramAuthority.V73)
        assertEquals(expectedSets,session.rows.filter { it.kind == SessionRowKind.RESISTANCE }.sumOf { it.effortCount })
        val last = session.rows.last { it.kind == SessionRowKind.RESISTANCE }
        for ((orientation, configuration, name) in listOf(
            Triple(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,Configuration.ORIENTATION_PORTRAIT,"portrait"),
            Triple(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,Configuration.ORIENTATION_LANDSCAPE,"landscape"),
        )) {
            store.clearWorkout()
            store.save(ProgramUiState().startV73().withCurrentPointer(17,day))
            lifecycle.recreate("today_header")
            lifecycle.orientation(orientation,configuration)
            lifecycle.withActivity { assertTrue(it.resources.configuration.fontScale >= 1.99f) }
            rule.onNodeWithTag("today_header").assertTextContains("v7.3",substring=true)
            rule.onNodeWithTag("start_resume_workout").assertIsDisplayed().performClick()
            lifecycle.screen("workout_mode")
            if (day == 4) {
                val tag = "workout_prescription_v73_w17_throws"
                reveal(tag)
                rule.onNodeWithTag(tag).assertTextContains("Nine total throws, both sides combined",substring=true)
                capture("peak-w17-d4-preparation-font2-$name")
            }
            reveal("workout_prescription_${last.id}")
            rule.onNodeWithTag("workout_prescription_${last.id}").assertTextContains(last.prescription,substring=true)
            capture("peak-w17-d$day-last-row-font2-$name")
            val choice = "workout_alternative_${last.id}_0"
            reveal(choice)
            rule.onNodeWithTag(choice).performClick()
            assertEquals(last.alternatives.first(),store.loadWorkout()!!.selectedSubstitutions[last.id])
            reveal("start_rest_180")
            rule.onNodeWithTag("start_rest_180").performClick()
            rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
            val deadline = store.loadWorkout()!!.restTimerEndEpochMillis
            assertTrue(requireNotNull(deadline) > System.currentTimeMillis())
            lifecycle.recreate("workout_mode")
            rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
            assertEquals(deadline,store.loadWorkout()!!.restTimerEndEpochMillis)
            assertEquals(last.alternatives.first(),store.loadWorkout()!!.selectedSubstitutions[last.id])
            capture("peak-w17-d$day-timer-font2-$name")
            reveal("mark_session_complete")
            rule.onNodeWithTag("mark_session_complete").assertIsDisplayed()
            capture("peak-w17-d$day-controls-font2-$name")
            reveal("return_today")
            rule.onNodeWithTag("return_today").performClick()
            lifecycle.screen("today_screen")
            rule.onNodeWithTag("start_resume_workout").assertTextContains("RESUME",substring=true).performClick()
            lifecycle.screen("workout_mode")
            assertEquals(last.alternatives.first(),store.loadWorkout()!!.selectedSubstitutions[last.id])
            reveal("mark_session_complete")
            rule.onNodeWithTag("mark_session_complete").performClick()
            lifecycle.screen("today_screen")
            assertTrue(store.loadWorkout()!!.complete)
            assertNull(store.loadWorkout()!!.restTimerEndEpochMillis)
            assertEquals(17,store.load().currentWeek)
            assertEquals(day,store.load().currentDay)
        }
    }

    private fun reveal(tag: String) {
        rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag(tag))
        rule.onNodeWithTag(tag).assertIsDisplayed()
    }

    private fun capture(name: String) {
        rule.waitForIdle()
        val output = File(requireNotNull(InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")))
        output.mkdirs()
        val bitmap = rule.onRoot().captureToImage().asAndroidBitmap()
        File(output,"$name.png").outputStream().use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,it)) }
        bitmap.recycle()
    }
}
