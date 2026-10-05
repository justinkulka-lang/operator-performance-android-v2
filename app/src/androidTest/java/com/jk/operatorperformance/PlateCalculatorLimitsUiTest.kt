package com.jk.operatorperformance

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PlateCalculatorLimitsUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @After fun clear() {
        context.getSharedPreferences("operator_v54_reference_state",Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences("ares_v71_reference_state",Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun oversizedStoredAndEnteredPairsAreRejectedAndValidResultsUpdate() {
        clear()
        val tools = context.getSharedPreferences("operator_v54_reference_state",Context.MODE_PRIVATE)
        tools.edit().putStringSet("lb.plateInventoryPairs",setOf("45.0|2147483647")).commit()
        LifecycleTestSupport(rule).recreate("start_v72")
        rule.onNodeWithTag("start_v72").performScrollTo().performClick()
        rule.onNodeWithTag("nav_TOOLS").performClick()
        reveal("plate_math_tool")
        rule.onNodeWithText("Some stored plate entries exceed supported limits",substring=true).performScrollTo().assertIsDisplayed()
        assertEquals(setOf("45.0|2147483647"),tools.getStringSet("lb.plateInventoryPairs",emptySet()))
        reveal("plate_equipment_weight")
        rule.onNodeWithTag("plate_equipment_weight").performTextReplacement("45")
        reveal("plate_inventory_weight")
        rule.onNodeWithTag("plate_inventory_weight").performTextReplacement("45")
        reveal("plate_inventory_pairs")
        rule.onNodeWithTag("plate_inventory_pairs").performTextReplacement("2147483647")
        reveal("plate_inventory_add")
        rule.onNodeWithTag("plate_inventory_add").assertIsNotEnabled()
        reveal("plate_inventory_pairs")
        rule.onNodeWithTag("plate_inventory_pairs").performTextReplacement("1")
        reveal("plate_inventory_add")
        rule.onNodeWithTag("plate_inventory_add").performClick()
        reveal("plate_target")
        rule.onNodeWithTag("plate_target").performTextReplacement("135")
        hideKeyboard()
        await("plate_result")
        reveal("plate_result")
        rule.onNodeWithText("EXACT • 135 total").assertIsDisplayed()
        reveal("plate_target")
        rule.onNodeWithTag("plate_target").performTextReplacement("132")
        hideKeyboard()
        await("plate_use_lower")
        reveal("plate_use_lower")
        rule.onNodeWithTag("plate_use_lower").assertTextContains("45",substring=true)
        reveal("plate_use_higher")
        rule.onNodeWithTag("plate_use_higher").assertTextContains("135",substring=true)
    }

    private fun reveal(tag: String) {
        rule.onNodeWithTag("tools_screen").performScrollToNode(hasTestTag(tag))
        rule.onNodeWithTag(tag).assertIsDisplayed()
    }

    private fun await(tag: String) = rule.waitUntil(10_000) {
        rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    private fun hideKeyboard() = LifecycleTestSupport(rule).withActivity {
        it.getSystemService(InputMethodManager::class.java)?.hideSoftInputFromWindow(it.window.decorView.windowToken,0)
    }
}
