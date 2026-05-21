# 07. Claude API 연동

## 7.1 연동 개요

Claude API는 QuestLog의 핵심 차별점인 **동적 스토리 생성**을 담당한다.
모든 AI 기능은 선택적이며, API 미설정 또는 오프라인 시 로컬 템플릿으로 폴백한다.

### 사용 모델

```
claude-sonnet-4-6 (기본)
  - 품질/비용/속도 균형
  - max_tokens: 256 (짧은 내러티브)
  - 대부분의 시나리오에 사용

claude-haiku-4-5-20251001 (빠른 처리 필요 시)
  - 인카운터 생성, 퀘스트 제안 등 빈도 높은 호출
  - max_tokens: 128
```

### API 호출 빈도 관리

```kotlin
object APIBudget {
    // 일일 최대 호출 횟수 (무료 사용자)
    const val FREE_DAILY_LIMIT = 3
    // 프리미엄 사용자: 무제한
    
    // 우선순위 (무료 사용자 쿼터 소진 시 순서)
    // 1. 레벨업 축하 (최고 우선순위)
    // 2. 전투 완료 내러티브
    // 3. Weekly Review 요약
    // 4. 퀘스트 제안
    // 5. 랜덤 인카운터 (최저 우선순위)
}
```

---

## 7.2 시나리오별 프롬프트 설계

### 시나리오 1: 전투 완료 내러티브 (CombatResultScreen)

**트리거**: 퀘스트 완료 + D20 명중

**프롬프트 템플릿**:
```
System:
당신은 판타지 TRPG의 던전마스터입니다. 
플레이어의 실생활 할 일 완료를 판타지 전투 승리 장면으로 묘사해주세요.
한국어로 2-3문장. 유머러스하고 서사적으로. 과장 환영.

User:
캐릭터: {name} (Lv.{level} {className})
완료한 퀘스트: "{taskTitle}"
상대 몬스터: CR {cr} {monsterType} "{monsterName}"
D20 결과: {d20Result}
크리티컬 히트: {isCritical}
연속 완료: {streakDays}일째

이 승리 장면을 2-3문장으로 묘사해주세요.
```

**예시 출력**:
```
"Aria가 마법서의 마지막 페이지를 넘기는 순간, 
보고서 더미 위에 도사리던 마감의 트롤이 비명을 지르며 사라졌다.
'역시 집중력의 수정구는 배신하지 않아...' 그녀는 승리의 미소를 지었다."
```

**로컬 폴백 템플릿 (오프라인/API 미설정)**:
```kotlin
val fallbackTemplates = listOf(
    "{name}이(가) {monsterName}을(를) 처치했습니다! +{xp} XP 획득!",
    "{taskTitle} 완료! {name}의 {className} 능력이 빛을 발했습니다!",
    "D{d20Result}! {name}이(가) {monsterName}에게 결정타를 날렸습니다!"
)
```

---

### 시나리오 2: 크리티컬 히트 특별 내러티브

**트리거**: D20 = 20

**프롬프트 템플릿**:
```
System:
판타지 TRPG 던전마스터. 전설적 크리티컬 히트 장면 묘사.
감탄사와 과장 필수. 한국어 3-4문장.

User:
캐릭터: {name} (Lv.{level} {className})
퀘스트: "{taskTitle}"
몬스터: {monsterName}
획득 XP: {xp} (2배!)
획득 아이템: {itemName}

D20 = 20, 전설적 크리티컬 히트 장면을 묘사해주세요!
```

**예시 출력**:
```
"20! 전설적인 일격!!
Aria의 손에서 뿜어져 나온 신비로운 광채가 던전 전체를 밝혔다.
마감의 리치는 '이... 이럴 수가!' 하는 비명과 함께 먼지로 사라졌고,
그 자리에 황금빛 [시간의 모래시계 검]이 빛나고 있었다."
```

---

### 시나리오 3: Weekly Review AI 요약

**트리거**: WeeklyReview Step 6 도달

**프롬프트 템플릿**:
```
System:
판타지 TRPG의 현명한 던전마스터. 
영웅의 주간 활동을 서사적으로 요약하고 다음 주 조언을 제공.
한국어. DM의 목소리로 3인칭 또는 2인칭 혼용.

User:
캐릭터: {name} Lv.{level} {className}
이번 주 ({weekStart} ~ {weekEnd}):
- 완료한 퀘스트: {completedCount}개
- 획득 XP: {xpGained}
- 연속 완료: {streakDays}일
- 크리티컬 히트: {critCount}회, 미스: {missCount}회
- 생활 영역별: {업무: 8개, 건강: 3개, 학습: 5개, 관계: 1개}
- 미완료 퀘스트: {unfinished}개

이번 주 영웅 보고서를 써주세요.
다음 주에 집중해야 할 조언도 2가지 포함해주세요.
총 4-5문장.
```

**예시 출력**:
```
"용감한 Wizard Aria여, 이번 주 그대는 16개의 퀘스트를 완료하며 
2,340 XP를 획득하는 놀라운 활약을 펼쳤습니다!
특히 학습 영역에서 크리티컬 히트 3회는 그대의 INT 18이 헛되지 않음을 증명했지요.

다음 주 DM의 조언: 첫째, 관계 영역 퀘스트가 1개에 그쳤습니다.
동료들과의 유대를 잊지 마세요 - Bard적인 면을 조금 발휘해볼 시간입니다.
둘째, 미완료 퀘스트 3개가 인박스에 남아있습니다. 
월요일 아침 가장 먼저 처리하는 것을 추천합니다."
```

---

### 시나리오 4: 퀘스트 명료화 AI 제안 (ClarifySheet)

**트리거**: ClarifySheet Step 2 (다음 행동 입력 시 AI 제안 버튼)

**프롬프트 템플릿**:
```
System:
GTD(Getting Things Done) 전문가. 
입력된 항목에서 구체적이고 즉시 실행 가능한 "다음 물리적 행동"을 제안.
동사로 시작하는 짧은 문장 2-3개. 한국어.

User:
수집된 항목: "{rawText}"
캐릭터 클래스: {className}
주요 능력치: {primaryAbility}

이 항목의 다음 물리적 행동 3개를 제안해주세요.
각각 다른 접근 방식으로.
```

**예시 입력**: "세금 신고"

**예시 출력**:
```json
{
  "suggestions": [
    "세무사 앱 열어서 작년 소득 서류 업로드하기",
    "국세청 홈택스 로그인하여 간편신고 시작하기",
    "세무 관련 서류 폴더 찾아서 필요 서류 목록 확인하기"
  ]
}
```

---

### 시나리오 5: 레벨업 축하 메시지 (LevelUpScreen)

**트리거**: 레벨업 임계값 도달

**프롬프트 템플릿**:
```
System:
판타지 세계의 현인. 새로운 레벨에 오른 모험가를 축하.
해당 레벨의 의미와 새로운 능력에 대한 서사적 설명.
한국어. 감동적이고 서사적. 3-4문장.

User:
캐릭터: {name} {className}
달성 레벨: {level}
새로운 특성: {newFeature}
총 완료 퀘스트: {totalQuests}개

레벨업 축하 메시지를 써주세요.
```

**예시 출력**:
```
"마침내 8레벨의 경지에 오르셨군요, Aria Stormwind!
{totalQuests}개의 퀘스트를 완료하며 쌓아온 지혜가 
이제 새로운 형태로 꽃을 피웁니다.
'Spell Mastery'의 힘으로, 당신이 가장 자주 다루는 퀘스트들이
마치 숨쉬듯 자연스러워질 것입니다.
전설은 이제 시작입니다."
```

---

### 시나리오 6: 랜덤 인카운터 생성 (WorkManager)

**트리거**: 하루 1-2회 WorkManager (오프라인 시 로컬 풀 사용)

**프롬프트 템플릿**:
```
System:
GTD-RPG 게임 콘텐츠 생성자.
오늘의 랜덤 인카운터 이벤트 1개를 생성.
생산성/일상과 연관된 판타지 이벤트. 유머러스하게.
한국어. 제목(10자 이내) + 설명(2문장) 형식.

User:
캐릭터: {name} Lv.{level} {className}
오늘 완료 퀘스트: {todayCompleted}개
인카운터 유형: {encounterType}  (BONUS_XP / ITEM_DROP / HP_HEAL / ...)

오늘의 랜덤 인카운터를 생성해주세요.
```

**예시 출력 (ITEM_DROP)**:
```json
{
  "title": "방랑 상인 출현!",
  "description": "오늘 퀘스트 3개를 완료한 Aria 앞에 수상한 망토를 걸친 상인이 나타났다. 'psst, 오늘 유독 열심히 하셨군요... 이거 드리죠.'",
  "effect": "집중력의 수정구 획득",
  "xpBonus": 0
}
```

---

### 시나리오 7: 크리티컬 미스 위로 메시지

**트리거**: D20 = 1

**프롬프트 템플릿**:
```
System:
DM이 크리티컬 미스한 모험가를 유머러스하게 위로.
절대 슬프거나 부정적이지 않게. 
실패가 이야기의 일부임을 강조. 한국어. 2문장.

User:
퀘스트: "{taskTitle}"
D20: 1
HP 손실: {hpLost}
캐릭터: {name}

크리티컬 미스 위로 메시지.
```

**예시 출력**:
```
"오늘따라 주사위의 신이 심술궂군요, Aria!
걱정 마세요 - 심지어 전설적인 마법사도 가끔은 지팡이를 잃어버린답니다. 😅"
```

---

## 7.3 API 클라이언트 구현

```kotlin
// data/remote/ClaudeApiService.kt
interface ClaudeApiService {
    @POST("v1/messages")
    suspend fun generateMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Header("content-type") contentType: String = "application/json",
        @Body request: ClaudeMessageRequest
    ): ClaudeMessageResponse
}

data class ClaudeMessageRequest(
    val model: String,
    val max_tokens: Int,
    val system: String? = null,
    val messages: List<ClaudeMessage>
)

data class ClaudeMessage(
    val role: String,   // "user" | "assistant"
    val content: String
)

data class ClaudeMessageResponse(
    val id: String,
    val content: List<ContentBlock>,
    val usage: Usage
)

data class ContentBlock(
    val type: String,
    val text: String
)

data class Usage(
    val input_tokens: Int,
    val output_tokens: Int
)
```

### Repository 구현

```kotlin
// data/remote/ClaudeRepository.kt
class ClaudeRepositoryImpl @Inject constructor(
    private val apiService: ClaudeApiService,
    private val secureStorage: SecureStorage,
    private val appSettings: AppSettings
) : ClaudeRepository {

    override suspend fun generateCombatNarrative(
        character: Character,
        task: Task,
        combatResult: CombatResult
    ): String {
        if (!appSettings.claudeApiEnabled) return getFallbackNarrative(task, combatResult)
        
        val apiKey = secureStorage.getApiKey() 
            ?: return getFallbackNarrative(task, combatResult)
        
        return try {
            val prompt = buildCombatNarrativePrompt(character, task, combatResult)
            val response = apiService.generateMessage(
                apiKey = apiKey,
                request = ClaudeMessageRequest(
                    model = "claude-sonnet-4-6",
                    max_tokens = 256,
                    system = COMBAT_NARRATIVE_SYSTEM_PROMPT,
                    messages = listOf(ClaudeMessage("user", prompt))
                )
            )
            response.content.firstOrNull()?.text ?: getFallbackNarrative(task, combatResult)
        } catch (e: Exception) {
            Timber.e(e, "Claude API 호출 실패")
            getFallbackNarrative(task, combatResult)
        }
    }
    
    private fun getFallbackNarrative(task: Task, result: CombatResult): String {
        val templates = when (result) {
            is CombatResult.CriticalHit -> FALLBACK_CRITICAL_HIT
            is CombatResult.Hit -> FALLBACK_HIT
            is CombatResult.Miss -> FALLBACK_MISS
            is CombatResult.CriticalMiss -> FALLBACK_CRITICAL_MISS
        }
        return templates.random()
            .replace("{taskTitle}", task.title)
            .replace("{xp}", (result as? CombatResult.Hit)?.xpGained?.toString() ?: "0")
    }
}
```

---

## 7.4 프롬프트 캐싱 전략

```kotlin
// Claude API 프롬프트 캐싱 활용
// System 프롬프트를 캐시하여 토큰 비용 절감

val cachedSystemPrompt = ClaudeMessageRequest(
    model = "claude-sonnet-4-6",
    max_tokens = 256,
    system = SYSTEM_PROMPT,  // 캐시됨 (5분 TTL)
    messages = listOf(
        // cache_control 헤더로 system prompt 캐시 표시
        ClaudeMessage("user", userPrompt)
    )
)

// 비용 추정 (일일):
// 무료 사용자 (3회/일): ~$0.01
// 프리미엄 (30회/일): ~$0.05
// 캐싱으로 system prompt 비용 90% 절감 가능
```

---

## 7.5 오프라인 폴백 템플릿

```kotlin
object LocalNarratives {
    val COMBAT_HIT = listOf(
        "{name}이(가) 집중력을 발휘하여 '{taskTitle}'을(를) 완료했습니다!",
        "오늘도 빛나는 활약! {name}이(가) {monsterName}을(를) 쓰러뜨렸습니다.",
        "'{taskTitle}' 처리 완료! +{xp} XP 획득."
    )
    
    val CRITICAL_HIT = listOf(
        "🔥 전설적인 일격!! {name}이(가) 완벽하게 처리했습니다! XP 2배!",
        "D20이 빛납니다! '{taskTitle}' 크리티컬 히트! +{xp} XP!!",
        "역대 최고의 퍼포먼스! {name}의 {className} 능력이 폭발했습니다!"
    )
    
    val CRITICAL_MISS = listOf(
        "오늘은 주사위가 심술궂었네요. 하지만 내일은 달라요! 😅",
        "크리티컬 미스! 이것도 이야기의 일부입니다. HP를 회복하고 다시!",
        "D20=1... 전설적인 모험가도 가끔 넘어지죠. 일어나세요!"
    )
    
    val WEEKLY_REVIEW = listOf(
        "이번 주도 수고했습니다, {name}! {completedCount}개 퀘스트 완료!",
        "주간 리뷰 완료! {name}의 모험 일지가 업데이트되었습니다.",
        "{weekXp} XP 획득! 다음 주도 화이팅, {name}!"
    )
    
    val LEVEL_UP = listOf(
        "레벨 {level} 달성! {name}이(가) 강해졌습니다!",
        "축하합니다! Lv.{level} {className} {name}! 새 특성이 해금되었습니다.",
        "{totalQuests}개의 퀘스트가 이 순간을 만들었습니다. Lv.{level} 달성!"
    )
    
    val RANDOM_ENCOUNTERS = listOf(
        EncounterTemplate("보물 상자 발견!", "오래된 서랍에서 보물이!", "+{xp} XP"),
        EncounterTemplate("방랑 상인 출현!", "수상한 상인이 아이템을 건넨다.", "아이템 획득"),
        EncounterTemplate("신비한 샘 발견", "맑은 물을 마시니 기운이 솟는다.", "HP 회복"),
        EncounterTemplate("수호 요정 등장", "반짝이는 요정이 나타났다!", "스트릭 토큰 +1"),
        EncounterTemplate("여관 무료 숙박", "친절한 여관 주인의 호의!", "HP 전체 회복")
    )
}
```

---

## 7.6 AI 기능 설정 화면

```
SettingsScreen → AI 설정 섹션

┌─────────────────────────────────────┐
│ 🤖 Claude AI 설정                   │
├─────────────────────────────────────┤
│ Claude API 키                        │
│ [sk-ant-••••••••••••••] [변경]      │
│                                     │
│ AI 기능 활성화    [토글: ON]         │
│                                     │
│ AI 사용 기능 선택                    │
│ [✓] 전투 완료 내러티브               │
│ [✓] Weekly Review 요약              │
│ [✓] 퀘스트 제안 (Clarify)           │
│ [✓] 레벨업 축하 메시지              │
│ [ ] 랜덤 인카운터 생성              │
│                                     │
│ 무료 사용자: 하루 3회 제한           │
│ 오늘 사용: 1/3회                    │
│                                     │
│ [프리미엄 업그레이드 → 무제한]       │
└─────────────────────────────────────┘
```
