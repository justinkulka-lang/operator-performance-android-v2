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
        appendLine("day,slot_id,movement,dose,normal_sets,deload_sets,owner_muscle,roles,alternatives")
        OperatorProgramV54.resistanceSlots.forEach { slot ->
            val roles = slot.roleMap.entries.joinToString("|") { (muscle, role) -> "${muscle.label}:${role.creditPerSet}" }
            val alternatives = slot.alternatives.joinToString("|") { "${it.rank}:${it.name}" }
            append(slot.day).append(',')
            append(csv(slot.id)).append(',')
            append(csv(slot.movement)).append(',')
            append(csv(slot.dose)).append(',')
            append(slot.workingSets).append(',')
            append(slot.deloadSets).append(',')
            append(csv(slot.ownerMuscle.label)).append(',')
            append(csv(roles)).append(',')
            append(csv(alternatives)).appendLine()
        }
    }

    fun programWeeksCsv(): String = buildString {
        appendLine("week,block,state,normal_fractional_vector,total,balance")
        val normal = ProgramAudit.credits(deload = false)
        val vector = Muscle.entries.joinToString(" / ") { muscle -> formatCredit(normal.getValue(muscle)) }
        val total = String.format(Locale.US, "%.1f", normal.values.sum())
        val balance = String.format(Locale.US, "%.3f", normal.values.max() / normal.values.min())

        (1..32).forEach { week ->
            val block = OperatorProgramV54.blocks.single { week in it.startWeek..it.endWeek }
            val canonicalWeek = OperatorProgramV54.weekOrNull(week)
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

    private fun formatCredit(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
