package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.security.MessageDigest

/**
 * Expected prescriptions are transcribed from the approved v7.2 DOCX.
 * They are not read back out of [ProgramV72] and then compared with themselves.
 */
class V72AuthorityComparisonTest {
    @Test fun approvedDocxHashMatches() {
        val file = authorityDocx()
        val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
        val hex = digest.joinToString("") { "%02x".format(it) }
        assertEquals(ProgramV72.EXPECTED_DOCX_SHA256, hex)
        assertTrue(ProgramV72.DOCX_SHA256_VERIFIED)
    }

    @Test fun everyConditioningCellIsPresent() {
        weeks.forEach { (week, engine, speed, easy) ->
            val plan = ProgramV72.conditioningForWeek(week)
            assertContains("W$week engine", engine, plan.engine.single().prescription)
            assertContains("W$week speed", speed, plan.speed.single().prescription)
            assertContains("W$week easy", easy, plan.d1.single().prescription)
        }
    }

    @Test fun everyEngineAndSpeedSessionIncludesCooldown() {
        (1..32).forEach { week ->
            listOf(1, 4).forEach { day ->
                val cooldown = SessionResolver.resolve(week, day, ProgramAuthority.V72).rows.filter { it.title == "Cooldown" }
                assertEquals("W$week D$day cooldown rows", 1, cooldown.size)
                assertTrue(cooldown.single().prescription.contains("8–10 min"))
            }
        }
        val easyOpening = SessionResolver.resolve(8, 1, ProgramAuthority.V72).rows.first()
        assertTrue(easyOpening.prescription.contains("no strides"))
        assertFalse(easyOpening.prescription.contains("extra cooldown"))
        assertFalse(easyOpening.note.orEmpty().contains("no added cooldown"))
    }

    @Test fun resistanceTablesMatchSection4() {
        val sampleWeek = mapOf(1 to 2, 2 to 9, 3 to 17, 4 to 25)
        phases.forEach { (phase, rows) ->
            val week = sampleWeek.getValue(phase)
            rows.groupBy { it.day }.forEach { (day, expected) ->
                val actual = ProgramV72.resistanceForDay(week, day).map { it.movement to it.dose }
                assertEquals("Phase $phase day $day", expected.map { it.movement to it.dose }, actual)
            }
        }
    }

    @Test fun phase1InclineCurlAlternativeIsPreservedLater() {
        val phase1 = ProgramV72.resistanceSlots.single { it.phase == 1 && it.movement == "Incline DB curl" }
        assertEquals(listOf("Behind-body cable curl"), phase1.alternatives.map { it.name })
        val later = ProgramV72.resistanceSlots.filter { it.phase > 1 && it.day == 3 && it.id.endsWith("_curl") }
        assertEquals(listOf(2, 3, 4), later.map { it.phase })
        assertTrue(later.all { it.alternatives.map { alt -> alt.name } == listOf("Behind-body cable curl") })
        assertTrue(later.all { slot -> slot.alternatives.none { it.name == "Band curl" || it.name == "Cable curl" } })
    }

    @Test fun consolidationHalvesSection4Sets() {
        listOf(1 to 8, 2 to 16, 3 to 24).forEach { (phase, week) ->
            phases.getValue(phase).forEach { row ->
                if (week == 24 && row.day == 1) return@forEach
                val session = SessionResolver.resolve(week, row.day, ProgramAuthority.V72)
                val actual = session.rows.first { it.title == row.movement }.prescription
                assertEquals("W$week ${row.movement}", consolidationDose(row.dose), actual)
            }
        }
    }

    @Test fun realizationAndFinalOverlayMatchSections62And63() {
        realization.forEach { (day, movement, dose) ->
            val actual = SessionResolver.resolve(30, day, ProgramAuthority.V72).rows.first { it.title == movement }.prescription
            assertEquals("W30 D$day $movement", dose, actual)
            assertEquals(dose, SessionResolver.resolve(31, day, ProgramAuthority.V72).rows.first { it.title == movement }.prescription)
        }
        realization.groupBy { it.day }.forEach { (day, rows) ->
            val titles = SessionResolver.resolve(30, day, ProgramAuthority.V72).rows.filter { it.kind == SessionRowKind.RESISTANCE }.map { it.title }
            assertEquals("W30 D$day movements", rows.map { it.movement }, titles)
        }
        val week30Names = SessionResolver.resolve(30, 6, ProgramAuthority.V72).rows.map { it.title }
        assertFalse(week30Names.contains("Leg extension"))
        week32.forEach { (day, movement, dose) ->
            val actual = SessionResolver.resolve(32, day, ProgramAuthority.V72).rows.first { it.title == movement }.prescription
            assertEquals("W32 $movement", dose, actual)
            assertFalse(actual.contains("×"))
        }
        week32.groupBy { it.day }.forEach { (day, rows) ->
            val titles = SessionResolver.resolve(32, day, ProgramAuthority.V72).rows.filter { it.kind == SessionRowKind.RESISTANCE }.map { it.title }
            assertEquals("W32 D$day movements", rows.map { it.movement }, titles)
        }
        assertTrue(SessionResolver.resolve(32, 2, ProgramAuthority.V72).rows.none { it.kind == SessionRowKind.RESISTANCE })
        assertTrue(SessionResolver.resolve(32, 5, ProgramAuthority.V72).rows.none { it.title == "Easy aerobic" })
        assertTrue(SessionResolver.resolve(32, 6, ProgramAuthority.V72).rows.none { it.kind == SessionRowKind.RESISTANCE })
    }

    private fun assertContains(label: String, expected: String, actual: String) {
        val expectedNorm = norm(expected)
        val actualNorm = norm(actual)
        assertTrue("$label\nexpected: $expectedNorm\nactual: $actualNorm", actualNorm == expectedNorm || actualNorm.contains(expectedNorm))
    }

    private fun norm(value: String) = value
        .replace('×', 'x')
        .replace(" recovery", "")
        .replace("(", "")
        .replace(")", "")
        .replace(Regex("\\s+"), " ")
        .trim()

    private fun consolidationDose(normalDose: String): String {
        val sets = normalDose.substringBefore(" ×").trim().toInt()
        val range = normalDose.substringAfter(" ×").substringBefore(" @")
        return "${maxOf(1, sets / 2)} ×$range @ 3–4 RIR"
    }

    private fun authorityDocx(): File {
        val name = "ARES_Performance_Unified_Master_Authority_v7.2_APPROVED.docx"
        return listOf(File("docs", name), File("../docs", name), File("../../docs", name)).firstOrNull { it.isFile }
            ?: error("Approved DOCX not found from ${File(".").absolutePath}")
    }

    private data class Lift(val day: Int, val movement: String, val dose: String)

    private val phases = mapOf(
        1 to listOf(
            Lift(1, "Incline DB press", "2 × 10–12 @ 1 RIR"),
            Lift(1, "Cable or DB fly", "2 × 12–15 @ 1 RIR"),
            Lift(1, "Rope pushdown", "3 × 10–15 @ 0–1 RIR"),
            Lift(1, "Cable or DB curl", "2 × 10–15 @ 0–1 RIR"),
            Lift(1, "Standing calf raise", "2 × 12–15 @ 1 RIR"),
            Lift(1, "Ab wheel or cable crunch", "2 × 10–15 @ 1 RIR"),
            Lift(2, "Back or front squat", "4 × 6–8 @ 2 RIR"),
            Lift(2, "Romanian deadlift", "3 × 8–10 @ 2 RIR"),
            Lift(2, "Leg press or hack squat", "3 × 10–12 @ 1–2 RIR"),
            Lift(2, "Seated leg curl", "3 × 10–15 @ 1 RIR"),
            Lift(2, "Standing calf raise", "4 × 8–12 @ 1 RIR"),
            Lift(2, "Weighted crunch", "3 × 10–15 @ 1 RIR"),
            Lift(3, "Flat barbell bench press", "4 × 6–8 @ 2 RIR"),
            Lift(3, "Chest-supported row", "4 × 8–10 @ 2 RIR"),
            Lift(3, "Incline press", "3 × 8–10 @ 1–2 RIR"),
            Lift(3, "Cable or DB row", "3 × 10–12 @ 1 RIR"),
            Lift(3, "Lateral raise", "3 × 12–20 @ 0–1 RIR"),
            Lift(3, "Overhead triceps extension", "3 × 10–15 @ 0–1 RIR"),
            Lift(3, "Incline DB curl", "3 × 10–15 @ 0–1 RIR"),
            Lift(3, "Paused shrug", "2 × 10–15 @ 1 RIR"),
            Lift(5, "Standing overhead press", "3 × 6–8 @ 2 RIR"),
            Lift(5, "Lat pulldown or pull-up", "4 × 8–12 @ 1–2 RIR"),
            Lift(5, "DB or cable pullover", "2 × 10–15 @ 1 RIR"),
            Lift(5, "Rear-delt fly or face pull", "3 × 12–20 @ 0–1 RIR"),
            Lift(5, "Lateral raise", "3 × 12–20 @ 0–1 RIR"),
            Lift(5, "Hammer curl", "3 × 10–15 @ 0–1 RIR"),
            Lift(5, "Reverse wrist curl", "2 × 12–20 @ 1 RIR"),
            Lift(5, "Paused shrug", "2 × 10–15 @ 1 RIR"),
            Lift(6, "Hip thrust", "3 × 8–12 @ 1–2 RIR"),
            Lift(6, "Bulgarian split squat", "3 × 8–10 @ 1–2 RIR"),
            Lift(6, "Leg extension", "3 × 12–15 @ 1 RIR"),
            Lift(6, "Seated or lying leg curl", "4 × 10–15 @ 1 RIR"),
            Lift(6, "DB Romanian deadlift", "2 × 10–12 @ 2 RIR"),
            Lift(6, "Seated calf raise", "4 × 10–15 @ 1 RIR"),
            Lift(6, "Hanging leg raise", "3 × 10–15 @ 1 RIR"),
            Lift(6, "Wrist curl", "2 × 12–20 @ 1 RIR"),
        ),
        2 to listOf(
            Lift(1, "Incline DB press", "2 × 8–12 @ 1 RIR"),
            Lift(1, "Fly", "1 × 12–15 @ 1 RIR"),
            Lift(1, "Pushdown", "3 × 10–15 @ 0–1 RIR"),
            Lift(1, "Curl", "2 × 10–15 @ 0–1 RIR"),
            Lift(1, "Standing calf raise", "2 × 12–15 @ 1 RIR"),
            Lift(1, "Ab wheel or cable crunch", "2 × 10–15 @ 1 RIR"),
            Lift(2, "Back, front, or safety-bar squat", "5 × 3–5 @ 2 RIR"),
            Lift(2, "Romanian deadlift", "3 × 5–8 @ 2 RIR"),
            Lift(2, "Leg press or hack squat", "3 × 8–10 @ 1–2 RIR"),
            Lift(2, "Seated leg curl", "3 × 8–12 @ 1 RIR"),
            Lift(2, "Standing calf raise", "3 × 8–12 @ 1 RIR"),
            Lift(2, "Weighted crunch", "2 × 10–15 @ 1 RIR"),
            Lift(3, "Flat barbell bench press", "5 × 3–5 @ 2 RIR"),
            Lift(3, "Chest-supported or barbell row", "4 × 6–8 @ 2 RIR"),
            Lift(3, "Incline press", "3 × 6–10 @ 1–2 RIR"),
            Lift(3, "Lat pulldown or pull-up", "3 × 6–10 @ 1–2 RIR"),
            Lift(3, "Lateral raise", "3 × 12–20 @ 0–1 RIR"),
            Lift(3, "Triceps extension", "3 × 8–12 @ 1 RIR"),
            Lift(3, "Incline curl", "3 × 8–12 @ 1 RIR"),
            Lift(3, "Paused shrug", "2 × 8–12 @ 1 RIR"),
            Lift(5, "Standing overhead press", "4 × 4–6 @ 2 RIR"),
            Lift(5, "Weighted pull-up or pulldown", "4 × 5–8 @ 2 RIR"),
            Lift(5, "Cable or chest-supported row", "3 × 8–10 @ 1–2 RIR"),
            Lift(5, "Rear-delt fly or face pull", "3 × 12–20 @ 0–1 RIR"),
            Lift(5, "Lateral raise", "2 × 12–20 @ 0–1 RIR"),
            Lift(5, "Hammer curl", "3 × 8–12 @ 1 RIR"),
            Lift(5, "Reverse wrist curl", "2 × 12–20 @ 1 RIR"),
            Lift(6, "Hip thrust", "3 × 6–10 @ 1–2 RIR"),
            Lift(6, "Bulgarian split squat", "3 × 6–8 @ 1–2 RIR"),
            Lift(6, "Leg curl", "4 × 8–12 @ 1 RIR"),
            Lift(6, "Leg extension", "3 × 10–15 @ 1 RIR"),
            Lift(6, "DB Romanian deadlift", "2 × 8–10 @ 2 RIR"),
            Lift(6, "Seated calf raise", "3 × 10–15 @ 1 RIR"),
            Lift(6, "Hanging leg raise", "2 × 10–15 @ 1 RIR"),
            Lift(6, "Wrist curl", "2 × 12–20 @ 1 RIR"),
        ),
        3 to listOf(
            Lift(1, "Incline DB press", "2 × 8–12 @ 1 RIR"),
            Lift(1, "Fly", "2 × 12–15 @ 1 RIR"),
            Lift(1, "Pushdown", "3 × 10–15 @ 0–1 RIR"),
            Lift(1, "Curl", "2 × 10–15 @ 0–1 RIR"),
            Lift(1, "Standing calf raise", "2 × 12–15 @ 1 RIR"),
            Lift(1, "Ab wheel or cable crunch", "2 × 10–15 @ 1 RIR"),
            Lift(2, "Squat", "3 × 4–6 @ 2 RIR"),
            Lift(2, "Romanian deadlift", "2 × 6–8 @ 2 RIR"),
            Lift(2, "Leg press or hack squat", "3 × 8–12 @ 1–2 RIR"),
            Lift(2, "Leg curl", "3 × 10–15 @ 1 RIR"),
            Lift(2, "Standing calf raise", "3 × 8–12 @ 1 RIR"),
            Lift(2, "Weighted crunch", "2 × 10–15 @ 1 RIR"),
            Lift(3, "Bench press", "3 × 4–6 @ 2 RIR"),
            Lift(3, "Chest-supported row", "3 × 6–10 @ 2 RIR"),
            Lift(3, "Incline press", "3 × 8–10 @ 1–2 RIR"),
            Lift(3, "Pulldown or pull-up", "3 × 8–12 @ 1–2 RIR"),
            Lift(3, "Lateral raise", "3 × 12–20 @ 0–1 RIR"),
            Lift(3, "Triceps extension", "3 × 10–15 @ 1 RIR"),
            Lift(3, "Curl", "3 × 10–15 @ 1 RIR"),
            Lift(5, "Overhead press", "3 × 4–6 @ 2 RIR"),
            Lift(5, "Pull-up or pulldown", "3 × 6–10 @ 2 RIR"),
            Lift(5, "Cable or chest-supported row", "3 × 8–12 @ 1–2 RIR"),
            Lift(5, "Rear-delt fly", "3 × 12–20 @ 0–1 RIR"),
            Lift(5, "Lateral raise", "3 × 12–20 @ 0–1 RIR"),
            Lift(5, "Hammer curl", "3 × 10–15 @ 1 RIR"),
            Lift(5, "Paused shrug", "2 × 10–15 @ 1 RIR"),
            Lift(6, "Hip thrust", "3 × 8–10 @ 2 RIR"),
            Lift(6, "Bulgarian split squat", "3 × 8–10 @ 2 RIR"),
            Lift(6, "Leg extension", "2 × 10–15 @ 1 RIR"),
            Lift(6, "Leg curl", "3 × 10–15 @ 1 RIR"),
            Lift(6, "DB Romanian deadlift", "2 × 8–10 @ 2 RIR"),
            Lift(6, "Seated calf raise", "3 × 10–15 @ 1 RIR"),
            Lift(6, "Hanging leg raise", "2 × 10–15 @ 1 RIR"),
        ),
        4 to listOf(
            Lift(1, "Incline DB press", "2 × 8–12 @ 1–2 RIR"),
            Lift(1, "Fly", "1 × 12–15 @ 1 RIR"),
            Lift(1, "Pushdown", "3 × 10–15 @ 1 RIR"),
            Lift(1, "Curl", "2 × 10–15 @ 1 RIR"),
            Lift(1, "Standing calf raise", "2 × 12–15 @ 1–2 RIR"),
            Lift(1, "Ab wheel or cable crunch", "2 × 10–15 @ 1–2 RIR"),
            Lift(2, "Squat", "3 × 4–6 @ 2–3 RIR"),
            Lift(2, "Romanian deadlift", "2 × 6–8 @ 2–3 RIR"),
            Lift(2, "Leg press or hack squat", "2 × 8–10 @ 2 RIR"),
            Lift(2, "Leg curl", "2 × 10–12 @ 1–2 RIR"),
            Lift(2, "Standing calf raise", "3 × 8–12 @ 2 RIR"),
            Lift(2, "Weighted crunch", "2 × 10–15 @ 2 RIR"),
            Lift(3, "Bench press", "3 × 4–6 @ 2 RIR"),
            Lift(3, "Chest-supported row", "3 × 6–10 @ 2 RIR"),
            Lift(3, "Incline press", "2 × 8–10 @ 2 RIR"),
            Lift(3, "Pulldown or pull-up", "2 × 8–10 @ 2 RIR"),
            Lift(3, "Lateral raise", "2 × 12–20 @ 1 RIR"),
            Lift(3, "Triceps extension", "3 × 10–15 @ 1 RIR"),
            Lift(3, "Curl", "2 × 10–15 @ 1 RIR"),
            Lift(5, "Overhead press", "2 × 4–6 @ 2 RIR"),
            Lift(5, "Pull-up or pulldown", "3 × 6–10 @ 2 RIR"),
            Lift(5, "Cable or chest-supported row", "2 × 8–12 @ 2 RIR"),
            Lift(5, "Rear-delt fly", "3 × 12–20 @ 1 RIR"),
            Lift(5, "Lateral raise", "2 × 12–20 @ 1 RIR"),
            Lift(5, "Hammer curl", "2 × 10–15 @ 1 RIR"),
            Lift(6, "Hip thrust", "2 × 8–10 @ 2 RIR"),
            Lift(6, "Bulgarian split squat", "2 × 8–10 @ 2 RIR"),
            Lift(6, "Leg extension", "1 × 10–15 @ 1–2 RIR"),
            Lift(6, "Leg curl", "2 × 10–12 @ 1–2 RIR"),
            Lift(6, "DB Romanian deadlift", "2 × 8–10 @ 2–3 RIR"),
            Lift(6, "Seated calf raise", "3 × 10–15 @ 2 RIR"),
            Lift(6, "Hanging leg raise", "2 × 10–15 @ 2 RIR"),
        ),
    )

    private val realization = listOf(
        Triple(1, "Incline DB press", "1 × 8–12 @ 2–3 RIR"),
        Triple(1, "Fly", "1 × 12–15 @ 1–2 RIR"),
        Triple(1, "Pushdown", "2 × 10–15 @ 1–2 RIR"),
        Triple(1, "Curl", "1 × 10–15 @ 1–2 RIR"),
        Triple(1, "Standing calf raise", "1 × 12–15 @ 1–2 RIR"),
        Triple(1, "Ab wheel or cable crunch", "1 × 10–15 @ 1–2 RIR"),
        Triple(2, "Squat", "2 × 4–6 @ 2–3 RIR"),
        Triple(2, "Romanian deadlift", "1 × 6–8 @ 2–3 RIR"),
        Triple(2, "Leg press or hack squat", "1 × 8–10 @ 2–3 RIR"),
        Triple(2, "Leg curl", "1 × 10–12 @ 1–2 RIR"),
        Triple(2, "Standing calf raise", "2 × 8–12 @ 1–2 RIR"),
        Triple(2, "Weighted crunch", "1 × 10–15 @ 1–2 RIR"),
        Triple(3, "Bench press", "2 × 4–6 @ 2–3 RIR"),
        Triple(3, "Chest-supported row", "2 × 6–10 @ 2–3 RIR"),
        Triple(3, "Incline press", "1 × 8–10 @ 2–3 RIR"),
        Triple(3, "Pulldown or pull-up", "1 × 8–10 @ 2–3 RIR"),
        Triple(3, "Lateral raise", "1 × 12–20 @ 1–2 RIR"),
        Triple(3, "Triceps extension", "1 × 10–15 @ 1–2 RIR"),
        Triple(3, "Curl", "1 × 10–15 @ 1–2 RIR"),
        Triple(5, "Overhead press", "1 × 4–6 @ 2–3 RIR"),
        Triple(5, "Pull-up or pulldown", "2 × 6–10 @ 2–3 RIR"),
        Triple(5, "Cable or chest-supported row", "1 × 8–12 @ 2–3 RIR"),
        Triple(5, "Rear-delt fly", "1 × 12–20 @ 1–2 RIR"),
        Triple(5, "Lateral raise", "1 × 12–20 @ 1–2 RIR"),
        Triple(5, "Hammer curl", "1 × 10–15 @ 1–2 RIR"),
        Triple(6, "Hip thrust", "1 × 8–10 @ 2–3 RIR"),
        Triple(6, "Bulgarian split squat", "1 × 8–10 @ 2–3 RIR"),
        Triple(6, "Leg curl", "1 × 10–12 @ 1–2 RIR"),
        Triple(6, "DB Romanian deadlift", "1 × 8–10 @ 2–3 RIR"),
        Triple(6, "Seated calf raise", "2 × 10–15 @ 1–2 RIR"),
        Triple(6, "Hanging leg raise", "1 × 10–15 @ 1–2 RIR"),
    )

    private val week32 = listOf(
        Triple(3, "Bench press", "2 sets @ 3 RIR"),
        Triple(3, "Chest-supported row", "2 sets @ 3 RIR"),
        Triple(3, "Pulldown or pull-up", "1 set @ 3 RIR"),
        Triple(3, "Lateral raise", "1 set @ 3 RIR"),
        Triple(3, "Triceps extension", "1 set @ 3 RIR"),
        Triple(3, "Curl", "1 set @ 3 RIR"),
        Triple(5, "Overhead press", "1 set @ 3 RIR"),
        Triple(5, "Pull-up or pulldown", "2 sets @ 3 RIR"),
        Triple(5, "Cable or chest-supported row", "1 set @ 3 RIR"),
        Triple(5, "Rear-delt fly", "1 set @ 3 RIR"),
    )

    private data class Triple(val day: Int, val movement: String, val dose: String)

    private val weeks = listOf(
        Row(1, "Controlled 1.5-mile baseline; no short upper", "Timed 300 m only if eligibility gate is met; otherwise 6 x 30 m at 70–85%; no jumps", "Bike 20 min easy"),
        Row(2, "3 x 6 min threshold; 2 min easy", "6 x 20 m acceleration; 3 min", "Bike 25 min easy"),
        Row(3, "5 x 3 min controlled interval; 2 min easy", "6 x 30 m acceleration; 3–4 min", "Bike 25 min easy"),
        Row(4, "20 min continuous threshold", "4 x 20 m build + 20 m fly; 5 min", "Bike 30 min easy"),
        Row(5, "6 x 400 m controlled interval; 90 s easy", "8 x 30 m acceleration; 3–4 min", "Easy run 15 min or bike 25 min"),
        Row(6, "4 x 1000 m controlled interval; 2 min easy", "5 x 20 m build + 20 m fly; 5 min", "Easy run 20 min or bike 30 min"),
        Row(7, "3 x 1 mile threshold; 1 min easy", "3 x 150 m at 90%; 5–6 min", "Bike 25 min easy"),
        Row(8, "Easy run 25 min; easy-only warm-up", "4 x 20 m relaxed acceleration; 3 min; no jumps", "Bike 20 min easy"),
        Row(9, "3 x 1 mile threshold; 1 min easy", "3 x 150 m at 90%; 5–6 min", "Easy run 20 min"),
        Row(10, "4 x 1000 m controlled interval; 2 min easy", "6 x 30 m acceleration; 3–4 min", "Bike 30 min easy"),
        Row(11, "25 min continuous threshold", "5 x 20 m build + 20 m fly; 5 min", "Easy run 22 min"),
        Row(12, "6 x 800 m controlled interval; 2 min easy", "3 x 200 m at 90–92%; 6–8 min; 600 m warning", "Bike 30 min easy"),
        Row(13, "6 x 400 m at goal pace; 90 s easy", "4 x 30 m acceleration + 3 x 20 m fly; full recovery", "Easy run 25 min"),
        Row(14, "2 x 1 mile threshold; 2 min easy", "2 x 250 m at 90–93%; 7–8 min", "Bike 30 min easy"),
        Row(15, "5 x 1000 m controlled interval; 2 min easy", "6 x 30 m acceleration; full recovery", "Easy run 25 min"),
        Row(16, "Easy run 20 min; easy-only warm-up", "300 m checkpoint only if gate is met; no jumps", "Bike 20 min easy"),
        Row(17, "25 min continuous threshold", "5 x 20 m build + 20 m fly; 5 min", "Easy run 25 min"),
        Row(18, "3 x 800 m at goal pace; 3 min easy", "3 x 200 m at 92–95%; 6–8 min; 600 m warning", "Bike 30 min easy"),
        Row(19, "6 x 800 m controlled interval; 2 min easy", "2 x 300 m at 88–92%; 8–10 min; 600 m warning", "Easy run 30 min"),
        Row(20, "4 x 600 m at goal pace; 2.5–3 min easy", "4 x 30 m acceleration + 3 x 20 m fly; full recovery", "Easy run 30 min"),
        Row(21, "20 min continuous threshold", "2 x 250 m at 93–95%; 8 min", "Bike 30 min easy"),
        Row(22, "4 x 1000 m controlled interval; 2 min easy", "3 x 200 m at 93–95%; 7–8 min; 600 m warning", "Easy run 30 min"),
        Row(23, "6 x 400 m at goal pace; 90 s easy", "2 x 300 m at 90–93%; 8–10 min; 600 m warning", "Easy run 30 min"),
        Row(24, "Timed 1.5-mile checkpoint; no short upper", "4 x 20 m relaxed acceleration; 3 min; no jumps", "Bike 20 min easy"),
        Row(25, "20 min continuous threshold", "4 x 20 m build + 20 m fly; 5 min", "Easy run 25 min"),
        Row(26, "3 x 800 m at goal pace; 3 min easy", "2 x 250 m at 95%; 8 min", "Easy run 25 min"),
        Row(27, "4 x 600 m at goal pace; 3 min easy", "2 x 200 m at 95%; 8 min", "Easy run 20 min"),
        Row(28, "4 x 400 m at goal pace; 2 min easy", "4 x 30 m acceleration + 2 x 20 m fly; full recovery", "Easy run 20 min"),
        Row(29, "3 x 800 m at goal pace; 3 min easy", "2 x 200 m at 95%; 8 min", "Bike 20 min easy"),
        Row(30, "4 x 400 m at goal pace; 2 min easy", "4 x 30 m acceleration + 2 x 20 m fly; full recovery; no jumps", "Bike 15–20 min easy"),
        Row(31, "3 x 400 m at goal pace; 2.5 min easy", "4 x 20 m relaxed acceleration; full recovery; no jumps", "Bike 15 min very easy"),
        Row(32, "Final timed 1.5-mile; no short upper", "Final timed 300 m only if gate is met; no jumps", "No Day 5 aerobic; use final-test overlay"),
    )

    private data class Row(val week: Int, val engine: String, val speed: String, val easy: String)
}
