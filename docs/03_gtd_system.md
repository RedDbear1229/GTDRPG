# 03. GTD 시스템 설계

## 3.1 GTD 5단계 전체 플로우

```
┌─────────────────────────────────────────────────────────┐
│                    GTD 5단계 플로우                      │
└─────────────────────────────────────────────────────────┘

[CAPTURE] ──→ [CLARIFY] ──→ [ORGANIZE] ──→ [REFLECT] ──→ [ENGAGE]
  📥 수집        ❓ 명료화      🗂️ 정리         🔍 검토         ⚔️ 실행
  Inbox          ClarifySheet   QuestBoard       WeeklyReview    전투 완료
  QuickCapture   결정 트리       프로젝트/맥락    JournalScreen   D20 굴림
```

---

## 3.2 CAPTURE (수집)

### 수집 원칙
- **모든 것을 수집한다**: 머릿속에 남아있는 것은 무엇이든 Inbox로
- **처리는 나중에**: 수집 시점에 판단하지 않는다
- **마찰 최소화**: 앱을 열지 않아도 수집 가능해야 한다

### 수집 채널
| 채널 | 구현 방법 | 우선순위 |
|------|----------|---------|
| 앱 내 FAB | QuickCaptureSheet | P0 |
| 텍스트 입력 | TextField + 즉시 저장 | P0 |
| 음성 입력 | Android SpeechRecognizer API | **P4 (F4.5)** — F4.0 동의 인프라 선행 필수, Phase 1 범위 밖 |
| 사진 첨부 | CameraX + 갤러리 선택 | P1 |
| 홈화면 위젯 (탭→앱 캡처) | `InboxWidgetProvider` RemoteViews + PendingIntent(`EXTRA_OPEN_CAPTURE`) | **P0 (v1 출시 범위)** |
| 공유 시트 연동 | Share Intent 수신 (`ACTION_SEND` 텍스트 → 즉시 Inbox 저장) | **P0 (v1 출시 범위)** |
| 알림에서 수집 | Notification Action | P2 |
| 위젯 인라인 캡처 / Glance API / 오늘 마감 카운트 | Glance Widget API 마이그레이션 | P2 (v1.5) |

### QuickCaptureSheet 동작
```
1. FAB 탭 → BottomSheet 슬라이드업 (200ms)
2. 텍스트 필드 자동 포커스 + 소프트 키보드 표시
3. 입력 후 Enter 또는 [저장] 버튼
4. InboxItem 생성 → Inbox 카운트 배지 +1
5. 시트 닫힘 (150ms)
6. 전체 소요시간: 목표 5초 이내
```

### InboxItem 데이터
```kotlin
data class InboxItem(
    val id: String,
    val rawText: String,           // 원시 입력 텍스트
    val audioPath: String?,        // 음성 파일 경로 (STT 전 원본) — Phase 4 F4.5 에서 사용, Phase 1 은 항상 null
    val transcribedText: String?,  // STT 변환 결과 — Phase 4 F4.5 에서 사용, Phase 1 은 항상 null
    val imagePaths: List<String>,  // 첨부 사진 목록
    val capturedAt: Long,
    val source: CaptureSource,     // APP, WIDGET, SHARE, NOTIFICATION
    val isClarified: Boolean = false
)
```

---

## 3.3 CLARIFY (명료화)

### GTD 결정 트리 전체 로직

```
입력: InboxItem (rawText)
      │
      ▼
┌─────────────────────────────────────┐
│ Q1: 실행 가능한 항목인가?            │
└─────────────────────────────────────┘
      │              │
     [예]           [아니오]
      │              │
      │    ┌─────────┴───────────────┐
      │    │         │               │
      │  [버릴것]  [언젠가/아마도]  [참고자료]
      │    │         │               │
      │   삭제    SomedayList    ReferenceList
      │
      ▼
┌─────────────────────────────────────┐
│ Q2: 다음 물리적 행동은 무엇인가?     │
│ (Claude AI 제안 옵션 제공)           │
└─────────────────────────────────────┘
      │
      ▼
┌─────────────────────────────────────┐
│ Q3: 2분 안에 완료 가능한가?          │
└─────────────────────────────────────┘
      │              │
     [예]           [아니오]
      │              │
  "지금 바로!"        │
  (앱에 기록만)       │
                      ▼
              ┌───────────────────┐
              │ Q4: 내가 해야 하나? │
              └───────────────────┘
                      │              │
                     [예]          [아니오]
                      │              │
                      │         [NPC 위임]
                      │         WaitingFor 목록
                      ▼
              ┌───────────────────┐
              │ Q5: 프로젝트인가?  │
              └───────────────────┘
                      │              │
              [단독 퀘스트]    [프로젝트에 추가]
                      │              │
                      └──────┬───────┘
                             ▼
                    ┌──────────────────┐
                    │ Q6: 세부 설정     │
                    │ - CR 자동 계산   │
                    │ - 예상 시간      │
                    │ - 마감일         │
                    │ - 맥락 태그      │
                    │ - 생활 영역      │
                    └──────────────────┘
                             │
                             ▼
                       Task 생성 완료
                    QuestBoard에 배치됨
```

### CR (Challenge Rating) 자동 계산 알고리즘

```kotlin
fun calculateCR(
    estimatedMinutes: Int,
    hasDeadline: Boolean,
    daysUntilDeadline: Int?,
    complexitySteps: Int,     // 하위 작업 수
    lifeArea: LifeArea,
    isRecurring: Boolean
): Float {

    // 1. 시간 기반 베이스 점수 (0-10)
    val timeScore = when {
        estimatedMinutes <= 2   -> 0f
        estimatedMinutes <= 15  -> 0.25f
        estimatedMinutes <= 30  -> 0.5f
        estimatedMinutes <= 60  -> 1f
        estimatedMinutes <= 120 -> 2f
        estimatedMinutes <= 240 -> 3f
        estimatedMinutes <= 480 -> 5f
        estimatedMinutes <= 960 -> 8f
        else                    -> 11f
    }

    // 2. 긴급도 점수 (0-10)
    val urgencyScore = if (hasDeadline && daysUntilDeadline != null) {
        when {
            daysUntilDeadline <= 0  -> 10f  // 마감 초과
            daysUntilDeadline <= 1  -> 8f
            daysUntilDeadline <= 3  -> 5f
            daysUntilDeadline <= 7  -> 3f
            daysUntilDeadline <= 30 -> 1f
            else                    -> 0f
        }
    } else 0f

    // 3. 복잡도 점수 (0-10)
    val complexityScore = (complexitySteps * 1.5f).coerceAtMost(10f)

    // 4. 가중 합산
    val rawCR = (timeScore * 0.4f) + (urgencyScore * 0.35f) + (complexityScore * 0.25f)

    // 5. 루틴 보정 (반복 작업은 CR 낮춤)
    val routineMultiplier = if (isRecurring) 0.7f else 1f

    return (rawCR * routineMultiplier).coerceIn(0f, 30f)
}
```

### 맥락 태그 (@Context) 시스템

GTD의 Context = 어디서/무엇으로 할 수 있는지

**기본 제공 태그**:
| 태그 | 아이콘 | 설명 |
|------|--------|------|
| @컴퓨터 | 💻 | PC/노트북 필요 |
| @스마트폰 | 📱 | 폰으로 가능 |
| @집 | 🏠 | 집에서만 가능 |
| @사무실 | 🏢 | 사무실에서 가능 |
| @외출중 | 🚶 | 외출 시 가능 |
| @전화 | 📞 | 통화 필요 |
| @사람이름 | 👤 | 특정 사람과 함께 |
| @집중 | 🎯 | 깊은 집중 필요 |
| @짬날때 | ⏰ | 짧은 시간에 가능 |

**커스텀 태그**: 사용자가 직접 생성/색상 지정

### 생활 영역 (Life Areas)

```kotlin
enum class LifeArea(
    val label: String,
    val icon: String,
    val primaryAbility: AbilityType,
    val color: Color
) {
    WORK("업무", "🏢", INT, Color(0xFF2196F3)),
    HEALTH("건강", "💪", CON, Color(0xFF4CAF50)),
    LEARNING("학습", "📚", INT, Color(0xFF9C27B0)),
    RELATIONSHIP("관계", "👥", CHA, Color(0xFFE91E63)),
    FINANCE("재정", "💰", WIS, Color(0xFFFF9800)),
    PERSONAL("개인", "✨", WIS, Color(0xFF00BCD4)),
    CREATIVE("창작", "🎨", CHA, Color(0xFFFF5722))
}
```

---

## 3.4 ORGANIZE (정리)

### 정리 목록 유형

| 목록 | 내용 | 화면 |
|------|------|------|
| 다음 액션 목록 | 즉시 실행 가능한 퀘스트 | QuestBoard - 활성 탭 |
| 프로젝트 목록 | 2단계 이상 필요한 목표 | QuestBoard - 프로젝트 탭 |
| 대기중 목록 | 위임하거나 타인을 기다리는 것 | QuestBoard - 대기중 탭 |
| 언젠가/아마도 | 지금 당장은 아닌 것 | QuestBoard - 언젠가 탭 |
| 참고자료 | 실행 불필요, 보관용 | 설정 내 참고자료 |
| 캘린더 | 특정 날짜/시간에 해야 하는 것 | (v2) 캘린더 연동 |

### 프로젝트 정의

GTD에서 프로젝트 = **두 개 이상의 단계가 필요한 결과물**

```kotlin
data class Project(
    val id: String,
    val title: String,
    val desiredOutcome: String,    // "이 프로젝트가 완료되면 어떤 상태인가?"
    val nextAction: Task?,         // 현재 다음 행동
    val tasks: List<Task>,
    val status: ProjectStatus,
    val challengeRating: Float,    // 전체 CR (최고 하위 퀘스트 CR 기준)
    val lifeArea: LifeArea,
    val dueDate: Long?,
    val campaign: String?          // D&D 캠페인명 (사용자 자유 입력)
)
```

**프로젝트 CR = max(하위 태스크 CR들) × 1.2 (보스 보너스)**

### 우선순위 결정 방식

QuestLog는 전통적인 P1/P2/P3 우선순위 대신 **CR과 마감일 조합**으로 자동 정렬:

```
정렬 기준 (내림차순):
1. 마감 D-0 (오늘): 무조건 최상위
2. 마감 D-1 이내: CR 높은 것 우선
3. 나머지: CR × (긴급도 보정) 점수순

긴급도 보정:
  오늘 마감: ×3.0
  내일 마감: ×2.0
  3일 내:   ×1.5
  1주 내:   ×1.2
  이후:     ×1.0
```

---

## 3.5 REFLECT (검토)

### Daily Review (자동, 매일 아침 08:00 알림)

```
알림 내용:
"오늘의 퀘스트 3개가 기다리고 있습니다!
 긴급: 세금 신고 (CR 8, 마감 D-1)
 [앱 열기]"
```

WorkManager로 구현:
- 매일 08:00 알림
- 오늘 마감 퀘스트 + 추천 퀘스트 3개 표시

### Weekly Review (매주 토요일 10:00 알림)

**6단계 체크리스트**:
```
Step 1: 인박스 비우기
  - 미처리 Inbox 항목 모두 처리
  - 완료 조건: InboxCount == 0

Step 2: 프로젝트 목록 검토
  - 각 프로젝트의 다음 액션이 정의되어 있는가?
  - 진행이 멈춘 프로젝트가 있는가?

Step 3: 대기중 항목 팔로우업
  - 위임 후 7일 이상 경과한 항목 확인
  - 팔로우업 퀘스트 생성 여부 결정

Step 4: 언젠가/아마도 검토
  - 지금 활성화할 것이 있는가?
  - 영구 삭제할 것이 있는가?

Step 5: 이번 주 돌아보기
  - 통계 자동 표시 (완료 수, XP, 크리티컬)
  - 메모 자유 입력

Step 6: 다음 주 준비
  - Claude AI 요약 + 전략 제안
  - 다음 주 주요 퀘스트 확인
```

**완료 보상**: +200 XP + "현명한 DM" 칭호 (주 연속 리뷰 시 스택)

### Monthly Review (매월 1일, 선택적)

- 월간 XP 추이, 생활 영역 분포 차트
- 완료된 프로젝트 목록
- Claude AI 월간 영웅 보고서

---

## 3.6 ENGAGE (실행)

### 퀘스트 선택 기준 (책의 "Choosing Your Battles" 반영)

사용자가 퀘스트를 선택할 때 고려 요소:

1. **맥락 (@Context)**: 현재 있는 장소/도구에 맞는 것
2. **시간**: 현재 가용 시간에 맞는 CR
3. **에너지 (HP)**: HP가 낮을 때는 저CR 퀘스트 추천
4. **우선순위**: 마감과 중요도

**스마트 추천 엔진** (v1.5):
```kotlin
fun recommendNextQuest(
    currentContext: String,
    availableMinutes: Int,
    currentHpPercentage: Float,
    character: Character
): List<Task> {
    return allActiveTasks
        .filter { it.context == currentContext || it.context == null }
        .filter { it.estimatedMinutes <= availableMinutes }
        .filter { 
            if (currentHpPercentage < 0.3f) it.challengeRating <= 3f 
            else true 
        }
        .sortedByDescending { it.urgencyScore }
        .take(3)
}
```

### 집중 모드 (Monk Class 특성 연동)

```
집중 모드 활성화 시:
- 알림 완전 차단 (DND 모드 연동)
- 화면 상단에 퀘스트 제목 + 타이머
- 완료 버튼만 표시 (미니멀 UI)
- Monk 클래스: 기본 25분 → 30분 확장

타이머 완료 시:
- 진동 알림
- "집중 완료! 퀘스트를 완료했나요?" 확인
- 완료 → D20RollSheet 트리거
```

### 2분 규칙 구현

Clarify 단계에서 "2분 이내 가능" 선택 시:
```
1. Task를 생성하지 않음 (Inbox에도 남기지 않음)
2. QuickDoneLog에만 기록 (완료 기록, XP는 소량)
3. +10 XP (CR 0 보너스)
4. "빠른 처리!" 애니메이션 (0.5초)
```

### 루틴/반복 퀘스트

```kotlin
data class RecurringRule(
    val frequency: RecurringFrequency,  // DAILY, WEEKLY, MONTHLY, CUSTOM
    val daysOfWeek: Set<DayOfWeek>?,    // WEEKLY인 경우
    val dayOfMonth: Int?,               // MONTHLY인 경우
    val time: LocalTime?,               // 시간 지정 시
    val endDate: Long?                  // 종료일 (없으면 무기한)
)
```

루틴 퀘스트의 CR:
- 첫 번째: 정상 CR
- 반복될수록: CR × (0.7^연속완료횟수) 하한 CR 1/4
- 7일 연속 달성 시: "마스터 루틴" 배지 + CR 리셋
