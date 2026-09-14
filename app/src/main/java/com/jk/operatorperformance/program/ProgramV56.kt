package com.jk.operatorperformance.program

/**
 * Canonical ARES Performance v5.7 source.
 * The historical object name is retained only to minimize migration churn in the existing package.
 */
object OperatorProgramV56 {
    const val AUTHORITY_VERSION = "5.7"
    const val AUTHORITY_SHA256 = "867c468ba616a22185b871c649d8c026be5c1c61b3a7f690edac5b6169fc3d0a"
    const val PROGRAM_NAME = "ARES PERFORMANCE"
    const val PROGRAM_TAGLINE = "Muscle · Strength · Speed · Engine"

    val canonicalMuscles: List<Muscle> = Muscle.entries.toList()

    val blocks = listOf(
        ProgramBlock(
            name = "FOUNDATION",
            godName = "HEPHAESTUS",
            startWeek = 1,
            endWeek = 8,
            status = BlockStatus.POPULATED,
            purpose = "Forge the hypertrophy chassis, aerobic base, sprint exposure, and running tolerance.",
        ),
        ProgramBlock(
            name = "DEVELOPMENT",
            godName = "HERMES",
            startWeek = 9,
            endWeek = 16,
            status = BlockStatus.POPULATED,
            purpose = "Develop acceleration, maximum velocity, threshold capacity, and VO2 power while preserving hypertrophy.",
        ),
        ProgramBlock(
            name = "SPECIFIC DEVELOPMENT",
            godName = "ARES",
            startWeek = 17,
            endWeek = 24,
            status = BlockStatus.POPULATED,
            purpose = "Weaponize capacity with 300 m special endurance and increasingly specific 1.5-mile work.",
        ),
        ProgramBlock(
            name = "INTEGRATION / PEAK",
            godName = "ZEUS",
            startWeek = 25,
            endWeek = 32,
            status = BlockStatus.POPULATED,
            purpose = "Integrate event-specific qualities, reduce avoidable fatigue, and express peak performance.",
        ),
    )

    val weeks = (1..32).map(::WeekDefinition)

    val dayTitles = mapOf(
        1 to "Lower A + easy aerobic",
        2 to "Upper A",
        3 to "Speed",
        4 to "Upper B",
        5 to "Lower B",
        6 to "Engine + short upper",
        7 to "Rest",
    )

    const val restPrescription = "Walking or easy mobility only. No lifting and no running."
    const val compoundRestGuidance = "Rest ~2–3+ min. Extend as needed to preserve prescribed reps, RIR, technique, and output."
    const val accessoryRestGuidance = "Rest ~1–2 min. Extend when shorter recovery would materially degrade the next prescribed set."

    val day1EasyAerobic = AerobicRow(
        id = "d1_easy_aerobic",
        day = 1,
        title = "Easy aerobic",
        prescription = "20–25 min easy. Stationary bike preferred after Lower A; keep the effort conversational and low cost.",
        alternatives = listOf(
            "Rower — 20–25 min easy.",
            "Treadmill easy walk — 20–25 min.",
            "Stairclimber — 20–25 min easy; lower priority when local quad/glute fatigue is high.",
        ),
        note = "Conditioning receives 0 hypertrophy credit.",
    )

    val machinePowerFinisher = MachinePowerFinisher(
        efforts = 6,
        hardSeconds = 10,
        easySeconds = 80,
        hierarchy = listOf(
            MachineModality.AIR_FAN_BIKE,
            MachineModality.STATIONARY_CYCLE,
            MachineModality.ROW_ERG,
            MachineModality.SKI_ERG,
        ),
    )

    const val speedWarmupPrescription = "Dynamic warm-up, then submaximal sprint build-ups. Use a safe flat running surface and arrive at the first quality rep fresh."
    const val speedCooldownPrescription = "Easy walk after the final sprint or optional machine finisher."
    const val engineWarmupPrescription = "Easy walk/jog followed by progressive strides before threshold, VO2, goal-pace, checkpoint, or test running."
    const val engineCooldownPrescription = "Easy walk/jog after the final running effort before the short-upper resistance sequence."
    const val speedStopRule = "Stop quality sprint work if mechanics clearly deteriorate, gait changes, or lower-leg pain appears. Hold sprint progression if Achilles/shin symptoms persist 48 hours later."
    const val engineStopRule = "Stop running if pain changes gait or Achilles/shin symptoms worsen during the session."
    const val easyRunProgressionNote = "Easy run is conditional: proposed continuous distance should not exceed 110% of the longest continuous run completed in the prior 30 days. If it would, use the listed duration on the stationary bike instead."

    val resistanceSlots: List<ResistanceSlot> = listOf(
        slot("d1_squat", 1, "Back squat or front squat", "4 × 5–8 @ 2–3 RIR", 4, 4, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Safety-bar squat", "Smith squat", "Heel-elevated DB squat"),
        slot("d1_rdl", 1, "Romanian deadlift", "3 × 8–10 @ 2 RIR", 3, 3, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), compoundRestGuidance,
            "DB RDL", "Smith RDL", "Good morning"),
        slot("d1_leg_press", 1, "Leg press or hack squat", "3 × 10–12 @ 1–2 RIR", 3, 3, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Smith squat", "Heavy DB split squat", "Heel-elevated goblet squat", "Bulgarian split squat"),
        slot("d1_leg_curl", 1, "Seated leg curl", "3 × 10–12 @ 1 RIR", 3, 3, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME), accessoryRestGuidance,
            "Lying leg curl", "Slider leg curl", "Banded leg curl", "Nordic eccentric"),
        slot("d1_calf", 1, "Standing calf raise", "4 × 8–12", 4, 4, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME), accessoryRestGuidance,
            "Smith standing calf raise", "DB off block", "Single-leg calf raise", "Leg-press calf raise"),
        slot("d1_abs", 1, "Weighted decline crunch", "3 × 10–12 @ 1 RIR", 3, 3, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME), accessoryRestGuidance,
            "Cable crunch", "Machine crunch", "Weighted sit-up", "Banded kneeling crunch"),

        slot("d2_bench", 2, "Flat barbell bench press", "4 × 5–8 @ 2–3 RIR", 4, 4, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Flat DB press", "Smith bench press", "Machine chest press", "Weighted push-up"),
        slot("d2_row", 2, "Chest-supported or barbell row", "4 × 8–10 @ 2 RIR", 4, 4, Muscle.UPPER_BACK,
            roles(Muscle.UPPER_BACK to MuscleRole.PRIME, Muscle.LATS to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Chest-supported DB row", "Seal row", "Cable row", "One-arm DB row"),
        slot("d2_incline", 2, "Incline barbell press", "3 × 8–10 @ 2 RIR", 3, 3, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Incline DB press", "Smith incline press", "Incline machine press", "Weighted feet-elevated push-up"),
        slot("d2_row_2", 2, "Cable row or DB row", "3 × 10–12 @ 1–2 RIR", 3, 2, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), compoundRestGuidance,
            "One-arm DB row", "Chest-supported row", "Band row", "T-bar row"),
        slot("d2_triceps", 2, "Overhead triceps extension", "3 × 10–12 @ 1 RIR", 3, 1, Muscle.TRICEPS,
            roles(Muscle.TRICEPS to MuscleRole.PRIME), accessoryRestGuidance,
            "DB overhead extension", "EZ-bar overhead extension", "Cable overhead extension", "Band overhead extension"),
        slot("d2_curl", 2, "Incline DB curl", "3 × 10–12 @ 1 RIR", 3, 3, Muscle.BICEPS,
            roles(Muscle.BICEPS to MuscleRole.PRIME), accessoryRestGuidance,
            "Behind-body cable curl", "Bayesian cable curl", "Standing DB curl", "Band curl from behind"),
        slot("d2_shrug", 2, "DB shrug, paused/full stretch", "3 × 10–15", 3, 2, Muscle.TRAPS,
            roles(Muscle.TRAPS to MuscleRole.PRIME), accessoryRestGuidance,
            "Barbell shrug", "Smith shrug", "Trap-bar shrug", "Cable shrug"),
        slot("d2_abs", 2, "Cable crunch", "2 × 12–15 @ 1 RIR", 2, 0, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME), accessoryRestGuidance,
            "Weighted decline crunch", "Banded kneeling crunch", "Machine crunch", "Weighted sit-up"),

        slot("d4_ohp", 4, "Standing overhead press", "4 × 5–8 @ 2–3 RIR", 4, 2, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Seated DB press", "Standing DB press", "Smith overhead press", "Z-press"),
        slot("d4_pulldown", 4, "Lat pulldown", "4 × 8–12 @ 1–2 RIR", 4, 4, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME, Muscle.BICEPS to MuscleRole.SYNERGIST, Muscle.UPPER_BACK to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Pull-up/chin-up", "Assisted pull-up", "Band pulldown", "One-arm band pulldown"),
        slot("d4_pullover", 4, "DB pullover", "3 × 10–12 @ 1 RIR", 3, 2, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME), accessoryRestGuidance,
            "Cable pullover", "Straight-arm pulldown", "Band pullover", "Straight-arm band pulldown"),
        slot("d4_lateral", 4, "Lateral raise", "3 × 12–20 @ 0–1 RIR", 3, 0, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME), accessoryRestGuidance,
            "Cable lateral raise", "Machine lateral raise", "Band lateral raise", "Leaning DB lateral raise"),
        slot("d4_rear", 4, "Rear delt or face pull", "3 × 15–20", 3, 3, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST), accessoryRestGuidance,
            "Reverse pec deck", "Cable rear-delt fly", "Band face pull", "DB rear-delt raise"),
        slot("d4_shrug", 4, "Barbell shrug, paused", "4 × 10–15", 4, 4, Muscle.TRAPS,
            roles(Muscle.TRAPS to MuscleRole.PRIME), accessoryRestGuidance,
            "DB shrug", "Smith shrug", "Trap-bar shrug", "Cable shrug"),
        slot("d4_hammer", 4, "Hammer curl", "3 × 12–15 @ 0–1 RIR", 3, 1, Muscle.BICEPS,
            roles(Muscle.BICEPS to MuscleRole.PRIME, Muscle.FOREARMS to MuscleRole.SYNERGIST), accessoryRestGuidance,
            "Cross-body DB hammer curl", "Rope hammer curl", "Band hammer curl", "Neutral-grip cable curl"),
        slot("d4_reverse_wrist", 4, "Reverse wrist curl", "2 × 15–20", 2, 2, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME), accessoryRestGuidance,
            "DB reverse wrist curl", "Barbell reverse wrist curl", "Cable reverse wrist curl", "Band reverse wrist curl"),

        slot("d5_deadlift", 5, "Deadlift variation or heavy RDL", "4 × 5–8 @ 2–3 RIR", 4, 3, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.TRAPS to MuscleRole.SYNERGIST, Muscle.FOREARMS to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Trap-bar deadlift", "Barbell RDL", "DB RDL", "Smith RDL"),
        slot("d5_hip", 5, "Hip thrust", "3 × 8–12 @ 1–2 RIR", 3, 2, Muscle.GLUTES,
            roles(Muscle.GLUTES to MuscleRole.PRIME, Muscle.HAMSTRINGS to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Barbell hip thrust", "Smith hip thrust", "DB hip thrust", "Band hip thrust"),
        slot("d5_bss", 5, "Bulgarian split squat", "3 × 8–10 @ 1–2 RIR", 3, 3, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), compoundRestGuidance,
            "DB Bulgarian split squat", "Barbell split squat", "Smith split squat", "Reverse lunge"),
        slot("d5_extension", 5, "Leg extension", "3 × 12–15 @ 0–1 RIR", 3, 0, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME), accessoryRestGuidance,
            "Banded leg extension", "Sissy squat", "Reverse Nordic", "Spanish squat"),
        slot("d5_calf", 5, "Seated calf raise", "4 × 12–15", 4, 4, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME), accessoryRestGuidance,
            "DB seated calf raise", "Bent-knee Smith calf raise", "Seated calf machine", "Bent-knee single-leg calf raise"),
        slot("d5_abs", 5, "Hanging leg raise", "3 × 12–15 @ 1 RIR", 3, 3, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME), accessoryRestGuidance,
            "Captain's-chair raise", "Reverse crunch", "Weighted reverse crunch", "Ab-strap knee raise"),
        slot("d5_wrist", 5, "Wrist curl", "2 × 15–20", 2, 2, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME), accessoryRestGuidance,
            "DB wrist curl", "Barbell wrist curl", "Cable wrist curl", "Band wrist curl"),

        slot("d6_incline", 6, "Incline DB press", "3 × 10–12 @ 1 RIR", 3, 3, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST), compoundRestGuidance,
            "Incline barbell press", "Smith incline press", "Incline machine press", "Weighted feet-elevated push-up"),
        slot("d6_fly", 6, "Cable or DB fly", "2 × 12–15 @ 0–1 RIR", 2, 0, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME), accessoryRestGuidance,
            "Cable fly", "DB fly", "Pec deck", "Band fly"),
        slot("d6_pushdown", 6, "Rope pushdown", "2 × 12–15 @ 0–1 RIR", 2, 2, Muscle.TRICEPS,
            roles(Muscle.TRICEPS to MuscleRole.PRIME), accessoryRestGuidance,
            "Straight-bar pushdown", "Single-arm cable pushdown", "Band pushdown", "DB kickback"),
        slot("d6_wrist", 6, "Wrist curl", "2 × 15–20", 2, 0, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME), accessoryRestGuidance,
            "DB wrist curl", "Barbell wrist curl", "Cable wrist curl", "Band wrist curl"),
        slot("d6_calf", 6, "Standing calf raise", "2 × 12–15", 2, 0, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME), accessoryRestGuidance,
            "Smith standing calf raise", "DB off block", "Single-leg calf raise", "Leg-press calf raise"),
        slot("d6_abs", 6, "Ab wheel or cable crunch", "2 × 10–12 @ 1 RIR", 2, 2, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME), accessoryRestGuidance,
            "Ab wheel", "Weighted decline crunch", "Banded kneeling crunch", "Weighted sit-up"),
    )

    private val week32SetOverrides = mapOf(
        "d1_squat" to 2, "d1_rdl" to 1, "d1_leg_press" to 1, "d1_leg_curl" to 1, "d1_calf" to 2, "d1_abs" to 1,
        "d5_deadlift" to 2, "d5_hip" to 1, "d5_bss" to 1, "d5_extension" to 1, "d5_calf" to 2, "d5_abs" to 1, "d5_wrist" to 1,
        "d6_incline" to 1, "d6_fly" to 1, "d6_pushdown" to 1, "d6_wrist" to 1, "d6_calf" to 1, "d6_abs" to 1,
    )

    fun week32Sets(slotId: String, normalSets: Int): Int = week32SetOverrides[slotId] ?: normalSets
    fun week32RirFloor(day: Int): Int? = when (day) { 1, 5 -> 3; 2, 4 -> 1; 6 -> 2; else -> null }

    private val fixedConditioningByWeek: Map<Int, WeekConditioning> = listOf(
        fixedWeek(9, bike("w9_d1", "Bike 25 min easy", "25 min easy on the stationary bike."),
            listOf(sprint("w9_accel", "ACCELERATION", "6 × 30 m; full recovery", 6, 180)),
            listOf(engine("w9_threshold", "THRESHOLD", "20 min continuous at current threshold effort")), true),
        fixedWeek(10, bike("w10_d1", "Bike 25 min easy", "25 min easy on the stationary bike."),
            listOf(sprint("w10_maxv", "MAX VELOCITY", "4 × (20 m build + 20 m fly); 5 min recovery", 4, 80, 300)),
            listOf(engine("w10_vo2", "VO2", "4 × 1000 m at current 3K–5K-equivalent effort; 2 min easy", 4, 120)), true),
        fixedWeek(11, bike("w11_d1", "Bike 30 min easy", "30 min easy on the stationary bike."),
            listOf(sprint("w11_accel", "ACCELERATION", "8 × 30 m; full recovery", 8, 240)),
            listOf(engine("w11_threshold", "THRESHOLD", "3 × 1 mile; 1 min easy", 3, 60)), true),
        fixedWeek(12, bike("w12_d1", "Bike 30 min easy", "30 min easy on the stationary bike."),
            listOf(sprint("w12_maxv", "MAX VELOCITY", "5 × (20 m build + 20 m fly); 5 min recovery", 5, 100, 300)),
            listOf(engine("w12_vo2", "VO2", "5 × 800 m; 2 min easy", 5, 120)), true),
        fixedWeek(13, bike("w13_d1", "Bike or rower 30 min easy", "30 min easy on stationary bike or rower."),
            listOf(sprint("w13_special", "SPECIAL ENDURANCE", "3 × 150 m @ ~90–92%; 5–6 min recovery", 3, 450)),
            listOf(engine("w13_threshold", "THRESHOLD", "22–25 min continuous"))),
        fixedWeek(14, bike("w14_d1", "Bike 30 min easy", "30 min easy on the stationary bike."),
            listOf(sprint("w14_accel", "ACCELERATION", "6 × 30 m; full recovery", 6, 180)),
            listOf(engine("w14_vo2", "VO2", "5 × 1000 m at current 3K–5K-equivalent effort; 2 min easy", 5, 120)), true),
        fixedWeek(15, bike("w15_d1", "Bike 30 min easy", "30 min easy on the stationary bike."),
            listOf(sprint("w15_maxv", "MAX VELOCITY", "5 × (20 m build + 20 m fly); 5 min recovery", 5, 100, 300)),
            listOf(engine("w15_threshold", "THRESHOLD", "3 × 1 mile; 1 min easy", 3, 60)), true),
        fixedWeek(16, bike("w16_d1", "Bike 20–25 min easy", "20–25 min easy on the stationary bike."),
            listOf(sprint("w16_checkpoint", "300 M CHECKPOINT", "One timed 300 m after normal sprint build-ups. Result is not stored.", 1, 300)),
            listOf(engine("w16_consolidation", "CONSOLIDATION", "20–25 min easy continuous run"))),

        fixedWeek(17, easyRun("w17_d1", "Easy run 20 min", "20 min easy continuous run."),
            listOf(sprint("w17_accel", "ACCELERATION", "6 × 30 m; full recovery", 6, 180)),
            listOf(engine("w17_vo2", "VO2", "5 × 1000 m; 2 min easy", 5, 120)), true),
        fixedWeek(18, bike("w18_d1", "Bike 30 min easy", "30 min easy on the stationary bike."),
            listOf(sprint("w18_maxv", "MAX VELOCITY", "5 × (20 m build + 20 m fly); 5 min recovery", 5, 100, 300)),
            listOf(engine("w18_threshold", "THRESHOLD", "20–25 min continuous")), true),
        fixedWeek(19, easyRun("w19_d1", "Easy run 20–22 min", "20–22 min easy continuous run."),
            listOf(sprint("w19_special", "SPECIAL ENDURANCE", "3 × 200 m @ ~90–92%; 6–8 min recovery", 3, 600)),
            listOf(engine("w19_threshold", "THRESHOLD", "3 × 1 mile; 1 min easy", 3, 60))),
        fixedWeek(20, bike("w20_d1", "Bike 30 min easy", "30 min easy on the stationary bike."),
            listOf(sprint("w20_maxv", "MAX VELOCITY", "5 × (20 m build + 20 m fly); 5 min recovery", 5, 100, 300)),
            listOf(engine("w20_vo2", "VO2", "6 × 800 m; 2 min easy", 6, 120)), true),
        fixedWeek(21, easyRun("w21_d1", "Easy run 25 min", "25 min easy continuous run."),
            listOf(sprint("w21_special", "SPECIAL ENDURANCE", "2 × 300 m @ ~90%; 8 min recovery", 2, 600, 480)),
            listOf(engine("w21_threshold", "THRESHOLD", "20 min continuous"))),
        fixedWeek(22, bike("w22_d1", "Bike 30 min easy", "30 min easy on the stationary bike."),
            listOf(sprint("w22_accel", "ACCELERATION", "6 × 30 m; full recovery", 6, 180)),
            listOf(engine("w22_vo2", "VO2", "5 × 1000 m; 2 min easy", 5, 120)), true),
        fixedWeek(23, easyRun("w23_d1", "Easy run 25–30 min", "25–30 min easy continuous run."),
            listOf(sprint("w23_special", "SPECIAL ENDURANCE", "2 × 250 m @ ~93–95%; 7–8 min recovery", 2, 500)),
            listOf(engine("w23_specific", "1.5-MILE SPECIFIC", "6 × 400 m at current goal 1.5-mile pace; 90 s easy", 6, 90))),
        fixedWeek(24, bike("w24_d1", "Bike 20–25 min easy", "20–25 min easy on the stationary bike."),
            listOf(sprint("w24_maxv", "MAX-VELOCITY SHARPENER", "4 × (20 m build + 20 m fly); 5 min recovery", 4, 80, 300)),
            listOf(engine("w24_checkpoint", "1.5-MILE CHECKPOINT", "One timed 1.5-mile run. Result is not stored.")), true),

        fixedWeek(25, easyRun("w25_d1", "Easy run 25 min", "25 min easy continuous run."),
            listOf(sprint("w25_maxv", "MAX VELOCITY", "5 × (20 m build + 20 m fly); 5 min recovery", 5, 100, 300)),
            listOf(engine("w25_vo2", "VO2", "5 × 1000 m; 2 min easy", 5, 120)), true),
        fixedWeek(26, easyRun("w26_d1", "Easy run 30 min", "30 min easy continuous run."),
            listOf(sprint("w26_special", "SPECIAL ENDURANCE", "3 × 200 m @ ~92–95%; 6–8 min recovery", 3, 600)),
            listOf(engine("w26_goal", "GOAL PACE", "6 × 400 m at current goal 1.5-mile pace; 90 s easy", 6, 90))),
        fixedWeek(27, easyRun("w27_d1", "Easy run 30 min", "30 min easy continuous run."),
            listOf(
                sprint("w27_accel", "ACCELERATION", "4 × 30 m; full recovery", 4, 120),
                sprint("w27_maxv", "MAX VELOCITY", "3 × (20 m build + 20 m fly); 5 min recovery", 3, 60, 300),
            ),
            listOf(engine("w27_threshold", "THRESHOLD", "20 min continuous")), true),
        fixedWeek(28, easyRun("w28_d1", "Easy run 30 min", "30 min easy continuous run."),
            listOf(sprint("w28_special", "SPECIAL ENDURANCE", "2 × 300 m @ ~90–92%; 8 min recovery", 2, 600, 480)),
            listOf(engine("w28_goal", "GOAL PACE", "3 × 800 m at current goal 1.5-mile pace; 3 min easy", 3, 180))),
        fixedWeek(29, easyRun("w29_d1", "Easy run 25–30 min", "25–30 min easy continuous run."),
            listOf(sprint("w29_special", "SPECIAL ENDURANCE", "2 × 250 m @ ~93–95%; 7–8 min recovery", 2, 500)),
            listOf(engine("w29_vo2", "VO2", "4 × 1000 m at current 3K–5K-equivalent effort; 2 min easy", 4, 120))),
        fixedWeek(30, easyRun("w30_d1", "Easy run 25 min", "25 min easy continuous run."),
            listOf(sprint("w30_maxv", "MAX VELOCITY", "4 × (20 m build + 20 m fly); 5 min recovery", 4, 80, 300)),
            listOf(engine("w30_goal", "GOAL PACE", "4 × 600 m at current goal 1.5-mile pace; 2.5–3 min easy", 4)), true),
        fixedWeek(31, easyRun("w31_d1", "Easy run 20 min", "20 min easy continuous run."),
            listOf(
                sprint("w31_accel", "ACCELERATION SHARPENER", "4 × 30 m; full recovery", 4, 120),
                sprint("w31_maxv", "MAX-VELOCITY SHARPENER", "2 × (20 m build + 20 m fly); 5 min recovery", 2, 40, 300),
            ),
            listOf(engine("w31_goal", "GOAL-PACE SHARPENER", "4 × 400 m at current goal 1.5-mile pace; 2 min easy", 4, 120)), true),
        fixedWeek(32, bike("w32_d1", "Bike 15–20 min very easy", "15–20 min very easy on the stationary bike."),
            listOf(sprint("w32_final300", "FINAL 300 M TEST", "One timed 300 m after normal sprint build-ups. Result is not stored.", 1, 300)),
            listOf(engine("w32_final15", "FINAL 1.5-MILE TEST", "One timed 1.5-mile run. Result is not stored."))),
    ).associateBy { it.week }

    fun blockForWeek(week: Int): ProgramBlock {
        require(week in 1..32)
        return blocks.single { week in it.startWeek..it.endWeek }
    }

    fun weekOrNull(week: Int): WeekDefinition? = weeks.firstOrNull { it.week == week }
    fun resistanceForDay(day: Int): List<ResistanceSlot> = resistanceSlots.filter { it.day == day }

    fun conditioningForWeek(
        week: Int,
        speedVariant: SpeedVariant = SpeedVariant.ACCELERATION,
        engineVariant: EngineVariant = EngineVariant.THRESHOLD,
    ): WeekConditioning {
        require(week in 1..32)
        fixedConditioningByWeek[week]?.let { return it }
        return when (week) {
            1 -> WeekConditioning(
                week = 1,
                d1 = listOf(
                    ConditioningSegment(
                        id = day1EasyAerobic.id,
                        title = day1EasyAerobic.title,
                        prescription = day1EasyAerobic.prescription,
                        alternatives = day1EasyAerobic.alternatives,
                        note = day1EasyAerobic.note,
                    ),
                ),
                speed = listOf(
                    sprint("w1_300_baseline", "300 M BASELINE", "One timed 300 m after submaximal build-ups. Time externally; the app does not store the result.", 1, 300),
                ),
                engine = listOf(
                    engine("w1_15_baseline", "1.5-MILE BASELINE", "One controlled, strong-but-repeatable 1.5-mile effort. Time externally; the app does not store the result."),
                ),
                allowsMachineFinisher = false,
            )
            in 2..8 -> WeekConditioning(
                week = week,
                d1 = listOf(
                    ConditioningSegment(
                        id = "w${week}_d1_easy",
                        title = day1EasyAerobic.title,
                        prescription = day1EasyAerobic.prescription,
                        alternatives = day1EasyAerobic.alternatives,
                        note = day1EasyAerobic.note,
                    ),
                ),
                speed = speedReference(speedVariant),
                engine = engineReference(engineVariant),
                allowsMachineFinisher = speedVariant != SpeedVariant.SPECIAL_ENDURANCE,
            )
            else -> error("Unreachable week")
        }
    }

    fun speedReference(variant: SpeedVariant): List<ConditioningSegment> = when (variant) {
        SpeedVariant.ACCELERATION -> listOf(
            sprint("ref_accel", "ACCELERATION", "6 × 30 m from a controlled start; full recovery", 6, 180),
        )
        SpeedVariant.MAX_VELOCITY -> listOf(
            sprint("ref_maxv", "MAX VELOCITY", "4 × (20 m build + 20 m fly); 5 min recovery", 4, 80, 300),
        )
        SpeedVariant.SPECIAL_ENDURANCE -> listOf(
            ConditioningSegment(
                id = "ref_special",
                title = "SPECIAL ENDURANCE",
                prescription = "Choose one: 3 × 200 m at 90–95% OR 2 × 300 m at ~90%; 6–8 min recovery. Do not perform both options.",
                effortLabel = "BLOCK",
                qualifyingSprintMeters = 600,
                note = speedStopRule,
            ),
        )
    }

    fun engineReference(variant: EngineVariant): List<ConditioningSegment> = when (variant) {
        EngineVariant.THRESHOLD -> listOf(
            ConditioningSegment(
                id = "ref_threshold",
                title = "THRESHOLD",
                prescription = "Choose one: 20 min continuous at current threshold effort OR 3 × 1 mile with 1 min easy recovery.",
                effortLabel = "BLOCK",
                note = engineStopRule,
            ),
        )
        EngineVariant.VO2 -> listOf(
            ConditioningSegment(
                id = "ref_vo2",
                title = "VO2",
                prescription = "PRIMARY: 5 × 1000 m at current 3K–5K-equivalent pace. SECONDARY: 30 s hard / 30 s easy × 16–20 when deliberately selected. Use one option, not both.",
                effortLabel = "BLOCK",
                note = engineStopRule,
            ),
        )
    }

    private fun fixedWeek(
        week: Int,
        d1: ConditioningSegment,
        speed: List<ConditioningSegment>,
        engine: List<ConditioningSegment>,
        allowsFinisher: Boolean = false,
    ) = WeekConditioning(week, listOf(d1), speed, engine, allowsFinisher)

    private fun bike(id: String, title: String, prescription: String) = ConditioningSegment(
        id = id,
        title = title,
        prescription = prescription,
        alternatives = listOf("Rower — same listed duration at easy effort."),
        note = "Easy aerobic conditioning only; 0 hypertrophy credit.",
    )

    private fun easyRun(id: String, title: String, prescription: String) = ConditioningSegment(
        id = id,
        title = title,
        prescription = prescription,
        alternatives = listOf("Stationary bike — same listed duration if the 110% continuous-run reference would be exceeded."),
        note = easyRunProgressionNote,
    )

    private fun sprint(
        id: String,
        title: String,
        prescription: String,
        efforts: Int = 1,
        sprintMeters: Int = 0,
        restSeconds: Int? = null,
    ) = ConditioningSegment(
        id = id,
        title = title,
        prescription = prescription,
        effortCount = efforts,
        effortLabel = if (efforts == 1) "EFFORT" else "REP",
        restSeconds = restSeconds,
        qualifyingSprintMeters = sprintMeters,
        note = speedStopRule,
    )

    private fun engine(
        id: String,
        title: String,
        prescription: String,
        efforts: Int = 1,
        restSeconds: Int? = null,
    ) = ConditioningSegment(
        id = id,
        title = title,
        prescription = prescription,
        effortCount = efforts,
        effortLabel = if (efforts == 1) "BLOCK" else "REP",
        restSeconds = restSeconds,
        note = engineStopRule,
    )

    private fun roles(vararg entries: Pair<Muscle, MuscleRole>): Map<Muscle, MuscleRole> = linkedMapOf(*entries)

    private fun slot(
        id: String,
        day: Int,
        movement: String,
        dose: String,
        workingSets: Int,
        deloadSets: Int,
        ownerMuscle: Muscle,
        roleMap: Map<Muscle, MuscleRole>,
        restGuidance: String,
        vararg alternatives: String,
    ) = ResistanceSlot(
        id = id,
        day = day,
        movement = movement,
        dose = dose,
        workingSets = workingSets,
        deloadSets = deloadSets,
        ownerMuscle = ownerMuscle,
        roleMap = roleMap,
        alternatives = alternatives.mapIndexed { index, name -> Alternative(index + 1, name) },
        restGuidance = restGuidance,
    )
}
