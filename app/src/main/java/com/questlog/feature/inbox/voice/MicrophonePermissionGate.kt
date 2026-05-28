package com.questlog.feature.inbox.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// RECORD_AUDIO 런타임 권한 요청 래퍼.
// trigger=true 일 때 권한 확인 → 이미 있으면 onGranted 즉시 / 없으면 시스템 다이얼로그 → 결과 전달.
// onReset: trigger를 false로 돌려놓는 콜백 (호출 측 상태 리셋).
@Composable
fun MicrophonePermissionGate(
    trigger: Boolean,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
    onReset: () -> Unit,
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) onGranted() else onDenied()
        onReset()
    }

    LaunchedEffect(trigger) {
        if (!trigger) return@LaunchedEffect
        val already = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (already) {
            onGranted()
            onReset()
        } else {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}
