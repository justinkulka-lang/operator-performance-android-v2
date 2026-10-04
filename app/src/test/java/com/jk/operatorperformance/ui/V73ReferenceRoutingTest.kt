package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.V73AuthorityMigration
import com.jk.operatorperformance.state.WeekStartEvidence
import org.junit.Assert.*
import org.junit.Test

class V73ReferenceRoutingTest {
    @Test fun searchesRouteHistoricalAndNewWeeksWithoutStateMutation() {
        val old=ProgramUiState(initialized=true,currentWeek=13,currentDay=4,authority=ProgramAuthority.V72,v72ActivationProven=true)
        val state=V73AuthorityMigration.apply(old,true,WeekStartEvidence.ENGAGED)
        assertTrue(searchProgram("week 13 day 4",state).all { it.session.authority == ProgramAuthority.V72 })
        assertTrue(searchProgram("week 17 day 4 pullover",state).any { it.session.authority == ProgramAuthority.V73 })
        assertTrue(searchProgram("week 17 day 1 squat",state).isEmpty())
        assertEquals(state,state.copy())
        assertEquals(13,state.currentWeek);assertEquals(4,state.currentDay)
    }
    @Test fun newRowsKeepSymbolsAndEngineOnlyExecutionGuidance() {
        val session=SessionResolver.resolve(17,4,ProgramAuthority.V73)
        val row=session.rows.single { it.title == "DB pullover" }
        assertEquals(MapSymbol.UPPER_B,rowSymbol(session,row))
        assertEquals(MapSymbol.SPEED,sessionSymbol(session))
        assertTrue(sessionExecutionGuidance(SessionResolver.resolve(17,1,ProgramAuthority.V73)).any { it.contains("Engine is the only Day 1 work") })
    }
}
