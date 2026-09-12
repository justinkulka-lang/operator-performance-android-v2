package com.jk.operatorperformance.program

object OperatorProgramV54 {
    const val AUTHORITY_VERSION = "5.4"
    const val AUTHORITY_SHA256 = "4b157477b9758bef3b3f2f3f01f047e6ad8aca735faccad6bdd31e4caaf78631"

    val canonicalMuscles: List<Muscle> = Muscle.entries.toList()

    val blocks = listOf(
        ProgramBlock("Block 1 — Accumulation", 1, 8, BlockStatus.POPULATED),
        ProgramBlock("Block 2", 9, 16, BlockStatus.UNPOPULATED_NOT_PROGRAMMED),
        ProgramBlock("Block 3", 17, 24, BlockStatus.UNPOPULATED_NOT_PROGRAMMED),
        ProgramBlock("Block 4", 25, 32, BlockStatus.UNPOPULATED_NOT_PROGRAMMED),
    )

    val weeks = listOf(
        WeekDefinition(1, speedBaseline = true, engineBaseline = true),
        WeekDefinition(2, speedBaseline = false, engineBaseline = false),
        WeekDefinition(3, speedBaseline = false, engineBaseline = false),
        WeekDefinition(4, speedBaseline = false, engineBaseline = false),
        WeekDefinition(5, speedBaseline = false, engineBaseline = false),
        WeekDefinition(6, speedBaseline = false, engineBaseline = false),
        WeekDefinition(7, speedBaseline = false, engineBaseline = false),
        WeekDefinition(8, speedBaseline = false, engineBaseline = false),
    )

    val firstPostBaselineSpeedDefault = SpeedVariant.ACCELERATION
    val firstPostBaselineEngineDefault = EngineVariant.THRESHOLD

    val dayTitles = mapOf(
        1 to "Lower A + easy aerobic",
        2 to "Upper A",
        3 to "Speed",
        4 to "Upper B",
        5 to "Lower B",
        6 to "Engine + short upper",
        7 to "Rest",
    )

    val day1EasyAerobic = AerobicRow(
        id = "d1_easy_aerobic",
        day = 1,
        title = "Low-eccentric aerobic finisher",
        prescription = "20–25 min easy",
        rankedModalities = listOf("Air/fan bike", "Stationary cycle", "Row erg", "Ski erg"),
        additionalOptions = listOf("Incline treadmill/walk"),
    )

    val machinePowerFinisher = MachinePowerFinisher(
        efforts = 6,
        hardSeconds = 10,
        easySeconds = 80,
        rankedModalities = listOf(
            MachineModality.AIR_FAN_BIKE,
            MachineModality.STATIONARY_CYCLE,
            MachineModality.ROW_ERG,
            MachineModality.SKI_ERG,
        ),
    )

    const val speedBaselinePrescription = "Use submaximal build-ups, then one timed 300 m. Result is not stored."
    const val engineBaselinePrescription = "One controlled, strong-but-repeatable 1.5-mile time trial. Result is not stored."
    const val deloadSpeedGuidance = "Keep running easy; remove sprint and special-endurance work."
    const val deloadEngineGuidance = "Keep running easy."

    val speedOptions = listOf(
        SpeedOption(
            variant = SpeedVariant.ACCELERATION,
            prescriptions = listOf("6 × 30 m from a controlled start; full recovery"),
            qualifyingSprintMeters = listOf(180),
            guidance = "After two quality exposures with stable mechanics and no adverse next-morning lower-leg response, the athlete may manually select MAX VELOCITY.",
            includesMachinePowerFinisher = true,
        ),
        SpeedOption(
            variant = SpeedVariant.MAX_VELOCITY,
            prescriptions = listOf("4 × (20 m build + 20 m fly); 5 min recovery"),
            qualifyingSprintMeters = listOf(80),
            guidance = "After two quality exposures with stable mechanics and no adverse next-morning response, the athlete may manually select SPECIAL ENDURANCE.",
            includesMachinePowerFinisher = true,
        ),
        SpeedOption(
            variant = SpeedVariant.SPECIAL_ENDURANCE,
            prescriptions = listOf(
                "3 × 200 m at 90–95%; 6–8 min recovery",
                "2 × 300 m at ~90%; 6–8 min recovery",
            ),
            qualifyingSprintMeters = listOf(600, 600),
            guidance = "After one quality exposure without a cap breach or adverse next-morning response, the athlete may manually return to ACCELERATION.",
            includesMachinePowerFinisher = false,
        ),
    )

    val engineOptions = listOf(
        EngineOption(
            variant = EngineVariant.THRESHOLD,
            prescriptions = listOf(
                "20 min continuous at current threshold effort",
                "3 × 1 mile with 1 min easy recovery",
            ),
            guidance = "First post-baseline default. Repeat or manually switch only when pacing/mechanics and next-morning lower-leg response are acceptable.",
        ),
        EngineOption(
            variant = EngineVariant.VO2,
            prescriptions = listOf(
                "5 × 1000 m at current 3K–5K-equivalent pace",
                "30 s hard / 30 s easy × 16–20",
            ),
            guidance = "Use one option, not both. Repeat or manually switch based on self-assessed session quality and lower-leg response.",
        ),
    )

    val resistanceSlots: List<ResistanceSlot> = listOf(
        slot("d1_squat", 1, "Back squat or front squat", "4 × 5–8 @ 2–3 RIR", 4, 4, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST),
            "Safety-bar squat", "Smith squat", "Heel-elevated DB squat"),
        slot("d1_rdl", 1, "Romanian deadlift", "3 × 8–10 @ 2 RIR", 3, 3, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST),
            "DB RDL", "Smith RDL", "Good morning"),
        slot("d1_leg_press", 1, "Leg press or hack squat", "3 × 10–12 @ 1–2 RIR", 3, 3, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST),
            "Smith squat", "Heavy DB split squat", "Heel-elevated goblet squat", "Bulgarian split squat"),
        slot("d1_leg_curl", 1, "Seated leg curl", "3 × 10–12 @ 1 RIR", 3, 3, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME),
            "Lying leg curl", "Slider leg curl", "Banded leg curl", "Nordic eccentric"),
        slot("d1_calf", 1, "Standing calf raise", "4 × 8–12", 4, 4, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME),
            "Smith standing calf raise", "DB off block", "Single-leg calf raise", "Leg-press calf raise"),
        slot("d1_abs", 1, "Weighted decline crunch", "3 × 10–12 @ 1 RIR", 3, 3, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME),
            "Cable crunch", "Machine crunch", "Weighted sit-up", "Banded kneeling crunch"),

        slot("d2_bench", 2, "Flat barbell bench press", "4 × 5–8 @ 2–3 RIR", 4, 4, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST),
            "Flat DB press", "Smith bench press", "Machine chest press", "Weighted push-up"),
        slot("d2_row", 2, "Chest-supported or barbell row", "4 × 8–10 @ 2 RIR", 4, 4, Muscle.UPPER_BACK,
            roles(Muscle.UPPER_BACK to MuscleRole.PRIME, Muscle.LATS to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST),
            "Chest-supported DB row", "Seal row", "Cable row", "One-arm DB row"),
        slot("d2_incline", 2, "Incline barbell press", "3 × 8–10 @ 2 RIR", 3, 3, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST),
            "Incline DB press", "Smith incline press", "Incline machine press", "Weighted feet-elevated push-up"),
        slot("d2_row_2", 2, "Cable row or DB row", "3 × 10–12 @ 1–2 RIR", 3, 2, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST),
            "One-arm DB row", "Chest-supported row", "Band row", "T-bar row"),
        slot("d2_triceps", 2, "Overhead triceps extension", "3 × 10–12 @ 1 RIR", 3, 1, Muscle.TRICEPS,
            roles(Muscle.TRICEPS to MuscleRole.PRIME),
            "DB overhead extension", "EZ-bar overhead extension", "Cable overhead extension", "Band overhead extension"),
        slot("d2_curl", 2, "Incline DB curl", "3 × 10–12 @ 1 RIR", 3, 3, Muscle.BICEPS,
            roles(Muscle.BICEPS to MuscleRole.PRIME),
            "Behind-body cable curl", "Bayesian cable curl", "Standing DB curl", "Band curl from behind"),
        slot("d2_shrug", 2, "DB shrug, paused/full stretch", "3 × 10–15", 3, 2, Muscle.TRAPS,
            roles(Muscle.TRAPS to MuscleRole.PRIME),
            "Barbell shrug", "Smith shrug", "Trap-bar shrug", "Cable shrug"),
        slot("d2_abs", 2, "Cable crunch", "2 × 12–15 @ 1 RIR", 2, 0, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME),
            "Weighted decline crunch", "Banded kneeling crunch", "Machine crunch", "Weighted sit-up"),

        slot("d4_ohp", 4, "Standing overhead press", "4 × 5–8 @ 2–3 RIR", 4, 2, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST),
            "Seated DB press", "Standing DB press", "Smith overhead press", "Z-press"),
        slot("d4_pulldown", 4, "Lat pulldown", "4 × 8–12 @ 1–2 RIR", 4, 4, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME, Muscle.BICEPS to MuscleRole.SYNERGIST, Muscle.UPPER_BACK to MuscleRole.SYNERGIST),
            "Pull-up/chin-up", "Assisted pull-up", "Band pulldown", "One-arm band pulldown"),
        slot("d4_pullover", 4, "DB pullover", "3 × 10–12 @ 1 RIR", 3, 2, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME),
            "Cable pullover", "Straight-arm pulldown", "Band pullover", "Straight-arm band pulldown"),
        slot("d4_lateral_raise", 4, "Lateral raise", "3 × 12–20 @ 0–1 RIR", 3, 0, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME),
            "Cable lateral raise", "Machine lateral raise", "Band lateral raise", "Leaning DB lateral raise"),
        slot("d4_rear_delt", 4, "Rear delt or face pull", "3 × 15–20", 3, 3, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST),
            "Reverse pec deck", "Cable rear-delt fly", "Band face pull", "DB rear-delt raise"),
        slot("d4_shrug", 4, "Barbell shrug, paused", "4 × 10–15", 4, 4, Muscle.TRAPS,
            roles(Muscle.TRAPS to MuscleRole.PRIME),
            "DB shrug", "Smith shrug", "Trap-bar shrug", "Cable shrug"),
        slot("d4_hammer", 4, "Hammer curl", "3 × 12–15 @ 0–1 RIR", 3, 1, Muscle.BICEPS,
            roles(Muscle.BICEPS to MuscleRole.PRIME, Muscle.FOREARMS to MuscleRole.SYNERGIST),
            "Cross-body DB hammer curl", "Rope hammer curl", "Band hammer curl", "Neutral-grip cable curl"),
        slot("d4_reverse_wrist", 4, "Reverse wrist curl", "2 × 15–20", 2, 2, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME),
            "DB reverse wrist curl", "Barbell reverse wrist curl", "Cable reverse wrist curl", "Band reverse wrist curl"),

        slot("d5_deadlift", 5, "Deadlift variation or heavy RDL", "4 × 5–8 @ 2–3 RIR", 4, 3, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.TRAPS to MuscleRole.SYNERGIST, Muscle.FOREARMS to MuscleRole.SYNERGIST),
            "Trap-bar deadlift", "Barbell RDL", "DB RDL", "Smith RDL"),
        slot("d5_hip_thrust", 5, "Hip thrust", "3 × 8–12 @ 1–2 RIR", 3, 2, Muscle.GLUTES,
            roles(Muscle.GLUTES to MuscleRole.PRIME, Muscle.HAMSTRINGS to MuscleRole.SYNERGIST),
            "Barbell hip thrust", "Smith hip thrust", "DB hip thrust", "Band hip thrust"),
        slot("d5_split_squat", 5, "Bulgarian split squat", "3 × 8–10 @ 1–2 RIR", 3, 3, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST),
            "DB Bulgarian split squat", "Barbell split squat", "Smith split squat", "Reverse lunge"),
        slot("d5_leg_extension", 5, "Leg extension", "3 × 12–15 @ 0–1 RIR", 3, 0, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME),
            "Banded leg extension", "Sissy squat", "Reverse Nordic", "Spanish squat"),
        slot("d5_calf", 5, "Seated calf raise", "4 × 12–15", 4, 4, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME),
            "DB seated calf raise", "Bent-knee Smith calf raise", "Seated calf machine", "Bent-knee single-leg calf raise"),
        slot("d5_abs", 5, "Hanging leg raise", "3 × 12–15 @ 1 RIR", 3, 3, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME),
            "Captain’s-chair raise", "Reverse crunch", "Weighted reverse crunch", "Ab-strap knee raise"),
        slot("d5_wrist", 5, "Wrist curl", "2 × 15–20", 2, 2, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME),
            "DB wrist curl", "Barbell wrist curl", "Cable wrist curl", "Band wrist curl"),

        slot("d6_incline", 6, "Incline DB press", "3 × 10–12 @ 1 RIR", 3, 3, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST),
            "Incline barbell press", "Smith incline press", "Incline machine press", "Weighted feet-elevated push-up"),
        slot("d6_fly", 6, "Cable or DB fly", "2 × 12–15 @ 0–1 RIR", 2, 0, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME),
            "Cable fly", "DB fly", "Pec deck", "Band fly"),
        slot("d6_pushdown", 6, "Rope pushdown", "2 × 12–15 @ 0–1 RIR", 2, 2, Muscle.TRICEPS,
            roles(Muscle.TRICEPS to MuscleRole.PRIME),
            "Straight-bar pushdown", "Single-arm cable pushdown", "Band pushdown", "DB kickback"),
        slot("d6_wrist", 6, "Wrist curl", "2 × 15–20", 2, 0, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME),
            "DB wrist curl", "Barbell wrist curl", "Cable wrist curl", "Band wrist curl"),
        slot("d6_calf", 6, "Standing calf raise", "2 × 12–15", 2, 0, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME),
            "Smith standing calf raise", "DB off block", "Single-leg calf raise", "Leg-press calf raise"),
        slot("d6_abs", 6, "Ab wheel or cable crunch", "2 × 10–12 @ 1 RIR", 2, 2, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME),
            "Ab wheel", "Weighted decline crunch", "Banded kneeling crunch", "Weighted sit-up"),
    )

    fun weekOrNull(week: Int): WeekDefinition? = weeks.firstOrNull { it.week == week }

    fun resistanceForDay(day: Int): List<ResistanceSlot> = resistanceSlots.filter { it.day == day }

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
        vararg alternatives: String,
    ): ResistanceSlot = ResistanceSlot(
        id = id,
        day = day,
        movement = movement,
        dose = dose,
        workingSets = workingSets,
        deloadSets = deloadSets,
        ownerMuscle = ownerMuscle,
        roleMap = roleMap,
        alternatives = alternatives.mapIndexed { index, name -> Alternative(index + 1, name) },
    )
}
