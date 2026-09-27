package com.jk.operatorperformance.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.program.ProgramNotesV71
import com.jk.operatorperformance.state.LegacyDataManager
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ToolsDataState
import com.jk.operatorperformance.state.ToolsDataStore
import com.jk.operatorperformance.tools.PlateInventoryItem
import com.jk.operatorperformance.tools.PlateLoad
import com.jk.operatorperformance.tools.PlateMath
import com.jk.operatorperformance.tools.UnitSystem

@Composable
internal fun AdditionalToolsCards() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PlateCalculatorCard()
        ProgramNotesCard()
        DataControlsCard()
    }
}

@Composable
private fun PlateCalculatorCard() {
    val context = LocalContext.current.applicationContext
    val store = remember(context) { ToolsDataStore(context) }
    var state by remember { mutableStateOf(store.load()) }
    var equipmentDraft by rememberSaveable { mutableStateOf(state.equipmentEffectiveWeight?.let(::formatWeight).orEmpty()) }
    var collarDraft by rememberSaveable { mutableStateOf(state.collarPairWeight?.let(::formatWeight).orEmpty()) }
    var plateDraft by rememberSaveable { mutableStateOf("") }
    var pairDraft by rememberSaveable { mutableStateOf("") }
    var targetDraft by rememberSaveable { mutableStateOf("") }
    var selectedCandidate by remember { mutableStateOf<PlateLoad?>(null) }

    fun persist(next: ToolsDataState) {
        state = next
        store.save(next)
        selectedCandidate = null
    }

    val equipment = equipmentDraft.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
    val collars = collarDraft.toDoubleOrNull()?.takeIf { it.isFinite() && it >= 0.0 }
    val target = targetDraft.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
    val calculation = if (target != null && equipment != null) {
        runCatching { PlateMath.calculate(target, equipment, collars, state.plateInventory) }.getOrNull()
    } else null
    val displayedLoad = calculation?.exact ?: selectedCandidate

    Card(modifier = Modifier.fillMaxWidth().testTag("plate_math_tool")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PLATE CALCULATOR", fontWeight = FontWeight.Bold)
            Text("Standalone utility. Workout-row plate-math eligibility is not inferred.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UnitSystem.entries.forEach { unit ->
                    FilterChip(
                        selected = state.unitSystem == unit,
                        onClick = {
                            state = store.setUnit(unit)
                            equipmentDraft = state.equipmentEffectiveWeight?.let(::formatWeight).orEmpty()
                            collarDraft = state.collarPairWeight?.let(::formatWeight).orEmpty()
                            targetDraft = ""
                            selectedCandidate = null
                        },
                        label = { Text(unit.name) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("plate_unit_${unit.name}"),
                    )
                }
            }
            OutlinedTextField(
                value = equipmentDraft,
                onValueChange = { equipmentDraft = numericDecimal(it) },
                label = { Text("Bar/equipment effective weight (${state.unitSystem.name})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("plate_equipment_weight"),
            )
            OutlinedTextField(
                value = collarDraft,
                onValueChange = { collarDraft = numericDecimal(it) },
                label = { Text("Collar pair total (${state.unitSystem.name}, optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("plate_collar_weight"),
            )
            Button(
                onClick = { persist(state.copy(equipmentEffectiveWeight = equipment, collarPairWeight = collars)) },
                enabled = equipment != null && (collarDraft.isBlank() || collars != null),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plate_config_save"),
            ) { Text("SAVE EQUIPMENT") }

            Text("AVAILABLE PLATE PAIRS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            state.plateInventory.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${formatWeight(item.weight)} ${state.unitSystem.name} × ${item.pairs} pair${if (item.pairs == 1) "" else "s"}")
                    TextButton(
                        onClick = { persist(state.copy(plateInventory = state.plateInventory.filterNot { it.weight == item.weight })) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text("REMOVE") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = plateDraft,
                    onValueChange = { plateDraft = numericDecimal(it) },
                    label = { Text("Plate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("plate_inventory_weight"),
                )
                OutlinedTextField(
                    value = pairDraft,
                    onValueChange = { pairDraft = it.filter(Char::isDigit) },
                    label = { Text("Pairs") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("plate_inventory_pairs"),
                )
            }
            val newPlate = plateDraft.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0.0 }
            val newPairs = pairDraft.toIntOrNull()?.takeIf { it > 0 }
            Button(
                onClick = {
                    val item = PlateInventoryItem(requireNotNull(newPlate), requireNotNull(newPairs))
                    val next = (state.plateInventory.filterNot { it.weight == item.weight } + item).sortedByDescending { it.weight }
                    persist(state.copy(plateInventory = next))
                    plateDraft = ""
                    pairDraft = ""
                },
                enabled = newPlate != null && newPairs != null,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plate_inventory_add"),
            ) { Text("ADD / REPLACE PLATE") }

            OutlinedTextField(
                value = targetDraft,
                onValueChange = {
                    targetDraft = numericDecimal(it)
                    selectedCandidate = null
                },
                label = { Text("Target total load (${state.unitSystem.name})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("plate_target"),
            )
            when {
                targetDraft.isNotBlank() && target == null -> Text("Enter a positive target load.")
                equipment == null -> Text("Configure the bar/equipment effective weight before calculating.")
                calculation?.exact != null -> PlateLoadResult("EXACT", calculation.exact)
                calculation != null -> {
                    Text("Target is not exactly achievable with the configured inventory. Select a nearby achievable load; none is chosen automatically.")
                    calculation.lower?.let { lower ->
                        TextButton(
                            onClick = { selectedCandidate = lower },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plate_use_lower"),
                        ) { Text("USE LOWER • ${formatWeight(lower.totalLoad)} ${state.unitSystem.name}") }
                    }
                    calculation.higher?.let { higher ->
                        TextButton(
                            onClick = { selectedCandidate = higher },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plate_use_higher"),
                        ) { Text("USE HIGHER • ${formatWeight(higher.totalLoad)} ${state.unitSystem.name}") }
                    }
                    displayedLoad?.let { PlateLoadResult("SELECTED", it) }
                }
            }
        }
    }
}

@Composable
private fun PlateLoadResult(label: String, load: PlateLoad) {
    Column(modifier = Modifier.fillMaxWidth().testTag("plate_result")) {
        Text("$label • ${formatWeight(load.totalLoad)} total", fontWeight = FontWeight.Bold)
        if (load.perSide.isEmpty()) Text("No plates required per side.")
        else load.perSide.forEach { (weight, count) -> Text("Per side: ${formatWeight(weight)} × $count") }
    }
}

@Composable
private fun ProgramNotesCard() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().testTag("program_notes_tool")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PROGRAM / COACHING NOTES", fontWeight = FontWeight.Bold)
            Text("Execution, progression, interference, sprint-volume, run-impact, and planned-overlay guidance from the approved ARES v7.1 authority. No performance logging required.")
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("program_notes_toggle"),
            ) { Text(if (expanded) "HIDE NOTES" else "SHOW NOTES") }
            if (expanded) {
                NotesSection("EXECUTION", ProgramNotesV71.execution)
                NotesSection("RESISTANCE PROGRESSION", ProgramNotesV71.resistanceProgression)
                NotesSection("INTERFERENCE", ProgramNotesV71.interference)
                NotesSection("SPRINT VOLUME", ProgramNotesV71.sprintVolume)
                NotesSection("RUN / IMPACT", ProgramNotesV71.runImpact)
                NotesSection("PLANNED OVERLAYS", ProgramNotesV71.plannedOverlays)
                NotesSection("AUTHORITY LIMITS", ProgramNotesV71.authorityLimits)
            }
        }
    }
}

@Composable
private fun NotesSection(title: String, rows: List<String>) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    rows.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
}

@Composable
private fun DataControlsCard() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val legacy = remember(appContext) { LegacyDataManager(appContext) }
    val stateStore = remember(appContext) { ProgramStateStore(appContext) }
    var legacyPresent by remember { mutableStateOf(legacy.hasLegacyTrackingData()) }
    var pendingExport by remember { mutableStateOf<String?>(null) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val payload = pendingExport
        if (uri != null && payload != null) {
            exportStatus = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { it.write(payload) }
                    ?: error("Unable to open export destination")
                "Export saved."
            }.getOrElse { "Export failed." }
        }
        pendingExport = null
    }

    Card(modifier = Modifier.fillMaxWidth().testTag("data_controls_tool")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("DATA / RESET", fontWeight = FontWeight.Bold)
            Text("Export is local-only. Reset and legacy deletion require confirmation.")
            Button(
                onClick = {
                    pendingExport = legacy.buildLocalExport()
                    exportLauncher.launch("ares-performance-v7.1-local-export.json")
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("export_data"),
            ) { Text("EXPORT DATA") }
            exportStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            TextButton(
                onClick = { confirmReset = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("reset_pointer"),
            ) { Text("RESET CURRENT POINTER") }
            if (legacyPresent) {
                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("delete_legacy_data"),
                ) { Text("DELETE LEGACY TRACKING DATA") }
            } else Text("No retained legacy tracking data detected.", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset current program pointer?") },
            text = { Text("This sets the current reference pointer to Week 1, Day 1 and clears any active workout focus pointer. It does not erase program content, retained v5.7 data, or utility settings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        stateStore.clearWorkout()
                        stateStore.save(stateStore.load().withCurrentPointer(1, 1))
                        confirmReset = false
                        context.findActivity()?.recreate()
                    },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_reset_pointer"),
                ) { Text("RESET") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") } },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete legacy tracking data?") },
            text = { Text("This permanently clears retained v0.34 tracking/reference preference stores. Export first if you want a local copy. Declining deletion does not enable any legacy feature.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (legacy.deleteLegacyTrackingData()) legacyPresent = false
                        confirmDelete = false
                    },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_delete_legacy_data"),
                ) { Text("DELETE") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") } },
        )
    }
}

private fun numericDecimal(value: String): String {
    var dotSeen = false
    return buildString {
        value.forEach { c ->
            when {
                c.isDigit() -> append(c)
                c == '.' && !dotSeen -> { append(c); dotSeen = true }
            }
        }
    }
}

private fun formatWeight(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString()
else value.toString().trimEnd('0').trimEnd('.')

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
