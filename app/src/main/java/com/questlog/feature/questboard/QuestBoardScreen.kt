package com.questlog.feature.questboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.core.domain.model.Task
import com.questlog.feature.questboard.components.FilterBar
import com.questlog.feature.questboard.components.ProjectCard
import com.questlog.feature.questboard.components.QuestCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestBoardScreen(
    onTaskClick: (String) -> Unit,
    onProjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: QuestBoardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(title = { Text("퀘스트 보드") })
                TabRow(selectedTabIndex = state.tab.ordinal) {
                    QuestBoardTab.values().forEach { tab ->
                        Tab(
                            selected = state.tab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            text = { Text(tabLabel(tab)) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.tab != QuestBoardTab.PROJECTS) {
                FilterBar(
                    filters = state.filters,
                    onQueryChange = viewModel::setQuery,
                    onLifeAreaChange = viewModel::setLifeArea,
                    onClear = viewModel::clearFilters,
                )
            }
            when (state.tab) {
                QuestBoardTab.TODAY -> TaskList(state.today, onTaskClick)
                QuestBoardTab.ACTIVE -> TaskList(state.active, onTaskClick)
                QuestBoardTab.PROJECTS -> ProjectList(state.projects, onProjectClick)
            }
        }
    }
}

@Composable
private fun TaskList(tasks: List<Task>, onClick: (String) -> Unit) {
    if (tasks.isEmpty()) {
        EmptyState(message = "해당하는 퀘스트가 없습니다")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(tasks, key = { it.id }) { task ->
                QuestCard(task = task, onClick = { onClick(task.id) })
            }
        }
    }
}

@Composable
private fun ProjectList(summaries: List<ProjectSummary>, onClick: (String) -> Unit) {
    if (summaries.isEmpty()) {
        EmptyState(message = "활성 프로젝트가 없습니다")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(summaries, key = { it.project.id }) { summary ->
                ProjectCard(
                    project = summary.project,
                    completedTasks = summary.completedCount,
                    totalTasks = summary.taskCount,
                    onClick = { onClick(summary.project.id) },
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun tabLabel(tab: QuestBoardTab): String = when (tab) {
    QuestBoardTab.TODAY -> "오늘"
    QuestBoardTab.ACTIVE -> "전체"
    QuestBoardTab.PROJECTS -> "프로젝트"
}
