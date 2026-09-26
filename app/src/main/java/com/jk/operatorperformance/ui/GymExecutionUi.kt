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
import com.jk.operatorperformance.program.AresProgramV70
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.WorkoutProgress

internal enum class WorkoutMapVisualState {
    COMPLETE,
    CURRENT,
    UPCOMING,
}

internal fun sessionExecutionGuidance(session: SessionReference): List<String> = buildList {
    if (session.day == 3) {
        add("Perform speed fresh on a safe surface; quality and mechanics govern the session.")
        if (AresProgramV70.isTimed300(session.cycle)) add(AresProgramV70.testGate)
    }
    if (session.day == 7) {
        add("Complete the prescribed engine work before short upper. Do not add result entry or extra intervals.")
    }
}

internal fun foundationReferenceLabel(session: SessionReference, state: ProgramUiState): String? {
    return if (AresProgramV70.isConsolidation(session.cycle)) "PLANNED CONSOLIDATION • FIXED PRESCRIPTION" else null
}

internal fun rowIsComplete(progress: ActiveWorkoutState, row: SessionRow): Boolean =
    (1..row.effortCount).all { WorkoutProgress.isEffortComplete(progress, row.id, it) }

internal fun workoutMapState(
    progress: ActiveWorkoutState,
    session: SessionReference,
    rowIndex: Int,
): WorkoutMapVisualState {
    val row = session.rows[rowIndex]
    return when {
        rowIsComplete(progress, row) -> WorkoutMapVisualState.COMPLETE
        !progress.complete && rowIndex == progress.movementIndex -> WorkoutMapVisualState.CURRENT
        else -> WorkoutMapVisualState.UPCOMING
    }
}

internal fun nextIncompleteMovementTitle(progress: ActiveWorkoutState, session: SessionReference): String? {
    if (progress.complete) return null
    val orderedIndices = ((progress.movementIndex + 1)..session.rows.lastIndex).toList() +
        (0 until progress.movementIndex).toList()
    val nextIndex = orderedIndices.firstOrNull { !rowIsComplete(progress, session.rows[it]) } ?: return null
    val row = session.rows[nextIndex]
    return progress.selectedSubstitutions[row.id] ?: row.title
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
                "This overlay is part of v7.0 and is not a user-selectable deload.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
