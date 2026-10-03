package com.jk.operatorperformance.program

object ProgramExport {
    fun resistanceRowsCsv():String=buildString {
        appendLine("phase,day,slot_id,movement,dose,working_sets,rest_guidance,owner_muscle,roles,ranked_alternatives")
        ProgramV72.resistanceSlots.forEach { s ->
            append(s.phase).append(',').append(s.day).append(',').append(csv(s.id)).append(',').append(csv(s.movement)).append(',').append(csv(s.dose)).append(',')
                .append(s.workingSets).append(',').append(csv(s.restGuidance)).append(',').append(csv(s.ownerMuscle.label)).append(',')
                .append(csv(s.roleMap.entries.joinToString("|") { "${it.key.label}:${it.value.creditPerSet}" })).append(',')
                .append(csv(s.alternatives.joinToString("|") { "${it.rank}:${it.name}" })).appendLine()
        }
    }
    fun programWeeksCsv():String=buildString {
        appendLine("week,phase,state,days")
        for(week in 1..32) {
            val phase=ProgramV72.blockForWeek(week)
            val state=when { week in setOf(8,16,24)->"PLANNED_CONSOLIDATION"; week in setOf(30,31)->"PLANNED_REALIZATION"; week==32->"FIXED_PRESCRIPTION"; else->"POPULATED_NORMAL" }
            append(week).append(',').append(csv(phase.name)).append(',').append(state).append(",7").appendLine()
        }
    }
    fun conditioningRowsCsv():String=buildString {
        appendLine("week,phase,day,segment_id,title,prescription,efforts,effort_label,qualifying_sprint_meters,alternatives")
        for(week in 1..32) {
            val p=ProgramV72.conditioningForWeek(week); val phase=ProgramV72.blockForWeek(week).name
            p.engine.forEach { appendConditioning(week,phase,1,it) }; p.speed.forEach { appendConditioning(week,phase,4,it) }; p.d1.forEach { appendConditioning(week,phase,5,it) }
        }
    }
    fun phaseReferenceCsv():String=buildString {
        appendLine("phase,greek_label,weeks,purpose")
        ProgramV72.blocks.forEach { b -> append(csv(b.name)).append(',').append(csv(b.godName)).append(',').append(csv("${b.startWeek}-${b.endWeek}")).append(',').append(csv(b.purpose)).appendLine() }
    }
    private fun StringBuilder.appendConditioning(week:Int,phase:String,day:Int,s:ConditioningSegment) {
        append(week).append(',').append(csv(phase)).append(',').append(day).append(',').append(csv(s.id)).append(',').append(csv(s.title)).append(',').append(csv(s.prescription)).append(',').append(s.effortCount).append(',').append(csv(s.effortLabel)).append(',').append(s.qualifyingSprintMeters).append(',').append(csv(s.alternatives.joinToString("|"))).appendLine()
    }
    private fun csv(v:String)="\"${v.replace("\"","\"\"")}\""
}
