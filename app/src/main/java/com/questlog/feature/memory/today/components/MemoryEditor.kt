package com.questlog.feature.memory.today.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val BODY_MAX_LENGTH = 500

@Composable
fun MemoryEditor(
    selectedTitle: String,
    onSave: (String) -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    var body by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Text(
            text = "오늘의 기억: $selectedTitle",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { if (it.length <= BODY_MAX_LENGTH) body = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("오늘 어떤 일이 있었나요? 느낀 점, 배운 점을 자유롭게 남겨보세요.") },
            minLines = 4,
            maxLines = 8,
            supportingText = {
                Text(
                    text = "${body.length} / $BODY_MAX_LENGTH",
                    color = if (body.length >= BODY_MAX_LENGTH)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onSave(body) },
            enabled = body.isNotBlank() && !isSaving,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(if (isSaving) "저장 중..." else "기억 저장")
        }
    }
}
