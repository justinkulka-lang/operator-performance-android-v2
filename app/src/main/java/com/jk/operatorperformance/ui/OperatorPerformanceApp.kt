package com.jk.operatorperformance.ui

import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalConfiguration

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.R
import com.jk.operatorperformance.program.ProgramV73
import com.jk.operatorperformance.program.ProgramV72
import com.jk.operatorperformance.program.ProgramV74
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.V74SurfaceText
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind
import com.jk.operatorperformance.program.V74Pointer
import com.jk.operatorperformance.state.ActiveWorkoutState
import com.jk.operatorperformance.state.ProgramStateStore
import com.jk.operatorperformance.state.ProgramUiState
import com.jk.operatorperformance.state.SessionStatus
import com.jk.operatorperformance.state.WorkoutProgress
import com.jk.operatorperformance.state.workoutForToday
import kotlinx.coroutines.delay

private enum class Destination(val label: String) {
    TODAY("TODAY"), WEEK("WEEK"), PROGRAM("PROGRAM"), TOOLS("TOOLS")
}

@Composable
fun OperatorPerformanceApp(stateStore: ProgramStateStore) {
    var state by remember { mutableStateOf(stateStore.load()) }
    var activeWorkout by remember { mutableStateOf(stateStore.loadWorkout()) }
    var workoutMode by rememberSaveable { mutableStateOf(false) }
    var destinationName by rememberSaveable { mutableStateOf(Destination.TODAY.name) }
    var showKey by rememberSaveable { mutableStateOf(false) }
    var selectedProgramWeek by rememberSaveable { mutableIntStateOf(state.currentWeek) }
    var referenceSession by remember { mutableStateOf<SessionReference?>(null) }
    var referenceCanSetCurrent by remember { mutableStateOf(false) }
    var pendingPointer by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var showStartPosition by rememberSaveable { mutableStateOf(false) }

    fun persist(next: ProgramUiState) {
        state = next
        stateStore.save(next)
    }

    fun persistWorkout(next: ActiveWorkoutState) {
        activeWorkout = next
        state = stateStore.saveWorkout(next)
    }

    fun clearWorkout() {
        activeWorkout = null
        stateStore.clearWorkout()
    }

    MaterialTheme(colorScheme = LegendColors, typography = legendTypography()) {
        if (!state.initialized) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(
                    Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BrandHeader()
                    Text("LEGEND", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(V74Pointer.CONTINUOUS_LINE)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { clearWorkout(); stateStore.clearSessionStatus(); persist(state.startV74()); selectedProgramWeek = 1 },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("start_v72"),
                    ) { Text(V74SurfaceText.START_WEEK_ONE, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { showStartPosition = true },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("start_at_position"),
                    ) { Text(V74SurfaceText.START_AT_POSITION, fontWeight = FontWeight.Bold) }
                }
            }
            if (showStartPosition) {
                StartPositionFlow(
                    onDismiss = { showStartPosition = false },
                    onConfirm = { block, blockWeek, day ->
                        clearWorkout()
                        stateStore.clearSessionStatus()
                        persist(state.startAtPosition(block, blockWeek, day))
                        selectedProgramWeek = V74Pointer.absoluteWeek(block, blockWeek)
                        showStartPosition = false
                    },
                )
            }
            return@MaterialTheme
        }
        if (workoutMode) {
            val progress = activeWorkout
            if (progress == null) {
                workoutMode = false
            } else {
                WorkoutModeScreen(
                    state = state,
                    progress = progress,
                    authority = progress.authority ?: state.authorityForWeek(progress.week),
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
        val navigationConfiguration = LocalConfiguration.current
        val compactNavigation = navigationConfiguration.screenWidthDp < 280 ||
            (navigationConfiguration.fontScale >= 1.5f && navigationConfiguration.screenWidthDp < 360)
        val shortProgramLabel = navigationConfiguration.fontScale >= 1.5f &&
            navigationConfiguration.screenWidthDp < 480
        // Key is a Tools subpage, not a separate activity or persisted destination.
        BackHandler(enabled = destination == Destination.TOOLS && showKey) {
            showKey = false
        }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (navigationConfiguration.screenWidthDp < 240) {
                    Surface(color = MaterialTheme.colorScheme.surface) {
                        Column(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                            Destination.entries.chunked(2).forEach { row ->
                                Row(Modifier.fillMaxWidth()) {
                                    row.forEach { item ->
                                        TextButton(
                                            onClick = {
                                                destinationName = item.name
                                                showKey = false
                                            },
                                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                                .testTag("nav_${item.name}")
                                                .semantics {
                                                    contentDescription = item.label
                                                    stateDescription = if (destination == item) "Selected" else "Not selected"
                                                },
                                        ) {
                                            Text(
                                                if (item == Destination.PROGRAM && shortProgramLabel) "PLAN" else item.label,
                                                maxLines = 1,
                                                color = if (destination == item) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (destination == item) FontWeight.Bold else FontWeight.Normal,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LegendNavigationRow(Destination.entries, destination, compactNavigation, shortProgramLabel) { item ->
                        destinationName = item.name
                        showKey = false
                    }
                }
            },
        ) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding), color = MaterialTheme.colorScheme.background) {
                val todayWorkout = workoutForToday(state, activeWorkout)
                LaunchedEffect(activeWorkout) {
                    val saved = activeWorkout
                    if (saved != null && workoutForToday(state, saved) == null) clearWorkout()
                }
                when (destination) {
                    Destination.TODAY -> TodayScreen(
                        state = state,
                        activeWorkout = todayWorkout?.takeIf { it.matchesPointer(state) },
                        onStartWorkout = {
                            val existing = todayWorkout?.takeIf { it.matchesPointer(state) && !it.complete }
                            persistWorkout(existing ?: WorkoutProgress.start(state))
                            workoutMode = true
                        },
                        onProgress = ::persistWorkout,
                        onResetWorkout = ::clearWorkout,
                        canConsolidate = state.canConsolidate(
                            workoutActive = activeWorkout != null,
                            evidence = stateStore.evidenceForWeek(state.currentWeek),
                        ),
                        onConsolidate = {
                            persist(state.consolidateNow(
                                workoutActive = activeWorkout != null,
                                evidence = stateStore.evidenceForWeek(state.currentWeek),
                            ))
                        },
                        onAdvance = {
                            val engaged = stateStore.markSessionEngaged(state.currentWeek, state.currentDay)
                            clearWorkout()
                            val next = engaged.advanceDay()
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
                    Destination.TOOLS -> if (showKey) {
                        KeyScreen(onBack = { showKey = false })
                    } else {
                        ToolsScreen(
                            state = state,
                            onStateChanged = ::persist,
                            onOpenKey = { showKey = true },
                            onProgramReset = { next ->
                                activeWorkout = null
                                workoutMode = false
                                showKey = false
                                referenceSession = null
                                pendingPointer = null
                                persist(next)
                                selectedProgramWeek = next.currentWeek
                                destinationName = Destination.TODAY.name
                            },
                        )
                    }
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
                text = {
                    Text(
                        if (state.v74MigrationApplied) V74SurfaceText.setCurrentDay(day)
                        else "Set the current pointer to Week $week, Day $day? This clears the current session status, substitutions and workout timer. It preserves the utility timer, recorded engagement and each week’s authority; it does not mark any new movement complete.",
                    )
                },
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
private fun LegendNavigationRow(
    destinations: List<Destination>,
    destination: Destination,
    iconsOnly: Boolean,
    shortProgramLabel: Boolean,
    onNavigate: (Destination) -> Unit,
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    destinations.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = {
                                onNavigate(item)
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        when (item) {
                                            Destination.TODAY -> R.drawable.ic_nav_today
                                            Destination.WEEK -> R.drawable.ic_nav_week
                                            Destination.PROGRAM -> R.drawable.ic_nav_program
                                            Destination.TOOLS -> R.drawable.ic_nav_tools
                                        },
                                    ),
                                    contentDescription = null,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            label = if (iconsOnly) null else {
                                { Text(if (item == Destination.PROGRAM && shortProgramLabel) "PLAN" else item.label, maxLines = 1) }
                            },
                            alwaysShowLabel = !iconsOnly,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("nav_${item.name}")
                                .semantics {
                                    contentDescription = item.label
                                    stateDescription = if (destination == item) "Selected" else "Not selected"
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
    onResetWorkout: () -> Unit,
    canConsolidate: Boolean,
    onConsolidate: () -> Unit,
    onAdvance: () -> Unit,
) {
    val session = resolveSession(state.currentWeek, state.currentDay, state)
    val progress = activeWorkout?.let { WorkoutProgress.normalize(it, session) }
    val sessionStatus = WorkoutProgress.status(progress, showingWorkout = false)
    val atEnd = state.atProgramEnd()
    val isRest = session.rows.all { it.kind == SessionRowKind.REST }
    val executionGuidance = sessionExecutionGuidance(session)
    val foundationReference = foundationReferenceLabel(session, state)
    var confirmAdvance by rememberSaveable(state.currentWeek, state.currentDay) { mutableStateOf(false) }
    var confirmReset by rememberSaveable(state.currentWeek, state.currentDay) { mutableStateOf(false) }
    var confirmConsolidate by rememberSaveable(state.currentWeek, state.currentDay, state.v74BlockWeek) { mutableStateOf(false) }

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
                if (session.authority == ProgramAuthority.V74 && state.v74Block != null && state.v74BlockWeek != null) {
                    V74Pointer.header(state.currentWeek, state.v74Block, state.v74BlockWeek)
                } else {
                    "v${session.authority.versionLabel} • WEEK ${session.week} • DAY ${session.day}"
                },
                style = technicalStyle(MaterialTheme.typography.labelLarge),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("today_header"),
            )
            CigarBand(session.title)
            V74GlobalLine(session.authority == ProgramAuthority.V74)
            if ((session.authority == ProgramAuthority.V71 || session.authority == ProgramAuthority.V72) && state.v73MigrationApplied) {
                Text("RETAINED WEEK • v${session.authority.versionLabel}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
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
            if (canConsolidate) {
                Button(
                    onClick = { confirmConsolidate = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("consolidate_now"),
                ) { Text("CONSOLIDATE NOW") }
            }
        }
        if (sessionStatus == SessionStatus.RESUMABLE) {
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .testTag("saved_workout_status")
                        .semantics { contentDescription = "Session paused. Resume shows the same reference. Set completion is not stored." },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("SESSION PAUSED", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                session = session,
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
                    Text("LEGEND • 32-WEEK / 224-DAY PROGRAM COMPLETE", Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
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
                colors = legendButtonColors(),
                modifier = Modifier.fillMaxWidth().padding(12.dp).heightIn(min = 56.dp).testTag("start_resume_workout"),
            ) { Text(if (sessionStatus == SessionStatus.NOT_STARTED) "START WORKOUT" else "RESUME WORKOUT", fontWeight = FontWeight.Bold) }
        }
    }
    }

    if (confirmAdvance) {
        AlertDialog(
            onDismissRequest = { confirmAdvance = false },
            title = { Text("Advance to the next day?") },
            text = { Text(V74SurfaceText.advanceConfirmation(session.authority == ProgramAuthority.V74)) },
            confirmButton = {
                TextButton(
                    onClick = { confirmAdvance = false; onAdvance() },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_incomplete_advance"),
                ) { Text("ADVANCE") }
            },
            dismissButton = { TextButton(onClick = { confirmAdvance = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") } },
        )
    }

    if (confirmConsolidate) {
        AlertDialog(
            onDismissRequest = { confirmConsolidate = false },
            title = { Text("Consolidate this block week?") },
            text = { Text(V74SurfaceText.consolidateConfirmation()) },
            confirmButton = {
                TextButton(
                    onClick = { confirmConsolidate = false; onConsolidate() },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("confirm_consolidate"),
                ) { Text("CONSOLIDATE") }
            },
            dismissButton = { TextButton(onClick = { confirmConsolidate = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("CANCEL") } },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset today's workout?") },
            text = { Text(V74SurfaceText.resetWorkoutConfirmation(session.authority == ProgramAuthority.V74)) },
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
    session: SessionReference,
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
            VisualRowTitle(session = session, row = row, title = title, style = MaterialTheme.typography.titleMedium)
            if (selected != null) Text("ALTERNATIVE FOR ${row.title}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            Text(row.prescription, style = technicalStyle(), modifier = Modifier.testTag("prescription_${row.id}"))
            if (row.optional) Text("OPTIONAL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
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
    state: ProgramUiState,
    progress: ActiveWorkoutState,
    authority: ProgramAuthority,
    onProgress: (ActiveWorkoutState) -> Unit,
    onReturnToToday: () -> Unit,
    onMarkComplete: () -> Unit,
) {
    val session = resolveSession(progress.week, progress.day, state)
    val normalized = WorkoutProgress.normalize(progress, session)
    val latestProgress by rememberUpdatedState(normalized)
    val latestOnProgress by rememberUpdatedState(onProgress)
    val haptic = LocalHapticFeedback.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val restSeconds = WorkoutProgress.remainingRestSeconds(normalized, now)

    KeepScreenOnEffect()

    LaunchedEffect(progress.week, progress.day, authority, normalized.restTimerEndEpochMillis) {
        val end = normalized.restTimerEndEpochMillis
        now = System.currentTimeMillis()
        while (end != null && end > System.currentTimeMillis()) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
        if (end != null && end == latestProgress.restTimerEndEpochMillis && end <= System.currentTimeMillis()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            latestOnProgress(WorkoutProgress.clearExpiredRest(latestProgress, System.currentTimeMillis()))
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
                            style = technicalStyle(MaterialTheme.typography.titleLarge),
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
                    style = technicalStyle(MaterialTheme.typography.labelLarge),
                    modifier = Modifier.testTag("workout_context"),
                )
                Text(
                    "SESSION ACTIVE • FULL REFERENCE",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("workout_status"),
                )
                V74GlobalLine(session.authority == ProgramAuthority.V74)
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
                        VisualRowTitle(session = session, row = row, title = selected ?: row.title, style = MaterialTheme.typography.titleMedium)
                        if (selected != null) Text("ALTERNATIVE FOR ${row.title}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        Text(row.prescription, style = technicalStyle(), modifier = Modifier.testTag("workout_prescription_${row.id}"))
                        row.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        if (row.optional) Text("OPTIONAL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        row.alternatives.forEachIndexed { index, alternative ->
                            FilterChip(
                                selected = selected == alternative,
                                onClick = {
                                    val choice = if (selected == alternative) null else alternative
                                    onProgress(WorkoutProgress.selectSubstitution(normalized, session, row.id, choice))
                                },
                                label = { Text("${index + 1}. $alternative") },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("workout_alternative_${row.id}_$index"),
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
            Text(
                "WEEK ${state.currentWeek}",
                style = technicalStyle(MaterialTheme.typography.headlineSmall),
            )
            V74GlobalLine(state.authorityForWeek(state.currentWeek) == ProgramAuthority.V74)
        }
        items(split, key = { it.day }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("week_day_${session.day}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
private fun V74ProgramScreen(
    state: ProgramUiState,
    onOpenDay: (SessionReference) -> Unit,
) {
    var selectedBlock by rememberSaveable(state.v74Block) { mutableIntStateOf(state.v74Block ?: 1) }
    var selectedBlockWeek by rememberSaveable(state.v74BlockWeek) { mutableIntStateOf(state.v74BlockWeek ?: 1) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchHits = remember(
        searchQuery,
        state.v74ActivationWeek,
        state.v74MigrationApplied,
        state.historicalAuthorities,
    ) { searchProgram(searchQuery, state) }
    val preview = state.copy(currentDay = 7).advanceDay()
    val selectedSplit = (1..7).map { day ->
        V74Pointer.reference(V74Pointer.absoluteWeek(selectedBlock, selectedBlockWeek), selectedBlock, selectedBlockWeek, day)
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("program_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BrandHeader()
            Text(V74Pointer.CONTINUOUS_LINE, style = technicalStyle(MaterialTheme.typography.headlineSmall))
            V74GlobalLine(true)
            Text(
                "Loop ${state.v74Loop}",
                style = technicalStyle(MaterialTheme.typography.labelSmall),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("program_loop"),
            )
            state.v74Block?.let { block ->
                Text(
                    "${V74Pointer.roman(block)} · ${V74Pointer.phaseName(block)}",
                    modifier = Modifier.testTag("current_phase"),
                )
                Text(V74Pointer.phase(block).purpose)
            }
        }
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("SEARCH / JUMP") },
                supportingText = { Text("Examples: block, squat, threshold") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("program_search"),
            )
            if (searchHits.isNotEmpty()) {
                TextButton(
                    onClick = { onOpenDay(searchHits.first().session) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("program_search_open_first"),
                ) { Text("OPEN FIRST MATCH") }
            }
        }
        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    V74SurfaceText.searchStatus(searchHits.size),
                    modifier = Modifier.testTag("program_search_status"),
                )
                TextButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("program_search_clear"),
                ) { Text("CLEAR SEARCH") }
            }
            items(searchHits, key = { "search_${it.session.authority}_${it.session.week}_${it.session.day}" }) { hit ->
                Card(
                    onClick = { onOpenDay(hit.session) },
                    modifier = Modifier.fillMaxWidth().testTag("program_search_result_${hit.session.week}_${hit.session.day}"),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(hit.matchLabel, fontWeight = FontWeight.Bold)
                        Text(hit.matchSnippet, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Text("BLOCKS", fontWeight = FontWeight.Bold)
            (1..4).forEach { block ->
                val phase = V74Pointer.phase(block)
                Text("${V74Pointer.roman(block)} ${phase.name}. ${phase.purpose}", modifier = Modifier.testTag("v74_block_$block"))
            }
        }
        item {
            Text("BROWSE BLOCK", fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..4).toList()) { block ->
                    FilterChip(
                        selected = selectedBlock == block,
                        onClick = { selectedBlock = block },
                        label = { Text(V74Pointer.roman(block)) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("program_block_$block"),
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items((1..8).toList()) { week ->
                    FilterChip(
                        selected = selectedBlockWeek == week,
                        onClick = { selectedBlockWeek = week },
                        label = { Text("W$week") },
                        modifier = Modifier.heightIn(min = 48.dp).testTag("program_block_week_$week"),
                    )
                }
            }
        }
        items(selectedSplit, key = { "b${selectedBlock}_w${selectedBlockWeek}_d${it.day}" }) { session ->
            val current = selectedBlock == state.v74Block && selectedBlockWeek == state.v74BlockWeek && session.day == state.currentDay
            Card(
                onClick = { onOpenDay(session) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().testTag("program_day_${session.week}_${session.day}"),
            ) {
                SessionDayCardContent(
                    session = session,
                    isCurrentDay = current,
                    showRowCount = true,
                    referenceLabel = foundationReferenceLabel(session, state),
                )
            }
        }
        item {
            LegendDivider()
            Text("NEXT WEEK • WEEK ${preview.currentWeek}", fontWeight = FontWeight.Bold, modifier = Modifier.testTag("next_week"))
            Text("${V74Pointer.roman(preview.v74Block ?: 1)} · Week ${preview.v74BlockWeek} of 8 · Loop ${preview.v74Loop}")
            splitForWeek(preview.currentWeek, preview).forEach { session ->
                Text("DAY ${session.day} • ${session.title}")
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
    if (state.authorityForWeek(state.currentWeek) == ProgramAuthority.V74) {
        V74ProgramScreen(state, onOpenDay)
        return
    }
    val currentPhase = blockFor(state, state.currentWeek)
    val selectedPhase = blockFor(state, selectedWeek)
    val selectedSplit = splitForWeek(selectedWeek, state)
    val nextWeek = (state.currentWeek + 1).takeIf { it <= 32 }
    val nextPhase = (if (state.authorityForWeek(state.currentWeek) == ProgramAuthority.V73) ProgramV73.blocks else ProgramV72.blocks).firstOrNull { it.startWeek > currentPhase.endWeek }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchHits = remember(searchQuery, state.retainedV71Week, state.authority, state.v73ActivationWeek, state.v74ActivationWeek, state.v74MigrationApplied, state.historicalAuthorities) { searchProgram(searchQuery, state) }
    val jumpWeeks = remember(state.currentWeek) { listOf(state.currentWeek, 1, 9, 17, 25, 32).distinct() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("program_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            BrandHeader()
            Text("32 WEEKS • 7 DAYS • 224 DAYS", style = technicalStyle(MaterialTheme.typography.headlineSmall))
            PhaseProgressStrip(currentPhase)
            Text(
                "${phaseFieldLabel(currentPhase)} • Weeks ${currentPhase.startWeek}–${currentPhase.endWeek}",
                color = phaseAccent(currentPhase),
                modifier = Modifier.testTag("current_phase"),
            )
            Text(
                "Weeks ${currentPhase.startWeek}–${currentPhase.endWeek}",
                style = technicalStyle(MaterialTheme.typography.labelSmall),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                nextPhase?.let { "Next • ${phaseFieldLabel(it)}" }
                    ?: "Next • Program complete at week 32. No week 33 is authorized.",
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
                    .semantics { contentDescription = "Search the approved 32-week program" },
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
                LegendDivider()
                Text("SEARCH RESULTS", fontWeight = FontWeight.Bold)
                Text(
                    V74SurfaceText.searchStatus(searchHits.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("program_search_status"),
                )
                TextButton(
                    onClick = { searchQuery = "" },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("program_search_clear"),
                ) { Text("CLEAR SEARCH") }
            }
            items(searchHits, key = { "search_${it.session.authority}_${it.session.week}_${it.session.day}" }) { hit ->
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
            (if (state.authorityForWeek(state.currentWeek) == ProgramAuthority.V73) ProgramV73.blocks else ProgramV72.blocks).forEach { phase ->
                PhaseSummaryCard(phase, isCurrent = phase.startWeek == currentPhase.startWeek)
            }
        }
        item {
            LegendDivider()
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
            PhaseIdentityLine(
                selectedPhase,
                active = selectedPhase.startWeek == currentPhase.startWeek,
            )
        }
        items(selectedSplit, key = { it.day }) { session ->
            Card(
                onClick = { onOpenDay(session) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("program_day_${session.week}_${session.day}"),
            ) {
                SessionDayCardContent(
                    session = session,
                    isCurrentDay = session.week == state.currentWeek && session.day == state.currentDay,
                    showRowCount = true,
                    referenceLabel = foundationReferenceLabel(session, state),
                )
            }
        }
        if (nextWeek != null) {
            item {
                LegendDivider()
                Text("NEXT WEEK • WEEK $nextWeek", fontWeight = FontWeight.Bold, modifier = Modifier.testTag("next_week"))
                val phase = blockFor(state, nextWeek)
                PhaseIdentityLine(phase, active = phase.startWeek == currentPhase.startWeek)
                splitForWeek(nextWeek, state).forEach { session -> Text("DAY ${session.day} • ${session.title}") }
            }
        }
    }
}

@Composable
private fun ToolsScreen(
    state: ProgramUiState,
    onStateChanged: (ProgramUiState) -> Unit,
    onOpenKey: () -> Unit,
    onProgramReset: (ProgramUiState) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val latestState by rememberUpdatedState(state)
    val latestOnStateChanged by rememberUpdatedState(onStateChanged)
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.utilityRestTimerEndEpochMillis) {
        val end = state.utilityRestTimerEndEpochMillis
        now = System.currentTimeMillis()
        while (end != null && end > System.currentTimeMillis()) {
            delay(1_000)
            now = System.currentTimeMillis()
        }
        if (end != null && end == latestState.utilityRestTimerEndEpochMillis && end <= System.currentTimeMillis()) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            latestOnStateChanged(latestState.copy(utilityRestTimerEndEpochMillis = null))
        }
    }
    val timerRemaining = state.utilityRestTimerEndEpochMillis?.let { ((it - now).coerceAtLeast(0L) / 1_000L).toInt() } ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("tools_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            BrandHeader()
            Text("TOOLS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                V74SurfaceText.FONT_LICENSE_LINE,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("font_licenses"),
            )
        }
        item { BuildIntegrityCard(state.authorityForWeek(state.currentWeek)) }
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
                    style = technicalStyle(MaterialTheme.typography.titleLarge),
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
                ) { Text("STOP TIMER", color = MaterialTheme.colorScheme.error) }
            } else {
                Button(
                    enabled = state.restTimerSeconds != null,
                    colors = legendButtonColors(),
                    onClick = {
                        val seconds = state.restTimerSeconds ?: return@Button
                        onStateChanged(state.copy(utilityRestTimerEndEpochMillis = System.currentTimeMillis() + seconds * 1_000L))
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("rest_timer_start"),
                ) { Text("START TIMER") }
            }
        }
        item {
            TextButton(
                onClick = onOpenKey,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("open_key"),
            ) { Text("OPEN KEY") }
        }
        item { AdditionalToolsCards(state.authorityForWeek(state.currentWeek), onProgramReset) }
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
                PhaseIdentityLine(
                    session.phase,
                    active = session.phase.startWeek == blockFor(state, state.currentWeek).startWeek,
                )
                Text("AUTHORITY v${session.authority.versionLabel}", style = MaterialTheme.typography.labelMedium)
                V74GlobalLine(session.authority == ProgramAuthority.V74)
                if (sessionExecutionGuidance(session).isNotEmpty()) SessionExecutionBanner(session)
                if (foundationReferenceLabel(session, state) != null) FoundationReferenceBanner(session, state)
                session.rows.forEach { row ->
                    VisualRowTitle(
                        session = session,
                        row = row,
                        title = row.title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.testTag("day_row_${row.id}"),
                    )
                    Text(row.prescription, style = technicalStyle(), modifier = Modifier.testTag("day_prescription_${row.id}"))
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
    LegendBrandHeader()
}

private fun newWorkout(state: ProgramUiState) = WorkoutProgress.start(state)

private fun resolveSession(week: Int, day: Int, state: ProgramUiState): SessionReference {
    val authority = state.authorityForWeek(week)
    if (authority != ProgramAuthority.V74) return SessionResolver.resolve(week, day, authority)
    val block: Int
    val blockWeek: Int
    if (week == state.currentWeek && state.v74Block != null && state.v74BlockWeek != null) {
        block = state.v74Block
        blockWeek = state.v74BlockWeek
    } else {
        val position = com.jk.operatorperformance.program.ProgramV74.position(week)
        block = position.first
        blockWeek = position.second
    }
    return V74Pointer.reference(week, block, blockWeek, day)
}

private fun splitForWeek(week: Int, state: ProgramUiState): List<SessionReference> =
    (1..7).map { resolveSession(week, it, state) }

private fun blockFor(state: ProgramUiState, week: Int) = when (state.authorityForWeek(week)) {
    ProgramAuthority.V71 -> com.jk.operatorperformance.program.ProgramV71.blockForWeek(week)
    ProgramAuthority.V73 -> ProgramV73.blockForWeek(week)
    ProgramAuthority.V72 -> ProgramV72.blockForWeek(week)
    ProgramAuthority.V74 -> V74Pointer.phase(
        if (week == state.currentWeek && state.v74Block != null) state.v74Block
        else com.jk.operatorperformance.program.ProgramV74.position(week).first,
    )
}

private fun formatRest(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

@Composable
private fun V74GlobalLine(show: Boolean) {
    if (!show) return
    Text(ProgramV74.GLOBAL_LINE, style = MaterialTheme.typography.bodyMedium)
}
