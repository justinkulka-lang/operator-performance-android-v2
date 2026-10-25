package com.jk.operatorperformance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.R

@Composable
internal fun KeyScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("key_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            LegendBrandHeader()
            Text("KEY", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Plain meanings for the marks on screen. This page does not change the program.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { KeyHeading("MARK") }
        item { KeyIconRow(R.drawable.ic_legend_mark, "LEGEND mark", "The map key. It names the program.") }
        item { KeyHeading("DAYS") }
        item { KeyIconRow(R.drawable.ic_day_engine, "Engine", "Contour rings. Aerobic and engine work.", Color(LegendPalette.Olive)) }
        item { KeyIconRow(R.drawable.ic_day_lower, "Lower", "Solid survey point. Lower-body work.", Color(LegendPalette.Olive)) }
        item { KeyIconRow(R.drawable.ic_day_upper_a, "Upper A", "Open station. Names the Upper A day, which mixes pressing and pulling.", Color(LegendPalette.Olive)) }
        item { KeyIconRow(R.drawable.ic_day_speed, "Speed", "Route line with ticks. Speed work.", Color(LegendPalette.Olive)) }
        item { KeyIconRow(R.drawable.ic_day_upper_b, "Upper B", "Station with a traverse. Names the Upper B day, which mixes pressing and pulling. Easy aerobic follows when that week prescribes it.", Color(LegendPalette.Olive)) }
        item { KeyIconRow(R.drawable.ic_day_rest, "Rest", "Open circle. Rest.", Color(LegendPalette.Olive)) }
        item { KeyHeading("PHASES") }
        item { KeyIconRow(R.drawable.ic_phase_draft, "Draft", "One stake. Phase 1.") }
        item { KeyIconRow(R.drawable.ic_phase_build, "Build", "Two stakes. Phase 2.") }
        item { KeyIconRow(R.drawable.ic_phase_backstop, "Backstop", "Three stakes. Phase 3.") }
        item { KeyIconRow(R.drawable.ic_phase_live, "Live", "Four stakes. Phase 4.") }
        item { KeyHeading("NAVIGATION") }
        item { KeyIconRow(R.drawable.ic_nav_today, "Today", "Marked point. The current day.") }
        item { KeyIconRow(R.drawable.ic_nav_week, "Week", "Even stakes. The current week.") }
        item { KeyIconRow(R.drawable.ic_nav_program, "Program", "Map sheet. The 32-week program.") }
        item { KeyIconRow(R.drawable.ic_nav_tools, "Tools", "Scale bar. Tools.") }
        item { KeyHeading("STATE") }
        item {
            Text(
                "Brass marks today, the active phase, primary actions, and the selected tab.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item {
            Text(
                "Red marks stop, pain, warning, and error. It is not a decoration.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("key_state_red"),
            )
        }
        item {
            Text(
                "Day labels use neutral type unless the day is today.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("key_back"),
            ) { Text("RETURN TO TOOLS") }
        }
    }
}

@Composable
private fun KeyHeading(title: String) {
    Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun KeyIconRow(icon: Int, name: String, meaning: String, tint: Color = Color.Unspecified) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = if (tint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else tint,
            modifier = Modifier.size(28.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, style = MaterialTheme.typography.titleSmall)
            Text(meaning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
