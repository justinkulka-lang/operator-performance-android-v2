package com.jk.operatorperformance.state

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.ProgramV74
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.V74Pointer
import com.jk.operatorperformance.ui.catalogSessions
import com.jk.operatorperformance.ui.exportCatalogCsv
import com.jk.operatorperformance.ui.foundationReferenceLabel
import com.jk.operatorperformance.ui.searchProgram
import com.jk.operatorperformance.ui.sessionExecutionGuidance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class V74ContinuousTest {
    @Test fun freshStartAdvancesNinetySixWeeksThroughStoreReload() {
        val prefs = MemoryPrefs()
        val store = ProgramStateStore(prefs, MemoryPrefs())
        store.save(ProgramUiState().startV74())
        var state = store.load()
        repeat(96 * 7) {
            if (state.currentDay == 1) assertC5(state)
            assertFalse(state.atProgramEnd())
            store.save(state.advanceDay())
            val reloaded = store.load()
            assertEquals(store.load(), reloaded)
            state = reloaded
        }
        assertC5(state)
        assertEquals(97, state.currentWeek)
    }

    @Test fun week33And65MatchTheStoredLoop() {
        val week33 = ProgramUiState().startV74()
        var state = week33
        repeat(32 * 7) { state = state.advanceDay() }
        assertEquals(33, state.currentWeek)
        assertEquals(1, state.v74Block)
        assertEquals(1, state.v74BlockWeek)
        assertEquals(2, state.v74Loop)
        repeat(32 * 7) { state = state.advanceDay() }
        assertEquals(65, state.currentWeek)
        assertEquals(1, state.v74Block)
        assertEquals(1, state.v74BlockWeek)
        assertEquals(3, state.v74Loop)
        assertFalse(state.atProgramEnd())
    }

    @Test fun savedWeeks33And64And97ReloadWithoutClamping() {
        val prefs = MemoryPrefs()
        val store = ProgramStateStore(prefs, MemoryPrefs())
        for (week in listOf(33, 64, 97)) {
            val (block, blockWeek, loop) = V74Pointer.position(week)
            val pointer = ProgramUiState().startV74().copy(
                currentWeek = week,
                v74Block = block,
                v74BlockWeek = blockWeek,
                v74Loop = loop,
            )
            store.save(pointer)
            val loaded = ProgramStateStore(prefs, MemoryPrefs()).load()
            assertEquals(week, loaded.currentWeek)
            assertEquals(block, loaded.v74Block)
            assertEquals(blockWeek, loaded.v74BlockWeek)
            assertEquals(loop, loaded.v74Loop)
            assertEquals(pointer, loaded)
        }
    }

    @Test fun consolidateNowFollowsC3AndSurvivesReload() {
        val ready = ProgramUiState().startV74().copy(currentWeek = 3, v74Block = 1, v74BlockWeek = 3)
        assertTrue(ready.canConsolidate(false, WeekStartEvidence.NOT_STARTED))
        assertFalse(ready.canConsolidate(true, WeekStartEvidence.NOT_STARTED))
        assertFalse(ready.canConsolidate(false, WeekStartEvidence.UNKNOWN))
        assertFalse(ready.canConsolidate(false, WeekStartEvidence.ENGAGED))
        assertFalse(ready.copy(currentDay = 2).canConsolidate(false, WeekStartEvidence.NOT_STARTED))
        assertFalse(ready.copy(v74BlockWeek = 8).canConsolidate(false, WeekStartEvidence.NOT_STARTED))
        val retained = v73(9, 3, engaged = 60).second.load()
        assertFalse(retained.canConsolidate(false, WeekStartEvidence.NOT_STARTED))

        val consolidated = ready.consolidateNow(false, WeekStartEvidence.NOT_STARTED)
        assertEquals(8, consolidated.v74BlockWeek)
        assertEquals(ready.currentWeek, consolidated.currentWeek)
        assertEquals(ready.v74Block, consolidated.v74Block)
        assertEquals(ready.v74Loop, consolidated.v74Loop)
        assertEquals(ready.currentDay, consolidated.currentDay)
        val next = consolidated.copy(currentDay = 7).advanceDay()
        assertEquals(2, next.v74Block)
        assertEquals(1, next.v74BlockWeek)
        assertEquals(ready.currentWeek + 1, next.currentWeek)

        val closing = ProgramUiState().startV74().copy(currentWeek = 32, v74Block = 4, v74BlockWeek = 4, v74Loop = 1)
        val closed = closing.consolidateNow(false, WeekStartEvidence.NOT_STARTED).copy(currentDay = 7).advanceDay()
        assertEquals(33, closed.currentWeek)
        assertEquals(1, closed.v74Block)
        assertEquals(1, closed.v74BlockWeek)
        assertEquals(2, closed.v74Loop)

        val prefs = MemoryPrefs()
        val store = ProgramStateStore(prefs, MemoryPrefs())
        store.save(consolidated)
        assertEquals(consolidated, store.load())
    }

    @Test fun migrationCasesUseThe040Keys() {
        val fresh = ProgramStateStore(MemoryPrefs(), MemoryPrefs()).load()
        assertFalse(fresh.initialized)
        assertEquals(ProgramAuthority.V74, fresh.authorityForWeek(1))
        assertEquals(1, fresh.v74Block)
        assertEquals(1, fresh.v74BlockWeek)
        assertEquals(1, fresh.v74Loop)
        assertEquals(1, fresh.v74ActivationWeek)
        assertTrue(fresh.historicalAuthorities.isEmpty())

        val (engagedPrefs, engagedStore) = v73(12, 3, engaged = 80, workout = true)
        val engaged = engagedStore.load()
        assertEquals(ProgramAuthority.V73, engaged.authorityForWeek(12))
        assertEquals(12, engaged.currentWeek)
        assertEquals(3, engaged.currentDay)
        assertNull(engaged.v74Block)
        val workout = engagedStore.loadWorkout()!!
        assertEquals(12, workout.week)
        assertEquals(3, workout.day)
        assertEquals("Home alternate", workout.selectedSubstitutions["slot"])
        val week13 = engaged.copy(currentDay = 7).advanceDay()
        assertEquals(13, week13.currentWeek)
        assertEquals(ProgramAuthority.V74, week13.authority)
        assertEquals(2, week13.v74Block)
        assertEquals(5, week13.v74BlockWeek)

        val unstarted = v73(9, 1, engaged = 56).second.load()
        assertEquals(ProgramAuthority.V74, unstarted.authorityForWeek(9))
        assertEquals(2, unstarted.v74Block)
        assertEquals(1, unstarted.v74BlockWeek)
        assertEquals(1, unstarted.v74Loop)

        val unknown = v73(9, 1, engaged = null).second.load()
        assertEquals(ProgramAuthority.V73, unknown.authorityForWeek(9))
        assertNull(unknown.v74Block)
        val unknownNext = unknown.copy(currentDay = 7).advanceDay()
        assertEquals(ProgramAuthority.V74, unknownNext.authority)
        assertEquals(10, unknownNext.currentWeek)

        val (activePrefs, activeStore) = v73(9, 1, engaged = 56, workout = true)
        val active = activeStore.load()
        assertEquals(ProgramAuthority.V73, active.authorityForWeek(9))
        assertTrue(ProgramStateStore(activePrefs, MemoryPrefs()).loadWorkout() != null)

        val later = v73(9, 3, engaged = (15 - 1) * 7 + 1).second.load()
        assertEquals(ProgramAuthority.V73, later.authorityForWeek(15))
        assertEquals(ProgramAuthority.V74, later.authorityForWeek(16))
        assertTrue(later.v74ActivationWeek!! >= 16)

        val end = v73(32, 7, engaged = 224).second.load()
        assertEquals(ProgramAuthority.V73, end.authorityForWeek(32))
        val week33 = end.advanceDay()
        assertEquals(33, week33.currentWeek)
        assertEquals(2, week33.v74Loop)
        assertEquals(1, week33.v74Block)
        assertEquals(1, week33.v74BlockWeek)
        assertFalse(week33.atProgramEnd())

        val mixedPrefs = MemoryPrefs()
        mixedPrefs.edit {
            v73Keys(10, 3, engaged = 70)
            putStringSet("historicalWeekAuthorities", setOf("4:V71", "8:V72"))
        }
        val mixed = ProgramStateStore(mixedPrefs, MemoryPrefs()).load()
        assertEquals(ProgramAuthority.V71, mixed.authorityForWeek(4))
        assertEquals(ProgramAuthority.V72, mixed.authorityForWeek(8))
        assertEquals(ProgramAuthority.V71, SessionResolver.resolve(4, 2, mixed.authorityForWeek(4)).authority)
        assertEquals(ProgramAuthority.V72, SessionResolver.resolve(8, 2, mixed.authorityForWeek(8)).authority)

        val again = ProgramStateStore(engagedPrefs, MemoryPrefs()).load()
        assertEquals(engaged, again)
        assertEquals(again, ProgramStateStore(engagedPrefs, MemoryPrefs()).load())
    }

    @Test fun v74Weeks30Through32OmitLegacyWeekText() {
        val state = ProgramUiState().startV74()
        for (week in listOf(30, 31, 32)) {
            for (day in 1..7) {
                val (block, blockWeek) = ProgramV74.position(week)
                val session = V74Pointer.reference(week, block, blockWeek, day)
                val text = buildString {
                    append(sessionExecutionGuidance(session).joinToString("\n"))
                    append('\n')
                    append(foundationReferenceLabel(session, state).orEmpty())
                    session.rows.forEach { row ->
                        append('\n')
                        append(row.title)
                        append('\n')
                        append(row.prescription)
                        append('\n')
                        append(row.note.orEmpty())
                    }
                }
                assertFalse(text.contains("WEEK 32 OVERLAY"))
                assertFalse(text.contains("REALIZATION"))
                assertFalse(text.contains("600 m"))
                assertFalse(text.contains("timed", ignoreCase = true))
                assertEquals(ProgramAuthority.V74, session.authority)
                assertEquals(V74Pointer.phaseName(block), session.phase.name)
            }
        }
    }

    @Test fun resetReturnsToLoopOneWithEmptyHistory() {
        val reset = ProgramUiState().startV74().copy(
            currentWeek = 20,
            v74Block = 3,
            v74BlockWeek = 4,
            v74Loop = 4,
            historicalAuthorities = mapOf(4 to ProgramAuthority.V72),
        ).resetToV74Start()
        assertEquals(1, reset.currentWeek)
        assertEquals(1, reset.currentDay)
        assertEquals(1, reset.v74Loop)
        assertEquals(1, reset.v74Block)
        assertEquals(1, reset.v74BlockWeek)
        assertTrue(reset.historicalAuthorities.isEmpty())
        assertTrue(reset.initialized)
    }

    @Test fun setCurrentUnderV74ChangesOnlyTheDay() {
        val state = ProgramUiState().startV74().copy(currentWeek = 20, currentDay = 2, v74Block = 3, v74BlockWeek = 4)
        val moved = state.withCurrentPointer(32, 6)
        assertEquals(20, moved.currentWeek)
        assertEquals(6, moved.currentDay)
        assertEquals(3, moved.v74Block)
        assertEquals(4, moved.v74BlockWeek)
        assertEquals(1, moved.v74Loop)
        assertEquals(state.historicalAuthorities, moved.historicalAuthorities)
    }

    @Test fun handoffDigestMatchesTheDisplayedValue() {
        val file = listOf(
            File("../docs/legend-v74/LEGEND_v7.4_Continuous.md"),
            File("docs/legend-v74/LEGEND_v7.4_Continuous.md"),
        ).first { it.isFile }
        val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
            .joinToString("") { "%02x".format(it) }
        assertEquals(V74Pointer.AUTHORITY_SHA256, digest)
    }

    @Test fun searchAndExportCoverOneLoopAndRetainedWeeksOnly() {
        val fresh = ProgramUiState().startV74()
        val sessions = catalogSessions(fresh)
        assertEquals(224, sessions.size)
        assertTrue(sessions.all { it.authority == ProgramAuthority.V74 && it.week in 1..32 })
        assertTrue(searchProgram("week 33", fresh).isEmpty())
        val week20 = sessions.filter { it.week == 20 && it.day == 2 }
        assertEquals(1, week20.size)
        assertEquals("Backstop", week20.single().phase.name)

        val retained = v73(12, 3, engaged = 80, workout = true).second.load()
        val catalog = catalogSessions(retained)
        assertTrue(catalog.any { it.week == 12 && it.authority == ProgramAuthority.V73 })
        assertEquals(224, catalog.count { it.authority == ProgramAuthority.V74 })
        assertTrue(catalog.filter { it.authority != ProgramAuthority.V74 }.all { it.week < retained.v74ActivationWeek!! })
        val csv = exportCatalogCsv(retained)
        assertTrue(csv.lineSequence().none { line ->
            val week = line.split(',').getOrNull(1)?.toIntOrNull()
            week != null && week > 32
        })
    }

    @Test fun retainedWeek32SeedStaysV72AndConsolidateSeedIsAvailable() {
        val prefs = MemoryPrefs()
        prefs.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 32)
            putInt("currentDay", 1)
            putString("authorityVersion", "V72")
            putBoolean("v72MigrationApplied", true)
            putInt("v72MigrationRevision", 2)
            putBoolean("v72ActivationProven", true)
            putBoolean("v74MigrationApplied", true)
            putInt("v74ActivationWeek", 33)
            putStringSet("historicalWeekAuthorities", (1..32).map { "$it:V72" }.toSet())
        }
        val retained = ProgramStateStore(prefs, MemoryPrefs()).load()
        assertEquals(32, retained.currentWeek)
        assertEquals(1, retained.currentDay)
        assertEquals(ProgramAuthority.V72, retained.authorityForWeek(32))
        assertEquals(33, retained.v74ActivationWeek)
        assertNull(retained.v74Block)
        assertFalse(retained.canConsolidate(false, WeekStartEvidence.NOT_STARTED))

        val consolidate = MemoryPrefs()
        consolidate.edit {
            putBoolean("v71Initialized", true)
            putInt("currentWeek", 5)
            putInt("currentDay", 1)
            putString("authorityVersion", "V74")
            putBoolean("v72MigrationApplied", true)
            putInt("v72MigrationRevision", 2)
            putBoolean("v72ActivationProven", true)
            putBoolean("v73MigrationApplied", true)
            putInt("v73ActivationWeek", 1)
            putBoolean("v74MigrationApplied", true)
            putInt("v74ActivationWeek", 1)
            putInt("v74Block", 1)
            putInt("v74BlockWeek", 5)
            putInt("v74Loop", 1)
            putInt("engagedAbsoluteDay", 28)
        }
        val store = ProgramStateStore(consolidate, MemoryPrefs())
        val ready = store.load()
        assertEquals(WeekStartEvidence.NOT_STARTED, store.evidenceForWeek(5))
        assertTrue(ready.canConsolidate(store.loadWorkout() != null, store.evidenceForWeek(5)))
    }

    @Test fun everyStartPositionPersistsAndAdvancesByTheNormalRule() {
        val prefs = MemoryPrefs()
        val store = ProgramStateStore(prefs, MemoryPrefs())
        for (block in 1..4) for (blockWeek in 1..8) for (day in 1..7) {
            val started = ProgramUiState().startAtPosition(block, blockWeek, day)
            store.save(started)
            store.clearSessionStatus()
            store.clearWorkout()
            val loaded = ProgramStateStore(prefs, MemoryPrefs()).load()
            assertEquals((block - 1) * 8 + blockWeek, loaded.currentWeek)
            assertEquals(block, loaded.v74Block)
            assertEquals(blockWeek, loaded.v74BlockWeek)
            assertEquals(day, loaded.currentDay)
            assertEquals(1, loaded.v74Loop)
            assertEquals(1, loaded.v74ActivationWeek)
            assertTrue(loaded.historicalAuthorities.isEmpty())
            assertNull(store.loadWorkout())
            assertFalse(prefs.contains("engagedAbsoluteDay"))
            val advanced = loaded.advanceDay()
            if (day < 7) {
                assertEquals(loaded.currentWeek, advanced.currentWeek)
                assertEquals(day + 1, advanced.currentDay)
                assertEquals(block, advanced.v74Block)
                assertEquals(blockWeek, advanced.v74BlockWeek)
                assertEquals(1, advanced.v74Loop)
            }
        }
        val edge = ProgramUiState().startAtPosition(4, 8, 7).advanceDay()
        assertEquals(33, edge.currentWeek)
        assertEquals(2, edge.v74Loop)
        assertEquals(1, edge.v74Block)
        assertEquals(1, edge.v74BlockWeek)
    }

    private fun assertC5(state: ProgramUiState) {
        val (block, blockWeek, loop) = V74Pointer.position(state.currentWeek)
        assertEquals(block, state.v74Block)
        assertEquals(blockWeek, state.v74BlockWeek)
        assertEquals(loop, state.v74Loop)
        assertEquals(ProgramAuthority.V74, state.authorityForWeek(state.currentWeek))
    }

    private fun v73(
        week: Int,
        day: Int,
        engaged: Int?,
        workout: Boolean = false,
    ): Pair<MemoryPrefs, ProgramStateStore> {
        val prefs = MemoryPrefs()
        prefs.edit { v73Keys(week, day, engaged, workout) }
        return prefs to ProgramStateStore(prefs, MemoryPrefs())
    }

    private fun PrefsEditor.v73Keys(week: Int, day: Int, engaged: Int?, workout: Boolean = false) {
        putBoolean("v71Initialized", true)
        putInt("currentWeek", week)
        putInt("currentDay", day)
        putString("authorityVersion", "V73")
        putBoolean("v73MigrationApplied", true)
        putInt("v73ActivationWeek", 1)
        putBoolean("v72MigrationApplied", true)
        putInt("v72MigrationRevision", 2)
        putBoolean("v72ActivationProven", true)
        if (engaged != null) putInt("engagedAbsoluteDay", engaged)
        if (workout) {
            putBoolean("workoutActive", true)
            putInt("workoutSchema", 5)
            putInt("workoutWeek", week)
            putInt("workoutDay", day)
            putString("workoutAuthority", "V73")
            putStringSet("substitutions", setOf("slot\u001FHome alternate"))
        }
    }
}
