package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClassQuiz

// 퀴즈 답변(question id → option index) 을 받아 클래스별 총점 계산 후 상위 3 개 반환.
// 동점이면 CharacterClass.ordinal 순으로 안정 정렬.
class ClassRecommendationUseCase {

    // answers: questionId(1-based) → 선택한 optionIndex(0-based)
    fun recommend(answers: Map<Int, Int>): List<CharacterClass> {
        val scores = mutableMapOf<CharacterClass, Int>()
        for (question in ClassQuiz.questions) {
            val optionIndex = answers[question.id] ?: continue
            val option = question.options.getOrNull(optionIndex) ?: continue
            for ((cls, weight) in option.weights) {
                scores[cls] = (scores[cls] ?: 0) + weight
            }
        }
        return CharacterClass.entries
            .sortedWith(compareByDescending<CharacterClass> { scores[it] ?: 0 }.thenBy { it.ordinal })
            .take(3)
    }
}
