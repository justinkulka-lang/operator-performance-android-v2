package com.jk.operatorperformance.program

object ProgramNotesV73 {
    val safety = ProgramNotesV72.safety
    val resistanceExecution = ProgramNotesV72.resistanceExecution
    val recovery = ProgramNotesV72.recovery
    val runningExecution = ProgramNotesV72.runningExecution
    val execution = listOf(
        "Day 1 is Engine alone. Day 4 is Speed, running cooldown, resistance warm-up, then Upper C except Week 32. Day 5 is Upper B then prescribed easy aerobic except Week 32.",
        "Day 7 is the only rest day. No extra conditioning, finishers or replacement repetitions.",
        "A missed day pauses the sequence. Resume that same program day; do not skip forward or double sessions.")
    val resistanceProgression = ProgramNotesV72.resistanceProgression
    val interference = ProgramNotesV72.interference
    val sprintVolume = ProgramNotesV72.sprintVolume
    val runImpact = ProgramNotesV72.runImpact + ProgramV71.easyRunNote
    val plannedOverlays = listOf(
        "Normal Blocks 1, 2 and 4 total 136 working sets; Block 3 totals 146. Fractional credit is reference only and never meets a direct-set target.",
        "Weeks 8 and 16 total 59 sets; Week 24 totals 65. Halve each slot, round down, minimum one; 3–4 RIR.",
        "Weeks 30 and 31 total 77 sets: 43.38% below the 136-set Block 4 base. Familiar loads; compounds 2–3 RIR, isolation 1–2 RIR.",
        "Week 32 retains the fixed 13-set prescription. No Upper C, Lower A, Lower B or Day 5 aerobic. There is no Week 33.")
    val authorityLimits = listOf(
        "The app is an offline reference with session status, substitutions and a manual timer. Results and training records stay outside the app.",
        "Engaged or ambiguous historical weeks retain their authority. v7.3 activates at the next positively unstarted Day 1. Browsing never changes progress.")
}
