package com.questlog.feature.character.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.ClassContent
import com.questlog.core.ui.components.AbilityScoreCircle
import com.questlog.core.ui.components.HpBar
import com.questlog.core.ui.components.XpBar

@Composable
fun StatsTab(character: Character, modifier: Modifier = Modifier) {
    val info = ClassContent.forClass(character.classType)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HpBar(currentHp = character.currentHp, maxHp = character.maxHp,
            modifier = Modifier.fillMaxWidth())
        XpBar(level = character.level, currentXp = character.currentXp,
            modifier = Modifier.fillMaxWidth())

        HorizontalDivider()

        // 능력치
        Text("능력치", style = MaterialTheme.typography.titleSmall)
        val abilities = listOf(
            AbilityType.STR to character.strength,
            AbilityType.DEX to character.dexterity,
            AbilityType.CON to character.constitution,
            AbilityType.INT to character.intelligence,
            AbilityType.WIS to character.wisdom,
            AbilityType.CHA to character.charisma,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            abilities.take(3).forEach { (type, score) ->
                AbilityScoreCircle(
                    label = type.name,
                    score = score,
                    isPrimary = character.classType.primaryAbility == type,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            abilities.drop(3).forEach { (type, score) ->
                AbilityScoreCircle(
                    label = type.name,
                    score = score,
                    isPrimary = character.classType.primaryAbility == type,
                )
            }
        }

        HorizontalDivider()

        // 전투 스탯
        Text("전투 스탯", style = MaterialTheme.typography.titleSmall)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatRow("🛡️ AC", character.armorClass.toString())
                StatRow("⚡ 숙련 보너스", "+${character.proficiencyBonus}")
                StatRow("🔥 연속 완료", "${character.streakDays}일")
                StatRow("⚔️ 완료 퀘스트", "${character.totalQuestsCompleted}개")
                StatRow("🎯 크리티컬 히트", "${character.totalCriticalHits}회")
                StatRow("💀 크리티컬 미스", "${character.totalCriticalMisses}회")
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
