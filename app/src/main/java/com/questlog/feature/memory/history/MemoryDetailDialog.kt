package com.questlog.feature.memory.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.MemoryEntry
import com.questlog.feature.memory.today.components.OutcomeBadge

@Composable
fun MemoryDetailDialog(
    entry: MemoryEntry,
    onDismiss: () -> Unit,
) {
    var showEnriched by remember { mutableStateOf(entry.enrichedBody != null) }
    val displayBody = if (showEnriched && entry.enrichedBody != null) entry.enrichedBody else entry.body

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row {
                    OutcomeBadge(outcomeType = entry.outcomeType)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.taskTitleSnapshot,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = entry.entryDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = displayBody,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("닫기") }
        },
        dismissButton = {
            if (entry.enrichedBody != null) {
                TextButton(onClick = { showEnriched = !showEnriched }) {
                    Text(if (showEnriched) "원본 보기" else "윤색본 보기")
                }
            }
        },
    )
}
