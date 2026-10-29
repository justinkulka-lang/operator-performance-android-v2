package com.jk.operatorperformance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class Day4ExplosiveUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val lifecycle get() = LifecycleTestSupport(rule)
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val airBike = "v74_b1_w3_airbike"
    private val pushPress = "v74_b1_w3_landmine_push_press"

    @Before fun prepare() {
        context.getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE).edit().clear().commit()
        lifecycle.recreate("start_v72")
        ProgramStateStore(context).save(ProgramUiState().startAtPosition(1, 3, 4))
        lifecycle.recreate("today_header")
    }

    @Test fun todayShowsAirBikeAndPushPressAndTicksBothRows() {
        rule.onNodeWithTag("today_header").assertTextContains("Week 3 · I · Draft · Week 3 of 8")
        rule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_$airBike"))
        rule.onNodeWithText("Air-bike sprints").assertIsDisplayed()
        rule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("row_$pushPress"))
        rule.onNodeWithText("Landmine push press").assertIsDisplayed()
        capture("today-day4-week3")
        rule.onNodeWithTag("start_resume_workout").performClick()
        lifecycle.screen("workout_mode")
        repeat(6) { index ->
            val effort = index + 1
            val tag = "effort_${airBike}_$effort"
            rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag(tag))
            rule.onNode(hasTestTag(tag) and hasContentDescription("Rep $effort of 6, not done")).assertIsDisplayed()
            rule.onNodeWithTag(tag).performClick()
            rule.onNode(hasTestTag(tag) and hasContentDescription("Rep $effort of 6, done")).assertIsDisplayed()
        }
        repeat(3) { index ->
            val effort = index + 1
            val tag = "effort_${pushPress}_$effort"
            rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag(tag))
            rule.onNode(hasTestTag(tag) and hasContentDescription("Set $effort of 3, not done")).assertIsDisplayed()
            rule.onNodeWithTag(tag).performClick()
            rule.onNode(hasTestTag(tag) and hasContentDescription("Set $effort of 3, done")).assertIsDisplayed()
        }
        val workout = ProgramStateStore(context).loadWorkout()
        assertEquals(9, workout!!.completedEffortIds.size)
        rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag("return_today"))
        rule.onNodeWithTag("return_today").performClick()
        lifecycle.screen("today_screen")
        rule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("today_effort_progress_$airBike"))
        rule.onNodeWithTag("today_effort_progress_$airBike").assertTextContains("6/6")
        rule.onNodeWithTag("today_screen").performScrollToNode(hasTestTag("today_effort_progress_$pushPress"))
        rule.onNodeWithTag("today_effort_progress_$pushPress").assertTextContains("3/3")
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
