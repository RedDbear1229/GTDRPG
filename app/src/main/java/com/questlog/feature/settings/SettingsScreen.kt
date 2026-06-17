package com.questlog.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.questlog.core.domain.model.ConsentScope
import com.questlog.core.domain.model.DriveAccount
import com.questlog.feature.settings.components.ConsentDialog
import com.questlog.feature.settings.components.PrivacyControlsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingConsent by remember { mutableStateOf<ConsentScope?>(null) }
    var apiKeyInput by remember { mutableStateOf("") }

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> viewModel.onSignInResult(result.data) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(8.dp))

            // ── 알림 설정 ──────────────────────────────────────────────────────
            NotificationSection()

            Spacer(Modifier.height(16.dp))

            // ── AI 동의 / Claude API ───────────────────────────────────────────
            PrivacyControlsSection(
                aiConsentGranted = state.aiConsentGranted,
                claudeApiEnabled = state.claudeApiEnabled,
                onToggleClaudeApi = viewModel::toggleClaudeApi,
                onRevokeAiConsent = viewModel::revokeAiConsent,
                onDeleteAiCache = { },
            )

            if (!state.aiConsentGranted) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { pendingConsent = ConsentScope.AI_OUTBOUND }) {
                    Text("Claude AI 기능 동의하기")
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = { apiKeyInput = it },
                label = { Text("Claude API 키") },
                placeholder = { Text(if (state.apiKeySet) "저장됨 (변경하려면 입력)" else "sk-ant-...") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (apiKeyInput.isNotBlank()) {
                        TextButton(onClick = {
                            viewModel.saveApiKey(apiKeyInput)
                            apiKeyInput = ""
                        }) { Text("저장") }
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            // ── Google Drive 백업 ──────────────────────────────────────────────
            DriveBackupSection(
                account = state.driveAccount,
                autoSyncEnabled = state.driveAutoSyncEnabled,
                isSyncing = state.isSyncing,
                syncStatus = state.syncStatus,
                errorMessage = state.error,
                onErrorDismiss = viewModel::clearError,
                onSignIn = { signInLauncher.launch(viewModel.getSignInIntent()) },
                onSignOut = viewModel::signOutDrive,
                onUpload = viewModel::uploadBackup,
                onDownload = viewModel::downloadBackup,
                onToggleAutoSync = viewModel::toggleDriveAutoSync,
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    // 복원 후 재시작 다이얼로그
    if (state.needsRestart) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("복원 완료") },
            text = { Text("데이터 복원이 완료되었습니다. 적용하려면 앱을 재시작해 주세요.") },
            confirmButton = {
                Button(onClick = {
                    android.os.Process.killProcess(android.os.Process.myPid())
                }) { Text("지금 재시작") }
            },
        )
    }

    pendingConsent?.let { scope ->
        ConsentDialog(
            scope = scope,
            onAccept = {
                viewModel.onConsentAccepted(scope)
                pendingConsent = null
            },
            onDecline = {
                viewModel.onConsentDeclined()
                pendingConsent = null
            },
        )
    }
}

@Composable
private fun DriveBackupSection(
    account: DriveAccount?,
    autoSyncEnabled: Boolean,
    isSyncing: Boolean,
    syncStatus: String?,
    errorMessage: String?,
    onErrorDismiss: () -> Unit,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onUpload: () -> Unit,
    onDownload: () -> Unit,
    onToggleAutoSync: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Google Drive 백업",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))

            errorMessage?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                TextButton(onClick = onErrorDismiss) { Text("닫기") }
                Spacer(Modifier.height(4.dp))
            }

            if (account == null) {
                Text(
                    "Google 계정을 연결하면 데이터를 Drive에 안전하게 백업할 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text("Google 로그인")
                }
            } else {
                Text(
                    account.email,
                    style = MaterialTheme.typography.bodyMedium,
                )
                syncStatus?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.height(12.dp))

                // 수동 백업 / 복원
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onUpload,
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f),
                    ) {
                        if (isSyncing) CircularProgressIndicator(strokeWidth = 2.dp)
                        else Text("백업")
                    }
                    OutlinedButton(
                        onClick = onDownload,
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("복원")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 자동 동기화 토글
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("자동 백업", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "6시간마다 Drive에 자동으로 백업합니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = autoSyncEnabled, onCheckedChange = onToggleAutoSync)
                }

                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onSignOut) {
                    Text("로그아웃", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun NotificationSection() {
    val context = LocalContext.current
    val areEnabled = remember {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    var permissionGranted by remember { mutableStateOf(areEnabled) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "알림",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("퀘스트 알림", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "일일 리마인더, 기한 임박, HP 위기, 스트릭 경고",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = permissionGranted,
                    onCheckedChange = { checked ->
                        if (checked && !permissionGranted) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    },
                    enabled = !permissionGranted,
                )
            }

            if (!permissionGranted) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "알림이 비활성화되어 있습니다. 시스템 설정에서 직접 허용할 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = {
                        // 시스템 앱 알림 설정 화면으로 이동
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        ).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("시스템 알림 설정 열기")
                }
            }
        }
    }
}
