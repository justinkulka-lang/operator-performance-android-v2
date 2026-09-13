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
        "Day 2 is resistance only.",
        "Only easy low-eccentric aerobic work may be paired with lifting by default.",
        "Authorized aerobic/conditioning modalities: run, air/fan bike, stationary cycle, row erg, ski erg, and incline treadmill/walk. Sled work and rucking are not authorized.",
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
        "No diagnosis, treatment, or medical-clearance advice is provided.",
    )

    val deload = listOf(
        "Deload activation is manual and reference-driven.",
        "Consider a deload when any two reference indicators are present: estimated 1RM or comparable bar-speed performance down roughly 5–10% across sessions; the same load feels at least 2 RIR harder for two or more sessions; or a multi-day subjective recovery decline.",
        "Backstop reference: 6–8 weeks since the previous deload.",
        "Deload duration: 5–7 days.",
        "During deload, hold load/intensity, stay short of failure, remove sprint/special-endurance work, and keep running easy.",
        "The app does not score these indicators, does not require those inputs, and does not automatically force a deload.",
        "The app applies only the canonical deload overlay and does not infer deload activation from hidden or entered performance statistics.",
    )
}
