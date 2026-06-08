package com.questlog.feature.memory.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.questlog.core.domain.model.MemoryEntry
import com.questlog.feature.memory.history.components.MemoryHistoryCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MemoryHistoryScreen(
    onBack: () -> Unit,
    viewModel: MemoryHistoryViewModel = hiltViewModel(),
) {
    val lazyItems = viewModel.pagingData.collectAsLazyPagingItems()
    var selectedEntry by remember { mutableStateOf<MemoryEntry?>(null) }

    selectedEntry?.let { entry ->
        MemoryDetailDialog(
            entry = entry,
            onDismiss = { selectedEntry = null },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("기억 기록") })
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                lazyItems.loadState.refresh is LoadState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                lazyItems.itemCount == 0 && lazyItems.loadState.refresh is LoadState.NotLoading -> {
                    Text(
                        text = "아직 기억이 없어요.\n오늘의 기억을 남겨보세요!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // 월별 sticky header + 카드
                        var lastMonth = ""
                        for (i in 0 until lazyItems.itemCount) {
                            val entry = lazyItems.peek(i) ?: continue
                            val month = entry.entryDate.take(7) // "yyyy-MM"
                            if (month != lastMonth) {
                                lastMonth = month
                                val monthLabel = formatMonthLabel(month)
                                stickyHeader(key = "header_$month") {
                                    Text(
                                        text = monthLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                    )
                                }
                            }
                            item(key = lazyItems.itemKey { it.id }.invoke(i)) {
                                val item = lazyItems[i]
                                if (item != null) {
                                    MemoryHistoryCard(
                                        entry = item,
                                        onClick = { selectedEntry = item },
                                    )
                                }
                            }
                        }

                        if (lazyItems.loadState.append is LoadState.Loading) {
                            item {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .wrapContentWidth(Alignment.CenterHorizontally),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMonthLabel(yearMonth: String): String {
    // "yyyy-MM" → "yyyy년 M월"
    return runCatching {
        val parts = yearMonth.split("-")
        val year = parts[0]
        val month = parts[1].trimStart('0')
        "${year}년 ${month}월"
    }.getOrElse { yearMonth }
}
