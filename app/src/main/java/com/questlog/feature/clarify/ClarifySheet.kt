package com.questlog.feature.clarify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.feature.clarify.components.ClarifyStep1Actionable
import com.questlog.feature.clarify.components.ClarifyStep2NextAction
import com.questlog.feature.clarify.components.ClarifyStep3TwoMinute
import com.questlog.feature.clarify.components.ClarifyStep4Delegate
import com.questlog.feature.clarify.components.ClarifyStep5Project
import com.questlog.feature.clarify.components.ClarifyStep6Details

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClarifySheet(
    inboxId: String,
    onDismiss: () -> Unit,
) {
    // key = inboxId 가 없으면 같은 NavBackStackEntry 안에서 다음 항목을 열 때
    // 이전 VM 이 재사용되어 잘못된 inboxId 로 submit 될 수 있음 (Codex 정식 리뷰 P1).
    val viewModel: ClarifyViewModel = hiltViewModel(
        key = inboxId,
        creationCallback = { factory: ClarifyViewModel.Factory -> factory.create(inboxId) },
    )
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { onDismiss() }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
                    .navigationBarsPadding(),
                topBar = {
                    TopAppBar(
                        title = { Text("명료화") },
                        navigationIcon = {
                            IconButton(onClick = viewModel::back) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                            }
                        },
                        actions = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Filled.Close, contentDescription = "닫기")
                            }
                        },
                    )
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    LinearProgressIndicator(
                        progress = { stepProgress(state.step) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    val inbox = state.inboxItem
                    if (inbox == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) { Text("로딩 중…") }
                    } else {
                        when (state.step) {
                            ClarifyStep.Q1_ACTIONABLE -> ClarifyStep1Actionable(
                                rawText = inbox.rawText,
                                onActionable = { viewModel.selectActionable(true) },
                                onNonActionable = viewModel::selectNonActionable,
                            )
                            ClarifyStep.Q2_NEXT_ACTION -> ClarifyStep2NextAction(
                                value = state.nextAction,
                                onValueChange = viewModel::setNextAction,
                                onConfirm = viewModel::confirmNextAction,
                            )
                            ClarifyStep.Q3_TWO_MINUTE -> ClarifyStep3TwoMinute(
                                onYes = { viewModel.selectTwoMinute(true) },
                                onNo = { viewModel.selectTwoMinute(false) },
                            )
                            ClarifyStep.Q4_OWNER -> ClarifyStep4Delegate(
                                onMine = { viewModel.selectMine(true) },
                                onDelegate = { viewModel.selectMine(false) },
                            )
                            ClarifyStep.Q5_PROJECT -> ClarifyStep5Project(
                                onContinue = viewModel::confirmProject,
                            )
                            ClarifyStep.Q6_DETAILS -> ClarifyStep6Details(
                                estimatedMinutes = state.estimatedMinutes,
                                onEstimatedMinutesChange = viewModel::setEstimatedMinutes,
                                dueDate = state.dueDate,
                                onPickDueDate = { showDatePicker = true },
                                onClearDueDate = { viewModel.setDueDate(null) },
                                lifeArea = state.lifeArea,
                                onLifeAreaChange = viewModel::setLifeArea,
                                isSubmitting = state.isSubmitting,
                                onSubmit = viewModel::submitDetails,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDueDate(pickerState.selectedDateMillis)
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun stepProgress(step: ClarifyStep): Float = when (step) {
    ClarifyStep.Q1_ACTIONABLE -> 1f / 6f
    ClarifyStep.Q2_NEXT_ACTION -> 2f / 6f
    ClarifyStep.Q3_TWO_MINUTE -> 3f / 6f
    ClarifyStep.Q4_OWNER -> 4f / 6f
    ClarifyStep.Q5_PROJECT -> 5f / 6f
    ClarifyStep.Q6_DETAILS -> 1f
}
