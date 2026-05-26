package com.questlog.feature.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.usecase.AbilityCalculator
import com.questlog.core.domain.usecase.AbilityScores

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbilityRollStep(
    selectedClass: CharacterClass?,
    scores: AbilityScores?,
    onReroll: () -> Unit,
    onConfirm: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("능력치 굴림") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = "🎲",
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "4d6 최솟값 제거",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))
            if (scores != null) {
                AbilityType.entries.forEach { ability ->
                    val score = scores[ability]
                    val mod = AbilityCalculator.modifier(score)
                    val isPrimary = selectedClass?.primaryAbility == ability
                    AbilityRow(ability, score, mod, isPrimary)
                    Spacer(Modifier.height(8.dp))
                }
            } else {
                Text("굴림 중…")
            }
            Spacer(Modifier.weight(1f))
            if (selectedClass != null) {
                Text(
                    text = "💡 ${selectedClass.label}은 ${selectedClass.primaryAbility.name}이 높을수록 유리합니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = onReroll, modifier = Modifier.fillMaxWidth()) {
                Text("다시 굴리기")
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onConfirm,
                enabled = scores != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("이 능력치로 확정")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AbilityRow(
    ability: AbilityType,
    score: Int,
    modifier: Int,
    isPrimary: Boolean,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isPrimary) "★ ${ability.name}" else ability.name,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.width(60.dp),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(12.dp))
            val sign = if (modifier >= 0) "+" else ""
            Text(
                text = "($sign$modifier)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
