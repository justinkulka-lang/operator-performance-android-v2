package com.jk.operatorperformance

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertContentDescriptionEquals
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

/** Actual emulator captures, not physical-device evidence. Test-only output, no app storage change. */
class V72VisualEvidenceUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val lifecycle get() = LifecycleTestSupport(rule)
    private val prefs get() = ApplicationProvider.getApplicationContext<Context>()
        .getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)

    @Before fun reset() {
        lifecycle.fontScale(1f)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
        lifecycle.shell("cmd uimode night yes")
        lifecycle.configuration { it.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES }
        prefs.edit().clear().commit()
        lifecycle.recreate("start_v72")
    }

    @After fun restore() {
        lifecycle.fontScale(1f)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
    }

    @Test fun darkFontTwoPortraitAndLandscapeScreensAndStaticKey() {
        lifecycle.fontScale(2f)
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        for ((orientation, expected, name) in listOf(
            Triple(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT, "portrait"),
            Triple(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_LANDSCAPE, "landscape"),
        )) {
            lifecycle.orientation(orientation, expected)
            rule.onNodeWithTag("nav_PROGRAM").assertContentDescriptionEquals("PROGRAM")
            if (expected == Configuration.ORIENTATION_PORTRAIT) {
                rule.onNodeWithText("PLAN").assertIsDisplayed()
            }
            rule.onNodeWithTag("nav_TODAY").performClick()
            lifecycle.screen("today_screen")
            val primary = rule.onNodeWithTag("start_resume_workout").assertIsDisplayed().fetchSemanticsNode()
            var density = 1f
            lifecycle.withActivity { density = it.resources.displayMetrics.density }
            assertTrue(primary.boundsInRoot.height / density >= 56f)
            capture("today-font2-$name")
            rule.onNodeWithTag("nav_WEEK").performClick()
            lifecycle.screen("week_screen")
            capture("week-font2-$name")
            rule.onNodeWithTag("nav_PROGRAM").performClick()
            lifecycle.screen("program_screen")
            capture("program-font2-$name")
            rule.onNodeWithTag("nav_TODAY").performClick()
            rule.onNodeWithTag("start_resume_workout").performClick()
            lifecycle.screen("workout_mode")
            capture("workout-font2-$name")
            rule.onNodeWithTag("workout_mode").performScrollToNode(hasTestTag("return_today"))
            rule.onNodeWithTag("return_today").performClick()
            rule.onNodeWithTag("nav_TOOLS").performClick()
            rule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("open_key"))
            val beforeKey = prefs.all.toMap()
            rule.onNodeWithTag("open_key").performClick()
            lifecycle.screen("key_screen")
            capture("key-font2-$name")
            rule.onNodeWithTag("key_screen").performScrollToNode(hasTestTag("key_back"))
            rule.onNodeWithTag("key_back").assertIsDisplayed()
            capture("key-end-font2-$name")
            lifecycle.back()
            lifecycle.screen("tools_screen")
            assertEquals("Key and back must not persist any state", beforeKey, prefs.all.toMap())
        }
    }

    private fun capture(name: String) {
        rule.waitForIdle()
        val directory = File(requireNotNull(InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")))
        directory.mkdirs()
        val image = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(directory, "$name.png").outputStream().use { assertTrue(image.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        image.recycle()
    }
}
