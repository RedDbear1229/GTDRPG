package com.questlog.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.db.dao.CompletionDao
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.data.db.entity.CharacterEntity
import com.questlog.core.data.db.entity.CombatLogEntity
import com.questlog.core.data.db.entity.InboxItemEntity
import com.questlog.core.data.db.entity.ProjectEntity
import com.questlog.core.data.db.entity.TaskEntity

// 스키마 버전 히스토리:
//   v1 = persisted 된 적 없는 의미적 placeholder. AutoMigration(1, 2) / MIGRATION_1_2 금지.
//   v2 = Phase 1 fresh install 첫 활성 스키마 (Inbox/Task/Project).
//   v3 = Phase 2 F2.1 추가 — characters 테이블 신설. AutoMigration(2, 3) 가능.
//   v4 = Phase 3 F3.1 추가 — combat_logs 테이블 신설. AutoMigration(3, 4) 가능.
// schemas/N.json 모두 app/schemas/com.questlog.core.data.db.QuestLogDatabase/ 에 커밋 필수.
@Database(
    entities = [
        InboxItemEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
        CharacterEntity::class,
        CombatLogEntity::class,
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ],
)
@TypeConverters(Converters::class)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun inboxItemDao(): InboxItemDao
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun characterDao(): CharacterDao
    abstract fun completionDao(): CompletionDao
}
