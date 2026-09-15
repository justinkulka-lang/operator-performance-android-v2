package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.state.ProgramUiState

internal data class ProgramSearchHit(
    val session: SessionReference,
    val matchLabel: String,
    val matchSnippet: String,
)

internal fun searchProgram(
    query: String,
    state: ProgramUiState,
    limit: Int = 24,
): List<ProgramSearchHit> {
    val normalized = normalizeProgramQuery(query)
    if (normalized.isBlank() || limit <= 0) return emptyList()
    val tokens = normalized.split(Regex("\\s+")).filter { it.isNotBlank() }

    return buildList {
        for (week in 1..32) {
            val sessions = SessionResolver.splitForWeek(
                week = week,
                deload = false,
                machineInventoryConfigured = state.machineInventoryConfigured,
                availableMachineModalities = state.availableMachineModalities,
                speedVariant = state.speedVariant,
                engineVariant = state.engineVariant,
            )
            for (session in sessions) {
                val phaseText = "${session.phase.godName} ${session.phase.name}"
                val header = "week ${session.week} day ${session.day} ${session.title} $phaseText"
                val rowText = session.rows.joinToString(" ") { row ->
                    buildString {
                        append(row.title)
                        append(' ')
                        append(row.prescription)
                        row.note?.let { append(' '); append(it) }
                        row.alternatives.forEach { append(' '); append(it) }
                    }
                }
                val haystack = "$header $rowText".lowercase()
                if (!tokens.all(haystack::contains)) continue

                val matchingRow = session.rows.firstOrNull { row ->
                    val candidate = buildString {
                        append(row.title)
                        append(' ')
                        append(row.prescription)
                        row.note?.let { append(' '); append(it) }
                        row.alternatives.forEach { append(' '); append(it) }
                    }.lowercase()
                    tokens.any(candidate::contains)
                }
                val snippet = matchingRow?.let { "${it.title} • ${it.prescription}" }
                    ?: "${session.phase.godName} • ${session.phase.name}"
                add(
                    ProgramSearchHit(
                        session = session,
                        matchLabel = "W${session.week} • D${session.day} • ${session.title}",
                        matchSnippet = snippet.take(220),
                    ),
                )
                if (size >= limit) return@buildList
            }
        }
    }
}

internal fun normalizeProgramQuery(query: String): String {
    val lower = query.trim().lowercase()
    val expandedWeeks = Regex("\\bw(\\d{1,2})\\b").replace(lower) { match ->
        "week ${match.groupValues[1]}"
    }
    val expandedDays = Regex("\\bd([1-7])\\b").replace(expandedWeeks) { match ->
        "day ${match.groupValues[1]}"
    }
    return expandedDays
        .replace(Regex("[^a-z0-9.+/%–—-]+"), " ")
        .trim()
}
