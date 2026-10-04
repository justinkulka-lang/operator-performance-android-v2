package com.jk.operatorperformance

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.FileInputStream

/**
 * Key is static presentation. These checks are emulator coverage, not a Galaxy S26 Ultra pass.
 */
class V72KeyUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun reset() {
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        shell("settings put system font_scale 1.0")
        shell("wm density reset")
        shell("wm size reset")
        rule.activityRule.scenario.recreate()
    }

    @After fun restore() {
        shell("settings put system font_scale 1.0")
        rule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    @Test fun keyOpensScrollsAndReturns() {
        openKey()
        revealText("key_screen", "Names the Upper A day, which mixes pressing and pulling.")
        rule.onNodeWithText("Names the Upper A day, which mixes pressing and pulling.", substring = true).assertIsDisplayed()
        reveal("key_screen", "key_state_red")
        rule.onNodeWithTag("key_state_red").assertIsDisplayed()
        revealText("key_screen", "Easy aerobic follows when that week prescribes it.")
        rule.onNodeWithText("Easy aerobic follows when that week prescribes it.", substring = true).assertIsDisplayed()
        reveal("key_screen", "key_back")
        rule.onNodeWithTag("key_back").assertIsDisplayed().performClick()
        rule.onNodeWithTag("tools_screen").assertIsDisplayed()
        reveal("tools_screen", "open_key")
        rule.onNodeWithTag("open_key").assertIsDisplayed()
    }

    @Test(timeout = 180_000) fun keyRemainsReachableAtLargeFontAndLandscape() {
        shell("settings put system font_scale 2.0")
        rule.activityRule.scenario.recreate()
        rule.activityRule.scenario.onActivity { activity ->
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        rule.waitForIdle()
        rule.onNodeWithTag("start_v72").performScrollTo().assertIsDisplayed()
        val startDp = rule.onNodeWithTag("start_v72").fetchSemanticsNode().boundsInRoot.height /
            rule.activity.resources.displayMetrics.density
        assertTrue("start height ${startDp}dp", startDp >= 56f)
        openKey()
        reveal("key_screen", "key_back")
        rule.onNodeWithTag("key_back").assertIsDisplayed()
        revealText("key_screen", "Names the Upper B day, which mixes pressing and pulling.")
        rule.onNodeWithText("Names the Upper B day, which mixes pressing and pulling.", substring = true).assertIsDisplayed()
        reveal("key_screen", "key_back")
        rule.onNodeWithTag("key_back").performClick()
        rule.waitUntil(10_000) {
            val screen = rule.onAllNodesWithTag("tools_screen").fetchSemanticsNodes().firstOrNull() ?: return@waitUntil false
            screen.boundsInRoot.width > 8f && screen.boundsInRoot.height > 8f
        }
        rule.onNodeWithTag("tools_screen").assertIsDisplayed()
        reveal("tools_screen", "open_key")
        rule.onNodeWithTag("open_key").assertIsDisplayed()
    }

    private fun openKey() {
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("nav_TOOLS").performClick()
        reveal("tools_screen", "open_key")
        rule.onNodeWithTag("open_key").performClick()
        rule.onNodeWithTag("key_screen").assertIsDisplayed()
    }

    private fun revealText(screenTag: String, substring: String) {
        repeat(3) { pass ->
            repeat(8) {
                val nodes = rule.onAllNodesWithText(substring, substring = true).fetchSemanticsNodes()
                if (nodes.isNotEmpty()) {
                    try {
                        rule.onNodeWithText(substring, substring = true).performScrollTo()
                    } catch (_: AssertionError) {
                        // A lazy row can leave the tree while the scroll settles.
                    }
                }
                val screen = rule.onAllNodesWithTag(screenTag).fetchSemanticsNodes().firstOrNull()
                val shown = screen != null && rule.onAllNodesWithText(substring, substring = true).fetchSemanticsNodes().any {
                    it.boundsInRoot.height > 8f && it.boundsInRoot.overlaps(screen.boundsInRoot)
                }
                if (shown) return
                rule.onNodeWithTag(screenTag).performTouchInput {
                    if (pass % 2 == 0) swipeUp(startY = bottom * 0.8f, endY = top + 80f)
                    else swipeDown(startY = top + 80f, endY = bottom * 0.8f)
                }
                rule.waitForIdle()
            }
        }
        rule.onNodeWithText(substring, substring = true).performScrollTo().assertIsDisplayed()
    }

    private fun reveal(screenTag: String, targetTag: String) {
        repeat(2) { pass ->
            repeat(8) {
                val screen = rule.onAllNodesWithTag(screenTag).fetchSemanticsNodes().firstOrNull()
                val visible = screen != null && rule.onAllNodesWithTag(targetTag).fetchSemanticsNodes().any {
                    it.boundsInRoot.height > 8f && it.boundsInRoot.overlaps(screen.boundsInRoot)
                }
                if (visible) return
                if (rule.onAllNodesWithTag(targetTag).fetchSemanticsNodes().isNotEmpty()) {
                    try {
                        rule.onNodeWithTag(targetTag).performScrollTo()
                    } catch (_: AssertionError) {
                        // A lazy row can leave the tree while the scroll settles.
                    }
                    val after = rule.onAllNodesWithTag(screenTag).fetchSemanticsNodes().firstOrNull()
                    val shown = after != null && rule.onAllNodesWithTag(targetTag).fetchSemanticsNodes().any {
                        it.boundsInRoot.height > 8f && it.boundsInRoot.overlaps(after.boundsInRoot)
                    }
                    if (shown) return
                }
                rule.onNodeWithTag(screenTag).performTouchInput {
                    if (pass == 0) swipeUp(startY = bottom * 0.8f, endY = top + 80f)
                    else swipeDown(startY = top + 80f, endY = bottom * 0.8f)
                }
                rule.waitForIdle()
            }
        }
        rule.onNodeWithTag(targetTag).performScrollTo()
    }

    private fun shell(command: String) {
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
        }
    }
}
