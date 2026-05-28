package com.questlog.feature.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.ConsentScope

private data class ScopeSpec(
    val title: String,
    val description: String,
    val dataTransmitted: String,
    val policyVersion: Int,
)

private fun specForScope(scope: ConsentScope): ScopeSpec = when (scope) {
    ConsentScope.AI_OUTBOUND -> ScopeSpec(
        title = "Claude AI 기능 동의",
        description = "퀘스트 완료 내러티브, 주간 리뷰 요약, 클래리파이 제안 등 AI 기능을 사용합니다.",
        dataTransmitted = "전송 데이터: 태스크 제목(최대 50자), 컨텍스트 태그, 난이도(CR), 캐릭터 이름·레벨·클래스, 스트릭 일수, 주간 완료/XP/크리티컬 통계. 메모·첨부·NPC 이름은 전송되지 않습니다.",
        policyVersion = 2,
    )
    ConsentScope.CONTACTS -> ScopeSpec(
        title = "연락처 접근 동의",
        description = "NPC 등록 시 연락처에서 이름을 가져옵니다.",
        dataTransmitted = "기기 내 연락처 이름만 로컬 저장. 외부 서버 전송 없음.",
        policyVersion = 1,
    )
    ConsentScope.MICROPHONE -> ScopeSpec(
        title = "마이크 접근 동의",
        description = "음성 입력으로 Inbox에 빠르게 캡처합니다.",
        dataTransmitted = "STT 변환은 기기 내 처리. 음성 파일은 외부 전송되지 않습니다.",
        policyVersion = 1,
    )
}

@Composable
fun ConsentDialog(
    scope: ConsentScope,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    val spec = specForScope(scope)
    AlertDialog(
        onDismissRequest = onDecline,
        title = { Text(spec.title) },
        text = {
            Column {
                Text(spec.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(spec.dataTransmitted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "정책 v${spec.policyVersion}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        },
        confirmButton = { TextButton(onClick = onAccept) { Text("동의") } },
        dismissButton = { TextButton(onClick = onDecline) { Text("거부") } },
    )
}
