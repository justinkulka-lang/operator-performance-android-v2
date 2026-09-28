package com.jk.operatorperformance.program

import org.junit.Assert.*
import org.junit.Test

class ProgramV71Test {
    @Test fun authorityAndTopologyAreExact() {
        assertEquals("7.1",AresProgramV71.AUTHORITY_VERSION)
        assertEquals("1d33dbca214b34dd456a971d32bc1c19d59e3a13069393429fc9e2115e3a036c",AresProgramV71.AUTHORITY_SHA256)
        assertEquals((1..32).toList(),AresProgramV71.weeks.map { it.week })
        assertEquals(listOf(1 to 8,9 to 16,17 to 24,25 to 32),AresProgramV71.blocks.map { it.startWeek to it.endWeek })
        assertTrue(ProgramAudit.validate().isEmpty())
    }

    @Test fun everyWeekResolvesSevenDaysWithOneRestDay() {
        for(week in 1..32) {
            val split=SessionResolver.splitForWeek(week)
            assertEquals(7,split.size)
            val expected=listOf("Lower A + easy aerobic","Upper A","Speed","Upper B",if(week==32)"Very easy recovery primer" else "Lower B","Engine + short upper","Rest")
            assertEquals(expected,split.map { it.title })
            assertEquals(1,split.count { session -> session.rows.any { it.kind==SessionRowKind.REST } })
        }
    }

    @Test fun overlaysAndFinalOmissionsAreDeterministic() {
        for(week in listOf(8,16,24)) assertTrue(SessionResolver.resolve(week,1).rows.filter { it.kind==SessionRowKind.RESISTANCE }.all { "3–4 RIR" in it.prescription })
        assertTrue(SessionResolver.resolve(24,6).rows.none { it.kind==SessionRowKind.RESISTANCE })
        assertEquals("Very easy recovery primer",SessionResolver.resolve(32,5).title)
        assertEquals(SessionRowKind.CONDITIONING,SessionResolver.resolve(32,5).rows.single().kind)
        assertTrue(SessionResolver.resolve(32,6).rows.none { it.kind==SessionRowKind.RESISTANCE })
        assertEquals(setOf("Overhead press","Pull-up or pulldown","Row","Rear delt raise"),SessionResolver.resolve(32,4).rows.map { it.title }.toSet())
    }

    @Test fun timed300GateAndPlyometricsAreScoped() {
        for(week in listOf(1,16,32)) assertTrue(SessionResolver.resolve(week,3).rows.any { it.id.endsWith("timed300_gate") })
        for(week in listOf(8,16,24,30,31,32)) assertFalse(SessionResolver.resolve(week,3).rows.any { it.id.endsWith("plyometrics") })
        assertTrue(SessionResolver.resolve(3,3).rows.any { it.id.endsWith("plyometrics") })
    }

    @Test fun everySpeedAndEngineDayHasExactExecutionGuidance() {
        for (week in 1..32) {
            val speed = SessionResolver.resolve(week,3).rows
            val speedWarmup = speed.first()
            assertEquals("d3_speed_warmup", speedWarmup.id)
            assertTrue(speedWarmup.prescription.contains("Walk/jog 5 min"))
            assertTrue(speedWarmup.prescription.contains("Ankle rocks 2 × 15 m"))
            assertTrue(speedWarmup.prescription.contains("3 × 40 m"))
            assertTrue(speed.last().note.orEmpty().contains("recovery", true))
            assertTrue(speed.last().note.orEmpty().contains("Stop", true))

            val engine = SessionResolver.resolve(week,6).rows
            val engineWarmup = engine.first()
            assertTrue(engineWarmup.id.endsWith("d6_warmup"))
            val engineWork = engine.first { it.id == "w${week}_d6" }
            assertFalse(engineWork.note.isNullOrBlank())
            assertTrue(engineWork.note.orEmpty().contains("Do not", true) || engineWork.note.orEmpty().contains("RPE", true))
        }
        for (week in listOf(1,16,32)) {
            assertTrue(SessionResolver.resolve(week,3).rows.first().prescription.contains("90%"))
        }
        for (week in listOf(8,16)) {
            val warmup = SessionResolver.resolve(week,6).rows.first()
            assertEquals("Easy-run opening", warmup.title)
            assertTrue(warmup.prescription.contains("included"))
            assertTrue(warmup.prescription.contains("Do not add strides", true))
        }
        for (week in listOf(13,18,20,23,26,27,28,29,30,31)) {
            val work = SessionResolver.resolve(week,6).rows.first { it.id == "w${week}_d6" }
            assertTrue(work.note.orEmpty().contains("400 m 1:40"))
        }
    }
}
