package com.jk.operatorperformance.program

object ProgramNotesV56 {
    val execution = listOf(
        "Day 3 Speed is performed fresh. Do not perform heavy resistance work in the same session; sprint quality is the priority stimulus.",
        "Week 1 Day 3: use submaximal build-ups, then one timed 300 m. Time externally; the app does not store the result. Do not perform the machine-power finisher on the baseline day.",
        "Week 1 Day 6: perform one controlled, strong-but-repeatable 1.5-mile effort. Do not add a separate VO2 block, threshold block, or sprint session on that baseline day.",
        "Day 6 Engine is performed before the prescribed short-upper resistance sequence.",
    )

    val resistanceProgression = listOf(
        "Double progression uses reps before load as training guidance.",
        "Stay within the prescribed rep range and RIR target for the movement; the app does not require or store load, reps, or RIR history.",
        "Heavy compound rest is approximately 2–3+ minutes; isolation/accessory rest is approximately 1–2 minutes. Extend rest when needed to preserve prescribed reps, RIR, technique, and output.",
    )

    val foundationSpeedSelection = listOf(
        "ACCELERATION is the first post-baseline default: 6 × 30 m from a controlled start with full recovery.",
        "After two quality ACCELERATION exposures with stable mechanics and no adverse next-morning lower-leg response, you may manually select MAX VELOCITY.",
        "After two quality MAX VELOCITY exposures with stable mechanics and no adverse next-morning response, you may manually select SPECIAL ENDURANCE.",
        "After one quality SPECIAL ENDURANCE exposure without a sprint-volume-cap breach or adverse next-morning response, you may manually return to ACCELERATION.",
        "Variant selection remains manual. The app does not score, pass/fail, or infer progression from performance data.",
    )

    val foundationEngineSelection = listOf(
        "THRESHOLD is the first post-baseline default: 20 min continuous at current threshold effort OR 3 × 1 mile with 1 min easy recovery.",
        "Repeat THRESHOLD or manually switch only when pacing/mechanics and next-morning lower-leg response are acceptable.",
        "VO2 PRIMARY: 5 × 1000 m at current 3K–5K-equivalent pace. SECONDARY: 30 s hard / 30 s easy × 16–20 when deliberately selected. Use one option, not both.",
        "The long-interval VO2 option is preferred for dedicated VO2 development; 30/30 remains an authorized secondary option.",
        "Repeat or manually switch based on self-assessed session quality and lower-leg response. The app does not collect pace, time, distance, mileage, or next-morning scores.",
    )

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
        "Consider a deload when any two of these indicators are present: estimated 1RM or comparable bar-speed performance is down roughly 5–10% across sessions; the same load feels at least 2 RIR harder for two or more sessions; or there is a multi-day subjective recovery decline.",
        "Backstop reference: consider a deload when 6–8 weeks have elapsed since the previous deload.",
        "Deload duration remains 5–7 days when used.",
        "During deload, hold load/intensity, stay short of failure, remove sprint/special-endurance work, and keep running easy.",
        "Resistance work uses the canonical reduced-set overlay (117.5 fractional total).",
        "The app does not score recovery state, collect performance data, or automatically trigger a deload.",
    )

    val hypertrophyProtection = listOf(
        "Productive resistance training is the structural foundation of ARES Performance.",
        "Normal Weeks 1–31 retain the 148.0 fractional hypertrophy chassis and at least two meaningful resistance exposures per canonical muscle group.",
        "Conditioning receives 0 hypertrophy credit.",
        "Reduce avoidable conditioning fatigue before sacrificing productive hypertrophy work, except during the explicit Week 32 realization/taper.",
    )

    val authorityLimits = listOf(
        "v5.7 does not specify an exact count, distance, or percentage progression for the submaximal sprint build-ups; the app must not invent one.",
        "When a sprint prescription says full recovery, v5.7 does not provide a numeric recovery duration; the app must not fabricate one.",
        "The app does not request or store 300 m or 1.5-mile results, run pace, distance, readiness, bodyweight, load, reps, or performed RIR.",
        "If a future exact execution value is required, it must come from an owner-approved authority rather than a hidden app rule.",
    )
}
