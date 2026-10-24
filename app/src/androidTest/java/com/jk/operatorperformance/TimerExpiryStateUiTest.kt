package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.WorkoutProgress
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Real deadline expiry must clear only the timer, using state edited after it began. */
class TimerExpiryStateUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val context get() = ApplicationProvider.getApplicationContext<Context>()
    private val store get() = ProgramStateStore(context)
    private val lifecycle get() = LifecycleTestSupport(rule)

    @Before fun prepare() {
        clear()
        lifecycle.recreate("start_v72")
    }

    @After fun clear() {
        context.getSharedPreferences("ares_v71_reference_state", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun workoutExpiryKeepsSubstitutionChosenDuringTimerForCurrentAndRetainedAuthorities() {
        for (authority in listOf(ProgramAuthority.V72, ProgramAuthority.V73)) {
            store.clearWorkout()
            store.clearSessionStatus()
            val state = stateFor(authority)
            store.save(state)
            val deadline = System.currentTimeMillis() + 30_000L
            store.saveWorkout(WorkoutProgress.start(state).copy(restTimerEndEpochMillis = deadline))
            lifecycle.recreate("today_header")
            rule.onNodeWithTag("today_header").assertTextContains("v${authority.versionLabel}", substring = true)
            rule.onNodeWithTag("start_resume_workout").performClick()
            lifecycle.screen("workout_mode")
            rule.onNodeWithTag("workout_rest_countdown").assertIsDisplayed()
            val row = SessionResolver.resolve(17, 2, authority).rows.first {
                it.kind == SessionRowKind.RESISTANCE && it.alternatives.isNotEmpty()
            }
            reveal("workout_mode", "workout_alternative_${row.id}_0")
            rule.onNodeWithTag("workout_alternative_${row.id}_0").performClick()
            assertTrue("Selection must happen before expiry", deadline > System.currentTimeMillis() + 2_000L)
            assertEquals(deadline, store.loadWorkout()!!.restTimerEndEpochMillis)
            assertEquals(row.alternatives.first(), store.loadWorkout()!!.selectedSubstitutions[row.id])
            rule.waitUntil(40_000L) { store.loadWorkout()?.restTimerEndEpochMillis == null }
            val expired = store.loadWorkout()!!
            assertEquals(row.alternatives.first(), expired.selectedSubstitutions[row.id])
            assertEquals(authority, expired.authority)
            assertFalse(expired.complete)
            reveal("workout_mode", "return_today")
            rule.onNodeWithTag("return_today").performClick()
            lifecycle.recreate("today_header")
            assertEquals(expired, store.loadWorkout())
            assertEquals(17, store.load().currentWeek)
            assertEquals(2, store.load().currentDay)
        }
    }

    @Test fun utilityExpiryKeepsDurationSelectedDuringTimerAcrossRecreation() {
        val state = stateFor(ProgramAuthority.V73).copy(
            restTimerSeconds = 60, utilityRestTimerEndEpochMillis = System.currentTimeMillis() + 30_000L,
        )
        store.save(state)
        lifecycle.recreate("today_header")
        rule.onNodeWithTag("nav_TOOLS").performClick()
        reveal("tools_screen", "utility_rest_countdown")
        rule.onNodeWithTag("utility_rest_countdown").assertIsDisplayed()
        reveal("tools_screen", "rest_choice_90")
        rule.onNodeWithTag("rest_choice_90").performClick()
        assertTrue("Duration must change before expiry", state.utilityRestTimerEndEpochMillis!! > System.currentTimeMillis() + 2_000L)
        assertEquals(state.utilityRestTimerEndEpochMillis, store.load().utilityRestTimerEndEpochMillis)
        assertEquals(90, store.load().restTimerSeconds)
        rule.waitUntil(40_000L) { store.load().utilityRestTimerEndEpochMillis == null }
        val loaded = store.load()
        assertEquals(90, loaded.restTimerSeconds)
        assertNull(loaded.utilityRestTimerEndEpochMillis)
        assertEquals(17, loaded.currentWeek)
        assertEquals(2, loaded.currentDay)
        assertEquals(ProgramAuthority.V73, loaded.authorityForWeek(17))
        lifecycle.recreate("tools_screen")
        assertEquals(90, store.load().restTimerSeconds)
        reveal("tools_screen", "rest_choice_90")
        rule.onNodeWithTag("rest_choice_90").assertTextContains("SELECTED", substring = true)
    }

    private fun stateFor(authority: ProgramAuthority): ProgramUiState {
        val current = ProgramUiState().startV73().withCurrentPointer(17, 2)
        return if (authority == ProgramAuthority.V73) current else current.copy(
            authority = authority, v73ActivationWeek = 18, historicalAuthorities = mapOf(17 to authority),
        )
    }

    private fun reveal(screen: String, tag: String) {
        rule.onNodeWithTag(screen).performScrollToNode(hasTestTag(tag))
        rule.onNodeWithTag(tag).assertIsDisplayed()
    }
}
