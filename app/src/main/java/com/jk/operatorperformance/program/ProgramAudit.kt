package com.jk.operatorperformance.program

object ProgramAudit {
    fun sprintVolumeStatus(meters:Int)=when {
        meters<0 -> SprintVolumeStatus.PROHIBITED
        meters<=500 -> SprintVolumeStatus.ALLOWED
        meters<=600 -> SprintVolumeStatus.WARNING
        else -> SprintVolumeStatus.PROHIBITED
    }

    fun validate():List<String> = buildList {
        if(AresProgramV71.AUTHORITY_VERSION!="7.1") add("Authority version must be 7.1.")
        if(AresProgramV71.AUTHORITY_SHA256!="1d33dbca214b34dd456a971d32bc1c19d59e3a13069393429fc9e2115e3a036c") add("Authority SHA-256 mismatch.")
        if(AresProgramV71.weeks.map { it.week }!=(1..32).toList()) add("Exactly Weeks 1–32 must be populated.")
        if(AresProgramV71.blocks.map { it.startWeek to it.endWeek }!=listOf(1 to 8,9 to 16,17 to 24,25 to 32)) add("Phase boundaries mismatch.")
        if(AresProgramV71.dayTitles.keys!=(1..7).toSet()) add("Every week must contain Days 1–7.")
        if(AresProgramV71.dayTitles[7]!="Rest" || AresProgramV71.dayTitles.filterValues { it=="Rest" }.keys!=setOf(7)) add("Day 7 must be the only rest day.")
        if(setOf(8,16,24).any { !AresProgramV71.isConsolidation(it) }) add("Consolidation weeks mismatch.")
        if(setOf(8,16,24,30,31,32).any(AresProgramV71::includesPlyometrics)) add("Plyometrics prohibited in consolidation/realization/test weeks.")
        AresProgramV71.resistanceSlots.forEach { slot ->
            val primes=slot.roleMap.filterValues { it==MuscleRole.PRIME }.keys
            if(primes.size!=1 || primes.single()!=slot.ownerMuscle) add("${slot.id}: owner must be unique PRIME.")
            if(slot.alternatives.size>4 || slot.alternatives.map { it.rank }!=(1..slot.alternatives.size).toList()) add("${slot.id}: invalid alternatives.")
        }
        for(week in 1..32) {
            val plan=AresProgramV71.conditioningForWeek(week)
            if(plan.d1.isEmpty()||plan.speed.isEmpty()||plan.engine.isEmpty()) add("Week $week missing conditioning.")
            if(sprintVolumeStatus(plan.speed.sumOf { it.qualifyingSprintMeters })==SprintVolumeStatus.PROHIBITED) add("Week $week exceeds sprint cap.")
            val split=SessionResolver.splitForWeek(week)
            if(split.size!=7) add("Week $week must resolve seven days.")
            if(split.count { session -> session.rows.any { it.kind==SessionRowKind.REST } }!=1) add("Week $week must have exactly one rest day.")
        }
        if(!AresProgramV71.conditioningForWeek(1).speed.single().title.contains("baseline",true)) add("Week 1 baseline missing.")
        if(!AresProgramV71.conditioningForWeek(16).speed.single().title.contains("checkpoint",true)) add("Week 16 checkpoint missing.")
        if(!AresProgramV71.conditioningForWeek(24).engine.single().title.contains("checkpoint",true)) add("Week 24 checkpoint missing.")
        if(!AresProgramV71.conditioningForWeek(32).speed.single().title.contains("Final",true)) add("Week 32 300 m final missing.")
        if(!AresProgramV71.conditioningForWeek(32).engine.single().title.contains("Final",true)) add("Week 32 1.5-mile final missing.")
        if(!AresProgramV71.omitShortUpper(24)||!AresProgramV71.omitShortUpper(32)) add("Required short-upper omissions missing.")
        if(sprintVolumeStatus(500)!=SprintVolumeStatus.ALLOWED||sprintVolumeStatus(501)!=SprintVolumeStatus.WARNING||sprintVolumeStatus(601)!=SprintVolumeStatus.PROHIBITED) add("Sprint cap boundary mismatch.")
    }
}
