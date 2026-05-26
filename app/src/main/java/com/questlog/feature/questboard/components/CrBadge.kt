package com.questlog.feature.questboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.questlog.core.ui.theme.CrEasy
import com.questlog.core.ui.theme.CrEpic
import com.questlog.core.ui.theme.CrHard
import com.questlog.core.ui.theme.CrLegendary
import com.questlog.core.ui.theme.CrNormal
import com.questlog.core.ui.theme.CrVeryHard
import com.questlog.core.ui.theme.QuestLogTheme

@Composable
fun CrBadge(
    cr: Float,
    modifier: Modifier = Modifier,
) {
    val color = crColor(cr)
    Text(
        text = "CR ${formatCr(cr)}",
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .background(color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

internal fun crColor(cr: Float): Color = when {
    cr < 2f -> CrEasy
    cr < 5f -> CrNormal
    cr < 10f -> CrHard
    cr < 15f -> CrVeryHard
    cr < 20f -> CrEpic
    else -> CrLegendary
}

private fun formatCr(cr: Float): String =
    if (cr == cr.toInt().toFloat()) cr.toInt().toString() else String.format("%.1f", cr)

@Preview
@Composable
private fun CrBadgePreview() {
    QuestLogTheme {
        CrBadge(cr = 3.5f)
    }
}
