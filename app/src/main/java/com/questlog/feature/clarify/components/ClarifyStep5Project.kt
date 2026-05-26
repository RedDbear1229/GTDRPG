package com.questlog.feature.clarify.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Phase 1: 프로젝트 생성/선택 UI 는 F1.4 QuestBoard 프로젝트 탭에서 도입.
// 본 단계에선 "프로젝트 없음" 단일 옵션만 제공하고 다음으로.
@Composable
fun ClarifyStep5Project(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Q5. 프로젝트에 묶을까요?", style = MaterialTheme.typography.titleMedium)
        Text(
            "프로젝트 픽커는 곧 추가됩니다 (F1.4). 지금은 \"단독 퀘스트\" 로 진행합니다.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("단독 퀘스트로 진행") }
    }
}
