package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.CombatLog
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.model.CompleteTaskResult
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.MonsterType
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.TaskStatus
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.CompletionRepository
import com.questlog.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CompleteTaskUseCaseTest {

    private val task = Task(
        id = "task-1",
        title = "테스트",
        status = TaskStatus.ACTIVE,
        lifeArea = LifeArea.WORK,
        primaryAbility = AbilityType.STR,
        challengeRating = 2f,
        monsterType = MonsterType.ORC,
    )

    private val character = Character(
        id = "char-1",
        name = "Tester",
        classType = CharacterClass.FIGHTER,
        level = 1,
        maxHp = 12,
        currentHp = 12,
        strength = 16, dexterity = 10, constitution = 14,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
    )

    private fun fakeTaskRepo(task: Task?): TaskRepository = object : TaskRepository {
        override fun observeInbox(): Flow<List<Task>> = flowOf(emptyList())
        override fun observeActive(): Flow<List<Task>> = flowOf(listOf(task).filterNotNull())
        override fun observeByProject(id: String): Flow<List<Task>> = flowOf(emptyList())
        override fun observeWaiting(): Flow<List<Task>> = flowOf(emptyList())
        override fun observeSomeday(): Flow<List<Task>> = flowOf(emptyList())
        override fun observeInboxCount(): Flow<Int> = flowOf(0)
        override fun observeDueToday(end: Long): Flow<List<Task>> = flowOf(emptyList())
        override fun observeCompleted(): Flow<List<Task>> = flowOf(emptyList())
        override fun observeCompletedCount(s: Long, e: Long): Flow<Int> = flowOf(0)
        override fun search(query: String): Flow<List<Task>> = flowOf(emptyList())
        override suspend fun getById(id: String): Task? = task?.takeIf { it.id == id }
        override suspend fun upsert(task: Task) {}
        override suspend fun softDelete(id: String) {}
    }

    private fun fakeCharRepo(character: Character?): CharacterRepository = object : CharacterRepository {
        private var stored = character
        override fun observeActive(): Flow<Character?> = flowOf(stored)
        override suspend fun getActive(): Character? = stored
        override suspend fun getById(id: String): Character? = stored?.takeIf { it.id == id }
        override suspend fun upsert(c: Character) { stored = c }
        override suspend fun delete(id: String) { stored = null }
    }

    // alwaysSuccess = true: commitCompletion returns true (first call)
    // alwaysSuccess = false: returns false (already completed)
    private fun fakeCompletionRepo(alwaysSuccess: Boolean): CompletionRepository =
        object : CompletionRepository {
            override suspend fun completeTask(taskId: String, log: CombatLog, updatedCharacter: Character, now: Long) = alwaysSuccess
        }

    private fun makeUseCase(
        taskRepo: TaskRepository = fakeTaskRepo(task),
        charRepo: CharacterRepository = fakeCharRepo(character),
        completionRepo: CompletionRepository = fakeCompletionRepo(true),
        fixedRoll: Int = 10,
    ): CompleteTaskUseCase {
        val random = object : Random() {
            override fun nextBits(bitCount: Int): Int = fixedRoll - 1
        }
        return CompleteTaskUseCase(taskRepo, charRepo, completionRepo, ResolveCombatUseCase(random))
    }

    @Test
    fun `정상 완료 시 Success 반환`() = runTest {
        val result = makeUseCase()(task.id)
        assertInstanceOf(CompleteTaskResult.Success::class.java, result)
    }

    @Test
    fun `더블탭 시 AlreadyCompleted 반환`() = runTest {
        val result = makeUseCase(completionRepo = fakeCompletionRepo(false))(task.id)
        assertEquals(CompleteTaskResult.AlreadyCompleted, result)
    }

    @Test
    fun `태스크 없으면 Error 반환`() = runTest {
        val result = makeUseCase(taskRepo = fakeTaskRepo(null))("unknown-id")
        assertInstanceOf(CompleteTaskResult.Error::class.java, result)
    }

    @Test
    fun `캐릭터 없으면 Error 반환`() = runTest {
        val result = makeUseCase(charRepo = fakeCharRepo(null))(task.id)
        assertInstanceOf(CompleteTaskResult.Error::class.java, result)
    }

    @Test
    fun `CriticalHit(D20=20) 시 Success 반환`() = runTest {
        val result = makeUseCase(fixedRoll = 20)(task.id)
        assertInstanceOf(CompleteTaskResult.Success::class.java, result)
        val success = result as CompleteTaskResult.Success
        assertInstanceOf(CombatResult.CriticalHit::class.java, success.combatResult)
    }

    @Test
    fun `CriticalMiss(D20=1) 시 Success 반환 (태스크는 완료)`() = runTest {
        val result = makeUseCase(fixedRoll = 1)(task.id)
        assertInstanceOf(CompleteTaskResult.Success::class.java, result)
        val success = result as CompleteTaskResult.Success
        assertInstanceOf(CombatResult.CriticalMiss::class.java, success.combatResult)
    }
}
