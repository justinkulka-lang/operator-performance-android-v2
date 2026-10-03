package com.jk.operatorperformance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.program.AresProgramV71
import com.jk.operatorperformance.program.AresProgramV72
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.state.ProgramUiState

internal fun sessionExecutionGuidance(session: SessionReference): List<String> = buildList {
    val speedDay = if (session.authority == ProgramAuthority.V72) 4 else 3
    val engineDay = if (session.authority == ProgramAuthority.V72) 1 else 6
    if (session.day == speedDay) {
        add("Perform speed fresh on a safe surface. Quality and mechanics govern the session.")
        val timed = if (session.authority == ProgramAuthority.V72) AresProgramV72.isTimed300(session.week) else AresProgramV71.isTimed300(session.week)
        if (timed) add(if (session.authority == ProgramAuthority.V72) AresProgramV72.testGate else AresProgramV71.testGate)
    }
    if (session.day == engineDay) {
        add("Complete the prescribed engine work before any short upper. Do not add result entry or extra intervals.")
    }
}

internal fun foundationReferenceLabel(session: SessionReference, state: ProgramUiState): String? {
    val consolidation = if (session.authority == ProgramAuthority.V72) AresProgramV72.isConsolidation(session.week) else AresProgramV71.isConsolidation(session.week)
    return when {
        consolidation -> "PLANNED CONSOLIDATION • FIXED PRESCRIPTION"
        session.week in setOf(30, 31) -> "PLANNED REALIZATION • FIXED PRESCRIPTION"
        session.week == 32 -> "WEEK 32 OVERLAY • FIXED PRESCRIPTION"
        else -> null
    }
}

@Composable
internal fun KeepScreenOnEffect() {
    val view = LocalView.current
    DisposableEffect(view) {
        val previous = view.keepScreenOn
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = previous }
    }
}

@Composable
internal fun SessionExecutionBanner(session: SessionReference) {
    val guidance = sessionExecutionGuidance(session)
    if (guidance.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("EXECUTION", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            guidance.forEach {
                Text("• $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
internal fun FoundationReferenceBanner(session: SessionReference, state: ProgramUiState) {
    val label = foundationReferenceLabel(session, state) ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(
                "This overlay is part of v${session.authority.versionLabel} and is not a user-selectable deload.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
