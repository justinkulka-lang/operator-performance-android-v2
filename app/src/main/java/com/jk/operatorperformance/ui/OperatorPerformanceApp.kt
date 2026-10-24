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
import androidx.compose.material3.HorizontalDivider
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
import com.jk.operatorperformance.state.todayAuthorityMismatch
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

    fun persist(next: ProgramUiState) {
        state = next
        stateStore.save(next)
    }

    fun persistWorkout(next: ActiveWorkoutState) {
        activeWorkout = next
        val recorded = stateStore.saveWorkout(next)
        state = state.copy(historicalAuthorities = recorded.historicalAuthorities)
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
                    Text("The approved program uses 32 seven-day weeks with six training days and one rest day each week.")
                    Spacer(Modifier.height(12.dp))
                    Text("Starting begins at Week 1, Day 1. Existing progress is preserved. Engaged historical weeks keep their authority; LEGEND v7.3 starts at the next positively unstarted Day 1.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            clearWorkout()
                            stateStore.clearSessionStatus()
                            val started = state.startV73()
                            state = started
                            stateStore.save(started, replaceHistoricalAuthorities = true)
                            selectedProgramWeek = 1
                        },
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
                when (destination) {
                    Destination.TODAY -> TodayScreen(
                        state = state,
                        activeWorkout = activeWorkout?.takeIf { it.matchesPointer(state) },
                        onStartWorkout = {
                            val weekAuthority = state.authorityForWeek(state.currentWeek)
                            val saved = activeWorkout?.takeIf { it.matchesPointer(state) }
                            val savedAuthority = saved?.authority
                            if (savedAuthority == null || savedAuthority == weekAuthority) {
                                val existing = saved?.takeIf { !it.complete }
                                persistWorkout(existing ?: WorkoutProgress.start(state))
                                workoutMode = true
                            }
                        },
                        onProgress = ::persistWorkout,
                        onResetWorkout = ::clearWorkout,
                        onAdvance = {
                            val engaged = stateStore.markSessionEngaged(state.currentWeek, state.currentDay)
                            val weekAuthority = engaged.authorityForWeek(state.currentWeek)
                            val saved = activeWorkout?.takeIf { it.matchesPointer(state) }
                            val savedAuthority = saved?.authority
                            if (savedAuthority == null || savedAuthority == weekAuthority) clearWorkout()
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
                text = { Text("Set the current pointer to Week $week, Day $day? This clears the current session status, substitutions and workout timer. It preserves the utility timer, recorded engagement and each week’s authority; it does not mark any new movement complete.") },
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
    onAdvance: () -> Unit,
) {
    val session = resolveSession(state.currentWeek, state.currentDay, state)
    val mismatch = todayAuthorityMismatch(state, activeWorkout)
    val applicableWorkout = if (mismatch?.savedAuthority != null) null else activeWorkout
    val progress = applicableWorkout?.let { WorkoutProgress.normalize(it, session) }
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
                style = technicalStyle(MaterialTheme.typography.labelLarge),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("today_header"),
            )
            Text(session.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (session.authority != ProgramAuthority.V73 && state.v73MigrationApplied) {
                Text("RETAINED WEEK • v${session.authority.versionLabel}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            if (mismatch != null) {
                Text(
                    buildString {
                        append("WEEK AUTHORITY MISMATCH")
                        mismatch.savedAuthority?.let { append(" • saved session v${it.versionLabel}") }
                        mismatch.pointerAuthority?.let { append(" • pointer v${it.versionLabel}") }
                        append(" • week v${mismatch.weekAuthority.versionLabel}")
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("today_authority_mismatch"),
                )
                if (mismatch.savedAuthority != null) {
                    Text("Today's reference stays on this week's authority. The saved session is not applied.")
                }
                if (mismatch.pointerAuthority != null) {
                    Text("Today's reference uses this week's authority.")
                }
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
    if (!isRest && sessionStatus != SessionStatus.COMPLETED && mismatch?.savedAuthority == null) {
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
    progress: ActiveWorkoutState,
    authority: ProgramAuthority,
    onProgress: (ActiveWorkoutState) -> Unit,
    onReturnToToday: () -> Unit,
    onMarkComplete: () -> Unit,
) {
    val session = SessionResolver.resolve(progress.week, progress.day, authority)
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
        }
        items(split, key = { it.day }) { session ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("week_day_${session.day}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (session.day == state.currentDay) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface,
                ),
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
    val nextPhase = (if (state.authorityForWeek(state.currentWeek) == ProgramAuthority.V73) ProgramV73.blocks else ProgramV72.blocks).firstOrNull { it.startWeek > currentPhase.endWeek }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchHits = remember(searchQuery, state.retainedV71Week, state.authority, state.v73ActivationWeek, state.historicalAuthorities) { searchProgram(searchQuery, state) }
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
            (if (state.authorityForWeek(state.currentWeek) == ProgramAuthority.V73) ProgramV73.blocks else ProgramV72.blocks).forEach { phase ->
                PhaseSummaryCard(phase, isCurrent = phase.startWeek == currentPhase.startWeek)
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
            PhaseIdentityLine(
                selectedPhase,
                active = selectedPhase.startWeek == currentPhase.startWeek,
            )
        }
        items(selectedSplit, key = { it.day }) { session ->
            Card(onClick = { onOpenDay(session) }, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).testTag("program_day_${session.week}_${session.day}")) {
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
                HorizontalDivider()
                Text("NEXT WEEK • WEEK $nextWeek", fontWeight = FontWeight.Bold, modifier = Modifier.testTag("next_week"))
                val phase = blockFor(state, nextWeek)
                PhaseIdentityLine(phase, active = phase.startWeek == currentPhase.startWeek)
                splitForWeek(nextWeek, state).forEach { session -> Text("DAY ${session.day} • ${session.title}") }
            }
        }
    }
}

@Composable
private fun ToolsScreen(state: ProgramUiState, onStateChanged: (ProgramUiState) -> Unit, onOpenKey: () -> Unit) {
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
        item { BrandHeader(); Text("TOOLS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
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
        item { AdditionalToolsCards(state.authorityForWeek(state.currentWeek)) }
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

private fun resolveSession(week: Int, day: Int, state: ProgramUiState): SessionReference =
    SessionResolver.resolve(week, day, state.authorityForWeek(week))

private fun splitForWeek(week: Int, state: ProgramUiState): List<SessionReference> =
    SessionResolver.splitForWeek(week, state.authorityForWeek(week))

private fun blockFor(state: ProgramUiState, week: Int) =
    if (state.authorityForWeek(week) == ProgramAuthority.V71) com.jk.operatorperformance.program.ProgramV71.blockForWeek(week)
    else if (state.authorityForWeek(week) == ProgramAuthority.V73) ProgramV73.blockForWeek(week)
    else ProgramV72.blockForWeek(week)

private fun formatRest(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
