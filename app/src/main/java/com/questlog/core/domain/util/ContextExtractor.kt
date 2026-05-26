package com.questlog.core.domain.util

// rawText 에서 `@태그` 패턴 추출 — 한글/영문/숫자/언더스코어 허용.
// docs/03_gtd_system.md §3.3 "맥락 태그" 시스템.
object ContextExtractor {

    private val TAG_REGEX = Regex("@([\\p{L}\\p{N}_]+)")

    fun extract(rawText: String): List<String> =
        TAG_REGEX.findAll(rawText)
            .map { it.value }
            .distinct()
            .toList()

    fun joinForStorage(tags: List<String>): String? =
        tags.takeIf { it.isNotEmpty() }?.joinToString(",")
}
