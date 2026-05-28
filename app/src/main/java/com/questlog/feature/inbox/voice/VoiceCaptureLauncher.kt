package com.questlog.feature.inbox.voice

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// STT 결과 콜백. 빈 문자열이면 인식 실패 또는 취소.
typealias VoiceCaptureCallback = (String) -> Unit

// 시스템 STT 다이얼로그를 열고 인식된 텍스트를 onResult로 전달한다.
// RECORD_AUDIO 권한은 MicrophonePermissionGate가 선행 처리. 미지원 기기에서는 result 빈 문자열.
@Composable
fun rememberVoiceCaptureLauncher(onResult: VoiceCaptureCallback): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                .orEmpty()
            onResult(matches.firstOrNull()?.trim().orEmpty())
        } else {
            onResult("")
        }
    }

    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "무엇을 캡처할까요?")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    return remember(launcher) { { launcher.launch(intent) } }
}
