package com.jk.operatorperformance.program

object OperatorProgramV56 {
    const val AUTHORITY_VERSION = "5.6"
    const val AUTHORITY_SHA256 = "c48ae8c6b0585d809758d682abc872510645dca864992e36f837396c328ecf03"

    val canonicalMuscles: List<Muscle> = Muscle.entries.toList()

    val blocks = listOf(
        ProgramBlock("Block 1 — Accumulation", 1, 8, BlockStatus.POPULATED),
        ProgramBlock("Block 2", 9, 16, BlockStatus.UNPOPULATED_NOT_PROGRAMMED),
        ProgramBlock("Block 3", 17, 24, BlockStatus.UNPOPULATED_NOT_PROGRAMMED),
        ProgramBlock("Block 4", 25, 32, BlockStatus.UNPOPULATED_NOT_PROGRAMMED),
    )

    val weeks = (1..8).map(::WeekDefinition)

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

    val day1EasyAerobic = AerobicRow(
        id = "d1_easy_aerobic",
        day = 1,
        title = "Stationary cycle — easy aerobic",
        prescription = "25:00 continuous • 80–90 rpm • RPE 3–4/10 • full-sentence speech. Increase resistance only enough to hold the prescribed cadence at the prescribed effort.",
        alternative = "Air/fan bike — 25:00 continuous at RPE 3–4/10 with full-sentence speech.",
    )

    val machinePowerFinisher = MachinePowerFinisher(
        efforts = 6,
        hardSeconds = 10,
        easySeconds = 80,
        primary = MachineModality.AIR_FAN_BIKE,
        alternative = MachineModality.STATIONARY_CYCLE,
    )

    const val speedWarmupPrescription = "5:00 easy jog on a flat treadmill or track at RPE 2–3; 2 × 20 m A-march with walk-back; 2 × 20 m A-skip with walk-back; then 3 × 40 m progressive build-ups at approximately 60%, 70%, and 80% with 60–90 s walk-back recovery."
    const val engineWarmupPrescription = "5:00 walk at 3.0–3.5 mph; 5:00 easy jog at 5.0–5.5 mph; then 3 × 20 s progressive strides with 40 s easy between strides. Treadmill grade: 0%."
    const val engineCooldownPrescription = "5:00 easy walk/jog at RPE 1–2."
    const val engineAdjustmentRule = "If the first work interval already exceeds RPE 8/10, mechanics deteriorate, or the interval cannot be completed cleanly, reduce the current session by 0.5 mph. Do not increase above the prescribed speed merely because the session feels easy. No pace or result is stored."
    const val speedStopRule = "Stop sprint work if pain changes gait or Achilles/shin symptoms worsen during the session. Hold sprint progression if symptoms persist 48 hours later."
    const val engineStopRule = "Stop running if pain changes gait or Achilles/shin symptoms worsen during the session."

    val conditioningByWeek: List<WeekConditioning> = listOf(
        weekConditioning(
            1,
            speed = listOf(speedMain("w1_accel", "Acceleration", "6 × 30 m at approximately 90% from a controlled start", 6, 180, 180, "REP")),
            engine = listOf(engineMain("w1_engine", "Aerobic-power intervals", "3 × 6:00 at 7.5 mph • RPE 7–8/10", "Measured flat track/outdoor: 3 × 6:00 at approximately 8:00/mi pace • RPE 7–8/10", 3, 120)),
        ),
        weekConditioning(
            2,
            speed = listOf(speedMain("w2_accel", "Acceleration", "6 × 30 m at approximately 95% from a controlled start", 6, 180, 180, "REP")),
            engine = listOf(engineMain("w2_engine", "VO₂ intervals", "4 × 3:00 at 8.5 mph • RPE 8–9/10", "Measured flat track/outdoor: 4 × 3:00 at approximately 7:04/mi pace • RPE 8–9/10", 4, 180)),
        ),
        weekConditioning(
            3,
            speed = listOf(speedMain("w3_maxv", "Maximum velocity", "4 × (20 m controlled build + 20 m fly)", 4, 80, 300, "REP")),
            engine = listOf(engineMain("w3_engine", "Threshold continuous", "20:00 continuous at 7.8 mph • RPE 7–8/10", "Measured flat track/outdoor: 20:00 continuous at approximately 7:42/mi pace • RPE 7–8/10", 1, null)),
        ),
        weekConditioning(
            4,
            speed = listOf(speedMain("w4_maxv", "Maximum velocity", "5 × (20 m controlled build + 20 m fly)", 5, 100, 300, "REP")),
            engine = listOf(engineMain("w4_engine", "VO₂ intervals", "4 × 4:00 at 8.7 mph • RPE 8–9/10", "Measured flat track/outdoor: 4 × 4:00 at approximately 6:54/mi pace • RPE 8–9/10", 4, 180)),
        ),
        weekConditioning(
            5,
            speed = listOf(speedMain("w5_speed_endurance", "Speed endurance", "4 × 100 m at 90–92%", 4, 400, 360, "REP")),
            engine = listOf(engineMain("w5_engine", "1.5-mile pace intervals", "5 × 400 m at 9.0 mph • approximately 1:40 per 400 m", "Measured flat track/outdoor: 5 × 400 m in approximately 1:40 each", 5, 90)),
        ),
        weekConditioning(
            6,
            speed = listOf(speedMain("w6_speed_endurance", "Speed endurance", "3 × 150 m at 90–92%", 3, 450, 480, "REP")),
            engine = listOf(engineMain("w6_engine", "Threshold intervals", "3 × 8:00 at 8.0 mph • RPE 7–8/10", "Measured flat track/outdoor: 3 × 8:00 at approximately 7:30/mi pace • RPE 7–8/10", 3, 120)),
        ),
        weekConditioning(
            7,
            speed = listOf(speedMain("w7_special_endurance", "Special endurance", "2 × 200 m at 90–92%", 2, 400, 600, "REP")),
            engine = listOf(engineMain("w7_engine", "1.5-mile pace intervals", "5 × 3:00 at 9.0 mph • RPE 8–9/10", "Measured flat track/outdoor: 5 × 3:00 at approximately 6:40/mi pace • RPE 8–9/10", 5, 180)),
        ),
        weekConditioning(
            8,
            speed = listOf(
                ConditioningSegment(
                    id = "w8_special_endurance_300",
                    title = "Special endurance — 300 m",
                    prescription = "1 × 300 m at approximately 90%. This is training, not a test; do not time or store a result.",
                    effortCount = 1,
                    effortLabel = "REP",
                    restSeconds = 720,
                    restAfterLastEffort = true,
                    qualifyingSprintMeters = 300,
                    note = speedStopRule,
                ),
                ConditioningSegment(
                    id = "w8_special_endurance_150",
                    title = "Special endurance — 150 m",
                    prescription = "1 × 150 m at approximately 90% after the prescribed 12:00 recovery.",
                    effortCount = 1,
                    effortLabel = "REP",
                    qualifyingSprintMeters = 150,
                    note = speedStopRule,
                ),
            ),
            engine = listOf(engineMain("w8_engine", "1.5-mile pace intervals", "6 × 400 m at 9.0 mph • approximately 1:40 per 400 m", "Measured flat track/outdoor: 6 × 400 m in approximately 1:40 each", 6, 90)),
        ),
    )

    val deloadSpeedSegments = listOf(
        ConditioningSegment(
            id = "d3_deload_warmup",
            title = "Speed warm-up",
            prescription = speedWarmupPrescription,
        ),
        ConditioningSegment(
            id = "d3_deload_relaxed_accel",
            title = "Relaxed acceleration",
            prescription = "4 × 20 m at approximately 80–85%",
            effortCount = 4,
            effortLabel = "REP",
            restSeconds = 120,
            qualifyingSprintMeters = 80,
            note = speedStopRule,
        ),
    )

    val deloadEngineSegments = listOf(
        ConditioningSegment(
            id = "d6_deload_engine",
            title = "Easy engine",
            prescription = "20:00 easy at RPE 3–4/10. Primary: treadmill at 0% grade. Alternative: stationary cycle ergometer.",
            alternative = "Stationary cycle ergometer — 20:00 easy at RPE 3–4/10.",
            note = engineStopRule,
        ),
    )

    val resistanceSlots: List<ResistanceSlot> = listOf(
        slot("d1_squat", 1, "Back squat", "4 × 5–8 @ 2–3 RIR", 4, 4, 180, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), "Safety-bar squat"),
        slot("d1_rdl", 1, "Romanian deadlift", "3 × 8–10 @ 2 RIR", 3, 3, 120, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), "DB Romanian deadlift"),
        slot("d1_leg_press", 1, "Leg press", "3 × 10–12 @ 1–2 RIR", 3, 3, 120, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), "Hack squat"),
        slot("d1_leg_curl", 1, "Seated leg curl", "3 × 10–12 @ 1 RIR", 3, 3, 90, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME), "Lying leg curl"),
        slot("d1_calf", 1, "Standing calf raise machine", "4 × 8–12", 4, 4, 90, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME), "Smith standing calf raise"),
        slot("d1_abs", 1, "Weighted decline crunch", "3 × 10–12 @ 1 RIR", 3, 3, 90, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME), "Cable crunch"),

        slot("d2_bench", 2, "Flat barbell bench press", "4 × 5–8 @ 2–3 RIR", 4, 4, 180, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST), "Flat DB press"),
        slot("d2_row", 2, "Chest-supported row machine", "4 × 8–10 @ 2 RIR", 4, 4, 120, Muscle.UPPER_BACK,
            roles(Muscle.UPPER_BACK to MuscleRole.PRIME, Muscle.LATS to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), "Chest-supported DB row"),
        slot("d2_incline", 2, "Incline barbell press", "3 × 8–10 @ 2 RIR", 3, 3, 120, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST), "Incline DB press"),
        slot("d2_row_2", 2, "Seated cable row", "3 × 10–12 @ 1–2 RIR", 3, 2, 120, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), "One-arm DB row"),
        slot("d2_triceps", 2, "Cable overhead triceps extension", "3 × 10–12 @ 1 RIR", 3, 1, 90, Muscle.TRICEPS,
            roles(Muscle.TRICEPS to MuscleRole.PRIME), "DB overhead triceps extension"),
        slot("d2_curl", 2, "Incline DB curl", "3 × 10–12 @ 1 RIR", 3, 3, 90, Muscle.BICEPS,
            roles(Muscle.BICEPS to MuscleRole.PRIME), "Behind-body cable curl"),
        slot("d2_shrug", 2, "DB shrug, paused/full stretch", "3 × 10–15", 3, 2, 90, Muscle.TRAPS,
            roles(Muscle.TRAPS to MuscleRole.PRIME), "Barbell shrug, paused"),
        slot("d2_abs", 2, "Cable crunch", "2 × 12–15 @ 1 RIR", 2, 0, 90, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME), "Weighted decline crunch"),

        slot("d4_ohp", 4, "Standing barbell overhead press", "4 × 5–8 @ 2–3 RIR", 4, 2, 180, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST), "Seated DB press"),
        slot("d4_pulldown", 4, "Lat pulldown", "4 × 8–12 @ 1–2 RIR", 4, 4, 120, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME, Muscle.BICEPS to MuscleRole.SYNERGIST, Muscle.UPPER_BACK to MuscleRole.SYNERGIST), "Pull-up/chin-up"),
        slot("d4_pullover", 4, "DB pullover", "3 × 10–12 @ 1 RIR", 3, 2, 90, Muscle.LATS,
            roles(Muscle.LATS to MuscleRole.PRIME), "Cable pullover"),
        slot("d4_lateral_raise", 4, "DB lateral raise", "3 × 12–20 @ 0–1 RIR", 3, 0, 90, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME), "Cable lateral raise"),
        slot("d4_rear_delt", 4, "Reverse pec deck", "3 × 15–20", 3, 3, 90, Muscle.DELTS,
            roles(Muscle.DELTS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST), "Cable rear-delt fly"),
        slot("d4_shrug", 4, "Barbell shrug, paused/full stretch", "4 × 10–15", 4, 4, 90, Muscle.TRAPS,
            roles(Muscle.TRAPS to MuscleRole.PRIME), "DB shrug"),
        slot("d4_hammer", 4, "DB hammer curl", "3 × 12–15 @ 0–1 RIR", 3, 1, 90, Muscle.BICEPS,
            roles(Muscle.BICEPS to MuscleRole.PRIME, Muscle.FOREARMS to MuscleRole.SYNERGIST), "Rope hammer curl"),
        slot("d4_reverse_wrist", 4, "Barbell reverse wrist curl", "2 × 15–20", 2, 2, 90, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME), "DB reverse wrist curl"),

        slot("d5_deadlift", 5, "Barbell Romanian deadlift", "4 × 5–8 @ 2–3 RIR", 4, 3, 180, Muscle.HAMSTRINGS,
            roles(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.TRAPS to MuscleRole.SYNERGIST, Muscle.FOREARMS to MuscleRole.SYNERGIST), "Trap-bar deadlift"),
        slot("d5_hip_thrust", 5, "Barbell hip thrust", "3 × 8–12 @ 1–2 RIR", 3, 2, 120, Muscle.GLUTES,
            roles(Muscle.GLUTES to MuscleRole.PRIME, Muscle.HAMSTRINGS to MuscleRole.SYNERGIST), "Smith hip thrust"),
        slot("d5_split_squat", 5, "DB Bulgarian split squat", "3 × 8–10 @ 1–2 RIR", 3, 3, 120, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), "Smith split squat"),
        slot("d5_leg_extension", 5, "Leg extension machine", "3 × 12–15 @ 0–1 RIR", 3, 0, 90, Muscle.QUADS,
            roles(Muscle.QUADS to MuscleRole.PRIME), "Banded leg extension"),
        slot("d5_calf", 5, "Seated calf raise machine", "4 × 12–15", 4, 4, 90, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME), "DB seated calf raise"),
        slot("d5_abs", 5, "Hanging leg raise", "3 × 12–15 @ 1 RIR", 3, 3, 90, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME), "Captain’s-chair raise"),
        slot("d5_wrist", 5, "Barbell wrist curl", "2 × 15–20", 2, 2, 90, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME), "DB wrist curl"),

        slot("d6_incline", 6, "Incline DB press", "3 × 10–12 @ 1 RIR", 3, 3, 120, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST), "Incline barbell press"),
        slot("d6_fly", 6, "Cable fly", "2 × 12–15 @ 0–1 RIR", 2, 0, 90, Muscle.CHEST,
            roles(Muscle.CHEST to MuscleRole.PRIME), "Pec deck"),
        slot("d6_pushdown", 6, "Rope pushdown", "2 × 12–15 @ 0–1 RIR", 2, 2, 90, Muscle.TRICEPS,
            roles(Muscle.TRICEPS to MuscleRole.PRIME), "Straight-bar pushdown"),
        slot("d6_wrist", 6, "Barbell wrist curl", "2 × 15–20", 2, 0, 90, Muscle.FOREARMS,
            roles(Muscle.FOREARMS to MuscleRole.PRIME), "DB wrist curl"),
        slot("d6_calf", 6, "Standing calf raise machine", "2 × 12–15", 2, 0, 90, Muscle.CALVES,
            roles(Muscle.CALVES to MuscleRole.PRIME), "Smith standing calf raise"),
        slot("d6_abs", 6, "Ab wheel", "2 × 10–12 @ 1 RIR", 2, 2, 90, Muscle.ABS,
            roles(Muscle.ABS to MuscleRole.PRIME), "Cable crunch"),
    )

    fun weekOrNull(week: Int): WeekDefinition? = weeks.firstOrNull { it.week == week }
    fun conditioningForWeek(week: Int): WeekConditioning = conditioningByWeek.single { it.week == week }
    fun resistanceForDay(day: Int): List<ResistanceSlot> = resistanceSlots.filter { it.day == day }

    private fun weekConditioning(week: Int, speed: List<ConditioningSegment>, engine: List<ConditioningSegment>) =
        WeekConditioning(week = week, speed = speed, engine = engine)

    private fun speedMain(
        id: String,
        title: String,
        prescription: String,
        effortCount: Int,
        qualifyingMeters: Int,
        restSeconds: Int,
        effortLabel: String,
    ) = ConditioningSegment(
        id = id,
        title = title,
        prescription = prescription,
        effortCount = effortCount,
        effortLabel = effortLabel,
        restSeconds = restSeconds,
        qualifyingSprintMeters = qualifyingMeters,
        note = speedStopRule,
    )

    private fun engineMain(
        id: String,
        title: String,
        prescription: String,
        alternative: String,
        effortCount: Int,
        restSeconds: Int?,
    ) = ConditioningSegment(
        id = id,
        title = title,
        prescription = prescription,
        effortCount = effortCount,
        effortLabel = if (effortCount == 1) "EFFORT" else "INTERVAL",
        restSeconds = restSeconds,
        alternative = alternative,
        note = "$engineAdjustmentRule $engineStopRule",
    )

    private fun roles(vararg entries: Pair<Muscle, MuscleRole>): Map<Muscle, MuscleRole> = linkedMapOf(*entries)

    private fun slot(
        id: String,
        day: Int,
        movement: String,
        dose: String,
        workingSets: Int,
        deloadSets: Int,
        restSeconds: Int,
        ownerMuscle: Muscle,
        roleMap: Map<Muscle, MuscleRole>,
        alternative: String,
    ): ResistanceSlot = ResistanceSlot(
        id = id,
        day = day,
        movement = movement,
        dose = dose,
        workingSets = workingSets,
        deloadSets = deloadSets,
        restSeconds = restSeconds,
        ownerMuscle = ownerMuscle,
        roleMap = roleMap,
        alternatives = listOf(Alternative(1, alternative)),
    )
}
