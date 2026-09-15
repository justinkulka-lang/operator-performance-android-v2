package com.jk.operatorperformance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.OperatorProgramV56
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.program.SpeedVariant
import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.WorkoutProgress
import kotlinx.coroutines.delay

private val AresDarkColors = darkColorScheme(
    primary = Color(0xFFD7A93A),
    onPrimary = Color(0xFF161009),
    primaryContainer = Color(0xFF3A2A12),
    onPrimaryContainer = Color(0xFFFFE7A5),
    secondary = Color(0xFFB8BEC8),
    background = Color(0xFF0B0D10),
    onBackground = Color(0xFFF4F5F7),
    surface = Color(0xFF13171D),
    onSurface = Color(0xFFF4F5F7),
    surfaceVariant = Color(0xFF20262E),
    onSurfaceVariant = Color(0xFFB8BEC8),
    tertiary = Color(0xFF69D18B),
    error = Color(0xFFF0C55B),
)

private enum class Destination(val label: String) {
    TODAY("TODAY"), WEEK("WEEK"), PROGRAM("PROGRAM"), TOOLS("TOOLS")
}

@Composable
fun OperatorPerformanceApp(stateStore: ProgramStateStore) {
    var state by remember { mutableStateOf(stateStore.load()) }
    var activeWorkout by remember { mutableStateOf(stateStore.loadWorkout()) }
    var workoutMode by rememberSaveable { mutableStateOf(false) }
    var destinationName by rememberSaveable { mutableStateOf(Destination.TODAY.name) }
    var selectedProgramWeek by rememberSaveable { mutableIntStateOf(state.currentWeek) }
    var referenceSession by remember { mutableStateOf<SessionReference?>(null) }
    var referenceCanSetCurrent by remember { mutableStateOf(false) }
    var pendingPointer by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    fun persist(next: ProgramUiState) {
        state = next
        stateStore.save(next)
    }

    fun persistWorkout(next: ActiveWorkoutState) {
        activeWorkout = next
        stateStore.saveWorkout(next)
    }

    fun clearWorkout() {
        activeWorkout = null
        stateStore.clearWorkout()
    }

    MaterialTheme(colorScheme = AresDarkColors) {
        if (workoutMode) {
            val progress = activeWorkout
            if (progress == null) {
                workoutMode = false
            } else {
                WorkoutModeScreen(
                    progress = progress,
                    onProgress = ::persistWorkout,
                    onReturnToToday = { workoutMode = false },
                )
            }
            return@MaterialTheme
        }

        val destination = Destination.valueOf(destinationName)
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Destination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destinationName = item.name },
                            icon = {
                                Icon(
                                    imageVector = when (item) {
                                        Destination.TODAY -> Icons.Default.Home
                                        Destination.WEEK -> Icons.Default.DateRange
                                        Destination.PROGRAM -> Icons.Default.ListAlt
                                        Destination.TOOLS -> Icons.Default.Build
                                    },
                                    contentDescription = null,
                                )
                            },
                            label = { Text(item.label) },
                            alwaysShowLabel = true,
                            modifier = Modifier.testTag("nav_${item.name}"),
                        )
                    }
                }
            },
        ) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
                when (destination) {
                    Destination.TODAY -> TodayScreen(
                        state = state,
                        activeWorkout = activeWorkout?.takeIf { it.matchesPointer(state) },
                        onStartWorkout = {
                            val existing = activeWorkout?.takeIf { it.matchesPointer(state) }
                            val next = existing ?: newWorkout(state).also(::persistWorkout)
                            activeWorkout = next
                            workoutMode = true
                        },
                        onProgress = ::persistWorkout,
                        onAdvance = {
                            clearWorkout()
                            persist(state.advanceDay())
                            selectedProgramWeek = state.advanceDay().currentWeek
                        },
                    )
                    Destination.WEEK -> WeekScreen(
                        state = state,
                        onOpenDay = {
                            referenceCanSetCurrent = false
                            referenceSession = it
                        },
                    )
                    Destination.PROGRAM -> ProgramScreen(
                        state = state,
                        selectedWeek = selectedProgramWeek,
                        onSelectWeek = { selectedProgramWeek = it },
                        onOpenDay = {
                            referenceCanSetCurrent = true
                            referenceSession = it
                        },
                    )
                    Destination.TOOLS -> ToolsScreen(
                        state = state,
                        onStateChanged = { next ->
                            if (next.speedVariant != state.speedVariant || next.engineVariant != state.engineVariant) clearWorkout()
                            persist(next)
                        },
                    )
                }
            }
        }

        referenceSession?.let { session ->
            DayReferenceDialog(
                session = session,
                state = state,
                allowSetCurrent = referenceCanSetCurrent && (session.week != state.currentWeek || session.day != state.currentDay),
                onDismiss = { referenceSession = null },
                onRequestSetCurrent = { pendingPointer = session.week to session.day },
            )
        }

        pendingPointer?.let { (week, day) ->
            AlertDialog(
                onDismissRequest = { pendingPointer = null },
                title = { Text("Change current program day?") },
                text = { Text("Set the current pointer to Week $week, Day $day? This changes navigation state only.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            clearWorkout()
                            persist(state.withCurrentPointer(week, day))
                            selectedProgramWeek = week
                            pendingPointer = null
                            referenceSession = null
                        },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_set_current"),
                    ) { Text("CONFIRM") }
                },
                dismissButton = { TextButton(onClick = { pendingPointer = null }) { Text("CANCEL") } },
            )
        }
    }
}

@Composable
private fun TodayScreen(
    state: ProgramUiState,
    activeWorkout: ActiveWorkoutState?,
    onStartWorkout: () -> Unit,
    onProgress: (ActiveWorkoutState) -> Unit,
    onAdvance: () -> Unit,
) {
    val session = resolveSession(state.currentWeek, state.currentDay, state)
    val progress = activeWorkout?.let { WorkoutProgress.normalize(it, session) }
    val completed = progress?.let { WorkoutProgress.completedEfforts(it, session) } ?: 0
    val total = WorkoutProgress.totalEfforts(session)
    val atEnd = state.currentWeek == 32 && state.currentDay == 7
    val isRest = state.currentDay == 7
    val executionGuidance = sessionExecutionGuidance(session)
    val foundationReference = foundationReferenceLabel(session, state)
    var confirmAdvance by rememberSaveable(state.currentWeek, state.currentDay) { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("today_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            BrandHeader()
            PhaseIdentityLine(session.phase, Modifier.testTag("today_phase"))
            Text(
                "WEEK ${session.week} • DAY ${session.day}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("today_header"),
            )
            Text(session.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (state.deloadMode) Text("DELOAD MODE", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            if (!isRest) Text(
                if (progress == null) "$total SETS / EFFORTS PLANNED" else "$completed / $total COMPLETE",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("today_progress"),
            )
        }
        if (executionGuidance.isNotEmpty()) {
            item { SessionExecutionBanner(session) }
        }
        if (foundationReference != null) {
            item { FoundationReferenceBanner(session, state) }
        }
        items(session.rows, key = { it.id }) { row ->
            WorkoutRowCard(
                row = row,
                progress = progress,
                onToggleEffort = { effort ->
                    val base = progress ?: newWorkout(state)
                    onProgress(WorkoutProgress.toggleEffort(base, session, row.id, effort))
                },
                onSelectAlternative = { alternative ->
                    val base = progress ?: newWorkout(state)
                    val choice = if (base.selectedSubstitutions[row.id] == alternative) null else alternative
                    onProgress(WorkoutProgress.selectSubstitution(base, session, row.id, choice))
                },
            )
        }
        item {
            if (!isRest) {
                Button(
                    onClick = onStartWorkout,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("start_resume_workout"),
                ) { Text(if (progress == null) "START WORKOUT" else "RESUME WORKOUT", fontWeight = FontWeight.Bold) }
                Spacer(Modifier.height(8.dp))
            }
            if (atEnd) {
                Card(Modifier.fillMaxWidth()) {
                    Text("ARES PERFORMANCE • 32-WEEK PROGRAM COMPLETE", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { if (isRest || progress?.complete == true) onAdvance() else confirmAdvance = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("complete_day"),
                ) { Text("COMPLETE DAY / NEXT DAY") }
            }
        }
    }

    if (confirmAdvance) {
        val remaining = (total - completed).coerceAtLeast(0)
        AlertDialog(
            onDismissRequest = { confirmAdvance = false },
            title = { Text("Advance with unfinished work?") },
            text = { Text("$remaining prescribed sets/efforts remain incomplete. Advance anyway?") },
            confirmButton = {
                TextButton(
                    onClick = { confirmAdvance = false; onAdvance() },
                    modifier = Modifier.testTag("confirm_incomplete_advance"),
                ) { Text("ADVANCE") }
            },
            dismissButton = { TextButton(onClick = { confirmAdvance = false }) { Text("CANCEL") } },
        )
    }
}

@Composable
private fun WorkoutRowCard(
    row: SessionRow,
    progress: ActiveWorkoutState?,
    onToggleEffort: (Int) -> Unit,
    onSelectAlternative: (String) -> Unit,
) {
    var showDetails by rememberSaveable(row.id) { mutableStateOf(false) }
    val selected = progress?.selectedSubstitutions?.get(row.id)
    val title = selected ?: row.title
    val allDone = progress != null && rowIsComplete(progress, row)
    val hasDetails = row.note != null || row.alternatives.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth().testTag("row_${row.id}"),
        colors = CardDefaults.cardColors(containerColor = if (allDone) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VisualRowTitle(row = row, title = title, style = MaterialTheme.typography.titleMedium)
            if (selected != null) Text("ALTERNATIVE FOR ${row.title}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            Text(row.prescription, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("prescription_${row.id}"))
            if (row.optional) Text("OPTIONAL", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            if (row.kind != SessionRowKind.REST) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((1..row.effortCount).toList()) { effort ->
                        val done = progress?.let { WorkoutProgress.isEffortComplete(it, row.id, effort) } == true
                        FilterChip(
                            selected = done,
                            onClick = { onToggleEffort(effort) },
                            label = { Text(if (done) "✓ ${row.effortLabel} $effort" else "${row.effortLabel} $effort") },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("effort_${row.id}_$effort"),
                        )
                    }
                }
            }
            if (hasDetails) {
                val detailLabel = when {
                    row.note != null && row.alternatives.isNotEmpty() -> "COACHING / ALTERNATIVES"
                    row.note != null -> "COACHING"
                    else -> "ALTERNATIVES"
                }
                TextButton(
                    onClick = { showDetails = !showDetails },
                    modifier = Modifier.heightIn(min = 48.dp).testTag(if (row.alternatives.isNotEmpty()) "alternatives_${row.id}" else "coaching_${row.id}"),
                ) {
                    Text(if (showDetails) "HIDE $detailLabel" else detailLabel)
                }
                if (showDetails) {
                    row.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    row.alternatives.forEachIndexed { index, alternative ->
                        FilterChip(
                            selected = selected == alternative,
                            onClick = { onSelectAlternative(alternative) },
                            label = { Text("${index + 1}. $alternative") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutModeScreen(
    progress: ActiveWorkoutState,
    onProgress: (ActiveWorkoutState) -> Unit,
    onReturnToToday: () -> Unit,
) {
    val session = SessionResolver.resolve(
        week = progress.week,
        day = progress.day,
        deload = progress.deloadMode,
        machineInventoryConfigured = progress.machineInventoryConfigured,
        availableMachineModalities = progress.availableMachineModalities,
        speedVariant = progress.speedVariant,
        engineVariant = progress.engineVariant,
    )
    val normalized = WorkoutProgress.normalize(progress, session)
    val row = session.rows[normalized.movementIndex]
    val selected = normalized.selectedSubstitutions[row.id]
    val nextMovement = nextIncompleteMovementTitle(normalized, session)
    val haptic = LocalHapticFeedback.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    KeepScreenOnEffect()

    LaunchedEffect(normalized.restTimerEndEpochMillis) {
        val end = normalized.restTimerEndEpochMillis
        while (end != null && end > System.currentTimeMillis()) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
        if (end != null && end <= System.currentTimeMillis()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onProgress(WorkoutProgress.clearExpiredRest(normalized, System.currentTimeMillis()))
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (normalized.complete) {
                        Button(
                            onClick = onReturnToToday,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("return_today"),
                        ) { Text("WORKOUT COMPLETE • RETURN TO TODAY", fontWeight = FontWeight.Bold) }
                    } else {
                        Button(
                            onClick = { onProgress(WorkoutProgress.advance(normalized, session)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).testTag("set_done"),
                        ) {
                            Text(if (row.kind == SessionRowKind.RESISTANCE) "SET DONE" else "NEXT EFFORT / DONE", fontWeight = FontWeight.Black)
                        }
                        if (row.optional) {
                            TextButton(
                                onClick = { onProgress(WorkoutProgress.skipOptional(normalized, session)) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("skip_optional"),
                            ) { Text("SKIP OPTIONAL") }
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("workout_mode"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                BrandHeader()
                PhaseIdentityLine(session.phase)
                Text(
                    "WEEK ${session.week} • DAY ${session.day} • ${session.title}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.testTag("workout_context"),
                )
                Text("MOVEMENT ${normalized.movementIndex + 1} / ${session.rows.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (sessionExecutionGuidance(session).isNotEmpty()) {
                item { SessionExecutionBanner(session) }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        VisualRowTitle(
                            row = row,
                            title = selected ?: row.title,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.testTag("workout_movement"),
                        )
                        Text(
                            "${row.effortLabel} ${normalized.effortIndex} / ${row.effortCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.testTag("workout_effort"),
                        )
                        Text(row.prescription, style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("workout_prescription"))
                        row.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        row.alternatives.forEachIndexed { index, alternative ->
                            FilterChip(
                                selected = selected == alternative,
                                onClick = {
                                    val choice = if (selected == alternative) null else alternative
                                    onProgress(WorkoutProgress.selectSubstitution(normalized, session, row.id, choice))
                                },
                                label = { Text("ALT ${index + 1} • $alternative") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            )
                        }
                        nextMovement?.let {
                            HorizontalDivider()
                            Text("NEXT • $it", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            if (normalized.restTimerEndEpochMillis != null) {
                item {
                    val seconds = WorkoutProgress.remainingRestSeconds(normalized, now)
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .testTag("workout_rest")
                            .semantics {
                                contentDescription = "Workout rest timer"
                                stateDescription = "Rest timer remaining ${formatRest(seconds)}"
                                liveRegion = LiveRegionMode.Polite
                            },
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "REST • ${formatRest(seconds)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("workout_rest_countdown"),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { onProgress(WorkoutProgress.skipRest(normalized)) },
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("workout_rest_skip"),
                                ) { Text("SKIP REST") }
                                TextButton(
                                    onClick = { onProgress(WorkoutProgress.addRestSeconds(normalized, 30)) },
                                    modifier = Modifier.heightIn(min = 48.dp).testTag("workout_rest_add_30"),
                                ) { Text("+30 SEC") }
                            }
                        }
                    }
                }
            }
            item {
                if (normalized.canUndo) {
                    TextButton(
                        onClick = { onProgress(WorkoutProgress.undo(normalized)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("undo_workout"),
                    ) { Text("UNDO LAST ACTION") }
                }
                if (!normalized.complete) {
                    TextButton(
                        onClick = onReturnToToday,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("return_today"),
                    ) { Text("RETURN TO TODAY") }
                }
            }
            item {
                HorizontalDivider()
                Text("WORKOUT MAP", fontWeight = FontWeight.Bold)
                session.rows.forEachIndexed { index, mapRow ->
                    val mapState = workoutMapState(normalized, session, index)
                    val marker = when (mapState) {
                        WorkoutMapVisualState.COMPLETE -> "✓"
                        WorkoutMapVisualState.CURRENT -> "▶"
                        WorkoutMapVisualState.UPCOMING -> "○"
                    }
                    val color = when (mapState) {
                        WorkoutMapVisualState.COMPLETE -> MaterialTheme.colorScheme.tertiary
                        WorkoutMapVisualState.CURRENT -> MaterialTheme.colorScheme.primary
                        WorkoutMapVisualState.UPCOMING -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    val mapTitle = normalized.selectedSubstitutions[mapRow.id] ?: mapRow.title
                    Text(
                        "$marker $mapTitle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        fontWeight = if (mapState == WorkoutMapVisualState.CURRENT) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekScreen(state: ProgramUiState, onOpenDay: (SessionReference) -> Unit) {
    val split = splitForWeek(state.currentWeek, state)
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("week_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            BrandHeader()
            val phase = OperatorProgramV56.blockForWeek(state.currentWeek)
            PhaseIdentityLine(phase)
            Text("WEEK ${state.currentWeek} • ${phase.godName} • ${phase.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        items(split, key = { it.day }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("week_day_${session.day}"),
                colors = CardDefaults.cardColors(containerColor = if (session.day == state.currentDay) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
                onClick = { onOpenDay(session) },
            ) {
                SessionDayCardContent(
                    session = session,
                    isCurrentDay = session.day == state.currentDay,
                    showRowCount = true,
                    padding = 16.dp,
                    referenceLabel = foundationReferenceLabel(session, state),
                )
            }
        }
    }
}

@Composable
private fun ProgramScreen(
    state: ProgramUiState,
    selectedWeek: Int,
    onSelectWeek: (Int) -> Unit,
    onOpenDay: (SessionReference) -> Unit,
) {
    val currentPhase = OperatorProgramV56.blockForWeek(state.currentWeek)
    val selectedPhase = OperatorProgramV56.blockForWeek(selectedWeek)
    val selectedSplit = splitForWeek(selectedWeek, state)
    val nextWeek = (state.currentWeek + 1).takeIf { it <= 32 }
    val nextPhase = OperatorProgramV56.blocks.firstOrNull { it.startWeek > currentPhase.endWeek }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("program_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BrandHeader()
            Text("32-WEEK ARES PROGRAM", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            PhaseProgressStrip(currentPhase)
            Text("CURRENT • ${currentPhase.godName} — ${currentPhase.name} • Weeks ${currentPhase.startWeek}–${currentPhase.endWeek}", color = phaseAccent(currentPhase), fontWeight = FontWeight.Bold, modifier = Modifier.testTag("current_phase"))
            Text(
                nextPhase?.let { "NEXT PHASE • ${it.godName} — ${it.name} • POPULATED / APPROVED" }
                    ?: "NEXT PHASE • PROGRAM COMPLETE AT WEEK 32 • NO WEEK 33+ AUTHORIZED",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("next_phase_status"),
            )
        }
        item {
            Text("PHASES", fontWeight = FontWeight.Bold)
            OperatorProgramV56.blocks.forEach { phase ->
                PhaseSummaryCard(phase, isCurrent = phase.godName == currentPhase.godName)
            }
        }
        item {
            HorizontalDivider()
            Text("BROWSE WEEK", fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..32).toList()) { week ->
                    FilterChip(
                        selected = selectedWeek == week,
                        onClick = { onSelectWeek(week) },
                        label = { Text("W$week") },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("program_week_$week"),
                    )
                }
            }
            PhaseIdentityLine(selectedPhase)
        }
        items(selectedSplit, key = { it.day }) { session ->
            Card(onClick = { onOpenDay(session) }, modifier = Modifier.fillMaxWidth().testTag("program_day_${session.week}_${session.day}")) {
                SessionDayCardContent(
                    session = session,
                    isCurrentDay = session.week == state.currentWeek && session.day == state.currentDay,
                    showRowCount = false,
                    referenceLabel = foundationReferenceLabel(session, state),
                )
            }
        }
        if (nextWeek != null) {
            item {
                HorizontalDivider()
                Text("NEXT WEEK • WEEK $nextWeek", fontWeight = FontWeight.Bold, modifier = Modifier.testTag("next_week"))
                val phase = OperatorProgramV56.blockForWeek(nextWeek)
                PhaseIdentityLine(phase)
                splitForWeek(nextWeek, state).forEach { session -> Text("DAY ${session.day} • ${session.title}") }
            }
        }
    }
}

@Composable
private fun ToolsScreen(state: ProgramUiState, onStateChanged: (ProgramUiState) -> Unit) {
    val haptic = LocalHapticFeedback.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.utilityRestTimerEndEpochMillis) {
        val end = state.utilityRestTimerEndEpochMillis
        while (end != null && end > System.currentTimeMillis()) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
        if (end != null && end <= System.currentTimeMillis()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onStateChanged(state.copy(utilityRestTimerEndEpochMillis = null))
        }
    }
    val timerRemaining = state.utilityRestTimerEndEpochMillis?.let { ((it - now).coerceAtLeast(0L) / 1_000L).toInt() } ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("tools_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { BrandHeader(); Text("TOOLS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("MANUAL DELOAD MODE", fontWeight = FontWeight.Bold)
                        Switch(checked = state.deloadMode, onCheckedChange = { onStateChanged(state.copy(deloadMode = it)) }, modifier = Modifier.testTag("deload_switch"))
                    }
                    Text("Reference-driven only. No readiness questionnaire or automatic trigger.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth().testTag("phase1_reference_selectors")) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("FOUNDATION SPEED / ENGINE REFERENCES", fontWeight = FontWeight.Bold)
                    Text("Manual selection applies to Weeks 2–8 only. Week 1 and Weeks 9–32 use their populated canonical prescriptions.", style = MaterialTheme.typography.bodySmall)
                    Text("SPEED", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    SpeedVariant.entries.forEach { variant ->
                        FilterChip(
                            selected = state.speedVariant == variant,
                            onClick = { onStateChanged(state.copy(speedVariant = variant)) },
                            label = { Text(variant.label) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("speed_${variant.name}"),
                        )
                    }
                    Text("ENGINE", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    EngineVariant.entries.forEach { variant ->
                        FilterChip(
                            selected = state.engineVariant == variant,
                            onClick = { onStateChanged(state.copy(engineVariant = variant)) },
                            label = { Text(variant.label) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("engine_${variant.name}"),
                        )
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth().testTag("rest_timer_tool")) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("INDEPENDENT REST TIMER", fontWeight = FontWeight.Bold)
                    Text("Utility only. Resistance rest guidance is approximate and set quality takes precedence.", style = MaterialTheme.typography.bodySmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(60, 90, 120, 180)) { seconds ->
                            FilterChip(
                                selected = state.restTimerSeconds == seconds,
                                onClick = { onStateChanged(state.copy(restTimerSeconds = seconds)) },
                                label = { Text(formatRest(seconds)) },
                                modifier = Modifier.heightIn(min = 48.dp),
                            )
                        }
                    }
                    if (state.utilityRestTimerEndEpochMillis != null && timerRemaining > 0) {
                        Text(
                            "RUNNING • ${formatRest(timerRemaining)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .testTag("utility_rest_countdown")
                                .semantics {
                                    contentDescription = "Rest timer countdown"
                                    stateDescription = "Rest timer remaining ${formatRest(timerRemaining)}"
                                    liveRegion = LiveRegionMode.Polite
                                },
                        )
                        TextButton(
                            onClick = { onStateChanged(state.copy(utilityRestTimerEndEpochMillis = null)) },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("rest_timer_cancel"),
                        ) { Text("STOP TIMER") }
                    } else {
                        Button(
                            enabled = state.restTimerSeconds != null,
                            onClick = {
                                val seconds = state.restTimerSeconds ?: return@Button
                                onStateChanged(state.copy(utilityRestTimerEndEpochMillis = System.currentTimeMillis() + seconds * 1_000L))
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("rest_timer_start"),
                        ) { Text("START TIMER") }
                    }
                }
            }
        }
        item {
            MachineEquipmentInventoryCard(
                configured = state.machineInventoryConfigured,
                available = state.availableMachineModalities,
                onChanged = { configured, available -> onStateChanged(state.copy(machineInventoryConfigured = configured, availableMachineModalities = available)) },
            )
        }
        item { AdditionalToolsCards() }
    }
}

@Composable
private fun DayReferenceDialog(
    session: SessionReference,
    state: ProgramUiState,
    allowSetCurrent: Boolean,
    onDismiss: () -> Unit,
    onRequestSetCurrent: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("WEEK ${session.week} • DAY ${session.day}\n${session.title}") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { PhaseIdentityLine(session.phase) }
                if (sessionExecutionGuidance(session).isNotEmpty()) {
                    item { SessionExecutionBanner(session) }
                }
                if (foundationReferenceLabel(session, state) != null) {
                    item { FoundationReferenceBanner(session, state) }
                }
                items(session.rows, key = { it.id }) { row ->
                    VisualRowTitle(row = row, title = row.title, style = MaterialTheme.typography.bodyLarge)
                    Text(row.prescription)
                    row.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    row.alternatives.forEachIndexed { index, alt -> Text("ALT ${index + 1} • $alt", style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            if (allowSetCurrent) TextButton(onClick = onRequestSetCurrent, modifier = Modifier.heightIn(min = 48.dp).testTag("set_current_day")) { Text("SET CURRENT") }
            else TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("CLOSE") }
        },
        dismissButton = if (allowSetCurrent) ({ TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text("CLOSE") } }) else null,
    )
}

@Composable
private fun BrandHeader() {
    AresBrandHeader()
}

private fun newWorkout(state: ProgramUiState) = ActiveWorkoutState(
    week = state.currentWeek,
    day = state.currentDay,
    deloadMode = state.deloadMode,
    machineInventoryConfigured = state.machineInventoryConfigured,
    availableMachineModalities = state.availableMachineModalities,
    speedVariant = state.speedVariant,
    engineVariant = state.engineVariant,
)

private fun resolveSession(week: Int, day: Int, state: ProgramUiState): SessionReference = SessionResolver.resolve(
    week = week,
    day = day,
    deload = state.deloadMode,
    machineInventoryConfigured = state.machineInventoryConfigured,
    availableMachineModalities = state.availableMachineModalities,
    speedVariant = state.speedVariant,
    engineVariant = state.engineVariant,
)

private fun splitForWeek(week: Int, state: ProgramUiState): List<SessionReference> = SessionResolver.splitForWeek(
    week = week,
    deload = state.deloadMode,
    machineInventoryConfigured = state.machineInventoryConfigured,
    availableMachineModalities = state.availableMachineModalities,
    speedVariant = state.speedVariant,
    engineVariant = state.engineVariant,
)

private fun formatRest(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
