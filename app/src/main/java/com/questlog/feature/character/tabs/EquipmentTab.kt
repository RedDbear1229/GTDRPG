package com.questlog.feature.character.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.Item
import com.questlog.core.domain.model.ItemRarity

@Composable
fun EquipmentTab(
    equippedItems: List<Item>,
    inventory: List<Item>,
    onEquip: (itemId: String, slot: EquipmentSlot) -> Unit,
    onUnequip: (itemId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val equippedBySlot = equippedItems.associateBy { it.equippedSlot }
    val unequipped = inventory.filter { !it.isEquipped }

    LazyColumn(modifier = modifier, contentPadding = PaddingValues(16.dp)) {
        item {
            Text("장착 중", style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }
        item {
            EquippedSlotsGrid(equippedBySlot, onUnequip)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
        }
        item {
            Text("인벤토리 (${unequipped.size})",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
        }
        if (unequipped.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "아이템이 없습니다\n퀘스트 완료 시 드롭될 수 있어요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            items(unequipped, key = { it.id }) { item ->
                ItemCard(
                    item = item,
                    onAction = { onEquip(item.id, item.slot) },
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun EquippedSlotsGrid(
    equippedBySlot: Map<EquipmentSlot?, Item>,
    onUnequip: (itemId: String) -> Unit,
) {
    val slots = listOf(
        EquipmentSlot.WEAPON to "⚔️ 무기",
        EquipmentSlot.ARMOR to "🛡️ 방어구",
        EquipmentSlot.RING to "💍 반지",
        EquipmentSlot.NECKLACE to "📿 목걸이",
        EquipmentSlot.MISC to "🎒 기타",
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        slots.forEach { (slot, label) ->
            SlotRow(label = label, item = equippedBySlot[slot], onUnequip = onUnequip)
        }
    }
}

@Composable
private fun SlotRow(label: String, item: Item?, onUnequip: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(72.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (item != null) {
            RarityDot(item.rarity)
            Spacer(Modifier.width(8.dp))
            Text(item.name, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f))
            IconButton(onClick = { onUnequip(item.id) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = "해제",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Spacer(Modifier.weight(1f))
            Text("비어 있음", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun ItemCard(item: Item, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onAction),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RarityDot(item.rarity)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        item.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = rarityColor(item.rarity),
                    )
                }
                Text(item.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2)
                if (item.attackBonus != 0 || item.xpMultiplier != 1.0f || item.hpBonusFlat != 0) {
                    Spacer(Modifier.height(4.dp))
                    ItemStatLine(item)
                }
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Filled.CheckCircle, contentDescription = "장착",
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ItemStatLine(item: Item) {
    val stats = buildList {
        if (item.attackBonus != 0) add("ATK +${item.attackBonus}")
        if (item.xpMultiplier != 1.0f) add("XP ×${"%.2f".format(item.xpMultiplier)}")
        if (item.hpBonusFlat != 0) add("HP +${item.hpBonusFlat}")
    }
    Text(stats.joinToString(" · "), style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun RarityDot(rarity: ItemRarity) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(rarityColor(rarity), shape = CircleShape),
    )
}

private fun rarityColor(rarity: ItemRarity): Color =
    Color(android.graphics.Color.parseColor(rarity.colorHex))
