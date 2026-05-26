package com.questlog.feature.inbox.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.CaptureSource
import com.questlog.core.domain.model.InboxItem
import com.questlog.core.ui.theme.QuestLogTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxItemCard(
    item: InboxItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.rawText,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${sourceLabel(item.source)} · ${formatRelative(item.capturedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun sourceLabel(source: CaptureSource): String = when (source) {
    CaptureSource.APP -> "앱"
    CaptureSource.WIDGET -> "위젯"
    CaptureSource.SHARE -> "공유"
    CaptureSource.VOICE -> "음성"
    CaptureSource.NOTIFICATION -> "알림"
}

@Preview
@Composable
private fun InboxItemCardPreview() {
    QuestLogTheme {
        InboxItemCard(
            item = InboxItem(
                rawText = "친구한테 생일 선물 사기 — 다음 주 화요일까지",
                source = CaptureSource.APP,
                capturedAt = System.currentTimeMillis() - 3_600_000,
            ),
            onClick = {},
            onDelete = {},
        )
    }
}
