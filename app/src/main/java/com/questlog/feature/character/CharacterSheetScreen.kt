package com.questlog.feature.character

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.questlog.core.domain.model.ClassContent
import com.questlog.feature.character.tabs.AbilitiesTab
import com.questlog.feature.character.tabs.AchievementsTab
import com.questlog.feature.character.tabs.EquipmentTab
import com.questlog.feature.character.tabs.StatsTab

private val TABS = listOf("스탯", "장비", "특수능력", "업적", "NPC")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSheetScreen(
    onLevelUp: () -> Unit,
    onSettings: () -> Unit = {},
    onNpcList: () -> Unit = {},
    viewModel: CharacterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // LevelUpScreen 으로 단방향 전환 — consumed 후 상태 초기화
    LaunchedEffect(state.levelUpEvent) {
        if (state.levelUpEvent) {
            viewModel.consumeLevelUpEvent()
            onLevelUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("캐릭터 시트") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "설정")
                    }
                },
            )
        },
    ) { padding ->
        val character = state.character
        if (character == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("캐릭터 생성이 필요합니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        val info = ClassContent.forClass(character.classType)
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 캐릭터 헤더
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(info.emoji, style = MaterialTheme.typography.displaySmall)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(character.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${info.classType.label} · Lv.${character.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TabRow(selectedTabIndex = selectedTab) {
                TABS.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = {
                            if (idx == 4) onNpcList() else selectedTab = idx
                        },
                        text = { Text(title) },
                    )
                }
            }
            when (selectedTab) {
                0 -> StatsTab(character = character, modifier = Modifier.fillMaxSize())
                1 -> EquipmentTab(
                    equippedItems = state.equippedItems,
                    inventory = state.inventory,
                    onEquip = { itemId, slot -> viewModel.equipItem(itemId, slot) },
                    onUnequip = { itemId -> viewModel.unequipItem(itemId) },
                    modifier = Modifier.fillMaxSize(),
                )
                2 -> AbilitiesTab(modifier = Modifier.fillMaxSize())
                3 -> AchievementsTab(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
