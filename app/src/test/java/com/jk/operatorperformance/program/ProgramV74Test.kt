package com.jk.operatorperformance.program

import com.jk.operatorperformance.ui.phaseFieldLabel
import com.jk.operatorperformance.ui.positionConfirmation
import com.jk.operatorperformance.ui.sessionFocusLine
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgramV74Test {
    @Test fun authorityCopyMatchesTheHandoffBytes() {
        val handoff = repoFile("handoff/LEGEND_v7.4_Continuous.md").readBytes()
        val copy = repoFile("docs/legend-v74/LEGEND_v7.4_Continuous.md").readBytes()
        assertArrayEquals(handoff, copy)
        val approval = repoFile("docs/legend-v74/OWNER_APPROVAL.md").readText()
        assertTrue(approval.contains("On 10/20/2026 the owner wrote: \"Do a full scale audit then Go with the recommended.\""))
        assertTrue(approval.contains("10/24/2026"))
        val clarifications = repoFile("docs/legend-v74/CLARIFICATIONS.md").readText()
        assertTrue(clarifications.contains("The §9 blocklist entry \"anchor\" is too broad"))
        assertTrue(clarifications.contains("Use \"300 m anchor\" and \"anchor formula\" in the blocklist instead of \"anchor\"."))
        assertTrue(clarifications.contains("## C-8"))
        assertTrue(clarifications.contains("## C-9"))
        assertTrue(clarifications.contains("## C-10"))
        assertTrue(clarifications.contains("## C-11"))
        assertTrue(clarifications.contains("\"Stop for pain or declining landing quality.\" → \"Stop when landing quality declines.\""))
        assertTrue(clarifications.contains("\"Stop for pain or degraded mechanics.\" → \"Stop when mechanics degrade.\""))
    }

    @Test fun computedConsolidationMatchesTheTableForEverySlot() {
        val table = resistanceTable()
        assertEquals(40, table.size)
        assertEquals((1..40).toList(), ProgramV74.slots.map { it.number })
        table.forEach { row ->
            val slot = ProgramV74.slot(row.number)
            assertEquals("Slot ${row.number} base", row.baseSets, slot.baseSets)
            assertEquals("Slot ${row.number} peak", row.peakSets, slot.peakSets)
            assertEquals("Slot ${row.number} reps", row.reps, slot.reps)
            assertEquals("Slot ${row.number} cue", row.formCue, slot.formCue)
            assertEquals("Slot ${row.number} home", row.home, slot.home)
            assertEquals("Slot ${row.number} rir", row.rir, slot.rir)
            assertTrue("Slot ${row.number} rest", slot.rest.contains(row.rest))
            assertEquals("Slot ${row.number} alternatives", row.alternatives, slot.alternatives.sortedBy { it.rank }.map { it.name })
            listOf(1, 3, 4).forEach { block ->
                assertEquals("B$block slot ${row.number} week 1", row.baseSets, ProgramV74.plannedSets(block, 1, slot))
                assertEquals("B$block slot ${row.number} consol", row.consolBase, ProgramV74.plannedSets(block, 8, slot))
                assertEquals(row.consolBase, (row.baseSets / 2).coerceAtLeast(1))
            }
            assertEquals(row.peakSets, ProgramV74.plannedSets(2, 1, slot))
            assertEquals("Block II slot ${row.number}", row.consolPeak, ProgramV74.plannedSets(2, 8, slot))
            assertEquals(row.consolPeak, (row.peakSets / 2).coerceAtLeast(1))
            val weekOne = SessionResolverV74.resolve(1, 1, slot.day).rows.single { it.id.endsWith("s${row.number.toString().padStart(2, '0')}") }
            assertEquals("${row.baseSets} × ${row.reps} @ ${row.rir} RIR", weekOne.prescription)
            assertTrue(weekOne.note.orEmpty().startsWith(slot.rest))
            if (row.formCue == null) assertFalse(weekOne.note.orEmpty().contains("—"))
            else assertTrue(weekOne.note.orEmpty().contains(row.formCue))
            assertEquals(row.alternatives + "Home: ${row.home}", weekOne.alternatives)
            val consol = SessionResolverV74.resolve(1, 8, slot.day).rows.single { it.id == weekOne.id }
            assertEquals("${row.consolBase} × ${row.reps} @ 3–4 RIR", consol.prescription)
            assertTrue(consol.note.orEmpty().contains(ProgramV74.CONSOLIDATION_LABEL))
            assertFalse(weekOne.note.orEmpty().contains(ProgramV74.CONSOLIDATION_LABEL))
        }
    }

    @Test fun blockThreeRepsAndBlockFourNamesMatchTheTable() {
        val table = resistanceTable()
        table.forEach { row ->
            val slot = ProgramV74.slot(row.number)
            assertEquals(row.repsThree, ProgramV74.reps(3, slot))
            assertEquals(if (row.number in setOf(1, 8, 25, 34)) row.repsThree else row.reps, ProgramV74.reps(3, slot))
            val shown = SessionResolverV74.resolve(3, 1, slot.day).rows.single { it.title == ProgramV74.movement(3, slot) && it.kind == SessionRowKind.RESISTANCE && it.effortCount == slot.baseSets }
            assertTrue(shown.prescription.contains(row.repsThree))
        }
        val squat = ProgramV74.slot(1)
        val press = ProgramV74.slot(2)
        assertEquals("Safety-bar squat", ProgramV74.movement(4, squat))
        assertEquals(listOf("back squat", "front squat", "Smith squat"), ProgramV74.rankedAlternatives(4, squat).map { it.name })
        assertEquals(squat.home, ProgramV74.slot(1).home)
        assertEquals("Hack squat", ProgramV74.movement(4, press))
        assertEquals(listOf("leg press", "Smith squat", "heel-elevated DB squat"), ProgramV74.rankedAlternatives(4, press).map { it.name })
        assertEquals("Back squat", ProgramV74.movement(1, squat))
        assertEquals("Leg press", ProgramV74.movement(3, press))
        val live = SessionResolverV74.resolve(4, 1, 2)
        assertEquals("Safety-bar squat", live.rows.first { it.kind == SessionRowKind.RESISTANCE }.title)
        assertEquals("Hack squat", live.rows.filter { it.kind == SessionRowKind.RESISTANCE }[1].title)
        assertTrue(live.rows.first { it.kind == SessionRowKind.RESISTANCE }.alternatives.last().startsWith("Home: "))
    }

    @Test fun ownerMusclesReproduceSectionTwoCounts() {
        assertEquals(owners, ProgramV74.slots.map { it.ownerMuscle })
        val normal = mapOf(
            Muscle.QUADS to 16, Muscle.HAMSTRINGS to 14, Muscle.GLUTES to 8, Muscle.CHEST to 15,
            Muscle.LATS to 10, Muscle.UPPER_BACK to 10, Muscle.DELTS to 15, Muscle.BICEPS to 9,
            Muscle.TRICEPS to 7, Muscle.CALVES to 13, Muscle.TRAPS to 7, Muscle.FOREARMS to 5, Muscle.ABS to 7,
        )
        val peak = normal + mapOf(
            Muscle.QUADS to 18, Muscle.HAMSTRINGS to 16, Muscle.LATS to 11,
            Muscle.TRICEPS to 8, Muscle.TRAPS to 9, Muscle.FOREARMS to 7,
        )
        listOf(1, 3, 4).forEach { block -> assertEquals("Block $block", normal, ProgramV74.directSets(block, 1)) }
        assertEquals(peak, ProgramV74.directSets(2, 1))
        val days = mapOf(
            Muscle.QUADS to setOf(2, 6), Muscle.HAMSTRINGS to setOf(2, 6), Muscle.GLUTES to setOf(2, 6), Muscle.CALVES to setOf(2, 6),
            Muscle.CHEST to setOf(3, 4, 5), Muscle.UPPER_BACK to setOf(3, 4, 5), Muscle.LATS to setOf(3, 4, 5),
            Muscle.DELTS to setOf(3, 4, 5), Muscle.BICEPS to setOf(3, 4, 5), Muscle.TRICEPS to setOf(3, 4),
            Muscle.TRAPS to setOf(3, 5), Muscle.FOREARMS to setOf(4, 5), Muscle.ABS to setOf(2, 4, 6),
        )
        days.forEach { (muscle, expected) ->
            assertEquals(muscle.label, expected, ProgramV74.slots.filter { it.ownerMuscle == muscle }.map { it.day }.toSet())
        }
    }

    @Test fun sessionTotalsAndCapsHoldForThreeLoops() {
        val published = sessionTotalTable()
        published.forEach { row ->
            val day = row.day
            assertEquals(row.normalBase, ProgramV74.daySets(1, 1, day))
            assertEquals(row.normalPeak, ProgramV74.daySets(2, 1, day))
            assertEquals(row.consolBase, ProgramV74.daySets(1, 8, day))
            assertEquals(row.consolPeak, ProgramV74.daySets(2, 8, day))
            assertEquals(row.capBase, ProgramV74.sessionCap(1, day))
            assertEquals(row.capPeak, ProgramV74.sessionCap(2, day))
        }
        assertEquals(136, published.sumOf { it.normalBase })
        assertEquals(146, published.sumOf { it.normalPeak })
        assertEquals(59, published.sumOf { it.consolBase })
        assertEquals(66, published.sumOf { it.consolPeak })
        for (absolute in 1..96) {
            val (block, blockWeek) = ProgramV74.position(absolute)
            var total = 0
            for (day in 1..7) {
                val sets = SessionResolverV74.resolve(block, blockWeek, day).rows
                    .filter { it.kind == SessionRowKind.RESISTANCE }.sumOf { it.effortCount }
                assertTrue("Week $absolute day $day", sets <= ProgramV74.sessionCap(block, day))
                assertEquals(ProgramV74.daySets(block, blockWeek, day), sets)
                total += sets
            }
            val expected = when {
                blockWeek == 8 && block == 2 -> 66
                blockWeek == 8 -> 59
                block == 2 -> 146
                else -> 136
            }
            assertEquals("Absolute week $absolute", expected, total)
        }
        assertEquals(1 to 1, ProgramV74.position(33))
        assertEquals(1 to 8, ProgramV74.position(40))
        assertEquals(4 to 8, ProgramV74.position(64))
        assertEquals(1 to 1, ProgramV74.position(65))
        for (week in 1..32) {
            assertEquals(ProgramV74.position(week), ProgramV74.position(week + 32))
            assertEquals(ProgramV74.position(week), ProgramV74.position(week + 64))
        }
    }

    @Test fun runningCycleMatchesSectionFiveInEveryBlock() {
        val table = runningTable()
        assertEquals(8, table.size)
        for (block in 1..4) for (blockWeek in 1..8) {
            val expected = table[blockWeek - 1]
            val run = ProgramV74.runWeek(blockWeek)
            assertEquals(expected.engine, run.engine)
            assertEquals(expected.speed, run.speed)
            assertEquals(expected.meters, run.sprintMeters)
            assertEquals(expected.easy, run.easyAerobic)
            assertEquals(expected.jumps, run.jumps)
            assertEquals(expected.throws, run.medicineBallThrows)
            assertTrue(run.sprintMeters in 80..500)
            val engine = SessionResolverV74.resolve(block, blockWeek, 1).rows.single { it.title == "Engine" }
            val speed = SessionResolverV74.resolve(block, blockWeek, 4).rows.single { it.title == "Speed" }
            val easy = SessionResolverV74.resolve(block, blockWeek, 5).rows.single { it.title == "Easy aerobic" }
            assertEquals(expected.engine, engine.prescription)
            assertEquals(expected.speed, speed.prescription)
            assertEquals(expected.easy, easy.prescription)
            assertEquals(effortForEngine(blockWeek), engine.note)
            assertEquals(ProgramV74.EFFORT_SPEED, speed.note)
            assertEquals(ProgramV74.EFFORT_EASY, easy.note)
            val speedRows = SessionResolverV74.resolve(block, blockWeek, 4).rows
            val warmup = speedRows.indexOfFirst { it.title == "Speed warm-up" }
            val jumps = speedRows.indexOfFirst { it.title == "Low-dose plyometrics" }
            val throws = speedRows.indexOfFirst { it.title == "Hip-driven medicine-ball throws" }
            val sprint = speedRows.indexOfFirst { it.title == "Speed" }
            val cooldown = speedRows.indexOfFirst { it.title == "Cooldown" }
            assertEquals(blockWeek in 3..7, jumps >= 0)
            assertEquals(blockWeek in setOf(3, 4, 6), throws >= 0)
            if (jumps >= 0) assertEquals(warmup + 1, jumps)
            if (throws >= 0) assertEquals(jumps + 1, throws)
            assertTrue(sprint > warmup && cooldown > sprint)
            assertTrue(speedRows.indexOfFirst { it.id.endsWith("d4_resistance_warmup") } > cooldown)
        }
    }

    @Test fun jumpsAndThrowsReuseTheExistingRowsAndWarmupsDropRemovedSentences() {
        val landingBefore = "Stop for pain or declining landing quality."
        val landingAfter = "Stop when landing quality declines."
        val mechanicsBefore = "Stop for pain or degraded mechanics."
        val mechanicsAfter = "Stop when mechanics degrade."
        val v72Source = repoFile("app/src/main/java/com/jk/operatorperformance/program/ProgramV72.kt").readText()
        val v73Source = repoFile("app/src/main/java/com/jk/operatorperformance/program/SessionResolverV73.kt").readText()
        assertTrue(v72Source.contains(landingBefore))
        assertFalse(v72Source.contains(landingAfter))
        assertTrue(v73Source.contains(mechanicsBefore))
        assertFalse(v73Source.contains(mechanicsAfter))
        assertTrue(ProgramV72.plyometrics.contains(landingBefore))
        val v72Jumps = SessionResolver.resolve(3, 4, ProgramAuthority.V72).rows.single { it.id.endsWith("plyometrics") }
        val v74Jumps = SessionResolverV74.resolve(1, 3, 4).rows.single { it.title == "Low-dose plyometrics" }
        assertEquals(v72Jumps.title, v74Jumps.title)
        assertEquals(1, Regex.fromLiteral(landingBefore).findAll(ProgramV72.plyometrics).count())
        assertEquals(ProgramV72.plyometrics.replace(landingBefore, landingAfter), v74Jumps.prescription)
        assertEquals(v72Jumps.prescription, ProgramV72.plyometrics)
        assertFalse(v74Jumps.prescription.contains("pain"))
        assertEquals(v72Jumps.note, v74Jumps.note)
        assertEquals(v72Jumps.effortCount, v74Jumps.effortCount)
        assertEquals(v72Jumps.effortLabel, v74Jumps.effortLabel)
        val v73Throws = SessionResolver.resolve(3, 4, ProgramAuthority.V73).rows.single { it.title == "Hip-driven medicine-ball throws" }
        val v74Throws = SessionResolverV74.resolve(2, 4, 4).rows.single { it.title == v73Throws.title }
        val v73Note = requireNotNull(v73Throws.note)
        assertEquals(v73Throws.prescription, v74Throws.prescription)
        assertEquals(1, Regex.fromLiteral(mechanicsBefore).findAll(v73Note).count())
        assertEquals(v73Note.replace(mechanicsBefore, mechanicsAfter), v74Throws.note)
        assertFalse(v74Throws.note.orEmpty().contains("pain"))
        assertEquals(v73Throws.effortCount, v74Throws.effortCount)
        assertEquals(v73Throws.effortLabel, v74Throws.effortLabel)
        val legacyQuality = SessionResolver.resolve(2, 1, ProgramAuthority.V72).rows.single { it.title == "Engine warm-up" }
        val quality = SessionResolverV74.resolve(1, 1, 1).rows.single { it.title == "Engine warm-up" }
        val cut = "Complete before threshold conditioning, intervals, goal-pace, or timed work."
        assertEquals(cut, legacyQuality.note)
        assertEquals(legacyQuality.prescription, quality.prescription)
        assertNull(quality.note)
        val legacyEasy = SessionResolver.resolve(8, 1, ProgramAuthority.V72).rows.single { it.title == "Easy-run opening" }
        val easy = SessionResolverV74.resolve(3, 8, 1).rows.single { it.title == "Easy-run opening" }
        assertEquals(legacyEasy.prescription, easy.prescription)
        assertEquals(legacyEasy.note, easy.note)
        val speed = SessionResolverV74.resolve(1, 1, 4).rows.single { it.title == "Speed warm-up" }
        assertEquals(ProgramV72.speedWarmup, speed.prescription)
        assertFalse(speed.prescription.contains(ProgramV72.speedTestBuildup))
        assertEquals(ProgramV72.cooldown, SessionResolverV74.resolve(1, 1, 1).rows.single { it.title == "Cooldown" }.prescription)
        assertEquals(ProgramV72.cooldown, SessionResolverV74.resolve(1, 4, 4).rows.single { it.title == "Cooldown" }.prescription)
        val resistance = SessionResolverV74.resolve(1, 2, 2).rows.single { it.title == "Resistance warm-up" }
        assertEquals(ProgramV72.resistanceWarmup, resistance.prescription)
        assertNull(resistance.note)
    }

    @Test fun dayOrderDisplayAndBlocklist() {
        val upper = SessionResolverV74.resolve(1, 1, 3)
        val presses = upper.rows.filter { it.kind == SessionRowKind.RESISTANCE && it.title.contains("press", true) }
        assertEquals("Flat barbell bench press", upper.rows.first { it.kind == SessionRowKind.RESISTANCE }.title)
        assertEquals(listOf("Flat barbell bench press", "Incline barbell press"), presses.map { it.title })
        val lowerA = SessionResolverV74.resolve(1, 1, 2)
        val lower = SessionResolverV74.resolve(1, 1, 6)
        assertEquals(listOf("Seated leg curl"), lowerA.rows.filter { it.title == "Seated leg curl" }.map { it.title })
        assertEquals(listOf("Seated leg curl"), lower.rows.filter { it.title == "Seated leg curl" }.map { it.title })
        assertTrue(lower.rows.any { it.title == "Barbell hip thrust" })
        assertFalse((lowerA.rows + lower.rows).any { it.title.contains("Lying") })
        val day5 = SessionResolverV74.resolve(1, 6, 5)
        assertEquals("Easy aerobic", day5.rows.last().title)
        assertTrue(day5.rows.indexOfFirst { it.kind == SessionRowKind.RESISTANCE } < day5.rows.indexOfFirst { it.title == "Easy aerobic" })
        val rest = SessionResolverV74.resolve(4, 8, 7)
        assertEquals("Rest", rest.title)
        assertEquals(1, rest.rows.size)
        assertEquals(SessionRowKind.REST, rest.rows.single().kind)
        val forbidden = listOf(
            "MISSING", "goal pace", "300 m anchor", "anchor formula", "calibration",
            "validated injury", "outside the app", "v7.3 starts", "LEGEND v7.3",
        )
        val reachable = mutableListOf<String>()
        for (block in 1..4) for (blockWeek in 1..8) for (day in 1..7) {
            val session = SessionResolverV74.resolve(block, blockWeek, day)
            assertEquals(ProgramV74.dayTitles.getValue(day), session.title)
            assertEquals(ProgramV74.GLOBAL_LINE, session.globalLine)
            assertEquals(1, listOf(session.globalLine).count { it == ProgramV74.GLOBAL_LINE })
            assertFalse(session.rows.any { it.prescription == ProgramV74.GLOBAL_LINE || it.note == ProgramV74.GLOBAL_LINE })
            val absolute = V74Pointer.absoluteWeek(block, blockWeek)
            val shown = V74Pointer.reference(absolute, block, blockWeek, day)
            reachable += listOf(
                session.title,
                session.globalLine,
                V74Pointer.header(absolute, block, blockWeek),
                "Loop ${V74Pointer.loopFor(absolute)}",
                sessionFocusLine(shown),
                phaseFieldLabel(shown.phase),
            )
            session.rows.forEach { row ->
                reachable += row.title
                reachable += row.prescription
                reachable += row.note.orEmpty()
                reachable += row.alternatives
            }
        }
        val notes = CoachingNotes.page(ProgramAuthority.V74)
        assertEquals(
            listOf("Week structure", "Progression", "Autoregulation", "Running effort guide"),
            notes.sections.map { it.title },
        )
        assertNull(notes.intro)
        assertFalse(notes.showSprintReference)
        notes.sections.forEach { section ->
            reachable += section.title
            reachable += section.lines
        }
        val retained = CoachingNotes.page(ProgramAuthority.V73)
        assertTrue(retained.showSprintReference)
        assertEquals(ProgramNotesV73.authorityLimits, retained.sections.single { it.title == "AUTHORITY LIMITS" }.lines)
        assertEquals(ProgramNotesV72.resistanceProgression, retained.sections.single { it.title == "RESISTANCE PROGRESSION" }.lines)
        reachable += V74SurfaceText.chrome()
        reachable += (1..7).map { V74SurfaceText.setCurrentDay(it) }
        for (block in 1..4) for (blockWeek in 1..8) for (day in 1..7) {
            reachable += positionConfirmation(block, blockWeek, day)
        }
        reachable.forEach { text -> assertAllowedV74Copy(text, forbidden) }
        val app = repoFile("app/src/main/java/com/jk/operatorperformance/ui/OperatorPerformanceApp.kt").readText()
        assertFalse(app.contains("LEGEND v7.3"))
        assertFalse(app.contains("v7.3 starts"))
        assertTrue(reachable.any { it.contains("anchored to rack") })
        assertTrue(reachable.any { it.contains("anchored high on the rack") })
        val v71Notes = CoachingNotes.page(ProgramAuthority.V71)
        assertEquals(ProgramNotesV71.execution, v71Notes.sections.single { it.title == "EXECUTION" }.lines)
        assertEquals(ProgramNotesV72.safety, v71Notes.sections.single { it.title == "SAFETY / PARTICIPATION" }.lines)
        val homes = SessionResolverV74.resolve(1, 1, 2).rows.flatMap { it.alternatives } +
            SessionResolverV74.resolve(1, 1, 5).rows.flatMap { it.alternatives } +
            SessionResolverV74.resolve(1, 1, 6).rows.flatMap { it.alternatives }
        assertTrue(homes.any { it.contains("anchored to rack") })
        assertTrue(homes.any { it.contains("anchored high on the rack") })
    }

    @Test fun resolvedSessionDumpCoversEveryBlockWeekAndDay() {
        val text = buildString {
            for (block in 1..4) for (blockWeek in 1..8) for (day in 1..7) {
                for (row in SessionResolverV74.resolve(block, blockWeek, day).rows) {
                    append(listOf(block, blockWeek, day, row.id, row.title, row.prescription, row.alternatives, row.note)
                        .joinToString("\t", transform = ::encodeDumpField))
                    append('\n')
                }
            }
        }
        val dir = File("build/reports/legend-v74")
        dir.mkdirs()
        val file = File(dir, "resolved-sessions.tsv")
        file.writeText(text)
        val lines = text.lineSequence().filter { it.isNotEmpty() }.toList()
        assertEquals(lines, file.readLines())
        val sessions = lines.map { line -> line.split('\t').take(3).joinToString("\t") }.toSet()
        val expected = (1..4).flatMap { block -> (1..8).flatMap { blockWeek -> (1..7).map { day -> "$block\t$blockWeek\t$day" } } }.toSet()
        assertEquals(224, sessions.size)
        assertEquals(expected, sessions)
        assertTrue(lines.all { it.split('\t').size == 8 })
        val digest = java.security.MessageDigest.getInstance("SHA-256").digest(file.readBytes())
        assertEquals(
            "af562e6e0ccde91abd84777956e1adefabf60952b5a6ecd92090303bcc61c7fa",
            digest.joinToString("") { "%02x".format(it) },
        )
    }

    private fun assertAllowedV74Copy(text: String, forbidden: List<String>) {
        assertFalse("MISSING in: $text", text.contains("MISSING"))
        val lowerText = text.lowercase()
        forbidden.filter { it != "MISSING" }.forEach { term ->
            assertFalse("$term in: $text", lowerText.contains(term.lowercase()))
        }
        assertFalse(lowerText.contains("pull-up"))
        assertFalse(lowerText.contains("t-bar"))
        assertFalse(text.contains("goal-pace"))
        assertFalse(text.contains("Planned consolidation"))
        assertFalse(text.contains("Planned realization"))
        assertFalse(text.contains(ProgramV72.speedTestBuildup))
    }

    private fun encodeDumpField(value: Any?): String = value?.toString()
        ?.replace("\\", "\\\\")?.replace("\t", "\\t")?.replace("\n", "\\n") ?: "<null>"

    private fun effortForEngine(blockWeek: Int) = when (blockWeek) {
        1, 3, 5 -> ProgramV74.EFFORT_THRESHOLD
        2, 4, 6 -> ProgramV74.EFFORT_CONTROLLED
        7 -> ProgramV74.EFFORT_MILE_AND_HALF
        else -> ProgramV74.EFFORT_EASY
    }

    private fun resistanceTable(): List<ResistanceRow> = repoFile("handoff/LEGEND_v7.4_Continuous.md").readLines()
        .filter { it.matches(Regex("\\| \\d+ \\| D[2-6].*")) }
        .map { line ->
            val cells = line.trim('|').split('|').map { it.trim() }
            val movement = cells[2]
            val rotated = Regex("^(.*) \\(IV: (.*)\\)$").matchEntire(movement)
            ResistanceRow(
                number = cells[0].toInt(),
                day = cells[1].substring(1, 2).toInt(),
                movement = rotated?.groupValues?.get(1) ?: movement,
                blockFourMovement = rotated?.groupValues?.get(2),
                baseSets = cells[3].toInt(),
                peakSets = cells[4].toInt(),
                consolBase = cells[5].toInt(),
                consolPeak = cells[6].toInt(),
                reps = cells[7],
                repsThree = if (cells[8] == "same") cells[7] else cells[8],
                rir = cells[9],
                rest = cells[10],
                formCue = cells[11].takeUnless { it == "—" },
                alternatives = cells[12].split(';').map { it.trim().replace(Regex("^\\d+\\s+"), "") },
                home = cells[13],
            )
        }

    private fun runningTable(): List<RunningRow> = repoFile("handoff/LEGEND_v7.4_Continuous.md").readLines()
        .filter { it.matches(Regex("\\| \\d+.*\\|.*\\| \\d+ \\|.*")) && (it.contains("threshold") || it.contains("controlled interval") || it.contains("easy-only") || it.contains("1.5-mile")) }
        .map { line ->
            val cells = line.trim('|').split('|').map { it.trim() }
            RunningRow(cells[1], cells[2], cells[3].toInt(), cells[4], cells[5] == "Yes", cells[6] == "Yes")
        }

    private fun sessionTotalTable(): List<DayTotal> = repoFile("handoff/LEGEND_v7.4_Continuous.md").readLines()
        .filter { it.matches(Regex("\\| D[2-6] .*\\| \\d+ \\|.*")) }
        .map { line ->
            val cells = line.trim('|').split('|').map { it.trim() }
            DayTotal(cells[0].substring(1, 2).toInt(), cells[1].toInt(), cells[2].toInt(), cells[3].toInt(), cells[4].toInt(), cells[5].toInt(), cells[6].toInt())
        }

    private fun repoFile(relative: String): File =
        listOf(File(relative), File("app/$relative"), File("../$relative")).first { it.isFile }

    private data class ResistanceRow(
        val number: Int,
        val day: Int,
        val movement: String,
        val blockFourMovement: String?,
        val baseSets: Int,
        val peakSets: Int,
        val consolBase: Int,
        val consolPeak: Int,
        val reps: String,
        val repsThree: String,
        val rir: String,
        val rest: String,
        val formCue: String?,
        val alternatives: List<String>,
        val home: String,
    )

    private data class RunningRow(
        val engine: String,
        val speed: String,
        val meters: Int,
        val easy: String,
        val jumps: Boolean,
        val throws: Boolean,
    )

    private data class DayTotal(
        val day: Int,
        val normalBase: Int,
        val normalPeak: Int,
        val consolBase: Int,
        val consolPeak: Int,
        val capBase: Int,
        val capPeak: Int,
    )

    private companion object {
        val owners = listOf(
            Muscle.QUADS, Muscle.QUADS, Muscle.QUADS, Muscle.HAMSTRINGS, Muscle.GLUTES, Muscle.CALVES, Muscle.ABS,
            Muscle.CHEST, Muscle.UPPER_BACK, Muscle.CHEST, Muscle.LATS, Muscle.DELTS, Muscle.TRICEPS, Muscle.BICEPS, Muscle.TRAPS,
            Muscle.CHEST, Muscle.LATS, Muscle.UPPER_BACK, Muscle.DELTS, Muscle.DELTS, Muscle.BICEPS, Muscle.TRICEPS, Muscle.FOREARMS, Muscle.ABS,
            Muscle.DELTS, Muscle.LATS, Muscle.UPPER_BACK, Muscle.CHEST, Muscle.DELTS, Muscle.DELTS, Muscle.BICEPS, Muscle.FOREARMS, Muscle.TRAPS,
            Muscle.HAMSTRINGS, Muscle.GLUTES, Muscle.QUADS, Muscle.HAMSTRINGS, Muscle.QUADS, Muscle.CALVES, Muscle.ABS,
        )
    }
}
