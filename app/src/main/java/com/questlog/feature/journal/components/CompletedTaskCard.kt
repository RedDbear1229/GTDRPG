package com.questlog.feature.journal.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.MonsterType
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.TaskStatus
import com.questlog.core.ui.theme.QuestLogTheme
import com.questlog.feature.questboard.components.ContextChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CompletedTaskCard(
    task: Task,
    modifier: Modifier = Modifier,
) {
    val completedAt = task.completedAt
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                if (task.isQuickDone) {
                    ContextChip(text = "2분")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ContextChip(text = task.lifeArea.name.lowercase().replaceFirstChar { it.uppercase() })
                task.context?.split(",")?.filter { it.isNotBlank() }?.take(2)?.forEach {
                    ContextChip(text = it)
                }
            }
            if (completedAt != null) {
                Text(
                    text = "완료 ${formatCompletedAt(completedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatCompletedAt(epochMillis: Long): String =
    SimpleDateFormat("M월 d일 HH:mm", Locale.KOREAN).format(Date(epochMillis))

@Preview
@Composable
private fun CompletedTaskCardPreview() {
    QuestLogTheme {
        CompletedTaskCard(
            task = Task(
                title = "이메일 답장",
                status = TaskStatus.DONE,
                lifeArea = LifeArea.WORK,
                context = "@사무실",
                primaryAbility = AbilityType.INT,
                challengeRating = 1f,
                monsterType = MonsterType.GOBLIN,
                isQuickDone = true,
                completedAt = System.currentTimeMillis(),
            ),
        )
    }
}
