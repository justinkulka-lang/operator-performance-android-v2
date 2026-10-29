package com.jk.operatorperformance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.program.CoachingNotes
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.ProgramNotesV72
import com.jk.operatorperformance.program.V74SurfaceText
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState

@Composable
internal fun DossierPositionControls(onProgramReset: (ProgramUiState) -> Unit) {
    val context = LocalContext.current
    val stateStore = remember(context.applicationContext) { ProgramStateStore(context.applicationContext) }
    var confirmWeekOne by remember { mutableStateOf(false) }
    var showStartPosition by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(
            onClick = { showStartPosition = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("change_position"),
        ) { Text("CHANGE POSITION") }
        TextButton(
            onClick = { confirmWeekOne = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("start_over"),
        ) { Text("START OVER") }
    }

    if (confirmWeekOne) {
        AlertDialog(
            onDismissRequest = { confirmWeekOne = false },
            title = { Text("Reset current program pointer?") },
            text = { Text(V74SurfaceText.RESET_WEEK_ONE_CONFIRMATION) },
            confirmButton = {
                TextButton(
                    onClick = {
                        stateStore.clearWorkout()
                        stateStore.clearSessionStatus()
                        stateStore.clearSealedDays()
                        val next = stateStore.load().resetToV74Start()
                        stateStore.save(next)
                        confirmWeekOne = false
                        onProgramReset(next)
                    },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_start_over"),
                ) { Text("RESET") }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmWeekOne = false },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("start_over_cancel"),
                ) { Text("CANCEL") }
            },
        )
    }
    if (showStartPosition) {
        StartPositionFlow(
            onDismiss = { showStartPosition = false },
            onConfirm = { block, blockWeek, day ->
                stateStore.clearWorkout()
                stateStore.clearSessionStatus()
                stateStore.clearSealedDays()
                val next = stateStore.load().startAtPosition(block, blockWeek, day)
                stateStore.save(next)
                showStartPosition = false
                onProgramReset(next)
            },
        )
    }
}

@Composable
internal fun DossierNotes(authority: ProgramAuthority) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val page = CoachingNotes.page(authority)
    Card(modifier = Modifier.fillMaxWidth().testTag("program_notes_tool")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PROGRAM / COACHING NOTES", style = MaterialTheme.typography.titleSmall)
            page.intro?.let { Text(it) }
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("program_notes_toggle"),
            ) { Text(if (expanded) "HIDE NOTES" else "SHOW NOTES") }
            if (expanded) {
                page.sections.forEach { section ->
                    if (page.showSprintReference && section.title == "PLANNED OVERLAYS") SprintReferenceBlock()
                    NotesSection(section.title, section.lines)
                }
            }
        }
    }
}

@Composable
private fun SprintReferenceBlock() {
    Text(CoachingNotes.SPRINT_HEADING, style = MaterialTheme.typography.titleSmall)
    ProgramNotesV72.sprintReference.forEach { (distance, time) ->
        Row(Modifier.fillMaxWidth().testTag("sprint_reference_${distance.substringBefore(' ')}"), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(distance)
            Text(time, style = technicalStyle(MaterialTheme.typography.bodyMedium))
        }
    }
    Text(ProgramNotesV72.sprintReferenceLimit)
    Text(CoachingNotes.PERCENTAGE_NOTE)
}

@Composable
private fun NotesSection(title: String, rows: List<String>) {
    Text(title, style = MaterialTheme.typography.titleSmall)
    rows.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
}
