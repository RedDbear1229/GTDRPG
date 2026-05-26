package com.questlog.feature.questboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.Project
import com.questlog.core.domain.model.ProjectStatus
import com.questlog.core.ui.theme.QuestLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCard(
    project: Project,
    completedTasks: Int,
    totalTasks: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = project.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            project.desiredOutcome?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "$completedTasks / $totalTasks 완료",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun ProjectCardPreview() {
    QuestLogTheme {
        ProjectCard(
            project = Project(
                title = "이사 준비",
                desiredOutcome = "5월 말까지 새 집 입주 완료",
                status = ProjectStatus.ACTIVE,
                lifeArea = LifeArea.PERSONAL,
                challengeRating = 5f,
            ),
            completedTasks = 4,
            totalTasks = 10,
            onClick = {},
        )
    }
}
