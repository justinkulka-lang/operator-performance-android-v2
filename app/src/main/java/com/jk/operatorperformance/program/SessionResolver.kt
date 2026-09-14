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
    val effortLabel: String = "SET",
    val restSeconds: Int? = null,
    val restAfterLastEffort: Boolean = false,
    val optional: Boolean = false,
) {
    init {
        require(effortCount >= 1) { "effortCount must be at least 1." }
        require(restSeconds == null || restSeconds > 0) { "restSeconds must be positive when present." }
        require(alternatives.size <= 1) { "v5.6 allows at most one approved alternative per row." }
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
        deload: Boolean,
        machineInventoryConfigured: Boolean = false,
        availableMachineModalities: Set<MachineModality> = emptySet(),
    ): SessionReference {
        require(week in 1..8) { "Only populated Weeks 1–8 may resolve." }
        require(day in 1..7) { "Day must be 1–7." }

        val title = requireNotNull(OperatorProgramV56.dayTitles[day])
        val rows = when (day) {
            1 -> resistanceRows(day, deload) + aerobicRow()
            2 -> resistanceRows(day, deload)
            3 -> speedRows(week, deload, machineInventoryConfigured, availableMachineModalities)
            4 -> resistanceRows(day, deload)
            5 -> resistanceRows(day, deload)
            6 -> engineRows(week, deload) + resistanceRows(day, deload)
            7 -> listOf(
                SessionRow(
                    id = "d7_rest",
                    title = "Rest",
                    prescription = OperatorProgramV56.restPrescription,
                    kind = SessionRowKind.REST,
                    effortLabel = "REST",
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
        deload: Boolean,
        machineInventoryConfigured: Boolean = false,
        availableMachineModalities: Set<MachineModality> = emptySet(),
    ): List<SessionReference> {
        require(week in 1..8) { "Only populated Weeks 1–8 may resolve." }
        return (1..7).map { day ->
            resolve(
                week = week,
                day = day,
                deload = deload,
                machineInventoryConfigured = machineInventoryConfigured,
                availableMachineModalities = availableMachineModalities,
            )
        }
    }

    private fun resistanceRows(day: Int, deload: Boolean): List<SessionRow> =
        OperatorProgramV56.resistanceForDay(day).mapNotNull { slot ->
            val sets = if (deload) slot.deloadSets else slot.workingSets
            if (sets == 0) return@mapNotNull null
            SessionRow(
                id = slot.id,
                title = slot.movement,
                prescription = "${doseWithSets(slot.dose, sets)} • REST ${formatRest(slot.restSeconds)}",
                kind = SessionRowKind.RESISTANCE,
                alternatives = listOf(slot.alternatives.single().name),
                effortCount = sets,
                effortLabel = "SET",
                restSeconds = slot.restSeconds,
            )
        }

    private fun aerobicRow(): SessionRow {
        val row = OperatorProgramV56.day1EasyAerobic
        return SessionRow(
            id = row.id,
            title = row.title,
            prescription = row.prescription,
            kind = SessionRowKind.CONDITIONING,
            alternatives = listOf(row.alternative),
            effortLabel = "BLOCK",
            note = "Low-eccentric aerobic work. Do not turn the prescribed easy effort into intervals.",
        )
    }

    private fun speedRows(
        week: Int,
        deload: Boolean,
        machineInventoryConfigured: Boolean,
        availableMachineModalities: Set<MachineModality>,
    ): List<SessionRow> {
        if (deload) return OperatorProgramV56.deloadSpeedSegments.map(::conditioningRow)

        val fixed = OperatorProgramV56.conditioningForWeek(week)
        val rows = mutableListOf(
            SessionRow(
                id = "d3_speed_warmup",
                title = "Speed warm-up",
                prescription = OperatorProgramV56.speedWarmupPrescription,
                kind = SessionRowKind.CONDITIONING,
                effortLabel = "WARM-UP",
            ),
        )
        rows += fixed.speed.map(::conditioningRow)

        if (week in 1..4) {
            resolveMachineFinisher(machineInventoryConfigured, availableMachineModalities)?.let(rows::add)
        }
        return rows
    }

    private fun resolveMachineFinisher(
        configured: Boolean,
        available: Set<MachineModality>,
    ): SessionRow? {
        val finisher = OperatorProgramV56.machinePowerFinisher
        val resolved = when {
            !configured -> null
            finisher.primary in available -> finisher.primary
            finisher.alternative in available -> finisher.alternative
            else -> return null
        }
        val title = resolved?.let { "Optional machine-power finisher — ${it.label}" }
            ?: "Optional machine-power finisher — ${finisher.primary.label}"
        val alternatives = if (resolved == null) listOf(finisher.alternative.label) else emptyList()
        return SessionRow(
            id = "d3_machine_power_finisher",
            title = title,
            prescription = "${finisher.efforts} × ${finisher.hardSeconds} s hard / ${finisher.easySeconds} s easy",
            kind = SessionRowKind.CONDITIONING,
            alternatives = alternatives,
            note = "Use the air/fan bike first. If unavailable, use the stationary cycle ergometer. If neither is available, omit this optional finisher. Do not add running.",
            effortCount = finisher.efforts,
            effortLabel = "EFFORT",
            restSeconds = finisher.easySeconds,
            optional = true,
        )
    }

    private fun engineRows(week: Int, deload: Boolean): List<SessionRow> {
        if (deload) return OperatorProgramV56.deloadEngineSegments.map(::conditioningRow)

        val fixed = OperatorProgramV56.conditioningForWeek(week)
        return buildList {
            add(
                SessionRow(
                    id = "d6_engine_warmup",
                    title = "Engine warm-up — treadmill",
                    prescription = OperatorProgramV56.engineWarmupPrescription,
                    kind = SessionRowKind.CONDITIONING,
                    alternatives = listOf("Measured flat track/outdoor — use the same durations and progressive effort structure."),
                    effortLabel = "WARM-UP",
                ),
            )
            addAll(fixed.engine.map(::conditioningRow))
            add(
                SessionRow(
                    id = "d6_engine_cooldown",
                    title = "Engine cooldown",
                    prescription = OperatorProgramV56.engineCooldownPrescription,
                    kind = SessionRowKind.CONDITIONING,
                    alternatives = listOf("Measured flat track/outdoor — 5:00 easy walk/jog at RPE 1–2."),
                    effortLabel = "COOLDOWN",
                ),
            )
        }
    }

    private fun conditioningRow(segment: ConditioningSegment): SessionRow = SessionRow(
        id = segment.id,
        title = segment.title,
        prescription = buildString {
            append(segment.prescription)
            segment.restSeconds?.let { append(" • RECOVERY ").append(formatRest(it)) }
        },
        kind = SessionRowKind.CONDITIONING,
        alternatives = segment.alternative?.let(::listOf).orEmpty(),
        note = segment.note,
        effortCount = segment.effortCount,
        effortLabel = segment.effortLabel,
        restSeconds = segment.restSeconds,
        restAfterLastEffort = segment.restAfterLastEffort,
    )

    private fun doseWithSets(canonicalDose: String, sets: Int): String {
        val separator = " × "
        val index = canonicalDose.indexOf(separator)
        require(index > 0) { "Canonical resistance dose must contain ' × ': $canonicalDose" }
        return sets.toString() + canonicalDose.substring(index)
    }

    private fun formatRest(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
}
