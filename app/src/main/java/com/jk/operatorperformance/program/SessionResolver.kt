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
    val week: Int,
    val day: Int,
    val title: String,
    val rows: List<SessionRow>,
    val phase: ProgramBlock = AresProgramV71.blockForWeek(week),
)

object SessionResolver {
    fun resolve(week: Int, day: Int): SessionReference {
        require(week in 1..AresProgramV71.TOTAL_WEEKS) { "Week must be 1–32." }
        require(day in 1..AresProgramV71.DAYS_PER_WEEK) { "Day must be 1–7." }
        val plan = AresProgramV71.conditioningForWeek(week)
        val rows = when (day) {
            1 -> resistanceRows(week, day) + plan.d1.map(::conditioningRow)
            2 -> resistanceRows(week, day)
            3 -> speedRows(week, plan)
            4 -> resistanceRows(week, day)
            5 -> if (week == 32) listOf(finalRecoveryRow()) else resistanceRows(week, day)
            6 -> listOf(engineWarmupRow(plan)) + plan.engine.map { engineRow(it) } + if (AresProgramV71.omitShortUpper(week)) emptyList() else resistanceRows(week, day)
            7 -> listOf(restRow(day))
            else -> error("Unreachable day")
        }
        val title = if (week == 32 && day == 5) "Very easy recovery primer" else requireNotNull(AresProgramV71.dayTitles[day])
        return SessionReference(week, day, title, rows)
    }

    fun splitForWeek(week: Int): List<SessionReference> =
        (1..AresProgramV71.DAYS_PER_WEEK).map { resolve(week, it) }

    private fun resistanceRows(week:Int, day:Int):List<SessionRow> =
        AresProgramV71.resistanceForDay(week, day).mapNotNull { slot ->
            val sets = AresProgramV71.plannedSets(week, slot) ?: return@mapNotNull null
            val overlay = when {
                AresProgramV71.isConsolidation(week) -> "Planned consolidation: half normal sets (odd totals rounded down, minimum one), 3–4 RIR; no extra work."
                week in setOf(30,31) -> "Planned realization overlay. Do not add omitted sets or movements."
                week == 32 -> "Final-test overlay. Do not add omitted sets or movements."
                else -> null
            }
            SessionRow(
                id=slot.id,title=slot.movement,prescription=AresProgramV71.plannedDose(week,slot,sets),kind=SessionRowKind.RESISTANCE,
                alternatives=slot.alternatives.sortedBy { it.rank }.map { it.name },
                note=listOfNotNull(slot.restGuidance,overlay).joinToString(" "),effortCount=sets,effortLabel="SET")
        }

    private fun speedRows(week:Int, plan:WeekConditioning):List<SessionRow> = buildList {
        val warmup = listOfNotNull(
            AresProgramV71.speedWarmupPrescription,
            AresProgramV71.speedTestWarmupAddition.takeIf { AresProgramV71.isTimed300(week) },
        ).joinToString(" ")
        add(SessionRow("d3_speed_warmup","Speed warm-up",warmup,SessionRowKind.CONDITIONING,
            note="Perform speed fresh; no heavy lifting in the same session. Complete every warm-up step before quality running.",effortLabel="WARM-UP"))
        if (AresProgramV71.isTimed300(week)) {
            add(SessionRow("w${week}_timed300_gate","Timed 300 m eligibility gate",AresProgramV71.testGate,SessionRowKind.CONDITIONING,
                alternatives=listOf("6 × 30 m at 70–85%; 3 min recovery when the gate is not met."),effortLabel="CHECK"))
        }
        if (AresProgramV71.includesPlyometrics(week)) {
            add(SessionRow("w${week}_plyometrics","Low-volume plyometrics","3 × 3 pogo jumps + 3 × 2 broad jumps. Full-quality contacts; stop before output falls.",SessionRowKind.CONDITIONING,
                note="Approved only for this week; omitted during consolidations and tests.",effortCount=6,effortLabel="SET"))
        }
        addAll(plan.speed.map { speedRow(week, it) })
    }

    private fun engineWarmupRow(plan: WeekConditioning): SessionRow {
        val easyOnly = plan.engine.single().title.contains("Easy run", true)
        return SessionRow(
            id="w${plan.week}_d6_warmup",
            title=if (easyOnly) "Easy-run opening" else "Engine warm-up",
            prescription=if (easyOnly) AresProgramV71.engineEasyWarmupPrescription else AresProgramV71.engineQualityWarmupPrescription,
            kind=SessionRowKind.CONDITIONING,
            note=if (easyOnly) "This opening is part of the prescribed easy-run duration." else "Complete before threshold, interval, goal-pace, or timed work.",
            effortLabel="WARM-UP",
        )
    }

    private fun speedRow(week: Int, segment: ConditioningSegment): SessionRow =
        conditioningRow(segment, AresProgramV71.speedExecutionGuidance(week, segment.title))

    private fun engineRow(segment: ConditioningSegment): SessionRow =
        conditioningRow(segment, AresProgramV71.engineExecutionGuidance(segment.title, segment.prescription))

    private fun restRow(day:Int) = SessionRow("d${day}_rest","Rest",AresProgramV71.restPrescription,SessionRowKind.REST,effortLabel="REST")

    private fun finalRecoveryRow() = SessionRow(
        "w32_d5_recovery", "Final-test recovery primer",
        "15–20 min very easy bike and mobility only; no Lower B lifting.",
        SessionRowKind.CONDITIONING, effortLabel="RECOVERY",
    )

    private fun conditioningRow(segment:ConditioningSegment, executionGuidance:String?=null):SessionRow {
        val warning = when (ProgramAudit.sprintVolumeStatus(segment.qualifyingSprintMeters)) {
            SprintVolumeStatus.WARNING -> "SPRINT VOLUME WARNING: exceeds 500 m; 600 m is the absolute cap."
            SprintVolumeStatus.PROHIBITED -> error("Unauthorized sprint volume: ${segment.id}")
            SprintVolumeStatus.ALLOWED -> null
        }
        return SessionRow(segment.id,segment.title,segment.prescription,SessionRowKind.CONDITIONING,segment.alternatives,
            listOfNotNull(executionGuidance,segment.note,warning).joinToString(" ").ifBlank { null },segment.effortCount,segment.effortLabel,
            segment.restSeconds,segment.restAfterLastEffort)
    }
}
