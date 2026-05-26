package com.questlog.core.domain.model

// 12 클래스별 UI 콘텐츠. docs/04_game_mechanics.md §4.4 / docs/10_design_system.md SSOT.
data class ClassInfo(
    val classType: CharacterClass,
    val emoji: String,
    val tagline: String,       // 한 줄 설명
    val description: String,   // 3-4 문장
    val gtdTraits: String,     // GTD 성격
    val namePool: List<String>,
)

object ClassContent {

    val all: List<ClassInfo> = listOf(
        ClassInfo(
            classType = CharacterClass.BARBARIAN,
            emoji = "🪓",
            tagline = "분노가 곧 나의 힘",
            description = "열정과 즉각적인 행동이 당신의 무기입니다. 목표가 생기면 망설임 없이 돌진하고, 에너지가 넘쳐 주변을 이끕니다. 루틴보다 순간적인 집중력이 강점입니다.",
            gtdTraits = "빠른 포착·즉각 실행. 완벽보다 완료.",
            namePool = listOf("Kaz", "Thorm", "Ulfric", "Brom", "Mira", "Zarya", "Gunnar", "Ragna"),
        ),
        ClassInfo(
            classType = CharacterClass.BARD,
            emoji = "🎸",
            tagline = "이야기로 세상을 움직인다",
            description = "창의성과 소통이 당신의 핵심입니다. 다양한 분야를 넘나들며 연결하고, 사람들에게 영감을 줍니다. 지루한 일도 재미있게 만드는 방법을 압니다.",
            gtdTraits = "브레인스토밍·다목적 협업·스토리텔링.",
            namePool = listOf("Lyra", "Dorian", "Senna", "Vex", "Calla", "Finn", "Aria", "Caelum"),
        ),
        ClassInfo(
            classType = CharacterClass.CLERIC,
            emoji = "✝️",
            tagline = "사람을 돕는 것이 사명",
            description = "공감과 봉사가 삶의 중심입니다. 다른 사람을 지원하고 팀의 균형을 맞추는 역할에서 보람을 느낍니다. 체계적이고 원칙에 충실합니다.",
            gtdTraits = "일정 관리·위임·협력 지원.",
            namePool = listOf("Selene", "Aldric", "Miriam", "Theron", "Lumen", "Cas", "Deva", "Oryn"),
        ),
        ClassInfo(
            classType = CharacterClass.DRUID,
            emoji = "🌿",
            tagline = "균형 속에서 성장한다",
            description = "자연스러운 흐름과 지속 가능한 리듬을 중시합니다. 장기적인 관점으로 문제를 보고, 무리하지 않는 균형 잡힌 생산성을 추구합니다.",
            gtdTraits = "장기 프로젝트·자기 관리·에너지 보전.",
            namePool = listOf("Sylva", "Oaken", "Fern", "Rowan", "Mira", "Cedar", "Ash", "Briar"),
        ),
        ClassInfo(
            classType = CharacterClass.FIGHTER,
            emoji = "⚔️",
            tagline = "체계와 훈련이 결과를 만든다",
            description = "꾸준한 훈련과 체계적인 접근으로 목표를 달성합니다. 계획을 세우고 일관되게 실행하는 것이 강점입니다. 어떤 도전에도 준비된 자세를 유지합니다.",
            gtdTraits = "할 일 목록·루틴·체계적 실행.",
            namePool = listOf("Aldric", "Brynn", "Gareth", "Nora", "Rook", "Tarn", "Isolde", "Cavan"),
        ),
        ClassInfo(
            classType = CharacterClass.MONK,
            emoji = "🥋",
            tagline = "규율과 집중이 한계를 깬다",
            description = "깊은 집중과 자기 수련으로 탁월함을 추구합니다. 방해 요소를 차단하고 몰입하는 능력이 뛰어납니다. 몸과 마음의 균형을 통해 생산성을 극대화합니다.",
            gtdTraits = "딥워크·집중 블록·디지털 미니멀리즘.",
            namePool = listOf("Zen", "Kira", "Dao", "Shen", "Lian", "Haru", "Yuna", "Rei"),
        ),
        ClassInfo(
            classType = CharacterClass.PALADIN,
            emoji = "⚜️",
            tagline = "신념이 있기에 흔들리지 않는다",
            description = "가치와 원칙을 중심으로 행동합니다. 장기적인 목표와 일상의 행동을 일치시키려 노력하며, 주변 사람들에게 신뢰와 안정감을 줍니다.",
            gtdTraits = "미션 정렬·일관성·책임 완수.",
            namePool = listOf("Dawnash", "Seraph", "Valiant", "Lux", "Eryn", "Caleb", "Vesper", "Avira"),
        ),
        ClassInfo(
            classType = CharacterClass.RANGER,
            emoji = "🏹",
            tagline = "혼자서도, 함께도 잘한다",
            description = "독립적으로 일하는 것을 즐기면서도 필요할 때 협력할 줄 압니다. 목표를 향해 꾸준히 나아가며, 환경 변화에 빠르게 적응합니다.",
            gtdTraits = "자율 작업·컨텍스트 전환·원격 작업.",
            namePool = listOf("Scout", "Wren", "Cian", "Aera", "Lyric", "Dex", "Neva", "Tracker"),
        ),
        ClassInfo(
            classType = CharacterClass.ROGUE,
            emoji = "🗡️",
            tagline = "기회를 보는 눈, 결정적 한 방",
            description = "기회를 포착하고 효율적으로 실행하는 능력이 탁월합니다. 남들이 놓치는 지름길과 패턴을 발견합니다. 적은 자원으로 최대 효과를 냅니다.",
            gtdTraits = "우선순위 집중·2분 룰·스마트 위임.",
            namePool = listOf("Shade", "Vex", "Riven", "Cipher", "Nix", "Sable", "Wick", "Zara"),
        ),
        ClassInfo(
            classType = CharacterClass.SORCERER,
            emoji = "⚡",
            tagline = "직관과 에너지로 세상을 바꾼다",
            description = "타고난 재능과 직관으로 빠르게 결과를 만들어냅니다. 열정이 넘치고 변화를 두려워하지 않습니다. 영감이 떠오를 때 폭발적인 집중력을 발휘합니다.",
            gtdTraits = "영감 주도·빠른 프로토타입·에너지 버스트.",
            namePool = listOf("Spark", "Nova", "Ember", "Zephyr", "Blaze", "Volta", "Surge", "Pyra"),
        ),
        ClassInfo(
            classType = CharacterClass.WARLOCK,
            emoji = "🕯️",
            tagline = "신비로운 힘과의 계약",
            description = "깊은 집중력과 신비로운 통찰력으로 남들이 이해하지 못하는 패턴을 파악합니다. 독창적인 방식으로 문제를 해결하고, 강한 목적의식을 가집니다.",
            gtdTraits = "장기 비전·심층 분석·독창적 해결책.",
            namePool = listOf("Hex", "Morne", "Voidus", "Elara", "Pact", "Darkon", "Seraph", "Riven"),
        ),
        ClassInfo(
            classType = CharacterClass.WIZARD,
            emoji = "📚",
            tagline = "지식이 곧 힘이다",
            description = "분석적이고 방법론적인 당신. 지식을 쌓고 체계적으로 접근하는 것을 선호합니다. 복잡한 문제를 단계별로 해결하며, 배운 것을 시스템화합니다.",
            gtdTraits = "지식 관리·체계적 계획·주간 리뷰.",
            namePool = listOf("Arcane", "Lore", "Sage", "Codex", "Rune", "Elara", "Magnus", "Syla"),
        ),
    )

    fun forClass(classType: CharacterClass): ClassInfo =
        all.first { it.classType == classType }

    fun randomName(classType: CharacterClass): String =
        forClass(classType).namePool.random()
}
