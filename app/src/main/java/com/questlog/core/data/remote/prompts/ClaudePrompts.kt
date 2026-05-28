package com.questlog.core.data.remote.prompts

import com.questlog.core.data.sanitizer.SanitizedTask
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatResult

// ── 시스템 프롬프트 (캐시 대상 — 변경 금지, 변경 시 캐시 미스) ──────────────────

private const val SYS_COMBAT = """당신은 판타지 TRPG의 던전마스터입니다.
플레이어의 실생활 할 일 완료를 판타지 전투 승리 장면으로 묘사해주세요.
한국어로 2-3문장. 유머러스하고 서사적으로. 과장 환영."""

private const val SYS_CRITICAL_HIT = """판타지 TRPG 던전마스터. 전설적 크리티컬 히트 장면 묘사.
감탄사와 과장 필수. 한국어 3-4문장."""

private const val SYS_CRITICAL_MISS = """DM이 크리티컬 미스한 모험가를 유머러스하게 위로.
절대 슬프거나 부정적이지 않게.
실패가 이야기의 일부임을 강조. 한국어. 2문장."""

private const val SYS_WEEKLY_REVIEW = """판타지 TRPG의 현명한 던전마스터.
영웅의 주간 활동을 서사적으로 요약하고 다음 주 조언을 제공.
한국어. DM의 목소리로 3인칭 또는 2인칭 혼용."""

private const val SYS_CLARIFY = """GTD(Getting Things Done) 전문가.
입력된 항목에서 구체적이고 즉시 실행 가능한 "다음 물리적 행동"을 제안.
동사로 시작하는 짧은 문장 2-3개. 한국어."""

private const val SYS_LEVEL_UP = """판타지 세계의 현인. 새로운 레벨에 오른 모험가를 축하.
해당 레벨의 의미와 새로운 능력에 대한 서사적 설명.
한국어. 감동적이고 서사적. 3-4문장."""

private const val SYS_ENCOUNTER = """GTD-RPG 게임 콘텐츠 생성자.
오늘의 랜덤 인카운터 이벤트 1개를 생성.
생산성/일상과 연관된 판타지 이벤트. 유머러스하게.
한국어. 제목(10자 이내) + 설명(2문장) 형식."""

// ── 프롬프트 빌더 ─────────────────────────────────────────────────────────────

object ClaudePrompts {

    fun combatNarrative(
        character: Character,
        task: SanitizedTask,
        result: CombatResult.Hit,
    ) = PromptPair(
        system = SYS_COMBAT,
        user = """캐릭터: ${character.name} (Lv.${character.level} ${character.classType.label})
완료한 퀘스트: "${task.title}"
D20 결과: ${result.d20Result}
연속 완료: ${character.streakDays}일째

이 승리 장면을 2-3문장으로 묘사해주세요.""",
    )

    fun criticalHit(
        character: Character,
        task: SanitizedTask,
        xpGained: Long,
        itemName: String?,
    ) = PromptPair(
        system = SYS_CRITICAL_HIT,
        user = """캐릭터: ${character.name} (Lv.${character.level} ${character.classType.label})
퀘스트: "${task.title}"
획득 XP: $xpGained (2배!)
${if (itemName != null) "획득 아이템: $itemName" else ""}

D20 = 20, 전설적 크리티컬 히트 장면을 묘사해주세요!""",
    )

    fun criticalMiss(
        character: Character,
        task: SanitizedTask,
        hpLost: Int,
    ) = PromptPair(
        system = SYS_CRITICAL_MISS,
        user = """퀘스트: "${task.title}"
D20: 1
HP 손실: $hpLost
캐릭터: ${character.name}

크리티컬 미스 위로 메시지.""",
    )

    fun weeklyReview(
        character: Character,
        completedCount: Int,
        xpGained: Long,
        critCount: Int,
        missCount: Int,
        unfinished: Int,
        weekLabel: String,
    ) = PromptPair(
        system = SYS_WEEKLY_REVIEW,
        user = """캐릭터: ${character.name} Lv.${character.level} ${character.classType.label}
이번 주 ($weekLabel):
- 완료한 퀘스트: ${completedCount}개
- 획득 XP: $xpGained
- 연속 완료: ${character.streakDays}일
- 크리티컬 히트: ${critCount}회, 미스: ${missCount}회
- 미완료 퀘스트: ${unfinished}개

이번 주 영웅 보고서를 써주세요.
다음 주에 집중해야 할 조언도 2가지 포함해주세요.
총 4-5문장.""",
    )

    fun clarify(
        rawText: String,
        character: Character,
    ) = PromptPair(
        system = SYS_CLARIFY,
        user = """수집된 항목: "${rawText.take(50)}"
캐릭터 클래스: ${character.classType.label}

이 항목의 다음 물리적 행동 3개를 제안해주세요.
각각 다른 접근 방식으로.""",
    )

    fun levelUp(
        character: Character,
        newLevel: Int,
        totalQuests: Int,
    ) = PromptPair(
        system = SYS_LEVEL_UP,
        user = """캐릭터: ${character.name} ${character.classType.label}
달성 레벨: $newLevel
총 완료 퀘스트: ${totalQuests}개

레벨업 축하 메시지를 써주세요.""",
    )

    fun encounter(
        character: Character,
        todayCompleted: Int,
    ) = PromptPair(
        system = SYS_ENCOUNTER,
        user = """캐릭터: ${character.name} Lv.${character.level} ${character.classType.label}
오늘 완료 퀘스트: ${todayCompleted}개

오늘의 랜덤 인카운터를 생성해주세요.
형식: 제목(10자 이내) + 설명(2문장)""",
    )
}

data class PromptPair(val system: String, val user: String)

// ── 오프라인/API 미설정 폴백 ──────────────────────────────────────────────────

object LocalNarratives {
    val COMBAT_HIT = listOf(
        "{name}이(가) 집중력을 발휘하여 '{taskTitle}'을(를) 완료했습니다! +{xp} XP 획득!",
        "오늘도 빛나는 활약! {name}이(가) 어둠의 마감을 쓰러뜨렸습니다.",
        "'{taskTitle}' 처리 완료! +{xp} XP 획득.",
    )
    val CRITICAL_HIT = listOf(
        "🔥 전설적인 일격!! {name}이(가) 완벽하게 처리했습니다! XP 2배!",
        "D20이 빛납니다! '{taskTitle}' 크리티컬 히트! +{xp} XP!!",
        "역대 최고의 퍼포먼스! {name}의 능력이 폭발했습니다!",
    )
    val CRITICAL_MISS = listOf(
        "오늘은 주사위가 심술궂었네요. 하지만 내일은 달라요! 😅",
        "크리티컬 미스! 이것도 이야기의 일부입니다. HP를 회복하고 다시!",
        "D20=1... 전설적인 모험가도 가끔 넘어지죠. 일어나세요!",
    )
    val WEEKLY_REVIEW = listOf(
        "이번 주도 수고했습니다, {name}! {completedCount}개 퀘스트 완료!",
        "주간 리뷰 완료! {name}의 모험 일지가 업데이트되었습니다.",
        "{xp} XP 획득! 다음 주도 화이팅, {name}!",
    )
    val LEVEL_UP = listOf(
        "레벨 {level} 달성! {name}이(가) 강해졌습니다!",
        "축하합니다! Lv.{level} {className} {name}! 새 특성이 해금되었습니다.",
        "{totalQuests}개의 퀘스트가 이 순간을 만들었습니다. Lv.{level} 달성!",
    )
    val ENCOUNTER = listOf(
        "보물 상자 발견!\n오래된 서랍에서 보물이! 오늘도 열심히 하셨군요.",
        "방랑 상인 출현!\n수상한 망토를 걸친 상인이 아이템을 건넨다.",
        "집중력 포션 발견!\n오늘 하루도 끝까지 버텨낸 모험가에게 주어지는 선물!",
    )

    fun fill(template: String, vararg pairs: Pair<String, String>): String {
        var result = template
        pairs.forEach { (k, v) -> result = result.replace("{$k}", v) }
        return result
    }
}
