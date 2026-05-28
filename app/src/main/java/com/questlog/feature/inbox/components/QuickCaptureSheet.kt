package com.questlog.feature.inbox.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.questlog.feature.inbox.voice.MicrophonePermissionGate
import com.questlog.feature.inbox.voice.rememberVoiceCaptureLauncher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCaptureSheet(
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit,
    onVoiceCapture: (String) -> Unit,
    isSubmitting: Boolean,
    microphoneConsented: Boolean,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    var requestMic by remember { mutableStateOf(false) }

    val launchVoice = rememberVoiceCaptureLauncher { recognized ->
        if (recognized.isNotBlank()) {
            text = recognized
        }
    }

    // 권한 요청 후 승인되면 STT 즉시 실행
    MicrophonePermissionGate(
        trigger = requestMic,
        onGranted = { launchVoice() },
        onDenied = { /* 거부 시 버튼만 비활성 — 별도 안내 없음 */ },
        onReset = { requestMic = false },
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Inbox에 캡처", style = MaterialTheme.typography.titleMedium)
                if (microphoneConsented) {
                    IconButton(
                        onClick = { requestMic = true },
                        enabled = !isSubmitting,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "음성 캡처",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    IconButton(onClick = {}, enabled = false) {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "음성 캡처 비활성 (설정 > 개인정보 동의 필요)",
                            tint = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("머릿속에 떠오른 것을 그대로 적기") },
                minLines = 3,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default,
                ),
                enabled = !isSubmitting,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                    Text("취소")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onSubmit(text) },
                    enabled = !isSubmitting && text.isNotBlank(),
                ) {
                    Text(if (isSubmitting) "저장 중…" else "저장")
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
