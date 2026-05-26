package com.questlog.feature.questboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.questlog.core.domain.model.LifeArea
import com.questlog.feature.questboard.QuestBoardFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    filters: QuestBoardFilters,
    onQueryChange: (String) -> Unit,
    onLifeAreaChange: (LifeArea?) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = filters.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("검색") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = if (
                filters.query.isNotBlank() || filters.lifeArea != null || filters.context != null
            ) {
                {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Clear, contentDescription = "필터 지우기")
                    }
                }
            } else null,
            singleLine = true,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LifeArea.values().forEach { area ->
                FilterChip(
                    selected = filters.lifeArea == area,
                    onClick = {
                        onLifeAreaChange(if (filters.lifeArea == area) null else area)
                    },
                    label = { Text("${area.icon} ${area.label}") },
                )
            }
        }
    }
}
