package com.jk.operatorperformance.program

object ProgramNotesV72 {
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
