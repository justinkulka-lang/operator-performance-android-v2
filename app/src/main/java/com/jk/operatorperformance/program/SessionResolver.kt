package com.jk.operatorperformance.program

enum class SessionRowKind { RESISTANCE, CONDITIONING, REST }

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
        require(alternatives.size <= 4) { "At most four ranked alternatives may be displayed." }
    }
}

data class SessionReference(
    val week: Int,
    val day: Int,
    val title: String,
    val rows: List<SessionRow>,
    val deload: Boolean,
    val phase: ProgramBlock,
)

object SessionResolver {
    fun resolve(
        week: Int,
        day: Int,
        deload: Boolean,
        machineInventoryConfigured: Boolean = false,
        availableMachineModalities: Set<MachineModality> = emptySet(),
        speedVariant: SpeedVariant = SpeedVariant.ACCELERATION,
        engineVariant: EngineVariant = EngineVariant.THRESHOLD,
    ): SessionReference {
        require(week in 1..32) { "Only populated Weeks 1–32 may resolve." }
        require(day in 1..7) { "Day must be 1–7." }

        val plan = OperatorProgramV56.conditioningForWeek(week, speedVariant, engineVariant)
        val title = requireNotNull(OperatorProgramV56.dayTitles[day])
        val rows = when (day) {
            1 -> resistanceRows(day, week, deload) + plan.d1.map(::conditioningRow)
            2 -> resistanceRows(day, week, deload)
            3 -> speedRows(plan, deload, machineInventoryConfigured, availableMachineModalities)
            4 -> resistanceRows(day, week, deload)
            5 -> resistanceRows(day, week, deload)
            6 -> engineRows(plan, deload) + resistanceRows(day, week, deload)
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

        return SessionReference(week, day, title, rows, deload, OperatorProgramV56.blockForWeek(week))
    }

    fun splitForWeek(
        week: Int,
        deload: Boolean,
        machineInventoryConfigured: Boolean = false,
        availableMachineModalities: Set<MachineModality> = emptySet(),
        speedVariant: SpeedVariant = SpeedVariant.ACCELERATION,
        engineVariant: EngineVariant = EngineVariant.THRESHOLD,
    ): List<SessionReference> {
        require(week in 1..32) { "Only populated Weeks 1–32 may resolve." }
        return (1..7).map { day ->
            resolve(week, day, deload, machineInventoryConfigured, availableMachineModalities, speedVariant, engineVariant)
        }
    }

    private fun resistanceRows(day: Int, week: Int, deload: Boolean): List<SessionRow> =
        OperatorProgramV56.resistanceForDay(day).mapNotNull { slot ->
            val sets = when {
                deload -> slot.deloadSets
                week == 32 -> OperatorProgramV56.week32Sets(slot.id, slot.workingSets)
                else -> slot.workingSets
            }
            if (sets == 0) return@mapNotNull null
            val taperNote = if (!deload && week == 32) {
                OperatorProgramV56.week32RirFloor(day)?.let { floor -> "Week 32 realization: keep at least $floor RIR on every working set." }
            } else null
            SessionRow(
                id = slot.id,
                title = slot.movement,
                prescription = doseWithSets(slot.dose, sets),
                kind = SessionRowKind.RESISTANCE,
                alternatives = slot.alternatives.sortedBy { it.rank }.map { it.name },
                note = listOfNotNull(slot.restGuidance, taperNote).joinToString(" "),
                effortCount = sets,
                effortLabel = "SET",
                restSeconds = null,
            )
        }

    private fun speedRows(
        plan: WeekConditioning,
        deload: Boolean,
        machineInventoryConfigured: Boolean,
        availableMachineModalities: Set<MachineModality>,
    ): List<SessionRow> {
        if (deload) {
            return listOf(
                SessionRow(
                    id = "d3_deload",
                    title = "Speed deload",
                    prescription = "No sprint or special-endurance work. Keep conditioning easy only.",
                    kind = SessionRowKind.CONDITIONING,
                    effortLabel = "BLOCK",
                    note = "Manual deload suppresses sprint/special-endurance work.",
                ),
            )
        }

        val rows = mutableListOf(
            SessionRow(
                id = "d3_speed_warmup",
                title = "Speed warm-up",
                prescription = OperatorProgramV56.speedWarmupPrescription,
                kind = SessionRowKind.CONDITIONING,
                effortLabel = "WARM-UP",
                note = OperatorProgramV56.speedStopRule,
            ),
        )
        rows += plan.speed.map(::conditioningRow)
        if (plan.allowsMachineFinisher) {
            machineFinisherRow(machineInventoryConfigured, availableMachineModalities)?.let(rows::add)
        }
        rows += SessionRow(
            id = "d3_speed_cooldown",
            title = "Speed cooldown",
            prescription = OperatorProgramV56.speedCooldownPrescription,
            kind = SessionRowKind.CONDITIONING,
            effortLabel = "COOLDOWN",
        )
        return rows
    }

    private fun machineFinisherRow(configured: Boolean, available: Set<MachineModality>): SessionRow? {
        val finisher = OperatorProgramV56.machinePowerFinisher
        val selected = if (configured) finisher.hierarchy.firstOrNull { it in available } else null
        if (configured && selected == null) return null
        val title = selected?.let { "Optional machine-power finisher — ${it.label}" } ?: "Optional machine-power finisher"
        val ranked = if (selected == null) finisher.hierarchy.map { it.label } else emptyList()
        val note = if (selected == null) {
            "Equipment order: ${finisher.hierarchy.joinToString(" → ") { it.label }}. Use the first available. If none is available, omit the finisher. Never add replacement running."
        } else {
            "Selected from the approved equipment hierarchy. If unavailable, use the next approved machine or omit. Never add replacement running."
        }
        return SessionRow(
            id = "d3_machine_power_finisher",
            title = title,
            prescription = "${finisher.efforts} × ${finisher.hardSeconds} sec hard / ${finisher.easySeconds} sec easy",
            kind = SessionRowKind.CONDITIONING,
            alternatives = ranked,
            note = note,
            effortCount = finisher.efforts,
            effortLabel = "EFFORT",
            restSeconds = finisher.easySeconds,
            optional = true,
        )
    }

    private fun engineRows(plan: WeekConditioning, deload: Boolean): List<SessionRow> {
        if (deload) {
            return listOf(
                SessionRow(
                    id = "d6_deload_engine",
                    title = "Easy engine",
                    prescription = "Keep running easy. Omit threshold, VO2, goal-pace, checkpoint, and test work while manual deload is active.",
                    kind = SessionRowKind.CONDITIONING,
                    alternatives = listOf("Stationary bike — easy conversational effort."),
                    effortLabel = "BLOCK",
                    note = OperatorProgramV56.engineStopRule,
                ),
            )
        }
        return buildList {
            add(SessionRow("d6_engine_warmup", "Engine warm-up", OperatorProgramV56.engineWarmupPrescription, SessionRowKind.CONDITIONING, effortLabel = "WARM-UP"))
            addAll(plan.engine.map(::conditioningRow))
            add(
                SessionRow(
                    id = "d6_engine_cooldown",
                    title = "Engine cooldown",
                    prescription = OperatorProgramV56.engineCooldownPrescription,
                    kind = SessionRowKind.CONDITIONING,
                    effortLabel = "COOLDOWN",
                    note = "After the cooldown, complete the prescribed short-upper resistance work.",
                ),
            )
        }
    }

    private fun conditioningRow(segment: ConditioningSegment): SessionRow = SessionRow(
        id = segment.id,
        title = segment.title,
        prescription = segment.prescription,
        kind = SessionRowKind.CONDITIONING,
        alternatives = segment.alternatives,
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
}
