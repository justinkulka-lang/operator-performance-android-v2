package com.jk.operatorperformance.program

enum class Muscle(val label: String) {
    QUADS("Quads"),
    GLUTES("Glutes"),
    HAMSTRINGS("Hamstrings"),
    CHEST("Chest"),
    LATS("Lats"),
    UPPER_BACK("Upper back"),
    DELTS("Delts"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    CALVES("Calves"),
    TRAPS("Traps"),
    FOREARMS("Forearms"),
    ABS("Abs"),
}

enum class MuscleRole(val creditPerSet: Double) {
    PRIME(1.0),
    SYNERGIST(0.5),
}

data class Alternative(
    val rank: Int,
    val name: String,
)

data class ResistanceSlot(
    val id: String,
    val day: Int,
    val movement: String,
    val dose: String,
    val workingSets: Int,
    val deloadSets: Int,
    val restSeconds: Int,
    val ownerMuscle: Muscle,
    val roleMap: Map<Muscle, MuscleRole>,
    val alternatives: List<Alternative>,
)

data class AerobicRow(
    val id: String,
    val day: Int,
    val title: String,
    val prescription: String,
    val alternative: String,
)

enum class MachineModality(val label: String) {
    AIR_FAN_BIKE("Air/fan bike"),
    STATIONARY_CYCLE("Stationary cycle ergometer"),
    ROW_ERG("Row ergometer"),
    SKI_ERG("Ski ergometer"),
}

data class MachinePowerFinisher(
    val efforts: Int,
    val hardSeconds: Int,
    val easySeconds: Int,
    val primary: MachineModality,
    val alternative: MachineModality,
)

data class ConditioningSegment(
    val id: String,
    val title: String,
    val prescription: String,
    val effortCount: Int = 1,
    val effortLabel: String = "SEGMENT",
    val restSeconds: Int? = null,
    val restAfterLastEffort: Boolean = false,
    val qualifyingSprintMeters: Int = 0,
    val alternative: String? = null,
    val note: String? = null,
)

data class WeekConditioning(
    val week: Int,
    val speed: List<ConditioningSegment>,
    val engine: List<ConditioningSegment>,
)

data class WeekDefinition(
    val week: Int,
)

enum class BlockStatus {
    POPULATED,
    UNPOPULATED_NOT_PROGRAMMED,
}

data class ProgramBlock(
    val name: String,
    val startWeek: Int,
    val endWeek: Int,
    val status: BlockStatus,
)

enum class SprintVolumeStatus {
    ALLOWED,
    WARNING,
    PROHIBITED,
}

data class ProgramMatrixRow(
    val muscle: Muscle,
    val normalCredit: Double,
    val normalExposures: Int,
    val deloadCredit: Double,
    val deloadExposures: Int,
    val normalDirectSets: Int,
    val deloadDirectSets: Int,
)
