package com.questlog.feature.combat

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.core.domain.model.CombatResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun D20RollSheet(
    taskId: String,
    onDismiss: () -> Unit,
    viewModel: CombatViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // 시트가 열리면 즉시 주사위 굴림
    LaunchedEffect(taskId) { viewModel.rollDice(taskId) }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.dismiss()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                state.alreadyCompleted -> AlreadyCompletedContent()
                state.error != null -> ErrorContent(state.error!!)
                state.isRolling -> RollingContent()
                state.result != null -> ResultContent(state.result!!)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.dismiss(); onDismiss() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isRolling,
            ) {
                Text(if (state.isRolling) "굴리는 중..." else "확인")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RollingContent() {
    D20Die(number = "?", color = MaterialTheme.colorScheme.primary)
    Text("D20 굴리는 중...", style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ResultContent(result: CombatResult) {
    when (result) {
        is CombatResult.CriticalHit -> {
            D20Die(number = "20", color = Color(0xFFFFD700))
            Text("⚡ 크리티컬 히트!", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
            Text(result.narrative, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            XpChip(xp = result.xpGained)
        }
        is CombatResult.Hit -> {
            D20Die(number = result.d20Result.toString(), color = MaterialTheme.colorScheme.primary)
            Text("✅ 명중!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            AttackRow(attack = result.totalAttack, ac = result.monsterAC)
            XpChip(xp = result.xpGained)
        }
        is CombatResult.Miss -> {
            D20Die(number = result.d20Result.toString(), color = MaterialTheme.colorScheme.error)
            Text("❌ 빗나감", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
            AttackRow(attack = result.totalAttack, ac = result.monsterAC)
            if (result.hpLost > 0) Text("HP -${result.hpLost}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        }
        is CombatResult.CriticalMiss -> {
            D20Die(number = "1", color = MaterialTheme.colorScheme.error)
            Text("💀 크리티컬 미스!", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            Text(result.humorousMessage, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            if (result.hpLost > 0) Text("HP -${result.hpLost}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun AlreadyCompletedContent() {
    Text("⚔️", style = MaterialTheme.typography.displayMedium)
    Text("이미 완료된 퀘스트입니다", style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ErrorContent(message: String) {
    Text("⚠️", style = MaterialTheme.typography.displayMedium)
    Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
}

@Composable
private fun D20Die(number: String, color: Color) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(number, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun AttackRow(attack: Int, ac: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("공격 굴림: $attack", style = MaterialTheme.typography.bodyMedium)
        Text("vs AC $ac", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun XpChip(xp: Long) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text("+${xp} XP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
