package com.jk.operatorperformance.program

/**
 * Immutable ARES Performance v7.2 prescription.
 * The approved DOCX was not in this workspace, so [EXPECTED_DOCX_SHA256] is the
 * owner-supplied digest and has not been recomputed from file bytes.
 */
object AresProgramV72 {
    const val AUTHORITY_VERSION = "7.2"
    const val EXPECTED_DOCX_SHA256 = "25a7ce497569913c4a56c057bcaa139290adddf20876a2d736790d5fefd436f3"
    const val DOCX_SHA256_VERIFIED = false
    const val PROGRAM_NAME = "ARES PERFORMANCE"
    const val PROGRAM_TAGLINE = "Muscle · Strength · Speed · Engine"
    const val TOTAL_WEEKS = 32
    const val DAYS_PER_WEEK = 7
    const val TOTAL_DAYS = 224

    const val COMPOUND_REST = "Rest 2–4 min."
    const val ISOLATION_REST = "Rest 1–2 min."
    const val GOAL_PACE_TABLE =
        "400 m 1:39.4; 600 m 2:29.1; 800 m 3:18.8; 1000 m 4:08.6; 1 mile 6:40.0; 1.5 miles 10:00.0"
    const val STATUTE_MILE_AND_HALF_METRES = 2414.016
    const val ANCHOR_NOTE =
        "Percentage reps use current pain-free 300 m average velocity only. Target time = valid 300 m time × distance / 300 / stated decimal percentage. v7.2 states no RPE fallback and the 46-second goal is not a substitute anchor. Use an external calculator. The app does not store the result."

    val PERCENTAGE_SPEED_WEEKS = setOf(7, 9, 12, 14, 18, 19, 21, 22, 23, 26, 27, 29)

    val blocks = listOf(
        ProgramBlock("FOUNDATION", "HEPHAESTUS", 1, 8, BlockStatus.POPULATED, "Hypertrophy base, running tolerance, acceleration, aerobic foundation."),
        ProgramBlock("DEVELOPMENT", "HERMES", 9, 16, BlockStatus.POPULATED, "Strength, maximum velocity, threshold and interval development."),
        ProgramBlock("SPECIFIC DEVELOPMENT", "ARES", 17, 24, BlockStatus.POPULATED, "Special endurance and 1.5-mile goal-pace development while retaining hypertrophy volume."),
        ProgramBlock("INTEGRATION / PEAK", "ZEUS", 25, 32, BlockStatus.POPULATED, "Event specificity, controlled fatigue reduction, and final tests."),
    )
    val weeks = (1..TOTAL_WEEKS).map(::WeekDefinition)
    val dayTitles = mapOf(
        1 to "Engine then short upper",
        2 to "Lower A",
        3 to "Upper A",
        4 to "Speed",
        5 to "Upper B then easy aerobic",
        6 to "Lower B",
        7 to "Rest",
    )

    const val restPrescription = "Walking and ordinary daily activity only. No lifting and no running."
    const val resistanceWarmup =
        "5–8 min easy movement. Before the first compound perform 3–5 progressive ramp sets; before later compounds perform 1–3 as needed. Ramp sets stay well short of fatigue and do not count as working sets."
    const val speedWarmup =
        "5 min easy walk/jog; ankle rocks 10/side; front-to-back and side-to-side leg swings 10/side each; walking lunge with rotation 8/side; A-march 2 × 20 m; A-skip 2 × 20 m; then 3 × 40 m build-ups at about 60%, 75%, and 85%. Walk back and rest until breathing is controlled; allow about 2 min between build-ups."
    const val speedTestBuildup = "Before a timed test add 1 × 40 m at about 90%."
    const val engineQualityWarmup =
        "10 min easy jog; ankle rocks and leg swings 10/side; 4 × 20 s progressive strides with 60 s easy walking/jogging."
    const val engineEasyWarmup =
        "5 min easy movement and no strides. This opening starts the listed easy run. Do not add strides or an extra cooldown."
    const val cooldown =
        "Walk or jog very easily for 8–10 min. Do not add strides, intervals, or conditioning."
    const val plyometrics =
        "3 × 3 pogo jumps and 3 × 2 standing broad jumps at 70–80% of the best comfortable distance. Rest 60–90 s. Stop for pain or declining landing quality."
    const val testGate =
        "A timed 300 m is permitted only after two pain-free running exposures in the preceding 14 days, including one exposure reaching about 90% of current perceived top speed without altered gait or next-day symptom escalation. If the gate is not met, replace the timed 300 m with 6 × 30 m progressive accelerations at 70–85% and 3 min recovery, and classify the test as MISSING. Do not perform both and do not reschedule the test."
    const val stopRule =
        "Stop the quality portion for altered gait, pain, mechanical breakdown, or a repetition slower than the first by more than 3%. Do not add replacement repetitions."
    const val calibrationNote =
        "RIR calibration is reference guidance only. Every fourth normal week, the final set of one safe upper-body isolation and one safe lower-body isolation may continue to technical failure to compare actual repetitions with the predicted RIR. That set replaces the prescribed final set. It is not an extra set, and this app does not choose the week or add the set."

    fun absoluteDay(week: Int, day: Int): Int {
        require(week in 1..TOTAL_WEEKS)
        require(day in 1..DAYS_PER_WEEK)
        return (week - 1) * DAYS_PER_WEEK + day
    }

    fun blockForWeek(week: Int): ProgramBlock {
        require(week in 1..TOTAL_WEEKS)
        return blocks.single { week in it.startWeek..it.endWeek }
    }

    fun phaseForWeek(week: Int) = when (week) {
        in 1..8 -> 1
        in 9..16 -> 2
        in 17..24 -> 3
        else -> 4
    }

    fun isConsolidation(week: Int) = week in setOf(8, 16, 24)
    fun includesPlyometrics(week: Int) = week in 3..7 || week in 9..15 || week in 17..23 || week in 25..29
    fun omitShortUpper(week: Int) = week in setOf(1, 24, 32)
    fun isEasyOnlyEngine(week: Int) = week in setOf(8, 16)
    fun isTimed300(week: Int) = week in setOf(1, 16, 32)
    fun showsSprintWarning(meters: Int) = meters == 600

    /** Target seconds from a valid external 300 m time. Does not use the 46 s goal as capacity. */
    fun targetSeconds(valid300Seconds: Double, distanceMetres: Double, decimalPercentage: Double): Double {
        require(valid300Seconds > 0.0)
        require(distanceMetres > 0.0)
        require(decimalPercentage > 0.0)
        return valid300Seconds * distanceMetres / 300.0 / decimalPercentage
    }

    fun resistanceForDay(week: Int, day: Int) =
        resistanceSlots.filter { it.phase == phaseForWeek(week) && it.day == day }

    fun plannedSets(week: Int, slot: ResistanceSlot): Int? {
        if (omitShortUpper(week) && slot.day == 1) return null
        if (week == 32) return week32Sets[slot.id]
        if (week in setOf(30, 31)) return realizationSets[slot.id]
        if (isConsolidation(week)) return (slot.workingSets / 2).coerceAtLeast(1)
        return slot.workingSets
    }

    fun plannedDose(week: Int, slot: ResistanceSlot, sets: Int): String {
        if (week == 32) return "$sets ${if (sets == 1) "set" else "sets"} @ 3 RIR"
        val range = slot.dose.substringAfter(" ×").substringBefore(" @")
        if (week in setOf(30, 31)) {
            val rir = if (slot.restGuidance == COMPOUND_REST) "2–3 RIR" else "1–2 RIR"
            return "$sets ×$range @ $rir"
        }
        if (isConsolidation(week)) return "$sets ×$range @ 3–4 RIR"
        return slot.dose
    }

    fun phaseVolume(phase: Int): Map<String, Int> {
        val slots = resistanceSlots.filter { it.phase == phase }
        fun sum(predicate: (ResistanceSlot) -> Boolean) = slots.filter(predicate).sumOf { it.workingSets }
        return mapOf(
            "total" to slots.sumOf { it.workingSets },
            "chest" to sum { it.ownerMuscle == Muscle.CHEST },
            "back" to sum { it.ownerMuscle == Muscle.LATS || it.ownerMuscle == Muscle.UPPER_BACK },
            "quads" to sum { it.ownerMuscle == Muscle.QUADS },
            "hamstrings" to sum { it.ownerMuscle == Muscle.HAMSTRINGS },
            "biceps" to sum { it.ownerMuscle == Muscle.BICEPS },
            "triceps" to sum { it.ownerMuscle == Muscle.TRICEPS },
        )
    }

    private val realizationSets = mapOf(
        "d1_incline" to 1, "d1_fly" to 1, "d1_pushdown" to 2, "d1_curl" to 1, "d1_calf" to 1, "d1_abs" to 1,
        "d2_squat" to 2, "d2_rdl" to 1, "d2_press" to 1, "d2_curl" to 1, "d2_calf" to 2, "d2_abs" to 1,
        "d3_bench" to 2, "d3_row" to 2, "d3_incline" to 1, "d3_pull" to 1, "d3_lateral" to 1, "d3_triceps" to 1, "d3_curl" to 1,
        "d5_ohp" to 1, "d5_pull" to 2, "d5_row" to 1, "d5_rear" to 1, "d5_lateral" to 1, "d5_hammer" to 1,
        "d6_hip" to 1, "d6_bss" to 1, "d6_curl" to 1, "d6_rdl" to 1, "d6_calf" to 2, "d6_abs" to 1,
    )
    private val week32Sets = mapOf(
        "d3_bench" to 2, "d3_row" to 2, "d3_pull" to 1, "d3_lateral" to 1, "d3_triceps" to 1, "d3_curl" to 1,
        "d5_ohp" to 1, "d5_pull" to 2, "d5_row" to 1, "d5_rear" to 1,
    )

    val resistanceSlots: List<ResistanceSlot> = buildList {
        phase1(); phase2(); phase3(); phase4()
    }

    private fun MutableList<ResistanceSlot>.phase1() {
        lift(1, 1, "incline", "Incline DB press", "2 × 10–12 @ 1 RIR", Muscle.CHEST, true, listOf("Smith incline", "Machine incline"))
        lift(1, 1, "fly", "Cable or DB fly", "2 × 12–15 @ 1 RIR", Muscle.CHEST, false, listOf("Pec deck", "Band fly"))
        lift(1, 1, "pushdown", "Rope pushdown", "3 × 10–15 @ 0–1 RIR", Muscle.TRICEPS, false, listOf("Bar pushdown", "Single-arm pushdown"))
        lift(1, 1, "curl", "Cable or DB curl", "2 × 10–15 @ 0–1 RIR", Muscle.BICEPS, false, listOf("Band curl"))
        lift(1, 1, "calf", "Standing calf raise", "2 × 12–15 @ 1 RIR", Muscle.CALVES, false, listOf("Smith calf raise", "Leg-press calf raise"))
        lift(1, 1, "abs", "Ab wheel or cable crunch", "2 × 10–15 @ 1 RIR", Muscle.ABS, false, listOf("Weighted crunch"))
        lift(1, 2, "squat", "Back or front squat", "4 × 6–8 @ 2 RIR", Muscle.QUADS, true, listOf("Safety-bar squat", "Smith squat"))
        lift(1, 2, "rdl", "Romanian deadlift", "3 × 8–10 @ 2 RIR", Muscle.HAMSTRINGS, true, listOf("DB RDL", "Smith RDL"))
        lift(1, 2, "press", "Leg press or hack squat", "3 × 10–12 @ 1–2 RIR", Muscle.QUADS, true, listOf("Smith squat", "Heel-elevated DB squat"))
        lift(1, 2, "curl", "Seated leg curl", "3 × 10–15 @ 1 RIR", Muscle.HAMSTRINGS, false, listOf("Lying leg curl", "Slider curl"))
        lift(1, 2, "calf", "Standing calf raise", "4 × 8–12 @ 1 RIR", Muscle.CALVES, false, listOf("Smith calf raise", "Leg-press calf raise"))
        lift(1, 2, "abs", "Weighted crunch", "3 × 10–15 @ 1 RIR", Muscle.ABS, false, listOf("Cable crunch", "Machine crunch"))
        lift(1, 3, "bench", "Flat barbell bench press", "4 × 6–8 @ 2 RIR", Muscle.CHEST, true, listOf("DB press", "Smith press", "Machine press"))
        lift(1, 3, "row", "Chest-supported row", "4 × 8–10 @ 2 RIR", Muscle.UPPER_BACK, true, listOf("Barbell row", "Cable row", "DB row"))
        lift(1, 3, "incline", "Incline press", "3 × 8–10 @ 1–2 RIR", Muscle.CHEST, true, listOf("DB incline", "Smith incline", "Machine incline"))
        lift(1, 3, "row2", "Cable or DB row", "3 × 10–12 @ 1 RIR", Muscle.LATS, true, listOf("T-bar row", "One-arm row"))
        lift(1, 3, "lateral", "Lateral raise", "3 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, listOf("Cable lateral", "Machine lateral"))
        lift(1, 3, "triceps", "Overhead triceps extension", "3 × 10–15 @ 0–1 RIR", Muscle.TRICEPS, false, listOf("DB extension", "EZ-bar extension", "Cable extension"))
        lift(1, 3, "curl", "Incline DB curl", "3 × 10–15 @ 0–1 RIR", Muscle.BICEPS, false, listOf("Behind-body cable curl"))
        lift(1, 3, "shrug", "Paused shrug", "2 × 10–15 @ 1 RIR", Muscle.TRAPS, false, listOf("DB shrug", "Barbell shrug", "Smith shrug", "Cable shrug"))
        lift(1, 5, "ohp", "Standing overhead press", "3 × 6–8 @ 2 RIR", Muscle.DELTS, true, listOf("Seated DB press", "Smith press"))
        lift(1, 5, "pull", "Lat pulldown or pull-up", "4 × 8–12 @ 1–2 RIR", Muscle.LATS, true, listOf("Assisted pull-up", "Band pulldown"))
        lift(1, 5, "pullover", "DB or cable pullover", "2 × 10–15 @ 1 RIR", Muscle.LATS, false, listOf("Straight-arm pulldown"))
        lift(1, 5, "rear", "Rear-delt fly or face pull", "3 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, listOf("Reverse pec deck"))
        lift(1, 5, "lateral", "Lateral raise", "3 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, listOf("Cable lateral", "Machine lateral"))
        lift(1, 5, "hammer", "Hammer curl", "3 × 10–15 @ 0–1 RIR", Muscle.BICEPS, false, listOf("Rope hammer curl", "Cross-body curl"))
        lift(1, 5, "reverse", "Reverse wrist curl", "2 × 12–20 @ 1 RIR", Muscle.FOREARMS, false, listOf("DB reverse wrist curl", "Cable reverse wrist curl", "Band reverse wrist curl"))
        lift(1, 5, "shrug", "Paused shrug", "2 × 10–15 @ 1 RIR", Muscle.TRAPS, false, listOf("DB shrug", "Barbell shrug", "Smith shrug", "Cable shrug"))
        lift(1, 6, "hip", "Hip thrust", "3 × 8–12 @ 1–2 RIR", Muscle.GLUTES, true, listOf("Barbell hip thrust", "Smith hip thrust", "DB hip thrust"))
        lift(1, 6, "bss", "Bulgarian split squat", "3 × 8–10 @ 1–2 RIR", Muscle.QUADS, true, listOf("Reverse lunge", "Smith split squat"))
        lift(1, 6, "extension", "Leg extension", "3 × 12–15 @ 1 RIR", Muscle.QUADS, false, listOf("Spanish squat", "Reverse Nordic"))
        lift(1, 6, "curl", "Seated or lying leg curl", "4 × 10–15 @ 1 RIR", Muscle.HAMSTRINGS, false, listOf("Slider curl"))
        lift(1, 6, "rdl", "DB Romanian deadlift", "2 × 10–12 @ 2 RIR", Muscle.HAMSTRINGS, true, listOf("Smith RDL", "Cable pull-through"))
        lift(1, 6, "calf", "Seated calf raise", "4 × 10–15 @ 1 RIR", Muscle.CALVES, false, listOf("Bent-knee Smith calf raise", "DB calf raise"))
        lift(1, 6, "abs", "Hanging leg raise", "3 × 10–15 @ 1 RIR", Muscle.ABS, false, listOf("Reverse crunch", "Captain's chair"))
        lift(1, 6, "wrist", "Wrist curl", "2 × 12–20 @ 1 RIR", Muscle.FOREARMS, false, listOf("DB wrist curl", "Cable wrist curl", "Band wrist curl"))
    }

    private fun MutableList<ResistanceSlot>.phase2() {
        lift(2, 1, "incline", "Incline DB press", "2 × 8–12 @ 1 RIR", Muscle.CHEST, true, laterAlts("incline"))
        lift(2, 1, "fly", "Fly", "1 × 12–15 @ 1 RIR", Muscle.CHEST, false, laterAlts("fly"))
        lift(2, 1, "pushdown", "Pushdown", "3 × 10–15 @ 0–1 RIR", Muscle.TRICEPS, false, laterAlts("pushdown"))
        lift(2, 1, "curl", "Curl", "2 × 10–15 @ 0–1 RIR", Muscle.BICEPS, false, laterAlts("curl"))
        lift(2, 1, "calf", "Standing calf raise", "2 × 12–15 @ 1 RIR", Muscle.CALVES, false, laterAlts("calf-straight"))
        lift(2, 1, "abs", "Ab wheel or cable crunch", "2 × 10–15 @ 1 RIR", Muscle.ABS, false, laterAlts("trunk"))
        lift(2, 2, "squat", "Back, front, or safety-bar squat", "5 × 3–5 @ 2 RIR", Muscle.QUADS, true, laterAlts("squat"))
        lift(2, 2, "rdl", "Romanian deadlift", "3 × 5–8 @ 2 RIR", Muscle.HAMSTRINGS, true, laterAlts("rdl"))
        lift(2, 2, "press", "Leg press or hack squat", "3 × 8–10 @ 1–2 RIR", Muscle.QUADS, true, laterAlts("squat-machine"))
        lift(2, 2, "curl", "Seated leg curl", "3 × 8–12 @ 1 RIR", Muscle.HAMSTRINGS, false, laterAlts("curl-leg"))
        lift(2, 2, "calf", "Standing calf raise", "3 × 8–12 @ 1 RIR", Muscle.CALVES, false, laterAlts("calf-straight"))
        lift(2, 2, "abs", "Weighted crunch", "2 × 10–15 @ 1 RIR", Muscle.ABS, false, laterAlts("trunk"))
        lift(2, 3, "bench", "Flat barbell bench press", "5 × 3–5 @ 2 RIR", Muscle.CHEST, true, laterAlts("bench"))
        lift(2, 3, "row", "Chest-supported or barbell row", "4 × 6–8 @ 2 RIR", Muscle.UPPER_BACK, true, laterAlts("row"))
        lift(2, 3, "incline", "Incline press", "3 × 6–10 @ 1–2 RIR", Muscle.CHEST, true, laterAlts("incline"))
        lift(2, 3, "pull", "Lat pulldown or pull-up", "3 × 6–10 @ 1–2 RIR", Muscle.LATS, true, laterAlts("pull"))
        lift(2, 3, "lateral", "Lateral raise", "3 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, laterAlts("lateral"))
        lift(2, 3, "triceps", "Triceps extension", "3 × 8–12 @ 1 RIR", Muscle.TRICEPS, false, laterAlts("triceps"))
        lift(2, 3, "curl", "Incline curl", "3 × 8–12 @ 1 RIR", Muscle.BICEPS, false, laterAlts("curl"))
        lift(2, 3, "shrug", "Paused shrug", "2 × 8–12 @ 1 RIR", Muscle.TRAPS, false, laterAlts("shrug"))
        lift(2, 5, "ohp", "Standing overhead press", "4 × 4–6 @ 2 RIR", Muscle.DELTS, true, laterAlts("ohp"))
        lift(2, 5, "pull", "Weighted pull-up or pulldown", "4 × 5–8 @ 2 RIR", Muscle.LATS, true, laterAlts("pull"))
        lift(2, 5, "row", "Cable or chest-supported row", "3 × 8–10 @ 1–2 RIR", Muscle.UPPER_BACK, true, laterAlts("row"))
        lift(2, 5, "rear", "Rear-delt fly or face pull", "3 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, laterAlts("rear"))
        lift(2, 5, "lateral", "Lateral raise", "2 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, laterAlts("lateral"))
        lift(2, 5, "hammer", "Hammer curl", "3 × 8–12 @ 1 RIR", Muscle.BICEPS, false, laterAlts("hammer"))
        lift(2, 5, "reverse", "Reverse wrist curl", "2 × 12–20 @ 1 RIR", Muscle.FOREARMS, false, laterAlts("wrist-reverse"))
        lift(2, 6, "hip", "Hip thrust", "3 × 6–10 @ 1–2 RIR", Muscle.GLUTES, true, laterAlts("hip"))
        lift(2, 6, "bss", "Bulgarian split squat", "3 × 6–8 @ 1–2 RIR", Muscle.QUADS, true, laterAlts("single"))
        lift(2, 6, "curl", "Leg curl", "4 × 8–12 @ 1 RIR", Muscle.HAMSTRINGS, false, laterAlts("curl-leg"))
        lift(2, 6, "extension", "Leg extension", "3 × 10–15 @ 1 RIR", Muscle.QUADS, false, laterAlts("extension"))
        lift(2, 6, "rdl", "DB Romanian deadlift", "2 × 8–10 @ 2 RIR", Muscle.HAMSTRINGS, true, laterAlts("rdl"))
        lift(2, 6, "calf", "Seated calf raise", "3 × 10–15 @ 1 RIR", Muscle.CALVES, false, laterAlts("calf-bent"))
        lift(2, 6, "abs", "Hanging leg raise", "2 × 10–15 @ 1 RIR", Muscle.ABS, false, laterAlts("trunk"))
        lift(2, 6, "wrist", "Wrist curl", "2 × 12–20 @ 1 RIR", Muscle.FOREARMS, false, laterAlts("wrist"))
    }

    private fun MutableList<ResistanceSlot>.phase3() {
        lift(3, 1, "incline", "Incline DB press", "2 × 8–12 @ 1 RIR", Muscle.CHEST, true, laterAlts("incline"))
        lift(3, 1, "fly", "Fly", "2 × 12–15 @ 1 RIR", Muscle.CHEST, false, laterAlts("fly"))
        lift(3, 1, "pushdown", "Pushdown", "3 × 10–15 @ 0–1 RIR", Muscle.TRICEPS, false, laterAlts("pushdown"))
        lift(3, 1, "curl", "Curl", "2 × 10–15 @ 0–1 RIR", Muscle.BICEPS, false, laterAlts("curl"))
        lift(3, 1, "calf", "Standing calf raise", "2 × 12–15 @ 1 RIR", Muscle.CALVES, false, laterAlts("calf-straight"))
        lift(3, 1, "abs", "Ab wheel or cable crunch", "2 × 10–15 @ 1 RIR", Muscle.ABS, false, laterAlts("trunk"))
        lift(3, 2, "squat", "Squat", "3 × 4–6 @ 2 RIR", Muscle.QUADS, true, laterAlts("squat"))
        lift(3, 2, "rdl", "Romanian deadlift", "2 × 6–8 @ 2 RIR", Muscle.HAMSTRINGS, true, laterAlts("rdl"))
        lift(3, 2, "press", "Leg press or hack squat", "3 × 8–12 @ 1–2 RIR", Muscle.QUADS, true, laterAlts("squat-machine"))
        lift(3, 2, "curl", "Leg curl", "3 × 10–15 @ 1 RIR", Muscle.HAMSTRINGS, false, laterAlts("curl-leg"))
        lift(3, 2, "calf", "Standing calf raise", "3 × 8–12 @ 1 RIR", Muscle.CALVES, false, laterAlts("calf-straight"))
        lift(3, 2, "abs", "Weighted crunch", "2 × 10–15 @ 1 RIR", Muscle.ABS, false, laterAlts("trunk"))
        lift(3, 3, "bench", "Bench press", "3 × 4–6 @ 2 RIR", Muscle.CHEST, true, laterAlts("bench"))
        lift(3, 3, "row", "Chest-supported row", "3 × 6–10 @ 2 RIR", Muscle.UPPER_BACK, true, laterAlts("row"))
        lift(3, 3, "incline", "Incline press", "3 × 8–10 @ 1–2 RIR", Muscle.CHEST, true, laterAlts("incline"))
        lift(3, 3, "pull", "Pulldown or pull-up", "3 × 8–12 @ 1–2 RIR", Muscle.LATS, true, laterAlts("pull"))
        lift(3, 3, "lateral", "Lateral raise", "3 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, laterAlts("lateral"))
        lift(3, 3, "triceps", "Triceps extension", "3 × 10–15 @ 1 RIR", Muscle.TRICEPS, false, laterAlts("triceps"))
        lift(3, 3, "curl", "Curl", "3 × 10–15 @ 1 RIR", Muscle.BICEPS, false, laterAlts("curl"))
        lift(3, 5, "ohp", "Overhead press", "3 × 4–6 @ 2 RIR", Muscle.DELTS, true, laterAlts("ohp"))
        lift(3, 5, "pull", "Pull-up or pulldown", "3 × 6–10 @ 2 RIR", Muscle.LATS, true, laterAlts("pull"))
        lift(3, 5, "row", "Cable or chest-supported row", "3 × 8–12 @ 1–2 RIR", Muscle.UPPER_BACK, true, laterAlts("row"))
        lift(3, 5, "rear", "Rear-delt fly", "3 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, laterAlts("rear"))
        lift(3, 5, "lateral", "Lateral raise", "3 × 12–20 @ 0–1 RIR", Muscle.DELTS, false, laterAlts("lateral"))
        lift(3, 5, "hammer", "Hammer curl", "3 × 10–15 @ 1 RIR", Muscle.BICEPS, false, laterAlts("hammer"))
        lift(3, 5, "shrug", "Paused shrug", "2 × 10–15 @ 1 RIR", Muscle.TRAPS, false, laterAlts("shrug"))
        lift(3, 6, "hip", "Hip thrust", "3 × 8–10 @ 2 RIR", Muscle.GLUTES, true, laterAlts("hip"))
        lift(3, 6, "bss", "Bulgarian split squat", "3 × 8–10 @ 2 RIR", Muscle.QUADS, true, laterAlts("single"))
        lift(3, 6, "extension", "Leg extension", "2 × 10–15 @ 1 RIR", Muscle.QUADS, false, laterAlts("extension"))
        lift(3, 6, "curl", "Leg curl", "3 × 10–15 @ 1 RIR", Muscle.HAMSTRINGS, false, laterAlts("curl-leg"))
        lift(3, 6, "rdl", "DB Romanian deadlift", "2 × 8–10 @ 2 RIR", Muscle.HAMSTRINGS, true, laterAlts("rdl"))
        lift(3, 6, "calf", "Seated calf raise", "3 × 10–15 @ 1 RIR", Muscle.CALVES, false, laterAlts("calf-bent"))
        lift(3, 6, "abs", "Hanging leg raise", "2 × 10–15 @ 1 RIR", Muscle.ABS, false, laterAlts("trunk"))
    }

    private fun MutableList<ResistanceSlot>.phase4() {
        lift(4, 1, "incline", "Incline DB press", "2 × 8–12 @ 1–2 RIR", Muscle.CHEST, true, laterAlts("incline"))
        lift(4, 1, "fly", "Fly", "1 × 12–15 @ 1 RIR", Muscle.CHEST, false, laterAlts("fly"))
        lift(4, 1, "pushdown", "Pushdown", "3 × 10–15 @ 1 RIR", Muscle.TRICEPS, false, laterAlts("pushdown"))
        lift(4, 1, "curl", "Curl", "2 × 10–15 @ 1 RIR", Muscle.BICEPS, false, laterAlts("curl"))
        lift(4, 1, "calf", "Standing calf raise", "2 × 12–15 @ 1–2 RIR", Muscle.CALVES, false, laterAlts("calf-straight"))
        lift(4, 1, "abs", "Ab wheel or cable crunch", "2 × 10–15 @ 1–2 RIR", Muscle.ABS, false, laterAlts("trunk"))
        lift(4, 2, "squat", "Squat", "3 × 4–6 @ 2–3 RIR", Muscle.QUADS, true, laterAlts("squat"))
        lift(4, 2, "rdl", "Romanian deadlift", "2 × 6–8 @ 2–3 RIR", Muscle.HAMSTRINGS, true, laterAlts("rdl"))
        lift(4, 2, "press", "Leg press or hack squat", "2 × 8–10 @ 2 RIR", Muscle.QUADS, true, laterAlts("squat-machine"))
        lift(4, 2, "curl", "Leg curl", "2 × 10–12 @ 1–2 RIR", Muscle.HAMSTRINGS, false, laterAlts("curl-leg"))
        lift(4, 2, "calf", "Standing calf raise", "3 × 8–12 @ 2 RIR", Muscle.CALVES, false, laterAlts("calf-straight"))
        lift(4, 2, "abs", "Weighted crunch", "2 × 10–15 @ 2 RIR", Muscle.ABS, false, laterAlts("trunk"))
        lift(4, 3, "bench", "Bench press", "3 × 4–6 @ 2 RIR", Muscle.CHEST, true, laterAlts("bench"))
        lift(4, 3, "row", "Chest-supported row", "3 × 6–10 @ 2 RIR", Muscle.UPPER_BACK, true, laterAlts("row"))
        lift(4, 3, "incline", "Incline press", "2 × 8–10 @ 2 RIR", Muscle.CHEST, true, laterAlts("incline"))
        lift(4, 3, "pull", "Pulldown or pull-up", "2 × 8–10 @ 2 RIR", Muscle.LATS, true, laterAlts("pull"))
        lift(4, 3, "lateral", "Lateral raise", "2 × 12–20 @ 1 RIR", Muscle.DELTS, false, laterAlts("lateral"))
        lift(4, 3, "triceps", "Triceps extension", "3 × 10–15 @ 1 RIR", Muscle.TRICEPS, false, laterAlts("triceps"))
        lift(4, 3, "curl", "Curl", "2 × 10–15 @ 1 RIR", Muscle.BICEPS, false, laterAlts("curl"))
        lift(4, 5, "ohp", "Overhead press", "2 × 4–6 @ 2 RIR", Muscle.DELTS, true, laterAlts("ohp"))
        lift(4, 5, "pull", "Pull-up or pulldown", "3 × 6–10 @ 2 RIR", Muscle.LATS, true, laterAlts("pull"))
        lift(4, 5, "row", "Cable or chest-supported row", "2 × 8–12 @ 2 RIR", Muscle.UPPER_BACK, true, laterAlts("row"))
        lift(4, 5, "rear", "Rear-delt fly", "3 × 12–20 @ 1 RIR", Muscle.DELTS, false, laterAlts("rear"))
        lift(4, 5, "lateral", "Lateral raise", "2 × 12–20 @ 1 RIR", Muscle.DELTS, false, laterAlts("lateral"))
        lift(4, 5, "hammer", "Hammer curl", "2 × 10–15 @ 1 RIR", Muscle.BICEPS, false, laterAlts("hammer"))
        lift(4, 6, "hip", "Hip thrust", "2 × 8–10 @ 2 RIR", Muscle.GLUTES, true, laterAlts("hip"))
        lift(4, 6, "bss", "Bulgarian split squat", "2 × 8–10 @ 2 RIR", Muscle.QUADS, true, laterAlts("single"))
        lift(4, 6, "extension", "Leg extension", "1 × 10–15 @ 1–2 RIR", Muscle.QUADS, false, laterAlts("extension"))
        lift(4, 6, "curl", "Leg curl", "2 × 10–12 @ 1–2 RIR", Muscle.HAMSTRINGS, false, laterAlts("curl-leg"))
        lift(4, 6, "rdl", "DB Romanian deadlift", "2 × 8–10 @ 2–3 RIR", Muscle.HAMSTRINGS, true, laterAlts("rdl"))
        lift(4, 6, "calf", "Seated calf raise", "3 × 10–15 @ 2 RIR", Muscle.CALVES, false, laterAlts("calf-bent"))
        lift(4, 6, "abs", "Hanging leg raise", "2 × 10–15 @ 2 RIR", Muscle.ABS, false, laterAlts("trunk"))
    }

    private fun laterAlts(kind: String): List<String> = when (kind) {
        "squat" -> listOf("Safety-bar squat", "Smith squat", "Hack squat", "Leg press")
        "squat-machine" -> listOf("Smith squat", "Heel-elevated DB squat")
        "rdl" -> listOf("Barbell RDL", "Smith RDL", "Dumbbell RDL")
        "bench" -> listOf("Dumbbell bench", "Smith press", "Machine chest press")
        "row" -> listOf("Cable row", "T-bar row", "One-arm dumbbell row")
        "incline" -> listOf("Smith incline", "Machine incline")
        "pull" -> listOf("Assisted pull-up", "Lat pulldown", "Band pulldown")
        "ohp" -> listOf("Seated dumbbell press", "Smith press", "Machine shoulder press")
        "hip" -> listOf("Barbell hip thrust", "Smith hip thrust", "Dumbbell hip thrust")
        "single" -> listOf("Reverse lunge", "Smith split squat", "Supported split squat")
        "curl-leg" -> listOf("Seated leg curl", "Lying leg curl", "Slider curl")
        "extension" -> listOf("Spanish squat", "Reverse Nordic")
        "calf-straight" -> listOf("Smith calf raise", "Leg-press calf raise", "Dumbbell calf raise")
        "calf-bent" -> listOf("Bent-knee Smith calf raise", "Dumbbell calf raise")
        "fly" -> listOf("Pec deck", "Band fly")
        "pushdown" -> listOf("Bar pushdown", "Single-arm pushdown")
        "curl" -> listOf("Band curl", "Cable curl")
        "triceps" -> listOf("DB extension", "EZ-bar extension", "Cable extension")
        "lateral" -> listOf("Cable lateral", "Machine lateral")
        "rear" -> listOf("Reverse pec deck", "Face pull")
        "hammer" -> listOf("Rope hammer curl", "Cross-body curl")
        "shrug" -> listOf("DB shrug", "Barbell shrug", "Smith shrug", "Cable shrug")
        "wrist" -> listOf("DB wrist curl", "Cable wrist curl", "Band wrist curl")
        "wrist-reverse" -> listOf("DB reverse wrist curl", "Cable reverse wrist curl", "Band reverse wrist curl")
        "trunk" -> listOf("Cable crunch", "Machine crunch", "Reverse crunch")
        else -> emptyList()
    }

    private fun MutableList<ResistanceSlot>.lift(
        phase: Int,
        day: Int,
        key: String,
        movement: String,
        dose: String,
        owner: Muscle,
        compound: Boolean,
        alternatives: List<String>,
    ) {
        require(alternatives.size <= 4) { movement }
        val sets = dose.substringBefore(" ×").toInt()
        add(
            ResistanceSlot(
                id = "d${day}_$key",
                phase = phase,
                day = day,
                movement = movement,
                dose = dose,
                workingSets = sets,
                ownerMuscle = owner,
                roleMap = roleFor(owner, movement),
                alternatives = alternatives.mapIndexed { index, name -> Alternative(index + 1, name) },
                restGuidance = if (compound) COMPOUND_REST else ISOLATION_REST,
            ),
        )
    }

    private fun roleFor(owner: Muscle, movement: String): Map<Muscle, MuscleRole> {
        val name = movement.lowercase()
        return when (owner) {
            Muscle.QUADS -> linkedMapOf(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST)
            Muscle.HAMSTRINGS -> linkedMapOf(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST)
            Muscle.CHEST -> if (name.contains("fly")) linkedMapOf(Muscle.CHEST to MuscleRole.PRIME)
            else linkedMapOf(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST)
            Muscle.LATS, Muscle.UPPER_BACK -> linkedMapOf(
                owner to MuscleRole.PRIME,
                Muscle.BICEPS to MuscleRole.SYNERGIST,
                (if (owner == Muscle.LATS) Muscle.UPPER_BACK else Muscle.LATS) to MuscleRole.SYNERGIST,
            )
            Muscle.DELTS -> if (name.contains("press")) linkedMapOf(Muscle.DELTS to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST)
            else linkedMapOf(Muscle.DELTS to MuscleRole.PRIME)
            Muscle.BICEPS -> if (name.contains("hammer")) linkedMapOf(Muscle.BICEPS to MuscleRole.PRIME, Muscle.FOREARMS to MuscleRole.SYNERGIST)
            else linkedMapOf(Muscle.BICEPS to MuscleRole.PRIME)
            else -> linkedMapOf(owner to MuscleRole.PRIME)
        }
    }

    val conditioningByWeek: Map<Int, WeekConditioning> = listOf(
        c(1, "Controlled 1.5-mile baseline", "One controlled 1.5-mile time trial", "Timed 300 m baseline", "One timed 300 m if the eligibility gate is met; no jumps", 300, "Bike 20 min easy"),
        c(2, "Threshold", "3 × 6 min threshold; 2 min easy", "Acceleration", "6 × 20 m acceleration; 3 min recovery", 120, "Bike 25 min easy"),
        c(3, "Controlled interval", "5 × 3 min controlled interval; 2 min easy", "Acceleration", "6 × 30 m acceleration; 3–4 min recovery", 180, "Bike 25 min easy"),
        c(4, "Threshold", "20 min continuous threshold", "Build + fly", "4 × (20 m build + 20 m fly); 5 min recovery", 80, "Bike 30 min easy"),
        c(5, "Controlled interval", "6 × 400 m controlled interval; 90 s easy", "Acceleration", "8 × 30 m acceleration; 3–4 min recovery", 240, "Easy run 15 min or bike 25 min"),
        c(6, "Controlled interval", "4 × 1000 m controlled interval; 2 min easy", "Build + fly", "5 × (20 m build + 20 m fly); 5 min recovery", 100, "Easy run 20 min or bike 30 min"),
        c(7, "Threshold", "3 × 1 mile threshold; 1 min easy", "Special endurance", "3 × 150 m at 90%; 5–6 min recovery", 450, "Bike 25 min easy"),
        c(8, "Easy run", "Easy run 25 min; easy-only warm-up", "Relaxed acceleration", "4 × 20 m relaxed acceleration; 3 min recovery; no jumps", 80, "Bike 20 min easy"),
        c(9, "Threshold", "3 × 1 mile threshold; 1 min easy", "Special endurance", "3 × 150 m at 90%; 5–6 min recovery", 450, "Easy run 20 min"),
        c(10, "Controlled interval", "4 × 1000 m controlled interval; 2 min easy", "Acceleration", "6 × 30 m acceleration; 3–4 min recovery", 180, "Bike 30 min easy"),
        c(11, "Threshold", "25 min continuous threshold", "Build + fly", "5 × (20 m build + 20 m fly); 5 min recovery", 100, "Easy run 22 min"),
        c(12, "Controlled interval", "6 × 800 m controlled interval; 2 min easy", "Special endurance", "3 × 200 m at 90–92%; 6–8 min recovery; 600 m warning", 600, "Bike 30 min easy"),
        c(13, "Goal pace", "6 × 400 m at goal pace; 90 s easy", "Acceleration + fly", "4 × 30 m acceleration + 3 × 20 m fly; full recovery", 180, "Easy run 25 min"),
        c(14, "Threshold", "2 × 1 mile threshold; 2 min easy", "Special endurance", "2 × 250 m at 90–93%; 7–8 min recovery", 500, "Bike 30 min easy"),
        c(15, "Controlled interval", "5 × 1000 m controlled interval; 2 min easy", "Acceleration", "6 × 30 m acceleration; full recovery", 180, "Easy run 25 min"),
        c(16, "Easy run", "Easy run 20 min; easy-only warm-up", "Timed 300 m checkpoint", "300 m checkpoint only if the gate is met; no jumps", 300, "Bike 20 min easy"),
        c(17, "Threshold", "25 min continuous threshold", "Build + fly", "5 × (20 m build + 20 m fly); 5 min recovery", 100, "Easy run 25 min"),
        c(18, "Goal pace", "3 × 800 m at goal pace; 3 min easy", "Special endurance", "3 × 200 m at 92–95%; 6–8 min recovery; 600 m warning", 600, "Bike 30 min easy"),
        c(19, "Controlled interval", "6 × 800 m controlled interval; 2 min easy", "Special endurance", "2 × 300 m at 88–92%; 8–10 min recovery; 600 m warning", 600, "Easy run 30 min"),
        c(20, "Goal pace", "4 × 600 m at goal pace; 2.5–3 min easy", "Acceleration + fly", "4 × 30 m acceleration + 3 × 20 m fly; full recovery", 180, "Easy run 30 min"),
        c(21, "Threshold", "20 min continuous threshold", "Special endurance", "2 × 250 m at 93–95%; 8 min recovery", 500, "Bike 30 min easy"),
        c(22, "Controlled interval", "4 × 1000 m controlled interval; 2 min easy", "Special endurance", "3 × 200 m at 93–95%; 7–8 min recovery; 600 m warning", 600, "Easy run 30 min"),
        c(23, "Goal pace", "6 × 400 m at goal pace; 90 s easy", "Special endurance", "2 × 300 m at 90–93%; 8–10 min recovery; 600 m warning", 600, "Easy run 30 min"),
        c(24, "Timed 1.5-mile checkpoint", "Timed 1.5-mile checkpoint; no short upper", "Relaxed acceleration", "4 × 20 m relaxed acceleration; 3 min recovery; no jumps", 80, "Bike 20 min easy"),
        c(25, "Threshold", "20 min continuous threshold", "Build + fly", "4 × (20 m build + 20 m fly); 5 min recovery", 80, "Easy run 25 min"),
        c(26, "Goal pace", "3 × 800 m at goal pace; 3 min easy", "Special endurance", "2 × 250 m at 95%; 8 min recovery", 500, "Easy run 25 min"),
        c(27, "Goal pace", "4 × 600 m at goal pace; 3 min easy", "Special endurance", "2 × 200 m at 95%; 8 min recovery", 400, "Easy run 20 min"),
        c(28, "Goal pace", "4 × 400 m at goal pace; 2 min easy", "Acceleration + fly", "4 × 30 m acceleration + 2 × 20 m fly; full recovery", 160, "Easy run 20 min"),
        c(29, "Goal pace", "3 × 800 m at goal pace; 3 min easy", "Special endurance", "2 × 200 m at 95%; 8 min recovery", 400, "Bike 20 min easy"),
        c(30, "Goal pace", "4 × 400 m at goal pace; 2 min easy", "Acceleration + fly", "4 × 30 m acceleration + 2 × 20 m fly; full recovery; no jumps", 160, "Bike 15–20 min easy"),
        c(31, "Goal pace", "3 × 400 m at goal pace; 2.5 min easy", "Relaxed acceleration", "4 × 20 m relaxed acceleration; full recovery; no jumps", 80, "Bike 15 min very easy"),
        c(32, "Final 1.5-mile", "Final timed 1.5-mile; no short upper", "Final 300 m", "Final timed 300 m only if the gate is met; no jumps", 300, "No Day 5 aerobic; final-test overlay"),
    ).associateBy { it.week }

    fun conditioningForWeek(week: Int) = requireNotNull(conditioningByWeek[week])

    private fun c(
        week: Int,
        engineTitle: String,
        engineRx: String,
        speedTitle: String,
        speedRx: String,
        sprintMeters: Int,
        easyRx: String,
    ): WeekConditioning {
        val engineEfforts = Regex("^(\\d+) ×").find(engineRx)?.groupValues?.get(1)?.toInt() ?: 1
        val speedEfforts = Regex("^(\\d+) ×").find(speedRx)?.groupValues?.get(1)?.toInt() ?: 1
        return WeekConditioning(
            week,
            d1 = listOf(ConditioningSegment("w${week}_d5_easy", "Easy aerobic", easyRx, note = "RPE 2–3. Full-sentence conversation stays comfortable. Do not turn this into tempo work.")),
            speed = listOf(ConditioningSegment("w${week}_d4", speedTitle, speedRx, effortCount = speedEfforts, effortLabel = if (speedEfforts > 1) "REP" else "EFFORT", qualifyingSprintMeters = sprintMeters)),
            engine = listOf(ConditioningSegment("w${week}_d1", engineTitle, engineRx, effortCount = engineEfforts, effortLabel = if (engineEfforts > 1) "REP" else "BLOCK")),
        )
    }
}
