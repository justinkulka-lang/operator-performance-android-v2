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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.ProgramNotesV71
import com.jk.operatorperformance.program.ProgramNotesV73
import com.jk.operatorperformance.program.ProgramNotesV72
import com.jk.operatorperformance.state.LegacyDataManager
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ToolsDataState
import com.jk.operatorperformance.state.ToolsDataStore
import com.jk.operatorperformance.tools.PlateInventoryItem
import com.jk.operatorperformance.tools.PlateLoad
import com.jk.operatorperformance.tools.PlateMath
import com.jk.operatorperformance.tools.PlateMathResult
import com.jk.operatorperformance.tools.UnitSystem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

private data class PlateCalculationInput(
    val target: Double, val equipment: Double, val collars: Double?,
    val inventory: List<PlateInventoryItem>,
)

private data class PlateCalculationState(
    val input: PlateCalculationInput? = null, val working: Boolean = false,
    val result: PlateMathResult? = null, val error: String? = null,
)

@Composable
internal fun AdditionalToolsCards(authority: ProgramAuthority = ProgramAuthority.V73) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PlateCalculatorCard()
        ProgramNotesCard(authority)
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
    var calculationState by remember { mutableStateOf(PlateCalculationState()) }

    fun persist(next: ToolsDataState) {
        state = next
        store.save(next)
        selectedCandidate = null
    }

    val equipment = equipmentDraft.toDoubleOrNull()?.takeIf(PlateMath::validEquipmentWeight)
    val collars = collarDraft.toDoubleOrNull()?.takeIf(PlateMath::validEquipmentWeight)
    val target = targetDraft.toDoubleOrNull()?.takeIf(PlateMath::validTargetLoad)
    val input = if (target != null && equipment != null && (collarDraft.isBlank() || collars != null))
        PlateCalculationInput(target, equipment, collars, state.plateInventory) else null
    LaunchedEffect(input) {
        selectedCandidate = null
        calculationState = PlateCalculationState(input, working = input != null)
        if (input != null) {
            try {
                val result = withContext(Dispatchers.Default) {
                    val context = currentCoroutineContext()
                    PlateMath.calculate(input.target, input.equipment, input.collars, input.inventory) { context.ensureActive() }
                }
                calculationState = PlateCalculationState(input, result = result)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (invalid: IllegalArgumentException) {
                calculationState = PlateCalculationState(input, error = invalid.message)
            }
        }
    }
    val currentCalculation = calculationState.takeIf { it.input == input }
    val calculation = currentCalculation?.result
    val working = input != null && (currentCalculation == null || currentCalculation.working)
    val displayedLoad = calculation?.exact ?: selectedCandidate.takeIf { calculation != null }

    Card(modifier = Modifier.fillMaxWidth().testTag("plate_math_tool")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PLATE CALCULATOR", style = MaterialTheme.typography.titleSmall)
            Text("Standalone utility. Workout-row plate-math eligibility is not inferred.")
            state.plateInventoryWarning?.let { Text(it) }
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
                isError = equipmentDraft.isNotBlank() && equipment == null,
                supportingText = { Text("0–1000 ${state.unitSystem.name}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("plate_equipment_weight"),
            )
            OutlinedTextField(
                value = collarDraft,
                onValueChange = { collarDraft = numericDecimal(it) },
                label = { Text("Collar pair total (${state.unitSystem.name}, optional)") },
                isError = collarDraft.isNotBlank() && collars == null,
                supportingText = { Text("0–1000 ${state.unitSystem.name}, or leave blank") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("plate_collar_weight"),
            )
            Button(
                onClick = { persist(state.copy(equipmentEffectiveWeight = equipment, collarPairWeight = collars)) },
                enabled = equipment != null && (collarDraft.isBlank() || collars != null),
                colors = legendButtonColors(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plate_config_save"),
            ) { Text("SAVE EQUIPMENT") }

            Text("AVAILABLE PLATE PAIRS", style = MaterialTheme.typography.titleSmall)
            state.plateInventory.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${formatWeight(item.weight)} ${state.unitSystem.name} × ${item.pairs} pair${if (item.pairs == 1) "" else "s"}",
                        style = technicalStyle(MaterialTheme.typography.bodyMedium),
                    )
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
                    supportingText = { Text("0.001–200 ${state.unitSystem.name}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("plate_inventory_weight"),
                )
                OutlinedTextField(
                    value = pairDraft,
                    onValueChange = { pairDraft = it.filter(Char::isDigit) },
                    label = { Text("Pairs") },
                    isError = pairDraft.isNotBlank() && pairDraft.toIntOrNull()?.let { it in 1..PlateMath.MAX_PAIRS_PER_WEIGHT } != true,
                    supportingText = { Text("1–20 pairs") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("plate_inventory_pairs"),
                )
            }
            val newPlate = plateDraft.toDoubleOrNull()?.takeIf(PlateMath::validPlateWeight)
            val newPairs = pairDraft.toIntOrNull()?.takeIf { it in 1..PlateMath.MAX_PAIRS_PER_WEIGHT }
            val roomForPlate = state.plateInventory.size < PlateMath.MAX_PLATE_TYPES || state.plateInventory.any { it.weight == newPlate }
            if (!roomForPlate) Text("Use at most 12 plate types. Remove a type before adding another.")
            Button(
                onClick = {
                    val item = PlateInventoryItem(requireNotNull(newPlate), requireNotNull(newPairs))
                    val next = (state.plateInventory.filterNot { it.weight == item.weight } + item).sortedByDescending { it.weight }
                    persist(state.copy(plateInventory = next, plateInventoryWarning = null))
                    plateDraft = ""
                    pairDraft = ""
                },
                enabled = newPlate != null && newPairs != null && roomForPlate,
                colors = legendButtonColors(),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plate_inventory_add"),
            ) { Text("ADD / REPLACE PLATE") }

            OutlinedTextField(
                value = targetDraft,
                onValueChange = {
                    targetDraft = numericDecimal(it)
                    selectedCandidate = null
                },
                label = { Text("Target total load (${state.unitSystem.name})") },
                isError = targetDraft.isNotBlank() && target == null,
                supportingText = { Text("Positive total, at most 10000 ${state.unitSystem.name}") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("plate_target"),
            )
            when {
                targetDraft.isNotBlank() && target == null -> Text("Enter a positive target load up to 10000.")
                equipment == null -> Text("Configure the bar/equipment effective weight before calculating.")
                collarDraft.isNotBlank() && collars == null -> Text("Enter a collar pair weight from 0 to 1000, or leave it blank.")
                working -> Text("Calculating…", modifier = Modifier.testTag("plate_calculating"))
                currentCalculation?.error != null -> Text(requireNotNull(currentCalculation.error), modifier = Modifier.testTag("plate_calculation_error"))
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
        Text("$label • ${formatWeight(load.totalLoad)} total", style = technicalStyle(MaterialTheme.typography.titleSmall))
        if (load.perSide.isEmpty()) Text("No plates required per side.")
        else load.perSide.forEach { (weight, count) ->
            Text("Per side: ${formatWeight(weight)} × $count", style = technicalStyle(MaterialTheme.typography.bodyMedium))
        }
    }
}

@Composable
private fun ProgramNotesCard(authority: ProgramAuthority) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().testTag("program_notes_tool")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("PROGRAM / COACHING NOTES", style = MaterialTheme.typography.titleSmall)
            Text("Execution, progression, interference, sprint-volume, run-impact, and planned-overlay guidance for v${authority.versionLabel}. No performance logging required.")
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("program_notes_toggle"),
            ) { Text(if (expanded) "HIDE NOTES" else "SHOW NOTES") }
            if (expanded) {
                NotesSection("SAFETY / PARTICIPATION", (if (authority == ProgramAuthority.V73) ProgramNotesV73.safety else ProgramNotesV72.safety))
                NotesSection("RESISTANCE EXECUTION", (if (authority == ProgramAuthority.V73) ProgramNotesV73.resistanceExecution else ProgramNotesV72.resistanceExecution))
                NotesSection("RECOVERY / REVIEW", (if (authority == ProgramAuthority.V73) ProgramNotesV73.recovery else ProgramNotesV72.recovery))
                NotesSection("RUNNING EXECUTION", (if (authority == ProgramAuthority.V73) ProgramNotesV73.runningExecution else ProgramNotesV72.runningExecution))
                NotesSection("EXECUTION", (if (authority == ProgramAuthority.V73) ProgramNotesV73.execution else if (authority == ProgramAuthority.V71) ProgramNotesV71.execution else ProgramNotesV72.execution))
                NotesSection("RESISTANCE PROGRESSION", (if (authority == ProgramAuthority.V73) ProgramNotesV73.resistanceProgression else if (authority == ProgramAuthority.V71) ProgramNotesV71.resistanceProgression else ProgramNotesV72.resistanceProgression))
                NotesSection("INTERFERENCE", (if (authority == ProgramAuthority.V73) ProgramNotesV73.interference else if (authority == ProgramAuthority.V71) ProgramNotesV71.interference else ProgramNotesV72.interference))
                NotesSection("SPRINT VOLUME", (if (authority == ProgramAuthority.V73) ProgramNotesV73.sprintVolume else if (authority == ProgramAuthority.V71) ProgramNotesV71.sprintVolume else ProgramNotesV72.sprintVolume))
                NotesSection("RUN / IMPACT", (if (authority == ProgramAuthority.V73) ProgramNotesV73.runImpact else if (authority == ProgramAuthority.V71) ProgramNotesV71.runImpact else ProgramNotesV72.runImpact))
                Text("300 m • 46 s TARGET • AVERAGE-VELOCITY REFERENCES", style = MaterialTheme.typography.titleSmall)
                ProgramNotesV72.sprintReference.forEach { (distance, time) ->
                    Row(Modifier.fillMaxWidth().testTag("sprint_reference_${distance.substringBefore(' ')}"), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(distance)
                        Text(time, style = technicalStyle(MaterialTheme.typography.bodyMedium))
                    }
                }
                Text(ProgramNotesV72.sprintReferenceLimit)
                Text("Percentage repetitions use the current pain-free 300 m result, not the 46 s target. Acceleration and flying repetitions have no mandatory split.")
                NotesSection("PLANNED OVERLAYS", (if (authority == ProgramAuthority.V73) ProgramNotesV73.plannedOverlays else if (authority == ProgramAuthority.V71) ProgramNotesV71.plannedOverlays else ProgramNotesV72.plannedOverlays))
                NotesSection("AUTHORITY LIMITS", (if (authority == ProgramAuthority.V73) ProgramNotesV73.authorityLimits else if (authority == ProgramAuthority.V71) ProgramNotesV71.authorityLimits else ProgramNotesV72.authorityLimits))
            }
        }
    }
}

@Composable
private fun NotesSection(title: String, rows: List<String>) {
    Text(title, style = MaterialTheme.typography.titleSmall)
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
            Text("DATA / RESET", style = MaterialTheme.typography.titleSmall)
            Text("Legacy export is local-only. Includes retained legacy reference and tracking data plus plate-calculator settings. Excludes current program authority, pointer, session status, substitutions, and timers. Reset and legacy deletion require confirmation.", modifier = Modifier.testTag("legacy_export_scope"))
            Button(
                onClick = {
                    pendingExport = legacy.buildLocalExport()
                    exportLauncher.launch("legend-legacy-export.json")
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("export_data"),
            ) { Text("EXPORT LEGACY DATA") }
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
            text = { Text("This starts LEGEND v7.3 at Week 1, Day 1. It clears current session status, substitutions, workout timer, engagement evidence and historical week-authority assignments. Program content, retained legacy data and utility settings remain.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        stateStore.clearWorkout()
                        stateStore.clearSessionStatus()
                        stateStore.save(stateStore.load().resetToV73Start(), replaceHistoricalAuthorities = true)
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
