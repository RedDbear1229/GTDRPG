package com.questlog.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.usecase.XpThresholds

@Composable
fun XpBar(
    level: Int,
    currentXp: Long,
    modifier: Modifier = Modifier,
) {
    val xpToNext = XpThresholds.xpToNextLevel(XpThresholds.cumulativeForLevel(level) + currentXp)
    val xpNeeded = if (level < XpThresholds.MAX_LEVEL)
        XpThresholds.cumulativeForLevel(level + 1) - XpThresholds.cumulativeForLevel(level)
    else 1L
    val fraction = if (xpToNext == null) 1f
    else 1f - (xpToNext.toFloat() / xpNeeded.toFloat())
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "xp_anim",
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("✨ XP  Lv.$level", style = MaterialTheme.typography.labelMedium)
            if (xpToNext != null) {
                Text(
                    "다음 레벨까지 $xpToNext XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text("MAX LEVEL", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
        }
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
    }
}
