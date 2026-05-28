package com.questlog.feature.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.feature.clarify.ClarifySheet
import com.questlog.feature.inbox.components.InboxItemCard
import com.questlog.feature.inbox.components.QuickCaptureSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    modifier: Modifier = Modifier,
    viewModel: InboxViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var clarifyTarget by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.error) {
        state.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Inbox (${state.items.size})") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::openSheet) {
                Icon(Icons.Filled.Add, contentDescription = "캡처")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.items.isEmpty()) {
            EmptyInbox(modifier = Modifier.padding(padding))
        } else {
            InboxList(
                items = state.items.map { it.id to it },
                onClick = { clarifyTarget = it },
                onDelete = viewModel::delete,
                contentPadding = padding,
            )
        }
    }

    if (state.isSheetVisible) {
        QuickCaptureSheet(
            onDismiss = viewModel::dismissSheet,
            onSubmit = viewModel::captureFromSheet,
            onVoiceCapture = viewModel::captureFromVoice,
            isSubmitting = state.isCapturing,
            microphoneConsented = state.microphoneConsented,
        )
    }

    clarifyTarget?.let { inboxId ->
        ClarifySheet(
            inboxId = inboxId,
            onDismiss = { clarifyTarget = null },
        )
    }
}

@Composable
private fun EmptyInbox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "Inbox가 비어 있어요",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "+ 버튼으로 떠오른 것을 즉시 캡처하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InboxList(
    items: List<Pair<String, com.questlog.core.domain.model.InboxItem>>,
    onClick: (String) -> Unit,
    onDelete: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items, key = { it.first }) { (_, item) ->
            InboxItemCard(
                item = item,
                onClick = { onClick(item.id) },
                onDelete = { onDelete(item.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
