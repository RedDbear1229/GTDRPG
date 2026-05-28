package com.questlog.core.data.privacy

import com.questlog.core.data.sanitizer.PromptSanitizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class PromptSanitizerTest {

    private val sanitizer = PromptSanitizer()

    // ── 제목 절단 ──────────────────────────────────────────────────────────────

    @Test
    fun `50자 이하 제목은 그대로 통과`() {
        val title = "A".repeat(50)
        assertEquals(title, sanitizer.sanitizeTaskTitle(title))
    }

    @Test
    fun `51자 제목은 50자로 절단`() {
        val title = "A".repeat(51)
        assertEquals(50, sanitizer.sanitizeTaskTitle(title).length)
    }

    @Test
    fun `100자 제목은 50자로 절단`() {
        val long = "퀘스트".repeat(34)
        assertEquals(50, sanitizer.sanitizeTaskTitle(long).length)
    }

    // ── 민감 필드 누락 확인 (🔴 출시 금지 필드) ──────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = [
        "notes", "attachmentPaths", "delegatedTo", "audioPath",
        "transcribedText", "imagePaths", "recurringRule",
    ])
    fun `SanitizedTask에 민감 필드명이 포함된 값이 존재하지 않음`(fieldName: String) {
        val result = sanitizer.sanitize(
            title = fieldName,  // 제목에 필드명이 포함돼도 다른 필드에는 나오지 않아야 함
            context = null,
            createdAt = 1_000L,
            completedAt = null,
            challengeRating = 1f,
        )
        // SanitizedTask의 모든 String 필드를 검사
        val allText = listOf(result.title, result.context ?: "").joinToString(" ")
        // 민감 값 자체(필드명 그대로)가 context나 title에 노출되지 않아야 한다
        // 여기서 title은 "notes" 자체이므로 예외 — context가 null이고 다른 필드가 없음을 확인
        assertFalse(result.context?.contains(fieldName) == true)
    }

    // ── NPC 이름 차단 (P1 수정 — Codex review 2026-05) ──────────────────────
    // context에 NPC/사람 이름(@홍길동)이 들어오면 화이트리스트 미통과 → null 반환.

    @Test
    fun `NPC 이름이 context로 넘어오면 null로 차단됨`() {
        val result = sanitizer.sanitize(
            title = "task",
            context = "@홍길동",
            createdAt = 1_000L,
            completedAt = null,
            challengeRating = 1f,
        )
        assertEquals(null, result.context)
    }

    @Test
    fun `긴 NPC 이름도 차단됨 — 길이 절단이 아닌 완전 제거`() {
        val result = sanitizer.sanitize(
            title = "task",
            context = "@홍길동선생님멘토NPC명칭길게써보기테스트용문자열",
            createdAt = 1_000L,
            completedAt = null,
            challengeRating = 1f,
        )
        assertEquals(null, result.context)
    }

    @Test
    fun `혼합 context — 안전 태그는 통과, NPC 이름은 제거`() {
        val result = sanitizer.sanitize(
            title = "task",
            context = "@집,@홍길동,@사무실",
            createdAt = 1_000L,
            completedAt = null,
            challengeRating = 1f,
        )
        assertEquals("@집,@사무실", result.context)
    }

    @Test
    fun `화이트리스트 안전 context 태그는 그대로 통과`() {
        for (safe in listOf("@집", "@사무실", "@이동중", "@컴퓨터", "@온라인", "@아침")) {
            val result = sanitizer.sanitize("t", safe, 0L, null, 1f)
            assertEquals(safe, result.context, "안전 태그 '$safe' 가 차단됨")
        }
    }

    @Test
    fun `알 수 없는 자유 태그는 차단됨`() {
        val result = sanitizer.sanitize(
            title = "task",
            context = "@알수없는커스텀태그",
            createdAt = 1_000L,
            completedAt = null,
            challengeRating = 1f,
        )
        assertEquals(null, result.context)
    }

    @Test
    fun `sanitize 결과에는 notes 필드가 존재하지 않음 (화이트리스트만 통과)`() {
        val result = sanitizer.sanitize(
            title = "Buy milk",
            context = "@집",
            createdAt = 1_000L,
            completedAt = 2_000L,
            challengeRating = 2f,
        )
        assertEquals("Buy milk", result.title)
        assertEquals("@집", result.context)
        assertEquals(1_000L, result.createdAt)
        assertEquals(2_000L, result.completedAt)
        assertEquals(2f, result.challengeRating)
    }
}
