package com.jk.operatorperformance.program

/** Tools → Notes. V74 is the four-section page. Earlier authorities keep their existing chain. */
internal object CoachingNotes {
    const val SPRINT_HEADING = "300 m • 46 s TARGET • AVERAGE-VELOCITY REFERENCES"
    const val PERCENTAGE_NOTE =
        "Percentage repetitions use the current pain-free 300 m result, not the 46 s target. Acceleration and flying repetitions have no mandatory split."

    data class Section(val title: String, val lines: List<String>)
    data class Page(
        val intro: String?,
        val sections: List<Section>,
        val showSprintReference: Boolean,
    )

    fun page(authority: ProgramAuthority): Page = if (authority == ProgramAuthority.V74) {
        Page(
            intro = null,
            sections = listOf(
                Section("Week structure", ProgramNotesV74.weekStructure),
                Section("Progression", ProgramNotesV74.progression),
                Section("Autoregulation", ProgramNotesV74.autoregulation),
                Section("Running effort guide", ProgramNotesV74.runningEffort),
            ),
            showSprintReference = false,
        )
    } else {
        Page(
            intro = "Execution, progression, interference, sprint-volume, run-impact, and planned-overlay guidance for v${authority.versionLabel}. No performance logging required.",
            sections = listOf(
                Section("SAFETY / PARTICIPATION", v73OrV72(authority, ProgramNotesV73.safety, ProgramNotesV72.safety)),
                Section("RESISTANCE EXECUTION", v73OrV72(authority, ProgramNotesV73.resistanceExecution, ProgramNotesV72.resistanceExecution)),
                Section("RECOVERY / REVIEW", v73OrV72(authority, ProgramNotesV73.recovery, ProgramNotesV72.recovery)),
                Section("RUNNING EXECUTION", v73OrV72(authority, ProgramNotesV73.runningExecution, ProgramNotesV72.runningExecution)),
                Section("EXECUTION", tiered(authority, ProgramNotesV73.execution, ProgramNotesV71.execution, ProgramNotesV72.execution)),
                Section("RESISTANCE PROGRESSION", tiered(authority, ProgramNotesV73.resistanceProgression, ProgramNotesV71.resistanceProgression, ProgramNotesV72.resistanceProgression)),
                Section("INTERFERENCE", tiered(authority, ProgramNotesV73.interference, ProgramNotesV71.interference, ProgramNotesV72.interference)),
                Section("SPRINT VOLUME", tiered(authority, ProgramNotesV73.sprintVolume, ProgramNotesV71.sprintVolume, ProgramNotesV72.sprintVolume)),
                Section("RUN / IMPACT", tiered(authority, ProgramNotesV73.runImpact, ProgramNotesV71.runImpact, ProgramNotesV72.runImpact)),
                Section("PLANNED OVERLAYS", tiered(authority, ProgramNotesV73.plannedOverlays, ProgramNotesV71.plannedOverlays, ProgramNotesV72.plannedOverlays)),
                Section("AUTHORITY LIMITS", tiered(authority, ProgramNotesV73.authorityLimits, ProgramNotesV71.authorityLimits, ProgramNotesV72.authorityLimits)),
            ),
            showSprintReference = true,
        )
    }

    private fun v73OrV72(authority: ProgramAuthority, v73: List<String>, v72: List<String>): List<String> =
        if (authority == ProgramAuthority.V73) v73 else v72

    private fun tiered(
        authority: ProgramAuthority,
        v73: List<String>,
        v71: List<String>,
        v72: List<String>,
    ): List<String> = when (authority) {
        ProgramAuthority.V73 -> v73
        ProgramAuthority.V71 -> v71
        else -> v72
    }
}
