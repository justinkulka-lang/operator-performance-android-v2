package com.jk.operatorperformance

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Emulator coverage only. No activity reference survives a configuration change. */
class V72KeyUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val lifecycle get() = LifecycleTestSupport(rule)

    @Before fun reset() {
        lifecycle.fontScale(1f)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
        ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE)
            .edit().clear().commit()
        lifecycle.recreate("start_v72")
    }

    @After fun restore() {
        lifecycle.fontScale(1f)
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Configuration.ORIENTATION_PORTRAIT)
    }

    @Test fun keyOpensScrollsAndReturns() {
        openKey()
        revealText("Names the Upper A day, which mixes pressing and pulling.")
        rule.onNodeWithTag("key_screen").performScrollToNode(hasTestTag("key_state_red"))
        rule.onNodeWithTag("key_state_red").assertIsDisplayed()
        revealText("Easy aerobic follows when that week prescribes it.")
        returnButton()
        assertTools()
    }

    @Test fun systemBackReturnsToToolsWithoutFinishingActivity() {
        openKey()
        lifecycle.back()
        assertTools()
        lifecycle.withActivity { assertTrue(!it.isFinishing && !it.isDestroyed) }
    }

    @Test fun keySurvivesExplicitRecreationAndSystemBack() {
        openKey()
        lifecycle.recreate("key_screen")
        revealText("Names the Upper B day, which mixes pressing and pulling.")
        lifecycle.back()
        assertTools()
    }

    @Test fun keyRemainsReachableAtLargeFontAndLandscape() {
        // Wait for the font-setting recreation to finish before requesting rotation.
        lifecycle.fontScale(2f)
        rule.onNodeWithTag("start_v72").performScrollTo().assertIsDisplayed()
        var density = 1f
        lifecycle.withActivity { density = it.resources.displayMetrics.density }
        val startDp = rule.onNodeWithTag("start_v72").fetchSemanticsNode().boundsInRoot.height / density
        assertTrue("start height ${startDp}dp", startDp >= 56f)
        openKey()
        // Rotate the OPEN Key, rather than rotating the start gate then opening Key.
        lifecycle.orientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Configuration.ORIENTATION_LANDSCAPE)
        lifecycle.screen("key_screen")
        revealText("Names the Upper B day, which mixes pressing and pulling.")
        returnButton()
        assertTools()
        rule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("open_key"))
        rule.onNodeWithTag("open_key").performClick()
        lifecycle.screen("key_screen")
        lifecycle.back()
        assertTools()
    }

    private fun openKey() {
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("nav_TOOLS").performClick()
        assertTools()
        rule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag("open_key"))
        rule.onNodeWithTag("open_key").performClick()
        lifecycle.screen("key_screen")
    }

    private fun revealText(text: String) {
        rule.onNodeWithTag("key_screen").performScrollToNode(hasText(text, substring = true))
        rule.onNodeWithText(text, substring = true).assertIsDisplayed()
    }

    private fun returnButton() {
        rule.onNodeWithTag("key_screen").performScrollToNode(hasTestTag("key_back"))
        rule.onNodeWithTag("key_back").assertIsDisplayed().performClick()
    }

    private fun assertTools() {
        lifecycle.screen("tools_screen")
        rule.onNodeWithTag("tools_screen").assertIsDisplayed()
    }
}
