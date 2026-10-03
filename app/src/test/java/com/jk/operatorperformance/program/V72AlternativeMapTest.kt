package com.jk.operatorperformance.program

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Alternatives are transcribed from v7.2 Section 4 and Section 7.1.
 * Later phases are not compared with a value read back out of Phase 1.
 * Section 4 keeps the Phase 1 cell for the same slot. Section 7.1 rejects a cross-pattern swap.
 */
class V72AlternativeMapTest {
    @Test fun phase1CellsMatchTheApprovedAlternativeColumn() {
        assertEquals(phase1, names(1))
    }

    @Test fun everyLaterPhaseSlotMatchesTheInheritedMap() {
        assertEquals(100, later.size)
        assertEquals(later, names(2) + names(3) + names(4))
        later.forEach { expected ->
            val actual = AresProgramV72.resistanceSlots.single {
                it.phase == expected.phase && it.day == expected.day && it.movement == expected.movement
            }
            assertEquals("${expected.phase} D${expected.day} ${expected.movement}", expected.alternatives, actual.alternatives.map { it.name })
        }
    }

    @Test fun movementPatternsStayDistinct() {
        val all = AresProgramV72.resistanceSlots
        assertTrue(all.none { slot -> slot.alternatives.any { it.name.contains("trap", ignoreCase = true) } })
        listOf(2, 3, 4).forEach { phase ->
            assertEquals(listOf("Safety-bar squat", "Smith squat"), alts(phase, 2, "squat"))
            assertFalse(alts(phase, 2, "squat").any { it.contains("leg press", true) || it.contains("hack", true) })
            assertEquals(listOf("DB RDL", "Smith RDL"), alts(phase, 2, "rdl"))
            assertEquals(listOf("Smith RDL", "Cable pull-through"), alts(phase, 6, "rdl"))
            assertEquals(listOf("Band curl"), alts(phase, 1, "curl"))
            assertEquals(listOf("Behind-body cable curl"), alts(phase, 3, "curl"))
            assertEquals(listOf("Barbell row", "Cable row", "DB row"), alts(phase, 3, "row"))
            assertEquals(listOf("T-bar row", "One-arm row"), alts(phase, 5, "row"))
            assertEquals(listOf("Assisted pull-up", "Band pulldown"), alts(phase, 3, "pull"))
            assertEquals(listOf("Bent-knee Smith calf raise", "DB calf raise"), alts(phase, 6, "calf"))
            assertEquals(listOf("Smith calf raise", "Leg-press calf raise"), alts(phase, 1, "calf"))
            assertEquals(listOf("Reverse crunch", "Captain chair"), alts(phase, 6, "abs"))
            assertFalse(alts(phase, 6, "abs").any { it.contains("Cable crunch") })
            assertEquals(listOf("Reverse lunge", "Smith split squat"), alts(phase, 6, "bss"))
            assertFalse(alts(phase, 6, "bss").any { it.contains("supported", true) })
            assertEquals(listOf("Seated DB press", "Smith press"), alts(phase, 5, "ohp"))
            assertFalse(alts(phase, 5, "ohp").any { it.contains("machine", true) })
        }
    }

    private fun names(phase: Int) = AresProgramV72.resistanceSlots
        .filter { it.phase == phase }
        .map { Slot(it.phase, it.day, it.movement, it.alternatives.map { alt -> alt.name }) }

    private fun alts(phase: Int, day: Int, key: String) =
        AresProgramV72.resistanceSlots.single { it.phase == phase && it.id == "d${day}_$key" }.alternatives.map { it.name }

    private data class Slot(val phase: Int, val day: Int, val movement: String, val alternatives: List<String>)

    private val incline = listOf("Smith incline", "Machine incline")
    private val fly = listOf("Pec deck", "Band fly")
    private val pushdown = listOf("Bar pushdown", "Single-arm pushdown")
    private val curl = listOf("Band curl")
    private val standingCalf = listOf("Smith calf raise", "Leg-press calf raise")
    private val abWheel = listOf("Weighted crunch")
    private val squat = listOf("Safety-bar squat", "Smith squat")
    private val rdl = listOf("DB RDL", "Smith RDL")
    private val legPress = listOf("Smith squat", "Heel-elevated DB squat")
    private val legCurl = listOf("Lying leg curl", "Slider curl")
    private val crunch = listOf("Cable crunch", "Machine crunch")
    private val bench = listOf("DB press", "Smith press", "Machine press")
    private val chestRow = listOf("Barbell row", "Cable row", "DB row")
    private val inclinePress = listOf("DB incline", "Smith incline", "Machine incline")
    private val verticalPull = listOf("Assisted pull-up", "Band pulldown")
    private val lateral = listOf("Cable lateral", "Machine lateral")
    private val triceps = listOf("DB extension", "EZ-bar extension", "Cable extension")
    private val inclineCurl = listOf("Behind-body cable curl")
    private val shrug = listOf("DB shrug", "Barbell shrug", "Smith shrug", "Cable shrug")
    private val ohp = listOf("Seated DB press", "Smith press")
    private val pullover = listOf("Straight-arm pulldown")
    private val rear = listOf("Reverse pec deck")
    private val hammer = listOf("Rope or cross-body curl")
    private val reverseWrist = listOf("DB reverse wrist curl", "Cable reverse wrist curl", "Band reverse wrist curl")
    private val hip = listOf("Barbell hip thrust", "Smith hip thrust", "DB hip thrust")
    private val split = listOf("Reverse lunge", "Smith split squat")
    private val extension = listOf("Spanish squat", "Reverse Nordic")
    private val slider = listOf("Slider curl")
    private val dbRdl = listOf("Smith RDL", "Cable pull-through")
    private val seatedCalf = listOf("Bent-knee Smith calf raise", "DB calf raise")
    private val hanging = listOf("Reverse crunch", "Captain chair")
    private val wrist = listOf("DB wrist curl", "Cable wrist curl", "Band wrist curl")
    private val cableRow = listOf("T-bar row", "One-arm row")

    private val phase1 = listOf(
        Slot(1, 1, "Incline DB press", incline),
        Slot(1, 1, "Cable or DB fly", fly),
        Slot(1, 1, "Rope pushdown", pushdown),
        Slot(1, 1, "Cable or DB curl", curl),
        Slot(1, 1, "Standing calf raise", standingCalf),
        Slot(1, 1, "Ab wheel or cable crunch", abWheel),
        Slot(1, 2, "Back or front squat", squat),
        Slot(1, 2, "Romanian deadlift", rdl),
        Slot(1, 2, "Leg press or hack squat", legPress),
        Slot(1, 2, "Seated leg curl", legCurl),
        Slot(1, 2, "Standing calf raise", standingCalf),
        Slot(1, 2, "Weighted crunch", crunch),
        Slot(1, 3, "Flat barbell bench press", bench),
        Slot(1, 3, "Chest-supported row", chestRow),
        Slot(1, 3, "Incline press", inclinePress),
        Slot(1, 3, "Cable or DB row", cableRow),
        Slot(1, 3, "Lateral raise", lateral),
        Slot(1, 3, "Overhead triceps extension", triceps),
        Slot(1, 3, "Incline DB curl", inclineCurl),
        Slot(1, 3, "Paused shrug", shrug),
        Slot(1, 5, "Standing overhead press", ohp),
        Slot(1, 5, "Lat pulldown or pull-up", verticalPull),
        Slot(1, 5, "DB or cable pullover", pullover),
        Slot(1, 5, "Rear-delt fly or face pull", rear),
        Slot(1, 5, "Lateral raise", lateral),
        Slot(1, 5, "Hammer curl", hammer),
        Slot(1, 5, "Reverse wrist curl", reverseWrist),
        Slot(1, 5, "Paused shrug", shrug),
        Slot(1, 6, "Hip thrust", hip),
        Slot(1, 6, "Bulgarian split squat", split),
        Slot(1, 6, "Leg extension", extension),
        Slot(1, 6, "Seated or lying leg curl", slider),
        Slot(1, 6, "DB Romanian deadlift", dbRdl),
        Slot(1, 6, "Seated calf raise", seatedCalf),
        Slot(1, 6, "Hanging leg raise", hanging),
        Slot(1, 6, "Wrist curl", wrist),
    )

    private val later = listOf(
        Slot(2, 1, "Incline DB press", incline),
        Slot(2, 1, "Fly", fly),
        Slot(2, 1, "Pushdown", pushdown),
        Slot(2, 1, "Curl", curl),
        Slot(2, 1, "Standing calf raise", standingCalf),
        Slot(2, 1, "Ab wheel or cable crunch", abWheel),
        Slot(2, 2, "Back, front, or safety-bar squat", squat),
        Slot(2, 2, "Romanian deadlift", rdl),
        Slot(2, 2, "Leg press or hack squat", legPress),
        Slot(2, 2, "Seated leg curl", legCurl),
        Slot(2, 2, "Standing calf raise", standingCalf),
        Slot(2, 2, "Weighted crunch", crunch),
        Slot(2, 3, "Flat barbell bench press", bench),
        Slot(2, 3, "Chest-supported or barbell row", chestRow),
        Slot(2, 3, "Incline press", inclinePress),
        Slot(2, 3, "Lat pulldown or pull-up", verticalPull),
        Slot(2, 3, "Lateral raise", lateral),
        Slot(2, 3, "Triceps extension", triceps),
        Slot(2, 3, "Incline curl", inclineCurl),
        Slot(2, 3, "Paused shrug", shrug),
        Slot(2, 5, "Standing overhead press", ohp),
        Slot(2, 5, "Weighted pull-up or pulldown", verticalPull),
        Slot(2, 5, "Cable or chest-supported row", cableRow),
        Slot(2, 5, "Rear-delt fly or face pull", rear),
        Slot(2, 5, "Lateral raise", lateral),
        Slot(2, 5, "Hammer curl", hammer),
        Slot(2, 5, "Reverse wrist curl", reverseWrist),
        Slot(2, 6, "Hip thrust", hip),
        Slot(2, 6, "Bulgarian split squat", split),
        Slot(2, 6, "Leg curl", slider),
        Slot(2, 6, "Leg extension", extension),
        Slot(2, 6, "DB Romanian deadlift", dbRdl),
        Slot(2, 6, "Seated calf raise", seatedCalf),
        Slot(2, 6, "Hanging leg raise", hanging),
        Slot(2, 6, "Wrist curl", wrist),
        Slot(3, 1, "Incline DB press", incline),
        Slot(3, 1, "Fly", fly),
        Slot(3, 1, "Pushdown", pushdown),
        Slot(3, 1, "Curl", curl),
        Slot(3, 1, "Standing calf raise", standingCalf),
        Slot(3, 1, "Ab wheel or cable crunch", abWheel),
        Slot(3, 2, "Squat", squat),
        Slot(3, 2, "Romanian deadlift", rdl),
        Slot(3, 2, "Leg press or hack squat", legPress),
        Slot(3, 2, "Leg curl", legCurl),
        Slot(3, 2, "Standing calf raise", standingCalf),
        Slot(3, 2, "Weighted crunch", crunch),
        Slot(3, 3, "Bench press", bench),
        Slot(3, 3, "Chest-supported row", chestRow),
        Slot(3, 3, "Incline press", inclinePress),
        Slot(3, 3, "Pulldown or pull-up", verticalPull),
        Slot(3, 3, "Lateral raise", lateral),
        Slot(3, 3, "Triceps extension", triceps),
        Slot(3, 3, "Curl", inclineCurl),
        Slot(3, 5, "Overhead press", ohp),
        Slot(3, 5, "Pull-up or pulldown", verticalPull),
        Slot(3, 5, "Cable or chest-supported row", cableRow),
        Slot(3, 5, "Rear-delt fly", rear),
        Slot(3, 5, "Lateral raise", lateral),
        Slot(3, 5, "Hammer curl", hammer),
        Slot(3, 5, "Paused shrug", shrug),
        Slot(3, 6, "Hip thrust", hip),
        Slot(3, 6, "Bulgarian split squat", split),
        Slot(3, 6, "Leg extension", extension),
        Slot(3, 6, "Leg curl", slider),
        Slot(3, 6, "DB Romanian deadlift", dbRdl),
        Slot(3, 6, "Seated calf raise", seatedCalf),
        Slot(3, 6, "Hanging leg raise", hanging),
        Slot(4, 1, "Incline DB press", incline),
        Slot(4, 1, "Fly", fly),
        Slot(4, 1, "Pushdown", pushdown),
        Slot(4, 1, "Curl", curl),
        Slot(4, 1, "Standing calf raise", standingCalf),
        Slot(4, 1, "Ab wheel or cable crunch", abWheel),
        Slot(4, 2, "Squat", squat),
        Slot(4, 2, "Romanian deadlift", rdl),
        Slot(4, 2, "Leg press or hack squat", legPress),
        Slot(4, 2, "Leg curl", legCurl),
        Slot(4, 2, "Standing calf raise", standingCalf),
        Slot(4, 2, "Weighted crunch", crunch),
        Slot(4, 3, "Bench press", bench),
        Slot(4, 3, "Chest-supported row", chestRow),
        Slot(4, 3, "Incline press", inclinePress),
        Slot(4, 3, "Pulldown or pull-up", verticalPull),
        Slot(4, 3, "Lateral raise", lateral),
        Slot(4, 3, "Triceps extension", triceps),
        Slot(4, 3, "Curl", inclineCurl),
        Slot(4, 5, "Overhead press", ohp),
        Slot(4, 5, "Pull-up or pulldown", verticalPull),
        Slot(4, 5, "Cable or chest-supported row", cableRow),
        Slot(4, 5, "Rear-delt fly", rear),
        Slot(4, 5, "Lateral raise", lateral),
        Slot(4, 5, "Hammer curl", hammer),
        Slot(4, 6, "Hip thrust", hip),
        Slot(4, 6, "Bulgarian split squat", split),
        Slot(4, 6, "Leg extension", extension),
        Slot(4, 6, "Leg curl", slider),
        Slot(4, 6, "DB Romanian deadlift", dbRdl),
        Slot(4, 6, "Seated calf raise", seatedCalf),
        Slot(4, 6, "Hanging leg raise", hanging),
    )
}
