# 08. 기술 스택

## 8.1 아키텍처 전체 구조

```
┌──────────────────────────────────────────────────────────┐
│                    Presentation Layer                     │
│                                                          │
│  Jetpack Compose UI                                      │
│  ├── Screen Composables (Feature별)                      │
│  ├── Reusable Components (core/ui/components)            │
│  └── Navigation (Compose Navigation)                     │
│                                                          │
│  ViewModel (StateFlow + SharedFlow)                      │
│  ├── UiState (sealed class)                              │
│  ├── UiEvent (one-time events)                           │
│  └── Compose State Hoisting                              │
├──────────────────────────────────────────────────────────┤
│                     Domain Layer                          │
│                                                          │
│  UseCase (단일 책임, 순수 비즈니스 로직)                   │
│  ├── GetInboxItemsUseCase                                │
│  ├── ClarifyItemUseCase                                  │
│  ├── CompleteTaskUseCase (핵심 게임 루프)                 │
│  ├── ResolveCombatUseCase                                │
│  ├── GainXPUseCase                                       │
│  ├── CheckLevelUpUseCase                                 │
│  ├── WeeklyReviewUseCase                                 │
│  └── GenerateNarrativeUseCase                            │
│                                                          │
│  Domain Model (순수 Kotlin data class, 프레임워크 무의존) │
│  Repository Interface (추상화)                            │
├──────────────────────────────────────────────────────────┤
│                      Data Layer                           │
│                                                          │
│  Repository Implementation                               │
│  ├── TaskRepositoryImpl                                  │
│  ├── CharacterRepositoryImpl                             │
│  ├── CombatRepositoryImpl                                │
│  └── ClaudeRepositoryImpl                                │
│                                                          │
│  Local Data Sources:                                     │
│  ├── Room DB (QuestLogDatabase)                          │
│  │   ├── DAO 인터페이스                                  │
│  │   └── Entity 클래스                                   │
│  ├── DataStore Preferences (앱 설정)                     │
│  └── EncryptedSharedPreferences (API Key)                │
│                                                          │
│  Remote Data Sources:                                    │
│  └── Claude API (Retrofit + OkHttp)                      │
│                                                          │
│  Mapper (Entity ↔ Domain Model)                          │
└──────────────────────────────────────────────────────────┘
```

---

## 8.2 모듈 구조

```
:app                     앱 모듈 (Application, MainActivity, NavGraph)
:core
  :core:data             Room DB, DataStore, Retrofit, Repository 구현
  :core:domain           UseCase, 도메인 모델, Repository 인터페이스
  :core:ui               공통 Compose 컴포넌트, 테마, 유틸리티
  :core:testing          테스트 공통 유틸 (FakeRepository 등)
:feature
  :feature:onboarding    온보딩 플로우
  :feature:inbox         Inbox + QuickCapture
  :feature:clarify       ClarifySheet
  :feature:questboard    QuestBoard + 프로젝트 칸반
  :feature:combat        전투 씬 + D20 Roll + 결과
  :feature:journal       저널 + 주간 리뷰 + 통계
  :feature:character     캐릭터 시트 + 레벨업 + 아이템
  :feature:npc           NPC 관리 + 궁합
  :feature:settings      설정 + AI 설정
:worker                  WorkManager 작업
```

**초기 v1.0은 단일 모듈 (:app)으로 시작**, 규모 확장 시 멀티모듈로 전환

---

## 8.3 패키지 구조 (단일 모듈)

```
com.questlog/
├── QuestLogApp.kt              Application 클래스
├── MainActivity.kt
├── NavGraph.kt                 전체 네비게이션 그래프
│
├── core/
│   ├── data/
│   │   ├── db/
│   │   │   ├── QuestLogDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── TaskDao.kt
│   │   │   │   ├── CharacterDao.kt
│   │   │   │   ├── ProjectDao.kt
│   │   │   │   ├── CombatLogDao.kt
│   │   │   │   ├── NpcDao.kt
│   │   │   │   ├── ItemDao.kt
│   │   │   │   ├── WeeklyReviewDao.kt
│   │   │   │   └── AchievementDao.kt
│   │   │   ├── entity/          (Entity 클래스들)
│   │   │   ├── converter/       (TypeConverters)
│   │   │   └── migration/       (DB 마이그레이션)
│   │   ├── remote/
│   │   │   ├── ClaudeApiService.kt
│   │   │   ├── dto/             (Request/Response DTO)
│   │   │   └── interceptor/     (AuthInterceptor, LoggingInterceptor)
│   │   ├── datastore/
│   │   │   ├── AppSettingsDataStore.kt
│   │   │   └── SecureStorage.kt
│   │   ├── repository/          (Repository 구현체)
│   │   └── mapper/              (Entity ↔ Domain 변환)
│   │
│   ├── domain/
│   │   ├── model/               (도메인 모델)
│   │   ├── repository/          (Repository 인터페이스)
│   │   └── usecase/             (UseCase 클래스들)
│   │
│   └── ui/
│       ├── components/          (공통 Compose 컴포넌트)
│       │   ├── QuestCard.kt
│       │   ├── D20DiceView.kt
│       │   ├── HpBar.kt
│       │   ├── XpBar.kt
│       │   ├── CrBadge.kt
│       │   ├── ClassIcon.kt
│       │   └── EmptyState.kt
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Typography.kt
│       │   ├── Shape.kt
│       │   └── Theme.kt
│       └── animation/
│           ├── CombatAnimations.kt
│           └── LevelUpAnimations.kt
│
├── feature/
│   ├── onboarding/
│   │   ├── WelcomeScreen.kt
│   │   ├── ClassSelectionScreen.kt
│   │   ├── AbilityRollScreen.kt
│   │   ├── CharacterNamingScreen.kt
│   │   ├── GTDTutorialScreen.kt
│   │   └── OnboardingViewModel.kt
│   ├── inbox/
│   │   ├── InboxScreen.kt
│   │   ├── InboxViewModel.kt
│   │   ├── QuickCaptureSheet.kt
│   │   └── InboxItemCard.kt
│   ├── clarify/
│   │   ├── ClarifySheet.kt
│   │   └── ClarifyViewModel.kt
│   ├── questboard/
│   │   ├── QuestBoardScreen.kt
│   │   ├── QuestBoardViewModel.kt
│   │   ├── ProjectScreen.kt
│   │   ├── TaskDetailScreen.kt
│   │   └── ProjectDetailScreen.kt
│   ├── combat/
│   │   ├── CombatScreen.kt
│   │   ├── CombatViewModel.kt
│   │   ├── D20RollSheet.kt
│   │   ├── CombatResultScreen.kt
│   │   └── EncounterLogScreen.kt
│   ├── journal/
│   │   ├── JournalScreen.kt
│   │   ├── JournalViewModel.kt
│   │   ├── WeeklyReviewScreen.kt
│   │   └── StatisticsScreen.kt
│   ├── character/
│   │   ├── CharacterSheetScreen.kt
│   │   ├── CharacterViewModel.kt
│   │   ├── LevelUpScreen.kt
│   │   ├── ItemDetailScreen.kt
│   │   └── AchievementScreen.kt
│   ├── npc/
│   │   ├── NpcScreen.kt
│   │   ├── NpcViewModel.kt
│   │   └── ClassCompatibilitySheet.kt
│   └── settings/
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
│
└── worker/
    ├── StreakCheckWorker.kt
    ├── DailyReminderWorker.kt
    ├── WeeklyReviewReminderWorker.kt
    ├── RandomEncounterWorker.kt
    └── HPResetWorker.kt          (자정 HP 리셋)
```

---

## 8.4 확정 기술 스택 결정 사항

### 스택 선택 근거 요약

| 영역 | 선택 | 핵심 이유 |
|------|------|----------|
| UI | Jetpack Compose | 선언형 UI, Kotlin 네이티브, Material3 |
| 아키텍처 | MVVM + Clean Architecture | Jetpack 공식, Domain 레이어 순수 Kotlin → 미래 KMP 확장 가능 |
| DI | Hilt | WorkManager·ViewModel 통합, 컴파일 타임 검증 |
| DB | Room | Flow 네이티브, @Transaction 원자 쓰기, Jetpack 공식 |
| 직렬화 | kotlinx.serialization **단독** | Kotlin null-safe, KMP 대응, Gson 제거 |
| 네트워크 | Retrofit + kotlinx.serialization converter | Gson 컨버터 대체 |
| 내비게이션 | Jetpack Navigation Compose (type-safe routes) | Navigation 2.8+ 타입 안전 라우트 |
| 비동기 | Coroutines + Flow | Android 표준, Room/Retrofit 통합 |
| 테스트 | JUnit 5 + MockK + Turbine + Paparazzi | 파라미터화·Flow·스크린샷 테스트 |
| CI/배포 | GitHub Actions + ADB sideload | 혼자 사용, Play Store 불필요 |

---

## 8.5 의존성 목록 (build.gradle.kts + libs.versions.toml)

### libs.versions.toml

```toml
[versions]
# Kotlin
kotlin = "2.1.0"
kotlinx-coroutines = "1.9.0"
kotlinx-serialization = "1.7.3"
ksp = "2.1.0-1.0.29"

# Android
agp = "8.7.3"
compileSdk = "35"
minSdk = "26"
targetSdk = "35"

# Compose
composeBom = "2024.12.01"
activityCompose = "1.9.3"
navigationCompose = "2.8.5"

# Jetpack
lifecycle = "2.8.7"
room = "2.6.1"
datastore = "1.1.1"
hilt = "2.53.1"
hiltNavigation = "1.2.0"
work = "2.10.0"
securityCrypto = "1.1.0-alpha06"

# Networking
retrofit = "2.11.0"
retrofitKotlinxSerializer = "1.0.0"  # jakewharton/retrofit2-kotlinx-serialization-converter
okhttp = "4.12.0"

# UI 라이브러리
lottie = "6.6.0"
vico = "2.0.0-beta.3"
coil = "2.7.0"

# 로깅
timber = "5.0.1"

# 테스트
junit5 = "5.11.3"
junit5Android = "1.5.2"           # mannodermaus/android-junit5
mockk = "1.13.13"
turbine = "1.2.0"
paparazzi = "1.3.5"
robolectric = "4.14.1"

[libraries]
# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-compose-animation = { group = "androidx.compose.animation", name = "animation" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Lifecycle + ViewModel
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }

# Room
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

# DataStore
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

# Security
androidx-security-crypto = { group = "androidx.security", name = "security-crypto", version.ref = "securityCrypto" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigation" }
androidx-hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hiltNavigation" }
androidx-hilt-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hiltNavigation" }

# Coroutines
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinx-coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }

# Networking — Gson 제거, kotlinx.serialization 단독 사용
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-kotlinx-serialization = { group = "com.jakewharton.retrofit", name = "retrofit2-kotlinx-serialization-converter", version.ref = "retrofitKotlinxSerializer" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# 직렬화 (단독)
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

# WorkManager
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }

# UI 라이브러리
lottie-compose = { group = "com.airbnb.android", name = "lottie-compose", version.ref = "lottie" }
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }

# 테스트
junit-jupiter = { group = "org.junit.jupiter", name = "junit-jupiter", version.ref = "junit5" }
junit-jupiter-params = { group = "org.junit.jupiter", name = "junit-jupiter-params", version.ref = "junit5" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
# Compose UI Test (androidTest)
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version = "1.2.1" }
androidx-compose-ui-test-junit4-android = { group = "androidx.compose.ui", name = "ui-test-junit4" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
junit5-android = { id = "de.mannodermaus.android-junit5", version.ref = "junit5Android" }
paparazzi = { id = "app.cash.paparazzi", version.ref = "paparazzi" }
```

### build.gradle.kts (app 모듈)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.junit5.android)   // JUnit 5 Android 플러그인
    alias(libs.plugins.paparazzi)        // 스크린샷 테스트
}

dependencies {
    // ─────────────────────────────────────
    // Jetpack Compose
    // ─────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.activity.compose)

    // Navigation (type-safe routes — Navigation 2.8+)
    implementation(libs.androidx.navigation.compose)

    // ─────────────────────────────────────
    // ViewModel + Lifecycle
    // ─────────────────────────────────────
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ─────────────────────────────────────
    // Room DB
    // ─────────────────────────────────────
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // ─────────────────────────────────────
    // DataStore
    // ─────────────────────────────────────
    implementation(libs.androidx.datastore.preferences)

    // ─────────────────────────────────────
    // Security (EncryptedSharedPreferences — API Key 저장)
    // ─────────────────────────────────────
    implementation(libs.androidx.security.crypto)

    // ─────────────────────────────────────
    // Hilt (DI)
    // ─────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // ─────────────────────────────────────
    // Coroutines
    // ─────────────────────────────────────
    implementation(libs.kotlinx.coroutines.android)

    // ─────────────────────────────────────
    // 직렬화 (kotlinx.serialization 단독 — Gson 사용 안 함)
    // ─────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)

    // ─────────────────────────────────────
    // Networking (Claude API)
    // Gson 컨버터 제거 → kotlinx.serialization 컨버터 사용
    // ─────────────────────────────────────
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // ─────────────────────────────────────
    // WorkManager
    // ─────────────────────────────────────
    implementation(libs.androidx.work.runtime.ktx)

    // ─────────────────────────────────────
    // 애니메이션
    // ─────────────────────────────────────
    implementation(libs.lottie.compose)

    // ─────────────────────────────────────
    // 차트 (통계 화면)
    // ─────────────────────────────────────
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // ─────────────────────────────────────
    // 이미지
    // ─────────────────────────────────────
    implementation(libs.coil.compose)

    // ─────────────────────────────────────
    // 로깅
    // ─────────────────────────────────────
    implementation(libs.timber)

    // ─────────────────────────────────────
    // 테스트 — JUnit 5 (JUnit 4 사용 안 함)
    // ─────────────────────────────────────
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.jupiter.params)     // 파라미터화 테스트
    testImplementation(libs.mockk)                    // Kotlin 네이티브 모킹
    testImplementation(libs.turbine)                  // Flow 테스트
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)              // Android Context (Room In-Memory)
    testImplementation(libs.androidx.room.testing)

    // 스크린샷 테스트 (Paparazzi — 에뮬레이터 불필요)
    // paparazzi 플러그인이 자동으로 testImplementation 추가함

    // Compose UI 통합 테스트 (주요 플로우만)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

---

## 8.5 핵심 UseCase 구현 예시

### CompleteTaskUseCase (핵심 게임 루프)

#### 설계 원칙: 원자성 + 멱등성

퀘스트 완료는 여러 상태(Task, Character XP/HP/Streak, CombatLog, Item)를 동시에 변경한다.
더블탭, 재시도, 앱 크래시 등 중복 실행 시 XP/아이템 중복 지급이나 전투 기록 누락이 발생하지 않도록 두 가지 보호 장치를 적용한다.

1. **상태 전이 가드**: Task가 `ACTIVE` 상태일 때만 진행 (이미 `DONE`이면 기존 CombatLog 반환)
2. **단일 DB 트랜잭션**: Task 상태 변경 + CombatLog 삽입 + Character 업데이트를 하나의 `@Transaction`으로 묶음

```kotlin
// data/local/dao/CompletionDao.kt
@Dao
interface CompletionDao {
    /**
     * 퀘스트 완료의 모든 DB 쓰기를 단일 트랜잭션으로 처리.
     * ACTIVE 상태인 경우에만 상태를 DONE으로 전이하여 중복 실행을 방지한다.
     */
    @Transaction
    suspend fun commitCompletion(
        taskId: String,
        newStatus: String,           // "DONE" 또는 "INBOX" (Miss 복귀)
        completedAt: Long?,
        actualMinutes: Int?,
        updatedAt: Long,
        combatLog: CombatLogEntity,
        characterUpdate: CharacterSnapshotUpdate
    ): Boolean {
        // 조건부 상태 전이: ACTIVE가 아니면 0 rows affected → 중복 실행 감지
        val rowsAffected = conditionalCompleteTask(
            id = taskId,
            newStatus = newStatus,
            completedAt = completedAt,
            actualMinutes = actualMinutes,
            updatedAt = updatedAt
        )
        if (rowsAffected == 0) return false  // 이미 처리됨 (멱등성 보장)

        insertCombatLog(combatLog)
        updateCharacterSnapshot(characterUpdate)
        return true
    }

    @Query("""
        UPDATE tasks
        SET status = :newStatus,
            completedAt = :completedAt,
            actualMinutes = :actualMinutes,
            updatedAt = :updatedAt
        WHERE id = :id AND status = 'ACTIVE'
    """)
    suspend fun conditionalCompleteTask(
        id: String,
        newStatus: String,
        completedAt: Long?,
        actualMinutes: Int?,
        updatedAt: Long
    ): Int  // SQLite rows affected (0 = 이미 처리됨)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCombatLog(log: CombatLogEntity): Long

    @Query("""
        UPDATE characters
        SET currentXp = :xp, totalXpEarned = :totalXp,
            level = :level,
            currentHp = :hp,
            streakDays = :streak, longestStreak = MAX(longestStreak, :streak),
            lastActivityDate = :activityDate,
            totalQuestsCompleted = totalQuestsCompleted + :questDelta,
            totalCriticalHits = totalCriticalHits + :critDelta,
            totalCriticalMisses = totalCriticalMisses + :missDelta,
            updatedAt = :now
        WHERE id = :characterId
    """)
    suspend fun updateCharacterSnapshot(update: CharacterSnapshotUpdate)
}

// 캐릭터 업데이트를 묶는 값 객체
data class CharacterSnapshotUpdate(
    val characterId: String,
    val xp: Long, val totalXp: Long, val level: Int,
    val hp: Int,
    val streak: Int, val activityDate: Long,
    val questDelta: Int,    // +1 완료, 0 미스
    val critDelta: Int,     // +1 크리티컬, 0 otherwise
    val missDelta: Int,     // +1 미스, 0 otherwise
    val now: Long = System.currentTimeMillis()
)
```

```kotlin
// domain/usecase/CompleteTaskUseCase.kt
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val characterRepository: CharacterRepository,
    private val combatRepository: CombatRepository,
    private val narrativeRepository: ClaudeRepository,
    private val itemRepository: ItemRepository,
    private val completionDao: CompletionDao
) {
    suspend operator fun invoke(
        taskId: String,
        elapsedMinutes: Int? = null
    ): CompleteTaskResult {
        val now = System.currentTimeMillis()

        // 1. 태스크 + 캐릭터 조회
        val task = taskRepository.getTaskById(taskId)
            ?: return CompleteTaskResult.Error("Task not found")
        val character = characterRepository.getCharacterOnce()
            ?: return CompleteTaskResult.Error("No character")

        // 2. 이미 완료된 경우 기존 전투 기록 반환 (멱등성)
        if (task.status == TaskStatus.DONE) {
            val existing = combatRepository.getCombatLogByTaskId(taskId)
            if (existing != null) return CompleteTaskResult.AlreadyCompleted(existing)
        }

        // 3. D20 전투 해결 (순수 계산, DB 쓰기 없음)
        val combatResult = combatRepository.resolveCombat(task, character)

        // 4. 캐릭터 다음 상태 계산 (순수 함수)
        val nextCharacter = when (combatResult) {
            is CombatResult.Hit, is CombatResult.CriticalHit ->
                character.applyHit(combatResult.xpGained)
            is CombatResult.Miss, is CombatResult.CriticalMiss ->
                character.applyMiss(combatResult.hpLost)
        }

        // 5. 단일 트랜잭션: Task 상태 + CombatLog + Character 업데이트
        val combatLog = combatResult.toCombatLogEntity(taskId, character, now)
        val (newTaskStatus, completedAt) = when (combatResult) {
            is CombatResult.Hit, is CombatResult.CriticalHit ->
                TaskStatus.DONE to now
            is CombatResult.Miss, is CombatResult.CriticalMiss ->
                TaskStatus.INBOX to null   // Miss: Inbox 복귀
        }

        val committed = completionDao.commitCompletion(
            taskId = taskId,
            newStatus = newTaskStatus.name,
            completedAt = completedAt,
            actualMinutes = elapsedMinutes,
            updatedAt = now,
            combatLog = combatLog,
            characterUpdate = nextCharacter.toSnapshotUpdate(
                questDelta = if (combatResult is CombatResult.Hit || combatResult is CombatResult.CriticalHit) 1 else 0,
                critDelta = if (combatResult is CombatResult.CriticalHit) 1 else 0,
                missDelta = if (combatResult is CombatResult.Miss || combatResult is CombatResult.CriticalMiss) 1 else 0
            )
        )

        // 트랜잭션 충돌: 다른 스레드/탭이 먼저 완료한 경우
        if (!committed) {
            val existing = combatRepository.getCombatLogByTaskId(taskId)
            if (existing != null) return CompleteTaskResult.AlreadyCompleted(existing)
            return CompleteTaskResult.Error("Completion conflict — please retry")
        }

        // 6. 아이템 드롭 (트랜잭션 밖, 실패해도 완료는 보장됨)
        val droppedItem = (combatResult as? CombatResult.CriticalHit)?.itemDrop?.let {
            runCatching { itemRepository.addItemToInventory(it, character.id) }.getOrNull()
        }

        // 7. AI 내러티브 생성 (비동기, 결과를 기다리지 않음)
        val narrative = runCatching {
            when (combatResult) {
                is CombatResult.Hit, is CombatResult.CriticalHit ->
                    narrativeRepository.generateCombatNarrative(character, task, combatResult)
                is CombatResult.Miss, is CombatResult.CriticalMiss ->
                    narrativeRepository.generateMissNarrative(character, task, combatResult)
            }
        }.getOrElse { narrativeRepository.getFallback(combatResult) }

        return when (combatResult) {
            is CombatResult.Hit, is CombatResult.CriticalHit ->
                CompleteTaskResult.Success(
                    combatResult = combatResult,
                    levelUpResult = nextCharacter.levelUpResult,
                    droppedItem = droppedItem,
                    narrative = narrative
                )
            is CombatResult.Miss, is CombatResult.CriticalMiss ->
                CompleteTaskResult.Failed(
                    combatResult = combatResult,
                    narrative = narrative
                )
        }
    }
}
```

#### 멱등성 보장 흐름 요약

```
사용자 탭 (1회 또는 중복)
    ↓
task.status == ACTIVE? → NO  → 기존 CombatLog 반환 (AlreadyCompleted)
    ↓ YES
D20 전투 계산 (메모리, DB 없음)
    ↓
commitCompletion() — 단일 @Transaction
    ├─ UPDATE tasks … WHERE status='ACTIVE'  → rowsAffected
    │    rowsAffected == 0 → 다른 스레드가 먼저 처리 → AlreadyCompleted
    ├─ INSERT combat_logs (IGNORE 중복)
    └─ UPDATE characters (단일 UPDATE)
    ↓
아이템 드롭 (트랜잭션 밖, 실패해도 완료 롤백 없음)
    ↓
AI 내러티브 (비동기, 실패 시 폴백)
```

---

## 8.6 상태 관리 패턴

### ViewModel UiState 패턴

```kotlin
// feature/inbox/InboxViewModel.kt
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val getInboxItemsUseCase: GetInboxItemsUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val captureItemUseCase: CaptureItemUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()
    
    // 일회성 이벤트 (네비게이션, 스낵바)
    private val _events = MutableSharedFlow<InboxEvent>()
    val events: SharedFlow<InboxEvent> = _events.asSharedFlow()

    init {
        observeInboxItems()
    }
    
    private fun observeInboxItems() {
        viewModelScope.launch {
            getInboxItemsUseCase()
                .catch { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
                .collect { items ->
                    _uiState.update { it.copy(
                        items = items,
                        isLoading = false
                    )}
                }
        }
    }
    
    fun onCaptureText(text: String) {
        viewModelScope.launch {
            val itemId = captureItemUseCase(text, CaptureSource.APP)
            _events.emit(InboxEvent.ItemCaptured(itemId))
        }
    }
    
    fun onClarifyItem(itemId: String) {
        viewModelScope.launch {
            _events.emit(InboxEvent.NavigateToClarify(itemId))
        }
    }
}

data class InboxUiState(
    val items: List<InboxItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val filterType: InboxFilter = InboxFilter.ALL
)

sealed class InboxEvent {
    data class ItemCaptured(val itemId: String) : InboxEvent()
    data class NavigateToClarify(val itemId: String) : InboxEvent()
    data class ShowError(val message: String) : InboxEvent()
}
```

---

## 8.7 WorkManager 작업 목록

```kotlin
// 자정 HP 리셋 + 스트릭 체크
class HPResetAndStreakWorker(...) : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        // 1. HP 전체 회복 (Long Rest)
        characterRepository.healHP(Int.MAX_VALUE)
        
        // 2. 어제 퀘스트 완료 여부 확인
        val yesterdayCompleted = taskRepository.getCompletedCountYesterday() > 0
        if (!yesterdayCompleted) {
            // 스트릭 보호 토큰 확인
            val hasProtectToken = characterRepository.useStreakProtectToken()
            if (!hasProtectToken) {
                characterRepository.resetStreak()
            }
        } else {
            characterRepository.incrementStreak()
        }
        
        return Result.success()
    }
    
    companion object {
        fun schedule(context: Context) {
            val midnight = calculateNextMidnight()
            val request = OneTimeWorkRequestBuilder<HPResetAndStreakWorker>()
                .setInitialDelay(midnight, TimeUnit.MILLISECONDS)
                .addTag("hp_reset")
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}

// 일일 리마인더
class DailyReminderWorker(...) : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        val urgentTasks = taskRepository.getTasksDueToday()
        val notification = buildDailyNotification(urgentTasks)
        notificationManager.notify(DAILY_NOTIFICATION_ID, notification)
        return Result.success()
    }
}

// 주간 리뷰 리마인더 (토요일 10:00)
class WeeklyReviewReminderWorker(...) : CoroutineWorker(...) { ... }

// 랜덤 인카운터 (하루 1-2회)
class RandomEncounterWorker(...) : CoroutineWorker(...) { ... }
```

---

## 8.8 테스트 전략

### 계층별 테스트 도구 매핑

```
┌─────────────────────────────────────────────────────────┐
│  레이어          도구                  실행 환경          │
├─────────────────────────────────────────────────────────┤
│  UseCase         JUnit 5 + MockK       JVM (빠름)        │
│  ViewModel       JUnit 5 + Turbine     JVM              │
│  Repository      JUnit 5 + Robolectric JVM (Room 포함)  │
│  Composable      Paparazzi             JVM (에뮬 불필요) │
│  통합 플로우     Compose UI Test       에뮬레이터 (최소) │
└─────────────────────────────────────────────────────────┘
```

### 단위 테스트 (JVM, 빠름 — CI 필수)

```kotlin
// CompleteTaskUseCaseTest.kt — JUnit 5 + MockK + Turbine
class CompleteTaskUseCaseTest {

    @MockK lateinit var taskRepository: TaskRepository
    @MockK lateinit var characterRepository: CharacterRepository
    @MockK lateinit var combatRepository: CombatRepository
    @MockK lateinit var narrativeRepository: ClaudeRepository
    @MockK lateinit var completionDao: CompletionDao

    private lateinit var useCase: CompleteTaskUseCase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = CompleteTaskUseCase(
            taskRepository, characterRepository,
            combatRepository, narrativeRepository, completionDao
        )
    }

    @Test
    fun `더블탭 시 AlreadyCompleted 반환`() = runTest {
        // task가 이미 DONE 상태
        val task = fakeTask(status = TaskStatus.DONE)
        val log = fakeCombatLog()
        coEvery { taskRepository.getTaskById(any()) } returns task
        coEvery { combatRepository.getCombatLogByTaskId(any()) } returns log

        val result = useCase("task-1")

        assertIs<CompleteTaskResult.AlreadyCompleted>(result)
        coVerify(exactly = 0) { completionDao.commitCompletion(any(), any(), any(), any(), any(), any(), any()) }
    }

    @ParameterizedTest
    @ValueSource(ints = [20, 19, 15, 10, 2, 1])  // 크리티컬~미스 전 범위
    fun `D20 결과별 올바른 XP 계산`(d20: Int) = runTest { ... }
}
```

```kotlin
// XP 계산 순수 함수 테스트
class XpCalculatorTest {

    @ParameterizedTest
    @CsvSource(
        "5.0, 20, true,  250",   // CR5, 크리티컬, 2배
        "5.0, 15, false, 125",   // CR5, 명중, 기본
        "1.0,  1, false,  18",   // CR1, 미스, 최소
    )
    fun `XP 계산 공식 검증`(cr: Float, d20: Int, isCritical: Boolean, expectedBase: Long) {
        val xp = XpCalculator.calculate(cr, d20, isCritical, streakDays = 0, deadlineBonus = 1f)
        assertThat(xp).isEqualTo(expectedBase)
    }
}
```

### Flow 테스트 (Turbine)

```kotlin
@Test
fun `인박스 아이템 추가 시 UI 상태 즉시 반영`() = runTest {
    val viewModel = InboxViewModel(...)

    viewModel.uiState.test {
        assertThat(awaitItem().items).isEmpty()

        viewModel.onCaptureText("세금 신고")

        val updated = awaitItem()
        assertThat(updated.items).hasSize(1)
        assertThat(updated.items.first().rawText).isEqualTo("세금 신고")

        cancelAndIgnoreRemainingEvents()
    }
}
```

### 스크린샷 테스트 (Paparazzi — 에뮬레이터 불필요)

```kotlin
// QuestCardSnapshotTest.kt
class QuestCardSnapshotTest {
    @get:Rule val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_6,
        theme = "Theme.QuestLog"
    )

    @Test fun `QuestCard 일반 상태`() {
        paparazzi.snapshot {
            QuestLogTheme {
                QuestCard(
                    task = fakeTask(cr = 3f, title = "보고서 작성"),
                    onComplete = {}
                )
            }
        }
    }

    @Test fun `HPBar 위기 상태 (HP 25% 이하)`() {
        paparazzi.snapshot {
            QuestLogTheme {
                HPBar(currentHp = 5, maxHp = 40)  // 12.5% — 빨간색 확인
            }
        }
    }

    @Test fun `D20DiceView 크리티컬 히트 (20)`() {
        paparazzi.snapshot {
            QuestLogTheme { D20DiceView(result = 20) }
        }
    }
}
```

### Room 통합 테스트 (Robolectric — JVM)

```kotlin
@RunWith(RobolectricTestRunner::class)
class CompletionDaoTest {

    private lateinit var db: QuestLogDatabase
    private lateinit var dao: CompletionDao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            QuestLogDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.completionDao()
    }

    @Test fun `commitCompletion 중복 호출 시 두 번째는 false 반환`() = runTest {
        val task = insertActiveTask()
        val log = fakeCombatLog(taskId = task.id)
        val update = fakeCharacterUpdate()

        val first = dao.commitCompletion(task.id, "DONE", now, null, now, log, update)
        val second = dao.commitCompletion(task.id, "DONE", now, null, now, log, update)

        assertThat(first).isTrue()
        assertThat(second).isFalse()   // 멱등성: 두 번째는 rowsAffected == 0
    }

    @After fun tearDown() = db.close()
}
```

### 목표 커버리지 (CI 게이트)

```
UseCase (비즈니스 로직):  90%+  ← 핵심, CI 실패 기준
ViewModel (상태 변화):    80%+
Repository (DB 쿼리):     75%+
Composable (스냅샷):      핵심 컴포넌트 100% (QuestCard, HPBar, D20DiceView, CrBadge)
통합 플로우 E2E:          퀘스트 완료 플로우 1개만 (에뮬레이터)
```

---

## 8.9 Hilt 모듈 구성

### NetworkModule — kotlinx.serialization 컨버터 적용

```kotlin
// core/data/di/NetworkModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true   // API 응답에 새 필드 추가 시 앱 크래시 방지
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)   // Claude API 응답 대기
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.anthropic.com/")
            .client(okHttpClient)
            // Gson 대신 kotlinx.serialization 컨버터
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideClaudeApiService(retrofit: Retrofit): ClaudeApiService =
        retrofit.create(ClaudeApiService::class.java)
}
```

### DatabaseModule

```kotlin
// core/data/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuestLogDatabase =
        Room.databaseBuilder(context, QuestLogDatabase::class.java, "questlog.db")
            .addTypeConverter(Converters())   // @ProvidedTypeConverter
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides fun provideTaskDao(db: QuestLogDatabase) = db.taskDao()
    @Provides fun provideCharacterDao(db: QuestLogDatabase) = db.characterDao()
    @Provides fun provideCombatLogDao(db: QuestLogDatabase) = db.combatLogDao()
    @Provides fun provideCompletionDao(db: QuestLogDatabase) = db.completionDao()
    @Provides fun provideEncounterLogDao(db: QuestLogDatabase) = db.encounterLogDao()
    @Provides fun provideCharacterItemDao(db: QuestLogDatabase) = db.characterItemDao()
    @Provides fun provideItemDao(db: QuestLogDatabase) = db.itemDao()
    @Provides fun provideNpcDao(db: QuestLogDatabase) = db.npcDao()
    @Provides fun provideWeeklyReviewDao(db: QuestLogDatabase) = db.weeklyReviewDao()
    @Provides fun provideAchievementDao(db: QuestLogDatabase) = db.achievementDao()
    @Provides fun provideInboxItemDao(db: QuestLogDatabase) = db.inboxItemDao()
    @Provides fun provideProjectDao(db: QuestLogDatabase) = db.projectDao()
}
```

---

## 8.10 CI/CD — GitHub Actions

### .github/workflows/ci.yml

```yaml
name: CI

on:
  push:
    branches: [ master, main ]
  pull_request:
    branches: [ master, main ]

jobs:
  test:
    name: Unit Tests + Screenshot Tests
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      # 단위 테스트 (JVM — 빠름, 에뮬레이터 불필요)
      - name: Run Unit Tests
        run: ./gradlew testDebugUnitTest --stacktrace

      # 스크린샷 테스트 (Paparazzi — JVM)
      - name: Run Screenshot Tests
        run: ./gradlew verifyPaparazziDebug --stacktrace

      # 테스트 결과 업로드
      - name: Upload Test Report
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: test-report
          path: app/build/reports/tests/

      # Paparazzi diff 이미지 업로드 (스크린샷 불일치 시)
      - name: Upload Paparazzi Failures
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: paparazzi-failures
          path: app/build/paparazzi/failures/

  build:
    name: Build Debug APK
    runs-on: ubuntu-latest
    needs: test   # 테스트 통과 후에만 빌드

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace

      # APK를 GitHub Actions artifact로 업로드 (기기에 직접 설치 가능)
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: questlog-debug-${{ github.sha }}
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 14   # 2주 보관
```

### 배포 방식 (혼자 사용)

```
개발 중:
  ./gradlew installDebug     ← USB 연결 기기에 직접 설치
  adb install app-debug.apk  ← APK 직접 설치

CI 빌드 후:
  GitHub Actions → Artifacts → questlog-debug-{sha}.apk 다운로드
  → 기기에 설치 (개발자 옵션 + 알 수 없는 출처 허용)

Play Store: v1.0 이후 필요 시 검토 (현재 불필요)
```

---

## 8.10 빌드 설정

```kotlin
// app/build.gradle.kts
android {
    namespace = "com.questlog"
    compileSdk = 35
    
    defaultConfig {
        applicationId = "com.questlog"
        minSdk = 26          // Android 8.0 (2017+): 시장점유율 95%+
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true
            buildConfigField("String", "BASE_URL", "\"https://api.anthropic.com/\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "BASE_URL", "\"https://api.anthropic.com/\"")
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
}
```
