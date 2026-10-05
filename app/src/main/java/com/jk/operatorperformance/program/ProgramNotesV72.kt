package com.jk.operatorperformance.program

object ProgramNotesV72 {
    const val rangeOfMotion = "Use a full controlled range of motion that remains pain-free and technically stable."
    const val rirDefinition = "RIR means the number of additional technically acceptable repetitions that could have been completed with the same range and execution."
    const val calibrationCorrection = "If the calibration result exceeds the prediction by more than two repetitions, increase the next working load or correct the RIR estimate."
    const val energyRecovery = "Unintentional body-mass loss across two weeks together with regression in multiple lifts requires correction of energy intake and recovery before further lifting volume is removed."
    const val accelerationIntent = "Acceleration and flying repetitions use technical intent, not split times derived from a 300 m result. Accelerate smoothly, remain relaxed, and stop before straining."
    const val easyAerobic = "RPE 2–3 of 10. Full-sentence conversation remains comfortable. Run on a flat predictable route or use the prescribed bike option."
    const val fullRecovery = "Technique-led. No mandatory split. Full recovery means begin only when breathing is controlled and the athlete can reproduce relaxed mechanics."
    const val easyRecovery = "Easy means walking or very slow jogging. Do not turn recoveries into additional tempo work."
    const val rankedAlternative = "When a movement causes pain or cannot be performed safely, use the first available ranked alternative for the same movement pattern with the same sets, repetitions, and RIR."
    const val alternativeReplacement = "An exercise alternative replaces the listed movement; it is never added to it."
    const val painStop = "Pain that changes running gait or lifting mechanics ends that exercise. Persistent symptoms require qualified evaluation before progression."
    const val medicalRestrictions = "Medical restrictions and injury management override participation. This authority does not provide diagnosis, clearance, or rehabilitation."
    const val seriousSymptoms = "Chest pain, fainting, unexplained severe breathlessness, acute neurologic symptoms, severe heat illness, or suspected serious injury stops training and requires appropriate evaluation."
    const val sprintReferenceLimit = "These 300 m entries are average-velocity references. They are not standing-start segment predictions and do not define maximum sprint velocity."
    const val qualityReview = "Two consecutive unattained quality sessions require review before the next quality exposure."

    val resistanceExecution = listOf(rangeOfMotion, rirDefinition, rankedAlternative, alternativeReplacement)
    val safety = listOf(medicalRestrictions, painStop, seriousSymptoms)
    val recovery = listOf(energyRecovery, qualityReview)
    val runningExecution = listOf(easyAerobic, accelerationIntent, fullRecovery, easyRecovery)
    val sprintReference = listOf("150 m" to "23.0 s", "200 m" to "30.7 s", "250 m" to "38.3 s", "300 m" to "46.0 s")

    val execution = listOf(
        "Day 1 is Engine, then short upper when the week includes it. Day 4 is Speed. Day 5 is Upper B, then easy aerobic except the Week 32 fixed prescription.",
        "Day 7 is the only rest day. Quality runs are about 72 hours apart. No extra conditioning is added between Engine and Lower A.",
        "A missed day pauses the sequence. Resume that same program day. Do not skip forward, double sessions, or remove the rest day.",
    )
    val resistanceProgression = listOf(
        "Use double progression inside the listed range. When every working set reaches the top at the prescribed RIR, add the smallest practical load and return to the bottom of the range.",
        "Default increases are about 5 lb for upper-body lifts and 5–10 lb for lower-body lifts. Smaller increases are allowed.",
        "Primary compounds stop at the prescribed RIR. Lower-body isolation keeps at least 1 RIR. Upper-body isolation may reach 0–1 RIR only where prescribed.",
        ProgramV72.calibrationNote,
        calibrationCorrection,
        "One weak session does not change the plan. A greater than 5% decline in the same movement for two consecutive exposures permits removal of one set from that movement for the next exposure. Restore it when performance returns. The app does not remove the set automatically.",
    )
    val interference = listOf(
        "Keep the seven-day order. Lower A is about 48 hours before Speed, and Lower B is about 48 hours after Speed.",
        "Easy aerobic work stays conversational. It must not become an unlisted tempo session.",
    )
    val sprintVolume = listOf(
        "Quality sprint volume may not exceed 600 m. A 600 m session shows a workload warning. That warning is not a validated injury threshold.",
        "Build-ups and drill distances do not count toward the cap. Stop for altered gait, sharp pain, or clear mechanical deterioration, and do not replace the stopped repetitions.",
    )
    val runImpact = listOf(ProgramV72.testGate, ProgramV72.ANCHOR_NOTE, "Reference paces: ${ProgramV72.GOAL_PACE_TABLE}.")
    val plannedOverlays = listOf(
        "Weeks 8, 16, and 24 halve lifting sets and use 3–4 RIR. Conditioning still follows that week's exact row.",
        "Weeks 30 and 31 use the realization set table, familiar loads, 2–3 RIR on compounds, and 1–2 RIR on isolation.",
        "Week 32 keeps the fixed prescription. Day 2 and Day 6 are very easy bike and mobility. Day 5 has no easy aerobic. There is no Week 33.",
    )
    val authorityLimits = listOf(
        "The app is an offline reference. Session status is not a set log. It does not log loads, reps, RIR, pace, results, bodyweight, calories, sleep, readiness, heart rate, or photos.",
        "An active v7.1 week is finished under v7.1. v7.2 becomes active at the next unstarted Day 1.",
        "Benchmark records stay outside the app.",
    )
}
