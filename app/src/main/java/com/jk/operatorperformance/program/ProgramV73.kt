package com.jk.operatorperformance.program

/** Approved LEGEND v7.3 allocation. v7.2 remains immutable for historical sessions. */
object ProgramV73 {
    const val AUTHORITY_VERSION = "7.3"
    const val PROGRAM_NAME = "LEGEND"
    const val TOTAL_WEEKS = 32
    const val DAYS_PER_WEEK = 7
    const val TOTAL_DAYS = 224
    val THROW_WEEKS = setOf(3, 4, 5, 6, 10, 11, 13, 15, 17, 20, 25, 28)
    val blocks = ProgramV72.blocks.mapIndexed { index, block ->
        if (index == 2) block.copy(purpose = "Peak hypertrophy: 146 resistance working sets in normal weeks; approved conditioning retained.") else block
    }
    val dayTitles = mapOf(1 to "Engine", 2 to "Lower A", 3 to "Upper A", 4 to "Speed then Upper C", 5 to "Upper B then easy aerobic", 6 to "Lower B", 7 to "Rest")
    fun phaseForWeek(week: Int): Int { require(week in 1..32); return ProgramV72.phaseForWeek(week) }
    fun blockForWeek(week: Int) = blocks.single { week in it.startWeek..it.endWeek }
    fun conditioningForWeek(week: Int) = ProgramV72.conditioningForWeek(week)
    fun isConsolidation(week: Int) = ProgramV72.isConsolidation(week)
    val resistanceSlots: List<ResistanceSlot> = buildList {
        for (phase in 1..4) {
            add(slot(phase, 2, "01", "Back squat", 4, 4, "6–8", "1–2", Muscle.QUADS, mapOf(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), listOf(Alternative(1, "Safety-bar squat"), Alternative(2, "front squat"), Alternative(3, "Smith squat")), "Barbell back/front squat", ProgramV72.COMPOUND_REST))
            add(slot(phase, 2, "02", "Leg press", 3, 4, "10–12", "1–2", Muscle.QUADS, mapOf(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), listOf(Alternative(1, "hack squat"), Alternative(2, "Smith squat"), Alternative(3, "heel-elevated DB squat")), "Heel-elevated DB squat; different loading curve", ProgramV72.COMPOUND_REST))
            add(slot(phase, 2, "03", "Leg extension", 4, 4, "12–15", "≥1", Muscle.QUADS, mapOf(Muscle.QUADS to MuscleRole.PRIME), listOf(Alternative(1, "seated band knee extension"), Alternative(2, "reverse Nordic"), Alternative(3, "Spanish squat")), "Seated band knee extension, hip flexed; different resistance curve", ProgramV72.ISOLATION_REST))
            add(slot(phase, 2, "04", "Seated leg curl", 5, 6, "10–15", "≥1", Muscle.HAMSTRINGS, mapOf(Muscle.HAMSTRINGS to MuscleRole.PRIME), listOf(Alternative(1, "seated band leg curl"), Alternative(2, "lying leg curl"), Alternative(3, "slider curl")), "Seated band leg curl anchored to rack, hips flexed; lying/slider alternatives lose hip-flexed bias", ProgramV72.ISOLATION_REST))
            add(slot(phase, 2, "05", "Glute-biased 45° hip extension", 4, 4, "10–15", "≥1", Muscle.GLUTES, mapOf(Muscle.GLUTES to MuscleRole.PRIME, Muscle.HAMSTRINGS to MuscleRole.SYNERGIST), listOf(Alternative(1, "cable pull-through"), Alternative(2, "glute-biased DB RDL"), Alternative(3, "glute-biased good morning")), "Glute-biased DB RDL; unsupported, different resistance curve", ProgramV72.ISOLATION_REST))
            add(slot(phase, 2, "06", "Standing calf raise", 5, 5, "8–12", "≥1", Muscle.CALVES, mapOf(Muscle.CALVES to MuscleRole.PRIME), listOf(Alternative(1, "Smith calf raise"), Alternative(2, "leg-press calf raise"), Alternative(3, "DB standing calf raise")), "DB standing calf raise off block, rack support", ProgramV72.ISOLATION_REST))
            add(slot(phase, 2, "07", "Weighted crunch", 3, 3, "10–15", "1", Muscle.ABS, mapOf(Muscle.ABS to MuscleRole.PRIME), listOf(Alternative(1, "cable crunch"), Alternative(2, "band crunch"), Alternative(3, "machine crunch")), "Weighted crunch", ProgramV72.ISOLATION_REST))
            add(slot(phase, 3, "08", "Flat barbell bench press", 4, 4, "6–8", "1–2", Muscle.CHEST, mapOf(Muscle.CHEST to MuscleRole.PRIME, Muscle.DELTS to MuscleRole.SYNERGIST, Muscle.TRICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "DB bench"), Alternative(2, "Smith bench"), Alternative(3, "machine chest press")), "Barbell/DB bench", ProgramV72.COMPOUND_REST))
            add(slot(phase, 3, "09", "Chest-supported upper-back row", 4, 4, "8–10", "1–2", Muscle.UPPER_BACK, mapOf(Muscle.UPPER_BACK to MuscleRole.PRIME, Muscle.LATS to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "chest-supported DB row"), Alternative(2, "wide-elbow cable row"), Alternative(3, "barbell row")), "Chest-supported DB row, elbows 45–60°", ProgramV72.COMPOUND_REST))
            add(slot(phase, 3, "10", "Incline barbell press", 4, 4, "8–10", "1–2", Muscle.CHEST, mapOf(Muscle.CHEST to MuscleRole.PRIME, Muscle.DELTS to MuscleRole.SYNERGIST, Muscle.TRICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "incline DB press"), Alternative(2, "Smith incline"), Alternative(3, "machine incline")), "Incline barbell/DB press", ProgramV72.COMPOUND_REST))
            add(slot(phase, 3, "11", "Lat-biased one-arm cable row", 3, 4, "10–12", "1–2", Muscle.LATS, mapOf(Muscle.LATS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "one-arm DB row"), Alternative(2, "tucked-elbow chest-supported row"), Alternative(3, "one-arm band row")), "One-arm DB row, elbow toward hip", ProgramV72.COMPOUND_REST))
            add(slot(phase, 3, "12", "DB lateral raise", 3, 3, "12–20", "0–1", Muscle.DELTS, mapOf(Muscle.DELTS to MuscleRole.PRIME), listOf(Alternative(1, "cable lateral"), Alternative(2, "machine lateral"), Alternative(3, "band lateral")), "DB lateral raise", ProgramV72.ISOLATION_REST))
            add(slot(phase, 3, "13", "Overhead triceps extension", 3, 4, "10–15", "0–1", Muscle.TRICEPS, mapOf(Muscle.TRICEPS to MuscleRole.PRIME), listOf(Alternative(1, "overhead DB extension"), Alternative(2, "overhead EZ-bar extension"), Alternative(3, "overhead band extension")), "Overhead DB/EZ extension; shoulder stays flexed", ProgramV72.ISOLATION_REST))
            add(slot(phase, 3, "14", "Incline DB curl", 4, 4, "10–15", "0–1", Muscle.BICEPS, mapOf(Muscle.BICEPS to MuscleRole.PRIME), listOf(Alternative(1, "behind-body cable curl"), Alternative(2, "behind-body band curl")), "Incline DB curl; shoulder remains extended", ProgramV72.ISOLATION_REST))
            add(slot(phase, 3, "15", "Paused DB shrug", 3, 3, "10–15", "0–1", Muscle.TRAPS, mapOf(Muscle.TRAPS to MuscleRole.PRIME), listOf(Alternative(1, "barbell shrug"), Alternative(2, "Smith shrug"), Alternative(3, "cable shrug")), "DB/barbell shrug", ProgramV72.ISOLATION_REST))
            add(slot(phase, 4, "16", "Cable fly", 4, 4, "12–15", "0–1", Muscle.CHEST, mapOf(Muscle.CHEST to MuscleRole.PRIME), listOf(Alternative(1, "DB fly"), Alternative(2, "pec deck"), Alternative(3, "band fly")), "DB fly, controlled pain-free stretch; different loading curve", ProgramV72.ISOLATION_REST))
            add(slot(phase, 4, "17", "DB pullover", 2, 2, "10–15", "0–1", Muscle.LATS, mapOf(Muscle.LATS to MuscleRole.PRIME), listOf(Alternative(1, "cable pullover"), Alternative(2, "straight-arm pulldown"), Alternative(3, "band pullover")), "DB pullover; cable/pulldown loading curves differ", ProgramV72.ISOLATION_REST))
            add(slot(phase, 4, "18", "Chest-supported upper-back row", 3, 3, "10–12", "1–2", Muscle.UPPER_BACK, mapOf(Muscle.UPPER_BACK to MuscleRole.PRIME, Muscle.LATS to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "chest-supported DB row"), Alternative(2, "wide-elbow cable row"), Alternative(3, "supported band row")), "Chest-supported DB row, elbows 45–60°", ProgramV72.COMPOUND_REST))
            add(slot(phase, 4, "19", "DB lateral raise", 2, 3, "12–20", "0–1", Muscle.DELTS, mapOf(Muscle.DELTS to MuscleRole.PRIME), listOf(Alternative(1, "cable lateral"), Alternative(2, "machine lateral"), Alternative(3, "band lateral")), "DB lateral raise", ProgramV72.ISOLATION_REST))
            add(slot(phase, 4, "20", "Chest-supported rear-delt fly", 3, 3, "12–20", "0–1", Muscle.DELTS, mapOf(Muscle.DELTS to MuscleRole.PRIME), listOf(Alternative(1, "reverse pec deck"), Alternative(2, "cable rear-delt fly"), Alternative(3, "band rear-delt fly")), "Chest-supported DB rear-delt fly", ProgramV72.ISOLATION_REST))
            add(slot(phase, 4, "21", "Incline DB curl", 2, 3, "10–15", "0–1", Muscle.BICEPS, mapOf(Muscle.BICEPS to MuscleRole.PRIME), listOf(Alternative(1, "behind-body cable curl"), Alternative(2, "behind-body band curl")), "Incline DB curl; shoulder remains extended", ProgramV72.ISOLATION_REST))
            add(slot(phase, 4, "22", "Overhead triceps extension", 4, 4, "10–15", "0–1", Muscle.TRICEPS, mapOf(Muscle.TRICEPS to MuscleRole.PRIME), listOf(Alternative(1, "overhead DB extension"), Alternative(2, "overhead EZ-bar extension"), Alternative(3, "overhead band extension")), "Overhead DB/EZ extension; shoulder stays flexed", ProgramV72.ISOLATION_REST))
            add(slot(phase, 4, "23", "DB wrist curl", 2, 2, "12–20", "0–1", Muscle.FOREARMS, mapOf(Muscle.FOREARMS to MuscleRole.PRIME), listOf(Alternative(1, "barbell wrist curl"), Alternative(2, "cable wrist curl"), Alternative(3, "band wrist curl")), "Supported DB/barbell wrist curl", ProgramV72.ISOLATION_REST))
            add(slot(phase, 4, "24", "Cable crunch", 2, 2, "10–15", "1", Muscle.ABS, mapOf(Muscle.ABS to MuscleRole.PRIME), listOf(Alternative(1, "weighted crunch"), Alternative(2, "band crunch")), "Weighted/band crunch; resistance curves differ", ProgramV72.ISOLATION_REST))
            add(slot(phase, 5, "25", "Standing overhead press", 3, 3, "6–8", "1–2", Muscle.DELTS, mapOf(Muscle.DELTS to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "seated DB press"), Alternative(2, "standing DB press"), Alternative(3, "Smith overhead press")), "Barbell/DB overhead press", ProgramV72.COMPOUND_REST))
            add(slot(phase, 5, "26", "Lat pulldown", 5, 6, "8–12", "1–2", Muscle.LATS, mapOf(Muscle.LATS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "pull-up"), Alternative(2, "band-assisted pull-up"), Alternative(3, "seated band pulldown")), "Rack pull-up or band-assisted pull-up; overhead stretch retained", ProgramV72.COMPOUND_REST))
            add(slot(phase, 5, "27", "Chest-supported upper-back row", 3, 3, "8–12", "1–2", Muscle.UPPER_BACK, mapOf(Muscle.UPPER_BACK to MuscleRole.PRIME, Muscle.LATS to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "chest-supported DB row"), Alternative(2, "wide-elbow cable row"), Alternative(3, "T-bar row")), "Chest-supported DB row, elbows 45–60°", ProgramV72.COMPOUND_REST))
            add(slot(phase, 5, "28", "Incline DB press", 3, 4, "10–12", "1–2", Muscle.CHEST, mapOf(Muscle.CHEST to MuscleRole.PRIME, Muscle.DELTS to MuscleRole.SYNERGIST, Muscle.TRICEPS to MuscleRole.SYNERGIST), listOf(Alternative(1, "incline barbell press"), Alternative(2, "Smith incline"), Alternative(3, "machine incline")), "Incline DB/barbell press", ProgramV72.COMPOUND_REST))
            add(slot(phase, 5, "29", "DB lateral raise", 2, 2, "12–20", "0–1", Muscle.DELTS, mapOf(Muscle.DELTS to MuscleRole.PRIME), listOf(Alternative(1, "cable lateral"), Alternative(2, "machine lateral"), Alternative(3, "band lateral")), "DB lateral raise", ProgramV72.ISOLATION_REST))
            add(slot(phase, 5, "30", "Chest-supported rear-delt fly", 2, 2, "12–20", "0–1", Muscle.DELTS, mapOf(Muscle.DELTS to MuscleRole.PRIME), listOf(Alternative(1, "reverse pec deck"), Alternative(2, "cable rear-delt fly"), Alternative(3, "band rear-delt fly")), "Chest-supported DB rear-delt fly", ProgramV72.ISOLATION_REST))
            add(slot(phase, 5, "31", "DB hammer curl", 3, 3, "10–15", "0–1", Muscle.BICEPS, mapOf(Muscle.BICEPS to MuscleRole.PRIME, Muscle.FOREARMS to MuscleRole.SYNERGIST), listOf(Alternative(1, "rope hammer curl"), Alternative(2, "cross-body DB curl"), Alternative(3, "band neutral-grip curl")), "DB hammer curl", ProgramV72.ISOLATION_REST))
            add(slot(phase, 5, "32", "DB reverse wrist curl", 3, 3, "12–20", "0–1", Muscle.FOREARMS, mapOf(Muscle.FOREARMS to MuscleRole.PRIME), listOf(Alternative(1, "barbell reverse wrist curl"), Alternative(2, "cable reverse wrist curl"), Alternative(3, "band reverse wrist curl")), "Supported DB/barbell reverse wrist curl", ProgramV72.ISOLATION_REST))
            add(slot(phase, 5, "33", "Paused DB shrug", 4, 4, "10–15", "0–1", Muscle.TRAPS, mapOf(Muscle.TRAPS to MuscleRole.PRIME), listOf(Alternative(1, "barbell shrug"), Alternative(2, "Smith shrug"), Alternative(3, "cable shrug")), "DB/barbell shrug", ProgramV72.ISOLATION_REST))
            add(slot(phase, 6, "34", "Barbell Romanian deadlift", 4, 4, "8–10", "1–2", Muscle.HAMSTRINGS, mapOf(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), listOf(Alternative(1, "DB RDL"), Alternative(2, "Smith RDL"), Alternative(3, "band RDL")), "Barbell/DB RDL", ProgramV72.COMPOUND_REST))
            add(slot(phase, 6, "35", "Barbell hip thrust", 4, 5, "8–12", "1–2", Muscle.GLUTES, mapOf(Muscle.GLUTES to MuscleRole.PRIME), listOf(Alternative(1, "Smith hip thrust"), Alternative(2, "DB hip thrust"), Alternative(3, "band hip thrust")), "Barbell/DB hip thrust; shortened-position emphasis retained", ProgramV72.COMPOUND_REST))
            add(slot(phase, 6, "36", "Bulgarian split squat", 3, 3, "8–10/side", "1–2", Muscle.QUADS, mapOf(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST), listOf(Alternative(1, "Smith split squat"), Alternative(2, "DB split squat"), Alternative(3, "reverse lunge")), "DB Bulgarian split squat; knee-forward quad emphasis", ProgramV72.COMPOUND_REST))
            add(slot(phase, 6, "37", "Lying leg curl", 5, 5, "10–15", "≥1", Muscle.HAMSTRINGS, mapOf(Muscle.HAMSTRINGS to MuscleRole.PRIME), listOf(Alternative(1, "prone band curl"), Alternative(2, "slider curl"), Alternative(3, "Nordic eccentric")), "Prone band curl, hips neutral; sliders/Nordics change loading", ProgramV72.ISOLATION_REST))
            add(slot(phase, 6, "38", "Leg extension", 2, 3, "12–15", "≥1", Muscle.QUADS, mapOf(Muscle.QUADS to MuscleRole.PRIME), listOf(Alternative(1, "seated band knee extension"), Alternative(2, "reverse Nordic"), Alternative(3, "Spanish squat")), "Seated band knee extension, hip flexed; different resistance curve", ProgramV72.ISOLATION_REST))
            add(slot(phase, 6, "39", "Seated calf raise", 8, 8, "10–15", "≥1", Muscle.CALVES, mapOf(Muscle.CALVES to MuscleRole.PRIME), listOf(Alternative(1, "seated DB calf raise"), Alternative(2, "bent-knee Smith calf raise"), Alternative(3, "seated barbell calf raise")), "Seated DB calf raise, knees bent, block under forefeet", ProgramV72.ISOLATION_REST))
            add(slot(phase, 6, "40", "Hanging leg raise", 2, 2, "10–15", "1", Muscle.ABS, mapOf(Muscle.ABS to MuscleRole.PRIME), listOf(Alternative(1, "captain’s-chair raise"), Alternative(2, "reverse crunch"), Alternative(3, "lying leg raise")), "Rack hanging leg raise or reverse crunch; avoid lumbar extension", ProgramV72.ISOLATION_REST))
        }
    }
    private fun slot(phase: Int, day: Int, key: String, name: String, base: Int, peak: Int,
        reps: String, rir: String, owner: Muscle, roles: Map<Muscle, MuscleRole>,
        alternatives: List<Alternative>, home: String, rest: String): ResistanceSlot {
        val sets = if (phase == 3) peak else base
        return ResistanceSlot("v73_p${phase}_d${day}_$key", phase, day, name,
            "$sets × $reps @ $rir RIR", sets, owner, roles, alternatives,
            "$rest Home-rack alternative: $home. Unilateral sets include both sides; reps are per side.")
    }
    fun resistanceForDay(week: Int, day: Int): List<ResistanceSlot> {
        require(day in 1..7)
        // The final-test overlay retains v7.2 names, alternatives, sets and absent rep ranges.
        if (week == 32) return ProgramV72.resistanceForDay(week, day)
        return resistanceSlots.filter { it.phase == phaseForWeek(week) && it.day == day }
    }
    fun plannedSets(week: Int, slot: ResistanceSlot): Int? = when {
        week == 32 -> ProgramV72.plannedSets(week, slot)
        isConsolidation(week) -> (slot.workingSets / 2).coerceAtLeast(1)
        week in setOf(30, 31) -> (slot.workingSets + 1) / 2
        else -> slot.workingSets
    }
    fun plannedDose(week: Int, slot: ResistanceSlot, sets: Int): String {
        if (week == 32) return ProgramV72.plannedDose(week, slot, sets)
        val range = slot.dose.substringAfter(" ×").substringBefore(" @")
        val rir = when {
            isConsolidation(week) -> "3–4"
            week in setOf(30, 31) -> if (slot.restGuidance.startsWith(ProgramV72.COMPOUND_REST)) "2–3" else "1–2"
            else -> return slot.dose
        }
        return "$sets ×$range @ $rir RIR"
    }
    fun sessionCap(week: Int, day: Int): Int = when {
        day in setOf(1, 7) -> 0
        phaseForWeek(week) == 3 -> if (day == 4) 26 else 30
        else -> if (day == 4) 24 else 28
    }
    /** Reference totals include direct 1.0 plus explicitly mapped indirect 0.5 credits. */
    fun volumeForWeek(week: Int): Map<Muscle, Pair<Int, Double>> = Muscle.entries.associateWith { muscle ->
        val slots = (1..7).flatMap { resistanceForDay(week, it) }
        val direct = slots.sumOf { if (it.ownerMuscle == muscle) plannedSets(week, it) ?: 0 else 0 }
        val fractional = slots.sumOf { (plannedSets(week, it) ?: 0) * (it.roleMap[muscle]?.creditPerSet ?: 0.0) }
        direct to fractional
    }
    fun volumeMatrixTsv(): String = buildString {
        appendLine("week\tmuscle\tdirect_sets\tfractional_reference")
        for (week in 1..32) volumeForWeek(week).forEach { (muscle, values) ->
            appendLine("$week\t${muscle.label}\t${values.first}\t${values.second}")
        }
    }
}
