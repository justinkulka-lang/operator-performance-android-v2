package com.jk.operatorperformance.program

enum class ProgramAuthority(val versionLabel: String) {
    V71("7.1"),
    V72("7.2"),
}

enum class Muscle(val label: String) {
    QUADS("Quads"), GLUTES("Glutes"), HAMSTRINGS("Hamstrings"), CHEST("Chest"),
    LATS("Lats"), UPPER_BACK("Upper back"), DELTS("Delts"), BICEPS("Biceps"),
    TRICEPS("Triceps"), CALVES("Calves"), TRAPS("Traps"), FOREARMS("Forearms"), ABS("Abs"),
}

enum class MuscleRole(val creditPerSet: Double) { PRIME(1.0), SYNERGIST(0.5) }

data class Alternative(val rank: Int, val name: String)

/** One canonical resistance slot for one phase. Planned overlays are applied by week. */
data class ResistanceSlot(
    val id: String,
    val phase: Int,
    val day: Int,
    val movement: String,
    val dose: String,
    val workingSets: Int,
    val ownerMuscle: Muscle,
    val roleMap: Map<Muscle, MuscleRole>,
    val alternatives: List<Alternative>,
    val restGuidance: String,
)

data class ConditioningSegment(
    val id: String,
    val title: String,
    val prescription: String,
    val effortCount: Int = 1,
    val effortLabel: String = "BLOCK",
    val restSeconds: Int? = null,
    val restAfterLastEffort: Boolean = false,
    val qualifyingSprintMeters: Int = 0,
    val alternatives: List<String> = emptyList(),
    val note: String? = null,
)

data class WeekConditioning(
    val week: Int,
    val d1: List<ConditioningSegment>,
    val speed: List<ConditioningSegment>,
    val engine: List<ConditioningSegment>,
)

data class WeekDefinition(val week: Int)

enum class BlockStatus { POPULATED }

data class ProgramBlock(
    val name: String,
    val godName: String,
    val startWeek: Int,
    val endWeek: Int,
    val status: BlockStatus,
    val purpose: String,
) {
    val displayName: String get() = "$godName — $name"
}

enum class SprintVolumeStatus { ALLOWED, WARNING, PROHIBITED }

data class ProgramMatrixRow(
    val muscle: Muscle,
    val normalCredit: Double,
    val normalExposures: Int,
    val consolidationCredit: Double,
    val consolidationExposures: Int,
    val normalDirectSets: Int,
    val consolidationDirectSets: Int,
)
