package com.jk.operatorperformance.program

object ProgramNotesV56 {
    val interference = listOf(
        "Speed or special-endurance work → lower-body resistance: 48 h reference separation.",
        "Lower-body resistance → Speed: 12 h reference separation.",
        "Lower-body resistance ↔ hard running: 24 h reference separation.",
        "Hard running ↔ Speed: 24 h reference separation.",
        "Any two hard sessions on the same day: 6 h floor.",
        "Easy low-eccentric aerobic work has no additional separation restriction.",
        "Day 2 is resistance only. Day 6 performs Engine work before the short-upper resistance sequence.",
    )

    val sprintVolume = listOf(
        "0–500 m qualifying sprint/special-endurance volume: allowed.",
        ">500–600 m: warning range.",
        "600 m exactly: permitted with warning.",
        ">600 m: prohibited.",
        "Submaximal warm-up/build-up meters below quality-sprint intensity do not count toward the cap.",
        "No canonical sled work and no rucking.",
        "Optional 6 × 10 sec hard / 80 sec easy machine finisher is only for Acceleration/Max-Velocity type work and is omitted for baselines, Special Endurance, checkpoint/final tests, and deload.",
    )

    val runImpact = listOf(
        "Before extending a continuous run, compare the proposed distance with the longest continuous run completed in the prior 30 days; proposed distance should not exceed 110% of that reference.",
        "If a populated D1 easy run would exceed the reference, use the listed duration on the stationary bike instead.",
        "Stop a run or sprint if pain changes gait.",
        "Stop if Achilles or shin symptoms worsen during the session rather than warming up.",
        "Hold run/sprint progression if symptoms persist 48 hours later.",
        "The app does not collect the history required to calculate these rules automatically.",
    )

    val deload = listOf(
        "Deload activation is manual and reference-driven; duration remains 5–7 days when used.",
        "Resistance work uses the canonical reduced-set overlay (117.5 fractional total).",
        "During deload, stay short of failure, remove sprint/special-endurance work, and keep running easy.",
        "The app does not score recovery state, collect performance data, or automatically trigger a deload.",
    )

    val hypertrophyProtection = listOf(
        "Productive resistance training is the structural foundation of ARES Performance.",
        "Normal Weeks 1–31 retain the 148.0 fractional hypertrophy chassis and at least two meaningful resistance exposures per canonical muscle group.",
        "Conditioning receives 0 hypertrophy credit.",
        "Reduce avoidable conditioning fatigue before sacrificing productive hypertrophy work, except during the explicit Week 32 realization/taper.",
    )
}
