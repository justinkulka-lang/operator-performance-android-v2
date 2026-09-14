package com.jk.operatorperformance.program

import java.util.Locale

object ProgramExport {
    fun programMatrixCsv(): String = buildString {
        appendLine("muscle,normal_credit,normal_exposures,deload_credit,deload_exposures,normal_direct_sets,deload_direct_sets")
        ProgramAudit.matrix().forEach { row ->
            append(row.muscle.label).append(',')
            append(row.normalCredit).append(',')
            append(row.normalExposures).append(',')
            append(row.deloadCredit).append(',')
            append(row.deloadExposures).append(',')
            append(row.normalDirectSets).append(',')
            append(row.deloadDirectSets).appendLine()
        }
    }

    fun resistanceRowsCsv(): String = buildString {
        appendLine("day,slot_id,movement,dose,normal_sets,deload_sets,rest_guidance,owner_muscle,roles,ranked_alternatives")
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            val roles = slot.roleMap.entries.joinToString("|") { (muscle, role) -> "${muscle.label}:${role.creditPerSet}" }
            val alternatives = slot.alternatives.sortedBy { it.rank }.joinToString("|") { "${it.rank}:${it.name}" }
            append(slot.day).append(',')
            append(csv(slot.id)).append(',')
            append(csv(slot.movement)).append(',')
            append(csv(slot.dose)).append(',')
            append(slot.workingSets).append(',')
            append(slot.deloadSets).append(',')
            append(csv(slot.restGuidance)).append(',')
            append(csv(slot.ownerMuscle.label)).append(',')
            append(csv(roles)).append(',')
            append(csv(alternatives)).appendLine()
        }
    }

    fun programWeeksCsv(): String = buildString {
        appendLine("week,phase,greek_label,state,fractional_vector,total,balance")
        val normal = ProgramAudit.credits(false)
        val normalVector = Muscle.entries.joinToString(" / ") { formatCredit(normal.getValue(it)) }
        val normalTotal = String.format(Locale.US, "%.1f", normal.values.sum())
        val normalBalance = String.format(Locale.US, "%.3f", normal.values.max() / normal.values.min())

        for (week in 1..32) {
            val phase = OperatorProgramV56.blockForWeek(week)
            append(week).append(',')
            append(csv(phase.name)).append(',')
            append(csv(phase.godName)).append(',')
            if (week == 32) {
                val taper = week32Credits()
                val vector = Muscle.entries.joinToString(" / ") { formatCredit(taper.getValue(it)) }
                val total = String.format(Locale.US, "%.1f", taper.values.sum())
                val balance = String.format(Locale.US, "%.3f", taper.values.max() / taper.values.min())
                append("POPULATED_REALIZATION_TAPER,")
                append(csv(vector)).append(',').append(total).append(',').append(balance).appendLine()
            } else {
                append("POPULATED_NORMAL,")
                append(csv(normalVector)).append(',').append(normalTotal).append(',').append(normalBalance).appendLine()
            }
        }
    }

    fun conditioningRowsCsv(): String = buildString {
        appendLine("week,phase,selection_mode,day,segment_id,title,prescription,efforts,effort_label,rest_seconds,qualifying_sprint_meters,alternatives")
        for (week in 1..32) {
            val plan = OperatorProgramV56.conditioningForWeek(week)
            val phase = OperatorProgramV56.blockForWeek(week)
            val selectionMode = if (week in 2..8) "MANUAL_REFERENCE_DEFAULT" else "FIXED"
            plan.d1.forEach { appendConditioning(week, phase.name, selectionMode, 1, it) }
            plan.speed.forEach { appendConditioning(week, phase.name, selectionMode, 3, it) }
            plan.engine.forEach { appendConditioning(week, phase.name, selectionMode, 6, it) }
        }
    }

    fun phaseReferenceCsv(): String = buildString {
        appendLine("phase,greek_label,weeks,purpose")
        OperatorProgramV56.blocks.forEach { block ->
            append(csv(block.name)).append(',')
            append(csv(block.godName)).append(',')
            append(csv("${block.startWeek}-${block.endWeek}")).append(',')
            append(csv(block.purpose)).appendLine()
        }
    }

    private fun week32Credits(): Map<Muscle, Double> {
        val totals = Muscle.entries.associateWith { 0.0 }.toMutableMap()
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            val sets = OperatorProgramV56.week32Sets(slot.id, slot.workingSets)
            if (sets > 0) slot.roleMap.forEach { (muscle, role) ->
                totals[muscle] = totals.getValue(muscle) + sets * role.creditPerSet
            }
        }
        return Muscle.entries.associateWith { totals.getValue(it) }
    }

    private fun StringBuilder.appendConditioning(
        week: Int,
        phase: String,
        selectionMode: String,
        day: Int,
        segment: ConditioningSegment,
    ) {
        append(week).append(',')
        append(csv(phase)).append(',')
        append(selectionMode).append(',')
        append(day).append(',')
        append(csv(segment.id)).append(',')
        append(csv(segment.title)).append(',')
        append(csv(segment.prescription)).append(',')
        append(segment.effortCount).append(',')
        append(csv(segment.effortLabel)).append(',')
        append(segment.restSeconds ?: "").append(',')
        append(segment.qualifyingSprintMeters).append(',')
        append(csv(segment.alternatives.joinToString("|"))).appendLine()
    }

    private fun formatCredit(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
