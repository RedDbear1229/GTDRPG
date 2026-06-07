package com.questlog.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.db.dao.CharacterItemDao
import com.questlog.core.data.db.dao.ClaimEncounterRewardDao
import com.questlog.core.data.db.dao.CombatLogDao
import com.questlog.core.data.db.dao.CompletionDao
import com.questlog.core.data.db.dao.ConsentRecordDao
import com.questlog.core.data.db.dao.EncounterLogDao
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.MemoryDao
import com.questlog.core.data.db.dao.NpcDao
import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.data.db.dao.WeeklyReviewDao
import com.questlog.core.data.db.entity.CharacterEntity
import com.questlog.core.data.db.entity.CharacterItemEntity
import com.questlog.core.data.db.entity.CombatLogEntity
import com.questlog.core.data.db.entity.ConsentRecordEntity
import com.questlog.core.data.db.entity.EncounterLogEntity
import com.questlog.core.data.db.entity.InboxItemEntity
import com.questlog.core.data.db.entity.ItemEntity
import com.questlog.core.data.db.entity.MemoryEntryEntity
import com.questlog.core.data.db.entity.NpcEntity
import com.questlog.core.data.db.entity.ProjectEntity
import com.questlog.core.data.db.entity.TaskEntity
import com.questlog.core.data.db.entity.WeeklyReviewEntity
import com.questlog.core.data.db.entity.XpAwardEntity

// 스키마 버전 히스토리:
//   v1 = 의미적 placeholder. AutoMigration(1, 2) 금지.
//   v2 = Phase 1 (Inbox/Task/Project).
//   v3 = Phase 2 F2.1 — characters 테이블. AutoMigration(2, 3).
//   v4 = Phase 3 F3.1 — combat_logs 테이블. AutoMigration(3, 4).
//   v5 = Phase 4 F4.0 — consent_records 테이블. AutoMigration(4, 5).
//   v6 = Phase 4 F4.1 — items + character_items 테이블. MIGRATION_5_6 (수동, 부분 인덱스 포함).
//   v7 = Phase 4 F4.2 — npcs 테이블. MIGRATION_6_7 (수동).
//   v8 = Phase 4 F4.4 — encounter_logs + xp_awards 테이블. MIGRATION_7_8 (수동).
//   v9 = Phase 5 F5.3 — weekly_reviews 테이블. MIGRATION_8_9 (수동).
//   v10 = Phase 6 F6.1 — memory_entries 테이블. MIGRATION_9_10 (수동).
// schemas/N.json 모두 app/schemas/com.questlog.core.data.db.QuestLogDatabase/ 커밋 필수.
@Database(
    entities = [
        InboxItemEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
        CharacterEntity::class,
        CombatLogEntity::class,
        ConsentRecordEntity::class,
        ItemEntity::class,
        CharacterItemEntity::class,
        NpcEntity::class,
        EncounterLogEntity::class,
        XpAwardEntity::class,
        WeeklyReviewEntity::class,
        MemoryEntryEntity::class,
    ],
    version = 10,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun inboxItemDao(): InboxItemDao
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun characterDao(): CharacterDao
    abstract fun completionDao(): CompletionDao
    abstract fun consentRecordDao(): ConsentRecordDao
    abstract fun characterItemDao(): CharacterItemDao
    abstract fun npcDao(): NpcDao
    abstract fun encounterLogDao(): EncounterLogDao
    abstract fun claimEncounterRewardDao(): ClaimEncounterRewardDao
    abstract fun weeklyReviewDao(): WeeklyReviewDao
    abstract fun combatLogDao(): CombatLogDao
    abstract fun memoryDao(): MemoryDao
}

// v2→v3: characters 테이블 추가 (AutoMigration 대체 — 스키마 JSON 불필요)
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `characters` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `name` TEXT NOT NULL,
                `classType` TEXT NOT NULL,
                `avatarResId` INTEGER NOT NULL DEFAULT 0,
                `backstory` TEXT,
                `level` INTEGER NOT NULL DEFAULT 1,
                `currentXp` INTEGER NOT NULL DEFAULT 0,
                `totalXpEarned` INTEGER NOT NULL DEFAULT 0,
                `maxHp` INTEGER NOT NULL,
                `currentHp` INTEGER NOT NULL,
                `strength` INTEGER NOT NULL,
                `dexterity` INTEGER NOT NULL,
                `constitution` INTEGER NOT NULL,
                `intelligence` INTEGER NOT NULL,
                `wisdom` INTEGER NOT NULL,
                `charisma` INTEGER NOT NULL,
                `proficiencyBonus` INTEGER NOT NULL,
                `armorClass` INTEGER NOT NULL DEFAULT 10,
                `streakDays` INTEGER NOT NULL DEFAULT 0,
                `longestStreak` INTEGER NOT NULL DEFAULT 0,
                `lastActivityDate` INTEGER,
                `streakProtectTokens` INTEGER NOT NULL DEFAULT 0,
                `totalQuestsCompleted` INTEGER NOT NULL DEFAULT 0,
                `totalMonstersSlain` INTEGER NOT NULL DEFAULT 0,
                `totalCriticalHits` INTEGER NOT NULL DEFAULT 0,
                `totalCriticalMisses` INTEGER NOT NULL DEFAULT 0,
                `totalXpFromCriticals` INTEGER NOT NULL DEFAULT 0,
                `classResourceCurrent` INTEGER NOT NULL DEFAULT 0,
                `classResourceMax` INTEGER NOT NULL DEFAULT 0,
                `classResourceLastRefresh` INTEGER,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

// v3→v4: combat_logs 테이블 추가 (AutoMigration 대체)
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `combat_logs` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `taskId` TEXT,
                `characterId` TEXT NOT NULL,
                `d20Result` INTEGER NOT NULL,
                `totalAttack` INTEGER NOT NULL,
                `monsterAC` INTEGER NOT NULL,
                `xpGained` INTEGER NOT NULL,
                `hpLost` INTEGER NOT NULL,
                `isCriticalHit` INTEGER NOT NULL,
                `isCriticalMiss` INTEGER NOT NULL,
                `rolledAt` INTEGER NOT NULL,
                FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON DELETE SET NULL,
                FOREIGN KEY(`characterId`) REFERENCES `characters`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_combat_logs_taskId` ON `combat_logs` (`taskId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_combat_logs_characterId` ON `combat_logs` (`characterId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_combat_logs_rolledAt` ON `combat_logs` (`rolledAt`)")
    }
}

// v4→v5: consent_records 테이블 추가 (AutoMigration 대체)
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `consent_records` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `scope` TEXT NOT NULL,
                `policyVersion` INTEGER NOT NULL,
                `acceptedAt` INTEGER NOT NULL,
                `revokedAt` INTEGER
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_consent_records_scope` ON `consent_records` (`scope`)")
    }
}

// 수동 마이그레이션: items + character_items 추가.
// AutoMigration 대신 수동 사용 이유: (characterId, equippedSlot) 부분 인덱스(WHERE isEquipped=1)는
// Room @Entity 어노테이션으로 표현 불가 → 직접 SQL 실행.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `items` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `itemKey` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `flavorText` TEXT,
                `itemType` TEXT NOT NULL,
                `rarity` TEXT NOT NULL,
                `slot` TEXT NOT NULL,
                `attackBonus` INTEGER NOT NULL DEFAULT 0,
                `defenseBonus` INTEGER NOT NULL DEFAULT 0,
                `xpMultiplier` REAL NOT NULL DEFAULT 1.0,
                `hpBonusFlat` INTEGER NOT NULL DEFAULT 0,
                `specialEffectCode` TEXT
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `character_items` (
                `characterId` TEXT NOT NULL,
                `itemId` TEXT NOT NULL,
                `isEquipped` INTEGER NOT NULL DEFAULT 0,
                `equippedSlot` TEXT,
                `acquiredAt` INTEGER NOT NULL,
                `acquiredFromTaskId` TEXT,
                `acquiredFromEncounterId` TEXT,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`characterId`, `itemId`),
                FOREIGN KEY(`characterId`) REFERENCES `characters`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`itemId`) REFERENCES `items`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_character_items_characterId` ON `character_items` (`characterId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_character_items_itemId` ON `character_items` (`itemId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_character_items_equippedSlot` ON `character_items` (`equippedSlot`)")
        // 슬롯 단일성: 장착 중인 아이템은 슬롯당 1개만 허용
        db.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS `index_character_items_equipped_slot_unique`
            ON `character_items` (`characterId`, `equippedSlot`)
            WHERE isEquipped = 1
        """.trimIndent())
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `encounter_logs` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `templateKey` TEXT NOT NULL,
                `status` TEXT NOT NULL,
                `generatedAt` INTEGER NOT NULL,
                `claimedAt` INTEGER,
                `expiresAt` INTEGER NOT NULL,
                `rewardXp` INTEGER NOT NULL,
                `rewardItemId` TEXT
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_encounter_logs_status_expiresAt` ON `encounter_logs` (`status`, `expiresAt`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_encounter_logs_generatedAt` ON `encounter_logs` (`generatedAt`)")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `xp_awards` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `encounterId` TEXT NOT NULL,
                `characterId` TEXT NOT NULL,
                `xpAmount` INTEGER NOT NULL,
                `awardedAt` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_xp_awards_encounterId` ON `xp_awards` (`encounterId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_xp_awards_characterId` ON `xp_awards` (`characterId`)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `weekly_reviews` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `weekStart` TEXT NOT NULL,
                `weekLabel` TEXT NOT NULL,
                `completedCount` INTEGER NOT NULL,
                `xpGained` INTEGER NOT NULL,
                `critCount` INTEGER NOT NULL,
                `missCount` INTEGER NOT NULL,
                `unfinishedCount` INTEGER NOT NULL,
                `aiSummary` TEXT,
                `xpReward` INTEGER NOT NULL DEFAULT 200,
                `completedAt` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_weekly_reviews_weekStart` ON `weekly_reviews` (`weekStart`)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `npcs` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `name` TEXT NOT NULL,
                `displayName` TEXT,
                `phoneNumber` TEXT,
                `classType` TEXT NOT NULL,
                `source` TEXT NOT NULL,
                `notes` TEXT NOT NULL DEFAULT '',
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_npcs_source` ON `npcs` (`source`)")
    }
}

// v9→v10: memory_entries 테이블 추가.
// entryDate UNIQUE: 하루 1엔트리 DB 레벨 제약.
// taskId: SET NULL (Task 삭제 시 기억 유지).
// characterId: CASCADE (Character 삭제 시 기억 삭제).
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `memory_entries` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `entryDate` TEXT NOT NULL,
                `characterId` TEXT NOT NULL,
                `taskId` TEXT,
                `taskTitleSnapshot` TEXT NOT NULL,
                `outcomeType` TEXT NOT NULL,
                `body` TEXT NOT NULL,
                `enrichedBody` TEXT,
                `createdAt` INTEGER NOT NULL,
                `sealedAt` INTEGER NOT NULL,
                FOREIGN KEY(`characterId`) REFERENCES `characters`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON DELETE SET NULL
            )
        """.trimIndent())
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_memory_entries_entryDate` ON `memory_entries` (`entryDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_memory_entries_characterId` ON `memory_entries` (`characterId`)")
    }
}
