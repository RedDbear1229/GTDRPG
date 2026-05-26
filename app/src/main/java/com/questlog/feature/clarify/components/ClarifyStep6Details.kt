package com.questlog.feature.clarify.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.LifeArea
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClarifyStep6Details(
    estimatedMinutes: Int?,
    onEstimatedMinutesChange: (Int?) -> Unit,
    dueDate: Long?,
    onPickDueDate: () -> Unit,
    onClearDueDate: () -> Unit,
    lifeArea: LifeArea,
    onLifeAreaChange: (LifeArea) -> Unit,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var minutesText by remember(estimatedMinutes) {
        mutableStateOf(estimatedMinutes?.toString().orEmpty())
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Q6. 세부 설정", style = MaterialTheme.typography.titleMedium)

        Text("생활 영역", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LifeArea.values().forEach { area ->
                FilterChip(
                    selected = area == lifeArea,
                    onClick = { onLifeAreaChange(area) },
                    label = { Text("${area.icon} ${area.label}") },
                )
            }
        }

        OutlinedTextField(
            value = minutesText,
            onValueChange = {
                minutesText = it.filter(Char::isDigit)
                onEstimatedMinutesChange(minutesText.toIntOrNull())
            },
            label = { Text("예상 소요 (분)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Column {
            Text(
                text = "마감일: ${dueDate?.let { formatDate(it) } ?: "없음"}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Arrangement.spacedBy(8.dp)
            TextButton(onClick = onPickDueDate) { Text("마감일 선택") }
            if (dueDate != null) {
                TextButton(onClick = onClearDueDate) { Text("마감일 지우기") }
            }
        }

        Button(
            onClick = onSubmit,
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isSubmitting) "저장 중…" else "퀘스트 생성")
        }
    }
}

private fun formatDate(epochMillis: Long): String =
    SimpleDateFormat("yyyy.M.d", Locale.KOREAN).format(Date(epochMillis))
