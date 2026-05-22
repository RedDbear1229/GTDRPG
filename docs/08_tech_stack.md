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
