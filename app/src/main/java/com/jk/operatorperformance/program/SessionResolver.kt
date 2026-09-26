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
        require(effortCount >= 1)
        require(restSeconds == null || restSeconds > 0)
        require(alternatives.size <= 4)
    }
}

data class SessionReference(
    val cycle: Int,
    val day: Int,
    val title: String,
    val rows: List<SessionRow>,
    val phase: ProgramBlock = AresProgramV70.blockForCycle(cycle),
)

object SessionResolver {
    fun resolve(cycle: Int, day: Int): SessionReference {
        require(cycle in 1..AresProgramV70.TOTAL_CYCLES) { "Cycle must be 1–28." }
        require(day in 1..AresProgramV70.DAYS_PER_CYCLE) { "Day must be 1–8." }
        val plan = AresProgramV70.conditioningForCycle(cycle)
        val rows = when (day) {
            1 -> resistanceRows(cycle, day) + plan.d1.map(::conditioningRow)
            2 -> resistanceRows(cycle, day)
            3 -> speedRows(cycle, plan)
            4, 8 -> listOf(restRow(day))
            5 -> if (cycle == 28) listOf(restRow(day)) else resistanceRows(cycle, day)
            6 -> resistanceRows(cycle, day)
            7 -> plan.engine.map(::conditioningRow) + if (AresProgramV70.omitShortUpper(cycle)) emptyList() else resistanceRows(cycle, day)
            else -> error("Unreachable day")
        }
        val title = if (cycle == 28 && day == 5) "Rest" else requireNotNull(AresProgramV70.dayTitles[day])
        return SessionReference(cycle, day, title, rows)
    }

    fun splitForCycle(cycle: Int): List<SessionReference> =
        (1..AresProgramV70.DAYS_PER_CYCLE).map { resolve(cycle, it) }

    private fun resistanceRows(cycle:Int, day:Int):List<SessionRow> =
        AresProgramV70.resistanceForDay(cycle, day).mapNotNull { slot ->
            val sets = AresProgramV70.plannedSets(cycle, slot) ?: return@mapNotNull null
            val overlay = when {
                AresProgramV70.isConsolidation(cycle) -> "Planned consolidation: half normal sets (odd totals rounded down, minimum one), 3–4 RIR; no extra work."
                cycle in setOf(26,27) -> "Planned peak overlay. Do not add omitted sets or movements."
                cycle == 28 -> "Final realization overlay. Do not add omitted sets or movements."
                else -> null
            }
            SessionRow(
                id=slot.id,title=slot.movement,prescription=AresProgramV70.plannedDose(cycle,slot,sets),kind=SessionRowKind.RESISTANCE,
                alternatives=slot.alternatives.sortedBy { it.rank }.map { it.name },
                note=listOfNotNull(slot.restGuidance,overlay).joinToString(" "),effortCount=sets,effortLabel="SET")
        }

    private fun speedRows(cycle:Int, plan:CycleConditioning):List<SessionRow> = buildList {
        add(SessionRow("d3_speed_warmup","Speed warm-up",AresProgramV70.speedWarmupPrescription,SessionRowKind.CONDITIONING,
            note="Perform speed fresh; no heavy lifting in the same session. ${AresProgramV70.speedStopRule}",effortLabel="WARM-UP"))
        if (AresProgramV70.isTimed300(cycle)) {
            add(SessionRow("c${cycle}_timed300_gate","Timed 300 m eligibility gate",AresProgramV70.testGate,SessionRowKind.CONDITIONING,
                alternatives=listOf("6 × 30 m at 70–85%; 3 min recovery when the gate is not met."),effortLabel="CHECK"))
        }
        if (AresProgramV70.includesPlyometrics(cycle)) {
            add(SessionRow("c${cycle}_plyometrics","Low-volume plyometrics","3 × 3 pogo jumps + 3 × 2 broad jumps. Full-quality contacts; stop before output falls.",SessionRowKind.CONDITIONING,
                note="Approved only for this cycle; omitted during consolidations and tests.",effortCount=6,effortLabel="SET"))
        }
        addAll(plan.speed.map(::conditioningRow))
    }

    private fun restRow(day:Int) = SessionRow("d${day}_rest","Rest",AresProgramV70.restPrescription,SessionRowKind.REST,effortLabel="REST")

    private fun conditioningRow(segment:ConditioningSegment):SessionRow {
        val warning = when (ProgramAudit.sprintVolumeStatus(segment.qualifyingSprintMeters)) {
            SprintVolumeStatus.WARNING -> "SPRINT VOLUME WARNING: exceeds 500 m; 600 m is the absolute cap."
            SprintVolumeStatus.PROHIBITED -> error("Unauthorized sprint volume: ${segment.id}")
            SprintVolumeStatus.ALLOWED -> null
        }
        return SessionRow(segment.id,segment.title,segment.prescription,SessionRowKind.CONDITIONING,segment.alternatives,
            listOfNotNull(segment.note,warning).joinToString(" ").ifBlank { null },segment.effortCount,segment.effortLabel,
            segment.restSeconds,segment.restAfterLastEffort)
    }
}
