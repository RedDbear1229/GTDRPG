# 05. 데이터 모델

## 5.1 Entity 전체 목록

```
QuestLogDatabase (Room v1)
├── characters           캐릭터 정보
├── inbox_items          수집된 원시 항목
├── tasks                할 일 (명료화 완료)
├── projects             프로젝트 (캠페인)
├── combat_logs          전투 기록
├── npcs                 협력자/연락처
├── items                획득한 아이템 목록
├── character_items      캐릭터-아이템 장착 상태 (junction)
├── weekly_reviews       주간 리뷰 기록
├── encounter_logs       랜덤 인카운터 기록
├── achievements         업적 정의
└── character_achievements 달성한 업적 (junction)
```

---

## 5.2 Entity 상세 정의

### CharacterEntity

```kotlin
@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // 기본 정보
    val name: String,
    val classType: CharacterClass,
    val avatarResId: Int,
    val backstory: String? = null,        // 사용자 작성 배경 스토리
    
    // 레벨/XP
    val level: Int = 1,
    val currentXp: Long = 0,
    val totalXpEarned: Long = 0,          // 총 획득 XP (누적, 레벨업 후에도 감소 안 함)
    
    // HP
    val maxHp: Int,
    val currentHp: Int,
    
    // 능력치 (1-20 범위)
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    
    // 파생 스탯 (자동 계산되지만 저장)
    val proficiencyBonus: Int,            // 레벨 기반 자동 계산
    val armorClass: Int,                  // 기본 AC (장비 제외)
    
    // 스트릭
    val streakDays: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: Long? = null,   // 마지막 퀘스트 완료일
    val streakProtectTokens: Int = 0,     // 스트릭 보호 토큰
    
    // 통계
    val totalQuestsCompleted: Int = 0,
    val totalMonstersSlain: Int = 0,
    val totalCriticalHits: Int = 0,
    val totalCriticalMisses: Int = 0,
    val totalXpFromCriticals: Long = 0,
    
    // 클래스 특수 자원
    val classResourceCurrent: Int = 0,    // Ki, Sorcery Points 등
    val classResourceMax: Int = 0,        // 레벨 기반
    val classResourceLastRefresh: Long? = null,
    
    // 메타
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### InboxItemEntity

```kotlin
@Entity(tableName = "inbox_items")
data class InboxItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val rawText: String,
    val audioPath: String? = null,        // 음성 녹음 파일 경로
    val transcribedText: String? = null,  // STT 변환 결과
    val imagePaths: String = "[]",        // JSON 배열 문자열 (List<String>)
    
    val capturedAt: Long = System.currentTimeMillis(),
    val source: CaptureSource,            // APP, WIDGET, SHARE, VOICE, NOTIFICATION
    
    val isClarified: Boolean = false,
    val clarifiedAt: Long? = null,
    val resultType: ClarifyResultType? = null,  // TASK, SOMEDAY, REFERENCE, DELETED, DONE_NOW
    val clarifiedTaskId: String? = null,  // FK → tasks.id
    val clarifiedProjectId: String? = null
)
```

### TaskEntity

```kotlin
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("projectId"),
        Index("status"),
        Index("dueDate"),
        Index("context"),
        Index("lifeArea")
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // 기본 정보
    val title: String,
    val description: String? = null,
    val nextAction: String? = null,       // Clarify에서 정의한 구체적 다음 행동
    
    // 분류
    val projectId: String? = null,
    val status: TaskStatus,               // INBOX, ACTIVE, WAITING, SOMEDAY, REFERENCE, DONE, DELETED
    val lifeArea: LifeArea,
    val context: String? = null,          // @태그 문자열 (복수: "@컴퓨터,@집")
    val primaryAbility: AbilityType,      // 어떤 능력치 기반 퀘스트인지
    
    // 난이도
    val challengeRating: Float,
    val monsterType: MonsterType,         // CR 기반 자동 매핑
    val monsterName: String? = null,      // Claude AI 생성 몬스터 이름 (ex: "마감의 트롤")
    
    // 시간
    val estimatedMinutes: Int? = null,
    val actualMinutes: Int? = null,       // 실제 소요 시간 (완료 후 기록)
    val dueDate: Long? = null,
    val scheduledDate: Long? = null,      // 특정 날짜에 할 것
    
    // 위임
    val delegatedTo: String? = null,      // NPC id
    val delegatedAt: Long? = null,
    val waitingFollowUpDate: Long? = null,
    
    // 첨부
    val attachmentPaths: String = "[]",   // JSON 배열 문자열
    val notes: String? = null,
    
    // 반복
    val isRecurring: Boolean = false,
    val recurringRule: String? = null,    // JSON: RecurringRule
    val recurringParentId: String? = null,// 원본 반복 태스크 ID
    val recurringInstanceDate: Long? = null,
    
    // 게임
    val isQuickDone: Boolean = false,     // 2분 규칙으로 즉시 완료된 것
    
    // 메타
    val inboxItemId: String? = null,      // FK → inbox_items.id
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val deletedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
```

### ProjectEntity

```kotlin
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val title: String,
    val desiredOutcome: String? = null,   // "이 프로젝트가 완료되면 어떤 상태인가?"
    val campaignName: String? = null,     // D&D 캠페인명 (사용자 자유 입력)
    val description: String? = null,
    
    val status: ProjectStatus,            // ACTIVE, ON_HOLD, COMPLETED, SOMEDAY
    val lifeArea: LifeArea,
    
    val challengeRating: Float,           // 자동 계산: max(subtask CR) × 1.2
    val totalTaskCount: Int = 0,
    val completedTaskCount: Int = 0,
    
    val dueDate: Long? = null,
    val startDate: Long? = null,
    
    val xpReward: Long = 0,               // 프로젝트 완료 시 보너스 XP
    val isMilestone: Boolean = false,     // 마일스톤 프로젝트 여부
    
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
```

### CombatLogEntity

```kotlin
@Entity(
    tableName = "combat_logs",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId"), Index("combatAt")]
)
data class CombatLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val taskId: String,
    val characterId: String,
    val characterLevel: Int,              // 전투 당시 레벨 (스냅샷)
    
    // 전투 수치
    val d20Result: Int,                   // 1-20
    val abilityModifier: Int,
    val proficiencyBonus: Int,
    val equipmentBonus: Int,
    val totalAttack: Int,                 // d20 + 모든 보너스
    val monsterAC: Int,
    
    // 결과
    val isHit: Boolean,
    val isCriticalHit: Boolean,
    val isCriticalMiss: Boolean,
    
    // XP
    val baseXP: Long,
    val critMultiplier: Float,
    val deadlineBonus: Float,
    val streakBonus: Float,
    val classBonus: Float,
    val equipmentXPBonus: Float,
    val totalXPGained: Long,
    
    // HP
    val hpBefore: Int,
    val hpLost: Int,
    val hpAfter: Int,
    
    // 아이템 드롭
    val droppedItemId: String? = null,
    
    // 스트릭 (전투 당시)
    val streakDaysAtCombat: Int,
    
    // AI 내러티브
    val narrativeText: String? = null,    // Claude API 생성
    
    val combatAt: Long = System.currentTimeMillis()
)
```

### NpcEntity

```kotlin
@Entity(tableName = "npcs")
data class NpcEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val name: String,
    val nickname: String? = null,
    val relationship: NpcRelationship,    // ALLY, NEUTRAL, RIVAL, MENTOR, PARTY_MEMBER
    val estimatedClass: CharacterClass? = null,
    
    // 연락처 연동 (선택적)
    val contactId: String? = null,        // Android 연락처 ID
    val contactPhotoUri: String? = null,
    
    // 게임 스탯
    val charismaModifier: Int = 0,        // 소통 보너스/페널티 (-3 ~ +3)
    val delegationSuccessRate: Float = 0.7f, // 위임 성공률
    val trustLevel: Int = 1,              // 1-5, 위임 가능 CR 상한 결정
    
    // 통계
    val delegatedTaskCount: Int = 0,
    val completedDelegatedCount: Int = 0,
    val lastInteractionAt: Long? = null,
    
    // 클래스 궁합 캐시
    val compatibilityScore: Int? = null,  // -1(비호환), 0(중립), 1(호환)
    
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

### ItemEntity

```kotlin
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val itemKey: String,                  // 아이템 고유 키 (카탈로그 참조)
    val name: String,
    val description: String,
    val flavorText: String? = null,       // 판타지 설명 텍스트
    
    val itemType: ItemType,               // WEAPON, ARMOR, RING, NECKLACE, MISC
    val rarity: ItemRarity,               // COMMON, UNCOMMON, RARE, VERY_RARE, LEGENDARY
    val slot: EquipmentSlot,
    
    // 스탯 보너스 (JSON 직렬화)
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val strBonus: Int = 0,
    val dexBonus: Int = 0,
    val conBonus: Int = 0,
    val intBonus: Int = 0,
    val wisBonus: Int = 0,
    val chaBonus: Int = 0,
    val xpMultiplier: Float = 1.0f,       // XP 배율 아이템
    val hpBonusFlat: Int = 0,
    
    // 특수 효과 (코드로 처리)
    val specialEffectCode: String? = null,
    val specialEffectParams: String? = null,  // JSON 파라미터
    
    // 획득 정보
    val acquiredAt: Long = System.currentTimeMillis(),
    val acquiredFrom: String? = null,     // 획득 출처 설명
    val acquiredTaskId: String? = null,   // 획득한 전투의 Task ID
    
    val isEquipped: Boolean = false,
    val equippedSlot: EquipmentSlot? = null,
    val characterId: String              // 소유 캐릭터 ID
)
```

### WeeklyReviewEntity

```kotlin
@Entity(tableName = "weekly_reviews")
data class WeeklyReviewEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    val weekNumber: Int,                  // ISO 주차 번호
    val year: Int,
    val weekStartDate: Long,              // 월요일 00:00
    val weekEndDate: Long,                // 일요일 23:59
    
    // 완료 통계
    val completedTaskCount: Int,
    val completedProjectCount: Int,
    val totalXpGained: Long,
    val criticalHitCount: Int,
    val criticalMissCount: Int,
    val avgD20Result: Float,
    
    // 생활 영역별 완료 수 (JSON)
    val lifeAreaStats: String,            // JSON: Map<LifeArea, Int>
    
    // 스트릭
    val streakAtStart: Int,
    val streakAtEnd: Int,
    
    // 리뷰 체크리스트 완료 여부
    val step1InboxClear: Boolean = false,
    val step2ProjectsReviewed: Boolean = false,
    val step3WaitingReviewed: Boolean = false,
    val step4SomedayReviewed: Boolean = false,
    val step5ReflectionDone: Boolean = false,
    val step6NextWeekPrepared: Boolean = false,
    
    // AI 생성
    val aiSummary: String? = null,        // Claude API 생성 주간 요약
    val aiNextWeekStrategy: String? = null,
    
    // 사용자 메모
    val personalNotes: String? = null,
    val highlights: String? = null,       // 이번 주 하이라이트 (사용자 입력)
    
    val reviewStartedAt: Long? = null,
    val reviewCompletedAt: Long? = null,
    val xpAwarded: Long = 0              // 리뷰 완료 보상 XP
)
```

### AchievementEntity

```kotlin
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val key: String,                      // 고유 키 (하드코딩)
    
    val title: String,
    val description: String,
    val icon: String,                     // 이모지 또는 리소스명
    val rarity: AchievementRarity,        // BRONZE, SILVER, GOLD, PLATINUM, LEGENDARY
    
    val condition: String,                // JSON: AchievementCondition
    val xpReward: Long,
    val itemReward: String? = null,       // 아이템 키
    
    val isHidden: Boolean = false         // 숨겨진 업적 (달성 시에만 공개)
)

@Entity(
    tableName = "character_achievements",
    primaryKeys = ["characterId", "achievementKey"]
)
data class CharacterAchievementEntity(
    val characterId: String,
    val achievementKey: String,
    val unlockedAt: Long = System.currentTimeMillis(),
    val notified: Boolean = false
)
```

### CharacterItemEntity (캐릭터-아이템 장착 상태)

`items` 테이블이 인벤토리 전체 목록이라면, `character_items`는 현재 장착 상태를 관리하는 junction 테이블이다.
아이템 미래 이전/공유(v2)를 위해 별도 테이블로 분리한다.

```kotlin
@Entity(
    tableName = "character_items",
    primaryKeys = ["characterId", "itemId"],
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("characterId"),
        Index("itemId"),
        Index("equippedSlot")
    ]
)
data class CharacterItemEntity(
    val characterId: String,
    val itemId: String,

    // 장착 상태
    val isEquipped: Boolean = false,
    val equippedSlot: EquipmentSlot? = null,  // null = 인벤토리에만 있음

    // 획득 이력
    val acquiredAt: Long = System.currentTimeMillis(),
    val acquiredFromTaskId: String? = null,   // 드롭된 전투의 Task ID
    val acquiredFromEncounterId: String? = null, // 랜덤 인카운터 ID

    val updatedAt: Long = System.currentTimeMillis()
)

// EquipmentSlot enum
enum class EquipmentSlot {
    WEAPON,       // 주 무기 (공격 보너스)
    ARMOR,        // 방어구 (HP 보너스, AC)
    RING,         // 반지 (XP 배율 등 특수)
    NECKLACE,     // 목걸이 (능력치 보너스)
    MISC          // 소모품·기타 (인벤토리 전용)
}
```

#### CharacterItemDao

```kotlin
@Dao
interface CharacterItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToInventory(item: CharacterItemEntity): Long

    @Query("""
        UPDATE character_items
        SET isEquipped = :equipped, equippedSlot = :slot, updatedAt = :now
        WHERE characterId = :characterId AND itemId = :itemId
    """)
    suspend fun setEquipped(
        characterId: String, itemId: String,
        equipped: Boolean, slot: EquipmentSlot?,
        now: Long = System.currentTimeMillis()
    )

    // 같은 슬롯에 이미 장착된 아이템 해제 후 새 아이템 장착 (트랜잭션)
    @Transaction
    suspend fun equipItem(characterId: String, itemId: String, slot: EquipmentSlot) {
        unequipSlot(characterId, slot.name)
        setEquipped(characterId, itemId, true, slot)
    }

    @Query("""
        UPDATE character_items
        SET isEquipped = 0, equippedSlot = NULL, updatedAt = :now
        WHERE characterId = :characterId AND equippedSlot = :slotName
    """)
    suspend fun unequipSlot(
        characterId: String, slotName: String,
        now: Long = System.currentTimeMillis()
    )

    @Query("""
        SELECT ci.*, i.*
        FROM character_items ci
        INNER JOIN items i ON ci.itemId = i.id
        WHERE ci.characterId = :characterId AND ci.isEquipped = 1
    """)
    fun getEquippedItems(characterId: String): Flow<List<CharacterItemWithDetail>>

    @Query("""
        SELECT ci.*, i.*
        FROM character_items ci
        INNER JOIN items i ON ci.itemId = i.id
        WHERE ci.characterId = :characterId
        ORDER BY ci.acquiredAt DESC
    """)
    fun getInventory(characterId: String): Flow<List<CharacterItemWithDetail>>

    @Query("SELECT COUNT(*) FROM character_items WHERE characterId = :characterId")
    fun getInventoryCount(characterId: String): Flow<Int>

    @Query("DELETE FROM character_items WHERE characterId = :characterId AND itemId = :itemId")
    suspend fun removeFromInventory(characterId: String, itemId: String)
}

// Room @Relation을 활용한 조인 결과
data class CharacterItemWithDetail(
    @Embedded val characterItem: CharacterItemEntity,
    @Relation(
        parentColumn = "itemId",
        entityColumn = "id"
    )
    val item: ItemEntity
)
```

---

### EncounterLogEntity (랜덤 인카운터 기록)

WorkManager가 하루 1-2회 생성하는 랜덤 이벤트의 발생·처리 내역을 저장한다.

```kotlin
@Entity(
    tableName = "encounter_logs",
    indices = [
        Index("characterId"),
        Index("encounteredAt"),
        Index("status")
    ]
)
data class EncounterLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val characterId: String,

    // 인카운터 내용
    val encounterType: EncounterType,  // BONUS_XP, ITEM_DROP, HP_HEAL, STREAK_TOKEN, FULL_HP_HEAL
    val title: String,                 // "방랑 상인 출현!" 등
    val description: String,           // 이벤트 설명 2문장
    val sourceType: EncounterSource,   // LOCAL_TEMPLATE, CLAUDE_AI

    // 보상
    val xpBonus: Long = 0,
    val hpHealed: Int = 0,
    val streakTokenGiven: Int = 0,
    val droppedItemId: String? = null, // FK → items.id (드롭 아이템)

    // 상태
    val status: EncounterStatus,       // PENDING (알림 표시됨), CLAIMED (보상 수령), EXPIRED (48h 경과)
    val claimedAt: Long? = null,

    val encounteredAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 48 * 60 * 60 * 1000L  // 48시간 유효
)

enum class EncounterType {
    BONUS_XP,        // XP 보너스
    ITEM_DROP,       // 아이템 드롭
    HP_HEAL,         // HP 부분 회복 (25%)
    STREAK_TOKEN,    // 스트릭 보호 토큰 +1
    FULL_HP_HEAL     // HP 전체 회복 (여관 무료 숙박)
}

enum class EncounterSource { LOCAL_TEMPLATE, CLAUDE_AI }

enum class EncounterStatus { PENDING, CLAIMED, EXPIRED }
```

#### EncounterLogDao

```kotlin
@Dao
interface EncounterLogDao {
    @Insert
    suspend fun insert(log: EncounterLogEntity): Long

    @Query("""
        SELECT * FROM encounter_logs
        WHERE characterId = :characterId AND status = 'PENDING'
        ORDER BY encounteredAt DESC
    """)
    fun getPendingEncounters(characterId: String): Flow<List<EncounterLogEntity>>

    @Query("""
        SELECT * FROM encounter_logs
        WHERE characterId = :characterId
        ORDER BY encounteredAt DESC
        LIMIT :limit
    """)
    fun getRecentEncounters(characterId: String, limit: Int = 30): Flow<List<EncounterLogEntity>>

    // 보상 수령: PENDING → CLAIMED (조건부, 중복 수령 방지)
    @Query("""
        UPDATE encounter_logs
        SET status = 'CLAIMED', claimedAt = :now
        WHERE id = :id AND status = 'PENDING'
    """)
    suspend fun claimEncounter(id: String, now: Long = System.currentTimeMillis()): Int

    // 만료 처리: WorkManager가 주기적으로 실행
    @Query("""
        UPDATE encounter_logs
        SET status = 'EXPIRED'
        WHERE status = 'PENDING' AND expiresAt < :now
    """)
    suspend fun expireOldEncounters(now: Long = System.currentTimeMillis()): Int

    @Query("SELECT COUNT(*) FROM encounter_logs WHERE characterId = :characterId AND status = 'PENDING'")
    fun getPendingCount(characterId: String): Flow<Int>
}
```

---

## 5.3 DAO 인터페이스

### TaskDao

```kotlin
@Dao
interface TaskDao {
    // 기본 CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long
    
    @Update
    suspend fun update(task: TaskEntity)
    
    @Query("UPDATE tasks SET status = 'DELETED', deletedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())
    
    // 조회 (Flow로 실시간 업데이트)
    @Query("SELECT * FROM tasks WHERE status = 'INBOX' ORDER BY createdAt ASC")
    fun getInboxItems(): Flow<List<TaskEntity>>
    
    @Query("""
        SELECT * FROM tasks 
        WHERE status = 'ACTIVE' 
        AND (scheduledDate IS NULL OR scheduledDate <= :today)
        ORDER BY 
            CASE WHEN dueDate IS NOT NULL THEN dueDate ELSE 9999999999999 END ASC,
            challengeRating DESC
    """)
    fun getActiveTasks(today: Long = System.currentTimeMillis()): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE projectId = :projectId AND status != 'DELETED' ORDER BY createdAt ASC")
    fun getTasksByProject(projectId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE status = 'WAITING' ORDER BY delegatedAt ASC")
    fun getWaitingTasks(): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE status = 'SOMEDAY' ORDER BY updatedAt DESC")
    fun getSomedayTasks(): Flow<List<TaskEntity>>
    
    // 완료 처리
    @Query("""
        UPDATE tasks 
        SET status = 'DONE', completedAt = :now, actualMinutes = :minutes, updatedAt = :now
        WHERE id = :id
    """)
    suspend fun completeTask(id: String, now: Long, minutes: Int?)
    
    // 통계
    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'DONE' AND completedAt >= :since")
    suspend fun getCompletedCountSince(since: Long): Int
    
    @Query("SELECT COUNT(*) FROM tasks WHERE status = 'INBOX'")
    fun getInboxCount(): Flow<Int>
    
    // 검색
    @Query("""
        SELECT * FROM tasks 
        WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        AND status != 'DELETED'
        ORDER BY updatedAt DESC
    """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>
    
    // 오늘 마감
    @Query("""
        SELECT * FROM tasks 
        WHERE status = 'ACTIVE' 
        AND dueDate IS NOT NULL 
        AND dueDate <= :todayEnd
        ORDER BY dueDate ASC
    """)
    fun getTasksDueToday(todayEnd: Long): Flow<List<TaskEntity>>
    
    // 위임 후 팔로우업 필요
    @Query("""
        SELECT * FROM tasks 
        WHERE status = 'WAITING'
        AND delegatedAt IS NOT NULL
        AND delegatedAt < :threshold
    """)
    suspend fun getOverdueWaitingTasks(threshold: Long): List<TaskEntity>
}
```

### CharacterDao

```kotlin
@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: CharacterEntity)
    
    @Update
    suspend fun update(character: CharacterEntity)
    
    @Query("SELECT * FROM characters ORDER BY createdAt DESC LIMIT 1")
    fun getCharacter(): Flow<CharacterEntity?>
    
    @Query("SELECT * FROM characters ORDER BY createdAt DESC LIMIT 1")
    suspend fun getCharacterOnce(): CharacterEntity?
    
    @Query("UPDATE characters SET currentXp = :xp, totalXpEarned = :totalXp, updatedAt = :now WHERE id = :id")
    suspend fun updateXP(id: String, xp: Long, totalXp: Long, now: Long = System.currentTimeMillis())
    
    @Query("UPDATE characters SET level = :level, updatedAt = :now WHERE id = :id")
    suspend fun updateLevel(id: String, level: Int, now: Long = System.currentTimeMillis())
    
    @Query("UPDATE characters SET currentHp = :hp, updatedAt = :now WHERE id = :id")
    suspend fun updateHP(id: String, hp: Int, now: Long = System.currentTimeMillis())
    
    @Query("""
        UPDATE characters 
        SET streakDays = :streak, longestStreak = MAX(longestStreak, :streak),
            lastActivityDate = :date, updatedAt = :now
        WHERE id = :id
    """)
    suspend fun updateStreak(id: String, streak: Int, date: Long, now: Long = System.currentTimeMillis())
    
    @Query("UPDATE characters SET streakDays = 0, updatedAt = :now WHERE id = :id")
    suspend fun resetStreak(id: String, now: Long = System.currentTimeMillis())
}
```

### CombatLogDao

```kotlin
@Dao
interface CombatLogDao {
    @Insert
    suspend fun insert(log: CombatLogEntity): Long
    
    @Query("SELECT * FROM combat_logs ORDER BY combatAt DESC LIMIT :limit")
    fun getRecentCombats(limit: Int = 20): Flow<List<CombatLogEntity>>
    
    @Query("""
        SELECT * FROM combat_logs 
        WHERE combatAt >= :since AND combatAt <= :until
        ORDER BY combatAt DESC
    """)
    fun getCombatsBetween(since: Long, until: Long): Flow<List<CombatLogEntity>>
    
    @Query("SELECT SUM(totalXPGained) FROM combat_logs WHERE combatAt >= :since")
    fun getTotalXPSince(since: Long): Flow<Long?>
    
    @Query("SELECT COUNT(*) FROM combat_logs WHERE isHit = 1 AND combatAt >= :since")
    fun getHitCountSince(since: Long): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM combat_logs WHERE isCriticalHit = 1")
    fun getTotalCriticalHits(): Flow<Int>
    
    @Query("SELECT AVG(d20Result) FROM combat_logs WHERE combatAt >= :since")
    fun getAvgD20Since(since: Long): Flow<Float?>
}
```

---

## 5.4 Repository 구현 구조

```kotlin
// 도메인 레이어 인터페이스
interface TaskRepository {
    fun getInboxItems(): Flow<List<Task>>
    fun getActiveTasks(): Flow<List<Task>>
    fun getInboxCount(): Flow<Int>
    suspend fun captureItem(text: String, source: CaptureSource): String
    suspend fun clarifyItem(itemId: String, result: ClarifyResult)
    suspend fun completeTask(taskId: String, elapsedMinutes: Int?): CombatResult
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: String)
    fun searchTasks(query: String): Flow<List<Task>>
}

interface CharacterRepository {
    fun getCharacter(): Flow<Character>
    suspend fun createCharacter(config: CharacterCreationConfig): Character
    suspend fun gainXP(amount: Long): LevelUpResult?
    suspend fun takeDamage(amount: Int): Boolean  // true if still alive
    suspend fun healHP(amount: Int)
    suspend fun updateStreak(completed: Boolean)
    suspend fun equipItem(itemId: String, slot: EquipmentSlot)
    suspend fun unequipItem(slot: EquipmentSlot)
}

interface CombatRepository {
    suspend fun rollD20(): Int
    suspend fun resolveCombat(task: Task, character: Character): CombatResult
    fun getCombatHistory(limit: Int): Flow<List<CombatLog>>
    fun getWeeklyStats(): Flow<WeeklyCombatStats>
}
```

---

## 5.5 TypeConverters (Room)

```kotlin
// Gson 없음 — kotlinx.serialization + 표준 라이브러리만 사용
@ProvidedTypeConverter
class Converters {

    // Enum 변환: name 문자열로 저장 (외부 라이브러리 불필요)
    @TypeConverter
    fun fromCharacterClass(value: String?): CharacterClass? =
        value?.let { CharacterClass.valueOf(it) }

    @TypeConverter
    fun toCharacterClass(classType: CharacterClass?): String? = classType?.name

    @TypeConverter
    fun fromTaskStatus(value: String?): TaskStatus? =
        value?.let { TaskStatus.valueOf(it) }

    @TypeConverter
    fun toTaskStatus(status: TaskStatus?): String? = status?.name

    @TypeConverter
    fun fromLifeArea(value: String?): LifeArea? =
        value?.let { LifeArea.valueOf(it) }

    @TypeConverter
    fun toLifeArea(area: LifeArea?): String? = area?.name

    @TypeConverter
    fun fromEncounterType(value: String?): EncounterType? =
        value?.let { EncounterType.valueOf(it) }

    @TypeConverter
    fun toEncounterType(type: EncounterType?): String? = type?.name

    // List<String> 변환: kotlinx.serialization Json 사용 (Gson 대체)
    @TypeConverter
    fun fromStringList(value: String): List<String> =
        Json.decodeFromString(value)

    @TypeConverter
    fun toStringList(list: List<String>): String =
        Json.encodeToString(list)

    // Map<String, Int> 변환 (lifeAreaStats 등)
    @TypeConverter
    fun fromStringIntMap(value: String): Map<String, Int> =
        Json.decodeFromString(value)

    @TypeConverter
    fun toStringIntMap(map: Map<String, Int>): String =
        Json.encodeToString(map)
}
```

---

## 5.6 Migration 전략

```kotlin
// 버전 1 → 2 마이그레이션 예시
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 새 컬럼 추가 시 ALTER TABLE 사용
        database.execSQL(
            "ALTER TABLE tasks ADD COLUMN monsterName TEXT"
        )
    }
}

@Database(
    entities = [
        CharacterEntity::class,
        InboxItemEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
        CombatLogEntity::class,
        NpcEntity::class,
        ItemEntity::class,
        WeeklyReviewEntity::class,
        EncounterLogEntity::class,
        AchievementEntity::class,
        CharacterAchievementEntity::class
    ],
    version = 1,
    exportSchema = true   // 스키마 JSON 파일 생성 (버전 관리)
)
@TypeConverters(Converters::class)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun combatLogDao(): CombatLogDao
    abstract fun npcDao(): NpcDao
    abstract fun itemDao(): ItemDao
    abstract fun weeklyReviewDao(): WeeklyReviewDao
    abstract fun achievementDao(): AchievementDao
    
    companion object {
        const val DATABASE_NAME = "questlog.db"
    }
}
```

---

## 5.7 DataStore (앱 설정)

```kotlin
// Proto DataStore (타입 안전)
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val currentCharacterId: String? = null,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: Int = 8 * 60,  // 분 단위 (08:00)
    val weeklyReviewEnabled: Boolean = true,
    val weeklyReviewDayOfWeek: Int = 6,   // 토요일
    val weeklyReviewTime: Int = 10 * 60,  // 10:00
    val streakProtectEnabled: Boolean = true,
    val claudeApiKey: String? = null,     // EncryptedSharedPreferences에 별도 저장
    val claudeApiEnabled: Boolean = false,
    val theme: AppTheme = AppTheme.DARK,
    val language: String = "ko",
    val hapticFeedback: Boolean = true,
    val soundEffects: Boolean = true,
    val lastBackupAt: Long? = null
)
```

**보안**: Claude API Key는 EncryptedSharedPreferences에 저장 (DataStore X)

```kotlin
class SecureStorage @Inject constructor(
    private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveApiKey(key: String) = prefs.edit().putString("claude_api_key", key).apply()
    fun getApiKey(): String? = prefs.getString("claude_api_key", null)
    fun clearApiKey() = prefs.edit().remove("claude_api_key").apply()
}
```
