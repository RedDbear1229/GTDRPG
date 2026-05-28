package com.questlog.feature.weekly

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.core.data.db.entity.WeeklyReviewEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(
    onBack: () -> Unit,
    viewModel: WeeklyReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WeeklyReviewEvent.ReviewComplete -> {
                    if (event.xpGained > 0) {
                        snackbarHostState.showSnackbar("+${event.xpGained} XP 획득! 주간 리뷰 완료!")
                    }
                }
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("주간 리뷰") },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isComplete) {
            CompletionScreen(
                xpAwarded = state.xpAwarded,
                weekLabel = state.weekStats.weekLabel,
                aiSummary = state.aiSummary,
                onDone = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            ReviewStepsScreen(
                state = state,
                onCheckStep = viewModel::checkCurrentStep,
                onNext = viewModel::nextStep,
                onPrevious = viewModel::previousStep,
                onGenerateAi = viewModel::generateAiSummary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
private fun ReviewStepsScreen(
    state: WeeklyReviewUiState,
    onCheckStep: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onGenerateAi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val totalSteps = state.steps.size
    val progress = if (totalSteps == 0) 0f else (state.currentStep + 1f) / totalSteps

    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            // 진행률
            Text(
                text = "${state.currentStep + 1} / $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // 이번 주 스탯 카드
            StatsCard(stats = state.weekStats)

            Spacer(Modifier.height(24.dp))

            // 현재 스텝
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
            ) { stepIdx ->
                val step = state.steps.getOrNull(stepIdx)
                if (step != null) {
                    StepCard(
                        step = step,
                        stepNumber = stepIdx + 1,
                        isLastStep = stepIdx == totalSteps - 1,
                        aiSummary = if (stepIdx == totalSteps - 1) state.aiSummary else null,
                        isGeneratingAi = state.isGeneratingAi,
                        onGenerateAi = onGenerateAi,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 네비게이션 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.currentStep > 0) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                ) { Text("이전") }
            }

            val currentStep = state.steps.getOrNull(state.currentStep)
            val isLastStep = state.currentStep == totalSteps - 1
            Button(
                onClick = {
                    if (currentStep?.isChecked == false) onCheckStep()
                    else onNext()
                },
                modifier = Modifier.weight(if (state.currentStep > 0) 1f else 1f),
            ) {
                Text(
                    when {
                        currentStep?.isChecked == false -> "확인"
                        isLastStep -> "완료 (+${WeeklyReviewEntity.REWARD_XP} XP)"
                        else -> "다음"
                    }
                )
            }
        }
    }
}

@Composable
private fun StepCard(
    step: ReviewStep,
    stepNumber: Int,
    isLastStep: Boolean,
    aiSummary: String?,
    isGeneratingAi: Boolean,
    onGenerateAi: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (step.isChecked)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (step.isChecked) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = null,
                    tint = if (step.isChecked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.padding(horizontal = 8.dp))
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyLarge,
            )

            if (isLastStep) {
                Spacer(Modifier.height(16.dp))
                when {
                    isGeneratingAi -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("던전마스터가 보고서를 작성 중...")
                        }
                    }
                    aiSummary != null -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "DM 주간 보고서",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = aiSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                    else -> {
                        FilledTonalButton(
                            onClick = onGenerateAi,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("DM 주간 보고서 받기")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsCard(stats: WeekStats, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stats.weekLabel,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(value = stats.completedCount.toString(), label = "완료")
                StatItem(value = stats.xpGained.toString(), label = "XP")
                StatItem(value = stats.critCount.toString(), label = "크리티컬")
                StatItem(value = stats.unfinishedCount.toString(), label = "미완료")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CompletionScreen(
    xpAwarded: Long,
    weekLabel: String,
    aiSummary: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "주간 리뷰 완료!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = weekLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (xpAwarded > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "+$xpAwarded XP",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "이번 주 리뷰는 이미 완료되었습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (aiSummary != null) {
            Spacer(Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.Filled.AutoAwesome, null, modifier = Modifier.size(16.dp))
                        Text("DM 주간 보고서", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(aiSummary, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text("돌아가기")
            }
        }
    }
}
