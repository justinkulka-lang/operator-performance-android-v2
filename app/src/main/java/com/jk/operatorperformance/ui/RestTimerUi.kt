package com.jk.operatorperformance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
internal fun RestTimerToolsCard(
    configuredSeconds: Int?,
    activeEndEpochMillis: Long?,
    onConfiguredSecondsChanged: (Int?) -> Unit,
    onActiveEndChanged: (Long?) -> Unit,
) {
    var draft by rememberSaveable(configuredSeconds) {
        mutableStateOf(configuredSeconds?.toString().orEmpty())
    }
    val parsed = draft.toIntOrNull()?.takeIf { it > 0 }
    val invalid = draft.isNotBlank() && parsed == null

    Card(modifier = Modifier.fillMaxWidth().testTag("rest_timer_tool")) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("REST TIMER", fontWeight = FontWeight.Bold)
            Text("Optional independent timer. A saved duration may also auto-start after non-final resistance sets.")
            OutlinedTextField(
                value = draft,
                onValueChange = { value -> draft = value.filter(Char::isDigit) },
                label = { Text("Seconds") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = invalid,
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("rest_timer_input"),
            )
            if (invalid) Text("Enter a positive number of seconds.", style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { onConfiguredSecondsChanged(requireNotNull(parsed)) },
                    enabled = parsed != null,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("rest_timer_save"),
                ) { Text("SAVE") }
                if (configuredSeconds != null) {
                    TextButton(
                        onClick = {
                            draft = ""
                            onConfiguredSecondsChanged(null)
                            onActiveEndChanged(null)
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("rest_timer_disable"),
                    ) { Text("DISABLE") }
                }
            }

            if (configuredSeconds != null && activeEndEpochMillis == null) {
                Button(
                    onClick = {
                        onActiveEndChanged(System.currentTimeMillis() + configuredSeconds.toLong() * 1_000L)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("rest_timer_start"),
                ) { Text("START TIMER") }
            }

            if (activeEndEpochMillis != null) {
                RestCountdown(
                    endEpochMillis = activeEndEpochMillis,
                    testTag = "utility_rest_countdown",
                    onFinished = { onActiveEndChanged(null) },
                )
                TextButton(
                    onClick = { onActiveEndChanged(null) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("rest_timer_cancel"),
                ) { Text("CANCEL TIMER") }
            }
        }
    }
}

@Composable
internal fun WorkoutRestPanel(
    endEpochMillis: Long,
    onFinished: () -> Unit,
    onSkip: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().testTag("workout_rest")) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("REST", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            RestCountdown(
                endEpochMillis = endEpochMillis,
                testTag = "workout_rest_countdown",
                onFinished = onFinished,
            )
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("workout_rest_skip"),
            ) { Text("SKIP REST") }
        }
    }
}

@Composable
private fun RestCountdown(
    endEpochMillis: Long,
    testTag: String,
    onFinished: () -> Unit,
) {
    var remaining by remember(endEpochMillis) {
        mutableIntStateOf(remainingSeconds(endEpochMillis, System.currentTimeMillis()))
    }

    LaunchedEffect(endEpochMillis) {
        while (true) {
            val next = remainingSeconds(endEpochMillis, System.currentTimeMillis())
            remaining = next
            if (next <= 0) {
                onFinished()
                break
            }
            delay(250L)
        }
    }

    Text(
        text = formatSeconds(remaining),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.testTag(testTag),
    )
}

private fun remainingSeconds(endEpochMillis: Long, nowMillis: Long): Int {
    val remainingMillis = max(0L, endEpochMillis - nowMillis)
    return ((remainingMillis + 999L) / 1_000L).toInt()
}

private fun formatSeconds(seconds: Int): String {
    val minutes = seconds / 60
    val remainder = seconds % 60
    return "%d:%02d".format(minutes, remainder)
}
