package com.questlog.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 하루 1엔트리 보장: entryDate UNIQUE 제약.
// INSERT OR ABORT (OnConflictStrategy.ABORT) 로 중복 삽입 시 SQLiteConstraintException 발생 → Repository 가 AlreadyExists 로 변환.
// taskId: TaskEntity 삭제 시 SET NULL (기억은 유지).
// characterId: CharacterEntity 삭제 시 CASCADE (캐릭터 삭제 시 기억도 삭제).
@Entity(
    tableName = "memory_entries",
    indices = [
        Index(value = ["entryDate"], unique = true),
        Index(value = ["characterId"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class MemoryEntryEntity(
    @PrimaryKey val id: String,
    val entryDate: String,           // "yyyy-MM-dd" local tz, UNIQUE
    val characterId: String,
    val taskId: String?,             // SET NULL on task delete
    val taskTitleSnapshot: String,   // 컨텍스트 보존용 (task 삭제 후에도 유지)
    val outcomeType: String,         // STRONG_HIT | WEAK_HIT | MISS | NONE
    val body: String,                // 사용자 본문 max 500자
    val enrichedBody: String?,       // Claude 윤색본 (nullable)
    val createdAt: Long,
    val sealedAt: Long,              // 다음날 00:00 epoch ms — 이후 편집 불가
)
