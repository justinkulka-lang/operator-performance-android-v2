package com.jk.operatorperformance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.program.V74Pointer

internal fun positionConfirmation(block: Int, blockWeek: Int, day: Int): String =
    "Block ${V74Pointer.roman(block)} · Week $blockWeek · Day $day"

@Composable
internal fun StartPositionFlow(
    onDismiss: () -> Unit,
    onConfirm: (block: Int, blockWeek: Int, day: Int) -> Unit,
) {
    var block by rememberSaveable { mutableIntStateOf(1) }
    var blockWeek by rememberSaveable { mutableIntStateOf(1) }
    var day by rememberSaveable { mutableIntStateOf(1) }
    var confirming by rememberSaveable { mutableStateOf(false) }
    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Start at this position?") },
            text = {
                Text(
                    positionConfirmation(block, blockWeek, day),
                    modifier = Modifier.testTag("confirm_start_position_text"),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onConfirm(block, blockWeek, day) },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_start_position"),
                ) { Text("START") }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirming = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("BACK") }
            },
        )
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("START AT A POSITION") },
        text = {
            Column(
                Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("start_position_picker"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Block")
                PositionChoices(count = 4, selected = block, tagPrefix = "position_block", label = { V74Pointer.roman(it) }) { block = it }
                Text("Block week")
                PositionChoices(count = 8, selected = blockWeek, tagPrefix = "position_week", label = { it.toString() }) { blockWeek = it }
                Text("Day")
                PositionChoices(count = 7, selected = day, tagPrefix = "position_day", label = { it.toString() }) { day = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { confirming = true },
                modifier = Modifier.heightIn(min = 48.dp).testTag("position_continue"),
            ) { Text("CONTINUE") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") }
        },
    )
}

@Composable
private fun PositionChoices(
    count: Int,
    selected: Int,
    tagPrefix: String,
    label: (Int) -> String,
    onSelect: (Int) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (choice in 1..count) {
            FilterChip(
                selected = selected == choice,
                onClick = { onSelect(choice) },
                label = { Text(label(choice)) },
                modifier = Modifier.heightIn(min = 48.dp).testTag("${tagPrefix}_$choice"),
            )
        }
    }
}
