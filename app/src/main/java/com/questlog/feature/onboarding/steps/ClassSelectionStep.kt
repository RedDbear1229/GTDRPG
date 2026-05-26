package com.questlog.feature.onboarding.steps

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClassContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassSelectionStep(
    recommendations: List<CharacterClass>,
    selectedClass: CharacterClass?,
    onClassSelected: (CharacterClass) -> Unit,
    onConfirm: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("클래스 선택") }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (recommendations.isNotEmpty()) {
                Text(
                    text = "추천 클래스",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(ClassContent.all) { info ->
                    val isSelected = selectedClass == info.classType
                    val isRecommended = info.classType in recommendations
                    Card(
                        modifier = Modifier
                            .clickable { onClassSelected(info.classType) }
                            .then(
                                if (isSelected) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(12.dp),
                                ) else Modifier
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = if (isRecommended) "⭐ ${info.emoji}" else info.emoji,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = info.classType.label,
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = info.tagline,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
            Button(
                onClick = onConfirm,
                enabled = selectedClass != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                val label = selectedClass?.let { "${ClassContent.forClass(it).emoji} ${it.label} 선택" }
                    ?: "클래스를 선택해 주세요"
                Text(label)
            }
        }
    }
}
