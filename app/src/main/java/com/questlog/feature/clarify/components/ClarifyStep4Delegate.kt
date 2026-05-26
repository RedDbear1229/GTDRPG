package com.questlog.feature.clarify.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ClarifyStep4Delegate(
    onMine: () -> Unit,
    onDelegate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Q4. 내가 직접 해야 하나요?", style = MaterialTheme.typography.titleMedium)
        Text(
            "위임 시 \"대기중\" 상태가 됩니다. NPC 선택은 Phase 2 에서 추가됩니다.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onMine, modifier = Modifier.fillMaxWidth()) { Text("내가 한다") }
        OutlinedButton(onClick = onDelegate, modifier = Modifier.fillMaxWidth()) { Text("위임 / 대기") }
    }
}
