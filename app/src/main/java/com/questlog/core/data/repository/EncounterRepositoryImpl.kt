package com.questlog.core.data.repository

import com.questlog.core.data.EncounterTemplates
import com.questlog.core.data.db.dao.ClaimEncounterRewardDao
import com.questlog.core.data.db.dao.EncounterLogDao
import com.questlog.core.data.db.entity.EncounterLogEntity
import com.questlog.core.data.db.entity.XpAwardEntity
import com.questlog.core.data.mapper.toEntity
import com.questlog.core.data.mapper.toDomain
import com.questlog.core.domain.model.ClaimResult
import com.questlog.core.domain.model.EncounterLog
import com.questlog.core.domain.model.EncounterStatus
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.EncounterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncounterRepositoryImpl @Inject constructor(
    private val encounterLogDao: EncounterLogDao,
    private val claimEncounterRewardDao: ClaimEncounterRewardDao,
    private val characterRepository: CharacterRepository,
) : EncounterRepository {

    override fun getPending(): Flow<List<EncounterLog>> =
        encounterLogDao.getPending().map { list -> list.map { it.toDomain() } }

    override fun getAll(): Flow<List<EncounterLog>> =
        encounterLogDao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getPendingCount(): Flow<Int> = encounterLogDao.getPendingCount()

    override suspend fun getById(id: String): EncounterLog? =
        encounterLogDao.getById(id)?.toDomain()

    override suspend fun pendingCountForTemplate(key: String): Int =
        encounterLogDao.pendingCountForTemplate(key)

    override suspend fun pendingCountSince(since: Long): Int =
        encounterLogDao.pendingCountSince(since)

    override suspend fun generateEncounter(characterLevel: Int): EncounterLog? {
        val template = EncounterTemplates.forCr(characterLevel.toFloat()).randomOrNull() ?: return null
        if (encounterLogDao.pendingCountForTemplate(template.key) > 0) return null

        val now = System.currentTimeMillis()
        val entity = EncounterLogEntity(
            id = UUID.randomUUID().toString(),
            templateKey = template.key,
            status = EncounterStatus.PENDING.name,
            generatedAt = now,
            claimedAt = null,
            expiresAt = now + ENCOUNTER_TTL_MS,
            rewardXp = template.baseXp,
            rewardItemId = null,
        )
        val inserted = encounterLogDao.insert(entity)
        return if (inserted != -1L) entity.toDomain() else null
    }

    // rewardXp와 updatedCharacter는 ClaimEncounterRewardUseCase에서 계산 후 전달
    override suspend fun claimReward(
        encounterId: String,
        characterId: String,
        rewardXp: Long,
    ): ClaimResult {
        val character = characterRepository.getById(characterId) ?: return ClaimResult.AlreadyClaimedOrExpired
        val now = System.currentTimeMillis()
        val xpAward = XpAwardEntity(
            encounterId = encounterId,
            characterId = characterId,
            xpAmount = rewardXp,
            awardedAt = now,
        )
        val updatedEntity = character.copy(updatedAt = now).toEntity()
        return claimEncounterRewardDao.commitClaim(encounterId, now, xpAward, updatedEntity)
    }

    override suspend fun expireOld(): Int = encounterLogDao.expireOld()

    companion object {
        const val ENCOUNTER_TTL_MS = 24L * 60 * 60 * 1000
    }
}
