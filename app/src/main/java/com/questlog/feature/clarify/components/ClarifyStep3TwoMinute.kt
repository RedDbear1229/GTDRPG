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
fun ClarifyStep3TwoMinute(
    onYes: () -> Unit,
    onNo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Q3. 2분 안에 끝낼 수 있나요?", style = MaterialTheme.typography.titleMedium)
        Text("\"예\" → 지금 바로 처리 + 자동 완료 처리합니다.", style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onYes, modifier = Modifier.fillMaxWidth()) { Text("예 — 지금 끝낸다") }
        OutlinedButton(onClick = onNo, modifier = Modifier.fillMaxWidth()) { Text("아니오") }
    }
}
