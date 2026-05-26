package com.questlog.feature.clarify.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ClarifyStep2NextAction(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Q2. 다음 물리적 행동은?", style = MaterialTheme.typography.titleMedium)
        Text(
            "구체적이고 작은 행동으로 적어주세요 (예: \"지마켓에서 생일 케이크 주문\")",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("선택사항 — 비워도 됩니다") },
            minLines = 2,
            maxLines = 4,
        )
        Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text("다음") }
    }
}
