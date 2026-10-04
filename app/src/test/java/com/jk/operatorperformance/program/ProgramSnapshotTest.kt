package com.jk.operatorperformance.program

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

/** Frozen baseline with only the four authorized mixed-Speed reference rows split into groups. */
class ProgramSnapshotTest {
    @Test fun allSessionAndPhaseFieldsRemainByteIdentical() {
        val frozen = requireNotNull(javaClass.getResourceAsStream("/program-baseline.tsv")).use { it.readBytes() }
        val expected = approvedReferenceGroupFix(frozen.toString(UTF_8)).toByteArray(UTF_8)
        val actual = ProgramSnapshot.dump().toByteArray(UTF_8)
        val report = File("build/reports/program-parity")
        report.mkdirs()
        File(report, "actual.tsv").writeBytes(actual)
        File(report, "baseline.tsv").writeBytes(expected)
        File(report, "original-baseline.tsv").writeBytes(frozen)
        assertEquals(448, actual.toString(UTF_8).lineSequence().count { it.startsWith("SESSION\t") })
        assertEquals(2777, actual.toString(UTF_8).lineSequence().count { it.startsWith("ROW\t") })
        assertArrayEquals("All resolved fields must match the frozen candidate exactly", expected, actual)
    }

    // Four explicit reference corrections; every other field/session remains frozen.
    private fun approvedReferenceGroupFix(frozen: String): String {
        val changes = mapOf("w13_d4" to 3, "w20_d4" to 3, "w28_d4" to 2, "w30_d4" to 2)
        var replaced = 0
        val lines = frozen.lineSequence().flatMap { line ->
            val fields = line.split('\t')
            val flies = if (fields.firstOrNull() == "ROW") changes[fields.getOrNull(1)] else null
            if (flies == null) sequenceOf(line) else {
                replaced++
                val suffix = if (fields[1] == "w30_d4") "; full recovery; no jumps" else "; full recovery"
                assertEquals("4 × 30 m acceleration + $flies × 20 m fly$suffix", fields[3])
                assertEquals("4", fields[7])
                val acceleration = fields.toMutableList().apply {
                    this[1] += "_acceleration"
                    this[2] = "Acceleration"
                    this[3] = "4 × 30 m acceleration$suffix"
                    this[6] = this[6].removeSuffix(" Display the written build or acceleration distance. Do not add an approach or repetitions that are not written.")
                }
                val fly = fields.toMutableList().apply {
                    this[1] += "_fly"
                    this[2] = "Fly"
                    this[3] = "$flies × 20 m fly$suffix"
                    this[7] = flies.toString()
                }
                sequenceOf(acceleration.joinToString("\t"), fly.joinToString("\t"))
            }
        }.joinToString("\n")
        assertEquals(4, replaced)
        return lines
    }
}

internal object ProgramSnapshot {
    private fun encode(value: Any?): String = value?.toString()
        ?.replace("\\", "\\\\")?.replace("\t", "\\t")?.replace("\n", "\\n") ?: "<null>"

    fun dump(): String = buildString {
        fun line(vararg fields: Any?) { append(fields.joinToString("\t", transform = ::encode)).append('\n') }
        for (authority in ProgramAuthority.entries) for (week in 1..32) for (day in 1..7) {
            val session = SessionResolver.resolve(week, day, authority)
            val phase = session.phase
            line("SESSION", session.authority, session.week, session.day, session.title,
                phase.name, phase.godName, phase.startWeek, phase.endWeek, phase.status, phase.purpose)
            for (row in session.rows) line("ROW", row.id, row.title, row.prescription, row.kind,
                row.alternatives, row.note, row.effortCount, row.effortLabel, row.restSeconds,
                row.restAfterLastEffort, row.optional)
        }
    }
}
