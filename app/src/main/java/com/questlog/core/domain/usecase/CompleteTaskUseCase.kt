package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CombatLog
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.model.CompleteTaskResult
import com.questlog.core.domain.model.Item
import com.questlog.core.domain.repository.CharacterRepository
import com.questlog.core.domain.repository.CompletionRepository
import com.questlog.core.domain.repository.TaskRepository
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

// 원자성 계약 (CLAUDE.md §데이터 무결성):
//   CompletionRepository.completeTask() → CompletionDao.commitCompletion() 이 단일 진입점.
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val characterRepository: CharacterRepository,
    private val completionRepository: CompletionRepository,
    private val resolveCombat: ResolveCombatUseCase,
) {
    // equippedItems: ViewModel 이 현재 장착 아이템을 주입 (기본 emptyList = 장비 효과 없음)
    suspend operator fun invoke(
        taskId: String,
        equippedItems: List<Item> = emptyList(),
    ): CompleteTaskResult {
        val task = taskRepository.getById(taskId)
            ?: return CompleteTaskResult.Error("퀘스트를 찾을 수 없습니다")
        val character = characterRepository.getActive()
            ?: return CompleteTaskResult.Error("캐릭터가 없습니다")

        val combatResult = resolveCombat(task, character, equippedItems)
        val now = System.currentTimeMillis()
        val stats = combatStats(combatResult)
        val updatedCharacter = applyResult(character, stats, now)
        val log = buildLog(taskId, character, stats, now)

        val committed = completionRepository.completeTask(taskId, log, updatedCharacter, now)
        return if (!committed) CompleteTaskResult.AlreadyCompleted
        else CompleteTaskResult.Success(combatResult)
    }

    private data class Stats(
        val xpGained: Long, val hpLost: Int,
        val isCrit: Boolean, val isCritMiss: Boolean,
        val d20: Int, val totalAttack: Int, val monsterAC: Int,
    )

    private fun combatStats(result: CombatResult): Stats = when (result) {
        is CombatResult.Hit ->
            Stats(result.xpGained, 0, false, false, result.d20Result, result.totalAttack, result.monsterAC)
        is CombatResult.CriticalHit ->
            Stats(result.xpGained, 0, true, false, result.d20Result, 26, 0)
        is CombatResult.Miss ->
            Stats(0, result.hpLost, false, false, result.d20Result, result.totalAttack, result.monsterAC)
        is CombatResult.CriticalMiss ->
            Stats(0, result.hpLost, false, true, result.d20Result, 0, 0)
    }

    private fun applyResult(character: Character, s: Stats, now: Long): Character {
        val newTotalXp = character.totalXpEarned + s.xpGained
        val newLevel = XpThresholds.levelForXp(newTotalXp).coerceAtMost(XpThresholds.MAX_LEVEL)
        val newCurrentXp = (newTotalXp - XpThresholds.cumulativeForLevel(newLevel)).coerceAtLeast(0)
        val newMaxHp = HpCalculator.maxHp(character.classType, newLevel, character.constitution)

        val todayStart = todayStartMillis()
        val lastActivity = character.lastActivityDate ?: 0L
        val isConsecutive = lastActivity in (todayStart - DAY_MILLIS) until todayStart
        val newStreak = if (isConsecutive) character.streakDays + 1 else 1

        return character.copy(
            level = newLevel,
            currentXp = newCurrentXp,
            totalXpEarned = newTotalXp,
            proficiencyBonus = ProficiencyBonus.forLevel(newLevel),
            maxHp = newMaxHp,
            currentHp = (character.currentHp - s.hpLost).coerceIn(0, newMaxHp),
            streakDays = newStreak,
            longestStreak = maxOf(character.longestStreak, newStreak),
            lastActivityDate = now,
            totalQuestsCompleted = character.totalQuestsCompleted + 1,
            totalMonstersSlain = character.totalMonstersSlain + 1,
            totalCriticalHits = character.totalCriticalHits + if (s.isCrit) 1 else 0,
            totalCriticalMisses = character.totalCriticalMisses + if (s.isCritMiss) 1 else 0,
            totalXpFromCriticals = character.totalXpFromCriticals + if (s.isCrit) s.xpGained else 0,
            updatedAt = now,
        )
    }

    private fun buildLog(taskId: String, character: Character, s: Stats, now: Long) = CombatLog(
        id = UUID.randomUUID().toString(),
        taskId = taskId,
        characterId = character.id,
        d20Result = s.d20,
        totalAttack = s.totalAttack,
        monsterAC = s.monsterAC,
        xpGained = s.xpGained,
        hpLost = s.hpLost,
        isCriticalHit = s.isCrit,
        isCriticalMiss = s.isCritMiss,
        rolledAt = now,
    )

    private fun todayStartMillis(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}
