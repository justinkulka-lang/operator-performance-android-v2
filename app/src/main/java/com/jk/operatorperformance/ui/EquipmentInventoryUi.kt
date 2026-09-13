package com.jk.operatorperformance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.program.MachineModality
import com.jk.operatorperformance.program.OperatorProgramV54

@Composable
internal fun MachineEquipmentInventoryCard(
    configured: Boolean,
    available: Set<MachineModality>,
    onChanged: (Boolean, Set<MachineModality>) -> Unit,
) {
    val ranked = OperatorProgramV54.machinePowerFinisher.rankedModalities
    val resolved = ranked.firstOrNull { it in available }

    Card(modifier = Modifier.fillMaxWidth().testTag("machine_inventory_tool")) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("MACHINE POWER FINISHER EQUIPMENT", fontWeight = FontWeight.Bold)
            Text("Used only after ACCELERATION or MAX VELOCITY. Priority is fixed by v5.4; equipment availability does not change the running prescription.")
            Text(
                "Priority: " + ranked.joinToString(" → ") { it.label },
                style = MaterialTheme.typography.bodySmall,
            )

            when {
                !configured -> Text("STATUS • NOT CONFIGURED. The canonical equipment hierarchy remains visible for reference.")
                resolved == null -> Text("STATUS • CONFIGURED • NONE AVAILABLE. The optional machine finisher is omitted.")
                else -> Text("STATUS • CONFIGURED • RESOLVED: ${resolved.label}")
            }

            ranked.forEach { modality ->
                FilterChip(
                    selected = modality in available,
                    onClick = {
                        val next = available.toMutableSet()
                        if (!next.add(modality)) next.remove(modality)
                        onChanged(true, next.toSet())
                    },
                    label = { Text(modality.label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("machine_${modality.name}"),
                )
            }

            TextButton(
                onClick = { onChanged(true, emptySet()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("machine_none_available"),
            ) { Text("NONE AVAILABLE") }

            if (configured) {
                TextButton(
                    onClick = { onChanged(false, emptySet()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("machine_clear_configuration"),
                ) { Text("CLEAR CONFIGURATION") }
            }
        }
    }
}
