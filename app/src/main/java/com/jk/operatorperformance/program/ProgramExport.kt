package com.jk.operatorperformance.program

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

    private fun csv(value: String): String = "\"${value.replace("\"", "\"\"")}\""
}
