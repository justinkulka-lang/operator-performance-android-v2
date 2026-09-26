package com.jk.operatorperformance.program

object ProgramExport {
    fun resistanceRowsCsv():String=buildString {
        appendLine("phase,day,slot_id,movement,dose,working_sets,rest_guidance,owner_muscle,roles,ranked_alternatives")
        AresProgramV70.resistanceSlots.forEach { s ->
            append(s.phase).append(',').append(s.day).append(',').append(csv(s.id)).append(',').append(csv(s.movement)).append(',').append(csv(s.dose)).append(',')
                .append(s.workingSets).append(',').append(csv(s.restGuidance)).append(',').append(csv(s.ownerMuscle.label)).append(',')
                .append(csv(s.roleMap.entries.joinToString("|") { "${it.key.label}:${it.value.creditPerSet}" })).append(',')
                .append(csv(s.alternatives.joinToString("|") { "${it.rank}:${it.name}" })).appendLine()
        }
    }
    fun programCyclesCsv():String=buildString {
        appendLine("cycle,phase,state,days")
        for(cycle in 1..28) {
            val phase=AresProgramV70.blockForCycle(cycle)
            val state=when { cycle in setOf(7,14,21)->"PLANNED_CONSOLIDATION"; cycle in setOf(26,27)->"PLANNED_PEAK_REDUCTION"; cycle==28->"FINAL_REALIZATION"; else->"POPULATED_NORMAL" }
            append(cycle).append(',').append(csv(phase.name)).append(',').append(state).append(",8").appendLine()
        }
    }
    /** Compatibility entry point; content is cycle-based in v7.0. */
    fun programWeeksCsv():String=programCyclesCsv()
    fun conditioningRowsCsv():String=buildString {
        appendLine("cycle,phase,day,segment_id,title,prescription,efforts,effort_label,qualifying_sprint_meters,alternatives")
        for(cycle in 1..28) {
            val p=AresProgramV70.conditioningForCycle(cycle); val phase=AresProgramV70.blockForCycle(cycle).name
            p.d1.forEach { appendConditioning(cycle,phase,1,it) }; p.speed.forEach { appendConditioning(cycle,phase,3,it) }; p.engine.forEach { appendConditioning(cycle,phase,7,it) }
        }
    }
    fun phaseReferenceCsv():String=buildString {
        appendLine("phase,greek_label,cycles,purpose")
        AresProgramV70.blocks.forEach { b -> append(csv(b.name)).append(',').append(csv(b.godName)).append(',').append(csv("${b.startCycle}-${b.endCycle}")).append(',').append(csv(b.purpose)).appendLine() }
    }
    private fun StringBuilder.appendConditioning(cycle:Int,phase:String,day:Int,s:ConditioningSegment) {
        append(cycle).append(',').append(csv(phase)).append(',').append(day).append(',').append(csv(s.id)).append(',').append(csv(s.title)).append(',').append(csv(s.prescription)).append(',').append(s.effortCount).append(',').append(csv(s.effortLabel)).append(',').append(s.qualifyingSprintMeters).append(',').append(csv(s.alternatives.joinToString("|"))).appendLine()
    }
    private fun csv(v:String)="\"${v.replace("\"","\"\"")}\""
}
