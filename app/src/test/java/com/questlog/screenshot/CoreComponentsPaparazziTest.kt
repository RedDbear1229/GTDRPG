package com.questlog.screenshot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.CaptureSource
import com.questlog.core.domain.model.InboxItem
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.MonsterType
import com.questlog.core.domain.model.OutcomeType
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.TaskStatus
import com.questlog.core.domain.model.TaskSummary
import com.questlog.core.ui.components.AbilityScoreCircle
import com.questlog.core.ui.components.HpBar
import com.questlog.core.ui.components.XpBar
import com.questlog.core.ui.theme.QuestLogTheme
import com.questlog.feature.clarify.components.ClarifyStep1Actionable
import com.questlog.feature.clarify.components.ClarifyStep3TwoMinute
import com.questlog.feature.inbox.components.InboxItemCard
import com.questlog.feature.journal.components.CompletedTaskCard
import com.questlog.feature.memory.today.components.CandidateCard
import com.questlog.feature.memory.today.components.OutcomeBadge
import com.questlog.feature.questboard.components.QuestCard
import org.junit.Rule
import org.junit.Test

class CoreComponentsPaparazziTest {

    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    // ─── HpBar ───────────────────────────────────────────────────────────────

    @Test
    fun `HpBar healthy`() = paparazzi.snapshot {
        QuestLogTheme { HpBar(currentHp = 90, maxHp = 100) }
    }

    @Test
    fun `HpBar tired`() = paparazzi.snapshot {
        QuestLogTheme { HpBar(currentHp = 55, maxHp = 100) }
    }

    @Test
    fun `HpBar wounded`() = paparazzi.snapshot {
        QuestLogTheme { HpBar(currentHp = 30, maxHp = 100) }
    }

    @Test
    fun `HpBar critical`() = paparazzi.snapshot {
        QuestLogTheme { HpBar(currentHp = 8, maxHp = 100) }
    }

    @Test
    fun `HpBar unconscious`() = paparazzi.snapshot {
        QuestLogTheme { HpBar(currentHp = 0, maxHp = 100) }
    }

    // ─── XpBar ───────────────────────────────────────────────────────────────

    @Test
    fun `XpBar mid level`() = paparazzi.snapshot {
        QuestLogTheme { XpBar(level = 5, currentXp = 300L) }
    }

    @Test
    fun `XpBar max level`() = paparazzi.snapshot {
        QuestLogTheme { XpBar(level = 20, currentXp = 0L) }
    }

    // ─── AbilityScoreCircle ──────────────────────────────────────────────────

    @Test
    fun `AbilityScoreCircle primary`() = paparazzi.snapshot {
        QuestLogTheme { AbilityScoreCircle(label = "INT", score = 16, isPrimary = true) }
    }

    @Test
    fun `AbilityScoreCircle secondary`() = paparazzi.snapshot {
        QuestLogTheme { AbilityScoreCircle(label = "STR", score = 10, isPrimary = false) }
    }

    // ─── InboxItemCard ───────────────────────────────────────────────────────

    @Test
    fun `InboxItemCard app source`() = paparazzi.snapshot {
        QuestLogTheme {
            InboxItemCard(
                item = InboxItem(
                    rawText = "친구한테 생일 선물 사기 — 다음 주 화요일까지",
                    source = CaptureSource.APP,
                    capturedAt = 0L,
                ),
                onClick = {},
                onDelete = {},
            )
        }
    }

    @Test
    fun `InboxItemCard long text`() = paparazzi.snapshot {
        QuestLogTheme {
            InboxItemCard(
                item = InboxItem(
                    rawText = "분기 보고서 초안 작성 — 마케팅 팀 데이터 수집 후 슬라이드 20장 분량으로 정리. " +
                        "팀장님 피드백 반영하여 최종본 금요일까지 제출.",
                    source = CaptureSource.SHARE,
                    capturedAt = 0L,
                ),
                onClick = {},
                onDelete = {},
            )
        }
    }

    // ─── QuestCard ───────────────────────────────────────────────────────────

    @Test
    fun `QuestCard without checkbox`() = paparazzi.snapshot {
        QuestLogTheme { QuestCard(task = sampleTask(), onClick = {}) }
    }

    @Test
    fun `QuestCard with checkbox`() = paparazzi.snapshot {
        QuestLogTheme { QuestCard(task = sampleTask(), onClick = {}, onComplete = {}) }
    }

    @Test
    fun `QuestCard high CR`() = paparazzi.snapshot {
        QuestLogTheme {
            QuestCard(
                task = sampleTask().copy(
                    title = "연간 사업 계획서 전략 수립",
                    challengeRating = 18f,
                    monsterType = MonsterType.DRAGON,
                    lifeArea = LifeArea.WORK,
                    dueDate = System.currentTimeMillis() + 86_400_000L,
                ),
                onClick = {},
                onComplete = {},
            )
        }
    }

    // ─── CompletedTaskCard ───────────────────────────────────────────────────

    @Test
    fun `CompletedTaskCard normal`() = paparazzi.snapshot {
        QuestLogTheme {
            CompletedTaskCard(
                task = sampleTask().copy(
                    status = TaskStatus.DONE,
                    completedAt = System.currentTimeMillis() - 3_600_000L,
                ),
            )
        }
    }

    @Test
    fun `CompletedTaskCard quick done`() = paparazzi.snapshot {
        QuestLogTheme {
            CompletedTaskCard(
                task = sampleTask().copy(
                    title = "이메일 답장",
                    status = TaskStatus.DONE,
                    isQuickDone = true,
                    completedAt = System.currentTimeMillis() - 1_800_000L,
                ),
            )
        }
    }

    // ─── OutcomeBadge ────────────────────────────────────────────────────────

    @Test
    fun `OutcomeBadge all states`() = paparazzi.snapshot {
        QuestLogTheme {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutcomeBadge(outcomeType = OutcomeType.STRONG_HIT)
                OutcomeBadge(outcomeType = OutcomeType.WEAK_HIT)
                OutcomeBadge(outcomeType = OutcomeType.MISS)
                OutcomeBadge(outcomeType = OutcomeType.NONE)
            }
        }
    }

    // ─── CandidateCard ───────────────────────────────────────────────────────

    @Test
    fun `CandidateCard strong hit`() = paparazzi.snapshot {
        QuestLogTheme {
            CandidateCard(
                taskSummary = TaskSummary(
                    id = "1",
                    title = "보고서 완성",
                    outcomeType = OutcomeType.STRONG_HIT,
                    xpGained = 150L,
                ),
                onClick = {},
            )
        }
    }

    @Test
    fun `CandidateCard miss`() = paparazzi.snapshot {
        QuestLogTheme {
            CandidateCard(
                taskSummary = TaskSummary(
                    id = "2",
                    title = "헬스장 방문",
                    outcomeType = OutcomeType.MISS,
                    xpGained = 0L,
                ),
                onClick = {},
            )
        }
    }

    // ─── Clarify Steps ───────────────────────────────────────────────────────

    @Test
    fun `ClarifyStep1Actionable`() = paparazzi.snapshot {
        QuestLogTheme {
            ClarifyStep1Actionable(
                rawText = "친구한테 생일 선물 사기",
                onActionable = {},
                onNonActionable = {},
            )
        }
    }

    @Test
    fun `ClarifyStep3TwoMinute`() = paparazzi.snapshot {
        QuestLogTheme { ClarifyStep3TwoMinute(onYes = {}, onNo = {}) }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun sampleTask() = Task(
        id = "task-1",
        title = "월간 보고서 마무리",
        status = TaskStatus.ACTIVE,
        lifeArea = LifeArea.WORK,
        context = "@사무실",
        primaryAbility = AbilityType.INT,
        challengeRating = 7.5f,
        monsterType = MonsterType.OGRE,
        estimatedMinutes = 45,
    )
}
