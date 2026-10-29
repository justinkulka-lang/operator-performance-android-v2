package com.jk.operatorperformance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class DossierUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val lifecycle get() = LifecycleTestSupport(rule)
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @After fun restoreFont() { lifecycle.fontScale(1f) }

    @Before fun clear() {
        context.getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE).edit().clear().commit()
        lifecycle.fontScale(1f)
        lifecycle.recreate("start_v72")
    }

    @Test fun dossierPositionNotesKeyAndAbout() {
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("nav_DOSSIER").performClick()
        lifecycle.screen("dossier_screen")
        rule.onNodeWithTag("dossier_position_header").assertTextContains("Week 1 · I · Draft · Week 1 of 8")
        capture("dossier-font1")
        rule.onNodeWithTag("dossier_screen").performScrollToNode(hasTestTag("change_position"))
        rule.onNodeWithTag("change_position").performClick()
        rule.onNodeWithTag("position_block_2").performScrollTo().performClick()
        rule.onNodeWithTag("position_week_3").performScrollTo().performClick()
        rule.onNodeWithTag("position_day_2").performScrollTo().performClick()
        rule.onNodeWithTag("position_continue").performClick()
        rule.onNodeWithTag("confirm_start_position").performClick()
        lifecycle.screen("today_header")
        rule.onNodeWithTag("today_header").assertTextContains("Week 11 · II · Build · Week 3 of 8")
        val moved = ProgramStateStore(context).load()
        assertEquals(11, moved.currentWeek)
        assertEquals(2, moved.v74Block)
        assertEquals(3, moved.v74BlockWeek)
        assertEquals(2, moved.currentDay)

        rule.onNodeWithTag("nav_DOSSIER").performClick()
        lifecycle.screen("dossier_screen")
        rule.onNodeWithTag("dossier_position_header").assertTextContains("Week 11 · II · Build · Week 3 of 8")
        rule.onNodeWithTag("dossier_screen").performScrollToNode(hasTestTag("start_over"))
        rule.onNodeWithTag("start_over").performClick()
        rule.onNodeWithTag("start_over_cancel").performClick()
        assertEquals(11, ProgramStateStore(context).load().currentWeek)
        rule.onNodeWithTag("start_over").performClick()
        rule.onNodeWithTag("confirm_start_over").performClick()
        lifecycle.screen("today_header")
        rule.onNodeWithTag("today_header").assertTextContains("Week 1 · I · Draft · Week 1 of 8")
        assertEquals(1, ProgramStateStore(context).load().currentWeek)
        assertEquals(1, ProgramStateStore(context).load().currentDay)

        rule.onNodeWithTag("nav_DOSSIER").performClick()
        lifecycle.screen("dossier_screen")
        rule.onNodeWithTag("dossier_screen").performScrollToNode(hasTestTag("program_notes_toggle"))
        rule.onNodeWithTag("program_notes_toggle").performClick()
        listOf("Week structure", "Progression", "Autoregulation", "Running effort guide").forEach { title ->
            rule.onNodeWithTag("dossier_screen").performScrollToNode(hasText(title))
            rule.onNodeWithText(title).assertIsDisplayed()
        }
        rule.onNodeWithTag("dossier_screen").performScrollToNode(hasTestTag("open_key"))
        rule.onNodeWithTag("open_key").performClick()
        lifecycle.screen("key_screen")
        rule.onNodeWithTag("key_screen").performScrollToNode(hasTestTag("key_back"))
        rule.onNodeWithTag("key_back").performClick()
        lifecycle.screen("dossier_screen")
        rule.onNodeWithTag("dossier_screen").performScrollToNode(hasTestTag("dossier_about_version"))
        rule.onNodeWithTag("dossier_about_version").assertTextContains("LEGEND 0.44.0 (53)")
        rule.onNodeWithTag("font_licenses").assertTextContains("EB Garamond, SIL Open Font License 1.1.")
        rule.onAllNodesWithTag("plate_math_tool").assertCountEquals(0)
        rule.onAllNodesWithTag("rest_timer_tool").assertCountEquals(0)
        rule.onAllNodesWithTag("build_integrity_card").assertCountEquals(0)
        rule.onAllNodesWithTag("legacy_export_scope").assertCountEquals(0)
        rule.onAllNodesWithTag("export_data").assertCountEquals(0)
        rule.onAllNodesWithText("INDEPENDENT REST TIMER").assertCountEquals(0)
        rule.onAllNodesWithText("PLATE CALCULATOR").assertCountEquals(0)

        lifecycle.fontScale(2f)
        rule.onNodeWithTag("nav_DOSSIER").performClick()
        lifecycle.screen("dossier_screen")
        rule.onNodeWithTag("dossier_position_header").assertIsDisplayed()
        capture("dossier-font2")
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
