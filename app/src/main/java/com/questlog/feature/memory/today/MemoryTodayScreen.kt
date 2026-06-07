package com.questlog.feature.memory.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.questlog.core.domain.model.MemoryTodayState
import com.questlog.feature.memory.today.components.CandidateCard
import com.questlog.feature.memory.today.components.MemoryEditor
import com.questlog.feature.memory.today.components.OutcomeBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryTodayScreen(
    onBack: () -> Unit,
    onHistory: () -> Unit,
    viewModel: MemoryTodayViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MemoryTodayUiEvent.Saved ->
                    snackbarHostState.showSnackbar("오늘의 기억이 저장되었습니다!")
                is MemoryTodayUiEvent.AlreadyExists ->
                    snackbarHostState.showSnackbar("오늘의 기억이 이미 저장되어 있습니다.")
                is MemoryTodayUiEvent.Error ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("오늘의 기억") },
                actions = {
                    IconButton(onClick = onHistory) {
                        Icon(Icons.Filled.AutoStories, contentDescription = "기억 기록")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val state = uiState.state) {
                is MemoryTodayState.NoCompletions -> NoCompletionsContent()
                is MemoryTodayState.Selecting -> SelectingContent(
                    state = state,
                    onSelect = viewModel::selectTask,
                )
                is MemoryTodayState.Writing -> WritingContent(
                    state = state,
                    onSave = viewModel::saveMemory,
                    isSaving = uiState.isSaving,
                )
                is MemoryTodayState.Saved -> SavedContent(state = state)
                is MemoryTodayState.Expired -> ExpiredContent(state = state)
            }
        }
    }
}

@Composable
private fun NoCompletionsContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "아직 완료한 퀘스트가 없어요",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "오늘 퀘스트를 완료하면\n기억을 남길 수 있어요!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SelectingContent(
    state: MemoryTodayState.Selecting,
    onSelect: (com.questlog.core.domain.model.TaskSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "오늘 완료한 퀘스트 중 기억에 남길 하나를 선택해주세요",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.candidates, key = { it.id }) { candidate ->
                CandidateCard(
                    taskSummary = candidate,
                    onClick = { onSelect(candidate) },
                )
            }
        }
    }
}

@Composable
private fun WritingContent(
    state: MemoryTodayState.Writing,
    onSave: (String) -> Unit,
    isSaving: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        MemoryEditor(
            selectedTitle = state.selected.title,
            onSave = onSave,
            isSaving = isSaving,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SavedContent(
    state: MemoryTodayState.Saved,
    modifier: Modifier = Modifier,
) {
    val entry = state.entry
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutcomeBadge(outcomeType = entry.outcomeType)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = entry.taskTitleSnapshot,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = entry.entryDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = entry.enrichedBody ?: entry.body,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (entry.enrichedBody != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "* DM 윤색본",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExpiredContent(
    state: MemoryTodayState.Expired,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Block,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "어제의 기억이 잠겼어요",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${state.date}의 기억은 이제 남길 수 없어요.\n오늘의 퀘스트를 완료하고 기억을 남겨보세요!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
