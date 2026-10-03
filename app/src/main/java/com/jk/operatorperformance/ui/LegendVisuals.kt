package com.jk.operatorperformance.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.R
import com.jk.operatorperformance.program.ProgramV72
import com.jk.operatorperformance.program.ProgramBlock
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind

@DrawableRes
private fun phaseEmblemRes(phase: ProgramBlock): Int = when (phase.name) {
    "Draft" -> R.drawable.ic_phase_draft
    "Build" -> R.drawable.ic_phase_build
    "Backstop" -> R.drawable.ic_phase_backstop
    "Live" -> R.drawable.ic_phase_live
    else -> R.drawable.ic_legend_mark
}

@DrawableRes
private fun dayGlyphRes(day: Int): Int = when (day) {
    1 -> R.drawable.ic_lift_squat
    2 -> R.drawable.ic_lift_bench
    3 -> R.drawable.ic_event_sprint
    4 -> R.drawable.ic_rest_laurel
    5 -> R.drawable.ic_lift_deadlift
    6 -> R.drawable.ic_legend_mark
    7 -> R.drawable.ic_event_engine
    8 -> R.drawable.ic_rest_laurel
    else -> R.drawable.ic_legend_mark
}

@DrawableRes
private fun rowGlyphRes(row: SessionRow): Int? {
    val title = row.title.uppercase()
    return when {
        row.id == "d1_squat" -> R.drawable.ic_lift_squat
        row.id == "d2_bench" -> R.drawable.ic_lift_bench
        row.id == "d5_deadlift" -> R.drawable.ic_lift_deadlift
        row.kind == SessionRowKind.REST -> R.drawable.ic_rest_laurel
        row.id.startsWith("d3_") || title.contains("ACCELERATION") || title.contains("MAX VELOCITY") ||
            title.contains("SPECIAL ENDURANCE") || title.contains("300 M") -> R.drawable.ic_event_sprint
        row.id.startsWith("d6_engine") || title.contains("1.5-MILE") || title.contains("THRESHOLD") ||
            title.contains("VO2") || title.contains("GOAL PACE") -> R.drawable.ic_event_engine
        else -> null
    }
}

@Composable
internal fun LegendBrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_legend_mark),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                ProgramV72.PROGRAM_NAME,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                ProgramV72.PROGRAM_TAGLINE,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
    Spacer(Modifier.height(4.dp))
}

@Composable
internal fun PhaseIdentityLine(phase: ProgramBlock, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics { contentDescription = "${phaseFieldLabel(phase)}. Weeks ${phase.startWeek}–${phase.endWeek}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(phaseEmblemRes(phase)),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                phaseFieldLabel(phase),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Weeks ${phase.startWeek}–${phase.endWeek}",
                style = technicalStyle(MaterialTheme.typography.labelSmall),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun PhaseProgressStrip(currentPhase: ProgramBlock) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        ProgramV72.blocks.forEach { phase ->
            val current = phase.startWeek == currentPhase.startWeek
            val tint = if (current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            val index = ProgramV72.blocks.indexOfFirst { it.startWeek == phase.startWeek } + 1
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = phaseFieldLabel(phase) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    painter = painterResource(phaseEmblemRes(phase)),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(if (current) 28.dp else 22.dp),
                )
                Text(
                    index.toString(),
                    style = technicalStyle(MaterialTheme.typography.labelMedium),
                    color = tint,
                )
            }
        }
    }
}

@Composable
internal fun PhaseSummaryCard(phase: ProgramBlock, isCurrent: Boolean) {
    Card(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Box(Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(phaseEmblemRes(phase)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(72.dp)
                    .alpha(if (isCurrent) 0.16f else 0.08f),
            )
            Column(
                modifier = Modifier.padding(16.dp).padding(end = 64.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(phaseFieldLabel(phase), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                Text(
                    "Weeks ${phase.startWeek}–${phase.endWeek}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = technicalStyle(MaterialTheme.typography.labelMedium),
                )
                Text(phase.purpose, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
internal fun SessionDayCardContent(
    session: SessionReference,
    isCurrentDay: Boolean,
    showRowCount: Boolean,
    padding: Dp = 14.dp,
    referenceLabel: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(dayGlyphRes(session.day)),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "DAY ${session.day}${if (isCurrentDay) " • TODAY" else ""}",
                style = technicalStyle(MaterialTheme.typography.labelLarge),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(session.title, style = MaterialTheme.typography.titleMedium)
            referenceLabel?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (showRowCount) {
                Text(
                    "${session.rows.size} prescribed rows",
                    style = technicalStyle(MaterialTheme.typography.labelMedium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun VisualRowTitle(
    row: SessionRow,
    title: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rowGlyphRes(row)?.let { iconRes ->
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(title, style = style, modifier = modifier)
    }
}
