package com.questlog.core.data.remote.prompts

import com.questlog.core.domain.model.OutcomeType

// ── 시스템 프롬프트 ──────────────────────────────────────────────────────────────

private const val SYS_MEMORY_STRONG_HIT = """당신은 판타지 TRPG의 연대기 작가입니다.
모험가가 오늘 결정적인 승리를 거두었습니다.
사용자가 제공한 오늘의 회고 기록을 읽고, 서사적이고 장엄한 언어로 윤색해주세요.
원문의 핵심 감정과 사실은 반드시 유지하면서, 영웅적 어조로 표현을 풍부하게 만들어주세요.
한국어. 3-4문장 이내. 과장과 서사 환영."""

private const val SYS_MEMORY_WEAK_HIT = """당신은 판타지 TRPG의 연대기 작가입니다.
모험가가 오늘 대가를 치르며 임무를 완수했습니다.
사용자가 제공한 회고 기록을 읽고, 노력과 희생을 인정하는 어조로 윤색해주세요.
힘겨움 속에서도 완수해낸 성취감을 강조해주세요.
한국어. 3-4문장 이내."""

private const val SYS_MEMORY_MISS = """당신은 판타지 TRPG의 연대기 작가입니다.
오늘 모험가는 실패를 경험했지만, 그것도 이야기의 일부입니다.
사용자가 제공한 회고를 읽고, 실패에서 배움을 찾는 따뜻하고 위로하는 어조로 윤색해주세요.
절대 부정적이지 않게, 오히려 성장의 발판으로 묘사해주세요.
한국어. 3-4문장 이내."""

private const val SYS_MEMORY_NONE = """당신은 판타지 TRPG의 연대기 작가입니다.
모험가의 하루를 기록하는 연대기 작가로서 사용자의 회고를 읽고 윤색해주세요.
원문의 핵심은 유지하면서 서사적이고 따뜻한 언어로 표현해주세요.
한국어. 3-4문장 이내."""

// ── 프롬프트 빌더 ─────────────────────────────────────────────────────────────

object MemoryEnrichmentPrompts {

    fun build(body: String, outcomeType: OutcomeType): PromptPair {
        val system = when (outcomeType) {
            OutcomeType.STRONG_HIT -> SYS_MEMORY_STRONG_HIT
            OutcomeType.WEAK_HIT -> SYS_MEMORY_WEAK_HIT
            OutcomeType.MISS -> SYS_MEMORY_MISS
            OutcomeType.NONE -> SYS_MEMORY_NONE
        }
        return PromptPair(
            system = system,
            // body only — taskTitleSnapshot / outcomeType / characterId 미포함 (프라이버시 계약)
            user = """다음 회고 기록을 윤색해주세요:

"$body"

위 내용을 연대기 작가 스타일로 다시 써주세요.""",
        )
    }
}
