package com.questlog.feature.questboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestCard(
    task: Task,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(crColor(task.challengeRating))
                    .height(64.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    CrBadge(cr = task.challengeRating)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    task.context?.split(",")?.filter { it.isNotBlank() }?.take(2)?.forEach {
                        ContextChip(text = it)
                    }
                    task.estimatedMinutes?.let {
                        ContextChip(text = "${it}분")
                    }
                }
                task.dueDate?.let { due ->
                    Text(
                        text = "마감 ${formatDue(due)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun formatDue(epochMillis: Long): String =
    SimpleDateFormat("M월 d일", Locale.KOREAN).format(Date(epochMillis))

@Preview
@Composable
private fun QuestCardPreview() {
    QuestLogTheme {
        QuestCard(
            task = Task(
                title = "월간 보고서 마무리",
                status = TaskStatus.ACTIVE,
                lifeArea = LifeArea.WORK,
                context = "@사무실",
                primaryAbility = AbilityType.INT,
                challengeRating = 7.5f,
                monsterType = MonsterType.OGRE,
                estimatedMinutes = 45,
                dueDate = System.currentTimeMillis() + 86_400_000L,
            ),
            onClick = {},
        )
    }
}
