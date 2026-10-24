package com.jk.operatorperformance.ui

import com.jk.operatorperformance.program.ProgramAuthority
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionResolver
import com.jk.operatorperformance.program.V74Pointer
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

    val requestedWeek = Regex("\\bweek (\\d{1,2})\\b")
        .find(normalized)
        ?.groupValues
        ?.get(1)
        ?.toIntOrNull()
    val requestedDay = Regex("\\bday ([1-8])\\b")
        .find(normalized)
        ?.groupValues
        ?.get(1)
        ?.toIntOrNull()
    if (requestedWeek != null && !weekIsSearchable(state, requestedWeek)) return emptyList()

    val freeText = normalized
        .replace(Regex("\\bweek \\d{1,2}\\b"), " ")
        .replace(Regex("\\bday [1-8]\\b"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
    val freeTextTokens = freeText.split(Regex("\\s+")).filter { it.isNotBlank() }

    return buildList {
        for (session in catalogSessions(state)) {
            if (requestedWeek != null && session.week != requestedWeek) continue
            if (requestedDay != null && session.day != requestedDay) continue

            val phaseText = session.phase.name
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
                if (!freeTextTokens.all(haystack::contains)) continue

                val matchingRow = session.rows.firstOrNull { row ->
                    val candidate = buildString {
                        append(row.title)
                        append(' ')
                        append(row.prescription)
                        row.note?.let { append(' '); append(it) }
                        row.alternatives.forEach { append(' '); append(it) }
                    }.lowercase()
                    freeTextTokens.isNotEmpty() && freeTextTokens.any(candidate::contains)
                }
                val snippet = matchingRow?.let { "${it.title} • ${it.prescription}" }
                    ?: phaseFieldLabel(session.phase)
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

internal fun catalogSessions(state: ProgramUiState): List<SessionReference> {
    if (!state.v74MigrationApplied) {
        return (1..32).flatMap { week -> SessionResolver.splitForWeek(week, state.authorityForWeek(week)) }
    }
    val activation = state.v74ActivationWeek ?: 1
    val retained = (1 until activation).filter { it in 1..32 }.flatMap { week ->
        SessionResolver.splitForWeek(week, state.authorityForWeek(week))
    }
    val loop = (1..4).flatMap { block ->
        (1..8).flatMap { blockWeek ->
            val absolute = V74Pointer.absoluteWeek(block, blockWeek)
            (1..7).map { day -> V74Pointer.reference(absolute, block, blockWeek, day) }
        }
    }
    return retained + loop
}

internal fun exportCatalogCsv(state: ProgramUiState): String = buildString {
    appendLine("authority,week,block,block_week,day,title")
    for (session in catalogSessions(state)) {
        val block = if (session.authority == ProgramAuthority.V74) session.phase.godName else ""
        val blockWeek = if (session.authority == ProgramAuthority.V74) {
            ((session.week - 1) % 8) + 1
        } else ""
        append(session.authority.name).append(',')
            .append(session.week).append(',')
            .append(block).append(',')
            .append(blockWeek).append(',')
            .append(session.day).append(',')
            .append(session.title)
            .appendLine()
    }
}

private fun weekIsSearchable(state: ProgramUiState, week: Int): Boolean {
    if (!state.v74MigrationApplied) return week in 1..32
    val retained = week >= 1 && week < (state.v74ActivationWeek ?: 1)
    return retained || week in 1..32
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
