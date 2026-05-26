package com.questlog.feature.clarify.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.usecase.NonActionable

@Composable
fun ClarifyStep1Actionable(
    rawText: String,
    onActionable: () -> Unit,
    onNonActionable: (NonActionable) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Q1. 실행 가능한 항목인가요?", style = MaterialTheme.typography.titleMedium)
        Text(rawText, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onActionable, modifier = Modifier.fillMaxWidth()) { Text("예, 행동 가능") }
        Text("아니라면…", style = MaterialTheme.typography.labelMedium)
        OutlinedButton(
            onClick = { onNonActionable(NonActionable.SOMEDAY) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("언젠가/아마도") }
        OutlinedButton(
            onClick = { onNonActionable(NonActionable.REFERENCE) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("참고자료로 보관") }
        TextButton(
            onClick = { onNonActionable(NonActionable.DELETE) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("버리기") }
    }
}
