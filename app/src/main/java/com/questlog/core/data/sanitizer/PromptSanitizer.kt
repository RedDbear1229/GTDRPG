package com.questlog.core.data.sanitizer

import javax.inject.Inject
import javax.inject.Singleton

data class SanitizedTask(
    val title: String,
    val context: String?,
    val createdAt: Long,
    val completedAt: Long?,
    val challengeRating: Float,
)

// 화이트리스트만 통과 — 메모/첨부/NPC 이름 등 개인 민감 필드는 전송 금지.
// 단위 테스트(PromptSanitizerTest) 누락 시 🔴 출시 금지 (CLAUDE.md 프라이버시 계약).
@Singleton
class PromptSanitizer @Inject constructor() {

    fun sanitizeTaskTitle(raw: String): String = raw.take(TITLE_MAX_LENGTH)

    fun sanitize(
        title: String,
        context: String?,
        createdAt: Long,
        completedAt: Long?,
        challengeRating: Float,
    ): SanitizedTask = SanitizedTask(
        title = title.take(TITLE_MAX_LENGTH),
        context = sanitizeContext(context),
        createdAt = createdAt,
        completedAt = completedAt,
        challengeRating = challengeRating,
    )

    // context는 쉼표 구분 태그 목록 (예: "@집,@사무실").
    // 화이트리스트에 없는 태그(NPC 이름 등 자유 입력)는 전부 제거.
    // 빈 결과는 null 반환 — 호출자가 null 체크로 "컨텍스트 없음" 처리.
    internal fun sanitizeContext(raw: String?): String? {
        raw ?: return null
        val safe = raw.split(",")
            .map { it.trim() }
            .filter { it in SAFE_GTD_CONTEXTS }
            .joinToString(",")
        return safe.ifEmpty { null }
    }

    companion object {
        const val TITLE_MAX_LENGTH = 50

        // GTD 장소/도구/상태 태그만 허용. @이름 형태의 NPC 멘션은 포함하지 않는다.
        // 새 컨텍스트 추가 시: PromptSanitizerTest 에 대응 케이스 추가 필수.
        val SAFE_GTD_CONTEXTS: Set<String> = setOf(
            // 장소
            "@집", "@home",
            "@사무실", "@직장", "@office",
            "@이동중", "@transit",
            "@외출", "@errand",
            // 도구
            "@컴퓨터", "@computer",
            "@전화", "@통화", "@phone",
            "@온라인", "@online",
            // 에너지
            "@고에너지", "@저에너지",
            // 시간대
            "@아침", "@낮", "@저녁", "@밤",
            "@주말", "@평일",
        )
    }
}
