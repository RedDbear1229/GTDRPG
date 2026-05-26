package com.questlog.feature.character.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

// F4.4 아이템·NPC 이전까지 placeholder 탭. 빈 상태 텍스트만 표시.
@Composable
fun EquipmentTab(modifier: Modifier = Modifier) = PlaceholderTab("⚔️\n장비 (Phase 4)", modifier)

@Composable
fun AbilitiesTab(modifier: Modifier = Modifier) = PlaceholderTab("✨\n클래스 특수 능력 (Phase 3)", modifier)

@Composable
fun AchievementsTab(modifier: Modifier = Modifier) = PlaceholderTab("🏆\n업적 (Phase 5)", modifier)

@Composable
fun NpcTab(modifier: Modifier = Modifier) = PlaceholderTab("👥\nNPC (Phase 4)", modifier)

@Composable
private fun PlaceholderTab(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
