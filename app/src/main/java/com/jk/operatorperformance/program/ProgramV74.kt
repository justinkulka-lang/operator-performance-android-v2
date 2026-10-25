package com.jk.operatorperformance.program

/**
 * LEGEND v7.4 continuous allocation from the approved handoff.
 * Activation and migration stay out of this stage, so [ProgramAuthority] is unchanged.
 */
object ProgramV74 {
    const val GLOBAL_LINE = "Stop any exercise that causes pain; don't replace the stopped reps."
    const val CONSOLIDATION_LABEL = "Consolidation week · half sets · 3–4 RIR"
    const val EFFORT_EASY = "RPE 2–3, conversational"
    const val EFFORT_THRESHOLD = "RPE 7–8, short phrases only, even pace"
    const val EFFORT_CONTROLLED = "RPE 8; the last rep matches the first"
    const val EFFORT_MILE_AND_HALF = "RPE 8–9, hard but repeatable"
    const val EFFORT_SPEED = "Full recovery between reps; quality over quantity"

    val dayTitles = mapOf(
        1 to "Engine",
        2 to "Lower A",
        3 to "Upper A",
        4 to "Speed, cooldown, then Upper C",
        5 to "Upper B, then easy aerobic",
        6 to "Lower B",
        7 to "Rest",
    )

    fun position(absoluteWeek: Int): Pair<Int, Int> {
        require(absoluteWeek >= 1) { "Absolute week starts at 1." }
        val block = ((absoluteWeek - 1) / 8) % 4 + 1
        val blockWeek = (absoluteWeek - 1) % 8 + 1
        return block to blockWeek
    }

    fun workingSets(block: Int, slot: V74Slot): Int {
        require(block in 1..4) { "Block must be 1–4." }
        return if (block == 2) slot.peakSets else slot.baseSets
    }

    fun plannedSets(block: Int, blockWeek: Int, slot: V74Slot): Int {
        require(blockWeek in 1..8) { "Block week must be 1–8." }
        val sets = workingSets(block, slot)
        return if (blockWeek == 8) (sets / 2).coerceAtLeast(1) else sets
    }

    fun reps(block: Int, slot: V74Slot): String {
        require(block in 1..4) { "Block must be 1–4." }
        return if (block == 3 && slot.blockThreeReps != null) slot.blockThreeReps else slot.reps
    }

    fun movement(block: Int, slot: V74Slot): String {
        require(block in 1..4) { "Block must be 1–4." }
        return if (block == 4 && slot.blockFourMovement != null) slot.blockFourMovement else slot.movement
    }

    fun rankedAlternatives(block: Int, slot: V74Slot): List<Alternative> {
        require(block in 1..4) { "Block must be 1–4." }
        return if (block == 4 && slot.blockFourAlternatives != null) slot.blockFourAlternatives else slot.alternatives
    }

    fun displayedAlternatives(block: Int, slot: V74Slot): List<String> =
        rankedAlternatives(block, slot).sortedBy { it.rank }.map { it.name } + "Home: ${slot.home}"

    fun rir(blockWeek: Int, slot: V74Slot): String {
        require(blockWeek in 1..8) { "Block week must be 1–8." }
        return if (blockWeek == 8) "3–4" else slot.rir
    }

    fun plannedDose(block: Int, blockWeek: Int, slot: V74Slot): String =
        "${plannedSets(block, blockWeek, slot)} × ${reps(block, slot)} @ ${rir(blockWeek, slot)} RIR"

    fun slot(number: Int): V74Slot = slots.single { it.number == number }

    fun directSets(block: Int, blockWeek: Int): Map<Muscle, Int> = Muscle.entries.associateWith { muscle ->
        slots.filter { it.ownerMuscle == muscle }.sumOf { plannedSets(block, blockWeek, it) }
    }

    fun daySets(block: Int, blockWeek: Int, day: Int): Int =
        slots.filter { it.day == day }.sumOf { plannedSets(block, blockWeek, it) }

    fun sessionCap(block: Int, day: Int): Int = when {
        day == 1 || day == 7 -> 0
        block == 2 && day == 4 -> 26
        block == 2 -> 30
        day == 4 -> 24
        else -> 28
    }

    fun runWeek(blockWeek: Int): V74RunWeek {
        require(blockWeek in 1..8) { "Block week must be 1–8." }
        return runCycle[blockWeek - 1]
    }

    val runCycle: List<V74RunWeek> = listOf(
        V74RunWeek("3 × 6 min threshold; 2 min easy", "6 × 20 m acceleration; 3 min recovery", 120, "Bike 25 min easy", false, false, EFFORT_THRESHOLD),
        V74RunWeek("5 × 3 min controlled interval; 2 min easy", "6 × 30 m acceleration; 3–4 min recovery", 180, "Easy run 20 min or bike 30 min", false, false, EFFORT_CONTROLLED),
        V74RunWeek("20 min continuous threshold", "4 × (20 m build + 20 m fly); 5 min recovery", 80, "Bike 30 min easy", true, true, EFFORT_THRESHOLD),
        V74RunWeek("6 × 400 m controlled interval; 90 s easy", "8 × 30 m acceleration; 3–4 min recovery", 240, "Easy run 20 min", true, true, EFFORT_CONTROLLED),
        V74RunWeek("3 × 1 mile threshold; 1 min easy", "3 × 150 m at about 90% effort; 5–6 min recovery", 450, "Bike 30 min easy", true, false, EFFORT_THRESHOLD),
        V74RunWeek("4 × 1000 m controlled interval; 2 min easy", "5 × (20 m build + 20 m fly); 5 min recovery", 100, "Easy run 22 min", true, true, EFFORT_CONTROLLED),
        V74RunWeek("6 × 400 m at hard, even 1.5-mile effort; 90 s easy", "2 × 250 m at about 90–93% effort; 7–8 min recovery", 500, "Easy run 25 min", true, false, EFFORT_MILE_AND_HALF),
        V74RunWeek("Easy run 25 min; easy-only warm-up", "4 × 20 m relaxed acceleration; 3 min recovery", 80, "Bike 20 min easy", false, false, EFFORT_EASY),
    )

    private val quads = mapOf(Muscle.QUADS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST)
    private val quadsOnly = mapOf(Muscle.QUADS to MuscleRole.PRIME)
    private val hamstrings = mapOf(Muscle.HAMSTRINGS to MuscleRole.PRIME)
    private val hamstringsGlutes = mapOf(Muscle.HAMSTRINGS to MuscleRole.PRIME, Muscle.GLUTES to MuscleRole.SYNERGIST)
    private val glutesHamstrings = mapOf(Muscle.GLUTES to MuscleRole.PRIME, Muscle.HAMSTRINGS to MuscleRole.SYNERGIST)
    private val glutes = mapOf(Muscle.GLUTES to MuscleRole.PRIME)
    private val calves = mapOf(Muscle.CALVES to MuscleRole.PRIME)
    private val abs = mapOf(Muscle.ABS to MuscleRole.PRIME)
    private val chestPress = mapOf(Muscle.CHEST to MuscleRole.PRIME, Muscle.DELTS to MuscleRole.SYNERGIST, Muscle.TRICEPS to MuscleRole.SYNERGIST)
    private val chest = mapOf(Muscle.CHEST to MuscleRole.PRIME)
    private val upperBack = mapOf(Muscle.UPPER_BACK to MuscleRole.PRIME, Muscle.LATS to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST)
    private val latRow = mapOf(Muscle.LATS to MuscleRole.PRIME, Muscle.UPPER_BACK to MuscleRole.SYNERGIST, Muscle.BICEPS to MuscleRole.SYNERGIST)
    private val lats = mapOf(Muscle.LATS to MuscleRole.PRIME)
    private val delts = mapOf(Muscle.DELTS to MuscleRole.PRIME)
    private val triceps = mapOf(Muscle.TRICEPS to MuscleRole.PRIME)
    private val biceps = mapOf(Muscle.BICEPS to MuscleRole.PRIME)
    private val traps = mapOf(Muscle.TRAPS to MuscleRole.PRIME)
    private val forearms = mapOf(Muscle.FOREARMS to MuscleRole.PRIME)
    private val overheadPress = mapOf(Muscle.DELTS to MuscleRole.PRIME, Muscle.TRICEPS to MuscleRole.SYNERGIST)
    private val hammer = mapOf(Muscle.BICEPS to MuscleRole.PRIME, Muscle.FOREARMS to MuscleRole.SYNERGIST)

    val slots: List<V74Slot> = listOf(
        slot(1, 2, "Back squat", 4, 4, "6–8", "1–2", ProgramV72.COMPOUND_REST, Muscle.QUADS, quads, alts("Safety-bar squat", "front squat", "Smith squat"), "Barbell back/front squat", blockThreeReps = "4–6", blockFourMovement = "Safety-bar squat", blockFourAlternatives = alts("back squat", "front squat", "Smith squat")),
        slot(2, 2, "Leg press", 3, 4, "10–12", "1–2", ProgramV72.COMPOUND_REST, Muscle.QUADS, quads, alts("hack squat", "Smith squat", "heel-elevated DB squat"), "Heel-elevated DB squat; different loading curve", blockFourMovement = "Hack squat", blockFourAlternatives = alts("leg press", "Smith squat", "heel-elevated DB squat")),
        slot(3, 2, "Leg extension", 4, 4, "12–15", "≥1", ProgramV72.ISOLATION_REST, Muscle.QUADS, quadsOnly, alts("seated band knee extension", "reverse Nordic", "Spanish squat"), "Seated band knee extension, hip flexed; different resistance curve"),
        slot(4, 2, "Seated leg curl", 5, 6, "10–15", "≥1", ProgramV72.ISOLATION_REST, Muscle.HAMSTRINGS, hamstrings, alts("seated band leg curl", "lying leg curl", "slider curl"), "Seated band leg curl anchored to rack, hips flexed; lying/slider alternatives lose hip-flexed bias", formCue = "Hips flexed; full stretch at the bottom."),
        slot(5, 2, "Glute-biased 45° hip extension", 4, 4, "10–15", "≥1", ProgramV72.ISOLATION_REST, Muscle.GLUTES, glutesHamstrings, alts("cable pull-through", "glute-biased DB RDL", "glute-biased good morning"), "Glute-biased DB RDL; unsupported, different resistance curve", formCue = "Glute-biased; hips drive the movement."),
        slot(6, 2, "Standing calf raise", 5, 5, "8–12", "≥1", ProgramV72.ISOLATION_REST, Muscle.CALVES, calves, alts("Smith calf raise", "leg-press calf raise", "DB standing calf raise"), "DB standing calf raise off block, rack support", formCue = "Full stretch at the bottom."),
        slot(7, 2, "Weighted crunch", 3, 3, "10–15", "1", ProgramV72.ISOLATION_REST, Muscle.ABS, abs, alts("cable crunch", "band crunch", "machine crunch"), "Weighted crunch"),
        slot(8, 3, "Flat barbell bench press", 4, 4, "6–8", "1–2", ProgramV72.COMPOUND_REST, Muscle.CHEST, chestPress, alts("DB bench", "Smith bench", "machine chest press"), "Barbell/DB bench", blockThreeReps = "4–6"),
        slot(9, 3, "Chest-supported upper-back row", 4, 4, "8–10", "1–2", ProgramV72.COMPOUND_REST, Muscle.UPPER_BACK, upperBack, alts("chest-supported DB row", "wide-elbow cable row", "barbell row"), "Chest-supported DB row, elbows 45–60°", formCue = "Elbows 45–60° from the torso."),
        slot(10, 3, "Incline barbell press", 4, 4, "8–10", "1–2", ProgramV72.COMPOUND_REST, Muscle.CHEST, chestPress, alts("incline DB press", "Smith incline", "machine incline"), "Incline barbell/DB press"),
        slot(11, 3, "Lat-biased one-arm cable row", 3, 3, "10–12", "1–2", ProgramV72.COMPOUND_REST, Muscle.LATS, latRow, alts("one-arm DB row", "tucked-elbow chest-supported row", "one-arm band row"), "One-arm DB row, elbow toward hip", formCue = "Drive the elbow toward the hip."),
        slot(12, 3, "DB lateral raise", 3, 3, "12–20", "0–1", ProgramV72.ISOLATION_REST, Muscle.DELTS, delts, alts("cable lateral", "machine lateral", "band lateral"), "DB lateral raise"),
        slot(13, 3, "Overhead triceps extension", 3, 4, "10–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.TRICEPS, triceps, alts("overhead DB extension", "overhead EZ-bar extension", "overhead band extension"), "Overhead DB/EZ extension; shoulder stays flexed", formCue = "Upper arm stays overhead."),
        slot(14, 3, "Incline DB curl", 4, 4, "10–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.BICEPS, biceps, alts("behind-body cable curl", "behind-body band curl"), "Incline DB curl; shoulder remains extended", formCue = "Shoulder stays extended behind the torso."),
        slot(15, 3, "Paused DB shrug", 3, 4, "10–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.TRAPS, traps, alts("barbell shrug", "Smith shrug", "cable shrug"), "DB/barbell shrug", formCue = "One-second pause at the top."),
        slot(16, 4, "Cable fly", 4, 4, "12–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.CHEST, chest, alts("DB fly", "pec deck", "band fly"), "DB fly, controlled pain-free stretch; different loading curve", formCue = "Controlled, pain-free stretch."),
        slot(17, 4, "DB pullover", 2, 2, "10–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.LATS, lats, alts("cable pullover", "straight-arm pulldown", "band pullover"), "DB pullover; cable/pulldown loading curves differ", formCue = "Controlled, pain-free stretch."),
        slot(18, 4, "Chest-supported upper-back row", 3, 3, "10–12", "1–2", ProgramV72.COMPOUND_REST, Muscle.UPPER_BACK, upperBack, alts("chest-supported DB row", "wide-elbow cable row", "supported band row"), "Chest-supported DB row, elbows 45–60°", formCue = "Elbows 45–60° from the torso."),
        slot(19, 4, "DB lateral raise", 2, 2, "12–20", "0–1", ProgramV72.ISOLATION_REST, Muscle.DELTS, delts, alts("cable lateral", "machine lateral", "band lateral"), "DB lateral raise"),
        slot(20, 4, "Chest-supported rear-delt fly", 3, 3, "12–20", "0–1", ProgramV72.ISOLATION_REST, Muscle.DELTS, delts, alts("reverse pec deck", "cable rear-delt fly", "band rear-delt fly"), "Chest-supported DB rear-delt fly"),
        slot(21, 4, "Incline DB curl", 2, 2, "10–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.BICEPS, biceps, alts("behind-body cable curl", "behind-body band curl"), "Incline DB curl; shoulder remains extended", formCue = "Shoulder stays extended behind the torso."),
        slot(22, 4, "Overhead triceps extension", 4, 4, "10–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.TRICEPS, triceps, alts("overhead DB extension", "overhead EZ-bar extension", "overhead band extension"), "Overhead DB/EZ extension; shoulder stays flexed", formCue = "Upper arm stays overhead."),
        slot(23, 4, "DB wrist curl", 2, 4, "12–20", "0–1", ProgramV72.ISOLATION_REST, Muscle.FOREARMS, forearms, alts("barbell wrist curl", "cable wrist curl", "band wrist curl"), "Supported DB/barbell wrist curl"),
        slot(24, 4, "Cable crunch", 2, 2, "10–15", "1", ProgramV72.ISOLATION_REST, Muscle.ABS, abs, alts("weighted crunch", "band crunch"), "Weighted/band crunch; resistance curves differ"),
        slot(25, 5, "Standing overhead press", 3, 3, "6–8", "1–2", ProgramV72.COMPOUND_REST, Muscle.DELTS, overheadPress, alts("seated DB press", "standing DB press", "Smith overhead press"), "Barbell/DB overhead press", blockThreeReps = "4–6"),
        slot(26, 5, "Lat pulldown", 5, 6, "8–12", "1–2", ProgramV72.COMPOUND_REST, Muscle.LATS, latRow, alts("single-arm cable pulldown", "machine pulldown", "seated band pulldown"), "Seated or kneeling band pulldown anchored high on the rack; overhead stretch retained"),
        slot(27, 5, "Chest-supported upper-back row", 3, 3, "8–12", "1–2", ProgramV72.COMPOUND_REST, Muscle.UPPER_BACK, upperBack, alts("chest-supported DB row", "wide-elbow cable row", "supported band row"), "Chest-supported DB row, elbows 45–60°", formCue = "Elbows 45–60° from the torso."),
        slot(28, 5, "Incline DB press", 3, 3, "10–12", "1–2", ProgramV72.COMPOUND_REST, Muscle.CHEST, chestPress, alts("incline barbell press", "Smith incline", "machine incline"), "Incline DB/barbell press"),
        slot(29, 5, "DB lateral raise", 2, 2, "12–20", "0–1", ProgramV72.ISOLATION_REST, Muscle.DELTS, delts, alts("cable lateral", "machine lateral", "band lateral"), "DB lateral raise"),
        slot(30, 5, "Chest-supported rear-delt fly", 2, 2, "12–20", "0–1", ProgramV72.ISOLATION_REST, Muscle.DELTS, delts, alts("reverse pec deck", "cable rear-delt fly", "band rear-delt fly"), "Chest-supported DB rear-delt fly"),
        slot(31, 5, "DB hammer curl", 3, 3, "10–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.BICEPS, hammer, alts("rope hammer curl", "cross-body DB curl", "band neutral-grip curl"), "DB hammer curl"),
        slot(32, 5, "DB reverse wrist curl", 3, 3, "12–20", "0–1", ProgramV72.ISOLATION_REST, Muscle.FOREARMS, forearms, alts("barbell reverse wrist curl", "cable reverse wrist curl", "band reverse wrist curl"), "Supported DB/barbell reverse wrist curl"),
        slot(33, 5, "Paused DB shrug", 4, 5, "10–15", "0–1", ProgramV72.ISOLATION_REST, Muscle.TRAPS, traps, alts("barbell shrug", "Smith shrug", "cable shrug"), "DB/barbell shrug", formCue = "One-second pause at the top."),
        slot(34, 6, "Barbell Romanian deadlift", 4, 4, "8–10", "1–2", ProgramV72.COMPOUND_REST, Muscle.HAMSTRINGS, hamstringsGlutes, alts("DB RDL", "Smith RDL", "band RDL"), "Barbell/DB RDL", blockThreeReps = "6–8"),
        slot(35, 6, "Barbell hip thrust", 4, 4, "8–12", "1–2", ProgramV72.COMPOUND_REST, Muscle.GLUTES, glutes, alts("Smith hip thrust", "DB hip thrust", "band hip thrust"), "Barbell/DB hip thrust; shortened-position emphasis retained", formCue = "Squeeze hard at lockout."),
        slot(36, 6, "Bulgarian split squat", 3, 3, "8–10/side", "1–2", ProgramV72.COMPOUND_REST, Muscle.QUADS, quads, alts("Smith split squat", "DB split squat", "reverse lunge"), "DB Bulgarian split squat; knee-forward quad emphasis", formCue = "Knee travels forward; reps per side."),
        slot(37, 6, "Seated leg curl", 5, 6, "10–15", "≥1", ProgramV72.ISOLATION_REST, Muscle.HAMSTRINGS, hamstrings, alts("seated band leg curl", "lying leg curl", "slider curl"), "Seated band leg curl anchored to rack, hips flexed; lying/slider alternatives lose hip-flexed bias", formCue = "Hips flexed; full stretch at the bottom."),
        slot(38, 6, "Leg extension", 2, 3, "12–15", "≥1", ProgramV72.ISOLATION_REST, Muscle.QUADS, quadsOnly, alts("seated band knee extension", "reverse Nordic", "Spanish squat"), "Seated band knee extension, hip flexed; different resistance curve"),
        slot(39, 6, "Seated calf raise", 8, 8, "10–15", "≥1", ProgramV72.ISOLATION_REST, Muscle.CALVES, calves, alts("seated DB calf raise", "bent-knee Smith calf raise", "seated barbell calf raise"), "Seated DB calf raise, knees bent, block under forefeet", formCue = "Knees bent; full stretch at the bottom."),
        slot(40, 6, "Hanging leg raise", 2, 2, "10–15", "1", ProgramV72.ISOLATION_REST, Muscle.ABS, abs, alts("captain’s-chair raise", "reverse crunch", "lying leg raise"), "Rack hanging leg raise or reverse crunch; avoid lumbar extension", formCue = "No low-back arch."),
    )

    private fun slot(
        number: Int,
        day: Int,
        movement: String,
        baseSets: Int,
        peakSets: Int,
        reps: String,
        rir: String,
        rest: String,
        owner: Muscle,
        roles: Map<Muscle, MuscleRole>,
        alternatives: List<Alternative>,
        home: String,
        formCue: String? = null,
        blockThreeReps: String? = null,
        blockFourMovement: String? = null,
        blockFourAlternatives: List<Alternative>? = null,
    ) = V74Slot(number, day, movement, baseSets, peakSets, reps, blockThreeReps, rir, rest, formCue, alternatives, home, owner, roles, blockFourMovement, blockFourAlternatives)

    private fun alts(vararg names: String): List<Alternative> =
        names.mapIndexed { index, name -> Alternative(index + 1, name) }
}

data class V74Slot(
    val number: Int,
    val day: Int,
    val movement: String,
    val baseSets: Int,
    val peakSets: Int,
    val reps: String,
    val blockThreeReps: String?,
    val rir: String,
    val rest: String,
    val formCue: String?,
    val alternatives: List<Alternative>,
    val home: String,
    val ownerMuscle: Muscle,
    val roleMap: Map<Muscle, MuscleRole>,
    val blockFourMovement: String?,
    val blockFourAlternatives: List<Alternative>?,
)

data class V74RunWeek(
    val engine: String,
    val speed: String,
    val sprintMeters: Int,
    val easyAerobic: String,
    val jumps: Boolean,
    val medicineBallThrows: Boolean,
    val engineEffort: String,
)
