package com.questlog.core.domain.model

// 클래스 퀴즈 12 문항. 각 답변은 CharacterClass 별 가중치를 갖는다.
// ClassRecommendationUseCase 가 전체 점수를 합산하여 최고 클래스 반환.
data class QuizQuestion(
    val id: Int,
    val text: String,
    val options: List<QuizOption>,
)

data class QuizOption(
    val text: String,
    val weights: Map<CharacterClass, Int>,  // 클래스 → 점수 (0~3)
)

object ClassQuiz {

    val questions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = 1,
            text = "새로운 프로젝트가 생겼을 때 나는?",
            options = listOf(
                QuizOption("바로 시작한다. 생각은 나중에.", mapOf(CharacterClass.BARBARIAN to 3, CharacterClass.SORCERER to 2, CharacterClass.ROGUE to 1)),
                QuizOption("계획을 먼저 세우고 체계적으로 접근한다.", mapOf(CharacterClass.WIZARD to 3, CharacterClass.FIGHTER to 2, CharacterClass.PALADIN to 1)),
                QuizOption("팀원들과 상의하며 역할을 나눈다.", mapOf(CharacterClass.CLERIC to 3, CharacterClass.BARD to 2, CharacterClass.PALADIN to 1)),
                QuizOption("주변 환경과 상황을 먼저 파악한다.", mapOf(CharacterClass.RANGER to 3, CharacterClass.DRUID to 2, CharacterClass.MONK to 1)),
            ),
        ),
        QuizQuestion(
            id = 2,
            text = "할 일 목록이 넘쳐날 때 나는?",
            options = listOf(
                QuizOption("가장 급한 것 하나에만 집중한다.", mapOf(CharacterClass.ROGUE to 3, CharacterClass.BARBARIAN to 2, CharacterClass.MONK to 1)),
                QuizOption("전부 정리하고 우선순위를 매긴다.", mapOf(CharacterClass.WIZARD to 3, CharacterClass.FIGHTER to 2, CharacterClass.CLERIC to 1)),
                QuizOption("자연스럽게 에너지가 가는 것부터 한다.", mapOf(CharacterClass.DRUID to 3, CharacterClass.SORCERER to 2, CharacterClass.BARD to 1)),
                QuizOption("다른 사람에게 일부를 넘긴다.", mapOf(CharacterClass.PALADIN to 3, CharacterClass.CLERIC to 2, CharacterClass.RANGER to 1)),
            ),
        ),
        QuizQuestion(
            id = 3,
            text = "가장 잘 집중되는 환경은?",
            options = listOf(
                QuizOption("완전한 침묵, 혼자 있을 때.", mapOf(CharacterClass.MONK to 3, CharacterClass.WIZARD to 2, CharacterClass.WARLOCK to 2)),
                QuizOption("카페나 약간의 소음이 있을 때.", mapOf(CharacterClass.BARD to 3, CharacterClass.SORCERER to 2, CharacterClass.ROGUE to 1)),
                QuizOption("자연 속이나 야외에서.", mapOf(CharacterClass.DRUID to 3, CharacterClass.RANGER to 3, CharacterClass.BARBARIAN to 1)),
                QuizOption("팀원들과 함께 있을 때.", mapOf(CharacterClass.CLERIC to 3, CharacterClass.PALADIN to 2, CharacterClass.FIGHTER to 1)),
            ),
        ),
        QuizQuestion(
            id = 4,
            text = "나의 가장 큰 생산성 장애물은?",
            options = listOf(
                QuizOption("지루한 반복 작업.", mapOf(CharacterClass.BARBARIAN to 2, CharacterClass.BARD to 2, CharacterClass.SORCERER to 2)),
                QuizOption("너무 많은 옵션과 결정.", mapOf(CharacterClass.DRUID to 2, CharacterClass.WARLOCK to 2, CharacterClass.WIZARD to 1)),
                QuizOption("방해와 집중력 분산.", mapOf(CharacterClass.MONK to 3, CharacterClass.WIZARD to 2, CharacterClass.RANGER to 1)),
                QuizOption("동기부여와 에너지 저하.", mapOf(CharacterClass.FIGHTER to 2, CharacterClass.PALADIN to 2, CharacterClass.CLERIC to 1)),
            ),
        ),
        QuizQuestion(
            id = 5,
            text = "장기 목표를 추구할 때 나는?",
            options = listOf(
                QuizOption("큰 그림을 그리고 단계별로 쪼갠다.", mapOf(CharacterClass.WIZARD to 3, CharacterClass.PALADIN to 2, CharacterClass.FIGHTER to 2)),
                QuizOption("열정이 있을 때 집중적으로 몰아붙인다.", mapOf(CharacterClass.BARBARIAN to 3, CharacterClass.SORCERER to 2, CharacterClass.WARLOCK to 1)),
                QuizOption("꾸준히 조금씩 매일 해나간다.", mapOf(CharacterClass.DRUID to 3, CharacterClass.MONK to 3, CharacterClass.RANGER to 1)),
                QuizOption("누군가와 함께 진행하며 동기를 얻는다.", mapOf(CharacterClass.BARD to 2, CharacterClass.CLERIC to 3, CharacterClass.PALADIN to 1)),
            ),
        ),
        QuizQuestion(
            id = 6,
            text = "마감이 닥쳤을 때 나는?",
            options = listOf(
                QuizOption("위기감이 오히려 집중력을 높인다.", mapOf(CharacterClass.ROGUE to 3, CharacterClass.BARBARIAN to 2, CharacterClass.SORCERER to 1)),
                QuizOption("일찍 준비해서 여유 있게 완료한다.", mapOf(CharacterClass.FIGHTER to 3, CharacterClass.WIZARD to 2, CharacterClass.PALADIN to 1)),
                QuizOption("팀원들에게 도움을 요청한다.", mapOf(CharacterClass.CLERIC to 2, CharacterClass.BARD to 2, CharacterClass.RANGER to 1)),
                QuizOption("우선순위를 조정하고 핵심만 완료한다.", mapOf(CharacterClass.MONK to 2, CharacterClass.DRUID to 2, CharacterClass.WARLOCK to 1)),
            ),
        ),
        QuizQuestion(
            id = 7,
            text = "새로운 기술이나 지식을 배울 때?",
            options = listOf(
                QuizOption("책이나 강의로 체계적으로 배운다.", mapOf(CharacterClass.WIZARD to 3, CharacterClass.CLERIC to 2, CharacterClass.MONK to 1)),
                QuizOption("직접 해보면서 실수하며 배운다.", mapOf(CharacterClass.BARBARIAN to 2, CharacterClass.ROGUE to 2, CharacterClass.SORCERER to 2)),
                QuizOption("다양한 자료를 폭넓게 탐색한다.", mapOf(CharacterClass.BARD to 3, CharacterClass.RANGER to 2, CharacterClass.DRUID to 1)),
                QuizOption("직관이 이끄는 대로 깊이 파고든다.", mapOf(CharacterClass.WARLOCK to 3, CharacterClass.WIZARD to 1, CharacterClass.SORCERER to 1)),
            ),
        ),
        QuizQuestion(
            id = 8,
            text = "팀 프로젝트에서 나의 역할은?",
            options = listOf(
                QuizOption("아이디어를 내고 분위기를 이끈다.", mapOf(CharacterClass.BARD to 3, CharacterClass.BARBARIAN to 2, CharacterClass.SORCERER to 1)),
                QuizOption("계획을 세우고 실행을 관리한다.", mapOf(CharacterClass.PALADIN to 3, CharacterClass.FIGHTER to 2, CharacterClass.WIZARD to 1)),
                QuizOption("팀원들의 필요를 채우고 지원한다.", mapOf(CharacterClass.CLERIC to 3, CharacterClass.DRUID to 2, CharacterClass.RANGER to 1)),
                QuizOption("독립적으로 맡은 부분을 해결한다.", mapOf(CharacterClass.ROGUE to 3, CharacterClass.MONK to 2, CharacterClass.WARLOCK to 1)),
            ),
        ),
        QuizQuestion(
            id = 9,
            text = "에너지가 고갈되었을 때 회복 방법은?",
            options = listOf(
                QuizOption("격렬한 운동이나 활동.", mapOf(CharacterClass.BARBARIAN to 3, CharacterClass.FIGHTER to 2, CharacterClass.MONK to 1)),
                QuizOption("혼자만의 조용한 시간.", mapOf(CharacterClass.WIZARD to 2, CharacterClass.WARLOCK to 2, CharacterClass.DRUID to 2)),
                QuizOption("사람들과 어울리며 이야기한다.", mapOf(CharacterClass.BARD to 3, CharacterClass.CLERIC to 2, CharacterClass.PALADIN to 1)),
                QuizOption("자연 속에서 산책이나 휴식.", mapOf(CharacterClass.DRUID to 3, CharacterClass.RANGER to 2, CharacterClass.MONK to 1)),
            ),
        ),
        QuizQuestion(
            id = 10,
            text = "가장 뿌듯함을 느끼는 순간은?",
            options = listOf(
                QuizOption("불가능해 보이던 일을 해냈을 때.", mapOf(CharacterClass.BARBARIAN to 2, CharacterClass.FIGHTER to 2, CharacterClass.PALADIN to 2)),
                QuizOption("복잡한 문제의 해결책을 찾았을 때.", mapOf(CharacterClass.WIZARD to 3, CharacterClass.ROGUE to 2, CharacterClass.WARLOCK to 1)),
                QuizOption("누군가에게 진짜 도움이 됐을 때.", mapOf(CharacterClass.CLERIC to 3, CharacterClass.DRUID to 2, CharacterClass.BARD to 1)),
                QuizOption("완벽한 리듬과 흐름으로 작업했을 때.", mapOf(CharacterClass.MONK to 3, CharacterClass.SORCERER to 2, CharacterClass.RANGER to 1)),
            ),
        ),
        QuizQuestion(
            id = 11,
            text = "하루를 계획할 때 나는?",
            options = listOf(
                QuizOption("상세한 시간표를 만든다.", mapOf(CharacterClass.FIGHTER to 3, CharacterClass.WIZARD to 2, CharacterClass.MONK to 1)),
                QuizOption("중요한 것 3개만 정한다.", mapOf(CharacterClass.ROGUE to 3, CharacterClass.PALADIN to 2, CharacterClass.BARBARIAN to 1)),
                QuizOption("그날 기분과 에너지에 맞게 유연하게.", mapOf(CharacterClass.DRUID to 3, CharacterClass.SORCERER to 2, CharacterClass.BARD to 1)),
                QuizOption("계획보다 인연과 흐름을 따른다.", mapOf(CharacterClass.WARLOCK to 2, CharacterClass.RANGER to 2, CharacterClass.CLERIC to 1)),
            ),
        ),
        QuizQuestion(
            id = 12,
            text = "10년 후 나의 모습은?",
            options = listOf(
                QuizOption("내 분야의 전문가로 깊이 탐구하고 있다.", mapOf(CharacterClass.WIZARD to 3, CharacterClass.WARLOCK to 2, CharacterClass.MONK to 2)),
                QuizOption("사람들을 이끌고 영향력을 발휘한다.", mapOf(CharacterClass.PALADIN to 3, CharacterClass.BARD to 2, CharacterClass.BARBARIAN to 1)),
                QuizOption("자연스럽고 지속 가능한 삶을 살고 있다.", mapOf(CharacterClass.DRUID to 3, CharacterClass.RANGER to 2, CharacterClass.CLERIC to 1)),
                QuizOption("다양한 분야에서 자유롭게 활동한다.", mapOf(CharacterClass.ROGUE to 2, CharacterClass.SORCERER to 2, CharacterClass.BARD to 1)),
            ),
        ),
    )
}
