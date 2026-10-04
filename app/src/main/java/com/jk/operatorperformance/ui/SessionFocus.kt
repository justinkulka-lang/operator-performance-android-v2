package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.Muscle
import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.ProgramV71
import com.jk.operatorperformance.program.ProgramV72
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind

internal enum class MapSymbol { ENGINE, LOWER, SPEED, REST, UPPER_A, UPPER_B }

internal fun sessionFocusLine(session: SessionReference): String {
    if (isRestSession(session)) return "Rest · no training blocks"
    val work = session.rows.filter { !isSupportRow(it) }
    val count = work.size.coerceAtLeast(1)
    val noun = if (count == 1) "block" else "blocks"
    return "${leadLabel(session, work)} lead · $count $noun"
}

internal fun sessionSymbol(session: SessionReference): MapSymbol {
    if (isRestSession(session)) return MapSymbol.REST
    val title = session.title
    return when {
        title.contains("Upper B", ignoreCase = true) -> MapSymbol.UPPER_B
        title.startsWith("Upper", ignoreCase = true) -> MapSymbol.UPPER_A
        title.contains("Lower", ignoreCase = true) -> MapSymbol.LOWER
        title.contains("Speed", ignoreCase = true) -> MapSymbol.SPEED
        else -> MapSymbol.ENGINE
    }
}

internal fun rowSymbol(session: SessionReference, row: SessionRow): MapSymbol? {
    if (isSupportRow(row)) return null
    if (row.kind == SessionRowKind.REST) return MapSymbol.REST
    ownerMuscle(session, row)?.let { owner ->
        return when (owner) {
            Muscle.QUADS, Muscle.HAMSTRINGS, Muscle.GLUTES, Muscle.CALVES -> MapSymbol.LOWER
            Muscle.CHEST, Muscle.DELTS, Muscle.TRICEPS -> MapSymbol.UPPER_A
            Muscle.LATS, Muscle.UPPER_BACK, Muscle.BICEPS, Muscle.TRAPS, Muscle.FOREARMS -> MapSymbol.UPPER_B
            Muscle.ABS -> null
        }
    }
    val title = row.title
    return when {
        title.contains("300", ignoreCase = true) || title.contains("fly", ignoreCase = true) ||
            title.contains("Acceleration", ignoreCase = true) || title.contains("endurance", ignoreCase = true) -> MapSymbol.SPEED
        title.contains("Threshold", ignoreCase = true) || title.contains("interval", ignoreCase = true) ||
            title.contains("Goal pace", ignoreCase = true) || title.contains("1.5-mile", ignoreCase = true) ||
            title.contains("Easy", ignoreCase = true) || title.contains("Bike", ignoreCase = true) ||
            title.contains("aerobic", ignoreCase = true) -> MapSymbol.ENGINE
        else -> null
    }
}

private fun isRestSession(session: SessionReference): Boolean =
    session.title.equals("Rest", ignoreCase = true) || session.rows.all { it.kind == SessionRowKind.REST }

private fun isSupportRow(row: SessionRow): Boolean =
    row.effortLabel == "WARM-UP" || row.effortLabel == "COOLDOWN" || row.effortLabel == "CHECK"

private fun leadLabel(session: SessionReference, work: List<SessionRow>): String {
    val firstResistance = work.indexOfFirst { it.kind == SessionRowKind.RESISTANCE }
    val firstEngine = work.indexOfFirst { isEngineWork(it) }
    val firstSpeed = work.indexOfFirst { isSpeedWork(it) }
    val firstRecovery = work.indexOfFirst { isRecoveryWork(it) }
    if (firstEngine >= 0 && before(firstEngine, firstResistance) && before(firstEngine, firstSpeed)) return "Engine"
    if (firstSpeed >= 0 && before(firstSpeed, firstResistance)) return "Speed"
    if (firstRecovery >= 0 && firstResistance < 0 && firstEngine < 0 && firstSpeed < 0) return "Recovery"
    if (session.title.startsWith("Upper", ignoreCase = true)) return "Upper"
    val owners = work.mapNotNull { ownerMuscle(session, it) }
    if (owners.isNotEmpty()) {
        val counts = owners.groupingBy { it }.eachCount()
        val best = counts.maxOf { it.value }
        return owners.first { counts.getValue(it) == best }.label
    }
    return "Session"
}

private fun before(index: Int, other: Int): Boolean = other < 0 || index < other

private fun isSpeedWork(row: SessionRow): Boolean {
    if (row.kind != SessionRowKind.CONDITIONING || isSupportRow(row)) return false
    val title = row.title
    return title.contains("300", ignoreCase = true) || title.contains("fly", ignoreCase = true) ||
        title.contains("Acceleration", ignoreCase = true) || title.contains("endurance", ignoreCase = true) ||
        title.contains("Speed", ignoreCase = true)
}

private fun isRecoveryWork(row: SessionRow): Boolean {
    if (row.kind != SessionRowKind.CONDITIONING || isSupportRow(row)) return false
    return row.effortLabel == "RECOVERY" || row.title.contains("bike", ignoreCase = true) ||
        row.title.contains("mobility", ignoreCase = true)
}

private fun isEngineWork(row: SessionRow): Boolean =
    row.kind == SessionRowKind.CONDITIONING && !isSupportRow(row) && !isSpeedWork(row) && !isRecoveryWork(row)

private fun ownerMuscle(session: SessionReference, row: SessionRow): Muscle? {
    val slots = if (session.authority == ProgramAuthority.V72) ProgramV72.resistanceSlots else ProgramV71.resistanceSlots
    val phase = if (session.authority == ProgramAuthority.V72) ProgramV72.phaseForWeek(session.week) else ProgramV71.phaseForWeek(session.week)
    return slots.firstOrNull { it.phase == phase && it.id == row.id }?.ownerMuscle
}
