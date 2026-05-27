package com.questlog.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.db.dao.CharacterItemDao
import com.questlog.core.data.db.dao.CompletionDao
import com.questlog.core.data.db.dao.ConsentRecordDao
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.data.db.entity.CharacterEntity
import com.questlog.core.data.db.entity.CharacterItemEntity
import com.questlog.core.data.db.entity.CombatLogEntity
import com.questlog.core.data.db.entity.ConsentRecordEntity
import com.questlog.core.data.db.entity.InboxItemEntity
import com.questlog.core.data.db.entity.ItemEntity
import com.questlog.core.data.db.entity.ProjectEntity
import com.questlog.core.data.db.entity.TaskEntity

// 스키마 버전 히스토리:
//   v1 = 의미적 placeholder. AutoMigration(1, 2) 금지.
//   v2 = Phase 1 (Inbox/Task/Project).
//   v3 = Phase 2 F2.1 — characters 테이블. AutoMigration(2, 3).
//   v4 = Phase 3 F3.1 — combat_logs 테이블. AutoMigration(3, 4).
//   v5 = Phase 4 F4.0 — consent_records 테이블. AutoMigration(4, 5).
//   v6 = Phase 4 F4.1 — items + character_items 테이블. MIGRATION_5_6 (수동, 부분 인덱스 포함).
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
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ],
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
