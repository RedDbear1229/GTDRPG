package com.questlog.feature.encounter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.core.domain.model.EncounterLog
import com.questlog.core.domain.model.EncounterStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncounterLogScreen(
    onBack: () -> Unit,
    viewModel: EncounterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var claimTarget by remember { mutableStateOf<EncounterLog?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EncounterEvent.Claimed ->
                    snackbarHostState.showSnackbar("+${event.xpGained} XP 획득! (Lv ${event.newLevel})")
                is EncounterEvent.Error ->
                    snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("인카운터 로그") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (uiState.pending.isNotEmpty()) {
                item {
                    Text(
                        text = "대기 중 (${uiState.pending.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                items(uiState.pending, key = { it.id }) { encounter ->
                    EncounterCard(
                        encounter = encounter,
                        onClaim = { claimTarget = encounter },
                    )
                }
            }

            if (uiState.history.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "기록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                }
                items(uiState.history, key = { it.id }) { encounter ->
                    EncounterCard(encounter = encounter, onClaim = null)
                }
            }

            if (uiState.pending.isEmpty() && uiState.history.isEmpty() && !uiState.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                        Text("아직 인카운터가 없습니다.\n12시간마다 새로운 모험이 기다립니다!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }

    claimTarget?.let { encounter ->
        AlertDialog(
            onDismissRequest = { claimTarget = null },
            title = { Text(encounter.title) },
            text = {
                Column {
                    Text(encounter.description)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "\"${encounter.flavorText}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "보상: +${encounter.rewardXp} XP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.claimReward(encounter.id)
                    claimTarget = null
                }) { Text("보상 수령") }
            },
            dismissButton = {
                TextButton(onClick = { claimTarget = null }) { Text("나중에") }
            },
        )
    }
}

@Composable
private fun EncounterCard(
    encounter: EncounterLog,
    onClaim: (() -> Unit)?,
) {
    val borderColor = when (encounter.status) {
        EncounterStatus.PENDING -> MaterialTheme.colorScheme.primary
        EncounterStatus.CLAIMED -> Color(0xFF4CAF50)
        EncounterStatus.EXPIRED -> MaterialTheme.colorScheme.outline
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(encounter.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = statusLabel(encounter.status),
                        style = MaterialTheme.typography.labelSmall,
                        color = borderColor,
                    )
                }
                if (onClaim != null) {
                    Button(onClick = onClaim) { Text("+${encounter.rewardXp} XP") }
                } else {
                    Text(
                        text = if (encounter.status == EncounterStatus.CLAIMED) "+${encounter.rewardXp} XP" else "만료",
                        style = MaterialTheme.typography.labelMedium,
                        color = borderColor,
                    )
                }
            }
            if (encounter.status == EncounterStatus.PENDING) {
                Spacer(Modifier.height(4.dp))
                val expiresIn = encounter.expiresAt - System.currentTimeMillis()
                Text(
                    text = "만료까지 ${formatDuration(expiresIn)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun statusLabel(status: EncounterStatus) = when (status) {
    EncounterStatus.PENDING -> "대기 중"
    EncounterStatus.CLAIMED -> "보상 수령 완료"
    EncounterStatus.EXPIRED -> "만료됨"
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0) return "곧 만료"
    val hours = ms / 3_600_000
    val minutes = (ms % 3_600_000) / 60_000
    return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
}
