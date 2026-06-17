package com.questlog.core.domain.usecase

import com.questlog.core.domain.model.AbilityType
import com.questlog.core.domain.model.Character
import com.questlog.core.domain.model.CharacterClass
import com.questlog.core.domain.model.CombatResult
import com.questlog.core.domain.model.EquipmentSlot
import com.questlog.core.domain.model.Item
import com.questlog.core.domain.model.ItemRarity
import com.questlog.core.domain.model.ItemType
import com.questlog.core.domain.model.LifeArea
import com.questlog.core.domain.model.MonsterType
import com.questlog.core.domain.model.Task
import com.questlog.core.domain.model.TaskStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class EquipmentBonusTest {

    private val character = Character(
        name = "Tester", classType = CharacterClass.FIGHTER, level = 1,
        maxHp = 12, currentHp = 12,
        strength = 10, dexterity = 10, constitution = 10,
        intelligence = 10, wisdom = 10, charisma = 10,
        proficiencyBonus = 2,
    )

    private val task = Task(
        title = "테스트",
        status = TaskStatus.ACTIVE,
        lifeArea = LifeArea.WORK,
        primaryAbility = AbilityType.STR,
        challengeRating = 2f,
        monsterType = MonsterType.ORC,
    )

    private fun weapon(attackBonus: Int, xpMultiplier: Float = 1.0f) = Item(
        id = "item-1", itemKey = "w_test", name = "테스트 무기",
        description = "", flavorText = null,
        itemType = ItemType.WEAPON, rarity = ItemRarity.COMMON,
        slot = EquipmentSlot.WEAPON,
        attackBonus = attackBonus, defenseBonus = 0,
        xpMultiplier = xpMultiplier, hpBonusFlat = 0,
        specialEffectCode = null,
        isEquipped = true, equippedSlot = EquipmentSlot.WEAPON,
        acquiredAt = 0L, characterId = "char-1", acquiredFromTaskId = null,
    )

    private fun xpRing(multiplier: Float) = Item(
        id = "item-2", itemKey = "r_test", name = "테스트 반지",
        description = "", flavorText = null,
        itemType = ItemType.RING, rarity = ItemRarity.UNCOMMON,
        slot = EquipmentSlot.RING,
        attackBonus = 0, defenseBonus = 0,
        xpMultiplier = multiplier, hpBonusFlat = 0,
        specialEffectCode = null,
        isEquipped = true, equippedSlot = EquipmentSlot.RING,
        acquiredAt = 0L, characterId = "char-1", acquiredFromTaskId = null,
    )

    // D20=8: STR 0 + proficiency 2 = 10 < CR2 AC(12) → Miss; with weapon+2 → 12 = Hit
    private fun makeUseCase(fixedRoll: Int) = ResolveCombatUseCase(
        random = object : Random() {
            override fun nextBits(bitCount: Int): Int = fixedRoll - 1
            // Override directly to avoid JVM `1 shl 32 == 1` masking bug in nextBits
            override fun nextInt(from: Int, until: Int): Int = fixedRoll.coerceIn(from, until - 1)
        }
    )

    @Test
    fun `무기 ATK 보너스 적용 시 totalAttack 증가`() {
        // D20=8, STR modifier=0, proficiency=2 → 기본 10
        // CR2 AC=11 → 기본이면 Miss
        // 무기 +2 → totalAttack=12 → Hit
        val useCase = makeUseCase(8)
        val noWeapon = useCase(task, character, emptyList())
        val useCase2 = makeUseCase(8)
        val withWeapon = useCase2(task, character, listOf(weapon(attackBonus = 2)))

        assertTrue(noWeapon is CombatResult.Miss, "장비 없이 8 = Miss 여야 함")
        assertTrue(withWeapon is CombatResult.Hit, "ATK+2 무기 장착 시 Hit 이어야 함")
    }

    @Test
    fun `XP 배율 장비 적용 시 XP 증가`() {
        val useCase = ResolveCombatUseCase(Random.Default)
        val base = useCase.calculateXp(task, character, emptyList(), isCritical = false)
        val boosted = useCase.calculateXp(task, character, listOf(xpRing(1.5f)), isCritical = false)
        assertEquals((base * 1.5f).toLong(), boosted)
    }

    @Test
    fun `장비 없을 때 calculateXp 결과가 기존과 동일`() {
        val useCase = ResolveCombatUseCase(Random.Default)
        val withEmpty = useCase.calculateXp(task, character, emptyList(), isCritical = false)
        val legacyCall = useCase.calculateXp(task, character, isCritical = false)
        assertEquals(legacyCall, withEmpty)
    }
}
