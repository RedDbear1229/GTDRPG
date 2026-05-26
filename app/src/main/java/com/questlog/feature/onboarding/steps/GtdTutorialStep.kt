package com.questlog.feature.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class TutorialPage(
    val emoji: String,
    val title: String,
    val body: String,
)

private val PAGES = listOf(
    TutorialPage(
        emoji = "📥",
        title = "1. CAPTURE — 포착",
        body = "머릿속의 모든 것을 즉시 Inbox에 던져 넣으세요.\n생각, 아이디어, 할 일 — 판단 없이 일단 수집합니다.",
    ),
    TutorialPage(
        emoji = "🔍",
        title = "2. CLARIFY — 명료화",
        body = "Inbox의 항목마다 \"행동 가능한가?\"를 물어보세요.\n2분 이내라면 즉시 완료, 아니면 퀘스트로 만듭니다.",
    ),
    TutorialPage(
        emoji = "🗂️",
        title = "3. ORGANIZE — 정리",
        body = "퀘스트를 프로젝트, 컨텍스트, 마감일로 분류하세요.\nQuestBoard가 자동으로 우선순위를 계산합니다.",
    ),
    TutorialPage(
        emoji = "📖",
        title = "4. REFLECT — 검토",
        body = "매일 Journal에서 오늘의 완료 퀘스트를 확인하세요.\n주간 리뷰로 목표와 현실을 정렬합니다.",
    ),
    TutorialPage(
        emoji = "⚔️",
        title = "5. ENGAGE — 실행",
        body = "컨텍스트와 에너지에 맞는 퀘스트를 선택하세요.\n완료하면 D20 전투가 시작됩니다. XP를 모아 레벨업!",
    ),
)

@Composable
fun GtdTutorialStep(
    characterName: String,
    onComplete: () -> Unit,
) {
    val pagerState = rememberPagerState { PAGES.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == PAGES.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            val p = PAGES[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(p.emoji, style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(24.dp))
                Text(
                    text = p.title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = p.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        // 페이지 인디케이터
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(PAGES.size) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == pagerState.currentPage) 10.dp else 6.dp)
                        .then(
                            Modifier.padding(2.dp)
                        ),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        if (isLast) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            ) {
                val greeting = if (characterName.isBlank()) "모험을" else "\"$characterName\"의 모험을"
                Text("$greeting 시작!")
            }
        } else {
            OutlinedButton(
                onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
            ) {
                Text("다음")
            }
        }
    }
}
