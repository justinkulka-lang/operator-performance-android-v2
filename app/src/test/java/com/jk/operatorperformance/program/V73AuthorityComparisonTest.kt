package com.jk.operatorperformance.program

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.*
import org.junit.Test

class V73AuthorityComparisonTest {
    private fun doc(name: String): File = listOf(File("../docs/legend-v73/$name"),File("docs/legend-v73/$name")).first { it.isFile }
    @Test fun authorityBytesAndEverySlotMatchAcceptedPacket() {
        val bytes=doc("ARES_Performance_Unified_Master_Authority_v7.3_PROPOSED.md").readBytes()
        assertEquals(ProgramV73.AUTHORITY_SHA256,MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) })
        val text=doc("ARES_v7.3_Allocation_136_146_PROPOSED.md").readText()
        var day=0;var matched=0
        for (line in text.substringBefore("## Verified session totals").lines()) {
            if (line.startsWith("## Day ")) day=line.substringAfter("## Day ").take(1).toInt()
            if (!line.startsWith("| ") || line.startsWith("| Movement") || line.startsWith("| ---")) continue
            val c=line.trim('|').split('|').map { it.trim() }
            if(c.size != 11) continue
            for(phase in 1..4) {
                val slot=ProgramV73.resistanceSlots.single { it.phase == phase && it.day == day && it.movement == c[0] }
                val sets=c[if(phase == 3) 2 else 1].toInt()
                assertEquals(sets,slot.workingSets)
                assertEquals("$sets × ${c[6]} @ ${c[7]} RIR",slot.dose)
                assertEquals(c[8],slot.ownerMuscle.label)
                val alts=c[9].drop(2).split(Regex("; \\d "))
                assertEquals(alts,slot.alternatives.sortedBy { it.rank }.map { it.name })
                assertTrue(slot.restGuidance.contains("Home-rack alternative: ${c[10]}"))
            }
            matched++
        }
        assertEquals(40,matched);assertEquals(160,ProgramV73.resistanceSlots.size)
    }
    @Test fun safetyAndReferenceLimitsRemainAvailableForNewAuthority() {
        val session=SessionResolver.resolve(17,4,ProgramAuthority.V73)
        val guidance=com.jk.operatorperformance.ui.sessionExecutionGuidance(session)
        assertTrue(guidance.contains(ProgramNotesV72.seriousSymptoms))
        assertTrue(guidance.contains(ProgramNotesV72.rirDefinition))
        assertTrue(guidance.contains(ProgramNotesV72.accelerationIntent))
        assertTrue(guidance.contains(ProgramNotesV72.fullRecovery))
        assertTrue(ProgramNotesV73.recovery.contains(ProgramNotesV72.energyRecovery))
        assertTrue(ProgramNotesV73.resistanceProgression.contains(ProgramV72.calibrationNote))
    }
}
