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

@DrawableRes
private fun phaseEmblemRes(phase: ProgramBlock): Int = when (phase.name) {
    "Draft" -> R.drawable.ic_phase_draft
    "Build" -> R.drawable.ic_phase_build
    "Backstop" -> R.drawable.ic_phase_backstop
    "Live" -> R.drawable.ic_phase_live
    else -> R.drawable.ic_legend_mark
}

@DrawableRes
internal fun mapSymbolRes(symbol: MapSymbol): Int = when (symbol) {
    MapSymbol.ENGINE -> R.drawable.ic_day_engine
    MapSymbol.LOWER -> R.drawable.ic_day_lower
    MapSymbol.SPEED -> R.drawable.ic_day_speed
    MapSymbol.REST -> R.drawable.ic_day_rest
    MapSymbol.UPPER_A -> R.drawable.ic_day_upper_a
    MapSymbol.UPPER_B -> R.drawable.ic_day_upper_b
}

@Composable
internal fun LegendBrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_legend_mark),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(36.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                ProgramV72.PROGRAM_NAME,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                ProgramV72.PROGRAM_TAGLINE,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    HorizontalDivider(Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outline)
    Spacer(Modifier.height(4.dp))
}

@Composable
internal fun PhaseIdentityLine(phase: ProgramBlock, modifier: Modifier = Modifier, active: Boolean = true) {
    Row(
        modifier = modifier.semantics { contentDescription = "${phaseFieldLabel(phase)}. Weeks ${phase.startWeek}–${phase.endWeek}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(phaseEmblemRes(phase)),
            contentDescription = null,
            tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                phaseFieldLabel(phase),
                style = MaterialTheme.typography.titleSmall,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
                tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(72.dp)
                    .alpha(if (isCurrent) 0.22f else 0.12f),
            )
            Column(
                modifier = Modifier.padding(16.dp).padding(end = 64.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    phaseFieldLabel(phase),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
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
            painter = painterResource(mapSymbolRes(sessionSymbol(session))),
            contentDescription = null,
            tint = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(32.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "DAY ${session.day}${if (isCurrentDay) " • TODAY" else ""}",
                style = technicalStyle(MaterialTheme.typography.labelLarge),
                color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(session.title, style = MaterialTheme.typography.titleMedium)
            referenceLabel?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (showRowCount) {
                Text(
                    sessionFocusLine(session),
                    style = technicalStyle(MaterialTheme.typography.labelMedium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun VisualRowTitle(
    session: SessionReference,
    row: SessionRow,
    title: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        rowSymbol(session, row)?.let { symbol ->
            Icon(
                painter = painterResource(mapSymbolRes(symbol)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(title, style = style, modifier = modifier)
    }
}
