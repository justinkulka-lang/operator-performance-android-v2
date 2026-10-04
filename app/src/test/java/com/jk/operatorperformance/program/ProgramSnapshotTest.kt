package com.jk.operatorperformance.program

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

/** Frozen before the final navigation/CI fixes. No copy or dose normalization is permitted. */
class ProgramSnapshotTest {
    @Test fun allSessionAndPhaseFieldsRemainByteIdentical() {
        val expected = requireNotNull(javaClass.getResourceAsStream("/program-baseline.tsv")).use { it.readBytes() }
        val actual = ProgramSnapshot.dump().toByteArray(UTF_8)
        val report = File("build/reports/program-parity")
        report.mkdirs()
        File(report, "actual.tsv").writeBytes(actual)
        File(report, "baseline.tsv").writeBytes(expected)
        assertEquals(448, actual.toString(UTF_8).lineSequence().count { it.startsWith("SESSION\t") })
        assertEquals(2773, actual.toString(UTF_8).lineSequence().count { it.startsWith("ROW\t") })
        assertArrayEquals("All resolved fields must match the frozen candidate exactly", expected, actual)
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
