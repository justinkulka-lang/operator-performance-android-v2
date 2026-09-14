package com.jk.operatorperformance.program

import kotlin.math.abs
import kotlin.math.round

object ProgramAudit {
    val expectedNormalCredits = linkedMapOf(
        Muscle.QUADS to 13.0, Muscle.GLUTES to 11.5, Muscle.HAMSTRINGS to 11.5,
        Muscle.CHEST to 12.0, Muscle.LATS to 12.0, Muscle.UPPER_BACK to 11.0,
        Muscle.DELTS to 15.0, Muscle.BICEPS to 11.5, Muscle.TRICEPS to 12.0,
        Muscle.CALVES to 10.0, Muscle.TRAPS to 9.0, Muscle.FOREARMS to 9.5, Muscle.ABS to 10.0,
    )

    val expectedDeloadCredits = linkedMapOf(
        Muscle.QUADS to 10.0, Muscle.GLUTES to 10.0, Muscle.HAMSTRINGS to 10.0,
        Muscle.CHEST to 10.0, Muscle.LATS to 10.0, Muscle.UPPER_BACK to 10.0,
        Muscle.DELTS to 10.0, Muscle.BICEPS to 9.0, Muscle.TRICEPS to 9.0,
        Muscle.CALVES to 8.0, Muscle.TRAPS to 7.5, Muscle.FOREARMS to 6.0, Muscle.ABS to 8.0,
    )

    private val expectedNormalExposures = linkedMapOf(
        Muscle.QUADS to 2, Muscle.GLUTES to 2, Muscle.HAMSTRINGS to 2,
        Muscle.CHEST to 2, Muscle.LATS to 2, Muscle.UPPER_BACK to 3,
        Muscle.DELTS to 3, Muscle.BICEPS to 2, Muscle.TRICEPS to 3,
        Muscle.CALVES to 3, Muscle.TRAPS to 3, Muscle.FOREARMS to 3, Muscle.ABS to 4,
    )

    private val expectedDeloadExposures = linkedMapOf(
        Muscle.QUADS to 2, Muscle.GLUTES to 2, Muscle.HAMSTRINGS to 2,
        Muscle.CHEST to 2, Muscle.LATS to 2, Muscle.UPPER_BACK to 3,
        Muscle.DELTS to 3, Muscle.BICEPS to 2, Muscle.TRICEPS to 3,
        Muscle.CALVES to 2, Muscle.TRAPS to 3, Muscle.FOREARMS to 2, Muscle.ABS to 3,
    )

    private val fractionalFloors = linkedMapOf(
        Muscle.QUADS to 10.0, Muscle.GLUTES to 10.0, Muscle.HAMSTRINGS to 10.0,
        Muscle.CHEST to 10.0, Muscle.LATS to 10.0, Muscle.UPPER_BACK to 10.0,
        Muscle.DELTS to 10.0, Muscle.BICEPS to 9.0, Muscle.TRICEPS to 9.0,
        Muscle.CALVES to 8.0,
    )

    private val directFloors = linkedMapOf(
        Muscle.TRAPS to 6,
        Muscle.FOREARMS to 4,
        Muscle.ABS to 8,
    )

    private val week32SetOverrides = linkedMapOf(
        "d1_squat" to 2, "d1_rdl" to 1, "d1_leg_press" to 1, "d1_leg_curl" to 1, "d1_calf" to 2, "d1_abs" to 1,
        "d5_deadlift" to 2, "d5_hip" to 1, "d5_bss" to 1, "d5_extension" to 1, "d5_calf" to 2, "d5_abs" to 1, "d5_wrist" to 1,
        "d6_incline" to 1, "d6_fly" to 1, "d6_pushdown" to 1, "d6_wrist" to 1, "d6_calf" to 1, "d6_abs" to 1,
    )

    fun credits(deload: Boolean): Map<Muscle, Double> {
        val totals = Muscle.entries.associateWith { 0.0 }.toMutableMap()
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            val sets = if (deload) slot.deloadSets else slot.workingSets
            if (sets > 0) slot.roleMap.forEach { (muscle, role) ->
                totals[muscle] = totals.getValue(muscle) + sets * role.creditPerSet
            }
        }
        return Muscle.entries.associateWith { totals.getValue(it) }
    }

    fun exposures(deload: Boolean): Map<Muscle, Int> {
        val days = Muscle.entries.associateWith { linkedSetOf<Int>() }.toMutableMap()
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            val sets = if (deload) slot.deloadSets else slot.workingSets
            if (sets > 0) slot.roleMap.keys.forEach { days.getValue(it).add(slot.day) }
        }
        return Muscle.entries.associateWith { days.getValue(it).size }
    }

    fun directSets(deload: Boolean): Map<Muscle, Int> {
        val totals = Muscle.entries.associateWith { 0 }.toMutableMap()
        OperatorProgramV56.resistanceSlots.forEach { slot ->
            val sets = if (deload) slot.deloadSets else slot.workingSets
            if (sets > 0) totals[slot.ownerMuscle] = totals.getValue(slot.ownerMuscle) + sets
        }
        return Muscle.entries.associateWith { totals.getValue(it) }
    }

    fun matrix(): List<ProgramMatrixRow> {
        val normal = credits(false)
        val deload = credits(true)
        val normalExposures = exposures(false)
        val deloadExposures = exposures(true)
        val normalDirect = directSets(false)
        val deloadDirect = directSets(true)
        return Muscle.entries.map { muscle ->
            ProgramMatrixRow(
                muscle, normal.getValue(muscle), normalExposures.getValue(muscle),
                deload.getValue(muscle), deloadExposures.getValue(muscle),
                normalDirect.getValue(muscle), deloadDirect.getValue(muscle),
            )
        }
    }

    fun sprintVolumeStatus(meters: Int): SprintVolumeStatus = when {
        meters < 0 -> SprintVolumeStatus.PROHIBITED
        meters <= 500 -> SprintVolumeStatus.ALLOWED
        meters <= 600 -> SprintVolumeStatus.WARNING
        else -> SprintVolumeStatus.PROHIBITED
    }

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        val normal = credits(false)
        val deload = credits(true)
        val normalExposures = exposures(false)
        val deloadExposures = exposures(true)
        val normalDirect = directSets(false)
        val deloadDirect = directSets(true)

        if (OperatorProgramV56.AUTHORITY_VERSION != "5.7") errors += "Authority version must be 5.7."
        if (OperatorProgramV56.AUTHORITY_SHA256 != "867c468ba616a22185b871c649d8c026be5c1c61b3a7f690edac5b6169fc3d0a") {
            errors += "Authority SHA-256 does not match approved v5.7."
        }
        if (OperatorProgramV56.weeks.map { it.week } != (1..32).toList()) errors += "Exactly Weeks 1–32 must be populated."
        if (OperatorProgramV56.blocks.size != 4 || OperatorProgramV56.blocks.any { it.status != BlockStatus.POPULATED }) {
            errors += "All four phases must be populated."
        }
        if (OperatorProgramV56.blocks.map { it.godName } != listOf("HEPHAESTUS", "HERMES", "ARES", "ZEUS")) {
            errors += "Greek phase labels do not match the approved ARES display mapping."
        }
        if (normal != expectedNormalCredits) errors += "Normal fractional-credit vector mismatch: $normal"
        if (deload != expectedDeloadCredits) errors += "Deload fractional-credit vector mismatch: $deload"
        if (normalExposures != expectedNormalExposures) errors += "Normal exposure vector mismatch: $normalExposures"
        if (deloadExposures != expectedDeloadExposures) errors += "Deload exposure vector mismatch: $deloadExposures"

        fractionalFloors.forEach { (muscle, floor) ->
            if (normal.getValue(muscle) < floor) errors += "Normal fractional floor failed for ${muscle.label}."
            if (deload.getValue(muscle) < floor) errors += "Deload fractional floor failed for ${muscle.label}."
        }
        directFloors.forEach { (muscle, floor) ->
            if (normalDirect.getValue(muscle) < floor) errors += "Normal direct-set floor failed for ${muscle.label}."
            if (deloadDirect.getValue(muscle) < floor) errors += "Deload direct-set floor failed for ${muscle.label}."
        }
        Muscle.entries.forEach { muscle ->
            if (normalExposures.getValue(muscle) < 2) errors += "Normal exposure minimum failed for ${muscle.label}."
            if (deloadExposures.getValue(muscle) < 2) errors += "Deload exposure minimum failed for ${muscle.label}."
        }

        val normalTotal = normal.values.sum()
        val deloadTotal = deload.values.sum()
        if (abs(normalTotal - 148.0) > 1e-9) errors += "Normal total must equal 148.0, got $normalTotal"
        if (abs(deloadTotal - 117.5) > 1e-9) errors += "Deload total must equal 117.5, got $deloadTotal"
        val reduction = (normalTotal - deloadTotal) / normalTotal * 100.0
        if (abs(round(reduction * 10.0) / 10.0 - 20.6) > 1e-9) errors += "Deload reduction must round to 20.6%, got $reduction"
        if (normal.values.max() / normal.values.min() > 1.75) errors += "Normal balance exceeds 1.75."
        if (deload.values.max() / deload.values.min() > 1.75) errors += "Deload balance exceeds 1.75."

        OperatorProgramV56.resistanceSlots.forEach { slot ->
            val primes = slot.roleMap.filterValues { it == MuscleRole.PRIME }.keys
            if (primes.size != 1 || primes.single() != slot.ownerMuscle) errors += "${slot.id}: owner muscle must be unique PRIME."
            if (slot.alternatives.size !in 2..4) errors += "${slot.id}: requires 2–4 ranked alternatives."
            if (slot.alternatives.map { it.rank } != (1..slot.alternatives.size).toList()) errors += "${slot.id}: alternative ranks invalid."
            if ((slot.movement + slot.alternatives.joinToString { it.name }).contains("sled", true)) errors += "${slot.id}: sled reference prohibited."
        }

        for (week in 1..32) {
            val plan = OperatorProgramV56.conditioningForWeek(week)
            if (plan.d1.isEmpty() || plan.speed.isEmpty() || plan.engine.isEmpty()) errors += "Week $week missing D1/D3/D6 conditioning."
            val meters = plan.speed.sumOf { it.qualifyingSprintMeters }
            if (sprintVolumeStatus(meters) == SprintVolumeStatus.PROHIBITED) errors += "Week $week exceeds 600 m sprint cap: $meters"
        }

        if (OperatorProgramV56.conditioningForWeek(1).speed.single().title != "300 M BASELINE") errors += "Week 1 D3 baseline missing."
        if (OperatorProgramV56.conditioningForWeek(1).engine.single().title != "1.5-MILE BASELINE") errors += "Week 1 D6 baseline missing."
        if (OperatorProgramV56.conditioningForWeek(16).speed.single().title != "300 M CHECKPOINT") errors += "Week 16 checkpoint missing."
        if (OperatorProgramV56.conditioningForWeek(24).engine.single().title != "1.5-MILE CHECKPOINT") errors += "Week 24 checkpoint missing."
        if (OperatorProgramV56.conditioningForWeek(32).speed.single().title != "FINAL 300 M TEST") errors += "Week 32 300 m final missing."
        if (OperatorProgramV56.conditioningForWeek(32).engine.single().title != "FINAL 1.5-MILE TEST") errors += "Week 32 1.5-mile final missing."

        val taperActual = week32SetOverrides.mapValues { (slotId, _) ->
            val slot = OperatorProgramV56.resistanceSlots.singleOrNull { it.id == slotId }
            if (slot == null) -1 else OperatorProgramV56.week32Sets(slotId, slot.workingSets)
        }
        if (taperActual != week32SetOverrides) errors += "Week 32 reduced-set prescription mismatch: $taperActual"
        OperatorProgramV56.resistanceSlots
            .filter { it.day in setOf(2, 4) }
            .forEach { slot ->
                if (OperatorProgramV56.week32Sets(slot.id, slot.workingSets) != slot.workingSets) {
                    errors += "Week 32 ${slot.id}: D2/D4 must retain normal approved sets."
                }
            }
        if (OperatorProgramV56.week32RirFloor(1) != 3 || OperatorProgramV56.week32RirFloor(2) != 1 ||
            OperatorProgramV56.week32RirFloor(4) != 1 || OperatorProgramV56.week32RirFloor(5) != 3 ||
            OperatorProgramV56.week32RirFloor(6) != 2
        ) errors += "Week 32 RIR floors mismatch."

        val finisher = OperatorProgramV56.machinePowerFinisher
        if (finisher.efforts != 6 || finisher.hardSeconds != 10 || finisher.easySeconds != 80) errors += "Finisher must be 6×10/80."
        if (finisher.hierarchy != listOf(MachineModality.AIR_FAN_BIKE, MachineModality.STATIONARY_CYCLE, MachineModality.ROW_ERG, MachineModality.SKI_ERG)) {
            errors += "Finisher equipment hierarchy mismatch."
        }
        if (sprintVolumeStatus(500) != SprintVolumeStatus.ALLOWED || sprintVolumeStatus(501) != SprintVolumeStatus.WARNING ||
            sprintVolumeStatus(600) != SprintVolumeStatus.WARNING || sprintVolumeStatus(601) != SprintVolumeStatus.PROHIBITED
        ) errors += "Sprint cap boundary mismatch."

        return errors
    }
}
