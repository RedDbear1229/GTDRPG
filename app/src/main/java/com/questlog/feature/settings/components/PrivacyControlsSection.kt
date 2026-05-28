package com.questlog.feature.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.ConsentScope

@Composable
fun PrivacyControlsSection(
    aiConsentGranted: Boolean,
    claudeApiEnabled: Boolean,
    onToggleClaudeApi: (Boolean) -> Unit,
    onRevokeAiConsent: () -> Unit,
    onDeleteAiCache: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showRevokeConfirm by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("프라이버시 & AI 설정", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Claude AI 기능", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (aiConsentGranted) "동의됨 · 운영 토글로 일시 비활성화 가능"
                        else "동의 필요",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                Switch(
                    checked = claudeApiEnabled && aiConsentGranted,
                    onCheckedChange = { if (aiConsentGranted) onToggleClaudeApi(it) },
                    enabled = aiConsentGranted,
                )
            }

            if (aiConsentGranted) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { showRevokeConfirm = true }) {
                    Text("AI 동의 철회 및 캐시 삭제", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showRevokeConfirm) {
        AlertDialog(
            onDismissRequest = { showRevokeConfirm = false },
            title = { Text("AI 동의 철회") },
            text = { Text("AI 동의를 철회하면 Claude AI 기능이 즉시 비활성화됩니다. 계속하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    showRevokeConfirm = false
                    onRevokeAiConsent()
                    onDeleteAiCache()
                }) { Text("철회", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeConfirm = false }) { Text("취소") }
            },
        )
    }
}
