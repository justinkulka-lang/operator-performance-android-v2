package com.jk.operatorperformance.program

enum class SessionRowKind {
    RESISTANCE,
    CONDITIONING,
    REST,
}

data class SessionRow(
    val id: String,
    val title: String,
    val prescription: String,
    val kind: SessionRowKind,
    val alternatives: List<String> = emptyList(),
    val note: String? = null,
    val effortCount: Int = 1,
) {
    init {
        require(effortCount >= 1) { "effortCount must be at least 1." }
    }
}

data class SessionReference(
    val week: Int,
    val day: Int,
    val title: String,
    val rows: List<SessionRow>,
    val deload: Boolean,
)

object SessionResolver {
    fun resolve(
        week: Int,
        day: Int,
        speedVariant: SpeedVariant,
        engineVariant: EngineVariant,
        deload: Boolean,
        machineInventoryConfigured: Boolean = false,
        availableMachineModalities: Set<MachineModality> = emptySet(),
    ): SessionReference {
        require(week in 1..8) { "Only populated Weeks 1–8 may resolve." }
        require(day in 1..7) { "Day must be 1–7." }

        val title = requireNotNull(OperatorProgramV54.dayTitles[day])
        val rows = when (day) {
            1 -> resistanceRows(day, deload) + aerobicRow()
            2 -> resistanceRows(day, deload)
            3 -> speedRows(
                week,
                speedVariant,
                deload,
                machineInventoryConfigured,
                availableMachineModalities,
            )
            4 -> resistanceRows(day, deload)
            5 -> resistanceRows(day, deload)
            6 -> resistanceRows(day, deload) + engineRows(week, engineVariant, deload)
            7 -> listOf(
                SessionRow(
                    id = "d7_rest",
                    title = "Rest",
                    prescription = OperatorProgramV54.restPrescription,
                    kind = SessionRowKind.REST,
                ),
            )
            else -> error("Unreachable day")
        }

        return SessionReference(
            week = week,
            day = day,
            title = title,
            rows = rows,
            deload = deload,
        )
    }

    fun splitForWeek(
        week: Int,
        speedVariant: SpeedVariant,
        engineVariant: EngineVariant,
        deload: Boolean,
        machineInventoryConfigured: Boolean = false,
        availableMachineModalities: Set<MachineModality> = emptySet(),
    ): List<SessionReference> {
        require(week in 1..8) { "Only populated Weeks 1–8 may resolve." }
        return (1..7).map { day ->
            resolve(
                week,
                day,
                speedVariant,
                engineVariant,
                deload,
                machineInventoryConfigured,
                availableMachineModalities,
            )
        }
    }

    private fun resistanceRows(day: Int, deload: Boolean): List<SessionRow> =
        OperatorProgramV54.resistanceForDay(day).mapNotNull { slot ->
            val sets = if (deload) slot.deloadSets else slot.workingSets
            if (sets == 0) return@mapNotNull null
            SessionRow(
                id = slot.id,
                title = slot.movement,
                prescription = doseWithSets(slot.dose, sets),
                kind = SessionRowKind.RESISTANCE,
                alternatives = slot.alternatives.sortedBy { it.rank }.map { it.name },
                effortCount = sets,
            )
        }

    private fun aerobicRow(): SessionRow {
        val row = OperatorProgramV54.day1EasyAerobic
        return SessionRow(
            id = row.id,
            title = row.title,
            prescription = row.prescription,
            kind = SessionRowKind.CONDITIONING,
            alternatives = row.rankedModalities + row.additionalOptions,
        )
    }

    private fun speedRows(
        week: Int,
        selected: SpeedVariant,
        deload: Boolean,
        machineInventoryConfigured: Boolean,
        availableMachineModalities: Set<MachineModality>,
    ): List<SessionRow> {
        if (deload) {
            return listOf(
                SessionRow(
                    id = "d3_deload",
                    title = "Speed — deload guidance",
                    prescription = OperatorProgramV54.deloadSpeedGuidance,
                    kind = SessionRowKind.CONDITIONING,
                ),
            )
        }
        if (week == 1) {
            return listOf(
                SessionRow(
                    id = "d3_baseline_300m",
                    title = "300 m baseline",
                    prescription = OperatorProgramV54.speedBaselinePrescription,
                    kind = SessionRowKind.CONDITIONING,
                    note = OperatorProgramV54.speedSessionGuidance,
                ),
            )
        }

        val option = OperatorProgramV54.speedOptions.single { it.variant == selected }
        val speed = SessionRow(
            id = "d3_${option.variant.name.lowercase()}",
            title = option.variant.displayLabel(),
            prescription = option.prescriptions.joinToString(" OR "),
            kind = SessionRowKind.CONDITIONING,
            note = listOf(OperatorProgramV54.speedSessionGuidance, option.guidance).joinToString(" "),
        )
        if (!option.includesMachinePowerFinisher) return listOf(speed)

        val finisher = OperatorProgramV54.machinePowerFinisher
        val resolvedModality = if (machineInventoryConfigured) {
            finisher.rankedModalities.firstOrNull { it in availableMachineModalities }
        } else {
            null
        }
        if (machineInventoryConfigured && resolvedModality == null) return listOf(speed)

        val finisherRow = SessionRow(
            id = "d3_machine_power_finisher",
            title = if (resolvedModality == null) {
                "Optional machine-power finisher"
            } else {
                "Optional machine-power finisher — ${resolvedModality.label}"
            },
            prescription = "${finisher.efforts} × ${finisher.hardSeconds} s hard / ${finisher.easySeconds} s easy",
            kind = SessionRowKind.CONDITIONING,
            alternatives = if (resolvedModality == null) {
                finisher.rankedModalities.map { it.label }
            } else {
                emptyList()
            },
            note = if (resolvedModality == null) {
                OperatorProgramV54.machinePowerFinisherGuidance
            } else {
                "${OperatorProgramV54.machinePowerFinisherGuidance} Resolved from configured equipment inventory: ${resolvedModality.label}."
            },
        )
        return listOf(speed, finisherRow)
    }

    private fun engineRows(
        week: Int,
        selected: EngineVariant,
        deload: Boolean,
    ): List<SessionRow> {
        if (deload) {
            return listOf(
                SessionRow(
                    id = "d6_engine_deload",
                    title = "Engine — deload guidance",
                    prescription = OperatorProgramV54.deloadEngineGuidance,
                    kind = SessionRowKind.CONDITIONING,
                ),
            )
        }
        if (week == 1) {
            return listOf(
                SessionRow(
                    id = "d6_baseline_15mi",
                    title = "1.5-mile baseline",
                    prescription = OperatorProgramV54.engineBaselinePrescription,
                    kind = SessionRowKind.CONDITIONING,
                ),
            )
        }

        val option = OperatorProgramV54.engineOptions.single { it.variant == selected }
        return listOf(
            SessionRow(
                id = "d6_engine_${option.variant.name.lowercase()}",
                title = option.variant.displayLabel(),
                prescription = option.prescriptions.joinToString(" OR "),
                kind = SessionRowKind.CONDITIONING,
                note = option.guidance,
            ),
        )
    }

    private fun doseWithSets(canonicalDose: String, sets: Int): String {
        val separator = " × "
        val index = canonicalDose.indexOf(separator)
        require(index > 0) { "Canonical resistance dose must contain ' × ': $canonicalDose" }
        return sets.toString() + canonicalDose.substring(index)
    }

    private fun SpeedVariant.displayLabel(): String = name.replace('_', ' ')
    private fun EngineVariant.displayLabel(): String = name.replace('_', ' ')
}
