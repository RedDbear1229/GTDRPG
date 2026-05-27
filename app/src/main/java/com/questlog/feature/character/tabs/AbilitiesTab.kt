package com.questlog.feature.character.tabs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.ClassAbility
import com.questlog.core.domain.model.ClassAbilityDef

@Composable
fun AbilitiesTab(
    character: Character,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ability = ClassAbilityDef.forClass(character.classType)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Lv.1 특수 능력",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
        }
        item {
            AbilityCard(
                ability = ability,
                resourceCurrent = character.classResourceCurrent,
                resourceMax = character.classResourceMax.coerceAtLeast(ability.resourceMax),
                onActivate = onActivate,
            )
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "💤 Long Rest (자정) 시 ${ability.resourceName} 전체 충전",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AbilityCard(
    ability: ClassAbility,
    resourceCurrent: Int,
    resourceMax: Int,
    onActivate: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        ability.nameKo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        ability.nameEn,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ResourcePips(ability.resourceName, resourceCurrent, resourceMax)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                ability.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                ability.flavorText,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            val effectLabel = buildEffectLabel(ability)
            Text(
                effectLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(12.dp))

            val canActivate = resourceCurrent >= ability.cost
            Button(
                onClick = onActivate,
                enabled = canActivate,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val label = if (ability.isImmediate) "즉시 발동" else "다음 전투에 적용"
                Text("${ability.nameKo} 발동 (${ability.resourceName} ${ability.cost} 소모) — $label")
            }
        }
    }
}

@Composable
private fun ResourcePips(name: String, current: Int, max: Int) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(max) { idx ->
                val filled = idx < current
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape,
                        )
                )
            }
        }
        Text(
            "$current / $max",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun buildEffectLabel(ability: ClassAbility): String = when {
    ability.isImmediate -> when (ability.buffEffect.name) {
        "HP_RESTORE" -> "⚕ 즉시 HP +${ability.buffValue}% 회복"
        else -> "⚡ 즉시 효과 발동"
    }
    else -> when (ability.buffEffect.name) {
        "ATTACK_BONUS" -> "⚔ 다음 전투 공격 판정 +${ability.buffValue}"
        "XP_MULTIPLIER" -> "✨ 다음 전투 XP ×${"%.1f".format(ability.buffValue / 100f)}"
        "DAMAGE_REDUCE" -> "🛡 다음 전투 Miss 피해 ${100 - ability.buffValue}% 감소"
        "GUARANTEED_HIT" -> "🎯 다음 전투 무조건 성공"
        "CRIT_THRESHOLD" -> "💥 다음 전투 D20 ≥ ${ability.buffValue} 크리티컬"
        else -> "⚡ 다음 전투에 효과 적용"
    }
}
