package com.jk.operatorperformance.program

/** Copy shown on V74 screens, dialogs, and the start gate. Retained-week branches keep the previous sentences. */
internal object V74SurfaceText {
    const val FONT_LICENSE_LINE = "EB Garamond, SIL Open Font License 1.1."
    const val PAUSED_SESSION = "The prescription stays on screen. Set ticks stay with this workout."
    const val PAUSED_SESSION_DESCRIPTION = "Session paused. Resume shows the same reference and the same set ticks."
    const val WORKOUT_TICK_NOTE =
        "Read the whole session while training. Tick an effort when it is done. A tick only marks it done. It does not record a load, rep count, RIR, time, or result, and it does not start, stop, or change a timer."
    const val START_WEEK_ONE = "START WEEK 1 • DAY 1"
    const val START_AT_POSITION = "START AT A POSITION"

    const val RESET_CHOOSER = "Choose Week 1 or a block, block week, and day. ${V74Pointer.CONTINUOUS_LINE}"

    const val RESET_WEEK_ONE_CONFIRMATION =
        "This starts Week 1, Day 1. Continuous program: four 8-week blocks, repeated. Your current place, today's session and saved alternatives are cleared. Utility settings stay."

    fun advanceConfirmation(v74: Boolean): String = if (v74) {
        "Move the program pointer to the next day? This clears the active session."
    } else {
        "Move the program pointer to the next day? This clears the active session. It does not store loads, reps, or results."
    }

    fun resetWorkoutConfirmation(v74: Boolean): String = if (v74) {
        "Clear today's active session, chosen alternatives, and rest timer? The week stays marked as engaged."
    } else {
        "Clear today's active session, chosen alternatives, and rest timer? The week stays marked as engaged, and no loads, reps, or results are stored."
    }

    fun setCurrentDay(day: Int): String =
        "Set today to Day $day of this week? Today's session, alternatives and rest timer are cleared. The week and block stay."

    fun consolidateConfirmation(): String =
        "This block week becomes the consolidation week. ${ProgramV74.CONSOLIDATION_LABEL}"

    fun searchStatus(count: Int): String = if (count == 0) {
        "No matching approved sessions."
    } else {
        "$count matching session${if (count == 1) "" else "s"} shown."
    }

    /** Static sentences reachable on a V74 week, excluding session rows and the four notes sections. */
    fun chrome(): List<String> = listOf(
        "LEGEND",
        V74Pointer.CONTINUOUS_LINE,
        START_WEEK_ONE,
        START_AT_POSITION,
        "SESSION NOT STARTED",
        "SESSION PAUSED • RESUME WHEN READY",
        "SESSION ACTIVE",
        "SESSION COMPLETE",
        "CONSOLIDATE NOW",
        "SESSION PAUSED",
        PAUSED_SESSION,
        PAUSED_SESSION_DESCRIPTION,
        "RESET TODAY'S WORKOUT",
        "COMPLETE DAY / NEXT DAY",
        "START WORKOUT",
        "RESUME WORKOUT",
        "Advance to the next day?",
        advanceConfirmation(true),
        "ADVANCE",
        "CANCEL",
        "Consolidate this block week?",
        consolidateConfirmation(),
        "CONSOLIDATE",
        "Reset today's workout?",
        resetWorkoutConfirmation(true),
        "RESET",
        "OPTIONAL",
        "COACHING / ALTERNATIVES",
        "COACHING",
        "ALTERNATIVES",
        "SESSION ACTIVE • FULL REFERENCE",
        WORKOUT_TICK_NOTE,
        "COMPLETE",
        "SKIP REST",
        "+30 SEC",
        "REST TIMER",
        "Compounds rest 2–4 min. Isolation rests 1–2 min. Sprint and interval recoveries in the prescription take precedence.",
        "MARK SESSION COMPLETE",
        "RETURN TO TODAY",
        "SEARCH / JUMP",
        "Examples: block, squat, threshold",
        "OPEN FIRST MATCH",
        searchStatus(0),
        searchStatus(1),
        searchStatus(2),
        "CLEAR SEARCH",
        "BLOCKS",
        "BROWSE BLOCK",
        "Change current program day?",
        setCurrentDay(1),
        "CONFIRM",
        "SET CURRENT",
        "CLOSE",
        "DOSSIER",
        "Position",
        "CHANGE POSITION",
        "START OVER",
        "Notes",
        "Key",
        "About",
        "LEGEND 0.44.0 (53)",
        FONT_LICENSE_LINE,
        "OPEN KEY",
        "PROGRAM / COACHING NOTES",
        "SHOW NOTES",
        "HIDE NOTES",
        "Reset current program pointer?",
        RESET_WEEK_ONE_CONFIRMATION,
        "Start at this position?",
        "START",
        "BACK",
        "Block",
        "Block week",
        "Day",
        "CONTINUE",
        "KEY",
        "Plain meanings for the marks on screen. This page does not change the program.",
        "MARK",
        "LEGEND mark",
        "The map key. It names the program.",
        "DAYS",
        "Engine",
        "Contour rings. Aerobic and engine work.",
        "Lower",
        "Solid survey point. Lower-body work.",
        "Upper A",
        "Open station. Names the Upper A day, which mixes pressing and pulling.",
        "Speed",
        "Route line with ticks. Speed work.",
        "Upper B",
        "Station with a traverse. Names the Upper B day, which mixes pressing and pulling. Easy aerobic follows when that week prescribes it.",
        "Rest",
        "Open circle. Rest.",
        "PHASES",
        "Draft",
        "One stake. Phase 1.",
        "Build",
        "Two stakes. Phase 2.",
        "Backstop",
        "Three stakes. Phase 3.",
        "Live",
        "Four stakes. Phase 4.",
        "NAVIGATION",
        "Today",
        "Marked point. The current day.",
        "Week",
        "Even stakes. The current week.",
        "Program",
        "Map sheet. The 32-week program.",
        "Dossier",
        "Scale bar. Dossier.",
        "STATE",
        "Brass marks today, the active phase, primary actions, and the selected tab.",
        "Red marks stop, pain, warning, and error. It is not a decoration.",
        "Day labels use neutral type unless the day is today.",
        "RETURN TO DOSSIER",
        ProgramV74.GLOBAL_LINE,
        ProgramV74.CONSOLIDATION_LABEL,
        "1 matching session shown.",
        "2 matching sessions shown.",
    )
}
