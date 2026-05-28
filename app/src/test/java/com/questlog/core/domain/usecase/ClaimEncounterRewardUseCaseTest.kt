package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.ClaimResult
import com.questlog.core.domain.model.EncounterLog
import com.questlog.core.domain.model.EncounterStatus
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.EncounterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class ClaimEncounterRewardUseCaseTest {

    private val encounterRepository = mockk<EncounterRepository>(relaxUnitFun = true)
    private val characterRepository = mockk<CharacterRepository>(relaxUnitFun = true)
    private val useCase = ClaimEncounterRewardUseCase(encounterRepository, characterRepository)

    private val baseCharacter = Character(
        id = "char-1",
        name = "Hero",
        classType = CharacterClass.FIGHTER,
        level = 1,
        currentXp = 0,
        totalXpEarned = 0,
        maxHp = 12,
        currentHp = 12,
        strength = 16, dexterity = 12, constitution = 14,
        intelligence = 8, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
    )

    private fun pendingEncounter(id: String = "enc-1", rewardXp: Long = 200L) = EncounterLog(
        id = id,
        templateKey = "enc_deadline_orc",
        title = "마감일 오크",
        description = "마감일이 다가온다",
        flavorText = "\"마감은 지배자다.\"",
        status = EncounterStatus.PENDING,
        generatedAt = System.currentTimeMillis(),
        claimedAt = null,
        expiresAt = System.currentTimeMillis() + 3_600_000L,
        rewardXp = rewardXp,
        rewardItemId = null,
    )

    @Test
    fun `활성 캐릭터 없으면 NoCharacter 반환`() = runTest {
        coEvery { characterRepository.getActive() } returns null

        val result = useCase("enc-1")

        assertInstanceOf(ClaimEncounterRewardUseCase.Result.NoCharacter::class.java, result)
    }

    @Test
    fun `인카운터 없으면 NotFound 반환`() = runTest {
        coEvery { characterRepository.getActive() } returns baseCharacter
        coEvery { encounterRepository.getById("enc-1") } returns null

        val result = useCase("enc-1")

        assertInstanceOf(ClaimEncounterRewardUseCase.Result.NotFound::class.java, result)
    }

    @Test
    fun `정상 클레임 시 XP 획득 및 캐릭터 업데이트`() = runTest {
        val enc = pendingEncounter(rewardXp = 200L)
        coEvery { characterRepository.getActive() } returns baseCharacter
        coEvery { encounterRepository.getById(enc.id) } returns enc
        coEvery { encounterRepository.claimReward(enc.id, baseCharacter.id, 200L) } returns ClaimResult.Success

        val updatedSlot = slot<Character>()
        coEvery { characterRepository.upsert(capture(updatedSlot)) } returns Unit

        val result = useCase(enc.id)

        assertInstanceOf(ClaimEncounterRewardUseCase.Result.Success::class.java, result)
        val success = result as ClaimEncounterRewardUseCase.Result.Success
        assertEquals(200L, success.xpGained)
        assertEquals(200L, updatedSlot.captured.totalXpEarned)
    }

    @Test
    fun `중복 클레임 시 AlreadyClaimed 반환`() = runTest {
        val enc = pendingEncounter()
        coEvery { characterRepository.getActive() } returns baseCharacter
        coEvery { encounterRepository.getById(enc.id) } returns enc
        coEvery { encounterRepository.claimReward(enc.id, baseCharacter.id, enc.rewardXp) } returns ClaimResult.AlreadyClaimedOrExpired

        val result = useCase(enc.id)

        assertInstanceOf(ClaimEncounterRewardUseCase.Result.AlreadyClaimed::class.java, result)
        coVerify(exactly = 0) { characterRepository.upsert(any()) }
    }

    @Test
    fun `만료된 인카운터는 AlreadyClaimed 반환`() = runTest {
        val expired = pendingEncounter().copy(
            status = EncounterStatus.EXPIRED,
            expiresAt = System.currentTimeMillis() - 1000L,
        )
        coEvery { characterRepository.getActive() } returns baseCharacter
        coEvery { encounterRepository.getById(expired.id) } returns expired

        val result = useCase(expired.id)

        assertInstanceOf(ClaimEncounterRewardUseCase.Result.AlreadyClaimed::class.java, result)
    }

    @Test
    fun `레벨업 경계 XP에서 레벨 정확히 계산`() = runTest {
        val levelUpCharacter = baseCharacter.copy(totalXpEarned = 290L) // 300이면 Lv2
        val enc = pendingEncounter(rewardXp = 10L)
        coEvery { characterRepository.getActive() } returns levelUpCharacter
        coEvery { encounterRepository.getById(enc.id) } returns enc
        coEvery { encounterRepository.claimReward(any(), any(), any()) } returns ClaimResult.Success

        val updatedSlot = slot<Character>()
        coEvery { characterRepository.upsert(capture(updatedSlot)) } returns Unit

        val result = useCase(enc.id)

        assertInstanceOf(ClaimEncounterRewardUseCase.Result.Success::class.java, result)
        assertEquals(2, updatedSlot.captured.level)
    }
}
