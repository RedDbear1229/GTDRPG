package com.questlog.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.questlog.core.data.db.entity.CharacterEntity
import com.questlog.core.data.db.entity.XpAwardEntity
import com.questlog.core.domain.model.ClaimResult

// 원자성 계약 (CLAUDE.md §데이터 무결성 — CompletionDao 동일 패턴):
//   commitClaim() 이 단일 @Transaction 진입점.
//   🔴 insertXpAward 반환값 -1L → 감사 무결성 위반 → throw (트랜잭션 롤백 보장).
//   🔴 conditionalClaim 0 rows → AlreadyClaimedOrExpired 즉시 반환.
//   🔴 OnConflictStrategy.IGNORE + UNIQUE(encounterId) → 중복 보상 원천 차단.
@Dao
interface ClaimEncounterRewardDao {

    @Transaction
    suspend fun commitClaim(
        encounterId: String,
        now: Long,
        xpAward: XpAwardEntity,
        updatedCharacter: CharacterEntity,
    ): ClaimResult {
        val rows = conditionalClaim(encounterId, now)
        if (rows == 0) return ClaimResult.AlreadyClaimedOrExpired

        val insertedId = insertXpAward(xpAward)
        if (insertedId == -1L) {
            // 정상 흐름에선 도달 불가능 — UNIQUE(encounterId)와 conditionalClaim 게이트가
            // 동시 만족되는 경우는 동일 트랜잭션 내 race 뿐. throw → 롤백.
            throw IllegalStateException(
                "Audit row missing for encounter=$encounterId despite successful claim. " +
                    "Possible deterministic-id collision or upstream contract violation."
            )
        }
        updateCharacterSnapshot(updatedCharacter)
        return ClaimResult.Success
    }

    @Query("""
        UPDATE encounter_logs
        SET status = 'CLAIMED', claimedAt = :now
        WHERE id = :encounterId
          AND status = 'PENDING'
          AND expiresAt > :now
    """)
    suspend fun conditionalClaim(encounterId: String, now: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertXpAward(award: XpAwardEntity): Long

    @Update
    suspend fun updateCharacterSnapshot(character: CharacterEntity)
}
