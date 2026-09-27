package com.jk.operatorperformance.program

/** Immutable, runtime-authoritative ARES Performance v7.1 prescription. */
object AresProgramV71 {
    const val AUTHORITY_VERSION = "7.1"
    const val AUTHORITY_SHA256 = "1d33dbca214b34dd456a971d32bc1c19d59e3a13069393429fc9e2115e3a036c"
    const val PROGRAM_NAME = "ARES PERFORMANCE"
    const val PROGRAM_TAGLINE = "Muscle · Strength · Speed · Engine"
    const val TOTAL_WEEKS = 32
    const val DAYS_PER_WEEK = 7

    val blocks = listOf(
        ProgramBlock("FOUNDATION", "HEPHAESTUS", 1, 8, BlockStatus.POPULATED, "Build the hypertrophy chassis, aerobic base, sprint exposure, and running tolerance."),
        ProgramBlock("DEVELOPMENT", "HERMES", 9, 16, BlockStatus.POPULATED, "Build strength, speed, threshold capacity, and event-specific durability."),
        ProgramBlock("SPECIFIC DEVELOPMENT", "ARES", 17, 24, BlockStatus.POPULATED, "Increase specificity while preserving strength and muscle."),
        ProgramBlock("INTEGRATION / PEAK", "ZEUS", 25, 32, BlockStatus.POPULATED, "Reduce avoidable fatigue and express peak 300 m and 1.5-mile performance."),
    )
    val weeks = (1..TOTAL_WEEKS).map(::WeekDefinition)
    val dayTitles = mapOf(
        1 to "Lower A + easy aerobic", 2 to "Upper A", 3 to "Speed", 4 to "Upper B",
        5 to "Lower B", 6 to "Engine + short upper", 7 to "Rest",
    )

    const val restPrescription = "Walking or easy mobility only. No lifting and no running."
    const val compoundRestGuidance = "Rest ~2–3+ min; extend as needed to preserve reps, RIR, technique, and output."
    const val accessoryRestGuidance = "Rest ~1–2 min; extend if the next prescribed set would materially degrade."
    const val speedWarmupPrescription = "8–12 min easy movement; dynamic ankle, hip, and hamstring preparation; sprint drills; then 3–5 progressive build-ups. Start quality work fresh on a safe, flat surface."
    const val speedStopRule = "Stop if mechanics deteriorate, gait changes, or lower-leg pain appears. Hold progression if Achilles or shin symptoms persist 48 hours later."
    const val engineStopRule = "Stop running if pain changes gait or Achilles or shin symptoms worsen during the session."
    const val easyRunNote = "Easy running is conditional: do not exceed 110% of the longest continuous run completed in the prior 30 days. Use the listed bike option instead when that reference would be exceeded."
    const val testGate = "TIMED 300 M ELIGIBILITY: perform only after two pain-free running exposures in the prior 14 days, including one exposure near 90%. If the gate is not met, use 6 × 30 m at 70–85% with 3 min recovery. Week 1 baseline remains MISSING; do not estimate or store a result."

    private fun roles(vararg pairs: Pair<Muscle, MuscleRole>) = linkedMapOf(*pairs)
    private fun slot(phase: Int, id: String, day: Int, movement: String, dose: String, owner: Muscle, roleMap: Map<Muscle, MuscleRole>, vararg alternatives: String): ResistanceSlot {
        val sets = dose.substringBefore(" ×").toInt()
        return ResistanceSlot(id, phase, day, movement, dose, sets, owner, roleMap,
            alternatives.mapIndexed { index, value -> Alternative(index + 1, value) },
            if (owner in setOf(Muscle.QUADS, Muscle.GLUTES, Muscle.HAMSTRINGS, Muscle.CHEST, Muscle.LATS, Muscle.UPPER_BACK, Muscle.DELTS) && sets >= 3) compoundRestGuidance else accessoryRestGuidance)
    }
    private fun prime(m: Muscle) = roles(m to MuscleRole.PRIME)
    private fun lower(m: Muscle) = when (m) {
        Muscle.QUADS -> roles(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST)
        Muscle.HAMSTRINGS -> roles(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST)
        else -> prime(m)
    }
    private fun press() = roles(Muscle.CHEST to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST, Muscle.DELTS to MuscleRole.SYNERGIST)
    private fun pull(owner: Muscle) = roles(
        owner to MuscleRole.PRIME,
        Muscle.BICEPS to MuscleRole.SYNERGIST,
        (if (owner == Muscle.LATS) Muscle.UPPER_BACK else Muscle.LATS) to MuscleRole.SYNERGIST,
    )

    val resistanceSlots: List<ResistanceSlot> = buildList {
        // Phase 1 — Weeks 1–8.
        add(slot(1,"d1_squat",1,"Back squat or front squat","4 × 6–8 @ 2 RIR",Muscle.QUADS,lower(Muscle.QUADS),"Safety-bar squat","Smith squat","Heel-elevated DB squat"))
        add(slot(1,"d1_rdl",1,"Romanian deadlift","3 × 8–10 @ 2 RIR",Muscle.HAMSTRINGS,lower(Muscle.HAMSTRINGS),"DB RDL","Smith RDL","Good morning"))
        add(slot(1,"d1_press",1,"Leg press or hack squat","3 × 10–12 @ 1–2 RIR",Muscle.QUADS,lower(Muscle.QUADS),"Smith squat","Bulgarian split squat","Goblet squat"))
        add(slot(1,"d1_curl",1,"Seated leg curl","3 × 10–15 @ 1 RIR",Muscle.HAMSTRINGS,prime(Muscle.HAMSTRINGS),"Lying leg curl","Slider leg curl","Nordic eccentric"))
        add(slot(1,"d1_calf",1,"Standing calf raise","4 × 8–12 @ 1 RIR",Muscle.CALVES,prime(Muscle.CALVES),"Smith calf raise","Single-leg calf raise","Leg-press calf raise"))
        add(slot(1,"d1_abs",1,"Weighted decline crunch","3 × 10–15 @ 1 RIR",Muscle.ABS,prime(Muscle.ABS),"Cable crunch","Machine crunch","Weighted sit-up"))
        add(slot(1,"d2_bench",2,"Flat barbell bench press","4 × 6–8 @ 2 RIR",Muscle.CHEST,press(),"Flat DB press","Smith bench","Machine chest press","Weighted push-up"))
        add(slot(1,"d2_row",2,"Chest-supported row","4 × 8–10 @ 2 RIR",Muscle.UPPER_BACK,pull(Muscle.UPPER_BACK),"Seal row","Cable row","One-arm DB row"))
        add(slot(1,"d2_incline",2,"Incline press","3 × 8–10 @ 1–2 RIR",Muscle.CHEST,press(),"Incline DB press","Smith incline press","Incline machine press"))
        add(slot(1,"d2_row2",2,"Cable or DB row","3 × 10–12 @ 1 RIR",Muscle.LATS,pull(Muscle.LATS),"One-arm DB row","Chest-supported row","Band row"))
        add(slot(1,"d2_lateral",2,"Lateral raise","3 × 12–20 @ 0–1 RIR",Muscle.DELTS,prime(Muscle.DELTS),"Cable lateral raise","Machine lateral raise","Band lateral raise"))
        add(slot(1,"d2_triceps",2,"Overhead triceps extension","2 × 10–15 @ 0–1 RIR",Muscle.TRICEPS,prime(Muscle.TRICEPS),"Cable extension","DB extension","Band extension"))
        add(slot(1,"d2_curl",2,"Incline DB curl","2 × 10–15 @ 0–1 RIR",Muscle.BICEPS,prime(Muscle.BICEPS),"Bayesian curl","Standing DB curl","Band curl"))
        add(slot(1,"d2_shrug",2,"Shrug","2 × 10–15 @ 1 RIR",Muscle.TRAPS,prime(Muscle.TRAPS),"DB shrug","Smith shrug","Trap-bar shrug"))
        add(slot(1,"d5_deadlift",5,"Trap-bar deadlift or heavy RDL","3 × 6–8 @ 2–3 RIR",Muscle.HAMSTRINGS,lower(Muscle.HAMSTRINGS),"Barbell RDL","DB RDL","Smith RDL"))
        add(slot(1,"d5_hip",5,"Hip thrust","3 × 8–12 @ 1–2 RIR",Muscle.GLUTES,prime(Muscle.GLUTES),"Smith hip thrust","DB hip thrust","Band hip thrust"))
        add(slot(1,"d5_bss",5,"Bulgarian split squat","3 × 8–10 @ 1–2 RIR",Muscle.QUADS,lower(Muscle.QUADS),"Reverse lunge","Smith split squat","DB split squat"))
        add(slot(1,"d5_extension",5,"Leg extension","3 × 12–15 @ 1 RIR",Muscle.QUADS,prime(Muscle.QUADS),"Banded extension","Reverse Nordic","Spanish squat"))
        add(slot(1,"d5_curl",5,"Leg curl","3 × 10–15 @ 1 RIR",Muscle.HAMSTRINGS,prime(Muscle.HAMSTRINGS),"Seated curl","Lying curl","Slider curl"))
        add(slot(1,"d5_calf",5,"Seated calf raise","4 × 10–15 @ 1 RIR",Muscle.CALVES,prime(Muscle.CALVES),"DB seated calf raise","Bent-knee Smith calf raise"))
        add(slot(1,"d5_abs",5,"Hanging leg raise","3 × 10–15 @ 1 RIR",Muscle.ABS,prime(Muscle.ABS),"Captain's-chair raise","Reverse crunch"))
        add(slot(1,"d5_wrist",5,"Wrist curl","2 × 12–20 @ 1 RIR",Muscle.FOREARMS,prime(Muscle.FOREARMS),"DB wrist curl","Cable wrist curl"))
        add(slot(1,"d4_ohp",4,"Overhead press","3 × 6–8 @ 2 RIR",Muscle.DELTS,roles(Muscle.DELTS to MuscleRole.PRIME,Muscle.TRICEPS to MuscleRole.SYNERGIST),"Seated DB press","Smith press","Z-press"))
        add(slot(1,"d4_pull",4,"Pulldown or pull-up","4 × 8–12 @ 1–2 RIR",Muscle.LATS,pull(Muscle.LATS),"Assisted pull-up","Band pulldown","Chin-up"))
        add(slot(1,"d4_pullover",4,"Pullover","2 × 10–15 @ 1 RIR",Muscle.LATS,prime(Muscle.LATS),"Cable pullover","Straight-arm pulldown","Band pullover"))
        add(slot(1,"d4_rear",4,"Rear delt raise or face pull","3 × 12–20 @ 0–1 RIR",Muscle.DELTS,prime(Muscle.DELTS),"Reverse pec deck","Band face pull","DB rear-delt raise"))
        add(slot(1,"d4_lateral",4,"Lateral raise","3 × 12–20 @ 0–1 RIR",Muscle.DELTS,prime(Muscle.DELTS),"Cable lateral raise","Machine lateral raise"))
        add(slot(1,"d4_hammer",4,"Hammer curl","3 × 10–15 @ 0–1 RIR",Muscle.BICEPS,roles(Muscle.BICEPS to MuscleRole.PRIME,Muscle.FOREARMS to MuscleRole.SYNERGIST),"Rope hammer curl","Cross-body curl"))
        add(slot(1,"d4_reverse",4,"Reverse wrist curl","2 × 12–20 @ 1 RIR",Muscle.FOREARMS,prime(Muscle.FOREARMS),"DB reverse wrist curl","Cable reverse wrist curl"))
        add(slot(1,"d4_shrug",4,"Shrug","2 × 10–15 @ 1 RIR",Muscle.TRAPS,prime(Muscle.TRAPS),"DB shrug","Smith shrug","Trap-bar shrug"))
        addShortUpper(1,"2 × 10–12 @ 1 RIR","2 × 12–15 @ 1 RIR","2 × 10–15 @ 0–1 RIR","2 × 10–15 @ 0–1 RIR","2 × 12–15 @ 1 RIR","2 × 10–15 @ 1 RIR")

        addPhase2(); addPhase3(); addPhase4()
    }

    private fun MutableList<ResistanceSlot>.addShortUpper(phase:Int, incline:String, fly:String, tri:String, curl:String, calf:String, abs:String) {
        add(slot(phase,"d6_incline",6,"Incline DB press",incline,Muscle.CHEST,press(),"Incline barbell press","Smith incline press"))
        add(slot(phase,"d6_fly",6,"Cable or DB fly",fly,Muscle.CHEST,prime(Muscle.CHEST),"Pec deck","Band fly"))
        add(slot(phase,"d6_pushdown",6,"Triceps pushdown",tri,Muscle.TRICEPS,prime(Muscle.TRICEPS),"Band pushdown","DB kickback"))
        add(slot(phase,"d6_curl",6,"Curl",curl,Muscle.BICEPS,prime(Muscle.BICEPS),"DB curl","Cable curl","Band curl"))
        add(slot(phase,"d6_calf",6,"Standing calf raise",calf,Muscle.CALVES,prime(Muscle.CALVES),"Smith calf raise","Single-leg calf raise"))
        add(slot(phase,"d6_abs",6,"Ab wheel or crunch",abs,Muscle.ABS,prime(Muscle.ABS),"Cable crunch","Weighted sit-up"))
    }

    private fun MutableList<ResistanceSlot>.addPhase2() {
        addAll(phaseCore(2, listOf(
            Row(1,"squat","Back squat or front squat","5 × 3–5 @ 2 RIR",Muscle.QUADS), Row(1,"rdl","Romanian deadlift","3 × 5–8 @ 2 RIR",Muscle.HAMSTRINGS), Row(1,"press","Leg press or hack squat","3 × 8–10 @ 1–2 RIR",Muscle.QUADS), Row(1,"curl","Leg curl","3 × 8–12 @ 1 RIR",Muscle.HAMSTRINGS), Row(1,"calf","Standing calf raise","3 × 8–12 @ 1 RIR",Muscle.CALVES), Row(1,"abs","Weighted crunch","2 × 10–15 @ 1 RIR",Muscle.ABS),
            Row(2,"bench","Flat bench press","5 × 3–5 @ 2 RIR",Muscle.CHEST), Row(2,"row","Chest-supported row","4 × 6–8 @ 2 RIR",Muscle.UPPER_BACK), Row(2,"incline","Incline press","3 × 6–10 @ 1–2 RIR",Muscle.CHEST), Row(2,"pull","Vertical pull","3 × 6–10 @ 1–2 RIR",Muscle.LATS), Row(2,"lateral","Lateral raise","3 × 12–20 @ 0–1 RIR",Muscle.DELTS), Row(2,"triceps","Triceps extension","2 × 8–12 @ 1 RIR",Muscle.TRICEPS), Row(2,"curl","Curl","2 × 8–12 @ 1 RIR",Muscle.BICEPS), Row(2,"shrug","Shrug","2 × 8–12 @ 1 RIR",Muscle.TRAPS),
            Row(5,"deadlift","Deadlift or heavy RDL","4 × 3–5 @ 2 RIR",Muscle.HAMSTRINGS), Row(5,"hip","Hip thrust","3 × 6–10 @ 1–2 RIR",Muscle.GLUTES), Row(5,"bss","Bulgarian split squat","3 × 6–8 @ 1–2 RIR",Muscle.QUADS), Row(5,"curl","Leg curl","3 × 8–12 @ 1 RIR",Muscle.HAMSTRINGS), Row(5,"extension","Leg extension","2 × 10–15 @ 1 RIR",Muscle.QUADS), Row(5,"calf","Seated calf raise","3 × 10–15 @ 1 RIR",Muscle.CALVES), Row(5,"abs","Hanging leg raise","2 × 10–15 @ 1 RIR",Muscle.ABS), Row(5,"wrist","Wrist curl","2 × 12–20 @ 1 RIR",Muscle.FOREARMS),
            Row(4,"ohp","Overhead press","4 × 4–6 @ 2 RIR",Muscle.DELTS), Row(4,"pull","Pull-up or pulldown","4 × 5–8 @ 2 RIR",Muscle.LATS), Row(4,"row","Row","3 × 8–10 @ 1–2 RIR",Muscle.UPPER_BACK), Row(4,"rear","Rear delt raise","3 × 12–20 @ 0–1 RIR",Muscle.DELTS), Row(4,"lateral","Lateral raise","2 × 12–20 @ 0–1 RIR",Muscle.DELTS), Row(4,"hammer","Hammer curl","2 × 8–12 @ 1 RIR",Muscle.BICEPS), Row(4,"reverse","Reverse wrist curl","2 × 12–20 @ 1 RIR",Muscle.FOREARMS),
        )))
        addShortUpper(2,"2 × 8–12 @ 1 RIR","1 × 12–15 @ 1 RIR","2 × 10–15 @ 0–1 RIR","2 × 10–15 @ 0–1 RIR","2 × 12–15 @ 1 RIR","2 × 10–15 @ 1 RIR")
    }
    private fun MutableList<ResistanceSlot>.addPhase3() {
        addAll(phaseCore(3, listOf(
            Row(1,"squat","Back squat or front squat","3 × 4–6 @ 2 RIR",Muscle.QUADS),Row(1,"rdl","Romanian deadlift","2 × 6–8 @ 2 RIR",Muscle.HAMSTRINGS),Row(1,"press","Leg press or hack squat","2 × 8–12 @ 1–2 RIR",Muscle.QUADS),Row(1,"curl","Leg curl","2 × 10–15 @ 1 RIR",Muscle.HAMSTRINGS),Row(1,"calf","Standing calf raise","3 × 8–12 @ 1 RIR",Muscle.CALVES),Row(1,"abs","Weighted crunch","2 × 10–15 @ 1 RIR",Muscle.ABS),
            Row(2,"bench","Flat bench press","3 × 4–6 @ 2 RIR",Muscle.CHEST),Row(2,"row","Chest-supported row","3 × 6–10 @ 2 RIR",Muscle.UPPER_BACK),Row(2,"incline","Incline press","2 × 8–10 @ 1–2 RIR",Muscle.CHEST),Row(2,"pull","Vertical pull","2 × 8–12 @ 1–2 RIR",Muscle.LATS),Row(2,"lateral","Lateral raise","2 × 12–20 @ 0–1 RIR",Muscle.DELTS),Row(2,"triceps","Triceps extension","2 × 10–15 @ 1 RIR",Muscle.TRICEPS),Row(2,"curl","Curl","2 × 10–15 @ 1 RIR",Muscle.BICEPS),
            Row(5,"deadlift","Deadlift or heavy RDL","3 × 4–6 @ 2–3 RIR",Muscle.HAMSTRINGS),Row(5,"hip","Hip thrust","2 × 8–10 @ 2 RIR",Muscle.GLUTES),Row(5,"bss","Bulgarian split squat","2 × 8–10 @ 2 RIR",Muscle.QUADS),Row(5,"extension","Leg extension","2 × 10–15 @ 1 RIR",Muscle.QUADS),Row(5,"curl","Leg curl","2 × 10–15 @ 1 RIR",Muscle.HAMSTRINGS),Row(5,"calf","Seated calf raise","3 × 10–15 @ 1 RIR",Muscle.CALVES),Row(5,"abs","Hanging leg raise","2 × 10–15 @ 1 RIR",Muscle.ABS),
            Row(4,"ohp","Overhead press","3 × 4–6 @ 2 RIR",Muscle.DELTS),Row(4,"pull","Pull-up or pulldown","3 × 6–10 @ 2 RIR",Muscle.LATS),Row(4,"row","Row","2 × 8–12 @ 1–2 RIR",Muscle.UPPER_BACK),Row(4,"rear","Rear delt raise","2 × 12–20 @ 0–1 RIR",Muscle.DELTS),Row(4,"lateral","Lateral raise","2 × 12–20 @ 0–1 RIR",Muscle.DELTS),Row(4,"hammer","Hammer curl","2 × 10–15 @ 1 RIR",Muscle.BICEPS),Row(4,"shrug","Shrug","2 × 10–15 @ 1 RIR",Muscle.TRAPS),
        )))
        addShortUpper(3,"2 × 8–12 @ 1 RIR","1 × 12–15 @ 1 RIR","2 × 10–15 @ 1 RIR","2 × 10–15 @ 1 RIR","2 × 12–15 @ 1 RIR","2 × 10–15 @ 1 RIR")
    }
    private fun MutableList<ResistanceSlot>.addPhase4() {
        addAll(phaseCore(4, listOf(
            Row(1,"squat","Back squat or front squat","3 × 4–6 @ 2–3 RIR",Muscle.QUADS),Row(1,"rdl","Romanian deadlift","2 × 6–8 @ 2–3 RIR",Muscle.HAMSTRINGS),Row(1,"press","Leg press or hack squat","2 × 8–10 @ 2 RIR",Muscle.QUADS),Row(1,"curl","Leg curl","2 × 10–12 @ 2 RIR",Muscle.HAMSTRINGS),Row(1,"calf","Standing calf raise","2 × 8–12 @ 2 RIR",Muscle.CALVES),Row(1,"abs","Weighted crunch","2 × 10–15 @ 2 RIR",Muscle.ABS),
            Row(2,"bench","Flat bench press","3 × 4–6 @ 2 RIR",Muscle.CHEST),Row(2,"row","Chest-supported row","3 × 6–10 @ 2 RIR",Muscle.UPPER_BACK),Row(2,"incline","Incline press","2 × 8–10 @ 2 RIR",Muscle.CHEST),Row(2,"pull","Vertical pull","2 × 8–10 @ 2 RIR",Muscle.LATS),Row(2,"lateral","Lateral raise","2 × 12–20 @ 1 RIR",Muscle.DELTS),Row(2,"triceps","Triceps extension","1 × 10–15 @ 1 RIR",Muscle.TRICEPS),Row(2,"curl","Curl","1 × 10–15 @ 1 RIR",Muscle.BICEPS),
            Row(5,"deadlift","Deadlift or heavy RDL","2 × 4–6 @ 3 RIR",Muscle.HAMSTRINGS),Row(5,"hip","Hip thrust","2 × 8–10 @ 2 RIR",Muscle.GLUTES),Row(5,"bss","Bulgarian split squat","2 × 8–10 @ 2 RIR",Muscle.QUADS),Row(5,"extension","Leg extension","1 × 10–15 @ 2 RIR",Muscle.QUADS),Row(5,"curl","Leg curl","2 × 10–12 @ 2 RIR",Muscle.HAMSTRINGS),Row(5,"calf","Seated calf raise","2 × 10–15 @ 2 RIR",Muscle.CALVES),Row(5,"abs","Hanging leg raise","2 × 10–15 @ 2 RIR",Muscle.ABS),
            Row(4,"ohp","Overhead press","2 × 4–6 @ 2 RIR",Muscle.DELTS),Row(4,"pull","Pull-up or pulldown","3 × 6–10 @ 2 RIR",Muscle.LATS),Row(4,"row","Row","2 × 8–12 @ 2 RIR",Muscle.UPPER_BACK),Row(4,"rear","Rear delt raise","2 × 12–20 @ 1 RIR",Muscle.DELTS),Row(4,"lateral","Lateral raise","2 × 12–20 @ 1 RIR",Muscle.DELTS),Row(4,"hammer","Hammer curl","2 × 10–15 @ 1 RIR",Muscle.BICEPS),
        )))
        addShortUpper(4,"2 × 8–12 @ 2 RIR","1 × 12–15 @ 1 RIR","1 × 10–15 @ 1 RIR","1 × 10–15 @ 1 RIR","1 × 12–15 @ 2 RIR","2 × 10–15 @ 2 RIR")
    }
    private data class Row(val day:Int,val key:String,val movement:String,val dose:String,val muscle:Muscle)
    private fun phaseCore(phase:Int, rows:List<Row>) = rows.map { row ->
        val role = when (row.muscle) { Muscle.QUADS,Muscle.HAMSTRINGS -> lower(row.muscle); Muscle.CHEST -> press(); Muscle.LATS,Muscle.UPPER_BACK -> pull(row.muscle); else -> prime(row.muscle) }
        slot(phase,"d${row.day}_${row.key}",row.day,row.movement,row.dose,row.muscle,role)
    }

    fun blockForWeek(week: Int): ProgramBlock {
        require(week in 1..TOTAL_WEEKS)
        return blocks.single { week in it.startWeek..it.endWeek }
    }
    fun phaseForWeek(week:Int) = when (week) { in 1..8 -> 1; in 9..16 -> 2; in 17..24 -> 3; else -> 4 }
    fun resistanceForDay(week:Int, day:Int) = resistanceSlots.filter { it.phase == phaseForWeek(week) && it.day == day }
    fun isConsolidation(week:Int) = week in setOf(8,16,24)
    fun plannedSets(week:Int, slot:ResistanceSlot):Int? {
        if (week == 32) return week32Sets[slot.id]
        if (week in setOf(30,31)) return peakSets[slot.id]
        if (isConsolidation(week)) return (slot.workingSets / 2).coerceAtLeast(1)
        return slot.workingSets
    }
    fun plannedDose(week:Int, slot:ResistanceSlot, sets:Int):String {
        if (week == 32) week32Doses[slot.id]?.let { return it }
        val suffix = slot.dose.substringAfter(" ×")
        val dose = "$sets ×$suffix"
        return if (isConsolidation(week)) dose.replace(Regex("@ .*RIR"), "@ 3–4 RIR") else dose
    }
    private val peakSets = mapOf(
        "d1_squat" to 2,"d1_rdl" to 1,"d1_press" to 1,"d1_curl" to 1,"d1_calf" to 2,"d1_abs" to 1,
        "d2_bench" to 2,"d2_row" to 2,"d2_incline" to 1,"d2_pull" to 1,"d2_lateral" to 1,"d2_triceps" to 1,"d2_curl" to 1,
        "d5_deadlift" to 2,"d5_hip" to 1,"d5_bss" to 1,"d5_curl" to 1,"d5_calf" to 2,"d5_abs" to 1,
        "d4_ohp" to 2,"d4_pull" to 2,"d4_row" to 1,"d4_rear" to 1,"d4_lateral" to 1,"d4_hammer" to 1,
        "d6_incline" to 1,"d6_fly" to 1,"d6_pushdown" to 1,"d6_curl" to 1,"d6_calf" to 1,"d6_abs" to 1,
    )
    private val week32Sets = mapOf("d1_squat" to 2,"d1_rdl" to 1,"d1_curl" to 1,"d1_calf" to 1,"d2_bench" to 2,"d2_row" to 2,"d2_pull" to 1,"d2_lateral" to 1,"d2_triceps" to 1,"d2_curl" to 1,"d4_ohp" to 1,"d4_pull" to 2,"d4_row" to 1,"d4_rear" to 1)
    private val week32Doses = mapOf("d1_squat" to "2 × 4–6 @ 4 RIR","d1_rdl" to "1 × 6–8 @ 4 RIR","d1_curl" to "1 × 10 @ 4 RIR","d1_calf" to "1 × 10 @ 4 RIR")

    val conditioningByWeek: Map<Int,WeekConditioning> = listOf(
        c(1,"Bike 20 min easy","Timed 300 m baseline","One timed 300 m after the prescribed warm-up.","Controlled 1.5-mile baseline","One controlled, strong-but-repeatable 1.5-mile effort."),
        c(2,"Bike 25 min easy","Acceleration","6 × 20 m; 3 min recovery","Threshold","3 × 6 min; 2 min easy"),
        c(3,"Bike 25 min easy","Acceleration","6 × 30 m; 3–4 min recovery","Controlled intervals","5 × 3 min; 2 min easy"),
        c(4,"Bike 30 min easy","Build + fly","4 × (20 m build + 20 m fly); 5 min recovery","Threshold","20 min continuous"),
        c(5,"Easy run 15 min or bike 25 min","Acceleration","8 × 30 m; 3–4 min recovery","Controlled intervals","6 × 400 m; 90 sec easy"),
        c(6,"Easy run 20 min or bike 30 min","Build + fly","5 × (20 m build + 20 m fly); 5 min recovery","Controlled intervals","4 × 1000 m; 2 min easy"),
        c(7,"Bike 25 min easy","Special endurance","3 × 150 m @ ~90%; 5–6 min recovery","Threshold","3 × 1 mile; 1 min easy"),
        c(8,"Bike 20 min very easy","Relaxed acceleration","4 × 20 m; 3 min recovery; no jumps","Easy run","20–25 min easy"),
        c(9,"Easy run 20 min","Special endurance","3 × 150 m @ ~90%; 5–6 min recovery","Threshold","3 × 1 mile; 1 min easy"),
        c(10,"Bike 30 min easy","Acceleration","6 × 30 m; 3–4 min recovery","Controlled intervals","4 × 1000 m; 2 min easy"),
        c(11,"Easy run 22 min","Build + fly","5 × (20 m build + 20 m fly); 5 min recovery","Threshold","25 min continuous"),
        c(12,"Bike 30 min easy","Special endurance","3 × 200 m @ 90–92%; 6–8 min recovery","Controlled intervals","6 × 800 m; 2 min easy"),
        c(13,"Easy run 25 min","Acceleration + fly","4 × 30 m acceleration + 3 × 20 m fly","Goal pace","6 × 400 m; 90 sec easy"),
        c(14,"Bike 30 min easy","Special endurance","2 × 250 m @ 90–93%; 7–8 min recovery","Threshold","2 × 1 mile; 2 min easy"),
        c(15,"Easy run 25 min","Acceleration","6 × 30 m; full recovery","Controlled intervals","5 × 1000 m; 2 min easy"),
        c(16,"Bike 20 min very easy","Timed 300 m checkpoint","One timed 300 m; no jumps","Easy run","20–25 min easy"),
        c(17,"Easy run 25 min","Build + fly","5 × (20 m build + 20 m fly); 5 min recovery","Threshold","25 min continuous"),
        c(18,"Bike 30 min easy","Special endurance","3 × 200 m @ 92–95%; 6–8 min recovery","Goal pace","3 × 800 m; 3 min easy"),
        c(19,"Easy run 30 min","Special endurance","2 × 300 m @ 88–92%; 8–10 min recovery","Controlled intervals","6 × 800 m; 2 min easy"),
        c(20,"Easy run 30 min","Acceleration + fly","4 × 30 m acceleration + 3 × 20 m fly","Goal pace","4 × 600 m; 2.5–3 min easy"),
        c(21,"Bike 30 min easy","Special endurance","2 × 250 m @ 93–95%; 8 min recovery","Threshold","20 min continuous"),
        c(22,"Easy run 30 min","Special endurance","3 × 200 m @ 93–95%; 7–8 min recovery","Controlled intervals","4 × 1000 m; 2 min easy"),
        c(23,"Easy run 30 min","Special endurance","2 × 300 m @ 90–93%; 8–10 min recovery","Goal pace","6 × 400 m; 90 sec easy"),
        c(24,"Bike 20 min very easy","Build + fly","4 × (20 m build + 20 m fly); no jumps","Timed 1.5-mile checkpoint","One timed 1.5-mile run; omit short upper"),
        c(25,"Easy run 25 min","Build + fly","4 × (20 m build + 20 m fly); 5 min recovery","Threshold","20 min continuous"),
        c(26,"Easy run 25 min","Special endurance","2 × 250 m @ 95%; 8 min recovery","Goal pace","3 × 800 m; 3 min easy"),
        c(27,"Easy run 20 min","Special endurance","2 × 200 m @ 95%; 8 min recovery","Goal pace","4 × 600 m; 3 min easy"),
        c(28,"Easy run 20 min","Acceleration + fly","4 × 30 m acceleration + 2 × 20 m fly; no jumps","Goal pace","4 × 400 m; 2 min easy"),
        c(29,"Bike 20 min easy","Special endurance","2 × 200 m @ 95%; 8 min recovery","Goal pace","3 × 800 m; 3 min easy"),
        c(30,"Bike 15–20 min easy","Acceleration + fly","4 × 30 m acceleration + 2 × 20 m fly; no jumps","Goal pace","4 × 400 m; 2 min easy"),
        c(31,"Bike 15 min very easy","Relaxed acceleration","4 × 20 m; full recovery; no jumps","Goal pace","3 × 400 m; 2.5 min easy"),
        c(32,"Bike 15 min very easy","Final 300 m test","One timed 300 m; no jumps","Final 1.5-mile test","One timed 1.5-mile run; omit short upper"),
    ).associateBy { it.week }

    fun conditioningForWeek(week:Int) = requireNotNull(conditioningByWeek[week])
    fun includesPlyometrics(week:Int) = week in (3..7) || week in (9..15) || week in (17..23) || week in (25..29)
    fun omitShortUpper(week:Int) = week in setOf(24,32)
    fun isTimed300(week:Int) = week in setOf(1,16,32)

    private fun c(week:Int,d1:String,speedTitle:String,speedRx:String,engineTitle:String,engineRx:String):WeekConditioning {
        val sprintMeters = Regex("(\\d+) × (\\d+) m").find(speedRx)?.destructured?.let { (n,m) -> n.toInt()*m.toInt() } ?: 0
        val sprintEfforts = Regex("^(\\d+) ×").find(speedRx)?.groupValues?.get(1)?.toInt() ?: 1
        val engineEfforts = Regex("^(\\d+) ×").find(engineRx)?.groupValues?.get(1)?.toInt() ?: 1
        val d1Note = if (d1.startsWith("Easy run")) easyRunNote else "Easy aerobic conditioning only."
        return WeekConditioning(week,
            listOf(ConditioningSegment("w${week}_d1","Easy aerobic",d1,note=d1Note,alternatives=listOf("Stationary bike — same duration at easy effort."))),
            listOf(ConditioningSegment("w${week}_d3",speedTitle,speedRx,effortCount=sprintEfforts,effortLabel=if(sprintEfforts>1)"REP" else "EFFORT",qualifyingSprintMeters=sprintMeters,note=speedStopRule)),
            listOf(ConditioningSegment("w${week}_d6",engineTitle,engineRx,effortCount=engineEfforts,effortLabel=if(engineEfforts>1)"REP" else "BLOCK",note=engineStopRule)))
    }
}
