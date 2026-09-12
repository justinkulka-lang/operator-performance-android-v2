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
    val ownerMuscle: Muscle,
    val roleMap: Map<Muscle, MuscleRole>,
    val alternatives: List<Alternative>,
)

data class AerobicRow(
    val id: String,
    val day: Int,
    val title: String,
    val prescription: String,
    val rankedModalities: List<String>,
    val additionalOptions: List<String> = emptyList(),
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
    val rankedModalities: List<MachineModality>,
)

enum class SpeedVariant {
    ACCELERATION,
    MAX_VELOCITY,
    SPECIAL_ENDURANCE,
}

data class SpeedOption(
    val variant: SpeedVariant,
    val prescriptions: List<String>,
    val qualifyingSprintMeters: List<Int>,
    val guidance: String,
    val includesMachinePowerFinisher: Boolean,
)

enum class EngineVariant {
    THRESHOLD,
    VO2,
}

data class EngineOption(
    val variant: EngineVariant,
    val prescriptions: List<String>,
    val guidance: String,
)

data class WeekDefinition(
    val week: Int,
    val speedBaseline: Boolean,
    val engineBaseline: Boolean,
    val defaultSpeedVariant: SpeedVariant?,
    val defaultEngineVariant: EngineVariant?,
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
