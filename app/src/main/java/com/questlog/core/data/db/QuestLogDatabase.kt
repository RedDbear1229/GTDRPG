package com.questlog.core.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.questlog.core.data.db.dao.CharacterDao
import com.questlog.core.data.db.dao.InboxItemDao
import com.questlog.core.data.db.dao.ProjectDao
import com.questlog.core.data.db.dao.TaskDao
import com.questlog.core.data.db.entity.CharacterEntity
import com.questlog.core.data.db.entity.InboxItemEntity
import com.questlog.core.data.db.entity.ProjectEntity
import com.questlog.core.data.db.entity.TaskEntity

// 스키마 버전 히스토리:
//   v1 = persisted 된 적 없는 의미적 placeholder. AutoMigration(1, 2) / MIGRATION_1_2 금지.
//   v2 = Phase 1 fresh install 첫 활성 스키마 (Inbox/Task/Project).
//   v3 = Phase 2 F2.1 추가 — characters 테이블 신설. 새 테이블 추가만이므로 AutoMigration(2, 3) 가능.
// schemas/2.json, schemas/3.json 모두 `app/schemas/com.questlog.core.data.db.QuestLogDatabase/` 에 커밋 필수
// (docs/05_data_model.md §5.6 SSOT). 마이그 테스트는 v2 → v3 시드 로드 후 Task/Project 무손실 + characters 빈 테이블 확인.
@Database(
    entities = [
        InboxItemEntity::class,
        TaskEntity::class,
        ProjectEntity::class,
        CharacterEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
    ],
)
@TypeConverters(Converters::class)
abstract class QuestLogDatabase : RoomDatabase() {
    abstract fun inboxItemDao(): InboxItemDao
    abstract fun taskDao(): TaskDao
    abstract fun projectDao(): ProjectDao
    abstract fun characterDao(): CharacterDao
}
