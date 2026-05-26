package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClassQuiz
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClassRecommendationUseCaseTest {

    private val useCase = ClassRecommendationUseCase()

    @Test
    fun `빈 답변이면 결과는 3개 클래스를 여전히 반환한다`() {
        val result = useCase.recommend(emptyMap())
        assertEquals(3, result.size)
    }

    @Test
    fun `모든 문항에 첫 번째 보기 선택 시 3개 결과 반환`() {
        val allFirstOption = ClassQuiz.questions.associate { it.id to 0 }
        val result = useCase.recommend(allFirstOption)
        assertEquals(3, result.size)
        assertTrue(result.toSet().size == 3, "중복 없어야 함")
    }

    @Test
    fun `WIZARD 성향 답변 시 결과 최상위에 WIZARD 가 포함된다`() {
        // 문항 7: index 0 = "책이나 강의로 체계적으로 배운다" → WIZARD 3점
        // 문항 11: index 0 = "상세한 시간표를 만든다" → FIGHTER 3, WIZARD 2
        // 문항 12: index 0 = "내 분야의 전문가로" → WIZARD 3점
        val wizardAnswers = mapOf(7 to 0, 11 to 0, 12 to 0)
        val result = useCase.recommend(wizardAnswers)
        assertTrue(CharacterClass.WIZARD in result, "WIZARD 가 결과에 있어야 함: $result")
    }

    @Test
    fun `결과 목록에 중복이 없다`() {
        val answers = ClassQuiz.questions.associate { it.id to it.options.indices.random() }
        val result = useCase.recommend(answers)
        assertEquals(result.size, result.toSet().size)
    }
}
