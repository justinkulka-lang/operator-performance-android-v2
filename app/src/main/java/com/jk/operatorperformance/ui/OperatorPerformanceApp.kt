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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jk.operatorperformance.program.BlockStatus
import com.jk.operatorperformance.program.EngineVariant
import com.jk.operatorperformance.program.MachineModality
import com.jk.operatorperformance.program.OperatorProgramV54
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.program.SpeedVariant
import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.WorkoutProgress

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

    MaterialTheme {
        if (workoutMode) {
            val progress = activeWorkout
            if (progress == null) {
                workoutMode = false
            } else {
                WorkoutModeScreen(
                    progress = progress,
                    timerDurationSeconds = state.restTimerSeconds,
                    onProgress = ::persistWorkout,
                    onReturnToToday = { workoutMode = false },
                )
            }
            return@MaterialTheme
        }

        val destination = Destination.valueOf(destinationName)
        Scaffold(
            bottomBar = {
                NavigationBar {
                    Destination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destinationName = item.name },
                            icon = {},
                            label = { Text(item.label) },
                            alwaysShowLabel = true,
                            modifier = Modifier.testTag("nav_${item.name}"),
                        )
                    }
                }
            },
        ) { padding ->
            Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                        onSpeedChanged = { persist(state.copy(speedVariant = it)) },
                        onEngineChanged = { persist(state.copy(engineVariant = it)) },
                        onRestTimerSecondsChanged = { persist(state.copy(restTimerSeconds = it)) },
                        onUtilityRestTimerEndChanged = {
                            persist(state.copy(utilityRestTimerEndEpochMillis = it))
                        },
                        onMachineInventoryChanged = { configured, available ->
                            persist(
                                state.copy(
                                    machineInventoryConfigured = configured,
                                    availableMachineModalities = available,
                                ),
                            )
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
                    TextButton(
                        onClick = { pendingPointer = null },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) { Text("CANCEL") }
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
    onAdvance: () -> Unit,
) {
    val session = SessionResolver.resolve(
        week = state.currentWeek,
        day = state.currentDay,
        speedVariant = state.speedVariant,
        engineVariant = state.engineVariant,
        deload = state.deloadMode,
        machineInventoryConfigured = state.machineInventoryConfigured,
        availableMachineModalities = state.availableMachineModalities,
    )
    val atEnd = state.currentWeek == 8 && state.currentDay == 7

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("today_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "WEEK ${session.week} • DAY ${session.day}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("today_header"),
            )
            Text(session.title, style = MaterialTheme.typography.headlineMedium)
            if (state.deloadMode) Text("DELOAD MODE", style = MaterialTheme.typography.labelLarge)
        }
        items(session.rows, key = { it.id }) { row -> WorkoutRowCard(row) }
        item {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("start_resume_workout"),
            ) {
                Text(if (activeWorkout == null) "START WORKOUT" else "RESUME WORKOUT")
            }
            Spacer(Modifier.height(8.dp))
            if (atEnd) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "BLOCK 1 COMPLETE • WEEKS 9–32 NOT PROGRAMMED",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                Button(
                    onClick = onAdvance,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("complete_day"),
                ) { Text("COMPLETE DAY / NEXT DAY") }
            }
        }
    }
}

@Composable
private fun WorkoutModeScreen(
    progress: ActiveWorkoutState,
    timerDurationSeconds: Int?,
    onProgress: (ActiveWorkoutState) -> Unit,
    onReturnToToday: () -> Unit,
) {
    val session = SessionResolver.resolve(
        week = progress.week,
        day = progress.day,
        speedVariant = progress.speedVariant,
        engineVariant = progress.engineVariant,
        deload = progress.deloadMode,
        machineInventoryConfigured = progress.machineInventoryConfigured,
        availableMachineModalities = progress.availableMachineModalities,
    )
    val normalized = WorkoutProgress.normalize(progress, session)
    val currentRow = session.rows[normalized.movementIndex]
    val selectedSubstitution = normalized.selectedSubstitutions[currentRow.id]
    val visibleMovement = selectedSubstitution ?: currentRow.title
    val resting = normalized.restTimerEndEpochMillis != null
    var showSubstitutions by rememberSaveable(currentRow.id) { mutableStateOf(false) }
    var showMap by rememberSaveable { mutableStateOf(false) }
    var noteExpanded by rememberSaveable(currentRow.id) { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize().testTag("workout_mode")) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "WEEK ${session.week} • DAY ${session.day} • ${session.title}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.testTag("workout_context"),
            )
            Text(
                "MOVEMENT ${normalized.movementIndex + 1} OF ${session.rows.size}",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.testTag("workout_movement_index"),
            )

            Card(modifier = Modifier.fillMaxWidth().testTag("workout_hero")) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    if (normalized.complete) {
                        Text(
                            "WORKOUT COMPLETE",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("Return to TODAY, then use COMPLETE DAY / NEXT DAY when you are ready to advance the program pointer.")
                    } else {
                        Text(
                            visibleMovement,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("workout_movement"),
                        )
                        if (selectedSubstitution != null) {
                            Text(
                                "SUBSTITUTED FOR ${currentRow.title}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.testTag("selected_substitution"),
                            )
                        }
                        val effortLabel = if (currentRow.kind == SessionRowKind.RESISTANCE) {
                            "SET ${normalized.effortIndex} OF ${currentRow.effortCount}"
                        } else {
                            "EFFORT"
                        }
                        Text(
                            effortLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("workout_effort"),
                        )
                        Text(
                            currentRow.prescription,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.testTag("workout_prescription"),
                        )
                        currentRow.note?.let { note ->
                            Text(
                                note,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = if (noteExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            TextButton(
                                onClick = { noteExpanded = !noteExpanded },
                                modifier = Modifier.heightIn(min = 48.dp).testTag("workout_note_toggle"),
                            ) { Text(if (noteExpanded) "LESS" else "MORE") }
                        }
                    }
                }
            }

            if (!normalized.complete && normalized.restTimerEndEpochMillis != null) {
                WorkoutRestPanel(
                    endEpochMillis = normalized.restTimerEndEpochMillis,
                    onFinished = {
                        onProgress(WorkoutProgress.clearExpiredRest(normalized, System.currentTimeMillis()))
                    },
                    onSkip = { onProgress(WorkoutProgress.skipRest(normalized)) },
                )
            }

            if (!normalized.complete) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (currentRow.alternatives.isNotEmpty()) {
                        TextButton(
                            onClick = { showSubstitutions = true },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("workout_substitutions"),
                        ) { Text("SUBSTITUTIONS") }
                    }
                    TextButton(
                        onClick = { showMap = true },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("workout_map_button"),
                    ) { Text("WORKOUT MAP") }
                }

                if (!resting) {
                    Button(
                        onClick = {
                            onProgress(
                                WorkoutProgress.advance(
                                    progress = normalized,
                                    session = session,
                                    timerDurationSeconds = timerDurationSeconds,
                                    nowMillis = System.currentTimeMillis(),
                                ),
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("workout_primary"),
                    ) {
                        Text(if (currentRow.kind == SessionRowKind.RESISTANCE) "SET DONE" else "DONE")
                    }
                }
            } else {
                Button(
                    onClick = onReturnToToday,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("workout_complete"),
                ) { Text("RETURN TO TODAY") }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
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

    if (showSubstitutions) {
        AlertDialog(
            onDismissRequest = { showSubstitutions = false },
            title = { Text("Ranked substitutions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            onProgress(
                                WorkoutProgress.selectSubstitution(
                                    normalized,
                                    session,
                                    currentRow.id,
                                    null,
                                ),
                            )
                            showSubstitutions = false
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("substitution_canonical"),
                    ) { Text("CANONICAL • ${currentRow.title}") }
                    currentRow.alternatives.forEachIndexed { index, alternative ->
                        TextButton(
                            onClick = {
                                onProgress(
                                    WorkoutProgress.selectSubstitution(
                                        normalized,
                                        session,
                                        currentRow.id,
                                        alternative,
                                    ),
                                )
                                showSubstitutions = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("substitution_$index"),
                        ) { Text("${index + 1}. $alternative") }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showSubstitutions = false },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) { Text("CLOSE") }
            },
        )
    }

    if (showMap) {
        WorkoutMapDialog(
            session = session,
            progress = normalized,
            onDismiss = { showMap = false },
        )
    }
}

@Composable
private fun WorkoutMapDialog(
    session: SessionReference,
    progress: ActiveWorkoutState,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f).testTag("workout_map_dialog"),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text("WORKOUT MAP", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Completed / current / upcoming only")
                }
                items(session.rows.indices.toList()) { index ->
                    val status = when {
                        progress.complete || index < progress.movementIndex -> "COMPLETED"
                        index == progress.movementIndex -> "CURRENT"
                        else -> "UPCOMING"
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${index + 1}. ${session.rows[index].title}", fontWeight = FontWeight.Bold)
                            Text(status)
                        }
                    }
                }
                item {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text("CLOSE") }
                }
            }
        }
    }
}

@Composable
private fun WeekScreen(state: ProgramUiState, onOpenDay: (SessionReference) -> Unit) {
    val split = SessionResolver.splitForWeek(
        week = state.currentWeek,
        speedVariant = state.speedVariant,
        engineVariant = state.engineVariant,
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
            Text("WEEK ${state.currentWeek}", style = MaterialTheme.typography.headlineMedium)
            Text("Complete seven-day split", style = MaterialTheme.typography.bodyMedium)
        }
        items(split, key = { it.day }) { session ->
            DaySummaryCard(
                session = session,
                current = session.day == state.currentDay,
                testTag = "week_day_${session.day}",
                onClick = { onOpenDay(session) },
            )
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
        speedVariant = state.speedVariant,
        engineVariant = state.engineVariant,
        deload = state.deloadMode,
        machineInventoryConfigured = state.machineInventoryConfigured,
        availableMachineModalities = state.availableMachineModalities,
    )
    val nextWeek = if (state.currentWeek < 8) state.currentWeek + 1 else null
    val nextSplit = nextWeek?.let {
        SessionResolver.splitForWeek(
            week = it,
            speedVariant = state.speedVariant,
            engineVariant = state.engineVariant,
            deload = state.deloadMode,
            machineInventoryConfigured = state.machineInventoryConfigured,
            availableMachineModalities = state.availableMachineModalities,
        )
    }
    val currentPhase = OperatorProgramV54.blocks.first()
    val nextPhase = OperatorProgramV54.blocks[1]

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("program_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text("PROGRAM", style = MaterialTheme.typography.headlineMedium)
            Text("Browse populated Weeks 1–8 without changing the current pointer.")
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
            DaySummaryCard(
                session = session,
                current = session.week == state.currentWeek && session.day == state.currentDay,
                testTag = "program_day_${session.day}",
                onClick = { onOpenDay(session) },
            )
        }

        if (nextWeek != null && nextSplit != null) {
            item {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text("NEXT WEEK • WEEK $nextWeek", style = MaterialTheme.typography.titleLarge)
            }
            items(nextSplit, key = { "next_${it.day}" }) { session ->
                DaySummaryCard(
                    session = session,
                    current = false,
                    testTag = "next_week_day_${session.day}",
                    onClick = { onOpenDay(session) },
                )
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
        items(OperatorProgramV54.blocks.drop(2)) { block ->
            PhaseCard(block.name, block.startWeek, block.endWeek, block.status)
        }
    }
}

@Composable
private fun ToolsScreen(
    state: ProgramUiState,
    onDeloadChanged: (Boolean) -> Unit,
    onSpeedChanged: (SpeedVariant) -> Unit,
    onEngineChanged: (EngineVariant) -> Unit,
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
            Text("TOOLS", style = MaterialTheme.typography.headlineMedium)
            Text("Reference controls and utilities")
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
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DELOAD MODE", fontWeight = FontWeight.Bold)
                        Text("Use the canonical deload overlay. Activation is manual.")
                    }
                    Switch(
                        checked = state.deloadMode,
                        onCheckedChange = onDeloadChanged,
                        modifier = Modifier.testTag("deload_switch"),
                    )
                }
            }
        }
        item {
            Text("SPEED REFERENCE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("First post-baseline default: ACCELERATION. Selection remains manual.")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpeedVariant.entries.forEach { variant ->
                    FilterChip(
                        selected = state.speedVariant == variant,
                        onClick = { onSpeedChanged(variant) },
                        label = { Text(variant.name.replace('_', ' ')) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("speed_${variant.name}"),
                    )
                }
            }
        }
        item {
            Text("ENGINE REFERENCE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("First post-baseline default: THRESHOLD. Current selection repeats until changed.")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EngineVariant.entries.forEach { variant ->
                    FilterChip(
                        selected = state.engineVariant == variant,
                        onClick = { onEngineChanged(variant) },
                        label = { Text(variant.name) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("engine_${variant.name}"),
                    )
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
        modifier = Modifier
            .fillMaxWidth()
            .then(if (hasDetails) Modifier.clickable { expanded = !expanded } else Modifier)
            .testTag("row_${row.id}"),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(row.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(row.prescription, style = MaterialTheme.typography.bodyLarge)
            if (hasDetails) Text(if (expanded) "Hide details" else "Notes / alternatives", style = MaterialTheme.typography.labelLarge)
            if (expanded) {
                row.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                if (row.alternatives.isNotEmpty()) {
                    Text("Ranked alternatives", fontWeight = FontWeight.Bold)
                    row.alternatives.forEachIndexed { index, alternative -> Text("${index + 1}. $alternative") }
                }
            }
        }
    }
}

@Composable
private fun DaySummaryCard(
    session: SessionReference,
    current: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(testTag),
        colors = if (current) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("DAY ${session.day}", fontWeight = FontWeight.Bold)
                if (current) Text("CURRENT", fontWeight = FontWeight.Bold)
            }
            Text(session.title, style = MaterialTheme.typography.titleMedium)
            Text("Open complete prescription", style = MaterialTheme.typography.bodySmall)
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
            Text(status.displayText(), fontWeight = FontWeight.Bold)
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
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).testTag("reference_dialog"),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Text("WEEK ${session.week} • DAY ${session.day}", fontWeight = FontWeight.Bold)
                    Text(session.title, style = MaterialTheme.typography.headlineSmall)
                    if (session.deload) Text("DELOAD MODE", fontWeight = FontWeight.Bold)
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
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    ) { Text("CLOSE") }
                }
            }
        }
    }
}
