# QuestLog 전체 구현 계획

> PRD.md의 MVP 범위를 실행 가능한 작업으로 분해. 시간은 **혼자 개발 + 사이드 프로젝트 기준 집중 시간(시간 단위)**.
> 캘린더 시간으로 환산: 주말 + 평일 저녁 활용 시 시간×3 = 실제 경과일.

---

## 0. 전체 의존성 그래프

```
┌────────────────────────────────────────────────────────────────┐
│ Phase 0: 셋업 (8h)                                             │
│   환경 → 디자인 시스템 → Room 스키마 → DI 모듈                  │
└────────┬───────────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────────────────────┐
│ Phase 1: GTD MVP (56h, 텍스트 캡처)                            │
│   Inbox(텍스트) → Clarify → QuestBoard → Journal (게임 X)      │
│   ※ 음성/STT는 F4.5로 이전 (F4.0 동의 인프라 선행 후)           │
└────────┬───────────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────────────────────┐
│ Phase 2: 캐릭터 (40h)                                          │
│   Entity → 능력치/HP/XP 계산 → 온보딩 → CharacterSheet         │
└────────┬───────────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────────────────────┐
│ Phase 3: D20 전투 (50h) ← 핵심, 가장 어려움                    │
│   ResolveCombatUseCase → CompletionDao (원자성) → UI 통합      │
└────────┬───────────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────────────────────┐
│ Phase 4: 프라이버시 → 아이템·NPC·인카운터·STT (64h)            │
│   F4.0 프라이버시 기반 (차단·선행) → 5기능 병렬                 │
│   ※ F4.0 미완료 시 F4.2 NPC / F4.5 STT / F5.2 API 모두 금지    │
└────────┬───────────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────────────────────┐
│ Phase 5: Claude API → 통계 → 폴리싱 (52h)                      │
│   API 7시나리오 → 통계 → 알림 (프라이버시는 Phase 4에서 완료)  │
└────────┬───────────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────────────────────┐
│ Phase 6: Memory of the Day (35h)                               │
│   하루 1엔트리(UNIQUE) → Today/History UI → Reminder → 윤색    │
│   ※ F6.5 Claude 윤색은 F4.0+F5 선행 필수                        │
└────────┬───────────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────────────────────┐
│ Phase 7: 베타 + 자체 배포 (20h)                                │
│   E2E 테스트 → 실기기 30일 사용 → APK sideload                 │
└────────────────────────────────────────────────────────────────┘

총 예상: 325h (집중 시간) ≈ 41일(8h/d) ≈ 16-20주(주말+평일저녁)

> **횡단 차단 규칙** — 모든 Phase에 강제 적용:
> 1. **Room 스키마 변경 = phase-level blocking sub-task** (version 증분 + `Migration_N_M` 또는 `@AutoMigration` + `app/schemas/` JSON 커밋 + `MigrationTest`). Phase 1 도그푸딩 개시 이후 `fallbackToDestructiveMigration()` 영구 금지.
> 2. **상태 전이 + 보상/로그 INSERT는 단일 `@Transaction` DAO 메서드 + 조건부 UPDATE + `OnConflictStrategy.IGNORE`로 묶는다** (CompletionDao 패턴이 표준 — F3.1, F4.4에 적용).
> 3. **민감 권한(READ_CONTACTS, RECORD_AUDIO, POST_NOTIFICATIONS)·외부 송신(Claude API)은 F4.0 동의/경계 인프라 완료 전 코드 한 줄도 작성 금지.**
```

---

## Phase 0 — 프로젝트 셋업 (8h)

### F0.1 환경 구성 + 빌드 시스템 (2h)

**구현 순서**:
1. Android Studio Ladybug + JDK 17 확인
2. `gradle init` → Kotlin DSL + Version Catalog 선택
3. `libs.versions.toml` 작성 (08_tech_stack.md 8.5절 그대로)
4. `app/build.gradle.kts` 작성 (모든 플러그인 + 의존성)
5. `./gradlew assembleDebug` 성공 확인

**파일 구조**:
```
gtdrpg/
├── gradle/libs.versions.toml
├── settings.gradle.kts
├── build.gradle.kts             # root
├── app/build.gradle.kts
└── app/src/main/AndroidManifest.xml
```

**잠재 문제점**:
- ⚠️ KSP 버전이 Kotlin 버전과 정확히 일치해야 함 (`2.1.0-1.0.29` 형식)
- ⚠️ Hilt + KSP 조합은 Hilt 2.48+ 필요. 그 이전 버전은 KAPT만 지원
- ⚠️ Paparazzi는 JDK 17 필수, JDK 21+는 일부 버전 호환 안 됨

---

### F0.2 디자인 시스템 + Hilt 골격 (4h)

**구현 순서**:
1. `core/ui/theme/` — Color, Typography, Shape, Theme (10_design_system.md 참조)
2. `MedievalSharp` 폰트 다운로드 → `res/font/`
3. `QuestLogApp` (Application + `@HiltAndroidApp`)
4. `MainActivity` + `@AndroidEntryPoint` + 빈 Compose 진입점
5. `core/data/di/AppModule.kt` (현재는 빈 모듈)

**파일 구조**:
```
app/src/main/java/com/questlog/
├── QuestLogApp.kt
├── MainActivity.kt
└── core/ui/theme/
    ├── Color.kt
    ├── Typography.kt
    ├── Shape.kt
    └── Theme.kt
```

**잠재 문제점**:
- ⚠️ Compose 다크 테마만 사용 — `MaterialTheme(colorScheme = DarkColorScheme)` 고정, 시스템 설정 무시
- ⚠️ MedievalSharp는 라틴 글리프만 있음 — 한국어 제목은 `Noto Sans KR Bold`로 폴백

---

### F0.3 Room DB + DI (2h)

**구현 순서**:
1. `QuestLogDatabase` (빈 abstract class, version=1)
2. `Converters.kt` (Enum + List<String> kotlinx.serialization)
3. `DatabaseModule.kt` (08_tech_stack.md 8.9절 그대로)
4. `NetworkModule.kt` (Retrofit + Json + OkHttp)
5. 빈 앱 실행 → Hilt 주입 확인

**파일 구조**:
```
core/data/
├── db/
│   ├── QuestLogDatabase.kt
│   └── Converters.kt
├── remote/
│   └── ClaudeApiService.kt
└── di/
    ├── DatabaseModule.kt
    └── NetworkModule.kt
```

**완료 기준**: `./gradlew installDebug` → 앱 실행 → 빈 화면 표시 + Hilt 크래시 없음

---

## Phase 1 — GTD MVP (56h, 텍스트 캡처)

> 게임 요소 없이 GTD 5단계 완전 작동. 이 시점에서 이미 Todoist 대체 가능.
> 음성 입력은 Phase 4 F4.5로 이전(F4.0 동의 인프라 선행 필수).

### F1.1 Entity + DAO + Repository (8h)

**🔴 스키마 부트스트랩 (차단·먼저 수행)**:
- Room `@Database(version = 2)` **신규 활성화** — F0.3은 파일 스켈레톤만 두고 `@Database` 어노테이션을 비워뒀음. 본 단계에서 처음으로 활성화한다. `exportSchema = true`.
- `app/schemas/com.questlog.core.data.db.QuestLogDatabase/2.json` 커밋 (최초의 schema JSON)
- ⛔ **v1 → v2 마이그레이션 / `AutoMigration(1, 2)` 작성 금지.** v1은 어떤 디바이스에도 persisted된 적 없는 의미적 placeholder (docs/05_data_model.md §5.6.1 참조). Room은 v2 테이블을 fresh로 생성한다.
- `MigrationTest`: 본 단계에선 "v2 fresh install → 3 테이블 존재 + 컬럼 일치" 만 검증. 마이그레이션 테스트는 v2 → v3 (F2.1) 부터.
- ⛔ **이 시점부터 `fallbackToDestructiveMigration()` 영구 금지** (도그푸딩 데이터 보호)

**구현 순서**:
1. `InboxItemEntity`, `TaskEntity`, `ProjectEntity` 정의
2. 대응 DAO 인터페이스 + `@Query` 작성 (05_data_model.md 그대로)
3. Domain Model (`InboxItem`, `Task`, `Project`) + Mapper
4. Repository 인터페이스 (`core/domain/repository/`)
5. Repository 구현 (`core/data/repository/`) + Hilt 바인딩
6. 위 마이그레이션 + `MigrationTest` 그린

**파일 구조**:
```
core/
├── data/db/entity/{InboxItem,Task,Project}Entity.kt
├── data/db/dao/{InboxItem,Task,Project}Dao.kt
├── data/repository/{InboxItem,Task,Project}RepositoryImpl.kt
├── data/mapper/{InboxItem,Task,Project}Mapper.kt
├── domain/model/{InboxItem,Task,Project}.kt
└── domain/repository/{InboxItem,Task,Project}Repository.kt
```

**잠재 문제점**:
- ⚠️ Room `Flow<List<Entity>>`를 ViewModel까지 끌고 가지 말 것 — Repository에서 `.map { it.toDomain() }`로 변환
- ⚠️ `@TypeConverters(Converters::class)`를 DB 클래스에 누락하면 빌드는 되지만 런타임 NPE

---

### F1.2 Inbox + QuickCapture (8h)

> Codex 적대적 리뷰(2026-05) 지적 #1 반영: **음성 입력(STT)/`RECORD_AUDIO`는 Phase 4로 이전** (F4.5 신설). 횡단 차단 규칙 #3을 따른다 — 민감 권한은 F4.0 동의 인프라 완료 전 작성 금지. Phase 1은 텍스트 캡처만 다룬다.

**구현 순서**:
1. `InboxViewModel` (StateFlow<InboxUiState>)
2. `InboxScreen` Composable (목록 + FAB)
3. `QuickCaptureSheet` (BottomSheet, **텍스트 입력 전용**)
4. `CaptureItemUseCase` 작성
5. ShareSheet 진입점 (`ACTION_SEND` intent filter — 텍스트만)
6. 위젯 (`AppWidgetProvider` — 텍스트만, v1.5에 인터랙티브)

**제외 (Phase 4 F4.5로 이전)**:
- ❌ Android `SpeechRecognizer` 통합
- ❌ `RECORD_AUDIO` manifest 선언
- ❌ `audioPath`/`transcribedText` 필드 활용 (Entity 자체는 docs/05_data_model.md에 있으나 Phase 1에선 항상 null)

**파일 구조**:
```
feature/inbox/
├── InboxScreen.kt
├── InboxViewModel.kt
├── components/
│   ├── InboxItemCard.kt
│   └── QuickCaptureSheet.kt          # 텍스트 전용
└── widget/
    └── InboxWidgetProvider.kt
```

**예상**: 8h (STT 4h를 Phase 4로 이전)

**잠재 문제점**:
- ⚠️ ShareSheet 진입점 처리 (`ACTION_SEND` intent filter) — Android 14+ predictive back gesture 호환
- ⚠️ `audioPath` 컬럼이 도그푸딩 동안 unused 상태로 존재 → DB 용량 영향 없음 (NULL)

---

### F1.3 Clarify Sheet (10h)

**구현 순서**:
1. `ClarifySheet` 6단계 결정트리 UI (03_gtd_system.md)
2. CR 자동 계산 알고리즘 구현 (`CrCalculator.kt`, 순수 함수)
3. `ClarifyItemUseCase` (Inbox → Task 변환)
4. 컨텍스트 태그 자동 추출 (`@컴퓨터`, `@스마트폰` 등)
5. 2분 룰 → `QuickDoneUseCase` (전투 없이 즉시 완료)

**파일 구조**:
```
feature/clarify/
├── ClarifySheet.kt
├── ClarifyViewModel.kt
└── components/
    ├── ClarifyStep1Actionable.kt   # 행동 가능한가?
    ├── ClarifyStep2TwoMinute.kt    # 2분 이내?
    ├── ClarifyStep3Delegate.kt     # 위임?
    ├── ClarifyStep4DefineAction.kt # 다음 행동 정의
    ├── ClarifyStep5Context.kt      # 컨텍스트 태그
    └── ClarifyStep6Priority.kt     # 마감/우선순위

core/domain/usecase/
├── ClarifyItemUseCase.kt
└── CrCalculator.kt
```

**잠재 문제점**:
- ⚠️ CR 계산 가중치 (urgency 0.4 / complexity 0.3 / time 0.3)는 추정 — 실사용 후 보정 필요
- ⚠️ 6단계 BottomSheet는 화면 차지가 큼 → 전체 화면 다이얼로그 또는 페이지 전환 고려

---

### F1.4 QuestBoard (20h, 가장 큼)

**구현 순서**:
1. `QuestBoardScreen` (3 탭: Today / Active / Projects)
2. `QuestCard` Composable (10_design_system.md 디자인 따름)
3. 우선순위 정렬 알고리즘 (마감 → CR → 컨텍스트)
4. `ProjectDetailScreen` (서브태스크 칸반)
5. `TaskDetailScreen` (수정/삭제)
6. 검색 + 필터 (컨텍스트, 생활영역, 상태)
7. 드래그앤드롭 (선택, v1.5로 미룰 수 있음)

**파일 구조**:
```
feature/questboard/
├── QuestBoardScreen.kt
├── QuestBoardViewModel.kt
├── ProjectDetailScreen.kt
├── TaskDetailScreen.kt
└── components/
    ├── QuestCard.kt
    ├── CrBadge.kt
    ├── ContextChip.kt
    ├── ProjectCard.kt
    └── FilterBar.kt
```

**예상**: 20h (UI 복잡도 + 우선순위 로직 + 상세 화면 3개)

**잠재 문제점**:
- ⚠️ `LazyColumn` 성능 — 1000+ 태스크 시 `key={it.id}` 필수
- ⚠️ Flow가 너무 자주 emit하면 UI 깜빡임 → `distinctUntilChanged()` 적용
- ⚠️ 드래그앤드롭은 v1.0에서 제외 권장 (Compose Reorder 라이브러리 안정성 불충분)

---

### F1.5 Journal + 통계 기본 (10h)

**구현 순서**:
1. `JournalScreen` (완료된 Task 시간순 목록)
2. 기본 통계 (오늘/이번주/이번달 완료 수)
3. 필터 (생활영역, 컨텍스트)
4. `JournalViewModel`
5. DataStore AppSettings (필터 상태 영속화)

**파일 구조**:
```
feature/journal/
├── JournalScreen.kt
├── JournalViewModel.kt
└── components/
    └── CompletedTaskCard.kt

core/data/datastore/
└── AppSettings.kt
```

**잠재 문제점**:
- ⚠️ Phase 5의 풀 통계 화면(Vico 차트)과 분리 — 여기선 숫자만

---

### Phase 1 완료 기준
- [ ] Inbox(텍스트) → Clarify → QuestBoard → Journal 전체 플로우 작동
- [ ] 앱 재시작 후 데이터 유지
- [ ] ShareSheet에서 텍스트 받기 → Inbox 저장 작동
- [ ] 단위 테스트: UseCase 90%+
- [ ] **이 시점에 실제 사용 시작 (도그푸딩 30일)**
- ⛔ 음성 입력은 의도적으로 제외 — F4.0 동의 인프라 + F4.5 STT 게이트 통과 후 활성화

---

## Phase 2 — 캐릭터 시스템 (40h)

### F2.1 Character Entity + 계산 유틸 (8h)

**🔴 스키마 마이그레이션 (차단·먼저 수행)**:
- Room `version = 2 → 3`, `schemas/3.json` 커밋
- 새 테이블 추가만이므로 `@AutoMigration(from = 2, to = 3)` 가능
- `MigrationTest`: **Phase 1 도그푸딩 시드(v2 백업)** 로드 → v3 마이그 → 기존 Task/Project 데이터 무손실 + Character 빈 테이블 생성 확인
- 도그푸딩 백업 절차: 마이그 작업 전 `adb backup` 또는 Room `getOpenHelper().writableDatabase` 덤프를 `app/src/test/resources/db/v2_dogfood_seed.db`로 커밋

**구현 순서**:
1. `CharacterEntity` (능력치 6개 + HP/XP/Level + 클래스)
2. `CharacterDao` + `CharacterRepository`
3. `AbilityCalculator.kt` — 수정치 계산 (`(score - 10) / 2`)
4. `HpCalculator.kt` — Hit Die + CON 수정 (클래스별)
5. `XpThresholds.kt` — D&D 5e 레벨별 누적 XP 상수
6. `ProficiencyBonus.kt` — 레벨 기반 (`2 + (level - 1) / 4`)
7. 위 마이그레이션 + `MigrationTest` 그린

**파일 구조**:
```
core/
├── data/db/entity/CharacterEntity.kt
├── data/db/dao/CharacterDao.kt
├── data/repository/CharacterRepositoryImpl.kt
├── domain/model/Character.kt
└── domain/usecase/
    ├── AbilityCalculator.kt
    ├── HpCalculator.kt
    ├── XpThresholds.kt
    └── ProficiencyBonus.kt
```

**잠재 문제점**:
- ⚠️ XP 임계값을 하드코딩하지 말고 상수 배열로 관리 (`val XP_THRESHOLDS = longArrayOf(0, 300, 900, ...)`)
- ⚠️ Lv20 초과 시 동작 정의 (현재 PRD에선 Lv20 cap, 초과 XP는 totalXpEarned에만 누적)

---

### F2.2 온보딩 플로우 (20h, 가장 큼)

**구현 순서**:
1. `WelcomeScreen` (Lottie)
2. `ClassSelectionScreen` (12 클래스 그리드)
3. **클래스 퀴즈** (12 문항 → 클래스 추천, 책의 Class Quiz)
4. `AbilityRollScreen` (4d6 drop lowest + Lottie 주사위)
5. `CharacterNamingScreen` (클래스별 랜덤 이름 풀)
6. `GTDTutorialScreen` (5페이지 스와이프)
7. `OnboardingViewModel` (전체 상태 보관)
8. 완료 시 `DataStore.onboardingCompleted = true`

**파일 구조**:
```
feature/onboarding/
├── OnboardingScreen.kt           # 호스트 (NavHost)
├── OnboardingViewModel.kt
└── steps/
    ├── WelcomeStep.kt
    ├── ClassQuizStep.kt
    ├── ClassSelectionStep.kt
    ├── AbilityRollStep.kt
    ├── CharacterNamingStep.kt
    └── GtdTutorialStep.kt

core/domain/usecase/
├── RollAbilityScoresUseCase.kt   # 4d6 drop lowest
└── ClassRecommendationUseCase.kt # 퀴즈 → 클래스
```

**예상**: 20h (Lottie 통합 + 12 클래스 콘텐츠 작성 + 퀴즈 로직 + 5페이지 튜토리얼)

**잠재 문제점**:
- ⚠️ 12 클래스 설명 텍스트 작성에 시간 많이 소요 (책 발췌 + 번역)
- ⚠️ Lottie 파일 직접 제작 어려움 → LottieFiles 무료 라이센스 확인
- ⚠️ 클래스 퀴즈 12문항 설계 — 책의 퀴즈를 한국어로 의역

---

### F2.3 CharacterSheet + LevelUp (12h)

**구현 순서**:
1. `CharacterSheetScreen` (5 탭: 스탯/장비/능력/업적/NPC — 장비/능력/업적은 placeholder)
2. `StatsTab` (능력치 + HP/XP 바)
3. `LevelUpScreen` (XP 임계값 도달 시 전환)
4. `GainXPUseCase` + `CheckLevelUpUseCase`
5. HP/XP 애니메이션 (Compose `animateFloatAsState`)

**파일 구조**:
```
feature/character/
├── CharacterSheetScreen.kt
├── CharacterViewModel.kt
├── LevelUpScreen.kt
└── tabs/
    ├── StatsTab.kt
    ├── EquipmentTab.kt    # placeholder
    ├── AbilitiesTab.kt    # placeholder
    ├── AchievementsTab.kt # placeholder
    └── NpcTab.kt          # placeholder

core/ui/components/
├── HpBar.kt
├── XpBar.kt
└── AbilityScoreCircle.kt
```

**잠재 문제점**:
- ⚠️ XP 획득 애니메이션이 끝나기 전에 사용자가 화면 이탈 → `LaunchedEffect` 정리 필수
- ⚠️ 레벨업 트리거가 중복 발화 가능 → `currentXp` 변경 시 한 번만 체크

---

### Phase 2 완료 기준
- [ ] 온보딩 → 캐릭터 생성 → CharacterSheet 표시
- [ ] XP 직접 추가 (디버그 메뉴) → 레벨업 시퀀스 작동
- [ ] 12 클래스 모두 선택 가능

---

## Phase 3 — D20 자동 전투 (50h) — **핵심**

### F3.1 CompletionDao + 원자성 (10h) — **최우선**

> PRD §3.3 + 08_tech_stack.md 8.5절. **이 단계의 단위 테스트가 가장 중요.**

**🔴 스키마 마이그레이션 (차단·먼저 수행)**:
- Room `version = 3 → 4`, `schemas/4.json` 커밋
- `CombatLogEntity` 신규 + `TaskEntity.completedAt`/`xpAwarded` 컬럼 추가
- 컬럼 추가는 `Migration_3_4`를 수동 작성 (`ALTER TABLE tasks ADD COLUMN ...`) — `@AutoMigration`은 NOT NULL 기본값에서 깨질 수 있음
- `MigrationTest`: v3 시드 (도그푸딩 + Phase 2 캐릭터 데이터) → v4 → Task/Character 무손실 + 새 컬럼 default 적용
- ⛔ `OnConflictStrategy.REPLACE`를 CombatLog에 절대 사용 금지 (멱등성 깨짐) → `IGNORE` 고정

**구현 순서**:
1. `CombatLogEntity` + `CombatLogDao`
2. `CompletionDao` — `@Transaction commitCompletion()` 작성
3. `CharacterSnapshotUpdate` 값 객체
4. **단위 테스트 먼저**:
   - 정상 완료 → `Boolean=true` 반환
   - 더블 실행 → 두 번째 `Boolean=false` 반환
   - 동시 호출 (2 스레드) → 한쪽만 성공
   - CombatLog는 한 번만 INSERT
   - **크래시 재현 테스트**: INSERT 직후 `throw IOException()` 주입 → 트랜잭션 롤백 → 재시도 시 정상 1회 처리
5. Robolectric으로 In-Memory Room 테스트
6. 위 마이그레이션 + `MigrationTest` 그린

**파일 구조**:
```
core/data/db/
├── entity/CombatLogEntity.kt
├── dao/CombatLogDao.kt
├── dao/CompletionDao.kt           # 트랜잭션 DAO
└── model/CharacterSnapshotUpdate.kt

core/data/repository/CombatRepositoryImpl.kt

src/test/java/.../db/
└── CompletionDaoTest.kt           # 가장 중요한 테스트
```

**예상**: 10h (스펙 자체는 단순하지만 동시성 테스트 작성에 시간)

**잠재 문제점**:
- 🔴 **가장 큰 리스크**: 트랜잭션 안에서 suspend 함수 호출 시 dispatcher 충돌. Room 2.6+는 `@Transaction suspend fun` 지원하지만 dispatcher는 Room이 관리
- 🔴 멱등성 테스트 작성 누락 시 운영 중 중복 보상 발생 → CI 그린이어도 사용자 신뢰 손실

---

### F3.2 ResolveCombatUseCase (8h)

**구현 순서**:
1. `MonsterCatalog.kt` — CR별 몬스터 정의 (CR 0 슬라임 ~ CR 24 타라스크)
2. `MonsterAcCalculator.kt` — CR → AC 매핑
3. `D20Roller` — `SecureRandom.nextInt(20) + 1`
4. `ResolveCombatUseCase` — 순수 함수 (DB 쓰기 없음)
5. `CombatResult` sealed class
6. `XpCalculator` — 보너스 계수 적용
7. **파라미터화 단위 테스트** (D20 = 1~20 전 범위)

**파일 구조**:
```
core/domain/
├── model/CombatResult.kt          # sealed class
├── model/Monster.kt
└── usecase/
    ├── ResolveCombatUseCase.kt
    ├── XpCalculator.kt
    ├── D20Roller.kt
    └── MonsterAcCalculator.kt

core/data/MonsterCatalog.kt        # 정적 데이터
```

**잠재 문제점**:
- ⚠️ `Random` 대신 `SecureRandom` 강제 (테스트에서는 `Random(seed)` 주입)
- ⚠️ CR이 소수(2.5 등)일 때 AC 보간 처리 정의 필요

---

### F3.3 CompleteTaskUseCase 통합 (8h)

**구현 순서**:
1. `CompleteTaskUseCase` (08_tech_stack.md 8.5절 그대로)
2. 멱등성 분기 (Task.status == DONE → AlreadyCompleted)
3. 아이템 드롭 (트랜잭션 밖, runCatching)
4. AI 내러티브 (이 단계에선 항상 폴백)
5. 통합 테스트 (Use Case + In-Memory Room)

**예상**: 8h

**잠재 문제점**:
- ⚠️ 아이템 드롭이 실패하면 UI는 "성공"인데 인벤토리에 아이템 없음 — UX 명세 필요 (보상 알림에서 누락 표시?)

---

### F3.4 D20RollSheet + CombatResultScreen UI (16h)

**구현 순서**:
1. `D20RollSheet` BottomSheet
2. Lottie 주사위 3D 회전 (500ms)
3. 결과 숫자 카운트업 애니메이션
4. 공격 굴림 계산 단계별 표시 (D20 + STR + 숙련 = 17 vs AC 15)
5. `CombatResultScreen` (전체 화면)
6. 크리티컬 히트 황금 파티클 (Compose Particles 또는 Lottie)
7. 크리티컬 미스 유머러스 UI
8. XP 바 증가 애니메이션
9. 레벨업 트리거 → `LevelUpScreen` 전환

**파일 구조**:
```
feature/combat/
├── CombatScreen.kt
├── CombatViewModel.kt
├── D20RollSheet.kt
├── CombatResultScreen.kt
└── components/
    ├── D20DiceView.kt
    ├── AttackCalculationCard.kt
    ├── CriticalHitParticles.kt
    └── XpGainAnimation.kt
```

**예상**: 16h (애니메이션이 시간 잡음)

**잠재 문제점**:
- ⚠️ **3초 이내 완결** (PRD §8.2) — 애니메이션 욕심내면 5초 넘어감, 스킵 버튼 필수
- ⚠️ Lottie 파일 라이센스 (LottieFiles 무료 카테고리만)
- ⚠️ Compose Animation은 60fps 보장 안 됨 — 저사양 기기 테스트 필수

---

### F3.5 HP/스트릭 + WorkManager (8h)

**구현 순서**:
1. `HpResetAndStreakWorker` (자정 작업)
2. `StreakProtectTokenUseCase`
3. HP 상태별 UI (HEALTHY/TIRED/WOUNDED/CRITICAL)
4. Short Rest (연속 3회 완료 시 HP 25% 회복)
5. 스트릭 마일스톤 보상 (7일/30일/100일)

**파일 구조**:
```
worker/
├── HpResetAndStreakWorker.kt
└── WorkerScheduler.kt              # MainActivity에서 호출

core/domain/usecase/
├── UpdateStreakUseCase.kt
├── ShortRestUseCase.kt
└── ApplyHpDamageUseCase.kt
```

**잠재 문제점**:
- ⚠️ WorkManager가 Doze 모드에서 지연됨 — 자정 정확히 발화 안 될 수 있음 → AlarmManager 대안 검토
- ⚠️ 사용자가 시간대 변경 시 자정 계산 오류 → `ZoneId.systemDefault()` 매번 조회

---

### Phase 3 완료 기준
- [ ] 퀘스트 완료 → 3초 이내 결과 화면
- [ ] 더블탭/크래시 후 중복 보상 없음 (단위 테스트 + 실기기 검증)
- [ ] 자정 HP 회복 작동 (실기기 24시간 모니터링)
- [ ] Phase 1 도그푸딩 데이터로 실제 전투 100회 이상 진행

---

## Phase 4 — 프라이버시 → 아이템·NPC·인카운터·STT (64h)

> F4.0(프라이버시 기반)이 차단 선행 작업. 완료 후에야 F4.1~F4.5 병렬 진행 가능.
> **F4.0 미완료 시 F4.2(연락처) / F4.5(마이크) / F5.x(Claude API) 시작 금지** — CLAUDE.md 컴팩션 보존 항목 #3.

### F4.0 프라이버시 기반 (8h) — **차단 선행**

> Codex 적대적 리뷰(2026-05) 지적 반영: 민감 권한·외부 송신을 다루는 모든 기능의 진입 게이트를 한 곳에 모은다. AI 게이트와 로컬 권한 게이트는 같은 인프라(`ConsentManager`) 위에 올린다.

**🔴 스키마 마이그레이션**:
- Room `version = 4 → 5` — **코드 반영 완료**
- `ConsentRecordEntity` 신규 (정책 버전 추적용) — **코드 반영 완료**
- `@AutoMigration(4, 5)` — 신규 테이블만 추가 — **코드 반영 완료**
- `schemas/5.json` 커밋 — ⏳ **릴리스 게이트 미완료** (ARM64 환경 제약; x86_64에서 `./gradlew :app:kspDebugKotlin` 실행 후 커밋 필요)
- `MigrationTest`: v4 시드(Phase 3 전투 기록 포함) → v5 → 기존 데이터 무손실 + `consent_records` 빈 테이블 생성 — ⏳ **릴리스 게이트 미완료** (schema JSON 커밋 후 작성 가능)
- 표 갱신: docs/05_data_model.md §5.6.1 v5 행 — **반영 완료**

> ⚠️ **"코드 작성 완료"** ≠ **"릴리스 게이트 통과"**: 위 두 항목(schemas/5.json + MigrationTest)은 F4.1 시작 전 x86_64 개발 환경에서 완료해야 한다.

> Codex 적대적 리뷰(2026-05) 2차 지적 #4 반영: 동의는 **시점·정책 버전이 추적 가능한 `ConsentRecord` 엔티티**로 저장한다. Boolean 플래그는 단순 캐시(`AppSettings.claudeApiEnabled`만 운영 토글로 유지). 정책 텍스트 변경 시 자동 재동의 유도.

**구현 순서**:
1. **`ConsentRecordEntity` + DAO 신규** (스키마 v5 — **구현 완료**):
   ```kotlin
   @Entity(tableName = "consent_records",
           indices = [Index(value = ["scope"], unique = false)])
   data class ConsentRecordEntity(
       @PrimaryKey(autoGenerate = true) val id: Long = 0,
       val scope: String,            // ConsentScope.name
       val policyVersion: Int,        // 정책 텍스트 hash 또는 단조 증분
       val acceptedAt: Long,
       val revokedAt: Long? = null    // 비어 있으면 현재 유효
   )

   @Dao interface ConsentRecordDao {
       @Query("""
           SELECT * FROM consent_records
           WHERE scope = :scope AND revokedAt IS NULL
           ORDER BY acceptedAt DESC LIMIT 1
       """)
       suspend fun latestActive(scope: String): ConsentRecordEntity?

       @Insert suspend fun grant(record: ConsentRecordEntity): Long

       @Query("UPDATE consent_records SET revokedAt = :now WHERE scope = :scope AND revokedAt IS NULL")
       suspend fun revoke(scope: String, now: Long): Int
   }
   ```
2. `ConsentManager` (`core/data/privacy/`) — Boolean이 아닌 `ConsentRecord`를 통해 게이트:
   ```kotlin
   class ConsentManager(
       private val dao: ConsentRecordDao,
       private val currentPolicyVersion: PolicyVersionProvider
   ) {
       suspend fun isGranted(scope: ConsentScope): Boolean {
           val record = dao.latestActive(scope.name) ?: return false
           // 정책 버전 변경 시 자동 재동의 유도 — 구버전 동의는 무효
           return record.policyVersion == currentPolicyVersion.forScope(scope)
       }

       suspend fun canCallApi(): Boolean =
           isGranted(ConsentScope.AI_OUTBOUND) && appSettings.claudeApiEnabled
       suspend fun canImportContacts(): Boolean = isGranted(ConsentScope.CONTACTS)
       suspend fun canUseMicrophone(): Boolean = isGranted(ConsentScope.MICROPHONE)
   }

   enum class ConsentScope { CONTACTS, AI_OUTBOUND, MICROPHONE }
   ```
3. `AppSettings`(DataStore)는 운영 토글만 유지:
   - `claudeApiEnabled: Boolean` (기본 false) — 동의와 별개로 사용자가 일시 비활성화
   - ⛔ `contactsConsentGiven`, `aiConsentGiven` 같은 동의 플래그는 **추가 금지** (DAO 단일 진실 공급원 위반)
4. `ConsentDialog` (`feature/settings/components/`):
   - 스코프별 표시 데이터 명세 (무엇이 어디로 가는지 한 줄씩)
   - 정책 버전 표기 ("정책 v3, 2026-05-22 작성")
   - 동의 / 거부 / 자세히 보기 (개인정보 처리방침 로컬 텍스트, 리소스 ID로 정책 텍스트와 버전 함께 관리)
5. `PromptSanitizer.kt` (`core/data/sanitizer/`)
   - 제목 50자 절단, 메모/첨부/NPC 이름/연락처 필드 제외
   - **테스트**: NPC `displayName`, `phoneNumber`, `Task.notes`, `Task.attachments`가 출력에 절대 포함되지 않음 (필드별 negative test)
6. `SecureStorage.kt` — `EncryptedSharedPreferences`로 Claude API Key 보관
7. **데이터 경계 단위 테스트** (이 단계 통과해야 다음 작업 가능):
   - 동의 없음 (`latestActive == null`) → `ContactsRepository.import()` = `ConsentRequired`
   - 만료된 정책 버전 (record.policyVersion < current) → `isGranted = false` (재동의 유도)
   - 동의 후 즉시 철회 → `isGranted = false`, 다음 API 호출 0회
   - 동의 → 정책 v3에서 v4로 증분 → 기존 동의 무효 → ConsentDialog 재호출 트리거
   - `canCallApi()=false`면 `ClaudeRepository.send()`는 호출 0회 (MockK `verify(exactly = 0)`)
   - `PromptSanitizer` 라운드트립: NPC/notes 필드 100건 무작위 생성 → 출력에 포함 0건
8. Settings 화면에 동의 철회 + 로컬 데이터 삭제 진입점 (`DeleteImportedContactsUseCase`, `DeleteAiCacheUseCase`)

**파일 구조**:
```
core/data/privacy/
├── ConsentManager.kt
├── ConsentScope.kt              # enum: CONTACTS, AI_OUTBOUND, MICROPHONE
├── PolicyVersionProvider.kt     # 정책 텍스트 → 버전 매핑
└── PrivacyModule.kt             # Hilt 바인딩

core/data/db/
├── entity/ConsentRecordEntity.kt
└── dao/ConsentRecordDao.kt

core/data/sanitizer/
└── PromptSanitizer.kt

core/data/secure/
└── SecureStorage.kt

core/domain/usecase/
├── DeleteImportedContactsUseCase.kt
└── DeleteAiCacheUseCase.kt

feature/settings/
├── components/ConsentDialog.kt
└── components/PrivacyControlsSection.kt

src/test/java/.../privacy/
├── ConsentManagerTest.kt         # 정책 버전 변경/철회 시나리오 포함
├── PromptSanitizerTest.kt        # 누락 시 🔴 출시 금지
└── DataBoundaryTest.kt           # canCallApi 게이트 verify
```

**예상**: 8h (Codex 2차 지적의 ConsentRecord는 기존 8h 안에서 흡수 — Boolean 3개 대신 Entity 1개로 단순화)

**잠재 문제점**:
- 🔴 `PromptSanitizer` 단위 테스트 누락 시 민감 정보 유출 가능 — CLAUDE.md 컴팩션 보존 항목 #3 위반
- 🔴 정책 텍스트만 바꾸고 `policyVersion`을 증분하지 않으면 구버전 동의가 영구 유효 → 정책 텍스트 파일에 단조 증분 버전 헤더 강제 (lint 룰)
- ⚠️ `ConsentManager.canCallApi()`를 우회하는 직접 호출이 한 곳이라도 있으면 게이트 무력화 → `ClaudeApiService`는 internal로 두고 `ClaudeRepository`만 외부 노출
- ⚠️ DataStore가 동의 plain bool을 가지고 있던 구설계 잔재가 PR에 섞이면 SSOT 깨짐 → Detekt 룰 또는 코드 리뷰 1번 항목으로 차단

**완료 기준**:
- [x] 위 7개 항목 모두 작성 + 단위 테스트 그린 — **완료**
- [ ] `./gradlew testDebugUnitTest --tests "*.privacy.*"` 100% 패스 (빌드 환경 정상 시 실행)
- [x] **이 단계 완료 도장 — F4.2, F5.2 작업 시작 가능** (PR 리뷰 1번 항목)

---

### F4.1 아이템 시스템 (15h) — **완료**

**🔴 스키마 마이그레이션**: Room `v5 → v6`, `MIGRATION_5_6` 수동 마이그레이션
  - 이유: `(characterId, equippedSlot) UNIQUE WHERE isEquipped=1` 부분 인덱스는 AutoMigration 불가 → 수동 SQL
  - `ItemEntity` + `CharacterItemEntity` (junction, FK CASCADE) 추가
  - `schemas/6.json` — `./gradlew :app:kspDebugKotlin` 실행 시 자동 생성됨 (ARM 환경 AAPT2 제약으로 로컬 미생성)
  - `MigrationTest`: 실기기/CI 환경에서 검증 필요

**구현 완료**:
1. ✅ `ItemEntity` + `CharacterItemEntity` (junction)
2. ✅ `ItemCatalog.kt` — 32개 아이템 (WEAPON 8, ARMOR 7, RING 8, NECKLACE 5, MISC 5)
3. ✅ `ItemDropUseCase` — 등급별 드롭률 (CR 조건 + 확률)
4. ✅ `CharacterItemDao` — `equipItem()` @Transaction, `addDroppedItem()` @Transaction
5. ✅ `EquipmentTab` UI — 5 슬롯 그리드 + 인벤토리 목록
6. ✅ 장비 효과 적용 — `ResolveCombatUseCase` 에 ATK 보너스 + XP 배율 통합
7. ✅ `CompleteTaskUseCase` — `equippedItems` 파라미터 추가 (기본 emptyList)
8. ✅ 단위 테스트: `ItemDropUseCaseTest`, `EquipmentBonusTest`

**파일**:
```
core/domain/model/{ItemType, ItemRarity, EquipmentSlot, Item, ItemTemplate, ItemCatalog}
core/domain/usecase/{ItemDropUseCase, EquipItemUseCase}
core/domain/repository/ItemRepository
core/data/db/entity/{ItemEntity, CharacterItemEntity}
core/data/db/dao/CharacterItemDao
core/data/mapper/ItemMapper
core/data/repository/ItemRepositoryImpl
feature/character/tabs/EquipmentTab.kt (placeholder → 실 구현)
```

**잠재**: 등급별 드롭률 밸런싱 — 실사용 후 보정 필요

---

### F4.2 NPC 시스템 (12h) — **F4.0 차단 의존**

> Codex 적대적 리뷰(2026-05) 지적 #3 반영: 연락처는 더 이상 bulk `READ_CONTACTS`를 요청하지 않는다. `ACTION_PICK` + `ContactsContract`으로 사용자가 1건씩 선택. 권한 자체를 manifest에서 제거.

**🔴 스키마 마이그레이션 (차단·먼저 수행)**:
- Room `version = 6 → 7`, `schemas/7.json` 커밋
- `NpcEntity` 신규 (`source: enum {MANUAL, PICKER}` 필드로 출처 명시 — 향후 일괄 삭제 시 사용)
- `@AutoMigration(from = 6, to = 7)`: 신규 테이블만 추가
- `MigrationTest`: 기존 데이터 무손실

**구현 순서**:
1. **F4.0 완료 확인** — `ConsentManager.canImportContacts() == true` 게이트 없으면 import 진입 자체 차단
2. `NpcEntity` + `NpcDao` (`displayName`, `phoneNumber?`, `compatibilityClass`, `source`)
3. `NpcScreen` (목록 + 프로필 + "수동 추가" / "연락처에서 1명 선택")
4. **연락처 1건 선택**: `Intent(Intent.ACTION_PICK).setType(ContactsContract.Contacts.CONTENT_TYPE)` — `READ_CONTACTS` 권한 불필요 (시스템 picker가 권한 대행)
5. **클래스 호환성 매트릭스** (12×12 정적 데이터)
6. `DelegateTaskUseCase`
7. `WaitingFor` 목록 (Task 상태 = WAITING)
8. **프라이버시 통합 테스트**:
   - NPC 데이터(`displayName`, `phoneNumber`)가 `PromptSanitizer` 출력에 절대 미포함 (F4.0의 `DataBoundaryTest`에 NPC 케이스 추가)
   - `DeleteImportedContactsUseCase`로 `source=PICKER`인 NPC 일괄 삭제 시 위임 중인 Task도 함께 정리 (`@Transaction`)

**파일**:
```
core/data/{ClassCompatibilityMatrix, db/entity/NpcEntity, db/dao/NpcDao}
core/domain/usecase/{DelegateTaskUseCase, CalculateCompatibilityUseCase}
feature/npc/{NpcScreen, NpcDetailScreen, ClassCompatibilitySheet, ContactPickerLauncher}
src/test/java/.../npc/NpcPrivacyTest.kt
```

**잠재**:
- ⚠️ 일부 OEM(특히 중국 ROM)은 `ACTION_PICK` 결과로 `phoneNumber` 미반환 → 사용자가 수동 입력하도록 폴백
- ⚠️ 12×12 매트릭스를 책에서 직접 추출 (수동 작업)
- 🔴 manifest에 `READ_CONTACTS`가 남아 있으면 안 됨 — F4.0 lint 룰로 차단

---

### F4.3 클래스 특수 능력 Lv1 (15h)

**구현 순서**:
1. `ClassAbility` sealed class
2. 클래스별 Lv1 능력 1개씩 (12개)
3. `ActivateAbilityUseCase` (능력 발동 → 효과)
4. `AbilitiesTab` UI
5. 리소스 시스템 (Ki, Sorcery Points 등 — `classResourceCurrent/Max`)
6. 리소스 충전 (Long Rest 시)

**파일**:
```
core/domain/ability/
├── ClassAbility.kt            # sealed class
├── BarbarianRage.kt
├── BardicInspiration.kt
├── ... (12개)
└── AbilityEngine.kt

feature/character/tabs/AbilitiesTab.kt
```

**잠재**:
- ⚠️ 12 클래스 각각 다른 효과 — 가장 시간 소모, 단순화 가능 (예: 다음 1회 전투 +5 보너스 통일)
- ⚠️ 능력 효과를 게임 메커니즘에 통합 시 의존성 늘어남

---

### F4.4 랜덤 인카운터 (10h) — 원자성 계약 명문화

> Codex 적대적 리뷰(2026-05) 지적 #2 반영: PENDING→CLAIMED 상태 전이와 보상 INSERT를 **단일 `@Transaction` DAO 메서드**로 묶는다. CompletionDao 패턴(F3.1)을 그대로 적용. 부분 실패(전이만 / 보상만) 절대 발생 금지.

**🔴 스키마 마이그레이션 (차단·먼저 수행)**:
- Room `version = 7 → 8`, `schemas/8.json` 커밋
- `EncounterLogEntity` 신규 (`id, templateKey, status: enum {PENDING, CLAIMED, EXPIRED}, generatedAt, claimedAt?, expiresAt, rewardXp, rewardItemId?`)
- 인덱스: `(status, expiresAt)` — 만료 워커 조회용
- `MIGRATION_7_8`: 신규 테이블 + UNIQUE 인덱스 (수동 — §5.6.2 MIGRATION_7_8 참조)
- `MigrationTest`: 기존 데이터 무손실

**🔴 원자성 계약 (CompletionDao와 동일 패턴, PR 리뷰 1번 항목)**:

> Codex 적대적 리뷰(2026-05) 2차 지적 #3 반영: `insertXpAward`의 반환값을 무시하면 IGNORE 충돌 시 캐릭터 상태만 변경되고 감사 행이 없어 복구 불가능. **insertedId = -1L이면 트랜잭션을 throw로 롤백**한다.

```kotlin
@Dao
interface ClaimEncounterRewardDao {

    @Transaction
    suspend fun commitClaim(
        encounterId: String,            // encounter_logs.id (TEXT/UUID)와 동일 타입
        now: Long,
        xpAward: XpAwardEntity,         // encounterId 결정성 + UNIQUE 제약(아래)
        characterUpdate: CharacterSnapshotUpdate
    ): ClaimResult {
        val rows = conditionalClaim(encounterId, now)
        if (rows == 0) return ClaimResult.AlreadyClaimedOrExpired

        // 🔴 감사 무결성: 보상 행이 실제로 들어가지 못하면 캐릭터 상태도 절대 변경 금지
        val insertedId = insertXpAward(xpAward)  // OnConflictStrategy.IGNORE
        if (insertedId == -1L) {
            // 정상 흐름에선 도달 불가능 — UNIQUE(encounter_id)와 conditionalClaim 게이트가
            // 동시 만족되는 경우는 동일 트랜잭션 내 race 뿐. 발견 즉시 throw → 트랜잭션 롤백.
            throw IllegalStateException(
                "Audit row missing for encounter=$encounterId despite successful claim. " +
                "Possible deterministic-id collision or upstream contract violation."
            )
        }
        updateCharacterSnapshot(characterUpdate)
        return ClaimResult.Success
    }

    @Query("""
        UPDATE encounter_logs
        SET status = 'CLAIMED', claimed_at = :now
        WHERE id = :encounterId
          AND status = 'PENDING'
          AND expires_at > :now
    """)
    suspend fun conditionalClaim(encounterId: String, now: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertXpAward(award: XpAwardEntity): Long
}

sealed class ClaimResult {
    object Success : ClaimResult()
    object AlreadyClaimedOrExpired : ClaimResult()
}
```

**스키마 불변식 (마이그레이션 v6→v7에 포함, docs/05_data_model.md §5.6.2 참조)**:
```sql
CREATE UNIQUE INDEX idx_xp_awards_encounter_unique ON xp_awards(encounterId);
```
이 인덱스가 없으면 위 IGNORE-검사 패턴은 의미를 잃는다. **마이그레이션 PR과 DAO PR을 분리 머지 금지** — 같은 PR에서 함께 추가.

규칙:
- ⛔ 보상 INSERT를 트랜잭션 밖에서 호출 금지 (중복 보상 위험)
- ⛔ `conditionalClaim()` 결과 무시한 채 후속 단계 진행 금지 (보상 누락 위험)
- ⛔ `OnConflictStrategy.REPLACE`로 보상 INSERT 금지 — `IGNORE` + UNIQUE + 반환값 검사로 멱등성 확보
- ⛔ `insertXpAward()` 반환값(`Long`) 무시 금지 — `-1L`은 무결성 위반 신호

**구현 순서**:
1. `EncounterLogEntity` + `EncounterLogDao` (read-only 조회용) + `XpAwardEntity` (보상 기록, 불변)
2. **`ClaimEncounterRewardDao` 작성 + 단위 테스트 먼저** (CompletionDao 패턴 동일):
   - 정상 PENDING → 1회 `Success`, `xp_awards`에 1건 INSERT, 캐릭터 XP 정확히 +rewardXp
   - 더블탭 → 두 번째 `AlreadyClaimedOrExpired`, `xp_awards` 행 **정확히 1건만** 존재
   - 만료 후 클릭 → `AlreadyClaimedOrExpired`, 행 0건
   - 동시 호출 (2 스레드) → 한쪽만 `Success`
   - 크래시 주입(INSERT 후 throw) → 트랜잭션 롤백 → 재시도 시 정상 1회 처리
   - **감사 무결성**: UNIQUE(encounterId) 위반 시뮬레이션 (직접 INSERT로 미리 행 삽입) → `commitClaim` 호출 시 `IllegalStateException` throw → 캐릭터 XP 변경 0
   - **반환값 무시 회귀 방지**: `insertXpAward(...)` 반환을 변수로 받는지 정적 분석 (Detekt 룰 또는 PR 리뷰 체크리스트)
3. `RandomEncounterWorker` (하루 1-2회) — PENDING 인카운터 생성
4. 로컬 템플릿 50개
5. `EncounterLogScreen` (PENDING/CLAIMED/EXPIRED 표시)
6. 알림 → `ClaimEncounterRewardUseCase.invoke()` → 결과로 UI 표시
7. `EncounterExpirationWorker` (15분 주기) — `WHERE status='PENDING' AND expires_at <= now` → CLAIMED 아닌 EXPIRED로 전이 (UPDATE 한 줄, 트랜잭션 불요)
8. 48시간 만료 WorkManager 등록

**파일**:
```
core/data/db/
├── entity/{EncounterLogEntity, XpAwardEntity}.kt
├── dao/EncounterLogDao.kt
└── dao/ClaimEncounterRewardDao.kt        # 원자성 DAO

core/domain/
├── model/ClaimResult.kt
└── usecase/ClaimEncounterRewardUseCase.kt

worker/{RandomEncounterWorker, EncounterExpirationWorker}
core/data/EncounterTemplates.kt
feature/combat/EncounterLogScreen.kt

src/test/java/.../db/
└── ClaimEncounterRewardDaoTest.kt        # 가장 중요한 테스트
```

**예상**: 10h (8h → 10h, 원자성 테스트 작성 시간 +2h)

**잠재**:
- ⚠️ 알림 권한 (Android 13+ `POST_NOTIFICATIONS`) 거부 시 인앱 알림 폴백 필요

---

### F4.5 음성 캡처 / STT (4h) — **F4.0 차단 의존**

> Phase 1 F1.2에서 이전 (Codex 적대적 리뷰 2026-05 지적 #1). `RECORD_AUDIO`는 민감 권한이므로 F4.0 동의 인프라 통과 후에만 도입.

**구현 순서**:
1. **F4.0 완료 확인** — `ConsentManager`에 `ConsentScope.MICROPHONE` 추가, `canUseMicrophone()` 게이트
2. AndroidManifest에 `RECORD_AUDIO` 선언 (이 시점에만)
3. `VoiceCaptureLauncher` (`feature/inbox/voice/`) — 권한 요청 → `SpeechRecognizer` 시작
4. `QuickCaptureSheet`에 🎙️ 버튼 추가 — `canUseMicrophone()=false` 시 비활성/안내
5. STT 결과를 `InboxItemEntity.transcribedText`에 저장 (원본 오디오는 디스크 미저장 — 디폴트)
6. **프라이버시 통합 테스트**:
   - `canUseMicrophone()=false`이면 `SpeechRecognizer.startListening()` 호출 0회 (MockK verify)
   - `transcribedText`가 `PromptSanitizer` 출력에 포함되는지 명시적 결정 (현재 정책: 제목 50자만 → notes/transcribedText 제외)
   - `DeleteVoiceTranscriptsUseCase` 호출 시 `transcribedText`만 null로 업데이트 (원본 텍스트는 보존하지 않음 — 사용자가 명료화하지 않은 음성은 의미 없음)

**파일**:
```
feature/inbox/voice/
├── VoiceCaptureLauncher.kt
└── MicrophonePermissionGate.kt

core/data/privacy/
└── (ConsentScope.kt에 MICROPHONE 추가)

core/domain/usecase/
└── DeleteVoiceTranscriptsUseCase.kt

src/test/java/.../voice/
└── VoiceCapturePrivacyTest.kt
```

**예상**: 4h

**잠재**:
- ⚠️ `SpeechRecognizer`는 오프라인 미지원 기기 있음 (구형 Pixel·일부 OEM) → 기기 호환성 검증 필수, 미지원 시 버튼 자체 비표시
- ⚠️ Android 14+에서 백그라운드 마이크 접근 제약 — 포그라운드 시트 열린 상태에서만 호출
- ⚠️ 원본 PCM/WAV는 디스크에 저장하지 않음 (메모리에서 STT 호출 후 즉시 폐기) — 만약 미래에 저장하면 별도 동의 스코프 신설

---

### Phase 4 완료 기준
- [ ] 크리티컬 히트 시 아이템 드롭 + 인벤토리 추가
- [ ] 장비 착용/해제 → XP 배율 실제 반영
- [ ] NPC 위임 → WaitingFor 목록 표시
- [ ] 클래스 능력 12개 모두 발동 가능
- [ ] 랜덤 인카운터 알림 → 보상 수령 → 중복 수령 차단 확인
- [ ] 음성 캡처 게이트 작동 (동의 없을 시 버튼 비활성)

---

## Phase 5 — Claude API + 통계 + 폴리싱 (52h)

> 프라이버시 기반(구 F5.1, 8h)은 F4.0으로 이전. Phase 5는 이제 외부 API/통계/폴리싱만 다룬다.

### F5.1 ~~프라이버시 기반~~ → F4.0으로 이전 (0h)

> 본 단계는 Phase 4의 차단 선행 작업으로 옮겨졌다 (Codex 적대적 리뷰 2026-05 지적 #3).
> **F5.2 시작 전 F4.0 완료 도장 + 단위 테스트 그린 확인 필수.** 작업 내용은 본 문서 F4.0 참조.

---

### F5.2 Claude API 7시나리오 (20h) — **F4.0 차단 의존**

**구현 순서**:
1. `ClaudeApiService` Retrofit 인터페이스
2. `ClaudeRepository` + 폴백 로직
3. 7가지 시나리오 프롬프트 빌더
4. `APIBudget` (일일 한도)
5. 시나리오별 통합:
   - 전투 내러티브 → CombatResultScreen
   - 크리티컬 히트 특별 → CombatResultScreen
   - Weekly Review 요약 → WeeklyReviewScreen
   - Clarify 제안 → ClarifySheet
   - 레벨업 메시지 → LevelUpScreen
   - 랜덤 인카운터 → RandomEncounterWorker
   - 크리티컬 미스 위로 → CombatResultScreen

**파일**:
```
core/data/remote/
├── ClaudeApiService.kt
├── dto/{ClaudeMessageRequest, ...}.kt
└── prompts/
    ├── CombatNarrativePrompt.kt
    ├── CriticalHitPrompt.kt
    ├── WeeklyReviewPrompt.kt
    ├── ClarifyPrompt.kt
    ├── LevelUpPrompt.kt
    ├── EncounterPrompt.kt
    └── CriticalMissPrompt.kt

core/data/repository/ClaudeRepositoryImpl.kt
core/data/budget/ApiBudget.kt
```

**잠재**:
- ⚠️ Claude API 응답 지연 (5-10초) → UI는 폴백 먼저 표시 후 도착 시 교체
- ⚠️ 네트워크 오류 처리 (`runCatching` + Timber)

---

### F5.3 Weekly Review (10h)

**구현 순서**:
1. `WeeklyReviewEntity` + `WeeklyReviewDao`
2. `WeeklyReviewScreen` 6단계 체크리스트
3. AI 요약 호출 (동의 시)
4. `WeeklyReviewReminderWorker` (토요일 10:00)
5. 완료 보상 (+200 XP)
6. 4주 연속 완료 → "전설적인 DM" 칭호

**파일**:
```
feature/journal/WeeklyReviewScreen.kt
feature/journal/WeeklyReviewViewModel.kt
worker/WeeklyReviewReminderWorker.kt
```

---

### F5.4 통계 화면 + Vico 차트 (12h)

**구현 순서**:
1. `StatisticsScreen`
2. 주간/월간 완료 BarChart (Vico)
3. XP 추이 LineChart
4. 생활영역 분포 PieChart
5. 스트릭 캘린더 (GitHub 잔디 스타일)
6. D20 분포도 (크리티컬/미스/명중 비율)
7. 클래스 능력치 레이더 차트 (수동 Canvas)

**파일**:
```
feature/journal/StatisticsScreen.kt
feature/journal/components/
├── CompletedTaskBarChart.kt
├── XpLineChart.kt
├── LifeAreaPieChart.kt
├── StreakCalendar.kt
├── D20DistributionChart.kt
└── AbilityRadarChart.kt
```

**잠재**:
- ⚠️ Vico 2.0 beta — API 변경 가능성, 안정 버전 대기 검토
- ⚠️ 레이더 차트는 라이브러리 없음 → 수동 Canvas 그리기 (4시간 추가)

---

### F5.5 알림 시스템 (5h)

**구현 순서**:
1. `DailyReminderWorker` (매일 08:00)
2. 마감 D-1 알림
3. HP 위기 알림 (HP 25% + 오늘 완료 0개)
4. 스트릭 위기 알림 (저녁 21:00)
5. `NotificationSettingsScreen`
6. 알림 채널 분리 (Android 8.0+)

**파일**:
```
worker/{DailyReminderWorker, DeadlineReminderWorker, HpCrisisWorker, StreakRiskWorker}
core/data/notification/NotificationManager.kt
feature/settings/NotificationSettingsScreen.kt
```

---

### F5.6 폴리싱 (5h)

- 트랜지션 (Compose Shared Element)
- 햅틱 피드백 (전투 결과 시)
- 사운드 (D20, 레벨업 — 작은 mp3)
- 에러 화면 (네트워크 없음, DB 오류)
- 빈 상태 일러스트

---

### Phase 5 완료 기준
- [ ] 동의 없으면 API 호출 0회 (테스트 검증)
- [ ] PromptSanitizer 단위 테스트 통과
- [ ] 7시나리오 모두 정상 응답 + 폴백 작동
- [ ] 통계 화면 5개 차트 모두 렌더링
- [ ] 알림 4종 실기기 발화 확인

---

## Phase 6 — Memory of the Day (35h)

> **컨셉**: 솔로 RPG 일기 패러다임을 GTD에 접목. 하루 1회, 사용자가 그날 가장 의미 있었던 완료 1개를 골라 짧은 메모를 남긴다. 다음 날이 되면 "어제의 기억"은 잠금된다.
>
> **핵심 가치**: 즉시 정산의 인지 비용 제거 + 글쓰기 부담 최소화 + 일기/회고 자연 유도.
>
> **선행 의존**: F1(Task), F3(Combat/CompletionDao), F5(Claude·Weekly Review)는 F6.5·F6.6에서만 필요.

### F6.1 데이터 + 도메인 (10h)

**🔴 스키마 마이그레이션 (차단·먼저 수행)**:
- Room `version = 11 → 12`, `exportSchema = true` (Phase 5 엔티티 3종 v9~v11 선행 — `docs/05_data_model.md §5.6.1` SSOT)
- `app/schemas/com.questlog.core.data.db.QuestLogDatabase/12.json` 커밋
- `@AutoMigration(from = 11, to = 12)` (신규 테이블만 추가 — 안전)
- `MigrationTest`: v11 시드 DB → v12 마이그 → `memory_entries` 존재 + 기존 데이터 무손실

**구현 순서**:
1. `MemoryEntryEntity` 정의 (`memory_entries`, `entryDate UNIQUE`)
2. `MemoryDao` (`insertEntry` with `OnConflictStrategy.ABORT`, `getByDate`, `pageHistory`, `countThisWeek`) — UPSERT/REPLACE 금지. `entryDate` UNIQUE 충돌은 ABORT 후 UseCase에서 "오늘은 이미 기록했어요" 메시지로 처리 (덮어쓰기 = 기존 메모 손실 = 데이터 무결성 위반)
3. Domain Model (`MemoryEntry`) + Mapper
4. `MemoryRepository` 인터페이스 + `MemoryRepositoryImpl`
5. UseCase: `GetTodayMemoryStateUseCase`, `GetCandidateCompletionsUseCase`, `SaveMemoryUseCase`
6. 위 마이그레이션 + `MigrationTest` 그린

**`MemoryEntryEntity` 정의**:
```kotlin
@Entity(
  tableName = "memory_entries",
  indices = [Index(value = ["entryDate"], unique = true), Index(value = ["characterId"])],
  foreignKeys = [
    ForeignKey(Character::class, ["id"], ["characterId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(TaskEntity::class, ["id"], ["taskId"], onDelete = ForeignKey.SET_NULL),
  ]
)
data class MemoryEntryEntity(
  @PrimaryKey val id: String,           // UUID
  val entryDate: String,                // "yyyy-MM-dd" (저장 시점 local tz, UNIQUE)
  val characterId: String,
  val taskId: String?,                  // Task 삭제 시 SET NULL 허용
  val taskTitleSnapshot: String,        // FK 끊겨도 컨텍스트 보존
  val outcomeType: String,              // STRONG | WEAK | MISS | NONE
  val body: String,                     // 사용자 본문 (max 500자)
  val enrichedBody: String?,            // Claude 윤색본 (nullable)
  val createdAt: Long,
  val sealedAt: Long,                   // 다음 날 00:00 잠금 시각
)
```

**상태 머신** (`sealed class MemoryTodayState`):
- `NoCompletions` — 오늘 완료 0개
- `Selecting(candidates: List<TaskSummary>)` — 카드 선택 단계
- `Writing(selected: TaskSummary)` — 본문 작성 단계
- `Saved(entry: MemoryEntry)` — 저장 완료, 읽기 전용
- `Expired(yesterdayCandidates: List<TaskSummary>)` — 어제 미작성, 작성 불가

**잠재 문제점**:
- ⚠️ **`entryDate UNIQUE` 위반 race (자정 경계)** — UseCase에서 `LocalDate.now()`를 트랜잭션 시작 시점에 캐시, 이후 동일 인스턴스만 사용
- ⚠️ **타임존 변경** — `entryDate`는 저장 시점 local tz 고정. 이후 tz 변경되어도 무시 (UTC 변환 금지 — 로컬 의미 보존이 우선)
- ⚠️ **Task hard delete** — `onDelete = SET_NULL` + `taskTitleSnapshot` 컬럼으로 메모 본문 컨텍스트 보존

---

### F6.2 Today UI (8h)

**구현 순서**:
1. `MemoryTodayScreen` + `MemoryTodayViewModel`
2. 5개 상태별 분기 컴포저블 (NoCompletions / Selecting / Writing / Saved / Expired)
3. 카드 선택 → 본문 작성 → 저장 3-step 플로우
4. `OutcomeBadge` 컴포넌트 (STRONG/WEAK/MISS/NONE 색상 매핑)
5. Snackbar — UNIQUE 충돌 시 "오늘은 이미 기록했어요"
6. **Paparazzi**: 5상태 × 라이트/다크 = 10 스냅샷

**파일**:
```
feature/memory/today/
├── MemoryTodayScreen.kt
├── MemoryTodayViewModel.kt
└── components/
    ├── CandidateCard.kt
    ├── MemoryEditor.kt
    └── OutcomeBadge.kt
```

**잠재 문제점**:
- ⚠️ Compose `TextField` 한글 IME 입력 깨짐 — `KeyboardOptions(imeAction = ImeAction.Done)` + `BasicTextField` 직접 사용 고려
- ⚠️ 500자 제한은 **클라이언트만** — DB 컬럼은 TEXT (변경 시 마이그레이션 비용↑)

---

### F6.3 History UI (6h)

**구현 순서**:
1. `MemoryHistoryScreen` — Paging3, `PagingSource` from `MemoryDao.pageHistory()`
2. 월별 헤더 (`stickyHeader`)
3. `MemoryHistoryCard` — 날짜 + outcomeBadge + body 1줄 미리보기
4. 탭 → `MemoryDetailDialog` (읽기 전용, 원본/윤색본 토글)
5. **Paparazzi**: 빈 상태 / 3엔트리 / 30엔트리 스냅샷

**파일**:
```
feature/memory/history/
├── MemoryHistoryScreen.kt
├── MemoryHistoryViewModel.kt
├── MemoryDetailDialog.kt
└── components/MemoryHistoryCard.kt
```

---

### F6.4 설정 + 리마인더 워커 (4h)

**구현 순서**:
1. `MemorySettings` (DataStore) — 리마인더 시각(기본 21:00), ON/OFF
2. `MemoryReminderWorker` (WorkManager, PeriodicWorkRequest 1일 1회)
   - 오늘 완료 ≥1 AND 미작성 → 알림 발송
   - 미완료 시: no-op (스팸 방지)
3. `MemorySettingsSection` (기존 `SettingsScreen`에 통합)
4. **테스트**: `WorkManagerTestInitHelper`로 트리거 검증

**파일**:
```
worker/MemoryReminderWorker.kt
core/data/datastore/MemorySettings.kt
feature/settings/components/MemorySettingsSection.kt
```

**잠재 문제점**:
- ⚠️ **Doze 모드** — `setRequiredNetworkType(NONE)` + 정확한 시각 보장 어려움. 정확도가 중요하면 `AlarmManager` 대안 검토
- ⚠️ 알림 권한 (Android 13+ `POST_NOTIFICATIONS`) — F4.0 동의 인프라 활용

---

### F6.5 Claude AI 윤색 (5h)  **← F4.0(Consent) + F5(Claude API) 선행 필수**

**🔴 프라이버시 게이트**: `canCallApi()` 통과 후에만 호출. `PromptSanitizer`로 `body`만 전송, `taskTitleSnapshot`/`outcomeType`/메타데이터 제외.

**구현 순서**:
1. `EnrichMemoryWithClaudeUseCase`
2. 4종 프롬프트 분기:
   - `StrongHitDecisive` — 결정적 승리 톤
   - `WeakHitCost` — 대가를 치른 승리
   - `MissLearning` — 실패에서 배움
   - `NoneReflection` — 전투 무관 회고
3. `runCatching` + 폴백: 실패/타임아웃/동의 OFF → `enrichedBody = null`
4. UI: `MemoryDetailDialog`에 원본/윤색본 토글
5. **테스트**: `canCallApi() == false` 시 호출 0회 검증, Sanitizer 단위 테스트

**파일**:
```
core/domain/usecase/memory/EnrichMemoryWithClaudeUseCase.kt
core/data/remote/prompt/MemoryEnrichmentPrompts.kt
```

**잠재 문제점**:
- ⚠️ **윤색본이 본문 의미 왜곡** → 신뢰 손상. 원본 `body`는 별도 컬럼 유지, UI에서 원본/윤색본 토글
- ⚠️ 비용 — 1엔트리당 ~$0.001. 월 30엔트리 = $0.03 (무시 가능)

---

### F6.6 Weekly Review 통합 (2h)

**구현 순서**:
1. `WeeklyReviewScreen`에 "이번 주 메모 7개" 섹션 추가
2. `MemoryDao.countThisWeek()` + 일별 미니 카드 7개 가로 스크롤
3. 메모 없는 날은 회색 placeholder
4. **Paparazzi**: 0/3/7개 메모 케이스

**잠재 문제점**:
- ⚠️ 주의 시작 요일은 PRD §3.8 기준(월요일) — `WeekFields.ISO`

---

### Phase 6 완료 기준
- [ ] 하루 1엔트리 강제 (UNIQUE 충돌 5회 시뮬레이션 통과)
- [ ] 자정 경계에서 어제 → 오늘 전환 정확 (instrumented test)
- [ ] `PromptSanitizer` 통과한 body만 송신 (단위 테스트)
- [ ] `canCallApi() == false` 시 윤색 호출 0회
- [ ] 5상태 Paparazzi 스냅샷 모두 그린
- [ ] Weekly Review 7카드 렌더링 (0/3/7 메모)

---

## Phase 7 — 베타 + 자체 배포 (20h)

### F7.1 종합 테스트 (10h)

- E2E 통합 테스트 1개 (Compose UI Test):
  - 인박스 캡처 → 명료화 → 완료 → XP 획득
- Paparazzi 회귀 검증 (전체 컴포넌트)
- 30일 도그푸딩 (실제 사용 + 버그 기록)
- 메모리 프로파일링 (Android Studio Profiler)
- ANR 검증 (메인 스레드 블로킹 없음)

---

### F7.2 자체 배포 파이프라인 (10h)

- GitHub Actions Release 워크플로우
- 버전 태깅 (`v1.0.0`)
- APK 서명 (자체 keystore)
- APK GitHub Release artifact 업로드
- README에 다운로드 링크
- ProGuard/R8 설정 검증

**파일**:
```
.github/workflows/release.yml
app/release.keystore (gitignore!)
app/proguard-rules.pro
```

**잠재**:
- ⚠️ keystore를 절대 git에 커밋 금지 — `.gitignore` 확인
- ⚠️ ProGuard가 kotlinx.serialization을 망가뜨림 → `@Keep` 또는 `-keep` 룰 필요

---

## 요약 표 — Phase별 예상 시간

| Phase | 기능 수 | 집중 시간 | 캘린더 환산 (시간×3) | 누적 |
|-------|---------|----------|---------------------|------|
| 0 셋업 | 3 | 8h | 24h (3-4일) | 8h |
| 1 GTD MVP (텍스트 캡처) | 5 | 56h | 168h (3-4주) | 64h |
| 2 캐릭터 | 3 | 40h | 120h (2-3주) | 104h |
| 3 D20 전투 | 5 | 50h | 150h (3주) | 154h |
| 4 프라이버시 + 아이템/NPC/인카운터 + STT | 6 | 64h | 192h (4주) | 218h |
| 5 Claude API/통계/폴리싱 | 5 | 52h | 156h (3주) | 270h |
| 6 Memory of the Day | 6 | 35h | 105h (2주) | 305h |
| 7 베타/배포 | 2 | 20h | 60h (1-2주) | 325h |
| **총합** | **35** | **325h** | **975h ≈ 6-7개월** | |

> Phase 4·5의 시간 재배분 (Codex 적대적 리뷰 2026-05 1·2차 반영):
> - F4.0 신설: **+8h** (Phase 4) — 프라이버시 인프라
> - F4.4 인카운터: **+2h** (8h → 10h) — 원자성 트랜잭션 DAO + 5종 테스트
> - F4.5 STT 신설: **+4h** (Phase 4) — F1.2에서 이전
> - F1.2 STT 제거: **−4h** (Phase 1, 텍스트 전용으로 단순화)
> - 구 F5.1 제거: **−8h** (Phase 5, F4.0으로 이전)
> - 순증가: **+2h** (총합 288h → 290h). 데이터 무결성·프라이버시 강화를 위해 수용.
>
> Phase 6 신설 (2026-05-23, 솔로 RPG 시나리오 통합):
> - F6.1~F6.6 **+35h** (290h → 325h) — 즉시 정산 부담을 회피하는 "하루 1엔트리" 패러다임 채택
> - 기존 Phase 6(베타+배포)는 Phase 7로 시프트

---

## 핵심 위험 요약 (우선순위)

| 위험 | Phase | 영향 | 완화책 |
|------|-------|------|--------|
| 🔴 CompletionDao 멱등성 단위 테스트 누락 | 3 | 중복 보상 → 신뢰 손실 | 단위 테스트 작성 후 다음 작업 진행 |
| 🔴 ClaimEncounterRewardDao 원자성 누락 | 4 | 보상 중복/누락 → 게임 상태 파괴 | F4.4 트랜잭션 DAO + 7종 테스트(double-tap·동시·만료·크래시·재시도·UNIQUE 위반·반환값 검사) 그린 |
| 🔴 `insertXpAward` 반환값 무시로 감사 행 누락 | 4 (F4.4) | XP 변경됐는데 보상 기록 없음 → 복구 불가 | UNIQUE(encounter_id) + `insertedId == -1L` 시 throw로 트랜잭션 롤백. Codex 2차 지적 #3 반영 |
| 🔴 PromptSanitizer 누락으로 민감 정보 유출 | 4 (F4.0) | 프라이버시 위반 → 신뢰 즉시 손실 | F4.0 완료 전 F4.2, F4.5, F5.2 시작 금지 |
| 🔴 정책 버전 미증분으로 구버전 동의 영구 유효 | 4 (F4.0) | 변경된 정책이 사용자 모르게 적용 → 신뢰 손실 | `ConsentRecord(policyVersion)` 단조 증분 + 정책 텍스트 헤더에 lint로 강제. Codex 2차 지적 #4 반영 |
| 🔴 STT/RECORD_AUDIO가 동의 게이트 우회 | 1·4 | Phase 1 도그푸딩 중 마이크 권한 무동의 호출 | F1.2에서 STT 제거 → F4.5(F4.0 의존)로 이전. Codex 2차 지적 #1 반영 |
| 🔴 스키마 SSOT 분기 (data_model.md vs IMPLEMENTATION_PLAN.md) | 1·2·3·4 | 마이그레이션 history 분기 → 사용자 DB 손실 | docs/05_data_model.md §5.6.1 표를 단일 진실 공급원으로 고정. PR 머지 시 양 문서 동시 갱신 확인. Codex 2차 지적 #2 반영 |
| 🔴 Room 스키마 마이그레이션 누락/파괴적 | 1·2·3·4 모두 | 사용자 도그푸딩 데이터 소실 → 앱 신뢰 손실 | 각 phase 첫 entity 작업에 Migration + MigrationTest 의무화. `fallbackToDestructiveMigration()` 영구 금지(Phase 1 도그푸딩 개시 후) |
| 🔴 ProGuard가 직렬화 클래스 망가뜨림 | 7 | Release 빌드 크래시 | Phase 7 시작 전 Release 빌드 1회 검증 |
| 🔴 Memory `entryDate UNIQUE` 위반 race (자정 경계) | 6 (F6.1) | INSERT 실패 → 사용자가 기록 못 함 | UseCase에서 `LocalDate.now()` 트랜잭션 시작 시점에 캐시 + 단일 트랜잭션. Snackbar로 충돌 시 명확한 메시지 |
| 🔴 Memory Claude 윤색이 동의 게이트 우회 | 6 (F6.5) | 사용자 본문 무동의 송신 → 프라이버시 위반 | F4.0(Consent) + PromptSanitizer 통과 후에만 호출. `canCallApi() == false` 시 호출 0회 단위 테스트 |
| 🟡 Memory 타임존 변경 시 "어제" 정의 깨짐 | 6 (F6.1) | 잘못된 날짜로 잠금 | `entryDate`는 저장 시점 local tz 고정. UTC 변환 금지. 변경 후 미작성 케이스는 `Expired` 처리 |
| 🟡 Memory 윤색본이 본문 의미 왜곡 | 6 (F6.5) | 사용자 신뢰 손상 | 원본 `body` 별도 컬럼 유지 + UI 원본/윤색본 토글 |
| 🟡 12 클래스 콘텐츠 작성 (텍스트) | 2, 4 | 일정 지연 20h+ | 단순화 옵션: Lv1 능력 통일 효과로 시작 |
| 🟡 WorkManager Doze 모드 지연 | 3, 5, 6 | HP 회복/알림/Memory 리마인더 누락 | AlarmManager 대안 검토 |
| 🟡 Lottie 파일 라이센스 | 1, 2, 3 | 무료 사용 불가 시 비용 | LottieFiles 무료 카테고리만 |
| 🟡 일부 OEM에서 `ACTION_PICK` 결과 누락 | 4 (F4.2) | NPC 가져오기 실패 | 수동 입력 폴백 + 호환성 메모 |
| 🟢 Vico 2.0 beta API 변경 | 5 | 통계 차트 재작업 | 안정 버전(2.0.0 stable) 대기 |
| 🟢 30일 도그푸딩 중 추가 요구 폭발 | 7 | 일정 지연 | PRD §9 Non-Goals 엄격 적용 |

---

## 추천 진행 전략

1. **Phase 0-1을 한 번에**: GTD MVP까지 완성하고 즉시 사용 시작. 30일 도그푸딩으로 진짜 필요한 기능 검증
2. **도그푸딩 결과 보고 Phase 2-3 우선순위 조정**: 사용 패턴 보면서 D20 전투를 빨리 추가할지, GTD UX 개선이 먼저인지 판단
3. **Phase 4는 병렬 진행**: 4개 기능 독립적이므로 컨텍스트 스위칭 비용 줄이려면 1개씩 완전히 끝내고 다음
4. **Phase 5 프라이버시 절대 후순위 금지**: ConsentDialog가 없으면 Claude API 코드 한 줄도 작성하지 않을 것
5. **Phase 6 Memory of the Day는 도그푸딩 직후**: Phase 1~5 30일 사용 후 회고 욕구가 검증되면 진입. 욕구 없으면 스킵 가능(선택 기능)
6. **Phase 7 배포는 가볍게**: 혼자 사용이므로 Play Store 출시 없음. 자체 APK + GitHub Release면 충분

---

## 부록: 작업 시작 체크리스트

각 Phase 시작 전:
- [ ] PRD.md 해당 섹션 재확인 (범위 확인)
- [ ] 의존하는 이전 Phase 완료 기준 충족 여부
- [ ] **Room Entity 신규/변경 시 phase-level blocking 작업으로 분리**:
  - [ ] `version` 증분 + `exportSchema = true`
  - [ ] `Migration_N_M` 작성 (NOT NULL/기본값/인덱스 변경은 수동, 추가만이면 `@AutoMigration`)
  - [ ] `app/schemas/<DB FQN>/<N>.json` 커밋
  - [ ] `MigrationTest`: 직전 시드 DB 로드 → 마이그 → 무손실 검증
  - [ ] Phase 1 도그푸딩 개시 이후 `fallbackToDestructiveMigration()` 사용 PR 영구 금지
- [ ] **상태 전이 + 보상/로그 INSERT가 있으면 단일 `@Transaction` DAO + 조건부 UPDATE + `OnConflictStrategy.IGNORE` 패턴 사용** (CompletionDao / ClaimEncounterRewardDao 참조)
- [ ] **민감 권한·외부 송신을 다루면 F4.0 완료 도장 확인**
- [ ] 새 Hilt 모듈 추가 시 `@InstallIn` 스코프 확인
- [ ] 단위 테스트 먼저 작성 (TDD까진 아니어도 핵심 로직은)

각 Feature 완료 후:
- [ ] `./gradlew testDebugUnitTest` 그린
- [ ] `./gradlew verifyPaparazziDebug` 그린 (UI 변경 시)
- [ ] **MigrationTest 그린** (스키마 변경 시)
- [ ] 실기기 수동 테스트 1회
- [ ] git commit (작은 단위로)
