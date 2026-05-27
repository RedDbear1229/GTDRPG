package com.questlog.core.domain.model

// 04_game_mechanics.md §4.5 아이템 카탈로그 SSOT.
// 실제 게임 밸런싱은 실사용 후 보정.
object ItemCatalog {

    val ALL: List<ItemTemplate> = buildList {
        // ── WEAPON (무기) ────────────────────────────────────────
        add(ItemTemplate(
            key = "w_focus_quill", name = "집중력의 깃털펜", rarity = ItemRarity.COMMON,
            description = "INT 퀘스트 완료 시 추가 XP를 부여한다.",
            flavorText = "학자의 손에서 용사의 무기가 탄생했다.",
            itemType = ItemType.WEAPON, slot = EquipmentSlot.WEAPON,
            attackBonus = 1, xpMultiplier = 1.10f,
        ))
        add(ItemTemplate(
            key = "w_iron_dagger", name = "철의 단검", rarity = ItemRarity.COMMON,
            description = "기본 공격 보너스를 제공한다.",
            itemType = ItemType.WEAPON, slot = EquipmentSlot.WEAPON,
            attackBonus = 1,
        ))
        add(ItemTemplate(
            key = "w_flame_sword", name = "불꽃의 검", rarity = ItemRarity.UNCOMMON,
            description = "불꽃에 담긴 의지가 퀘스트 완료율을 높인다.",
            flavorText = "이 검에는 수천 번의 완료가 새겨져 있다.",
            itemType = ItemType.WEAPON, slot = EquipmentSlot.WEAPON,
            attackBonus = 1, xpMultiplier = 1.05f,
        ))
        add(ItemTemplate(
            key = "w_scholars_wand", name = "학자의 마법봉", rarity = ItemRarity.UNCOMMON,
            description = "집중력을 높여 INT 퀘스트에 특화된 힘을 준다.",
            itemType = ItemType.WEAPON, slot = EquipmentSlot.WEAPON,
            attackBonus = 1, xpMultiplier = 1.08f,
        ))
        add(ItemTemplate(
            key = "w_hourglass_sword", name = "시간의 모래시계 검", rarity = ItemRarity.RARE,
            description = "마감일 24시간 연장(하루 1회). 공격력도 뛰어나다.",
            flavorText = "시간도 이 검을 두려워한다.",
            itemType = ItemType.WEAPON, slot = EquipmentSlot.WEAPON,
            attackBonus = 2, specialEffectCode = "DEADLINE_EXTEND",
        ))
        add(ItemTemplate(
            key = "w_rangers_shortbow", name = "순찰자의 단궁", rarity = ItemRarity.UNCOMMON,
            description = "재빠른 공격으로 전투 우위를 점한다.",
            itemType = ItemType.WEAPON, slot = EquipmentSlot.WEAPON,
            attackBonus = 1, xpMultiplier = 1.06f,
        ))
        add(ItemTemplate(
            key = "w_divine_hammer", name = "신성 강타의 망치", rarity = ItemRarity.RARE,
            description = "신성한 힘이 깃든 전쟁 망치.",
            flavorText = "정의를 위한 일격은 두 배의 힘을 낸다.",
            itemType = ItemType.WEAPON, slot = EquipmentSlot.WEAPON,
            attackBonus = 2, xpMultiplier = 1.10f,
        ))
        add(ItemTemplate(
            key = "w_questmaster_sword", name = "전설의 퀘스트 마스터 검", rarity = ItemRarity.LEGENDARY,
            description = "모든 퀘스트 XP +20%. 크리티컬 히트 확률 증가.",
            flavorText = "이 검을 쥔 자만이 진정한 영웅이 될 수 있다.",
            itemType = ItemType.WEAPON, slot = EquipmentSlot.WEAPON,
            attackBonus = 3, xpMultiplier = 1.20f, specialEffectCode = "CRIT_CHANCE_BONUS",
        ))

        // ── ARMOR (방어구) ───────────────────────────────────────
        add(ItemTemplate(
            key = "a_focus_robe", name = "집중의 로브", rarity = ItemRarity.COMMON,
            description = "집중 모드 시 HP 손실을 막아준다.",
            flavorText = "고요한 마음이 최고의 갑옷이다.",
            itemType = ItemType.ARMOR, slot = EquipmentSlot.ARMOR,
            hpBonusFlat = 5, specialEffectCode = "FOCUS_HP_SHIELD",
        ))
        add(ItemTemplate(
            key = "a_leather_tunic", name = "가죽 조끼", rarity = ItemRarity.COMMON,
            description = "기본 HP를 소폭 높여준다.",
            itemType = ItemType.ARMOR, slot = EquipmentSlot.ARMOR,
            hpBonusFlat = 3,
        ))
        add(ItemTemplate(
            key = "a_chain_mail", name = "사슬 갑옷", rarity = ItemRarity.UNCOMMON,
            description = "HP를 크게 높이고 방어력도 상승한다.",
            itemType = ItemType.ARMOR, slot = EquipmentSlot.ARMOR,
            hpBonusFlat = 10, defenseBonus = 1,
        ))
        add(ItemTemplate(
            key = "a_vitality_armor", name = "강인함의 갑옷", rarity = ItemRarity.UNCOMMON,
            description = "루틴 연속 달성으로 얻는 불굴의 갑옷.",
            flavorText = "매일의 반복이 철벽을 만든다.",
            itemType = ItemType.ARMOR, slot = EquipmentSlot.ARMOR,
            hpBonusFlat = 15,
        ))
        add(ItemTemplate(
            key = "a_mage_robe", name = "마법사의 로브", rarity = ItemRarity.RARE,
            description = "지식의 힘으로 HP와 XP를 동시에 강화.",
            itemType = ItemType.ARMOR, slot = EquipmentSlot.ARMOR,
            hpBonusFlat = 8, xpMultiplier = 1.15f,
        ))
        add(ItemTemplate(
            key = "a_paladin_plate", name = "성기사의 판금 갑옷", rarity = ItemRarity.VERY_RARE,
            description = "신성한 힘으로 단련된 최고급 갑옷.",
            flavorText = "이 갑옷을 입는 자에게 두려움은 없다.",
            itemType = ItemType.ARMOR, slot = EquipmentSlot.ARMOR,
            hpBonusFlat = 20, defenseBonus = 2,
        ))
        add(ItemTemplate(
            key = "a_dragon_scale", name = "시간의 용 비늘 갑옷", rarity = ItemRarity.LEGENDARY,
            description = "크리티컬 미스 시 HP 손실 없음. HP+30.",
            flavorText = "고대 드래곤의 비늘로 짜인 궁극의 방어구.",
            itemType = ItemType.ARMOR, slot = EquipmentSlot.ARMOR,
            hpBonusFlat = 30, specialEffectCode = "CRIT_MISS_NO_DAMAGE",
        ))

        // ── RING (반지) ──────────────────────────────────────────
        add(ItemTemplate(
            key = "r_lucky", name = "행운의 반지", rarity = ItemRarity.COMMON,
            description = "소소한 행운이 XP를 늘려준다.",
            itemType = ItemType.RING, slot = EquipmentSlot.RING,
            xpMultiplier = 1.05f,
        ))
        add(ItemTemplate(
            key = "r_vitality", name = "생명력의 반지", rarity = ItemRarity.COMMON,
            description = "HP를 소폭 증가시킨다.",
            itemType = ItemType.RING, slot = EquipmentSlot.RING,
            hpBonusFlat = 5,
        ))
        add(ItemTemplate(
            key = "r_streak", name = "연속의 반지", rarity = ItemRarity.UNCOMMON,
            description = "스트릭 보너스 +10% 추가 누적.",
            flavorText = "끊지 않는 자만이 이 반지의 힘을 안다.",
            itemType = ItemType.RING, slot = EquipmentSlot.RING,
            xpMultiplier = 1.08f, specialEffectCode = "STREAK_BONUS_UP",
        ))
        add(ItemTemplate(
            key = "r_haste", name = "신속의 반지", rarity = ItemRarity.UNCOMMON,
            description = "재빠른 실행력으로 XP 획득이 늘어난다.",
            itemType = ItemType.RING, slot = EquipmentSlot.RING,
            xpMultiplier = 1.06f,
        ))
        add(ItemTemplate(
            key = "r_scholar", name = "학자의 반지", rarity = ItemRarity.UNCOMMON,
            description = "지식 추구를 통한 경험치 증가.",
            itemType = ItemType.RING, slot = EquipmentSlot.RING,
            xpMultiplier = 1.07f,
        ))
        add(ItemTemplate(
            key = "r_delegation", name = "위임의 인장반지", rarity = ItemRarity.RARE,
            description = "위임 퀘스트 완료 시 본인도 XP 50% 획득.",
            flavorText = "좋은 리더는 힘을 나눌 줄 안다.",
            itemType = ItemType.RING, slot = EquipmentSlot.RING,
            xpMultiplier = 1.10f, specialEffectCode = "DELEGATION_XP",
        ))
        add(ItemTemplate(
            key = "r_arcane", name = "신비의 반지", rarity = ItemRarity.RARE,
            description = "신비한 힘이 모든 XP를 증폭시킨다.",
            itemType = ItemType.RING, slot = EquipmentSlot.RING,
            xpMultiplier = 1.12f,
        ))
        add(ItemTemplate(
            key = "r_champion", name = "챔피언의 반지", rarity = ItemRarity.LEGENDARY,
            description = "진정한 챔피언에게만 허락된 반지. XP +25%.",
            flavorText = "이 반지는 최강자를 알아본다.",
            itemType = ItemType.RING, slot = EquipmentSlot.RING,
            xpMultiplier = 1.25f,
        ))

        // ── NECKLACE (목걸이) ────────────────────────────────────
        add(ItemTemplate(
            key = "n_strength_pendant", name = "힘의 펜던트", rarity = ItemRarity.COMMON,
            description = "STR 퀘스트 공격력을 높여준다.",
            itemType = ItemType.NECKLACE, slot = EquipmentSlot.NECKLACE,
            attackBonus = 1,
        ))
        add(ItemTemplate(
            key = "n_health_amulet", name = "건강의 부적", rarity = ItemRarity.UNCOMMON,
            description = "체력을 강화시켜 HP가 증가한다.",
            itemType = ItemType.NECKLACE, slot = EquipmentSlot.NECKLACE,
            hpBonusFlat = 10,
        ))
        add(ItemTemplate(
            key = "n_focus_crystal", name = "집중력의 수정구", rarity = ItemRarity.UNCOMMON,
            description = "INT 체크 +1, 집중 모드 시간 +15분.",
            flavorText = "수정 속에 담긴 완벽한 집중.",
            itemType = ItemType.NECKLACE, slot = EquipmentSlot.NECKLACE,
            xpMultiplier = 1.05f, specialEffectCode = "FOCUS_TIME_BONUS",
        ))
        add(ItemTemplate(
            key = "n_wisdom", name = "지혜의 목걸이", rarity = ItemRarity.RARE,
            description = "WIS +1, 모든 판단이 더 명확해진다.",
            itemType = ItemType.NECKLACE, slot = EquipmentSlot.NECKLACE,
            xpMultiplier = 1.08f,
        ))
        add(ItemTemplate(
            key = "n_all_seeing", name = "전지전능의 눈 목걸이", rarity = ItemRarity.LEGENDARY,
            description = "주간 리뷰 완료 후 7일간 모든 XP +25%.",
            flavorText = "이 눈은 모든 가능성을 본다.",
            itemType = ItemType.NECKLACE, slot = EquipmentSlot.NECKLACE,
            xpMultiplier = 1.25f, specialEffectCode = "WEEKLY_REVIEW_BONUS",
        ))

        // ── MISC (기타/소모품) ────────────────────────────────────
        add(ItemTemplate(
            key = "m_xp_tome", name = "경험의 서", rarity = ItemRarity.COMMON,
            description = "보유만 해도 소량의 XP 배율을 제공한다.",
            itemType = ItemType.MISC, slot = EquipmentSlot.MISC,
            xpMultiplier = 1.03f,
        ))
        add(ItemTemplate(
            key = "m_potion", name = "전투 물약", rarity = ItemRarity.COMMON,
            description = "HP를 소폭 회복시키는 붉은 물약.",
            itemType = ItemType.MISC, slot = EquipmentSlot.MISC,
            hpBonusFlat = 5,
        ))
        add(ItemTemplate(
            key = "m_elixir", name = "명료함의 영약", rarity = ItemRarity.UNCOMMON,
            description = "정신을 맑게 해 XP 효율을 높인다.",
            itemType = ItemType.MISC, slot = EquipmentSlot.MISC,
            xpMultiplier = 1.10f,
        ))
        add(ItemTemplate(
            key = "m_phoenix_feather", name = "불사조의 깃털", rarity = ItemRarity.RARE,
            description = "한 번의 치명적 실패를 무효화한다.",
            flavorText = "불사조는 재에서 다시 태어난다.",
            itemType = ItemType.MISC, slot = EquipmentSlot.MISC,
            specialEffectCode = "PREVENT_DEATH",
        ))
        add(ItemTemplate(
            key = "m_dragon_essence", name = "용의 정수", rarity = ItemRarity.VERY_RARE,
            description = "드래곤의 힘이 모든 XP를 크게 늘린다.",
            flavorText = "고대 드래곤의 숨결이 담겨 있다.",
            itemType = ItemType.MISC, slot = EquipmentSlot.MISC,
            xpMultiplier = 1.20f,
        ))
    }

    // 드롭 가능 아이템 필터 (희귀도 + CR 조건)
    fun getDropCandidates(rarity: ItemRarity): List<ItemTemplate> =
        ALL.filter { it.rarity == rarity }
}
