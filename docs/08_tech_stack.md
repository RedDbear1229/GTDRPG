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

## 8.4 의존성 목록 (build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
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
    
    // Navigation
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
    // Security (EncryptedSharedPreferences)
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
    // Networking (Claude API)
    // ─────────────────────────────────────
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    
    // ─────────────────────────────────────
    // 애니메이션
    // ─────────────────────────────────────
    implementation(libs.lottie.compose)           // Lottie 애니메이션
    
    // ─────────────────────────────────────
    // 차트 (통계 화면)
    // ─────────────────────────────────────
    implementation(libs.vico.compose)             // 라인/바 차트
    implementation(libs.vico.compose.m3)
    
    // ─────────────────────────────────────
    // 이미지
    // ─────────────────────────────────────
    implementation(libs.coil.compose)
    
    // ─────────────────────────────────────
    // WorkManager
    // ─────────────────────────────────────
    implementation(libs.androidx.work.runtime.ktx)
    
    // ─────────────────────────────────────
    // 음성 인식 (내장 API 사용)
    // ─────────────────────────────────────
    // Android 내장 SpeechRecognizer - 별도 의존성 없음
    
    // ─────────────────────────────────────
    // 로깅
    // ─────────────────────────────────────
    implementation(libs.timber)
    
    // ─────────────────────────────────────
    // 직렬화
    // ─────────────────────────────────────
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)
    
    // ─────────────────────────────────────
    // 크래시 리포팅 (v1.5)
    // ─────────────────────────────────────
    // implementation(libs.firebase.crashlytics)
    
    // ─────────────────────────────────────
    // 테스트
    // ─────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)               // Flow 테스트
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

---

## 8.5 핵심 UseCase 구현 예시

### CompleteTaskUseCase (핵심 게임 루프)

```kotlin
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val characterRepository: CharacterRepository,
    private val combatRepository: CombatRepository,
    private val narrativeRepository: ClaudeRepository,
    private val itemRepository: ItemRepository
) {
    suspend operator fun invoke(
        taskId: String,
        elapsedMinutes: Int? = null
    ): CompleteTaskResult {
        
        // 1. 태스크 조회
        val task = taskRepository.getTaskById(taskId)
            ?: return CompleteTaskResult.Error("Task not found")
        
        val character = characterRepository.getCharacterOnce()
            ?: return CompleteTaskResult.Error("No character")
        
        // 2. D20 전투 해결
        val combatResult = combatRepository.resolveCombat(task, character)
        
        // 3. 태스크 완료 처리
        taskRepository.completeTask(taskId, elapsedMinutes)
        
        // 4. 결과에 따른 처리
        return when (combatResult) {
            is CombatResult.Hit, is CombatResult.CriticalHit -> {
                // XP 획득
                val xpResult = characterRepository.gainXP(combatResult.xpGained)
                
                // 아이템 드롭 처리
                val droppedItem = combatResult.itemDrop?.let {
                    itemRepository.addItemToInventory(it, character.id)
                }
                
                // 스트릭 업데이트
                characterRepository.updateStreak(completed = true)
                
                // AI 내러티브 생성 (비동기, 결과 기다리지 않음)
                val narrative = narrativeRepository.generateCombatNarrative(
                    character, task, combatResult
                )
                
                CompleteTaskResult.Success(
                    combatResult = combatResult,
                    levelUpResult = xpResult,
                    droppedItem = droppedItem,
                    narrative = narrative
                )
            }
            
            is CombatResult.Miss, is CombatResult.CriticalMiss -> {
                // HP 손실
                characterRepository.takeDamage(combatResult.hpLost)
                
                // 태스크 Inbox로 복귀
                taskRepository.returnToInbox(taskId)
                
                val narrative = narrativeRepository.generateMissNarrative(
                    character, task, combatResult
                )
                
                CompleteTaskResult.Failed(
                    combatResult = combatResult,
                    narrative = narrative
                )
            }
        }
    }
}
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

```
단위 테스트 (Unit Tests):
  - UseCase 테스트 (비즈니스 로직)
  - ViewModel 테스트 (상태 변화)
  - Repository 테스트 (Fake 구현)
  - 게임 메커니즘 계산 테스트 (XP, CR, 능력치)
  
통합 테스트 (Integration Tests):
  - Room DB In-Memory 테스트
  - Repository 구현 테스트
  
UI 테스트 (End-to-End):
  - Compose UI 테스트 (주요 플로우)
  - 온보딩 플로우
  - 퀘스트 완료 플로우

목표 커버리지:
  UseCase: 90%+
  ViewModel: 80%+
  Repository: 70%+
  
```

---

## 8.9 빌드 설정

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
