package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.jk.operatorperformance.state.ProgramStateStore
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class V71LaunchUiTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Before fun resetV71() { ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("ares_v71_reference_state",Context.MODE_PRIVATE).edit().clear().commit(); rule.activityRule.scenario.recreate() }
    @Test fun explicitStartGateIsShown() { rule.onNodeWithTag("start_v72").assertIsDisplayed() }
}
