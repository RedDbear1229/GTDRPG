package com.questlog.core.domain.usecase

import com.questlog.core.data.privacy.ConsentManager
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.Npc
import com.questlog.core.domain.model.NpcSource
import com.questlog.core.domain.model.TaskStatus
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.MonsterType
import com.questlog.core.domain.repository.NpcRepository
import com.questlog.core.domain.repository.TaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class NpcPrivacyTest {

    private val taskRepository = mockk<TaskRepository>(relaxUnitFun = true)
    private val consentManager = mockk<ConsentManager>()
    private val npcRepository = mockk<NpcRepository>(relaxUnitFun = true)

    private val delegateUseCase = DelegateTaskUseCase(taskRepository, consentManager)
    private val deleteContactsUseCase = DeleteImportedContactsUseCase(npcRepository)

    private val pickerNpc = Npc(
        id = "npc-1",
        name = "Alice",
        displayName = "Alice Smith",
        phoneNumber = "010-1234-5678",
        classType = CharacterClass.BARD,
        source = NpcSource.PICKER,
    )

    private val manualNpc = Npc(
        id = "npc-2",
        name = "Bob",
        classType = CharacterClass.FIGHTER,
        source = NpcSource.MANUAL,
    )

    private val task = Task(
        id = "task-1",
        title = "테스트 퀘스트",
        status = TaskStatus.ACTIVE,
        lifeArea = LifeArea.WORK,
        primaryAbility = AbilityType.STR,
        challengeRating = 2f,
        monsterType = MonsterType.ORC,
    )

    @Test
    fun `연락처 동의 없을 때 PICKER NPC에 태스크 위임 불가`() = runTest {
        coEvery { consentManager.canImportContacts() } returns false

        val result = delegateUseCase("task-1", pickerNpc)

        assertInstanceOf(DelegateTaskUseCase.Result.ContactsConsentRequired::class.java, result)
        coVerify(exactly = 0) { taskRepository.upsert(any()) }
    }

    @Test
    fun `연락처 동의 없어도 MANUAL NPC에 태스크 위임 가능`() = runTest {
        coEvery { consentManager.canImportContacts() } returns false
        coEvery { taskRepository.getById("task-1") } returns task

        val result = delegateUseCase("task-1", manualNpc)

        assertInstanceOf(DelegateTaskUseCase.Result.Success::class.java, result)
        coVerify(exactly = 1) { taskRepository.upsert(match { it.status == TaskStatus.WAITING }) }
    }

    @Test
    fun `연락처 동의 있을 때 PICKER NPC에 태스크 위임 성공`() = runTest {
        coEvery { consentManager.canImportContacts() } returns true
        coEvery { taskRepository.getById("task-1") } returns task

        val result = delegateUseCase("task-1", pickerNpc)

        assertInstanceOf(DelegateTaskUseCase.Result.Success::class.java, result)
        val success = result as DelegateTaskUseCase.Result.Success
        assert(success.npcId == pickerNpc.id)
    }

    @Test
    fun `위임 성공 시 태스크 상태가 WAITING이고 delegatedTo가 NPC ID`() = runTest {
        coEvery { consentManager.canImportContacts() } returns true
        coEvery { taskRepository.getById("task-1") } returns task

        delegateUseCase("task-1", pickerNpc)

        coVerify(exactly = 1) {
            taskRepository.upsert(match {
                it.status == TaskStatus.WAITING &&
                    it.delegatedTo == pickerNpc.id &&
                    it.delegatedAt != null
            })
        }
    }

    @Test
    fun `DeleteImportedContactsUseCase 호출 시 npcRepository clearContactData 실행`() = runTest {
        deleteContactsUseCase()
        coVerify(exactly = 1) { npcRepository.clearContactData() }
    }

    @Test
    fun `존재하지 않는 태스크 위임 시 Error 반환`() = runTest {
        coEvery { consentManager.canImportContacts() } returns true
        coEvery { taskRepository.getById("nonexistent") } returns null

        val result = delegateUseCase("nonexistent", pickerNpc)

        assertInstanceOf(DelegateTaskUseCase.Result.Error::class.java, result)
    }
}
