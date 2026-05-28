# 05. 데이터 모델

## 5.1 Entity 전체 목록

> **버전·도입 phase·마이그레이션 방식의 단일 진실은 §5.6.1.** 본 절은 현재 구현 / 계획 최종 두 스냅샷만 제시한다.

### 5.1.1 현재 구현된 스키마 (Room v5, F4.0 시점)

```
QuestLogDatabase (Room v5 — 코드와 1:1 일치, app/src/main/java/com/questlog/core/data/db/QuestLogDatabase.kt)
├── inbox_items            수집된 원시 항목 (v2, F1.1)
├── tasks                  할 일 (명료화 완료, v2, F1.1)
├── projects               프로젝트 (캠페인, v2, F1.1)
├── characters             캐릭터 정보 (v3, F2.1)
├── combat_logs            전투 기록 (v4, F3.1)
└── consent_records        프라이버시 동의 이력 (v5, F4.0)
```

총 **6개 엔티티**. `schemas/2.json`이 최초의 schema JSON(fresh install 첫 활성 스키마). `schemas/5.json`이 현재 최신 — ARM64 빌드 환경 제약으로 아직 미커밋(x86_64 환경에서 `./gradlew :app:kspDebugKotlin` 실행 필요).

### 5.1.2 계획된 최종 스키마 (Room v12, F6.1 Memory of the Day 완료 후 — 미구현)

```
QuestLogDatabase (Room v12, F6.1 완료 시점 — 계획)
├── inbox_items            수집된 원시 항목 (v2, F1.1)            ✅ 구현됨
├── tasks                  할 일 (명료화 완료, v2, F1.1)           ✅ 구현됨
├── projects               프로젝트 (캠페인, v2, F1.1)             ✅ 구현됨
├── characters             캐릭터 정보 (v3, F2.1)                  ⏳ 미구현
├── combat_logs            전투 기록 (v4, F3.1)                    ✅ 구현됨
├── consent_records        프라이버시 동의 이력 (v5, F4.0)         ✅ 구현됨
├── items                  아이템 카탈로그 (v6, F4.1)              ⏳ 미구현
├── character_items        캐릭터-아이템 장착 상태 (v6, F4.1)      ⏳ 미구현
├── npcs                   협력자/연락처 (v7, F4.2)                ⏳ 미구현
├── encounter_logs         랜덤 인카운터 기록 (v8, F4.4)           ⏳ 미구현
├── xp_awards              인카운터 보상 감사 로그 (v8, F4.4)      ⏳ 미구현
├── weekly_reviews         주간 리뷰 기록 (v9, F5.x)               ⏳ 미구현
├── achievements           업적 카탈로그 (v10, F5.x)               ⏳ 미구현
├── character_achievements 달성한 업적 (junction, v11, F5.x)       ⏳ 미구현
└── memory_entries         하루 1엔트리 회고 (v12, F6.1)           ⏳ 미구현
```

총 **15개 엔티티** (F6.1 완료 시점, 계획). 본 절의 §5.2 ~ §5.5 Entity/DAO/Repository/TypeConverter 상세는 이 계획 최종 스키마 기준으로 기술된다 — 새 entity 도입 시 §5.6.1 SSOT 갱신 → 코드/스키마 JSON 추가가 머지 게이트.

> ⚠️ §5.1.1(현재 구현)과 §5.1.2(계획 최종)는 서로 다른 시점의 스냅샷이다. 새 phase 진입 전 §5.6.1과 §5.6.4가 일치하는지 확인 — 어긋나면 SSOT(§5.6.1)가 우선이고 본 목록·§5.6.4·DAO 등록을 일괄 갱신해야 한다.

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
    val imagePaths: List<String> = emptyList(),  // Converters 가 List<String>? <-> String? 변환
    
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
    val attachmentPaths: List<String> = emptyList(),  // Converters 가 List<String>? <-> String? 변환
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

### MemoryEntryEntity (Memory of the Day, F6.1)

하루 1엔트리 회고. PRD §3.9 / IMPLEMENTATION_PLAN F6.1 의 데이터 계약을 본 절이 정본으로 보유한다.
즉시 정산이 아닌 **지연 정산** — 사용자는 그날 완료한 퀘스트 중 1개를 골라 짧은 소회를 남긴다.

**계약**:
- `entryDate`는 저장 시점 **local timezone** 기준 `yyyy-MM-dd`. UTC 변환 금지 (로컬 의미 보존이 우선).
- `entryDate UNIQUE` → 하루 1엔트리 강제 (DB 레벨 제약).
- `characterId` ON DELETE **CASCADE** — 캐릭터 삭제 시 메모도 정리.
- `taskId` ON DELETE **SET NULL** — Task가 hard delete 되어도 메모는 보존하되 FK 끊김.
- `taskTitleSnapshot` 스냅샷 컬럼 — FK 끊겨도 "어떤 퀘스트였는지" 컨텍스트 유지.
- `body` (사용자 본문) ≠ `enrichedBody` (Claude 윤색본) — **별도 컬럼**. 원본은 윤색 후에도 보존, UI 토글로 둘 다 표시.
- Claude 윤색 호출 시 **`body` 만 전송** — `taskTitleSnapshot`/`outcomeType`/`entryDate`/`characterId` 모두 `PromptSanitizer`로 제외.

```kotlin
@Entity(
    tableName = "memory_entries",
    indices = [
        Index(value = ["entryDate"], unique = true),   // 하루 1엔트리 강제
        Index(value = ["characterId"]),
        Index(value = ["taskId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class MemoryEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val entryDate: String,                // "yyyy-MM-dd" (저장 시점 local tz, UNIQUE)
    val characterId: String,
    val taskId: String?,                  // Task 삭제 시 SET NULL 허용
    val taskTitleSnapshot: String,        // FK 끊겨도 컨텍스트 보존
    val outcomeType: MemoryOutcomeType,   // STRONG | WEAK | MISS | NONE
    val body: String,                     // 사용자 본문 (UI 500자 제한, 컬럼은 TEXT)
    val enrichedBody: String? = null,     // Claude 윤색본 (nullable, 동의·API 통과 후만)
    val enrichmentTone: MemoryTone? = null, // 윤색 톤: NARRATIVE/HUMOROUS/PHILOSOPHICAL/CONCISE

    val createdAt: Long = System.currentTimeMillis(),
    val sealedAt: Long                     // 다음 날 00:00 잠금 시각 (편집 불가 전환)
)

enum class MemoryOutcomeType {
    STRONG,    // 크리티컬/HIT 강
    WEAK,      // HIT 약 / 일반 성공
    MISS,      // 미스 (그래도 시도는 했음)
    NONE       // 무전투 (2분 룰, QuickDone 등)
}

enum class MemoryTone { NARRATIVE, HUMOROUS, PHILOSOPHICAL, CONCISE }
```

#### MemoryDao

```kotlin
@Dao
interface MemoryDao {

    // 저장: UNIQUE(entryDate) 위반 시 REPLACE 가 아닌 ABORT — 호출자는 충돌을 명확히 인지해야 함
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntry(entry: MemoryEntryEntity)

    // 윤색본만 갱신 (원본 body 는 절대 덮어쓰지 않음)
    @Query("""
        UPDATE memory_entries
        SET enrichedBody = :enrichedBody, enrichmentTone = :tone
        WHERE id = :id
    """)
    suspend fun updateEnrichment(id: String, enrichedBody: String, tone: MemoryTone)

    // 오늘 엔트리 조회 (없으면 null)
    @Query("SELECT * FROM memory_entries WHERE entryDate = :date LIMIT 1")
    suspend fun getByDate(date: String): MemoryEntryEntity?

    // History 화면용 Paging3 소스
    @Query("""
        SELECT * FROM memory_entries
        WHERE characterId = :characterId
        ORDER BY entryDate DESC
    """)
    fun pageHistory(characterId: String): PagingSource<Int, MemoryEntryEntity>

    // Weekly Review 통합 — 이번 주 메모 개수
    @Query("""
        SELECT COUNT(*) FROM memory_entries
        WHERE characterId = :characterId
          AND entryDate BETWEEN :weekStart AND :weekEnd
    """)
    fun countThisWeek(characterId: String, weekStart: String, weekEnd: String): Flow<Int>

    // Weekly Review 통합 — 이번 주 일별 미니 카드 (최대 7개)
    @Query("""
        SELECT * FROM memory_entries
        WHERE characterId = :characterId
          AND entryDate BETWEEN :weekStart AND :weekEnd
        ORDER BY entryDate ASC
    """)
    fun getThisWeekEntries(characterId: String, weekStart: String, weekEnd: String): Flow<List<MemoryEntryEntity>>
}
```

> **race 차단 패턴**: UseCase는 `LocalDate.now()`를 **트랜잭션 시작 시점에 단 한 번** 캡처하고, 이후 동일 인스턴스를 사용해 `insertEntry()` 한다. 자정 경계에서 분기되어 같은 호출 안에서 두 날짜가 섞이는 race 를 차단한다. UNIQUE 위반 시 Snackbar 로 "오늘은 이미 기록했어요" 명시.

> **타임존 변경**: `entryDate`는 저장 시점 local tz 기준. 이후 사용자가 timezone 을 바꿔도 과거 엔트리의 `entryDate`는 재해석하지 않음. UTC 변환 절대 금지.

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

> 현재 구현 (v2 기준)은 `app/src/main/java/com/questlog/core/data/db/Converters.kt` 와 1:1 일치.
> `@ProvidedTypeConverter` + Hilt 생성자 주입으로 `Json` 인스턴스를 받고, **모든 변환 시그니처는 nullable** (Room이 NULL 컬럼을 그대로 통과시킬 수 있도록).
> Phase 2 이후 새 enum/컬렉션 타입이 추가될 때는 동일한 nullable 패턴을 유지한다.

```kotlin
// Gson 없음 — kotlinx.serialization 단독. Hilt가 Json 인스턴스를 주입한다.
@ProvidedTypeConverter
class Converters @Inject constructor(
    private val json: Json,
) {

    // List<String>? <-> String? — JSON 직렬화 (nullable 양방향)
    @TypeConverter
    fun fromStringList(value: List<String>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.let { json.decodeFromString<List<String>>(it) }

    // Enum 변환: name 문자열로 저장 (외부 라이브러리 불필요, 모두 nullable)
    @TypeConverter fun fromTaskStatus(v: TaskStatus?): String? = v?.name
    @TypeConverter fun toTaskStatus(v: String?): TaskStatus? = v?.let(TaskStatus::valueOf)

    @TypeConverter fun fromProjectStatus(v: ProjectStatus?): String? = v?.name
    @TypeConverter fun toProjectStatus(v: String?): ProjectStatus? = v?.let(ProjectStatus::valueOf)

    @TypeConverter fun fromLifeArea(v: LifeArea?): String? = v?.name
    @TypeConverter fun toLifeArea(v: String?): LifeArea? = v?.let(LifeArea::valueOf)

    @TypeConverter fun fromAbilityType(v: AbilityType?): String? = v?.name
    @TypeConverter fun toAbilityType(v: String?): AbilityType? = v?.let(AbilityType::valueOf)

    @TypeConverter fun fromMonsterType(v: MonsterType?): String? = v?.name
    @TypeConverter fun toMonsterType(v: String?): MonsterType? = v?.let(MonsterType::valueOf)

    @TypeConverter fun fromCaptureSource(v: CaptureSource?): String? = v?.name
    @TypeConverter fun toCaptureSource(v: String?): CaptureSource? = v?.let(CaptureSource::valueOf)

    @TypeConverter fun fromClarifyResultType(v: ClarifyResultType?): String? = v?.name
    @TypeConverter fun toClarifyResultType(v: String?): ClarifyResultType? = v?.let(ClarifyResultType::valueOf)

    // Phase 2+ (계획) — 새 enum/컬렉션 타입은 동일 nullable 패턴으로 추가
    // @TypeConverter fun fromCharacterClass(v: CharacterClass?): String? = v?.name
    // @TypeConverter fun toCharacterClass(v: String?): CharacterClass? = v?.let(CharacterClass::valueOf)
    // @TypeConverter fun fromEncounterType(v: EncounterType?): String? = v?.name
    // @TypeConverter fun toEncounterType(v: String?): EncounterType? = v?.let(EncounterType::valueOf)
    // @TypeConverter fun fromStringIntMap(v: Map<String, Int>?): String? = v?.let { json.encodeToString(it) }
    // @TypeConverter fun toStringIntMap(v: String?): Map<String, Int>? = v?.let { json.decodeFromString(it) }
}
```

> `@ProvidedTypeConverter` 이므로 `DatabaseModule` 의 `Room.databaseBuilder(...).addTypeConverter(converters)` 호출이 필수. 직접 `class Converters {}` 만 두면 Room 이 인스턴스를 만들지 못해 런타임 크래시.

---

## 5.6 Migration 전략

### 5.6.1 단계별 스키마 진화 (정본)

> 본 표가 스키마 버전의 **단일 진실 공급원(SSOT)**이다. `IMPLEMENTATION_PLAN.md`의 phase별 작업과 일치해야 한다 (Codex 적대적 리뷰 2026-05 2차 반영).
> ⛔ Phase 1 도그푸딩 개시(F1.5 완료) 이후 `fallbackToDestructiveMigration()` **영구 금지**.

| Version | 도입 Phase / Feature | 추가/변경 사항 | 마이그레이션 방식 |
|--------:|---------------------|---------------|------------------|
| **1** | (예약) | **현재 코드/디바이스에 존재하지 않음.** Room은 `entities = []` 빈 DB를 허용하지 않으므로 F0.3은 `QuestLogDatabase` 파일 스켈레톤만 두고 `@Database` 활성화는 F1.1로 연기 (Codex 적대적 리뷰 2026-05-24 반영). v1 번호는 "엔티티가 한 개도 없던 초기 상태"의 의미적 placeholder. | — (한 번도 persisted된 적 없음) |
| **2** | F1.1 GTD MVP | `InboxItemEntity`, `TaskEntity`, `ProjectEntity` 신규 — `@Database(version = 2)`를 처음으로 활성화 | **v2가 첫 활성 스키마 (fresh install).** AutoMigration(1, 2) **불필요** — 어떤 디바이스에도 v1이 persisted된 적이 없으므로 Room은 v2 테이블을 fresh로 생성. `schemas/2.json`이 최초의 schema JSON. |
| **3** | F2.1 캐릭터 | `CharacterEntity` 신규 | `@AutoMigration(2, 3)` |
| **4** | F3.1 D20 전투 | `CombatLogEntity` 신규 + `TaskEntity.completedAt`/`xpAwarded` 컬럼 추가 | **수동** `MIGRATION_3_4` (NOT NULL 기본값 충돌 회피) |
| **5** | F4.0 프라이버시 기반 | `ConsentRecordEntity` 신규 (정책 버전 추적, Codex 2차 지적 #4 반영) — **구현 완료** | `@AutoMigration(4, 5)` |
| **6** | F4.1 아이템 | `ItemEntity`, `CharacterItemEntity`(junction) 신규 + 인덱스 | `@AutoMigration(5, 6)` |
| **7** | F4.2 NPC | `NpcEntity` 신규 (`source: enum {MANUAL, PICKER}`) | `@AutoMigration(6, 7)` |
| **8** | F4.4 인카운터 | `EncounterLogEntity`, `XpAwardEntity` 신규 + `UNIQUE(encounter_id) ON xp_awards` | **수동** `MIGRATION_7_8` (UNIQUE 인덱스 강제) |
| **9** | F5.x WeeklyReview | `WeeklyReviewEntity` 신규 — 주간 리뷰 결과 영속화 | `@AutoMigration(8, 9)` |
| **10** | F5.x Achievement | `AchievementEntity` 신규 — 업적 카탈로그 | `@AutoMigration(9, 10)` |
| **11** | F5.x Achievement 연결 | `CharacterAchievementEntity` 신규 (junction) + 인덱스 | `@AutoMigration(10, 11)` |
| **12** | F6.1 Memory of the Day | `MemoryEntryEntity` 신규 — `entryDate UNIQUE`, FK `characterId` CASCADE / `taskId` SET NULL, `taskTitleSnapshot` 스냅샷 컬럼 | `@AutoMigration(11, 12)` (신규 테이블만) |
| **13+** | Phase 7+ | 추가 엔티티 발생 시 별도 버전 부여 | 신규 테이블만 → AutoMigration / 컬럼·제약 변경 → 수동 |

### 5.6.2 수동 마이그레이션 작성 패턴

```kotlin
// v3 → v4 (F3.1): nullable 컬럼 추가 — TaskEntity.completedAt: Long?
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // completedAt은 nullable(Long?)이므로 NOT NULL / DEFAULT 없이 추가
        database.execSQL("""
            ALTER TABLE tasks
            ADD COLUMN completedAt INTEGER
        """.trimIndent())
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS combat_logs (
                id TEXT NOT NULL PRIMARY KEY,
                taskId TEXT NOT NULL,
                rollResult INTEGER NOT NULL,
                ...
            )
        """.trimIndent())
    }
}

// v7 → v8 (F4.4): 보상 감사 무결성 — UNIQUE(encounter_id) 강제
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS encounter_logs (
                id TEXT NOT NULL PRIMARY KEY,
                characterId TEXT NOT NULL,
                encounterType TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                sourceType TEXT NOT NULL,
                xpBonus INTEGER NOT NULL DEFAULT 0,
                hpHealed INTEGER NOT NULL DEFAULT 0,
                streakTokenGiven INTEGER NOT NULL DEFAULT 0,
                droppedItemId TEXT,
                status TEXT NOT NULL,
                claimedAt INTEGER,
                encounteredAt INTEGER NOT NULL,
                expiresAt INTEGER NOT NULL
            )
        """.trimIndent())
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_encounter_status_expires
              ON encounter_logs(status, expiresAt)
        """.trimIndent())
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS xp_awards (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                encounterId TEXT NOT NULL,
                characterId TEXT NOT NULL,
                xpAmount INTEGER NOT NULL,
                awardedAt INTEGER NOT NULL,
                FOREIGN KEY(encounterId) REFERENCES encounter_logs(id) ON DELETE RESTRICT
            )
        """.trimIndent())
        // 🔴 보상 멱등성 + 감사 무결성: UNIQUE 제약으로 중복 INSERT 시 SQLITE_CONSTRAINT
        // ⚠️ encounterId는 encounter_logs.id(TEXT/UUID)와 동일 타입이어야 FK 무결성 보장
        database.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_xp_awards_encounter_unique
              ON xp_awards(encounterId)
        """.trimIndent())
    }
}
```

### 5.6.3 마이그레이션 테스트 의무

각 버전 증분 PR은 다음을 포함해야 머지 가능:

1. `app/schemas/<DB FQN>/<N>.json` 커밋
2. `MigrationTest`:
   - 이전 버전 시드 DB(`src/androidTest/assets/db/v{N-1}_seed.db`) 로드
   - `MigrationTestHelper.runMigrationsAndValidate(...)` 호출
   - 기존 데이터 row count 무손실 검증
   - 신규 컬럼/테이블 존재 + 제약(UNIQUE, FK) 적용 확인
3. Phase 1 도그푸딩 시작 후에는 **직전 버전의 실제 사용자 DB 백업본**을 시드로 사용 (개발자 본인 데이터)

### 5.6.4 현재 구현 (v2 — F1.1 GTD MVP 시점, 코드와 1:1 일치)

`app/src/main/java/com/questlog/core/data/db/QuestLogDatabase.kt` 의 실제 정의:

```kotlin
@Database(
    entities = [
        InboxItemEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun inboxItemDao(): InboxItemDao
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
}
```

- v2가 fresh install 의 첫 활성 스키마 (§5.6.1 v1/v2 row 참조 — v1은 어떤 디바이스에도 persisted된 적 없음).
- `app/schemas/com.questlog.core.data.db.QuestLogDatabase/2.json` 커밋 필수 (마이그레이션 회귀 보호).
- `autoMigrations` 배열은 v3 이후부터 등장 — 현재는 비어 있음.

### 5.6.5 계획된 최종 정의 (v12 기준 — F6.1 Memory of the Day 완료 시점, 미구현)

> ⚠️ 본 코드 블록은 **계획**이다. 실제 `QuestLogDatabase.kt` 와 다르며, F2.1 ~ F6.1 진행에 따라 단계적으로 채워진다. 각 phase 머지 시 §5.6.1 표와 §5.6.4 현재 정의를 함께 갱신할 것.

```kotlin
@Database(
    entities = [
        // Phase 1 (v2, F1.1) — 현재 구현됨
        InboxItemEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
        // Phase 2 (v3, F2.1) — 미구현
        CharacterEntity::class,
        // Phase 3 (v4, F3.1) — 미구현
        CombatLogEntity::class,
        // Phase 4 F4.0 (v5) — 구현 완료
        ConsentRecordEntity::class,        // v5 (F4.0)
        // Phase 4 F4.1~F4.4 (v6-v8) — 미구현
        ItemEntity::class,                 // v6 (F4.1)
        CharacterItemEntity::class,        // v6 (F4.1)
        NpcEntity::class,                  // v7 (F4.2)
        EncounterLogEntity::class,         // v8 (F4.4)
        XpAwardEntity::class,              // v8 (F4.4)
        // Phase 5 (v9-v11) — 미구현
        WeeklyReviewEntity::class,         // v9
        AchievementEntity::class,          // v10
        CharacterAchievementEntity::class, // v11
        // Phase 6 (v12, F6.1) — 미구현
        MemoryEntryEntity::class
    ],
    version = 12,                          // §5.6.1 SSOT 의 최신 버전과 일치
    exportSchema = true,
    autoMigrations = [
        // v1 → v2 는 **없음**: v1은 한 번도 persisted된 적 없음 (§5.6.1 v1 row 참조).
        // v2 가 fresh install 의 첫 활성 스키마.
        AutoMigration(from = 2, to = 3),       // F2.1 CharacterEntity
        // 3 → 4 는 수동 (TaskEntity 컬럼 추가, §5.6.2 MIGRATION_3_4)
        AutoMigration(from = 4, to = 5),       // F4.0 ConsentRecord — 구현 완료
        AutoMigration(from = 5, to = 6),       // F4.1 Item + CharacterItem
        AutoMigration(from = 6, to = 7),       // F4.2 Npc
        // 7 → 8 은 수동 (UNIQUE(encounterId) 강제, §5.6.2 MIGRATION_7_8)
        AutoMigration(from = 8, to = 9),       // F5.x WeeklyReview
        AutoMigration(from = 9, to = 10),      // F5.x Achievement
        AutoMigration(from = 10, to = 11),     // F5.x CharacterAchievement
        AutoMigration(from = 11, to = 12)      // F6.1 MemoryEntry
    ]
)
@TypeConverters(Converters::class)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun inboxItemDao(): InboxItemDao              // v2 (F1.1) — 구현됨
    abstract fun taskDao(): TaskDao                        // v2 (F1.1) — 구현됨
    abstract fun projectDao(): ProjectDao                  // v2 (F1.1) — 구현됨
    abstract fun characterDao(): CharacterDao              // v3 (F2.1)
    abstract fun combatLogDao(): CombatLogDao              // v4 (F3.1)
    abstract fun completionDao(): CompletionDao            // F3.1 원자성 DAO
    abstract fun consentRecordDao(): ConsentRecordDao      // v5 (F4.0) — 구현 완료
    abstract fun npcDao(): NpcDao                          // v7 (F4.2)
    abstract fun itemDao(): ItemDao                        // v6 (F4.1)
    abstract fun characterItemDao(): CharacterItemDao      // v6 (F4.1)
    abstract fun encounterLogDao(): EncounterLogDao        // v8 (F4.4)
    abstract fun claimEncounterRewardDao(): ClaimEncounterRewardDao  // F4.4 원자성 DAO
    abstract fun weeklyReviewDao(): WeeklyReviewDao        // v9 (F5.x)
    abstract fun achievementDao(): AchievementDao          // v10 (F5.x)

    abstract fun memoryDao(): MemoryDao                    // v12 (F6.1)

    companion object {
        const val DATABASE_NAME = "questlog.db"
    }
}
```

> **금지 사항**:
> - ❌ `version = 1`로 되돌리기 (v1은 의미적 placeholder일 뿐, 어떤 시점에도 활성 스키마가 아니다)
> - ❌ AutoMigration(1, 2) 추가 (§5.6.1 v1 row 참조 — 한 번도 persisted된 적 없음)
> - ❌ `fallbackToDestructiveMigration()` 호출
> - ❌ §5.6.1 표에 없는 임의 버전 증분 — PR 머지 전 표 갱신 필수
> - ❌ 현재 §5.6.4 코드 블록을 §5.6.5 계획 정의로 바꾸기 — phase 진행에 따라 점진적으로 갱신할 것

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
