package com.jk.operatorperformance.program

data class V74Session(
    val block: Int,
    val blockWeek: Int,
    val day: Int,
    val title: String,
    val rows: List<SessionRow>,
    val globalLine: String,
)

object SessionResolverV74 {
    fun resolve(block: Int, blockWeek: Int, day: Int): V74Session {
        require(block in 1..4) { "Block must be 1–4." }
        require(blockWeek in 1..8) { "Block week must be 1–8." }
        require(day in 1..7) { "Day must be 1–7." }
        val rows = when (day) {
            1 -> engineDay(block, blockWeek)
            2, 3, 6 -> resistanceDay(block, blockWeek, day)
            4 -> speedDay(block, blockWeek) + resistanceDay(block, blockWeek, day)
            5 -> resistanceDay(block, blockWeek, day) + easyAerobic(block, blockWeek)
            7 -> listOf(restRow())
            else -> error("Unreachable day")
        }
        return V74Session(block, blockWeek, day, ProgramV74.dayTitles.getValue(day), rows, ProgramV74.GLOBAL_LINE)
    }

    private fun engineDay(block: Int, blockWeek: Int): List<SessionRow> {
        val easyOnly = blockWeek == 8
        val run = ProgramV74.runWeek(blockWeek)
        return listOf(
            SessionRow(
                id = id(block, blockWeek, "d1_warmup"),
                title = if (easyOnly) "Easy-run opening" else "Engine warm-up",
                prescription = if (easyOnly) ProgramV72.engineEasyWarmup else ProgramV72.engineQualityWarmup,
                kind = SessionRowKind.CONDITIONING,
                // The v7.3 quality-warm-up note is the single sentence
                // "Complete before threshold conditioning, intervals, goal-pace, or timed work."
                // That sentence is removed. The easy-run note has no removed item and stays.
                note = if (easyOnly) "Included in the listed easy run. No strides. The 8–10 min cooldown still follows the run." else null,
                effortLabel = "WARM-UP",
            ),
            SessionRow(
                id = id(block, blockWeek, "d1_engine"),
                title = "Engine",
                prescription = run.engine,
                kind = SessionRowKind.CONDITIONING,
                note = run.engineEffort,
                effortCount = effortCount(run.engine),
                effortLabel = effortLabel(run.engine),
            ),
            cooldown(block, blockWeek, "d1_cooldown"),
        )
    }

    private fun speedDay(block: Int, blockWeek: Int): List<SessionRow> {
        val run = ProgramV74.runWeek(blockWeek)
        return buildList {
            add(SessionRow(
                id = id(block, blockWeek, "d4_speed_warmup"),
                title = "Speed warm-up",
                prescription = ProgramV72.speedWarmup,
                kind = SessionRowKind.CONDITIONING,
                note = "Build-ups do not count toward quality-sprint volume.",
                effortLabel = "WARM-UP",
            ))
            if (run.jumps) add(jumps(block, blockWeek))
            if (run.medicineBallThrows) add(throwsRow(block, blockWeek))
            add(SessionRow(
                id = id(block, blockWeek, "d4_speed"),
                title = "Speed",
                prescription = run.speed,
                kind = SessionRowKind.CONDITIONING,
                note = ProgramV74.EFFORT_SPEED,
                effortCount = effortCount(run.speed),
                effortLabel = effortLabel(run.speed),
            ))
            add(cooldown(block, blockWeek, "d4_cooldown"))
        }
    }

    private fun jumps(block: Int, blockWeek: Int) = SessionRow(
        id = id(block, blockWeek, "plyometrics"),
        title = "Low-dose plyometrics",
        prescription = ProgramV72.plyometrics,
        kind = SessionRowKind.CONDITIONING,
        note = "After the running warm-up and before the sprint repetitions.",
        effortCount = 6,
        effortLabel = "SET",
    )

    private fun throwsRow(block: Int, blockWeek: Int) = SessionRow(
        id = id(block, blockWeek, "throws"),
        title = "Hip-driven medicine-ball throws",
        prescription = "3 × 3 hip-driven scoop OR rotational medicine-ball throws. Nine total throws, both sides combined. Alternate sides and the starting side between exposures. No chest passes.",
        kind = SessionRowKind.CONDITIONING,
        note = "After jumps, before sprints. Zero qualifying sprint meters. Stop for pain or degraded mechanics.",
        effortCount = 3,
        effortLabel = "SET",
    )

    private fun easyAerobic(block: Int, blockWeek: Int): List<SessionRow> {
        val run = ProgramV74.runWeek(blockWeek)
        return listOf(SessionRow(
            id = id(block, blockWeek, "d5_easy"),
            title = "Easy aerobic",
            prescription = run.easyAerobic,
            kind = SessionRowKind.CONDITIONING,
            note = ProgramV74.EFFORT_EASY,
            effortLabel = "BLOCK",
        ))
    }

    private fun resistanceDay(block: Int, blockWeek: Int, day: Int): List<SessionRow> = buildList {
        val daySlots = ProgramV74.slots.filter { it.day == day }
        if (daySlots.isEmpty()) return@buildList
        add(SessionRow(
            id = id(block, blockWeek, "d${day}_resistance_warmup"),
            title = "Resistance warm-up",
            prescription = ProgramV72.resistanceWarmup,
            kind = SessionRowKind.CONDITIONING,
            effortLabel = "WARM-UP",
        ))
        daySlots.forEach { slot ->
            val sets = ProgramV74.plannedSets(block, blockWeek, slot)
            val note = listOfNotNull(
                slot.rest,
                slot.formCue,
                ProgramV74.CONSOLIDATION_LABEL.takeIf { blockWeek == 8 },
            ).joinToString(" ")
            add(SessionRow(
                id = "v74_b${block}_s${slot.number.toString().padStart(2, '0')}",
                title = ProgramV74.movement(block, slot),
                prescription = ProgramV74.plannedDose(block, blockWeek, slot),
                kind = SessionRowKind.RESISTANCE,
                alternatives = ProgramV74.displayedAlternatives(block, slot),
                note = note,
                effortCount = sets,
                effortLabel = "SET",
            ))
        }
    }

    private fun restRow() = SessionRow(
        id = "v74_d7_rest",
        title = "Rest",
        prescription = ProgramV72.restPrescription,
        kind = SessionRowKind.REST,
        effortLabel = "REST",
    )

    private fun cooldown(block: Int, blockWeek: Int, key: String) = SessionRow(
        id = id(block, blockWeek, key),
        title = "Cooldown",
        prescription = ProgramV72.cooldown,
        kind = SessionRowKind.CONDITIONING,
        effortLabel = "COOLDOWN",
    )

    private fun id(block: Int, blockWeek: Int, key: String) = "v74_b${block}_w${blockWeek}_$key"

    private fun effortCount(prescription: String): Int =
        Regex("^(\\d+) × ").find(prescription)?.groupValues?.get(1)?.toInt() ?: 1

    private fun effortLabel(prescription: String): String =
        if (effortCount(prescription) > 1) "REP" else "BLOCK"
}
