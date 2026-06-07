package com.questlog.feature.memory.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.OutcomeType

@Composable
fun OutcomeBadge(outcomeType: OutcomeType, modifier: Modifier = Modifier) {
    val (label, color) = when (outcomeType) {
        OutcomeType.STRONG_HIT -> "크리티컬!" to Color(0xFFFFD700)
        OutcomeType.WEAK_HIT -> "성공" to Color(0xFF4CAF50)
        OutcomeType.MISS -> "크리티컬 미스" to Color(0xFFF44336)
        OutcomeType.NONE -> "완료" to Color(0xFF9E9E9E)
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
