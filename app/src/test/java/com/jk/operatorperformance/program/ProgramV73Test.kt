package com.jk.operatorperformance.program

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ProgramV73Test {
    @Test fun everyWeekHasApprovedTotalsAndCapsWithExplicitRecoveryExceptions() {
        assertEquals(emptyList<String>(),ProgramAudit.validateV73())
        for (week in 1..32) {
            val split = SessionResolver.splitForWeek(week, ProgramAuthority.V73)
            val total = split.flatMap { it.rows }.filter { it.kind == SessionRowKind.RESISTANCE }.sumOf { it.effortCount }
            val expected = when (week) { 8,16 -> 59; 24 -> 65; 30,31 -> 77; 32 -> 13; in 17..23 -> 146; else -> 136 }
            assertEquals("Week $week", expected, total)
            split.forEach { session ->
                assertTrue("W$week D${session.day}", session.rows.filter { it.kind == SessionRowKind.RESISTANCE }.sumOf { it.effortCount } <= ProgramV73.sessionCap(week, session.day))
            }
            assertFalse(split[0].rows.any { it.kind == SessionRowKind.RESISTANCE })
            assertEquals(1, split.count { it.rows.any { r -> r.kind == SessionRowKind.REST } })
        }
    }
    @Test fun normalDirectTargetsAndFractionalReferencesMatchApprovedMatrix() {
        val normal = listOf(16 to 16.0,14 to 16.0,8 to 15.0,15 to 15.0,10 to 15.0,10 to 14.0,15 to 20.5,9 to 18.0,7 to 14.0,13 to 13.0,7 to 7.0,5 to 6.5,7 to 7.0)
        val peak = listOf(18 to 18.0,15 to 17.0,9 to 16.5,16 to 16.0,12 to 17.0,10 to 15.0,16 to 22.0,10 to 20.0,8 to 15.5,13 to 13.0,7 to 7.0,5 to 6.5,7 to 7.0)
        // Independent order from approved matrix, not enum ordinal order.
        val muscles = listOf(Muscle.QUADS, Muscle.HAMSTRINGS, Muscle.GLUTES, Muscle.CHEST, Muscle.LATS, Muscle.UPPER_BACK, Muscle.DELTS, Muscle.BICEPS, Muscle.TRICEPS, Muscle.CALVES, Muscle.TRAPS, Muscle.FOREARMS, Muscle.ABS)
        listOf(1,9,17,25).forEach { week ->
            val expected = if (week == 17) peak else normal
            muscles.forEachIndexed { i, muscle -> assertEquals("Week $week ${muscle.label}", expected[i], ProgramV73.volumeForWeek(week).getValue(muscle)) }
        }
        val out = File("build/reports/legend-v73"); out.mkdirs()
        File(out,"muscle-by-week.tsv").writeText(ProgramV73.volumeMatrixTsv())
        File(out,"session-caps.tsv").writeText(buildString {
            appendLine("week\tday\tsets\tcap")
            for (w in 1..32) for (d in 1..7) {
                val sets = SessionResolver.resolve(w,d,ProgramAuthority.V73).rows.filter { it.kind == SessionRowKind.RESISTANCE }.sumOf { it.effortCount }
                appendLine("$w\t$d\t$sets\t${ProgramV73.sessionCap(w,d)}")
            }
        })
    }
    @Test fun allConditioningRowsAndFinalWeekAreRetainedExactly() {
        for (week in 1..32) for (day in listOf(1,4,5)) {
            val old = SessionResolver.resolve(week,day,ProgramAuthority.V72).rows
            val retained = old.filter { it.kind == SessionRowKind.CONDITIONING && it.id != "d${day}_resistance_warmup" }
            val current = SessionResolver.resolve(week,day,ProgramAuthority.V73).rows
            assertEquals("W$week D$day", retained, current.filter { r -> r.id in retained.map { it.id } })
        }
        for (day in 1..7) assertEquals(SessionResolver.resolve(32,day,ProgramAuthority.V72).rows,SessionResolver.resolve(32,day,ProgramAuthority.V73).rows)
    }
    @Test fun throwsAreNineTotalOnExactAllowedWeeksAndUpperCFollowsCooldown() {
        val allowed = setOf(3,4,5,6,10,11,13,15,17,20,25,28)
        for (week in 1..32) {
            val rows = SessionResolver.resolve(week,4,ProgramAuthority.V73).rows
            val throwIndex = rows.indexOfFirst { it.id == "v73_w${week}_throws" }
            assertEquals("Week $week", week in allowed, throwIndex >= 0)
            if (throwIndex >= 0) {
                assertTrue(rows[throwIndex-1].id.endsWith("plyometrics"))
                assertEquals(3,rows[throwIndex].effortCount)
                assertTrue(rows[throwIndex].prescription.contains("Nine total throws, both sides combined"))
                assertTrue(rows[throwIndex].note.orEmpty().contains("Zero qualifying sprint meters"))
            }
            val cooldown = rows.indexOfFirst { it.id == "w${week}_d4_cooldown" }
            if (week != 32) {
                assertTrue(cooldown >= 0)
                assertTrue(rows.indexOfFirst { it.id == "v73_d4_resistance_warmup" } > cooldown)
                assertTrue(rows.indexOfFirst { it.kind == SessionRowKind.RESISTANCE } > cooldown)
            } else assertFalse(rows.any { it.kind == SessionRowKind.RESISTANCE })
        }
    }
    @Test fun mixedSpeedReferencesPreserveCountsDistancesRecoveryAndMeters() {
        mapOf(13 to 3,20 to 3,28 to 2,30 to 2).forEach { (week, flies) ->
            val rows = SessionResolver.resolve(week,4,ProgramAuthority.V73).rows
            val acceleration = rows.single { it.id == "w${week}_d4_acceleration" }
            val fly = rows.single { it.id == "w${week}_d4_fly" }
            assertEquals(4, acceleration.effortCount); assertEquals(flies, fly.effortCount)
            assertTrue(acceleration.prescription.startsWith("4 × 30 m acceleration"))
            assertTrue(fly.prescription.startsWith("$flies × 20 m fly"))
            assertEquals(if (flies == 3) 180 else 160, ProgramV73.conditioningForWeek(week).speed.single().qualifyingSprintMeters)
        }
    }
    @Test fun newSlotsDoNotReuseLegacyIdsAndMapsHaveOnePrime() {
        val oldIds = ProgramV72.resistanceSlots.map { it.id }.toSet()
        assertTrue(ProgramV73.resistanceSlots.none { it.id in oldIds })
        ProgramV73.resistanceSlots.forEach { slot ->
            assertEquals(1,slot.roleMap.values.count { it == MuscleRole.PRIME })
            assertEquals(MuscleRole.PRIME,slot.roleMap[slot.ownerMuscle])
            assertTrue(slot.alternatives.size in 2..4)
            assertTrue(slot.restGuidance.contains("Home-rack alternative:"))
            if (slot.movement.contains("leg curl",true)) assertFalse(slot.roleMap.containsKey(Muscle.GLUTES))
            if (slot.movement == "DB pullover") assertFalse(slot.roleMap.containsKey(Muscle.BICEPS))
        }
    }
    @Test fun recoveryDosesKeepRangesAndRealizationReductionUsesNewBase() {
        assertEquals(43.38235294117647, (1.0-77.0/136)*100,0.000001)
        listOf(8,16,24,30,31).forEach { week ->
            ProgramV73.resistanceSlots.filter { it.phase == ProgramV73.phaseForWeek(week) }.forEach { slot ->
                val expectedSets = if (week in setOf(30,31)) (slot.workingSets+1)/2 else (slot.workingSets/2).coerceAtLeast(1)
                assertEquals(expectedSets,ProgramV73.plannedSets(week,slot))
                val dose = ProgramV73.plannedDose(week,slot,expectedSets)
                assertTrue(dose.contains(slot.dose.substringAfter(" ×").substringBefore(" @")))
                if (week !in setOf(30,31)) assertTrue(dose.endsWith("3–4 RIR"))
            }
        }
    }
}
