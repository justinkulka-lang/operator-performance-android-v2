package com.jk.operatorperformance.program

import kotlin.math.abs
import kotlin.math.round

object ProgramAudit {
    private val expectedNormalCredits = linkedMapOf(
        Muscle.QUADS to 13.0,
        Muscle.GLUTES to 11.5,
        Muscle.HAMSTRINGS to 11.5,
        Muscle.CHEST to 12.0,
        Muscle.LATS to 12.0,
        Muscle.UPPER_BACK to 11.0,
        Muscle.DELTS to 15.0,
        Muscle.BICEPS to 11.5,
        Muscle.TRICEPS to 12.0,
        Muscle.CALVES to 10.0,
        Muscle.TRAPS to 9.0,
        Muscle.FOREARMS to 9.5,
        Muscle.ABS to 10.0,
    )

    private val expectedDeloadCredits = linkedMapOf(
        Muscle.QUADS to 10.0,
        Muscle.GLUTES to 10.0,
        Muscle.HAMSTRINGS to 10.0,
        Muscle.CHEST to 10.0,
        Muscle.LATS to 10.0,
        Muscle.UPPER_BACK to 10.0,
        Muscle.DELTS to 10.0,
        Muscle.BICEPS to 9.0,
        Muscle.TRICEPS to 9.0,
        Muscle.CALVES to 8.0,
        Muscle.TRAPS to 7.5,
        Muscle.FOREARMS to 6.0,
        Muscle.ABS to 8.0,
    )

    private val expectedNormalExposures = linkedMapOf(
        Muscle.QUADS to 2,
        Muscle.GLUTES to 2,
        Muscle.HAMSTRINGS to 2,
        Muscle.CHEST to 2,
        Muscle.LATS to 2,
        Muscle.UPPER_BACK to 3,
        Muscle.DELTS to 3,
        Muscle.BICEPS to 2,
        Muscle.TRICEPS to 3,
        Muscle.CALVES to 3,
        Muscle.TRAPS to 3,
        Muscle.FOREARMS to 3,
        Muscle.ABS to 4,
    )

    private val expectedDeloadExposures = linkedMapOf(
        Muscle.QUADS to 2,
        Muscle.GLUTES to 2,
        Muscle.HAMSTRINGS to 2,
        Muscle.CHEST to 2,
        Muscle.LATS to 2,
        Muscle.UPPER_BACK to 3,
        Muscle.DELTS to 3,
        Muscle.BICEPS to 2,
        Muscle.TRICEPS to 3,
        Muscle.CALVES to 2,
        Muscle.TRAPS to 3,
        Muscle.FOREARMS to 2,
        Muscle.ABS to 3,
    )

    private val fractionalFloors = mapOf(
        Muscle.QUADS to 10.0,
        Muscle.GLUTES to 10.0,
        Muscle.HAMSTRINGS to 10.0,
        Muscle.CHEST to 10.0,
        Muscle.LATS to 10.0,
        Muscle.UPPER_BACK to 10.0,
        Muscle.DELTS to 10.0,
        Muscle.BICEPS to 9.0,
        Muscle.TRICEPS to 9.0,
        Muscle.CALVES to 8.0,
    )

    private val directFloors = mapOf(
        Muscle.TRAPS to 6,
        Muscle.FOREARMS to 4,
        Muscle.ABS to 8,
    )

    fun credits(deload: Boolean): Map<Muscle, Double> {
        val totals = Muscle.entries.associateWith { 0.0 }.toMutableMap()
        OperatorProgramV54.resistanceSlots.forEach { slot ->
            val sets = if (deload) slot.deloadSets else slot.workingSets
            if (sets > 0) {
                slot.roleMap.forEach { (muscle, role) ->
                    totals[muscle] = totals.getValue(muscle) + sets * role.creditPerSet
                }
            }
        }
        return Muscle.entries.associateWith { totals.getValue(it) }
    }

    fun exposures(deload: Boolean): Map<Muscle, Int> {
        val days = Muscle.entries.associateWith { linkedSetOf<Int>() }.toMutableMap()
        OperatorProgramV54.resistanceSlots.forEach { slot ->
            val sets = if (deload) slot.deloadSets else slot.workingSets
            if (sets > 0) slot.roleMap.keys.forEach { muscle -> days.getValue(muscle).add(slot.day) }
        }
        return Muscle.entries.associateWith { days.getValue(it).size }
    }

    fun directSets(deload: Boolean): Map<Muscle, Int> {
        val totals = Muscle.entries.associateWith { 0 }.toMutableMap()
        OperatorProgramV54.resistanceSlots.forEach { slot ->
            val sets = if (deload) slot.deloadSets else slot.workingSets
            if (sets > 0) totals[slot.ownerMuscle] = totals.getValue(slot.ownerMuscle) + sets
        }
        return Muscle.entries.associateWith { totals.getValue(it) }
    }

    fun matrix(): List<ProgramMatrixRow> {
        val normal = credits(deload = false)
        val deload = credits(deload = true)
        val normalExposures = exposures(deload = false)
        val deloadExposures = exposures(deload = true)
        val normalDirect = directSets(deload = false)
        val deloadDirect = directSets(deload = true)
        return Muscle.entries.map { muscle ->
            ProgramMatrixRow(
                muscle = muscle,
                normalCredit = normal.getValue(muscle),
                normalExposures = normalExposures.getValue(muscle),
                deloadCredit = deload.getValue(muscle),
                deloadExposures = deloadExposures.getValue(muscle),
                normalDirectSets = normalDirect.getValue(muscle),
                deloadDirectSets = deloadDirect.getValue(muscle),
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
        val normal = credits(deload = false)
        val deload = credits(deload = true)
        val normalExposures = exposures(deload = false)
        val deloadExposures = exposures(deload = true)
        val normalDirect = directSets(deload = false)
        val deloadDirect = directSets(deload = true)

        if (Muscle.entries.size != 13) errors += "Canonical muscle count must be 13."
        if (normal != expectedNormalCredits) errors += "Normal fractional-credit vector does not match v5.4. Actual=$normal"
        if (deload != expectedDeloadCredits) errors += "Deload fractional-credit vector does not match v5.4. Actual=$deload"
        if (normalExposures != expectedNormalExposures) errors += "Normal exposure vector does not match v5.4. Actual=$normalExposures"
        if (deloadExposures != expectedDeloadExposures) errors += "Deload exposure vector does not match v5.4. Actual=$deloadExposures"

        val normalTotal = normal.values.sum()
        val deloadTotal = deload.values.sum()
        if (abs(normalTotal - 148.0) > 1e-9) errors += "Normal total must equal 148.0, got $normalTotal"
        if (abs(deloadTotal - 117.5) > 1e-9) errors += "Deload total must equal 117.5, got $deloadTotal"

        val reduction = (normalTotal - deloadTotal) / normalTotal * 100.0
        if (abs(round(reduction * 10.0) / 10.0 - 20.6) > 1e-9) errors += "Deload reduction must round to 20.6%, got $reduction"

        val normalBalance = normal.values.max() / normal.values.min()
        val deloadBalance = deload.values.max() / deload.values.min()
        if (normalBalance > 1.75) errors += "Normal balance exceeds 1.75: $normalBalance"
        if (deloadBalance > 1.75) errors += "Deload balance exceeds 1.75: $deloadBalance"

        fractionalFloors.forEach { (muscle, floor) ->
            if (normal.getValue(muscle) < floor) errors += "Normal floor failed for ${muscle.label}."
            if (deload.getValue(muscle) < floor) errors += "Deload floor failed for ${muscle.label}."
        }
        directFloors.forEach { (muscle, floor) ->
            if (normalDirect.getValue(muscle) < floor) errors += "Normal direct-set floor failed for ${muscle.label}."
            if (deloadDirect.getValue(muscle) < floor) errors += "Deload direct-set floor failed for ${muscle.label}."
        }

        OperatorProgramV54.resistanceSlots.forEach { slot ->
            val primeMuscles = slot.roleMap.filterValues { it == MuscleRole.PRIME }.keys
            if (primeMuscles.size != 1 || primeMuscles.singleOrNull() != slot.ownerMuscle) {
                errors += "${slot.id}: ownerMuscle must be the unique PRIME muscle."
            }
            if (slot.alternatives.size !in 2..4) errors += "${slot.id}: alternatives must contain 2–4 ranked choices."
            if (slot.alternatives.map { it.rank } != (1..slot.alternatives.size).toList()) errors += "${slot.id}: alternative ranks are not contiguous from 1."
            if (slot.workingSets <= 0) errors += "${slot.id}: normal working sets must be positive."
            if (slot.deloadSets !in 0..slot.workingSets) errors += "${slot.id}: invalid deload set count."
            val text = buildString {
                append(slot.movement)
                slot.alternatives.forEach { append(' ').append(it.name) }
            }
            if (text.contains("sled", ignoreCase = true)) errors += "${slot.id}: sled reference is prohibited in resistance program data."
        }

        if (OperatorProgramV54.weeks.map { it.week } != (1..8).toList()) errors += "Exactly Weeks 1–8 must be populated."
        if ((9..32).any { OperatorProgramV54.weekOrNull(it) != null }) errors += "Weeks 9–32 must remain unpopulated."
        if (!OperatorProgramV54.weeks.first().speedBaseline || !OperatorProgramV54.weeks.first().engineBaseline) errors += "Week 1 must be the speed/engine baseline microcycle."
        if (OperatorProgramV54.weeks.drop(1).any { it.speedBaseline || it.engineBaseline }) errors += "Weeks 2–8 must use manually selected post-baseline references, not baseline sessions."
        if (OperatorProgramV54.firstPostBaselineSpeedDefault != SpeedVariant.ACCELERATION) errors += "First post-baseline Speed display must default to ACCELERATION."
        if (OperatorProgramV54.firstPostBaselineEngineDefault != EngineVariant.THRESHOLD) errors += "First post-baseline Engine display must default to THRESHOLD."

        val finisher = OperatorProgramV54.machinePowerFinisher
        if (finisher.efforts != 6 || finisher.hardSeconds != 10 || finisher.easySeconds != 80) errors += "Machine-power finisher must be 6 × 10 s hard / 80 s easy."
        val expectedModalities = listOf(MachineModality.AIR_FAN_BIKE, MachineModality.STATIONARY_CYCLE, MachineModality.ROW_ERG, MachineModality.SKI_ERG)
        if (finisher.rankedModalities != expectedModalities) errors += "Machine-power equipment hierarchy does not match v5.4."

        OperatorProgramV54.speedOptions.forEach { option ->
            option.qualifyingSprintMeters.forEach { meters ->
                if (sprintVolumeStatus(meters) == SprintVolumeStatus.PROHIBITED) errors += "${option.variant}: built-in sprint option exceeds 600 m."
            }
            val shouldHaveFinisher = option.variant == SpeedVariant.ACCELERATION || option.variant == SpeedVariant.MAX_VELOCITY
            if (option.includesMachinePowerFinisher != shouldHaveFinisher) errors += "${option.variant}: machine-power finisher eligibility is incorrect."
        }
        if (sprintVolumeStatus(500) != SprintVolumeStatus.ALLOWED) errors += "500 m must be allowed."
        if (sprintVolumeStatus(501) != SprintVolumeStatus.WARNING) errors += ">500 m through 600 m must warn."
        if (sprintVolumeStatus(600) != SprintVolumeStatus.WARNING) errors += "600 m must pass with warning."
        if (sprintVolumeStatus(601) != SprintVolumeStatus.PROHIBITED) errors += ">600 m must be prohibited."

        return errors
    }
}
