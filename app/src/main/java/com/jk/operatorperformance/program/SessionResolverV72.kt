package com.jk.operatorperformance.program

internal object V72SessionBuilder {
    fun resolve(week: Int, day: Int): SessionReference {
        require(week in 1..ProgramV72.TOTAL_WEEKS) { "Week must be 1–32." }
        require(day in 1..ProgramV72.DAYS_PER_WEEK) { "Day must be 1–7." }
        val plan = ProgramV72.conditioningForWeek(week)
        val rows = when (day) {
            1 -> engineDay(week, plan)
            2 -> if (week == 32) listOf(bikeRow("w32_d2", "Very easy bike and mobility", "15 min very easy bike plus mobility. No Lower A lifting.")) else resistanceDay(week, 2)
            3 -> resistanceDay(week, 3)
            4 -> speedDay(week, plan)
            5 -> dayFive(week, plan)
            6 -> if (week == 32) listOf(bikeRow("w32_d6", "Very easy bike and mobility", "15–20 min very easy bike plus mobility. No Lower B lifting.")) else resistanceDay(week, 6)
            7 -> listOf(SessionRow("d7_rest", "Rest", ProgramV72.restPrescription, SessionRowKind.REST, effortLabel = "REST"))
            else -> error("Unreachable day")
        }
        return SessionReference(
            week = week,
            day = day,
            title = titleFor(week, day),
            rows = rows,
            phase = ProgramV72.blockForWeek(week),
            authority = ProgramAuthority.V72,
        )
    }

    private fun titleFor(week: Int, day: Int): String = when {
        week == 1 && day == 1 -> "Engine baseline"
        week == 24 && day == 1 -> "1.5-mile checkpoint"
        week == 32 && day == 1 -> "1.5-mile benchmark"
        week == 32 && day == 2 -> "Very easy bike and mobility"
        week == 32 && day == 5 -> "Upper B"
        week == 32 && day == 6 -> "Very easy bike and mobility"
        else -> requireNotNull(ProgramV72.dayTitles[day])
    }

    private fun engineDay(week: Int, plan: WeekConditioning): List<SessionRow> = buildList {
        val easyOnly = ProgramV72.isEasyOnlyEngine(week)
        add(
            SessionRow(
                id = "w${week}_d1_warmup",
                title = if (easyOnly) "Easy-run opening" else "Engine warm-up",
                prescription = if (easyOnly) ProgramV72.engineEasyWarmup else ProgramV72.engineQualityWarmup,
                kind = SessionRowKind.CONDITIONING,
                note = if (easyOnly) "Included in the listed easy run. No strides. The 8–10 min cooldown still follows the run." else "Complete before threshold conditioning, intervals, goal-pace, or timed work.",
                effortLabel = "WARM-UP",
            ),
        )
        add(engineWork(plan.engine.single()))
        add(cooldownRow("w${week}_d1_cooldown"))
        if (!ProgramV72.omitShortUpper(week)) addAll(resistanceDay(week, 1))
    }

    private fun speedDay(week: Int, plan: WeekConditioning): List<SessionRow> = buildList {
        val warmup = listOfNotNull(
            ProgramV72.speedWarmup,
            ProgramV72.speedTestBuildup.takeIf { ProgramV72.isTimed300(week) },
        ).joinToString(" ")
        add(SessionRow("d4_speed_warmup", "Speed warm-up", warmup, SessionRowKind.CONDITIONING, note = "Build-ups do not count toward quality-sprint volume.", effortLabel = "WARM-UP"))
        if (ProgramV72.isTimed300(week)) {
            add(SessionRow("w${week}_timed300_gate", "Timed 300 m eligibility gate", ProgramV72.testGate, SessionRowKind.CONDITIONING, effortLabel = "CHECK"))
        }
        if (ProgramV72.includesPlyometrics(week)) {
            add(SessionRow("w${week}_plyometrics", "Low-dose plyometrics", ProgramV72.plyometrics, SessionRowKind.CONDITIONING, note = "After the running warm-up and before the sprint repetitions.", effortCount = 6, effortLabel = "SET"))
        }
        addAll(speedReferenceGroups(week, plan.speed.single()).map { speedWork(week, it) })
        add(cooldownRow("w${week}_d4_cooldown"))
    }

    private fun dayFive(week: Int, plan: WeekConditioning): List<SessionRow> = buildList {
        addAll(resistanceDay(week, 5))
        if (week != 32) add(easyAerobic(plan.d1.single()))
    }

    private fun resistanceDay(week: Int, day: Int): List<SessionRow> = buildList {
        val lifts = ProgramV72.resistanceForDay(week, day).mapNotNull { slot ->
            val sets = ProgramV72.plannedSets(week, slot) ?: return@mapNotNull null
            val overlay = when {
                ProgramV72.isConsolidation(week) -> "Planned consolidation: about half the working sets, odd counts rounded down, at least one set, 3–4 RIR. Do not chase progression."
                week in setOf(30, 31) -> "Planned realization. Familiar loads, 2–3 RIR on compounds and 1–2 RIR on isolation. Phase 4 rep ranges are kept. Do not add omitted sets."
                week == 32 -> "Fixed-prescription Week 32 overlay. Sets and 3 RIR only. Rep ranges are not stated for these movements, so none are shown."
                else -> null
            }
            SessionRow(
                id = slot.id,
                title = slot.movement,
                prescription = ProgramV72.plannedDose(week, slot, sets),
                kind = SessionRowKind.RESISTANCE,
                alternatives = slot.alternatives.sortedBy { it.rank }.map { it.name },
                note = listOfNotNull(slot.restGuidance, overlay).joinToString(" "),
                effortCount = sets,
                effortLabel = "SET",
            )
        }
        if (lifts.isEmpty()) return@buildList
        val calibration = if (week !in setOf(8, 16, 24, 30, 31, 32)) ProgramV72.calibrationNote else null
        add(SessionRow("d${day}_resistance_warmup", "Resistance warm-up", ProgramV72.resistanceWarmup, SessionRowKind.CONDITIONING, note = calibration, effortLabel = "WARM-UP"))
        addAll(lifts)
    }

    private fun engineWork(segment: ConditioningSegment): SessionRow {
        val guidance = when {
            segment.title.contains("Threshold", true) -> "Threshold conditioning at RPE 7–8. Controlled discomfort, speech limited to short phrases, even pace, no closing surge."
            segment.title.contains("Controlled interval", true) -> "RPE 8 with even repetitions. The last repetition stays mechanically clean and no slower than the first by more than 3%."
            segment.title.contains("Goal pace", true) -> "Exact 10:00 1.5-mile reference: ${ProgramV72.GOAL_PACE_TABLE}. One and one-half statute miles equals ${ProgramV72.STATUTE_MILE_AND_HALF_METRES} m. If the pace cannot be repeated inside the 3% rule, use the fastest clean repeatable pace and record the session as not attained outside the app."
            segment.title.contains("1.5-mile", true) || segment.title.contains("Final", true) -> "Use an external timer and a measured course. One authorized effort. Record the result only outside the app."
            segment.title.contains("Easy", true) -> "RPE 2–3 for the whole run. Do not add strides or turn this into tempo work."
            else -> "Follow the listed work and recovery exactly."
        }
        return SessionRow(
            id = segment.id,
            title = segment.title,
            prescription = segment.prescription,
            kind = SessionRowKind.CONDITIONING,
            note = "$guidance ${ProgramV72.stopRule}",
            effortCount = segment.effortCount,
            effortLabel = segment.effortLabel,
        )
    }

    private fun speedReferenceGroups(week: Int, segment: ConditioningSegment): List<ConditioningSegment> {
        if (week !in setOf(13, 20, 28, 30)) return listOf(segment)
        val groups = Regex("^(\\d+) × (\\d+) m acceleration \\+ (\\d+) × (\\d+) m fly(;.*)$")
            .matchEntire(segment.prescription) ?: error("Invalid mixed Speed prescription for week $week")
        val accelerationCount = groups.groupValues[1].toInt()
        val accelerationDistance = groups.groupValues[2].toInt()
        val flyCount = groups.groupValues[3].toInt()
        val flyDistance = groups.groupValues[4].toInt()
        val recovery = groups.groupValues[5]
        require(accelerationCount + flyCount == segment.effortCount)
        require(accelerationCount * accelerationDistance + flyCount * flyDistance == segment.qualifyingSprintMeters)
        return listOf(
            segment.copy(id = "${segment.id}_acceleration", title = "Acceleration",
                prescription = "$accelerationCount × $accelerationDistance m acceleration$recovery",
                effortCount = accelerationCount, qualifyingSprintMeters = accelerationCount * accelerationDistance),
            segment.copy(id = "${segment.id}_fly", title = "Fly",
                prescription = "$flyCount × $flyDistance m fly$recovery",
                effortCount = flyCount, qualifyingSprintMeters = flyCount * flyDistance),
        )
    }

    private fun speedWork(week: Int, segment: ConditioningSegment): SessionRow {
        require(segment.qualifyingSprintMeters <= 600) { "Week $week quality sprint volume ${segment.qualifyingSprintMeters} m exceeds 600 m." }
        val warning = if (segment.qualifyingSprintMeters == 600) {
            "WORKLOAD WARNING: this session prescribes 600 m of quality sprinting. This is a workload warning, not a validated injury threshold."
        } else null
        val anchor = if (week in ProgramV72.PERCENTAGE_SPEED_WEEKS) ProgramV72.ANCHOR_NOTE else null
        val fly = if (segment.prescription.contains("fly", true)) "Display the written build or acceleration distance. Do not add an approach or repetitions that are not written." else null
        return SessionRow(
            id = segment.id,
            title = segment.title,
            prescription = segment.prescription,
            kind = SessionRowKind.CONDITIONING,
            note = listOfNotNull(ProgramV72.stopRule, warning, anchor, fly).joinToString(" "),
            effortCount = segment.effortCount,
            effortLabel = segment.effortLabel,
        )
    }

    private fun easyAerobic(segment: ConditioningSegment) = SessionRow(
        id = segment.id,
        title = segment.title,
        prescription = segment.prescription,
        kind = SessionRowKind.CONDITIONING,
        note = segment.note,
        effortLabel = "BLOCK",
    )

    private fun cooldownRow(id: String) = SessionRow(id, "Cooldown", ProgramV72.cooldown, SessionRowKind.CONDITIONING, effortLabel = "COOLDOWN")

    private fun bikeRow(id: String, title: String, prescription: String) = SessionRow(
        id, title, prescription, SessionRowKind.CONDITIONING,
        note = "Very easy. No lifting and no invented mobility circuit.",
        effortLabel = "RECOVERY",
    )
}
