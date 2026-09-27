package com.jk.operatorperformance.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jk.operatorperformance.BuildConfig
import com.jk.operatorperformance.program.AresProgramV71

@Composable
internal fun BuildIntegrityCard() {
    Card(Modifier.fillMaxWidth().testTag("build_integrity_card")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("BUILD / AUTHORITY INTEGRITY", fontWeight = FontWeight.Bold)
            Text(
                "APP • ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "BUILD • ${BuildConfig.BUILD_COMMIT}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag("build_commit"),
            )
            Text(
                "PACKAGE • ${BuildConfig.APPLICATION_ID}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "AUTHORITY • v${AresProgramV71.AUTHORITY_VERSION}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "AUTHORITY SHA-256 • ${AresProgramV71.AUTHORITY_SHA256}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("authority_sha"),
            )
            Text(
                "${BuildConfig.BUILD_TYPE.uppercase()} BUILD • OFFLINE / LOCAL-ONLY",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Signing-certificate identity is enforced by release CI once the owner signing lineage is configured.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
