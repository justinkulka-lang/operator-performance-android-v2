package com.jk.operatorperformance

import android.content.res.Configuration
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream

/** Serializes configuration changes; never retains an Activity across a recreation. */
internal class LifecycleTestSupport(
    private val rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
) {
    fun withActivity(action: (MainActivity) -> Unit) {
        rule.activityRule.scenario.onActivity(action)
    }

    fun configuration(matches: (Configuration) -> Boolean) {
        rule.waitUntil(30_000) {
            if (rule.activityRule.scenario.state != Lifecycle.State.RESUMED) return@waitUntil false
            var ready = false
            withActivity { ready = !it.isFinishing && !it.isDestroyed && matches(it.resources.configuration) }
            ready
        }
        rule.waitForIdle()
    }

    fun fontScale(scale: Float) {
        shell("settings put system font_scale $scale")
        configuration { kotlin.math.abs(it.fontScale - scale) < 0.01f }
    }

    fun orientation(requested: Int, expected: Int) {
        withActivity { it.requestedOrientation = requested }
        configuration { it.orientation == expected }
    }

    fun recreate(screenTag: String) {
        configuration { true }
        rule.activityRule.scenario.recreate()
        configuration { true }
        screen(screenTag)
    }

    fun screen(tag: String) {
        configuration { true }
        rule.waitUntil(10_000) {
            rule.onAllNodesWithTag(tag).fetchSemanticsNodes().any {
                it.boundsInRoot.width > 8f && it.boundsInRoot.height > 8f
            }
        }
    }

    fun back() {
        withActivity { it.onBackPressedDispatcher.onBackPressed() }
        configuration { true }
    }

    fun shell(command: String): String = InstrumentationRegistry.getInstrumentation()
        .uiAutomation.executeShellCommand(command).use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
        }
}
