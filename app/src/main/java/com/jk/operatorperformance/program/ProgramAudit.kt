package com.jk.operatorperformance.program

object ProgramAudit {
    fun sprintVolumeStatus(meters:Int)=when {
        meters<0 -> SprintVolumeStatus.PROHIBITED
        meters<=500 -> SprintVolumeStatus.ALLOWED
        meters<=600 -> SprintVolumeStatus.WARNING
        else -> SprintVolumeStatus.PROHIBITED
    }

    fun validate():List<String> = buildList {
        if(AresProgramV70.AUTHORITY_VERSION!="7.0") add("Authority version must be 7.0.")
        if(AresProgramV70.AUTHORITY_SHA256!="0f9bd7981dc0671e836165a2b3efa70113c09cfb26e76ffc9c623ebc452dcca0") add("Authority SHA-256 mismatch.")
        if(AresProgramV70.cycles.map { it.cycle }!=(1..28).toList()) add("Exactly Cycles 1–28 must be populated.")
        if(AresProgramV70.blocks.map { it.startCycle to it.endCycle }!=listOf(1 to 7,8 to 14,15 to 21,22 to 28)) add("Phase boundaries mismatch.")
        if(AresProgramV70.dayTitles.keys!=(1..8).toSet()) add("Every cycle must contain Days 1–8.")
        if(AresProgramV70.dayTitles[4]!="Rest" || AresProgramV70.dayTitles[8]!="Rest") add("Days 4 and 8 must be rest days.")
        if(setOf(7,14,21).any { !AresProgramV70.isConsolidation(it) }) add("Consolidation cycles mismatch.")
        if(AresProgramV70.includesPlyometrics(7)||AresProgramV70.includesPlyometrics(14)||AresProgramV70.includesPlyometrics(21)||AresProgramV70.includesPlyometrics(28)) add("Plyometrics prohibited in consolidation/test cycles.")
        AresProgramV70.resistanceSlots.forEach { slot ->
            val primes=slot.roleMap.filterValues { it==MuscleRole.PRIME }.keys
            if(primes.size!=1 || primes.single()!=slot.ownerMuscle) add("${slot.id}: owner must be unique PRIME.")
            if(slot.alternatives.size>4 || slot.alternatives.map { it.rank }!=(1..slot.alternatives.size).toList()) add("${slot.id}: invalid alternatives.")
        }
        for(cycle in 1..28) {
            val plan=AresProgramV70.conditioningForCycle(cycle)
            if(plan.d1.isEmpty()||plan.speed.isEmpty()||plan.engine.isEmpty()) add("Cycle $cycle missing conditioning.")
            if(sprintVolumeStatus(plan.speed.sumOf { it.qualifyingSprintMeters })==SprintVolumeStatus.PROHIBITED) add("Cycle $cycle exceeds sprint cap.")
            val split=SessionResolver.splitForCycle(cycle)
            if(split.size!=8) add("Cycle $cycle must resolve eight days.")
        }
        if(!AresProgramV70.conditioningForCycle(1).speed.single().title.contains("baseline",true)) add("Cycle 1 baseline missing.")
        if(!AresProgramV70.conditioningForCycle(14).speed.single().title.contains("checkpoint",true)) add("Cycle 14 checkpoint missing.")
        if(!AresProgramV70.conditioningForCycle(21).engine.single().title.contains("checkpoint",true)) add("Cycle 21 checkpoint missing.")
        if(!AresProgramV70.conditioningForCycle(28).speed.single().title.contains("Final",true)) add("Cycle 28 300 m final missing.")
        if(!AresProgramV70.conditioningForCycle(28).engine.single().title.contains("Final",true)) add("Cycle 28 1.5-mile final missing.")
        if(!AresProgramV70.omitShortUpper(21)||!AresProgramV70.omitShortUpper(28)) add("Required short-upper omissions missing.")
        if(sprintVolumeStatus(500)!=SprintVolumeStatus.ALLOWED||sprintVolumeStatus(501)!=SprintVolumeStatus.WARNING||sprintVolumeStatus(601)!=SprintVolumeStatus.PROHIBITED) add("Sprint cap boundary mismatch.")
    }
}
