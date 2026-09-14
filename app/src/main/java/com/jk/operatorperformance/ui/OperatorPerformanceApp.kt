package com.jk.operatorperformance.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jk.operatorperformance.program.BlockStatus
import com.jk.operatorperformance.program.MachineModality
import com.jk.operatorperformance.program.OperatorProgramV56
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.WorkoutProgress

private val OperatorDarkColors = darkColorScheme(
    primary = Color(0xFF58C4DC),
    onPrimary = Color(0xFF071318),
    primaryContainer = Color(0xFF173844),
    onPrimaryContainer = Color(0xFFD9F5FB),
    secondary = Color(0xFFAAB4C0),
    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFF3F6F9),
    surface = Color(0xFF121820),
    onSurface = Color(0xFFF3F6F9),
    surfaceVariant = Color(0xFF1A222C),
    onSurfaceVariant = Color(0xFFAAB4C0),
    tertiary = Color(0xFF69D18B),
    error = Color(0xFFF0C55B),
)

private enum class Destination(val label: String) {
    TODAY("TODAY"),
    WEEK("WEEK"),
    PROGRAM("PROGRAM"),
    TOOLS("TOOLS"),
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

    MaterialTheme(colorScheme = OperatorDarkColors) {
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
            Surface(modifier = Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
                when (destination) {
                    Destination.TODAY -> TodayScreen(
                        state = state,
                        activeWorkout = activeWorkout?.takeIf { it.matchesPointer(state) },
                        onStartWorkout = {
                            val existing = activeWorkout?.takeIf { it.matchesPointer(state) }
                            val next = existing ?: WorkoutProgress.start(state).also(::persistWorkout)
                            activeWorkout = next
                            workoutMode = true
                        },
                        onProgress = ::persistWorkout,
                        onAdvance = {
                            clearWorkout()
                            persist(state.advanceDay())
                        },
                    )
                    Destination.WEEK -> WeekScreen(
                        state = state,
                        onOpenDay = { session ->
                            referenceCanSetCurrent = false
                            referenceSession = session
                        },
                    )
                    Destination.PROGRAM -> ProgramScreen(
                        state = state,
                        selectedWeek = selectedProgramWeek,
                        onSelectWeek = { selectedProgramWeek = it },
                        onOpenDay = { session ->
                            referenceCanSetCurrent = true
                            referenceSession = session
                        },
                    )
                    Destination.TOOLS -> ToolsScreen(
                        state = state,
                        onDeloadChanged = { persist(state.copy(deloadMode = it)) },
                        onRestTimerSecondsChanged = { persist(state.copy(restTimerSeconds = it)) },
                        onUtilityRestTimerEndChanged = { persist(state.copy(utilityRestTimerEndEpochMillis = it)) },
                        onMachineInventoryChanged = { configured, available ->
                            persist(state.copy(machineInventoryConfigured = configured, availableMachineModalities = available))
                        },
                    )
                }
            }
        }

        referenceSession?.let { session ->
            DayReferenceDialog(
                session = session,
                allowSetCurrent = referenceCanSetCurrent &&
                    (session.week != state.currentWeek || session.day != state.currentDay),
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
                dismissButton = {
                    TextButton(onClick = { pendingPointer = null }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") }
                },
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
    val unfinished = (total - completed).coerceAtLeast(0)
    val atEnd = state.currentWeek == 8 && state.currentDay == 7
    val isRestDay = state.currentDay == 7
    var confirmIncompleteAdvance by rememberSaveable(state.currentWeek, state.currentDay) { mutableStateOf(false) }

    key(state.currentWeek, state.currentDay) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag("today_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    "WEEK ${session.week} • DAY ${session.day}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("today_header"),
                )
                Text(session.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                if (state.deloadMode) Text("DELOAD MODE", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                if (!isRestDay) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (progress == null) "$total SETS / SEGMENTS PLANNED" else "$completed / $total COMPLETE",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("today_progress"),
                    )
                }
            }
            items(session.rows, key = { it.id }) { row ->
                TodayWorkoutRowCard(
                    row = row,
                    progress = progress,
                    onToggleEffort = { effort ->
                        val base = progress ?: WorkoutProgress.start(state)
                        onProgress(WorkoutProgress.toggleEffort(base, session, row.id, effort))
                    },
                    onToggleAlternative = { alternative ->
                        val base = progress ?: WorkoutProgress.start(state)
                        val nextChoice = if (base.selectedSubstitutions[row.id] == null) alternative else null
                        onProgress(WorkoutProgress.selectSubstitution(base, session, row.id, nextChoice))
                    },
                )
            }
            item {
                if (!isRestDay) {
                    Button(
                        onClick = onStartWorkout,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("start_resume_workout"),
                    ) { Text(if (progress == null) "START WORKOUT" else "RESUME WORKOUT", fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.height(8.dp))
                }
                if (atEnd) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "BLOCK 1 COMPLETE • WEEKS 9–32 UNPOPULATED / NOT PROGRAMMED",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (isRestDay || progress?.complete == true) onAdvance() else confirmIncompleteAdvance = true
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("complete_day"),
                    ) { Text("COMPLETE DAY / NEXT DAY") }
                    if (!isRestDay && progress?.complete != true) {
                        Text(
                            "$unfinished SETS / SEGMENTS REMAIN • ADVANCE REQUIRES CONFIRMATION",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (confirmIncompleteAdvance) {
        AlertDialog(
            onDismissRequest = { confirmIncompleteAdvance = false },
            title = { Text("Advance with unfinished work?") },
            text = { Text("$unfinished prescribed sets/segments remain incomplete. Advance to the next day anyway?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmIncompleteAdvance = false
                        onAdvance()
                    },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_incomplete_advance"),
                ) { Text("ADVANCE") }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmIncompleteAdvance = false },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("cancel_incomplete_advance"),
                ) { Text("CANCEL") }
            },
        )
    }
}

@Composable
private fun TodayWorkoutRowCard(
    row: SessionRow,
    progress: ActiveWorkoutState?,
    onToggleEffort: (Int) -> Unit,
    onToggleAlternative: (String) -> Unit,
) {
    val selectedAlternative = progress?.selectedSubstitutions?.get(row.id)
    val displayTitle = selectedAlternative ?: row.title
    val allComplete = progress != null && (1..row.effortCount).all { WorkoutProgress.isEffortComplete(progress, row.id, it) }
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth().testTag("row_${row.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (allComplete) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(displayTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (allComplete) Text("✓", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
            if (selectedAlternative != null) Text("ALTERNATIVE FOR ${row.title}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(row.prescription, style = MaterialTheme.typography.bodyLarge)
            if (row.optional) Text("OPTIONAL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            if (row.kind != SessionRowKind.REST) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items((1..row.effortCount).toList()) { effort ->
                        val done = progress?.let { WorkoutProgress.isEffortComplete(it, row.id, effort) } == true
                        FilterChip(
                            selected = done,
                            onClick = {
                                if (!done) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleEffort(effort)
                            },
                            label = { Text(if (done) "✓ ${row.effortLabel} $effort" else "${row.effortLabel} $effort") },
                            modifier = Modifier.heightIn(min = 48.dp).testTag("effort_${row.id}_$effort"),
                        )
                    }
                }
            }
            row.alternatives.singleOrNull()?.let { alternative ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Alternative • $alternative", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { onToggleAlternative(alternative) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("today_alt_${row.id}"),
                    ) { Text(if (selectedAlternative == null) "ALT" else "PRIMARY") }
                }
            }
            row.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
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
    )
    val normalized = WorkoutProgress.normalize(progress, session)
    val currentRow = session.rows[normalized.movementIndex]
    val selectedSubstitution = normalized.selectedSubstitutions[currentRow.id]
    val visibleMovement = selectedSubstitution ?: currentRow.title
    val resting = normalized.restTimerEndEpochMillis != null
    val completed = WorkoutProgress.completedEfforts(normalized, session)
    val total = WorkoutProgress.totalEfforts(session)
    var showSubstitution by rememberSaveable(currentRow.id) { mutableStateOf(false) }
    var showMap by rememberSaveable { mutableStateOf(false) }
    var noteExpanded by rememberSaveable(currentRow.id) { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Surface(modifier = Modifier.fillMaxSize().testTag("workout_mode"), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                "WEEK ${session.week} • DAY ${session.day} • ${session.title}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("workout_context"),
            )
            Text(
                "$completed / $total COMPLETE • MOVEMENT ${normalized.movementIndex + 1} OF ${session.rows.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("workout_movement_index"),
            )

            Card(
                modifier = Modifier.fillMaxWidth().testTag("workout_hero"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (normalized.complete) {
                        Text("WORKOUT COMPLETE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        Text("Return to TODAY, then use COMPLETE DAY / NEXT DAY to advance the program pointer.")
                    } else {
                        Text(visibleMovement, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.testTag("workout_movement"))
                        if (selectedSubstitution != null) {
                            Text("ALTERNATIVE FOR ${currentRow.title}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.testTag("selected_substitution"))
                        }
                        Text(
                            "${currentRow.effortLabel} ${normalized.effortIndex} OF ${currentRow.effortCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("workout_effort"),
                        )
                        Text(currentRow.prescription, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("workout_prescription"))
                        if (currentRow.optional) Text("OPTIONAL", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        currentRow.note?.let { note ->
                            Text(note, style = MaterialTheme.typography.bodySmall, maxLines = if (noteExpanded) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis)
                            TextButton(onClick = { noteExpanded = !noteExpanded }, modifier = Modifier.heightIn(min = 48.dp).testTag("workout_note_toggle")) {
                                Text(if (noteExpanded) "LESS" else "MORE")
                            }
                        }
                    }
                }
            }

            if (!normalized.complete && normalized.restTimerEndEpochMillis != null) {
                WorkoutRestPanel(
                    endEpochMillis = normalized.restTimerEndEpochMillis,
                    onFinished = { onProgress(WorkoutProgress.clearExpiredRest(normalized, System.currentTimeMillis())) },
                    onAdd30 = { onProgress(WorkoutProgress.addRestSeconds(normalized, 30)) },
                    onSkip = { onProgress(WorkoutProgress.skipRest(normalized)) },
                )
            }

            if (!normalized.complete) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (currentRow.alternatives.isNotEmpty()) {
                        TextButton(
                            onClick = { showSubstitution = true },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("workout_substitutions"),
                        ) { Text(if (selectedSubstitution == null) "USE ALTERNATIVE" else "USE PRIMARY") }
                    }
                    TextButton(
                        onClick = { showMap = true },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("workout_map_button"),
                    ) { Text("WORKOUT MAP") }
                }

                if (!resting) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onProgress(WorkoutProgress.advance(normalized, session))
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp).testTag("workout_primary"),
                    ) {
                        Text("COMPLETE ${currentRow.effortLabel}", fontWeight = FontWeight.Bold)
                    }
                    if (currentRow.optional) {
                        TextButton(
                            onClick = { onProgress(WorkoutProgress.advance(normalized, session)) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("skip_optional"),
                        ) { Text("SKIP OPTIONAL") }
                    }
                }
            } else {
                Button(
                    onClick = onReturnToToday,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("workout_complete"),
                ) { Text("RETURN TO TODAY") }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (normalized.canUndo) {
                    TextButton(
                        onClick = { onProgress(WorkoutProgress.undo(normalized)) },
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("workout_undo"),
                    ) { Text("UNDO") }
                }
                TextButton(
                    onClick = onReturnToToday,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("workout_return_today"),
                ) { Text("FULL TODAY") }
            }
        }
    }

    if (showSubstitution) {
        val alternative = currentRow.alternatives.singleOrNull()
        AlertDialog(
            onDismissRequest = { showSubstitution = false },
            title = { Text("Exercise choice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            onProgress(WorkoutProgress.selectSubstitution(normalized, session, currentRow.id, null))
                            showSubstitution = false
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("substitution_canonical"),
                    ) { Text("PRIMARY • ${currentRow.title}") }
                    alternative?.let {
                        TextButton(
                            onClick = {
                                onProgress(WorkoutProgress.selectSubstitution(normalized, session, currentRow.id, it))
                                showSubstitution = false
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("substitution_0"),
                        ) { Text("ALTERNATIVE • $it") }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSubstitution = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CLOSE") } },
        )
    }

    if (showMap) WorkoutMapDialog(session = session, progress = normalized, onDismiss = { showMap = false })
}

@Composable
private fun WorkoutMapDialog(session: SessionReference, progress: ActiveWorkoutState, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).testTag("workout_map_dialog"),
        ) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("WORKOUT MAP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Completed / current / upcoming")
                }
                items(session.rows.indices.toList()) { index ->
                    val row = session.rows[index]
                    val rowDone = (1..row.effortCount).all { WorkoutProgress.isEffortComplete(progress, row.id, it) }
                    val status = when {
                        rowDone -> "COMPLETED"
                        index == progress.movementIndex && !progress.complete -> "CURRENT"
                        else -> "UPCOMING"
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${index + 1}. ${row.title}", fontWeight = FontWeight.Bold)
                            Text(status, color = if (rowDone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                item { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("CLOSE") } }
            }
        }
    }
}

@Composable
private fun WeekScreen(state: ProgramUiState, onOpenDay: (SessionReference) -> Unit) {
    val split = SessionResolver.splitForWeek(
        week = state.currentWeek,
        deload = state.deloadMode,
        machineInventoryConfigured = state.machineInventoryConfigured,
        availableMachineModalities = state.availableMachineModalities,
    )
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("week_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("WEEK ${state.currentWeek}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Complete seven-day split", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(split, key = { it.day }) { session ->
            DaySummaryCard(session, session.day == state.currentDay, "week_day_${session.day}") { onOpenDay(session) }
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
    val selectedSplit = SessionResolver.splitForWeek(
        week = selectedWeek,
        deload = state.deloadMode,
        machineInventoryConfigured = state.machineInventoryConfigured,
        availableMachineModalities = state.availableMachineModalities,
    )
    val nextWeek = if (state.currentWeek < 8) state.currentWeek + 1 else null
    val nextSplit = nextWeek?.let {
        SessionResolver.splitForWeek(
            week = it,
            deload = state.deloadMode,
            machineInventoryConfigured = state.machineInventoryConfigured,
            availableMachineModalities = state.availableMachineModalities,
        )
    }
    val currentPhase = OperatorProgramV56.blocks.first()
    val nextPhase = OperatorProgramV56.blocks[1]

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("program_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("PROGRAM", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Weeks 1–8 are fully programmed. Weeks 9–32 are not programmed.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..8).toList()) { week ->
                    FilterChip(
                        selected = selectedWeek == week,
                        onClick = { onSelectWeek(week) },
                        label = { Text("W$week") },
                        modifier = Modifier.testTag("program_week_$week"),
                    )
                }
            }
        }
        item { Text("WEEK $selectedWeek", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(selectedSplit, key = { "selected_${it.day}" }) { session ->
            DaySummaryCard(session, session.week == state.currentWeek && session.day == state.currentDay, "program_day_${session.day}") { onOpenDay(session) }
        }
        if (nextWeek != null && nextSplit != null) {
            item {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("NEXT WEEK • WEEK $nextWeek", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            items(nextSplit, key = { "next_${it.day}" }) { session ->
                DaySummaryCard(session, false, "next_week_day_${session.day}") { onOpenDay(session) }
            }
        }
        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text(
                "CURRENT PHASE • ${currentPhase.name} • Weeks ${currentPhase.startWeek}–${currentPhase.endWeek} • ${currentPhase.status.displayText()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("current_phase"),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "NEXT PHASE • ${nextPhase.name} • Weeks ${nextPhase.startWeek}–${nextPhase.endWeek} • ${nextPhase.status.displayText()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("next_phase"),
            )
            Spacer(Modifier.height(8.dp))
            Text("LATER PHASES", style = MaterialTheme.typography.titleSmall)
        }
        items(OperatorProgramV56.blocks.drop(2)) { block -> PhaseCard(block.name, block.startWeek, block.endWeek, block.status) }
    }
}

@Composable
private fun ToolsScreen(
    state: ProgramUiState,
    onDeloadChanged: (Boolean) -> Unit,
    onRestTimerSecondsChanged: (Int?) -> Unit,
    onUtilityRestTimerEndChanged: (Long?) -> Unit,
    onMachineInventoryChanged: (Boolean, Set<MachineModality>) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("tools_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("TOOLS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Reference controls and standalone utilities", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            RestTimerToolsCard(
                configuredSeconds = state.restTimerSeconds,
                activeEndEpochMillis = state.utilityRestTimerEndEpochMillis,
                onConfiguredSecondsChanged = onRestTimerSecondsChanged,
                onActiveEndChanged = onUtilityRestTimerEndChanged,
            )
        }
        item {
            MachineEquipmentInventoryCard(
                configured = state.machineInventoryConfigured,
                available = state.availableMachineModalities,
                onChanged = onMachineInventoryChanged,
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DELOAD MODE", fontWeight = FontWeight.Bold)
                        Text("Apply the fixed v5.6 deload overlay manually.")
                    }
                    Switch(checked = state.deloadMode, onCheckedChange = onDeloadChanged, modifier = Modifier.testTag("deload_switch"))
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("CURRENT POINTER", fontWeight = FontWeight.Bold)
                    Text("Week ${state.currentWeek} • Day ${state.currentDay}")
                    Text("Change the current pointer from PROGRAM by opening a populated day and confirming.")
                }
            }
        }
        item { AdditionalToolsCards() }
    }
}

@Composable
private fun WorkoutRowCard(row: SessionRow) {
    var expanded by rememberSaveable(row.id) { mutableStateOf(false) }
    val hasDetails = row.alternatives.isNotEmpty() || !row.note.isNullOrBlank()
    Card(
        modifier = Modifier.fillMaxWidth().then(if (hasDetails) Modifier.clickable { expanded = !expanded } else Modifier).testTag("row_${row.id}"),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(row.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(row.prescription, style = MaterialTheme.typography.bodyLarge)
            if (row.optional) Text("OPTIONAL", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            row.alternatives.singleOrNull()?.let { Text("Alternative • $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (hasDetails) Text(if (expanded) "Hide details" else "Details", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            if (expanded) row.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun DaySummaryCard(session: SessionReference, current: Boolean, testTag: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(testTag),
        colors = if (current) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("DAY ${session.day}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                if (current) Text("CURRENT", fontWeight = FontWeight.Bold)
            }
            Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(session.rows.joinToString(" • ") { it.title }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun BlockStatus.displayText(): String = when (this) {
    BlockStatus.POPULATED -> "POPULATED"
    BlockStatus.UNPOPULATED_NOT_PROGRAMMED -> "UNPOPULATED / NOT PROGRAMMED"
}

@Composable
private fun PhaseCard(name: String, startWeek: Int, endWeek: Int, status: BlockStatus) {
    Card(modifier = Modifier.fillMaxWidth().testTag("phase_${startWeek}_${endWeek}")) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, fontWeight = FontWeight.Bold)
            Text("Weeks $startWeek–$endWeek")
            Text(status.displayText(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DayReferenceDialog(
    session: SessionReference,
    allowSetCurrent: Boolean,
    onDismiss: () -> Unit,
    onRequestSetCurrent: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
        ) {
            LazyColumn(
                modifier = Modifier.testTag("reference_dialog"),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text("WEEK ${session.week} • DAY ${session.day}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(session.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (session.deload) Text("DELOAD MODE", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
                items(session.rows, key = { it.id }) { row -> WorkoutRowCard(row) }
                item {
                    if (allowSetCurrent) {
                        Button(
                            onClick = onRequestSetCurrent,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("set_current_button"),
                        ) { Text("SET AS CURRENT") }
                        Spacer(Modifier.height(8.dp))
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("CLOSE") }
                }
            }
        }
    }
}

private fun resolveSession(week: Int, day: Int, state: ProgramUiState): SessionReference =
    SessionResolver.resolve(
        week = week,
        day = day,
        deload = state.deloadMode,
        machineInventoryConfigured = state.machineInventoryConfigured,
        availableMachineModalities = state.availableMachineModalities,
    )
