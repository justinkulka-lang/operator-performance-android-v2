package com.jk.operatorperformance.ui

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.jk.operatorperformance.program.AresProgramV72
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.SessionStatus
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
        if (!state.initialized) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BrandHeader()
                    Text("ARES v7.2", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("The approved program uses 32 seven-day weeks with six training days and one rest day each week.")
                    Spacer(Modifier.height(12.dp))
                    Text("Starting begins at Week 1, Day 1. Existing v7.0 progress is migrated when available. An in-progress v7.1 week finishes under v7.1, and v7.2 starts at the next unstarted Day 1.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { clearWorkout(); stateStore.clearSessionStatus(); persist(state.startV72()); selectedProgramWeek = 1 },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("start_v72"),
                    ) { Text("START WEEK 1 • DAY 1", fontWeight = FontWeight.Bold) }
                }
            }
            return@MaterialTheme
        }
        if (workoutMode) {
            val progress = activeWorkout
            if (progress == null) {
                workoutMode = false
            } else {
                WorkoutModeScreen(
                    progress = progress,
                    authority = state.authorityForWeek(progress.week),
                    onProgress = ::persistWorkout,
                    onReturnToToday = { workoutMode = false },
                    onMarkComplete = {
                        persistWorkout(WorkoutProgress.markSessionComplete(progress))
                        workoutMode = false
                    },
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
                                        Destination.PROGRAM -> Icons.AutoMirrored.Filled.ListAlt
                                        Destination.TOOLS -> Icons.Default.Build
                                    },
                                    contentDescription = null,
                                )
                            },
                            label = { Text(item.label) },
                            alwaysShowLabel = true,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("nav_${item.name}")
                                .semantics { stateDescription = if (destination == item) "Selected" else "Not selected" },
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
                            val existing = activeWorkout?.takeIf { it.matchesPointer(state) && !it.complete }
                            persistWorkout(existing ?: WorkoutProgress.start(state))
                            workoutMode = true
                        },
                        onProgress = ::persistWorkout,
                        onResetWorkout = ::clearWorkout,
                        onAdvance = {
                            stateStore.markSessionEngaged(state.currentWeek, state.currentDay)
                            clearWorkout()
                            val next = state.advanceDay()
                            persist(next)
                            selectedProgramWeek = next.currentWeek
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
                        onStateChanged = ::persist,
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
                dismissButton = { TextButton(onClick = { pendingPointer = null }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") } },
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
    onResetWorkout: () -> Unit,
    onAdvance: () -> Unit,
) {
    val session = resolveSession(state.currentWeek, state.currentDay, state)
    val progress = activeWorkout?.let { WorkoutProgress.normalize(it, session) }
    val sessionStatus = WorkoutProgress.status(progress, showingWorkout = false)
    val atEnd = state.currentWeek == 32 && state.currentDay == 7
    val isRest = session.rows.all { it.kind == SessionRowKind.REST }
    val executionGuidance = sessionExecutionGuidance(session)
    val foundationReference = foundationReferenceLabel(session, state)
    var confirmAdvance by rememberSaveable(state.currentWeek, state.currentDay) { mutableStateOf(false) }
    var confirmReset by rememberSaveable(state.currentWeek, state.currentDay) { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.weight(1f).testTag("today_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            BrandHeader()
            PhaseIdentityLine(session.phase, Modifier.testTag("today_phase"))
            Text(
                "v${session.authority.versionLabel} • WEEK ${session.week} • DAY ${session.day}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("today_header"),
            )
            Text(session.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (state.retainedV71Week == session.week) {
                Text("FINISHING THIS WEEK UNDER v7.1", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            if (!isRest) Text(
                when (sessionStatus) {
                    SessionStatus.NOT_STARTED -> "SESSION NOT STARTED"
                    SessionStatus.RESUMABLE -> "SESSION PAUSED • RESUME WHEN READY"
                    SessionStatus.ACTIVE -> "SESSION ACTIVE"
                    SessionStatus.COMPLETED -> "SESSION COMPLETE"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("today_progress"),
            )
        }
        if (sessionStatus == SessionStatus.RESUMABLE) {
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .testTag("saved_workout_status")
                        .semantics { contentDescription = "Session paused. Resume shows the same reference. Set completion is not stored." },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SESSION PAUSED", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("The prescription stays on screen. Resume does not ask you to mark sets, intervals, or repetitions.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
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
                onSelectAlternative = { alternative ->
                    val base = progress ?: newWorkout(state)
                    val choice = if (base.selectedSubstitutions[row.id] == alternative) null else alternative
                    onProgress(WorkoutProgress.selectSubstitution(base, session, row.id, choice))
                },
            )
        }
        item {
            if (!isRest && progress != null) {
                TextButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("reset_today_workout"),
                ) { Text("RESET TODAY'S WORKOUT") }
            }
            if (atEnd) {
                Card(Modifier.fillMaxWidth()) {
                    Text("ARES PERFORMANCE • 32-WEEK / 224-DAY PROGRAM COMPLETE", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { if (isRest || progress == null) onAdvance() else confirmAdvance = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("complete_day"),
                ) { Text("COMPLETE DAY / NEXT DAY") }
            }
        }
    }
    if (!isRest && sessionStatus != SessionStatus.COMPLETED) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Button(
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth().padding(12.dp).heightIn(min = 56.dp).testTag("start_resume_workout"),
            ) { Text(if (sessionStatus == SessionStatus.NOT_STARTED) "START WORKOUT" else "RESUME WORKOUT", fontWeight = FontWeight.Bold) }
        }
    }
    }

    if (confirmAdvance) {
        AlertDialog(
            onDismissRequest = { confirmAdvance = false },
            title = { Text("Advance to the next day?") },
            text = { Text("Move the program pointer to the next day? This clears the active session. It does not store loads, reps, or results.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmAdvance = false; onAdvance() },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_incomplete_advance"),
                ) { Text("ADVANCE") }
            },
            dismissButton = { TextButton(onClick = { confirmAdvance = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") } },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset today's workout?") },
            text = { Text("Clear today's active session, chosen alternatives, and rest timer? The week stays marked as engaged, and no loads, reps, or results are stored.") },
            confirmButton = {
                TextButton(
                    onClick = { confirmReset = false; onResetWorkout() },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_reset_workout"),
                ) { Text("RESET") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") } },
        )
    }
}

@Composable
private fun WorkoutRowCard(
    row: SessionRow,
    progress: ActiveWorkoutState?,
    onSelectAlternative: (String) -> Unit,
) {
    var showDetails by rememberSaveable(row.id) { mutableStateOf(false) }
    val selected = progress?.selectedSubstitutions?.get(row.id)
    val title = selected ?: row.title
    val hasDetails = row.note != null || row.alternatives.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth().testTag("row_${row.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VisualRowTitle(row = row, title = title, style = MaterialTheme.typography.titleMedium)
            if (selected != null) Text("ALTERNATIVE FOR ${row.title}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
            Text(row.prescription, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("prescription_${row.id}"))
            if (row.optional) Text("OPTIONAL", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
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
    authority: ProgramAuthority,
    onProgress: (ActiveWorkoutState) -> Unit,
    onReturnToToday: () -> Unit,
    onMarkComplete: () -> Unit,
) {
    val session = SessionResolver.resolve(progress.week, progress.day, authority)
    val normalized = WorkoutProgress.normalize(progress, session)
    val haptic = LocalHapticFeedback.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val restSeconds = WorkoutProgress.remainingRestSeconds(normalized, now)

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
            if (normalized.restTimerEndEpochMillis != null) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            "REST • ${formatRest(restSeconds)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("workout_rest_countdown")
                                .semantics {
                                    contentDescription = "Workout rest timer"
                                    stateDescription = "Rest timer remaining ${formatRest(restSeconds)}"
                                    liveRegion = LiveRegionMode.Polite
                                },
                        )
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
                Text(
                    "SESSION ACTIVE • FULL REFERENCE",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("workout_status"),
                )
                Text(
                    "Read the whole session while training. Nothing here asks for loads, reps, or a tap on every set.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (normalized.restTimerEndEpochMillis != null) {
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .testTag("workout_rest")
                            .semantics {
                                contentDescription = "Workout rest timer"
                                stateDescription = "Rest timer remaining ${formatRest(restSeconds)}"
                                liveRegion = LiveRegionMode.Polite
                            },
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { onProgress(WorkoutProgress.skipRest(normalized)) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("workout_rest_skip"),
                            ) { Text("SKIP REST") }
                            TextButton(
                                onClick = { onProgress(WorkoutProgress.addRestSeconds(normalized, 30)) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("workout_rest_add_30"),
                            ) { Text("+30 SEC") }
                        }
                    }
                }
            }
            if (sessionExecutionGuidance(session).isNotEmpty()) {
                item { SessionExecutionBanner(session) }
            }
            item {
                Text("REST TIMER", fontWeight = FontWeight.Bold)
                Text(
                    "Compounds rest 2–4 min. Isolation rests 1–2 min. Sprint and interval recoveries in the prescription take precedence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                listOf(120, 180, 60, 90).forEach { seconds ->
                    TextButton(
                        onClick = { onProgress(WorkoutProgress.startManualRest(normalized, seconds, System.currentTimeMillis())) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("start_rest_$seconds"),
                    ) { Text("START REST ${formatRest(seconds)}") }
                }
            }
            items(session.rows, key = { it.id }) { row ->
                val selected = normalized.selectedSubstitutions[row.id]
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("workout_row_${row.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        VisualRowTitle(row = row, title = selected ?: row.title, style = MaterialTheme.typography.titleMedium)
                        if (selected != null) Text("ALTERNATIVE FOR ${row.title}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                        Text(row.prescription, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.testTag("workout_prescription_${row.id}"))
                        row.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (row.optional) Text("OPTIONAL", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        row.alternatives.forEachIndexed { index, alternative ->
                            FilterChip(
                                selected = selected == alternative,
                                onClick = {
                                    val choice = if (selected == alternative) null else alternative
                                    onProgress(WorkoutProgress.selectSubstitution(normalized, session, row.id, choice))
                                },
                                label = { Text("${index + 1}. $alternative") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            )
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = onMarkComplete,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("mark_session_complete"),
                ) { Text("MARK SESSION COMPLETE", fontWeight = FontWeight.Bold) }
                TextButton(
                    onClick = onReturnToToday,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("return_today"),
                ) { Text("RETURN TO TODAY") }
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
            val phase = blockFor(state, state.currentWeek)
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
    val currentPhase = blockFor(state, state.currentWeek)
    val selectedPhase = blockFor(state, selectedWeek)
    val selectedSplit = splitForWeek(selectedWeek, state)
    val nextWeek = (state.currentWeek + 1).takeIf { it <= 32 }
    val nextPhase = AresProgramV72.blocks.firstOrNull { it.startWeek > currentPhase.endWeek }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchHits = remember(searchQuery, state.retainedV71Week, state.authority) { searchProgram(searchQuery, state) }
    val jumpWeeks = remember(state.currentWeek) { listOf(state.currentWeek, 1, 9, 17, 25, 32).distinct() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("program_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BrandHeader()
            Text("32 WEEKS • 7 DAYS • 224 DAYS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("SEARCH / JUMP") },
                supportingText = { Text("Examples: W17, D3, 300 m, squat, threshold") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("program_search")
                    .semantics { contentDescription = "Search the approved 32-week ARES program" },
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(jumpWeeks) { week ->
                    FilterChip(
                        selected = selectedWeek == week,
                        onClick = { onSelectWeek(week); searchQuery = "" },
                        label = { Text(if (week == state.currentWeek) "CURRENT W$week" else "W$week") },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("program_jump_week_$week"),
                    )
                }
            }
            if (searchHits.isNotEmpty()) {
                TextButton(
                    onClick = {
                        val hit = searchHits.first()
                        onSelectWeek(hit.session.week)
                        onOpenDay(hit.session)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("program_search_open_first"),
                ) { Text("OPEN FIRST MATCH") }
            }
        }
        if (searchQuery.isNotBlank()) {
            item {
                HorizontalDivider()
                Text("SEARCH RESULTS", fontWeight = FontWeight.Bold)
                Text(
                    if (searchHits.isEmpty()) "No matching approved sessions." else "${searchHits.size} matching session${if (searchHits.size == 1) "" else "s"} shown.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("program_search_status"),
                )
                TextButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("program_search_clear"),
                ) { Text("CLEAR SEARCH") }
            }
            items(searchHits, key = { "search_${it.session.week}_${it.session.day}" }) { hit ->
                Card(
                    onClick = {
                        onSelectWeek(hit.session.week)
                        onOpenDay(hit.session)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .testTag("program_search_result_${hit.session.week}_${hit.session.day}")
                        .semantics { contentDescription = "${hit.matchLabel}; ${hit.matchSnippet}" },
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(hit.matchLabel, fontWeight = FontWeight.Bold)
                        Text(hit.matchSnippet, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Text("PHASES", fontWeight = FontWeight.Bold)
            AresProgramV72.blocks.forEach { phase ->
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
            Card(onClick = { onOpenDay(session) }, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("program_day_${session.week}_${session.day}")) {
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
                val phase = blockFor(state, nextWeek)
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
        item { BuildIntegrityCard() }
        item {
            Column(Modifier.fillMaxWidth().testTag("rest_timer_tool"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("INDEPENDENT REST TIMER", fontWeight = FontWeight.Bold)
                Text(
                    "Utility only. Resistance rest guidance is approximate and set quality takes precedence.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        items(listOf(60, 90, 120, 180)) { seconds ->
            val chosen = state.restTimerSeconds == seconds
            Button(
                onClick = { onStateChanged(state.copy(restTimerSeconds = seconds)) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("rest_choice_$seconds"),
            ) { Text(if (chosen) "${formatRest(seconds)} SELECTED" else formatRest(seconds)) }
        }
        item {
            val running = state.utilityRestTimerEndEpochMillis != null
            if (running) {
                Text(
                    "RUNNING • ${formatRest(timerRemaining)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("utility_rest_countdown")
                        .semantics {
                            contentDescription = "Rest timer countdown"
                            stateDescription = "Rest timer remaining ${formatRest(timerRemaining)}"
                            liveRegion = LiveRegionMode.Polite
                        },
                )
                TextButton(
                    onClick = { onStateChanged(state.copy(utilityRestTimerEndEpochMillis = null)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("rest_timer_cancel"),
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
            Column(
                Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("day_reference_scroll"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PhaseIdentityLine(session.phase)
                Text("AUTHORITY v${session.authority.versionLabel}", style = MaterialTheme.typography.labelMedium)
                if (sessionExecutionGuidance(session).isNotEmpty()) SessionExecutionBanner(session)
                if (foundationReferenceLabel(session, state) != null) FoundationReferenceBanner(session, state)
                session.rows.forEach { row ->
                    VisualRowTitle(
                        row = row,
                        title = row.title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag("day_row_${row.id}"),
                    )
                    Text(row.prescription, modifier = Modifier.testTag("day_prescription_${row.id}"))
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

private fun newWorkout(state: ProgramUiState) = ActiveWorkoutState(week = state.currentWeek, day = state.currentDay)

private fun resolveSession(week: Int, day: Int, state: ProgramUiState): SessionReference =
    SessionResolver.resolve(week, day, state.authorityForWeek(week))

private fun splitForWeek(week: Int, state: ProgramUiState): List<SessionReference> =
    SessionResolver.splitForWeek(week, state.authorityForWeek(week))

private fun blockFor(state: ProgramUiState, week: Int) =
    if (state.authorityForWeek(week) == ProgramAuthority.V71) com.jk.operatorperformance.program.AresProgramV71.blockForWeek(week)
    else AresProgramV72.blockForWeek(week)

private fun formatRest(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
