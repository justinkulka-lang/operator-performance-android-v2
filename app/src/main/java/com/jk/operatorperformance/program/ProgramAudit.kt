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
            val split=SessionResolver.splitForWeek(week, ProgramAuthority.V71)
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

    fun validateV72(): List<String> = buildList {
        if (AresProgramV72.AUTHORITY_VERSION != "7.2") add("v7.2 version mismatch.")
        if (AresProgramV72.TOTAL_WEEKS * AresProgramV72.DAYS_PER_WEEK != AresProgramV72.TOTAL_DAYS) add("224-day duration mismatch.")
        val expectedVolume = mapOf(
            1 to mapOf("total" to 104, "chest" to 11, "back" to 13, "quads" to 13, "hamstrings" to 12, "biceps" to 8, "triceps" to 6),
            2 to mapOf("total" to 100, "chest" to 11, "back" to 14, "quads" to 14, "hamstrings" to 12, "biceps" to 8, "triceps" to 6),
            3 to mapOf("total" to 88, "chest" to 10, "back" to 12, "quads" to 11, "hamstrings" to 10, "biceps" to 8, "triceps" to 6),
            4 to mapOf("total" to 71, "chest" to 8, "back" to 10, "quads" to 8, "hamstrings" to 8, "biceps" to 6, "triceps" to 6),
        )
        expectedVolume.forEach { (phase, expected) ->
            val actual = AresProgramV72.phaseVolume(phase)
            expected.forEach { (key, value) -> if (actual[key] != value) add("Phase $phase $key expected $value was ${actual[key]}.") }
        }
        if (AresProgramV72.resistanceSlots.any { it.alternatives.any { alt -> alt.name.contains("trap-bar", true) && it.movement.contains("Romanian", true) } }) {
            add("RDL alternatives must not substitute a trap-bar deadlift.")
        }
        for (week in 1..32) {
            val split = SessionResolver.splitForWeek(week, ProgramAuthority.V72)
            if (split.size != 7) add("Week $week must resolve seven v7.2 days.")
            if (split.count { session -> session.day == 7 && session.rows.any { it.kind == SessionRowKind.REST } } != 1) add("Week $week rest day mismatch.")
            if (split.any { it.authority != ProgramAuthority.V72 }) add("Week $week leaked another authority.")
            val sprint = AresProgramV72.conditioningForWeek(week).speed.sumOf { it.qualifyingSprintMeters }
            if (sprint > 600) add("Week $week exceeds 600 m.")
            val speedNote = split[3].rows.joinToString(" ") { it.note.orEmpty() }
            if (sprint == 600 && !speedNote.contains("WORKLOAD WARNING")) add("Week $week missing 600 m warning.")
            if (sprint in 1..599 && speedNote.contains("WORKLOAD WARNING")) add("Week $week warns below 600 m.")
            if (AresProgramV72.includesPlyometrics(week) != split[3].rows.any { it.id.endsWith("plyometrics") }) add("Week $week plyometric scope mismatch.")
            if (split[0].rows.none { it.title == "Cooldown" }) add("Week $week Day 1 missing Engine cooldown.")
            if (split[3].rows.none { it.title == "Cooldown" }) add("Week $week Day 4 missing Speed cooldown.")
        }
        val laterCurls = AresProgramV72.resistanceSlots.filter { it.phase > 1 && it.day == 3 && it.id.endsWith("_curl") }
        if (laterCurls.size != 3 || laterCurls.any { slot -> slot.alternatives.map { it.name } != listOf("Behind-body cable curl") }) {
            add("Later-phase Day 3 curls must keep the Phase 1 behind-body cable alternative.")
        }
        if (SessionResolver.resolve(1, 1, ProgramAuthority.V72).rows.any { it.kind == SessionRowKind.RESISTANCE }) add("Week 1 Day 1 must omit short upper.")
        if (SessionResolver.resolve(32, 2, ProgramAuthority.V72).rows.any { it.kind == SessionRowKind.RESISTANCE }) add("Week 32 Day 2 must not lift.")
        if (SessionResolver.resolve(32, 5, ProgramAuthority.V72).rows.any { it.title == "Easy aerobic" }) add("Week 32 Day 5 must omit easy aerobic.")
        if (SessionResolver.resolve(32, 6, ProgramAuthority.V72).rows.any { it.kind == SessionRowKind.RESISTANCE }) add("Week 32 Day 6 must not lift.")
        val pace = SessionResolver.resolve(13, 1, ProgramAuthority.V72).rows.first { it.title == "Goal pace" }.note.orEmpty()
        if (!pace.contains("1:39.4") || pace.contains("400 m 1:40") || pace.contains("2:30") || pace.contains("3:20") || pace.contains("4:10")) add("Goal-pace table mismatch.")
    }
}
