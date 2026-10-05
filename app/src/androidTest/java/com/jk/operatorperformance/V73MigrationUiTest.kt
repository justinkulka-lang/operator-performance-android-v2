package com.jk.operatorperformance

import android.content.Context
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.state.ProgramStateStore
import java.io.File
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class V73MigrationUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun prefs() = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("ares_v71_reference_state",Context.MODE_PRIVATE)
    private fun seed(week:Int,day:Int,engaged:Int) {
        prefs().edit().clear().putBoolean("v71Initialized",true)
            .putString("authorityVersion","V72").putBoolean("v72MigrationApplied",true)
            .putInt("v72MigrationRevision",2).putBoolean("v72ActivationProven",true)
            .putInt("currentWeek",week).putInt("currentDay",day).putInt("engagedAbsoluteDay",engaged).commit()
        LifecycleTestSupport(rule).recreate("today_header")
    }
    @Test fun packagedReferenceHasAll32CorrectWeeksAndRealSourceProvenance() {
        assertTrue(BuildConfig.SOURCE_COMMIT.matches(Regex("[0-9a-f]{40}")))
        assertEquals(BuildConfig.SOURCE_COMMIT.take(12),BuildConfig.BUILD_COMMIT)
        val report=buildString {
            appendLine("week\tresistance_sets\tengine_resistance_sets")
            for (week in 1..32) {
                val split=SessionResolver.splitForWeek(week,ProgramAuthority.V73)
                val total=split.flatMap { it.rows }.filter { it.kind.name == "RESISTANCE" }.sumOf { it.effortCount }
                val expected=when(week) { 8,16 -> 59;24 -> 65;30,31 -> 77;32 -> 13;in 17..23 -> 146;else -> 136 }
                assertEquals("Packaged Week $week",expected,total)
                val engine=split[0].rows.filter { it.kind.name == "RESISTANCE" }.sumOf { it.effortCount }
                assertEquals(0,engine)
                appendLine("$week\t$total\t$engine")
            }
        }
        val path=InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        if(path != null) { val dir=File(path);dir.mkdirs();File(dir,"packaged-v73-reference.tsv").writeText(report) }
    }
    @Test fun engagedHistoricalWeekSurvivesActivityRecreationAndAdvancesAtBoundary() {
        seed(13,7,88)
        rule.onNodeWithTag("today_header").assertTextContains("v7.2",substring=true)
        val context=ApplicationProvider.getApplicationContext<Context>()
        val store=ProgramStateStore(context)
        val retained=store.load()
        assertEquals(ProgramAuthority.V72,retained.authorityForWeek(13))
        assertEquals(14,retained.v73ActivationWeek)
        store.save(retained.advanceDay())
        LifecycleTestSupport(rule).recreate("today_header")
        rule.onNodeWithTag("today_header").assertTextContains("v7.3",substring=true)
        assertEquals(ProgramAuthority.V72,ProgramStateStore(context).load().authorityForWeek(13))
        rule.onAllNodesWithText("LOG SET",substring=true).assertCountEquals(0)
    }
    @Test fun positivelyUnstartedDay1ActivatesEngineOnlyAcrossRecreation() {
        seed(17,1,112)
        rule.onNodeWithTag("today_header").assertTextContains("v7.3",substring=true)
        val store=ProgramStateStore(ApplicationProvider.getApplicationContext())
        val state=store.load()
        assertEquals(17,state.v73ActivationWeek)
        val session=SessionResolver.resolve(17,1,state.authorityForWeek(17))
        assertFalse(session.rows.any { it.kind.name == "RESISTANCE" })
        LifecycleTestSupport(rule).recreate("today_header")
        assertEquals(state,store.load())
    }
    @Test fun savedCompletedSessionAndTimerRetainV72WithoutMovingCompletion() {
        val p=prefs()
        p.edit().clear().putBoolean("v71Initialized",true).putInt("currentWeek",13).putInt("currentDay",4)
            .putString("authorityVersion","V72").putBoolean("v72MigrationApplied",true).putInt("v72MigrationRevision",2)
            .putBoolean("v72ActivationProven",true).putInt("engagedAbsoluteDay",88)
            .putBoolean("workoutActive",true).putInt("workoutSchema",5).putInt("workoutWeek",13).putInt("workoutDay",4)
            .putBoolean("complete",true).putLong("workoutTimerEnd",123456L).commit()
        LifecycleTestSupport(rule).recreate("today_header")
        val store=ProgramStateStore(ApplicationProvider.getApplicationContext())
        assertTrue(store.loadWorkout()!!.complete)
        assertEquals(ProgramAuthority.V72,store.loadWorkout()!!.authority)
        assertEquals(123456L,store.loadWorkout()!!.restTimerEndEpochMillis)
        assertEquals(14,store.load().v73ActivationWeek)
    }
}
