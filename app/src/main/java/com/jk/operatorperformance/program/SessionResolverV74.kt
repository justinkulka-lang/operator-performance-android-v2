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
    private const val AIR_BIKE_PRESCRIPTION = "6 × 8 s all-out on an air bike. 90–120 s easy spinning between."
    private const val AIR_BIKE_NOTE = "Before the sprints. Stop when power drops. Zero qualifying sprint meters."
    private const val AIR_BIKE_RANKED = "Uphill or stair acceleration: 4 × 10–15 m, walk back down"
    private const val AIR_BIKE_HOME = "DB jump squat: 4 × 3, 10–20 lb each hand, soft landing"
    private const val LANDMINE_PRESCRIPTION = "3 × 3 per side, as fast as possible."
    private const val LANDMINE_NOTE = "After the air bike, before the sprints. Stop when bar speed drops."
    private const val LANDMINE_HOME = "Single-arm DB push press: 3 × 3 per side"

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
        val run = ProgramV74.runWeek(blockWeek)
        return listOf(
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
            // C-13 and C-14 replace the jump and throw rows. C-15 removes the speed warm-up.
            if (run.jumps) add(airBike(block, blockWeek))
            if (run.medicineBallThrows) add(landminePushPress(block, blockWeek))
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

    private fun airBike(block: Int, blockWeek: Int) = SessionRow(
        id = id(block, blockWeek, "airbike"),
        title = "Air-bike sprints",
        prescription = AIR_BIKE_PRESCRIPTION,
        kind = SessionRowKind.CONDITIONING,
        alternatives = listOf(AIR_BIKE_RANKED, "Home: $AIR_BIKE_HOME"),
        note = AIR_BIKE_NOTE,
        effortCount = 6,
        effortLabel = "REP",
    )

    private fun landminePushPress(block: Int, blockWeek: Int) = SessionRow(
        id = id(block, blockWeek, "landmine_push_press"),
        title = "Landmine push press",
        prescription = LANDMINE_PRESCRIPTION,
        kind = SessionRowKind.CONDITIONING,
        alternatives = listOf("Home: $LANDMINE_HOME"),
        note = LANDMINE_NOTE,
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
