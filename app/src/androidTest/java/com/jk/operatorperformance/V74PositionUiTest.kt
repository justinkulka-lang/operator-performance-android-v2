package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Fresh-install position and Consolidate-now, including the committed prefs a new process reloads. */
class V74PositionUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val lifecycle get() = LifecycleTestSupport(rule)
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @Before fun clear() {
        context.getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE).edit().clear().commit()
        lifecycle.recreate("start_v72")
    }

    @Test fun freshInstallStartsAtAPositionAndTodayShowsThatHeader() {
        rule.onNodeWithTag("start_at_position").performScrollTo().performClick()
        rule.onNodeWithTag("position_block_3").performScrollTo().performClick()
        rule.onNodeWithTag("position_week_4").performScrollTo().performClick()
        rule.onNodeWithTag("position_day_2").performScrollTo().performClick()
        rule.onNodeWithTag("position_continue").performClick()
        rule.onNodeWithTag("confirm_start_position_text").assertTextContains("Block III · Week 4 · Day 2")
        rule.onNodeWithTag("confirm_start_position").performClick()
        rule.onNodeWithTag("today_header").assertTextContains("Week 20 · III · Backstop · Week 4 of 8")
        val loaded = ProgramStateStore(context).load()
        assertEquals(20, loaded.currentWeek)
        assertEquals(3, loaded.v74Block)
        assertEquals(4, loaded.v74BlockWeek)
        assertEquals(2, loaded.currentDay)
        assertEquals(1, loaded.v74Loop)
    }

    @Test fun consolidateSurvivesRecreationAndProcessDeath() {
        context.getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE).edit().clear()
            .putBoolean("v71Initialized", true)
            .putInt("currentWeek", 5)
            .putInt("currentDay", 1)
            .putString("authorityVersion", "V74")
            .putBoolean("v72MigrationApplied", true)
            .putInt("v72MigrationRevision", 2)
            .putBoolean("v72ActivationProven", true)
            .putBoolean("v73MigrationApplied", true)
            .putInt("v73ActivationWeek", 1)
            .putBoolean("v74MigrationApplied", true)
            .putInt("v74ActivationWeek", 1)
            .putInt("v74Block", 1)
            .putInt("v74BlockWeek", 5)
            .putInt("v74Loop", 1)
            .putInt("engagedAbsoluteDay", 28)
            .commit()
        lifecycle.recreate("today_header")
        rule.onNodeWithTag("today_header").assertTextContains("Week 5 · I · Draft · Week 5 of 8")
        rule.onNodeWithTag("consolidate_now").performScrollTo().performClick()
        rule.onNodeWithText("Consolidation week · half sets · 3–4 RIR", substring = true).assertIsDisplayed()
        rule.onNodeWithTag("confirm_consolidate").performClick()
        rule.onNodeWithTag("today_header").assertTextContains("Week 5 · I · Draft · Week 8 of 8")
        lifecycle.recreate("today_header")
        rule.onNodeWithTag("today_header").assertTextContains("Week 5 · I · Draft · Week 8 of 8")
        val reloaded = ProgramStateStore(context).load()
        assertEquals(5, reloaded.currentWeek)
        assertEquals(8, reloaded.v74BlockWeek)
        assertEquals(1, reloaded.v74Block)
        assertEquals(1, reloaded.v74Loop)
        rule.activityRule.scenario.recreate()
        lifecycle.screen("today_header")
        rule.onNodeWithTag("today_header").assertTextContains("Week 5 · I · Draft · Week 8 of 8")
        assertEquals(8, ProgramStateStore(context).load().v74BlockWeek)
    }
}
