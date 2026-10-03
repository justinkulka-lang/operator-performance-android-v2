package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramV72ParityTest {
    @Test fun durationIs224DaysWithOneRestAndNoWeek33() {
        assertEquals(224, AresProgramV72.TOTAL_DAYS)
        val days = (1..32).flatMap { week ->
            val split = SessionResolver.splitForWeek(week, ProgramAuthority.V72)
            assertEquals(7, split.size)
            assertEquals(1, split.count { it.day == 7 && it.rows.any { row -> row.kind == SessionRowKind.REST } })
            assertTrue(split.all { it.authority == ProgramAuthority.V72 })
            split
        }
        assertEquals(224, days.size)
        assertEquals(224, days.maxOf { AresProgramV72.absoluteDay(it.week, it.day) })
        assertTrue(ProgramAudit.validateV72().isEmpty())
    }

    @Test fun dayOrderAndSpecialTitlesMatchTheAuthority() {
        val normal = SessionResolver.splitForWeek(2, ProgramAuthority.V72).map { it.title }
        assertEquals(
            listOf("Engine then short upper", "Lower A", "Upper A", "Speed", "Upper B then easy aerobic", "Lower B", "Rest"),
            normal,
        )
        assertEquals("Engine baseline", SessionResolver.resolve(1, 1, ProgramAuthority.V72).title)
        assertEquals("1.5-mile checkpoint", SessionResolver.resolve(24, 1, ProgramAuthority.V72).title)
        assertEquals("Final 1.5-mile", SessionResolver.resolve(32, 1, ProgramAuthority.V72).title)
        assertEquals("Very easy bike and mobility", SessionResolver.resolve(32, 2, ProgramAuthority.V72).title)
        assertEquals("Upper B", SessionResolver.resolve(32, 5, ProgramAuthority.V72).title)
        assertEquals("Final 300 m", SessionResolver.resolve(32, 4, ProgramAuthority.V72).rows.first { it.id == "w32_d4" }.title)
    }

    @Test fun phaseMuscleVolumeMatchesSection4() {
        assertEquals(mapOf("total" to 104, "chest" to 11, "back" to 13, "quads" to 13, "hamstrings" to 12, "biceps" to 8, "triceps" to 6), AresProgramV72.phaseVolume(1))
        assertEquals(mapOf("total" to 100, "chest" to 11, "back" to 14, "quads" to 14, "hamstrings" to 12, "biceps" to 8, "triceps" to 6), AresProgramV72.phaseVolume(2))
        assertEquals(mapOf("total" to 88, "chest" to 10, "back" to 12, "quads" to 11, "hamstrings" to 10, "biceps" to 8, "triceps" to 6), AresProgramV72.phaseVolume(3))
        assertEquals(mapOf("total" to 71, "chest" to 8, "back" to 10, "quads" to 8, "hamstrings" to 8, "biceps" to 6, "triceps" to 6), AresProgramV72.phaseVolume(4))
        assertTrue(AresProgramV72.phaseVolume(1).getValue("biceps") >= 6)
        assertTrue(AresProgramV72.phaseVolume(4).getValue("triceps") >= 6)
    }

    @Test fun representativePrescriptionsAreExact() {
        val week2 = SessionResolver.resolve(2, 2, ProgramAuthority.V72)
        assertEquals("4 × 6–8 @ 2 RIR", week2.rows.first { it.title == "Back or front squat" }.prescription)
        assertEquals("3 × 10–15 @ 0–1 RIR", SessionResolver.resolve(2, 1, ProgramAuthority.V72).rows.first { it.title == "Rope pushdown" }.prescription)
        assertEquals("4 × 10–15 @ 1 RIR", SessionResolver.resolve(3, 6, ProgramAuthority.V72).rows.first { it.title == "Seated or lying leg curl" }.prescription)
        assertEquals("5 × 3–5 @ 2 RIR", SessionResolver.resolve(9, 3, ProgramAuthority.V72).rows.first { it.title == "Flat barbell bench press" }.prescription)
        assertEquals("3 × 4–6 @ 2 RIR", SessionResolver.resolve(17, 2, ProgramAuthority.V72).rows.first { it.title == "Squat" }.prescription)
        val week8squat = SessionResolver.resolve(8, 2, ProgramAuthority.V72).rows.first { it.title == "Back or front squat" }
        assertEquals("2 × 6–8 @ 3–4 RIR", week8squat.prescription)
        val week30 = SessionResolver.resolve(30, 2, ProgramAuthority.V72).rows.first { it.title == "Squat" }
        assertEquals("2 × 4–6 @ 2–3 RIR", week30.prescription)
        val week32bench = SessionResolver.resolve(32, 3, ProgramAuthority.V72).rows.first { it.title == "Bench press" }
        assertEquals("2 sets @ 3 RIR", week32bench.prescription)
        assertFalse(week32bench.prescription.contains("×"))
        assertTrue(SessionResolver.resolve(32, 3, ProgramAuthority.V72).rows.none { it.title == "Incline press" })
        assertTrue(SessionResolver.resolve(1, 1, ProgramAuthority.V72).rows.none { it.kind == SessionRowKind.RESISTANCE })
        assertTrue(SessionResolver.resolve(24, 1, ProgramAuthority.V72).rows.none { it.kind == SessionRowKind.RESISTANCE })
    }

    @Test fun conditioningRowsWarnOnlyAt600AndKeepPlyometricsScoped() {
        val expectedEngine = mapOf(
            1 to "Controlled 1.5-mile baseline; no short upper",
            2 to "3 × 6 min threshold; 2 min easy",
            8 to "Easy run 25 min; easy-only warm-up",
            12 to "6 × 800 m controlled interval; 2 min easy",
            16 to "Easy run 20 min; easy-only warm-up",
            24 to "Timed 1.5-mile checkpoint; no short upper",
            32 to "Final timed 1.5-mile; no short upper",
        )
        expectedEngine.forEach { (week, text) ->
            assertEquals(text, AresProgramV72.conditioningForWeek(week).engine.single().prescription)
        }
        assertEquals(600, AresProgramV72.conditioningForWeek(12).speed.single().qualifyingSprintMeters)
        assertEquals(600, AresProgramV72.conditioningForWeek(19).speed.single().qualifyingSprintMeters)
        assertEquals(500, AresProgramV72.conditioningForWeek(14).speed.single().qualifyingSprintMeters)
        assertEquals(80, AresProgramV72.conditioningForWeek(4).speed.single().qualifyingSprintMeters)
        val warned = (1..32).filter { week ->
            SessionResolver.resolve(week, 4, ProgramAuthority.V72).rows.any { it.note.orEmpty().contains("WORKLOAD WARNING") }
        }
        assertEquals(listOf(12, 18, 19, 22, 23), warned)
        assertFalse(SessionResolver.resolve(2, 4, ProgramAuthority.V72).rows.any { it.id.endsWith("plyometrics") })
        assertTrue(SessionResolver.resolve(3, 4, ProgramAuthority.V72).rows.any { it.id.endsWith("plyometrics") })
        assertFalse(SessionResolver.resolve(30, 4, ProgramAuthority.V72).rows.any { it.id.endsWith("plyometrics") })
        assertTrue(SessionResolver.resolve(7, 4, ProgramAuthority.V72).rows.any { it.note.orEmpty().contains("no RPE fallback") })
        assertTrue(SessionResolver.resolve(8, 1, ProgramAuthority.V72).rows.any { it.title == "Cooldown" && it.prescription.contains("8–10 min") })
        assertTrue(SessionResolver.resolve(16, 1, ProgramAuthority.V72).rows.any { it.title == "Cooldown" && it.prescription.contains("8–10 min") })
        assertTrue(SessionResolver.resolve(8, 1, ProgramAuthority.V72).rows.none { it.prescription.contains("extra cooldown") || it.note.orEmpty().contains("no added cooldown") })
        assertTrue(SessionResolver.resolve(2, 1, ProgramAuthority.V72).rows.any { it.title == "Cooldown" })
        val incline = AresProgramV72.resistanceSlots.filter { it.phase > 1 && it.day == 3 && it.id.endsWith("_curl") }
        assertEquals(3, incline.size)
        assertTrue(incline.all { it.alternatives.map { alt -> alt.name } == listOf("Behind-body cable curl") })
        assertTrue(SessionResolver.resolve(2, 4, ProgramAuthority.V72).rows.first().prescription.contains("ankle rocks 10/side"))
    }

    @Test fun paceMathUses2414MetresAndNotTheRoundedTable() {
        val fourHundred = 600.0 / AresProgramV72.STATUTE_MILE_AND_HALF_METRES * 400.0
        assertEquals(99.4, fourHundred, 0.05)
        assertEquals(46.0 * 200.0 / 300.0 / 0.90, AresProgramV72.targetSeconds(46.0, 200.0, 0.90), 0.0001)
        val note = SessionResolver.resolve(18, 1, ProgramAuthority.V72).rows.first { it.title == "Goal pace" }.note.orEmpty()
        assertTrue(note.contains("1:39.4"))
        assertTrue(note.contains("2:29.1"))
        assertTrue(note.contains("3:18.8"))
        assertTrue(note.contains("4:08.6"))
        assertFalse(note.contains("2:30"))
        assertFalse(note.contains("3:20"))
        assertFalse(note.contains("4:10"))
    }

    @Test fun romanianDeadliftAlternativesDoNotUseTrapBar() {
        val rdl = AresProgramV72.resistanceSlots.filter { it.movement.contains("Romanian", true) }
        assertTrue(rdl.isNotEmpty())
        assertTrue(rdl.all { slot -> slot.alternatives.none { it.name.contains("trap", true) } })
    }
}
