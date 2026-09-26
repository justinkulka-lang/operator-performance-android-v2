package com.jk.operatorperformance.program

import org.junit.Assert.*
import org.junit.Test

class ProgramV70Test {
    @Test fun authorityAndTopologyAreExact() {
        assertEquals("7.0",AresProgramV70.AUTHORITY_VERSION)
        assertEquals("0f9bd7981dc0671e836165a2b3efa70113c09cfb26e76ffc9c623ebc452dcca0",AresProgramV70.AUTHORITY_SHA256)
        assertEquals((1..28).toList(),AresProgramV70.cycles.map { it.cycle })
        assertEquals(listOf(1 to 7,8 to 14,15 to 21,22 to 28),AresProgramV70.blocks.map { it.startCycle to it.endCycle })
        assertTrue(ProgramAudit.validate().isEmpty())
    }

    @Test fun everyCycleResolvesEightDaysInApprovedOrder() {
        for(cycle in 1..28) {
            val split=SessionResolver.splitForCycle(cycle)
            assertEquals(8,split.size)
            val expected=listOf("Lower A + easy aerobic","Upper A","Speed","Rest",if(cycle==28)"Rest" else "Lower B","Upper B","Engine + short upper","Rest")
            assertEquals(expected,split.map { it.title })
        }
    }

    @Test fun overlaysAndFinalOmissionsAreDeterministic() {
        for(cycle in listOf(7,14,21)) assertTrue(SessionResolver.resolve(cycle,1).rows.filter { it.kind==SessionRowKind.RESISTANCE }.all { "3–4 RIR" in it.prescription })
        assertTrue(SessionResolver.resolve(21,7).rows.none { it.kind==SessionRowKind.RESISTANCE })
        assertEquals("Rest",SessionResolver.resolve(28,5).title)
        assertEquals(SessionRowKind.REST,SessionResolver.resolve(28,5).rows.single().kind)
        assertTrue(SessionResolver.resolve(28,7).rows.none { it.kind==SessionRowKind.RESISTANCE })
        assertEquals(setOf("Overhead press","Pull-up or pulldown","Row","Rear delt raise"),SessionResolver.resolve(28,6).rows.map { it.title }.toSet())
    }

    @Test fun timed300GateAndPlyometricsAreScoped() {
        for(cycle in listOf(1,14,28)) assertTrue(SessionResolver.resolve(cycle,3).rows.any { it.id.endsWith("timed300_gate") })
        for(cycle in listOf(7,14,21,28)) assertFalse(SessionResolver.resolve(cycle,3).rows.any { it.id.endsWith("plyometrics") })
        assertTrue(SessionResolver.resolve(3,3).rows.any { it.id.endsWith("plyometrics") })
    }
}
