package com.jk.operatorperformance.program

internal object V73SessionBuilder {
    fun resolve(week: Int, day: Int): SessionReference {
        val legacy = V72SessionBuilder.resolve(week, day)
        if (week == 32) return legacy.copy(authority = ProgramAuthority.V73, phase = ProgramV73.blockForWeek(week))
        val rows = when (day) {
            1 -> legacy.rows.takeWhile { it.id != "d1_resistance_warmup" }
            2, 3, 6 -> resistanceRows(week, day)
            4 -> buildList {
                legacy.rows.forEach { row ->
                    add(row)
                    if (row.id.endsWith("plyometrics") && week in ProgramV73.THROW_WEEKS) add(
                        SessionRow("v73_w${week}_throws", "Hip-driven medicine-ball throws",
                            "3 × 3 hip-driven scoop OR rotational medicine-ball throws. Nine total throws, both sides combined. Alternate sides and the starting side between exposures. No chest passes.",
                            SessionRowKind.CONDITIONING, note = "After jumps, before sprints. Zero qualifying sprint meters. Stop for pain or degraded mechanics.",
                            effortCount = 3, effortLabel = "SET"))
                }
                addAll(resistanceRows(week, 4))
            }
            5 -> resistanceRows(week, 5) + legacy.rows.filter { it.id == ProgramV72.conditioningForWeek(week).d1.single().id }
            else -> legacy.rows
        }
        return legacy.copy(title = if (day in setOf(1, 4) && week !in setOf(1, 24)) ProgramV73.dayTitles.getValue(day) else if (day == 4) ProgramV73.dayTitles.getValue(day) else legacy.title,
            rows = rows, phase = ProgramV73.blockForWeek(week), authority = ProgramAuthority.V73)
    }
    private fun resistanceRows(week: Int, day: Int): List<SessionRow> = buildList {
        val slots = ProgramV73.resistanceForDay(week, day)
        if (slots.isEmpty()) return@buildList
        add(SessionRow("v73_d${day}_resistance_warmup", "Resistance warm-up", ProgramV72.resistanceWarmup,
            SessionRowKind.CONDITIONING, effortLabel = "WARM-UP"))
        slots.forEach { slot ->
            val sets = requireNotNull(ProgramV73.plannedSets(week, slot))
            val overlay = when {
                ProgramV73.isConsolidation(week) -> "Planned consolidation: halve slot sets, round down, minimum one. 3–4 RIR. Do not chase progression."
                week in setOf(30, 31) -> "Planned realization: ceiling-halved base sets. Familiar loads; compounds 2–3 RIR, isolation 1–2 RIR."
                else -> "Compounds 1–2 RIR; upper isolation 0–1 RIR; lower isolation at least 1 RIR; trunk 1 RIR. No mandatory failure or intensity techniques."
            }
            add(SessionRow(slot.id, slot.movement, ProgramV73.plannedDose(week, slot, sets), SessionRowKind.RESISTANCE,
                alternatives = slot.alternatives.sortedBy { it.rank }.map { it.name },
                note = "${slot.restGuidance} $overlay", effortCount = sets, effortLabel = "SET"))
        }
    }
}
