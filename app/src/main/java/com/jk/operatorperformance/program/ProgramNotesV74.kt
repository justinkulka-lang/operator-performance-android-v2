package com.jk.operatorperformance.program

/**
 * Tools notes shown only while the current week is V74.
 * Wording is the v7.4 authority text, except double progression and load
 * increments, which are the v7.3 notes with the removed items left out.
 */
object ProgramNotesV74 {
    val weekStructure = listOf(
        "Day order is unchanged:",
        "D1: Engine alone",
        "D2: Lower A",
        "D3: Upper A",
        "D4: Speed, cooldown, then Upper C",
        "D5: Upper B, then easy aerobic",
        "D6: Lower B",
        "D7: Rest",
        blockLine(1),
        blockLine(2),
        blockLine(3),
        blockLine(4),
        "Consolidation week: halve each slot's sets for that block, round down, minimum one set. Use 3–4 RIR at the same loads, and keep rep ranges unchanged.",
    )

    val progression = listOf(
        ProgramNotesV73.resistanceProgression[0],
        ProgramNotesV73.resistanceProgression[1],
        "RIR rules (unchanged):",
        "Compounds: 1–2 RIR",
        "Upper-body isolation: 0–1 RIR",
        "Lower-body isolation: at least 1 RIR",
        "Trunk: 1 RIR",
        "No mandatory failure or intensity techniques.",
    )

    val autoregulation = listOf(
        "1. Backstop: every block week 8 is a consolidation week.",
        "2. Consolidate now (new control). The owner can turn the current week into that block's consolidation week, at an unstarted Day 1 only, after a confirmation step.",
        "The following week starts the next block at Week 1.",
        "The running cycle follows to its week 8.",
        "3. Set removal (existing rule): if a movement declines more than 5% on two consecutive exposures, remove one set from it at the next exposure. Restore the set when performance returns. The app does not do this automatically.",
    )

    val runningEffort = listOf(
        "Easy | ${ProgramV74.EFFORT_EASY}",
        "Threshold | ${ProgramV74.EFFORT_THRESHOLD}",
        "Controlled interval | ${ProgramV74.EFFORT_CONTROLLED}",
        "1.5-mile effort | ${ProgramV74.EFFORT_MILE_AND_HALF}",
        "Speed | ${ProgramV74.EFFORT_SPEED}",
    )

    private fun blockLine(block: Int): String {
        val phase = V74Pointer.phase(block)
        return "${phase.godName} · ${phase.name}. ${phase.purpose}"
    }
}
