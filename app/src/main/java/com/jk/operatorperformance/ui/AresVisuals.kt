package com.jk.operatorperformance.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.R
import com.jk.operatorperformance.program.AresProgramV71
import com.jk.operatorperformance.program.ProgramBlock
import com.jk.operatorperformance.program.SessionReference
import com.jk.operatorperformance.program.SessionRow
import com.jk.operatorperformance.program.SessionRowKind

private val HephaestusCopper = Color(0xFFD17A45)
private val HermesBlue = Color(0xFF62A9FF)
private val AresRed = Color(0xFFD95A57)
private val ZeusGold = Color(0xFFE0B743)

internal fun phaseAccent(phase: ProgramBlock): Color = when (phase.godName) {
    "HEPHAESTUS" -> HephaestusCopper
    "HERMES" -> HermesBlue
    "ARES" -> AresRed
    "ZEUS" -> ZeusGold
    else -> ZeusGold
}

@DrawableRes
private fun phaseEmblemRes(phase: ProgramBlock): Int = when (phase.godName) {
    "HEPHAESTUS" -> R.drawable.ic_phase_hephaestus
    "HERMES" -> R.drawable.ic_phase_hermes
    "ARES" -> R.drawable.ic_phase_ares
    "ZEUS" -> R.drawable.ic_phase_zeus
    else -> R.drawable.ic_ares_master
}

@DrawableRes
private fun dayGlyphRes(day: Int): Int = when (day) {
    1 -> R.drawable.ic_lift_squat
    2 -> R.drawable.ic_lift_bench
    3 -> R.drawable.ic_event_sprint
    4 -> R.drawable.ic_rest_laurel
    5 -> R.drawable.ic_lift_deadlift
    6 -> R.drawable.ic_ares_master
    7 -> R.drawable.ic_event_engine
    8 -> R.drawable.ic_rest_laurel
    else -> R.drawable.ic_ares_master
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
internal fun AresBrandHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_ares_master),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(42.dp),
        )
        Spacer(Modifier.width(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                AresProgramV71.PROGRAM_NAME,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "THE THRESHOLD",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                AresProgramV71.PROGRAM_TAGLINE,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    GreekKeyDivider()
    Spacer(Modifier.height(4.dp))
}

@Composable
internal fun PhaseIdentityLine(phase: ProgramBlock, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(phaseEmblemRes(phase)),
            contentDescription = null,
            tint = phaseAccent(phase),
            modifier = Modifier.size(34.dp),
        )
        Text(
            "${phase.godName} • ${phase.name}",
            color = phaseAccent(phase),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun PhaseProgressStrip(currentPhase: ProgramBlock) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        AresProgramV71.blocks.forEach { phase ->
            val current = phase.godName == currentPhase.godName
            val tint = if (current) phaseAccent(phase) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.46f)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    painter = painterResource(phaseEmblemRes(phase)),
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(if (current) 36.dp else 28.dp),
                )
                Text(
                    phase.godName,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
internal fun PhaseSummaryCard(phase: ProgramBlock, isCurrent: Boolean) {
    val accent = phaseAccent(phase)
    Card(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Box(Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(phaseEmblemRes(phase)),
                contentDescription = null,
                tint = accent,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(96.dp)
                    .alpha(if (isCurrent) 0.13f else 0.065f),
            )
            Column(
                modifier = Modifier.padding(16.dp).padding(end = 76.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("${phase.godName} — ${phase.name}", fontWeight = FontWeight.Bold, color = accent)
                Text(
                    "Weeks ${phase.startWeek}–${phase.endWeek} • POPULATED / APPROVED",
                    color = if (isCurrent) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
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
            tint = phaseAccent(session.phase),
            modifier = Modifier.size(40.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "DAY ${session.day}${if (isCurrentDay) " • TODAY" else ""}",
                color = phaseAccent(session.phase),
                fontWeight = FontWeight.Bold,
            )
            Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            referenceLabel?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            if (showRowCount) {
                Text("${session.rows.size} prescribed rows", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                modifier = Modifier.size(34.dp),
            )
        }
        Text(title, style = style, fontWeight = FontWeight.Bold, modifier = modifier)
    }
}

@Composable
private fun GreekKeyDivider() {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
    Canvas(Modifier.fillMaxWidth().height(12.dp)) {
        val h = size.height
        val segment = h * 1.25f
        val yTop = h * 0.24f
        val yBottom = h * 0.76f
        val path = Path()
        var x = 0f
        path.moveTo(0f, yBottom)
        while (x < size.width) {
            path.lineTo((x + segment).coerceAtMost(size.width), yBottom)
            path.lineTo((x + segment).coerceAtMost(size.width), yTop)
            path.lineTo((x + segment * 2f).coerceAtMost(size.width), yTop)
            path.lineTo((x + segment * 2f).coerceAtMost(size.width), yBottom)
            x += segment * 2f
        }
        drawPath(path = path, color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
    }
}
