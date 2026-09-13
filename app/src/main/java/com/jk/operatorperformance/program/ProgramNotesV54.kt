package com.jk.operatorperformance.program

object ProgramNotesV54 {
    val interference = listOf(
        "Speed or special-endurance work → lower-body resistance: 48 hours.",
        "Lower-body resistance → speed work: 12 hours.",
        "Lower-body resistance → hard run: 24 hours.",
        "Hard run → lower-body resistance: 24 hours.",
        "Hard run ↔ speed work: 24 hours.",
        "Any two hard sessions on the same day: 6-hour floor.",
        "Low-eccentric easy aerobic work → anything: no restriction.",
        "Day 2 is resistance only. No rucking.",
    )

    val sprintVolume = listOf(
        "0–500 m qualifying sprint/special-endurance volume: allowed without a cap warning.",
        ">500–600 m: allowed with a clear warning.",
        "600 m exactly: passes with a warning.",
        ">600 m: prohibited.",
        "Submaximal warm-up/build-up meters below quality-sprint intensity do not count toward the cap.",
    )

    val runImpact = listOf(
        "Before voluntarily extending a single continuous run, compare the proposed distance with the longest completed continuous run in the prior 30 calendar days. The reference ceiling is 110%. The app does not collect the history required to calculate or enforce that ceiling.",
        "Stop a run or sprint if pain changes gait.",
        "Stop if Achilles or shin symptoms worsen during the session rather than warming up.",
        "Hold run/sprint progression if symptoms persist 48 hours later.",
        "These are reference instructions only; the app does not ask you to score or log symptoms.",
    )

    val deload = listOf(
        "Deload activation is manual and reference-driven.",
        "The app applies only the canonical deload overlay and does not infer deload activation from hidden or entered performance statistics.",
    )
}
