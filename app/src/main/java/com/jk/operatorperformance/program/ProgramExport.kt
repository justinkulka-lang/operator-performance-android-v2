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
        appendLine("day,slot_id,movement,dose,normal_sets,deload_sets,rest_seconds,owner_muscle,roles,alternative")
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            val roles = slot.roleMap.entries.joinToString("|") { (muscle, role) -> "${muscle.label}:${role.creditPerSet}" }
            append(slot.day).append(',')
            append(csv(slot.id)).append(',')
            append(csv(slot.movement)).append(',')
            append(csv(slot.dose)).append(',')
            append(slot.workingSets).append(',')
            append(slot.deloadSets).append(',')
            append(slot.restSeconds).append(',')
            append(csv(slot.ownerMuscle.label)).append(',')
            append(csv(roles)).append(',')
            append(csv(slot.alternatives.single().name)).appendLine()
        }
    }

    fun programWeeksCsv(): String = buildString {
        appendLine("week,block,state,normal_fractional_vector,total,balance")
        val normal = ProgramAudit.credits(deload = false)
        val vector = Muscle.entries.joinToString(" / ") { muscle -> formatCredit(normal.getValue(muscle)) }
        val total = String.format(Locale.US, "%.1f", normal.values.sum())
        val balance = String.format(Locale.US, "%.3f", normal.values.max() / normal.values.min())

        (1..32).forEach { week ->
            val block = OperatorProgramV56.blocks.single { week in it.startWeek..it.endWeek }
            val canonicalWeek = OperatorProgramV56.weekOrNull(week)
            val blockLabel = block.name.substringBefore(" —")
            append(week).append(',').append(blockLabel).append(',')
            when (block.status) {
                BlockStatus.POPULATED -> {
                    requireNotNull(canonicalWeek) { "Populated block is missing canonical Week $week." }
                    append("POPULATED,")
                    append(csv(vector)).append(',')
                    append(total).append(',')
                    append(balance).appendLine()
                }
                BlockStatus.UNPOPULATED_NOT_PROGRAMMED -> {
                    require(canonicalWeek == null) { "Unpopulated Week $week must not have a canonical definition." }
                    append("UNPOPULATED,—,—,—").appendLine()
                }
            }
        }
    }

    fun conditioningRowsCsv(): String = buildString {
        appendLine("week,day,segment_id,title,prescription,efforts,effort_label,rest_seconds,qualifying_sprint_meters,alternative")
        OperatorProgramV56.conditioningByWeek.forEach { week ->
            week.speed.forEach { segment -> appendConditioning(week.week, 3, segment) }
            week.engine.forEach { segment -> appendConditioning(week.week, 6, segment) }
        }
    }

    private fun StringBuilder.appendConditioning(week: Int, day: Int, segment: ConditioningSegment) {
        append(week).append(',').append(day).append(',')
        append(csv(segment.id)).append(',')
        append(csv(segment.title)).append(',')
        append(csv(segment.prescription)).append(',')
        append(segment.effortCount).append(',')
        append(csv(segment.effortLabel)).append(',')
        append(segment.restSeconds ?: "").append(',')
        append(segment.qualifyingSprintMeters).append(',')
        append(csv(segment.alternative.orEmpty())).appendLine()
    }

    private fun formatCredit(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
