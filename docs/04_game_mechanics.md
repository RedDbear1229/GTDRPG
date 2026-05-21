# 04. 게임 메커니즘

## 4.1 D20 자동 전투 시스템

### 전투 흐름

```
할 일 완료 체크박스 탭
        │
        ▼ (즉시, 0ms)
BS-03 D20RollSheet 표시
        │
        ▼ (500ms 애니메이션)
D20 결과 표시 (SecureRandom.nextInt(20) + 1)
        │
        ▼
공격 굴림 계산
        │
        ├── [공격 굴림 ≥ 몬스터 AC] → 명중
        ├── [D20 = 20] → 크리티컬 히트
        └── [D20 = 1]  → 크리티컬 미스
```

### 공격 굴림 계산

```
공격 굴림 = D20 결과 + 능력치 수정치 + 숙련 보너스 + 장비 보너스

능력치 수정치: 퀘스트의 primaryAbility 기반
  STR 퀘스트 → STR 수정치 적용
  INT 퀘스트 → INT 수정치 적용
  CHA 퀘스트 → CHA 수정치 적용

숙련 보너스 (레벨 기반):
  Lv  1-4  → +2
  Lv  5-8  → +3
  Lv  9-12 → +4
  Lv 13-16 → +5
  Lv 17-20 → +6

장비 보너스: 착용 중인 무기 아이템의 공격 보너스
```

### 몬스터 AC (CR 기반)

```kotlin
fun getMonsterAC(cr: Float): Int = when {
    cr < 1f  -> 10
    cr < 2f  -> 11
    cr < 3f  -> 12
    cr < 4f  -> 13
    cr < 6f  -> 14
    cr < 8f  -> 15
    cr < 10f -> 16
    cr < 12f -> 17
    cr < 15f -> 18
    cr < 18f -> 19
    cr < 21f -> 20
    else     -> 22
}
```

### 전투 결과 처리

```kotlin
sealed class CombatResult {
    data class Hit(
        val d20Result: Int,
        val totalAttack: Int,
        val monsterAC: Int,
        val xpGained: Long,
        val itemDrop: Item?
    ) : CombatResult()

    data class CriticalHit(
        val d20Result: Int = 20,
        val xpGained: Long,        // Hit XP × 2
        val itemDrop: Item,        // 크리티컬은 반드시 아이템 드롭
        val bonusNarrative: String // Claude AI 특별 내러티브
    ) : CombatResult()

    data class Miss(
        val d20Result: Int,
        val totalAttack: Int,
        val monsterAC: Int,
        val hpLost: Int,           // 몬스터 반격 피해
        val taskReturnedToInbox: Boolean
    ) : CombatResult()

    data class CriticalMiss(
        val d20Result: Int = 1,
        val hpLost: Int,           // Miss보다 2배 피해
        val humourousMessage: String
    ) : CombatResult()
}
```

### 명중 시 XP 계산

```kotlin
fun calculateXP(
    task: Task,
    d20Result: Int,
    character: Character,
    isCriticalHit: Boolean
): Long {
    // 기본 XP
    val baseXP = (task.challengeRating * 25).toLong()

    // 크리티컬 히트 배수
    val critMultiplier = if (isCriticalHit) 2.0f else 1.0f

    // 마감 보너스
    val deadlineBonus = task.dueDate?.let { due ->
        val now = System.currentTimeMillis()
        if (due >= now) 1.2f else 0.9f  // 마감 전 +20%, 초과 -10%
    } ?: 1.0f

    // 연속 스트릭 보너스 (최대 +50%)
    val streakBonus = 1f + (character.streakDays * 0.05f).coerceAtMost(0.5f)

    // 클래스 특성 보너스
    val classBonus = getClassBonus(character.classType, task)

    // 장비 보너스 (XP 부스터 아이템)
    val equipmentBonus = getEquipmentXPBonus(character)

    return (baseXP * critMultiplier * deadlineBonus * streakBonus * classBonus * equipmentBonus).toLong()
}
```

### 미스 시 HP 손실 계산

```kotlin
fun calculateHPLoss(
    monsterCR: Float,
    isCriticalMiss: Boolean,
    character: Character
): Int {
    val baseDamage = (monsterCR * 1.5f).toInt().coerceAtLeast(1)
    val critMultiplier = if (isCriticalMiss) 2 else 1
    val armorReduction = character.equippedArmor?.defenseBonus ?: 0
    
    return ((baseDamage * critMultiplier) - armorReduction).coerceAtLeast(0)
}
```

---

## 4.2 HP 시스템

### 최대 HP 계산

```kotlin
fun calculateMaxHP(character: Character): Int {
    val classHitDie = character.classType.hitDie  // 각 클래스별 Hit Die
    val conModifier = abilityModifier(character.constitution)
    val level = character.level
    
    // 1레벨: 최대값 + CON 수정치
    // 이후 레벨: 평균값(hitDie/2 + 1) + CON 수정치
    val baseHP = classHitDie + conModifier
    val levelHP = (level - 1) * (classHitDie / 2 + 1 + conModifier)
    
    return baseHP + levelHP
}

val ClassHitDie = mapOf(
    BARBARIAN to 12,
    FIGHTER to 10, PALADIN to 10, RANGER to 10,
    BARD to 8, CLERIC to 8, DRUID to 8,
    MONK to 8, ROGUE to 8, WARLOCK to 8,
    SORCERER to 6,
    WIZARD to 6
)
```

### HP 회복 시스템

| 회복 이벤트 | 회복량 | 조건 |
|------------|--------|------|
| Long Rest (자정 리셋) | 최대 HP 전체 | 매일 자정 자동 |
| Short Rest (연속 3퀘스트 완료) | 최대 HP × 25% | 연속 완료 시 자동 |
| 특정 아이템 사용 | 아이템별 상이 | 수동 |
| Cleric 클래스 특성 | 최대 HP × 20% | 크리티컬 미스 방지 |
| Weekly Review 완료 | 최대 HP × 30% | 주 1회 |

### HP 상태 효과

```kotlin
enum class HPStatus {
    HEALTHY,    // HP > 75%: 정상
    TIRED,      // HP 50-75%: "피로함" 아이콘 표시
    WOUNDED,    // HP 25-50%: "부상" 아이콘, 고CR 퀘스트 경고
    CRITICAL,   // HP 0-25%: "위기" 아이콘, 긴급 퀘스트만 추천
    UNCONSCIOUS // HP 0%: "기절" 상태, Long Rest 강요 UI
}
```

HP가 낮을 때 UX 변화:
- QuestBoard: 고CR 퀘스트에 "⚠️ HP 부족" 경고 배지
- 캐릭터 시트 초상화: 부상 이미지로 변경
- 추천 퀘스트: 저CR 위주로 필터링

---

## 4.3 XP와 레벨업 시스템

### 레벨업 XP 임계값

| 레벨 | 필요 XP | 누적 XP | 추가 보상 |
|------|---------|---------|---------|
| 1→2 | 300 | 300 | 클래스 특성 1 |
| 2→3 | 600 | 900 | - |
| 3→4 | 1,800 | 2,700 | 능력치 보너스 선택 |
| 4→5 | 3,800 | 6,500 | 클래스 특성 2 |
| 5→6 | 7,500 | 14,000 | - |
| 6→7 | 9,000 | 23,000 | 클래스 특성 3 |
| 7→8 | 11,000 | 34,000 | 능력치 보너스 선택 |
| 8→9 | 14,000 | 48,000 | - |
| 9→10 | 16,000 | 64,000 | 클래스 특성 4 |
| 10→11 | 21,000 | 85,000 | - |
| 11→12 | 15,000 | 100,000 | 능력치 보너스 선택 |
| ...  | ... | ... | ... |
| 19→20 | 55,000 | 355,000 | 전설 특성 해금 |

### 레벨업 시퀀스 (LevelUpScreen)

```
1. D20RollSheet에서 XP 누적 중 임계값 도달 감지
2. 현재 전투 결과 먼저 보여줌 (CombatResultScreen)
3. "계속하기" 탭 → 자동으로 LevelUpScreen으로 전환

LevelUpScreen 구성:
┌─────────────────────────────────────┐
│  ✨ LEVEL UP! ✨                    │
│  Aria Stormwind                     │
│  Wizard Level 7 → Level 8          │
│                                     │
│  [황금 파티클 + 레벨업 사운드]      │
│                                     │
│ 📖 Claude AI 축하 메시지:           │
│ "전설적인 마법사여, 8레벨에 오신    │
│  것을 환영합니다! 당신의 Spellbook  │
│  은 더욱 두꺼워졌습니다..."         │
│                                     │
│ 📊 새로운 스탯:                     │
│  ❤️ 최대 HP: 52 → 58 (+6)          │
│  🎯 숙련 보너스: +3 → +4 (8레벨)   │
│                                     │
│ 🎁 보상 선택 (능력치 +2 or 특기):   │
│  ○ INT +2 (현재 18 → 20)           │
│  ○ WIS +2 (현재 15 → 17)           │
│  ○ 특기: 전쟁 마법사 (주문 집중력)  │
│                    [선택 확정]       │
│                                     │
│ ⚡ 새 클래스 특성 해금:              │
│  "Power Word Kill" - 마감 당일      │
│  CR 10 이하 퀘스트 자동 완료 1회/주 │
│                                     │
│         [모험을 계속하자!]           │
└─────────────────────────────────────┘
```

---

## 4.4 클래스 시스템 상세

### 12 클래스 전체 명세

#### BARBARIAN (바바리안)
```
GTD 성격: 열정, 경쟁, 즉각 행동
Primary Stat: STR
Hit Die: d12

클래스 특성:
  [Lv 1] Rage (분노)
    - 하루 2회 발동 가능
    - STR 퀘스트 XP 2배
    - 활성화 시: QuestBoard 우선순위 자동 재정렬 (마감 기준)
    - 지속 시간: 3개 퀘스트 완료까지
    
  [Lv 2] Reckless Attack (무모한 공격)
    - CR+3 높은 퀘스트 도전 가능
    - 명중 시: +30% XP
    - 실패 시: HP 손실 2배

  [Lv 5] Extra Attack (추가 공격)
    - DEX 또는 STR 퀘스트 연속 2개 완료 시
    - 두 번째 퀘스트 D20 재굴림 가능

  [Lv 9] Brutal Critical (잔인한 치명타)
    - 크리티컬 히트 시 XP 3배 (기존 2배 → 3배)

GTD 특화:
  - 긴급 태스크 처리에 자동 보너스
  - 인박스 항목이 10개 넘으면 "분노 게이지" 활성화 힌트
  - 즉흥적 실행력 강조
```

#### BARD (바드)
```
GTD 성격: 창의, 연결, 공감, 소통
Primary Stat: CHA
Hit Die: d8

클래스 특성:
  [Lv 1] Bardic Inspiration (바드의 영감)
    - 하루 CHA 수정치 회 발동
    - NPC에게 퀘스트 위임 시 성공률 +30%
    - 소셜 퀘스트(@사람 태그) XP +25%
    
  [Lv 2] Jack of All Trades (만능꾼)
    - 모든 능력치 관련 퀘스트에 +수정치/2 추가 보너스
    - 전문화는 없지만 균형 잡힌 수행 능력
    
  [Lv 3] Expertise (전문성)
    - 2개 컨텍스트 태그 선택 → 해당 퀘스트 숙련 보너스 2배
    
  [Lv 6] Countercharm (반마법)
    - 연속 미스 2회 시 자동 발동
    - 다음 퀘스트 D20 최소 10 보장
    
  [Lv 10] Magical Secrets (마법의 비밀)
    - 다른 클래스 특성 1개 복사 가능 (1회)

GTD 특화:
  - NPC(연락처) 시스템 확장 접근
  - 협업/위임 효율 최고
  - 관계 영역 퀘스트 특화
```

#### CLERIC (클레릭)
```
GTD 성격: 평등주의, 봉사, 지원, 안정
Primary Stat: WIS
Hit Die: d8

클래스 특성:
  [Lv 1] Channel Divinity (신성한 힘)
    - 하루 1회: 가장 오래된 미완료 퀘스트 자동 완료
    - "기적적 해결" - XP 절반만 획득
    
  [Lv 1] Healing Word (치유의 말)
    - 크리티컬 미스 시 HP 손실 방지 (1회/일)
    
  [Lv 2] Blessed Strikes (축복받은 일격)
    - 주간 리뷰 완료 후 3일간 모든 퀘스트 XP +15%
    
  [Lv 5] Destroy Undead (불사자 퇴치)
    - 3일 이상 지연된 퀘스트 완료 시 XP +50%
    
  [Lv 8] Divine Strike (신성한 일격)
    - WIS 관련 퀘스트 (의사결정, 검토) XP 2배

GTD 특화:
  - 묵은 퀘스트 처리 전문가
  - 주간 리뷰 보너스 최대
  - HP 관리 능력 탁월
```

#### DRUID (드루이드)
```
GTD 성격: 자연/균형, 장기 관점, 순환
Primary Stat: WIS
Hit Die: d8

클래스 특성:
  [Lv 1] Wild Shape (야생 변신)
    - 주간 퀘스트 배분 자동 균형 조정
    - 한 생활 영역이 70% 초과 시 경고 + 재조정 제안
    
  [Lv 2] Nature's Resilience (자연의 회복력)
    - 루틴 퀘스트 자동 반복 생성
    - 7일 연속 루틴 달성 시 "자연의 리듬" 보너스 +100 XP
    
  [Lv 4] Timeless Body (불로의 몸)
    - 연속 스트릭이 끊겨도 3일 보호
    - 스트릭 복구 토큰 (월 2회)
    
  [Lv 6] Land's Stride (대지의 걸음)
    - 컨텍스트 무시하고 어디서든 퀘스트 가능
    - 컨텍스트 미지정 퀘스트 XP +10%
    
  [Lv 10] Nature's Ward (자연의 가호)
    - HP가 0이 되어도 1 HP로 버팀 (1회/주)

GTD 특화:
  - 루틴/습관 형성 최적화
  - 생활 영역 균형 관리
  - 장기 프로젝트 지속력
```

#### FIGHTER (파이터)
```
GTD 성격: 체계적, 전략적, 다재다능
Primary Stat: STR 또는 DEX
Hit Die: d10

클래스 특성:
  [Lv 1] Action Surge (행동 쇄도)
    - 하루 1회: 한 번에 퀘스트 2개 동시 완료 처리
    - 두 퀘스트 합산 D20 굴림
    
  [Lv 1] Second Wind (제2의 바람)
    - 연속 3회 미스 시 자동 HP 회복 (최대 HP × 20%)
    
  [Lv 2] Combat Superiority (전투 우위)
    - 마감일이 있는 퀘스트 XP +10%
    
  [Lv 5] Extra Attack (추가 공격)
    - STR/DEX 퀘스트 완료 시 추가 D20 굴림 가능 (유리한 것 선택)
    
  [Lv 7] Battle Master (전투 마스터)
    - 퀘스트 난이도를 CR 1 낮춰서 시도 가능 (수동 조정)
    - 성공 시 XP는 원래 CR 기준

GTD 특화:
  - 멀티태스킹 우수
  - 실패 회복력
  - 마감 관리 탁월
```

#### MONK (몽크)
```
GTD 성격: 규율, 집중, 내면 성찰, 루틴
Primary Stat: DEX + WIS
Hit Die: d8

클래스 특성:
  [Lv 1] Stunning Strike (기절 일격)
    - 집중 모드 활성화 = 모든 알림 차단
    - 기본 25분, WIS 수정치 × 5분 추가
    - 완료 시 해당 퀘스트 XP +40%
    
  [Lv 1] Unarmored Defense (무장 방어)
    - 장비 없어도 AC 보너스 (WIS 수정치 추가)
    - 아이템에 의존 않는 플레이스타일
    
  [Lv 2] Ki Points (기 포인트)
    - 레벨과 동일한 수의 Ki 포인트 보유
    - 특수 능력 사용 시 소모, 매일 자정 충전
    
  [Lv 2] Flurry of Blows (연속 공격)
    - Ki 2 소모: DEX 퀘스트 3개 연속 완료 시
    - 마지막 퀘스트 XP 2배
    
  [Lv 5] Stunning Strike (강화)
    - 집중 모드 종료 후 30분간 모든 퀘스트 D20 +3 보너스
    
  [Lv 7] Evasion (회피)
    - 크리티컬 미스 시 HP 손실 절반

GTD 특화:
  - 딥 워크/집중 특화
  - 일상 루틴 강화
  - 분산 없는 실행
```

#### PALADIN (팔라딘)
```
GTD 성격: 안정, 공감, 원칙, 장기 비전
Primary Stat: STR + CHA
Hit Die: d10

클래스 특성:
  [Lv 1] Divine Smite (신성한 일격)
    - 마감 당일 퀘스트 완료 시 XP 폭발적 증가 (+100%)
    
  [Lv 1] Lay on Hands (안수 치유)
    - 매일 레벨×10의 HP 풀 보유
    - 자신 또는 "NPC 지원 퀘스트" 완료 시 HP 회복
    
  [Lv 2] Divine Smite 강화
    - CHA 퀘스트에도 적용 (발표, 협상 등)
    
  [Lv 3] Sacred Oath (신성한 맹세)
    - 월간 목표 선언 기능
    - 달성 시 대규모 XP 보너스 (+500 XP)
    - 미달성 시 "파기된 맹세" 페널티 (-100 XP)
    
  [Lv 6] Aura of Protection (수호의 오라)
    - NPC 관련 퀘스트에 CHA 수정치 추가 보너스
    
  [Lv 10] Aura of Courage (용기의 오라)
    - HP 25% 이하에서도 고CR 퀘스트 도전 가능 (경고 제거)

GTD 특화:
  - 약속/마감 이행 전문가
  - 장기 목표 유지력
  - NPC 지원 특화
```

#### RANGER (레인저)
```
GTD 성격: 독립, 탐험, 자립, 자연 친화
Primary Stat: DEX + WIS
Hit Die: d10

클래스 특성:
  [Lv 1] Favored Enemy (선호 적)
    - 2개 생활 영역 선택 (시작 시)
    - 해당 영역 퀘스트 숙련 보너스 +2 추가
    
  [Lv 1] Natural Explorer (자연 탐험가)
    - 새 컨텍스트 태그에서 첫 퀘스트 완료 시 탐험 보너스 +50 XP
    - 새로운 프로젝트 시작 시 "탐험가의 설렘" +25 XP
    
  [Lv 3] Primeval Awareness (원시 감지)
    - 마감 D-3 이내 퀘스트 자동 감지 및 경고
    - 주간 리뷰 없이도 긴급 항목 자동 하이라이트
    
  [Lv 5] Extra Attack (추가 공격)
    - 탐험/외출 컨텍스트 퀘스트 완료 시 추가 D20 굴림
    
  [Lv 7] Fleet of Foot (빠른 발)
    - 이동/외출 중 완료한 퀘스트 XP +25%
    
  [Lv 11] Feral Senses (야생의 감각)
    - 인박스 항목이 5개 이상이면 자동 알림 (놓치지 않음)

GTD 특화:
  - 이동 중 생산성 특화
  - 새 분야 도전 보상
  - 자율적 관리
```

#### ROGUE (로그)
```
GTD 성격: 창의적 문제해결, 효율, 기회주의
Primary Stat: DEX
Hit Die: d8

클래스 특성:
  [Lv 1] Sneak Attack (기습 공격)
    - 준비 없이 즉흥 완료 시 XP 보너스 +40%
    - Clarify 없이 직접 완료한 퀘스트에 적용
    
  [Lv 1] Cunning Action (영리한 행동)
    - 2분 규칙 퀘스트 자동 인식 및 Bonus Action 처리
    - XP는 낮지만 연속 처리 가능
    
  [Lv 2] Uncanny Dodge (놀라운 회피)
    - 크리티컬 미스 시 HP 손실 절반
    
  [Lv 3] Expertise (전문성)
    - 2개 스킬/컨텍스트 → 숙련 2배
    
  [Lv 5] Evasion (완전 회피)
    - 주 1회: 크리티컬 미스를 일반 미스로 전환
    
  [Lv 7] Reliable Talent (안정적 재능)
    - D20 결과가 10 미만이면 10으로 처리 (선택한 컨텍스트)
    
  [Lv 11] Blindsense (맹감)
    - 마감일이 없는 퀘스트에도 긴급도 자동 추정 기능

GTD 특화:
  - 빠른 처리, 기회 포착
  - 즉흥 실행 보상
  - 2분 규칙 마스터
```

#### SORCERER (소서러)
```
GTD 성격: 역동, 강렬, 직관, 폭발적 에너지
Primary Stat: CHA
Hit Die: d6

클래스 특성:
  [Lv 1] Innate Spellcasting (선천적 마법)
    - CHA 관련 퀘스트에 자동 보너스 (주문처럼 타고남)
    
  [Lv 2] Sorcery Points (마법 포인트)
    - 레벨 수만큼 포인트 보유
    - 퀘스트 완료 시 소량 획득, 특수 능력 사용 시 소모
    
  [Lv 2] Metamagic (변형 마법)
    - 포인트 소모로 퀘스트 조건 변형:
      · Quickened: 마감일 즉시 완료 (1포인트)
      · Extended: 마감일 3일 연장 (2포인트)
      · Empowered: XP 재계산, 높은 값 선택 (1포인트)
    
  [Lv 6] Magical Guidance (마법적 안내)
    - 하루 1회: 실패한 D20 굴림 재시도
    
  [Lv 10] Sorcerous Restoration (마법 회복)
    - Short Rest에 Sorcery Points 일부 회복

GTD 특화:
  - 유연한 마감 조정
  - 폭발적 집중력 구현
  - 실패 재시도 기회
```

#### WARLOCK (워록)
```
GTD 성격: 장기 헌신, 계약, 집중된 전문성
Primary Stat: CHA
Hit Die: d8

클래스 특성:
  [Lv 1] Patron Pact (후원자 계약)
    - Claude AI와의 "계약": 주 1회 최적 퀘스트 3개 AI 추천
    - 추천 퀘스트 완료 시 +50 XP 보너스
    
  [Lv 1] Eldritch Blast (마력 폭발)
    - 14일 이상 방치된 퀘스트 자동 감지
    - "강제 처리 모드": 해당 퀘스트 완료 시 XP 3배
    
  [Lv 2] Invocations (주문 발동)
    - 특정 Invocation 선택으로 특화 강화
    · Agonizing Blast: CHA 수정치 D20에 추가
    · Devil's Sight: 야간(22:00-06:00) 완료 XP +30%
    
  [Lv 5] Thirsting Blade (목마른 칼날)
    - 같은 프로젝트 내 연속 퀘스트 완료 시 XP 누적 증가
    - 1개: ×1.0, 2개: ×1.2, 3개: ×1.5, 4개+: ×2.0
    
  [Lv 9] Lifedrinker (생명 흡수)
    - CHA 수정치만큼 HP 회복 (CHA 퀘스트 완료 시마다)

GTD 특화:
  - 장기 방치 퀘스트 청산 전문
  - AI 추천 의존형 플레이
  - 프로젝트 집중 시 폭발적 성장
```

#### WIZARD (위저드)
```
GTD 성격: 지식, 방법론, 계획, 분석
Primary Stat: INT
Hit Die: d6

클래스 특성:
  [Lv 1] Arcane Recovery (신비 회복)
    - Short Rest마다 INT 관련 퀘스트 추가 XP 회복
    - 학습/연구 퀘스트 완료 시 HP 소량 회복 (정신 충전)
    
  [Lv 1] Spellbook (주문서)
    - 퀘스트 템플릿 저장 기능 (무제한)
    - 반복 사용 가능한 체크리스트 생성
    - 다른 Wizard와 템플릿 공유 가능 (v2)
    
  [Lv 2] Arcane Tradition (신비 전통)
    - 3가지 전통 중 선택:
      · Abjuration: 마감 보호 (마감 알림 강화)
      · Divination: 주간 퀘스트 예측 (AI 추천)
      · Conjuration: 자동 루틴 생성
    
  [Lv 5] Intelligence Mastery (지성 마스터리)
    - INT 20 도달 시: 학습 퀘스트 XP 2배
    
  [Lv 10] Spell Mastery (주문 마스터리)
    - 2개 퀘스트 템플릿 → 자동 일간 반복 설정
    
  [Lv 18] Spell Perfection (주문 완성)
    - 선택한 1개 컨텍스트: 항상 크리티컬 히트로 처리

GTD 특화:
  - 계획 수립, 연구, 분석 최강
  - 템플릿/체크리스트 시스템
  - 주간 리뷰 보너스 최대
```

---

## 4.5 아이템 시스템

### 장비 슬롯 구조

```
┌─────────────────────────────────────┐
│  [목걸이]                           │
│  [무기]  [캐릭터]  [방어구]          │
│  [반지1] [반지2]                    │
│  [소지품1] [소지품2] [소지품3] [소지품4] │
└─────────────────────────────────────┘
```

### 아이템 등급 및 드롭률

| 등급 | 색상 | 드롭 조건 | 드롭률 |
|------|------|----------|--------|
| 일반 (Common) | 회색 | 모든 명중 | 15% |
| 비범 (Uncommon) | 초록 | CR 3+ 명중 | 8% |
| 희귀 (Rare) | 파랑 | CR 7+ 명중 | 4% |
| 매우 희귀 (Very Rare) | 보라 | CR 12+ 명중 또는 크리티컬 | 2% |
| 전설 (Legendary) | 금색 | 크리티컬 히트만 | 0.5% |

### 아이템 카탈로그 (주요 아이템)

#### 무기 (Weapon)
```
[집중력의 깃털펜] (Common)
  - 공격 보너스: +1
  - INT 퀘스트 XP +10%
  - 드롭: 학습 퀘스트 완료

[시간의 모래시계 검] (Rare)
  - 공격 보너스: +2
  - 하루 1회: 마감일 24시간 연장
  - 드롭: CR 7+ 퀘스트

[전설의 퀘스트 마스터 검] (Legendary)
  - 공격 보너스: +3
  - 모든 퀘스트 XP +20%
  - 크리티컬 히트 확률 +5%
  - 드롭: CR 15+ 크리티컬 히트
```

#### 방어구 (Armor)
```
[집중의 로브] (Common)
  - HP 보너스: +5
  - 집중 모드 시 HP 손실 없음
  - 드롭: 집중 모드 완료

[강인함의 갑옷] (Uncommon)
  - HP 보너스: +15
  - CON 수정치 +1
  - 드롭: 루틴 7일 연속

[시간의 용 비늘 갑옷] (Legendary)
  - HP 보너스: +30
  - 크리티컬 미스 시 HP 손실 0
  - 매 10 퀘스트마다 추가 HP +1 (최대 +20)
  - 드롭: 타라스크급 퀘스트 완료
```

#### 반지 (Ring)
```
[연속의 반지] (Uncommon)
  - 스트릭 보너스 +10% 추가 누적
  - 스트릭 보호 토큰 +1/주
  
[위임의 인장반지] (Rare)
  - NPC 위임 성공률 +25%
  - 위임 퀘스트 완료 시 본인도 XP 50% 획득
```

#### 목걸이 (Necklace)
```
[집중력의 수정구] (Uncommon)
  - 집중 모드 시간 +15분
  - INT 체크 +1
  
[전지전능의 눈 목걸이] (Legendary)
  - 주간 리뷰 완료 후 7일간 모든 퀘스트 XP +25%
  - Weekly Review 미스 없이 완료 시 자동 장착 효과
```

---

## 4.6 몬스터 카탈로그

### CR ↔ 할 일 매핑 전체 표

| CR | 몬스터 | 예상 시간 | 퀘스트 예시 | 몬스터 AC | 기본 XP |
|----|--------|----------|------------|----------|---------|
| 0 | 먼지 슬라임 | ~2분 | 메시지 답장, 서류 서명 | 10 | 10 |
| 1/4 | 약한 고블린 | 5-15분 | 간단 메모, 배송 확인 | 11 | 50 |
| 1/2 | 고블린 | 15-30분 | 짧은 미팅, 견적 요청 | 12 | 100 |
| 1 | 오크 | 30-60분 | 이메일 초안, 간단 분석 | 13 | 200 |
| 2 | 리자드맨 | 1-2시간 | 보고서, 코드 리뷰 | 13 | 450 |
| 3 | 버그베어 | 2-3시간 | 기획서, 프레젠테이션 준비 | 14 | 700 |
| 4 | 오우거 | 3-4시간 | 복잡한 분석, 설계 | 14 | 1,100 |
| 5 | 트롤 | 4-8시간 | 주요 기능 구현, 중요 보고서 | 15 | 1,800 |
| 6 | 만티코어 | 1-2일 | 스프린트 단위 작업 | 15 | 2,300 |
| 7 | 뱀파이어 스폰 | 2-3일 | 중요 마일스톤 | 15 | 2,900 |
| 8 | 영 드래곤 | 3-5일 | 주요 기능 완성 | 16 | 3,900 |
| 9 | 클라우드 자이언트 | 5-7일 | 스프린트 목표 | 16 | 5,000 |
| 10 | 스톤 자이언트 | 1-2주 | 중기 프로젝트 단계 | 17 | 5,900 |
| 11 | 성체 드래곤 | 2-4주 | 중요 프로젝트 완성 | 17 | 7,200 |
| 12 | 아볼레스 | 1-2개월 | 사업 단계 목표 | 17 | 8,400 |
| 15 | 고대 드래곤 | 1분기 | 분기 OKR | 19 | 13,000 |
| 17 | 리치 | 반년 | 반기 목표 | 20 | 18,000 |
| 20 | 피치 리치 | 1년 | 연간 목표 | 21 | 25,000 |
| 24 | 타라스크 | 다년간 | 인생 목표, Epic Quest | 22 | 62,000 |

### 몬스터 이미지 스타일

픽셀 아트 스타일 (16×16 또는 32×32) 사용:
- 각 CR별 대표 몬스터 스프라이트
- 전투 씬에서 좌우 이동 애니메이션 (2프레임)
- 피격 시 빨간 점멸 효과
- 처치 시 소멸 파티클 이펙트

---

## 4.7 연속 스트릭 시스템

### 스트릭 정의

```
스트릭 = 연속으로 퀘스트를 완료한 일수
하루에 최소 1개 이상의 퀘스트 완료 시 스트릭 유지
자정(00:00)에 당일 완료 여부 판단 후 스트릭 업데이트
```

### 스트릭 보너스

```kotlin
fun getStreakBonus(streakDays: Int): Float {
    return when {
        streakDays == 0 -> 1.0f
        streakDays < 3  -> 1.0f + (streakDays * 0.05f)
        streakDays < 7  -> 1.15f + ((streakDays - 3) * 0.03f)
        streakDays < 14 -> 1.27f + ((streakDays - 7) * 0.02f)
        streakDays < 30 -> 1.41f + ((streakDays - 14) * 0.01f)
        else            -> 1.57f  // 최대 +57% (30일 이상)
    }
}
```

### 스트릭 마일스톤 보상

| 스트릭 | 칭호 | 보너스 |
|--------|------|--------|
| 3일 | 꾸준한 모험가 | +50 XP |
| 7일 | 일주일의 영웅 | +150 XP + 아이템 |
| 14일 | 철인 모험가 | +400 XP + 희귀 아이템 |
| 30일 | 전설의 퀘스터 | +1,000 XP + 전설 아이템 |
| 100일 | 불멸의 모험가 | +5,000 XP + 전용 칭호 |

### 스트릭 보호

```
스트릭 보호 토큰:
  - 기본: 월 1개 지급
  - Druid 클래스: 월 3개
  - 연속 반지 아이템: 월 +1개
  - Weekly Review 완료: +1개

토큰 사용:
  - 당일 퀘스트 완료 못했을 때 자동 발동 (선택 가능)
  - 하루 스트릭을 지켜줌
  - 알림: "오늘 퀘스트를 완료하지 않았습니다. 보호 토큰을 사용할까요?"
```

---

## 4.8 랜덤 인카운터 시스템

### 정의

랜덤 인카운터 = 예상치 못한 보너스 이벤트 (긍정/중립)

책의 철학: "What is life if not a series of random encounters?"

### 트리거 조건

```
WorkManager가 하루에 1-2회 랜덤 시간에 발동:
  - 조건 1: 당일 최소 1개 퀘스트 완료 후
  - 조건 2: 마지막 인카운터 발생 후 6시간 이상 경과
  - 발동 확률: 30% (완료 퀘스트 수만큼 +5%)
```

### 인카운터 유형 및 효과

```kotlin
enum class RandomEncounter(
    val title: String,
    val probability: Float
) {
    BONUS_XP("보물 상자 발견!", 0.30f),     // +50~200 XP
    ITEM_DROP("방랑 상인 만남", 0.20f),      // 랜덤 아이템 획득
    STAT_BOOST("신비한 샘 발견", 0.15f),     // 특정 능력치 1일 +2
    STREAK_PROTECT("수호 요정 등장", 0.10f),  // 스트릭 보호 토큰 +1
    HP_HEAL("여관 무료 제공", 0.15f),        // HP 30% 회복
    QUEST_SUGGEST("신비한 퀘스트 의뢰", 0.10f) // AI 추천 퀘스트 등장
}
```

### Claude AI 인카운터 내러티브 생성

각 인카운터마다 Claude API 호출로 유머러스한 2-3문장 내러티브 생성:

```
예시:
"오늘의 랜덤 인카운터: 방랑 상인 만남
Aria가 퀘스트를 완료하고 잠시 휴식을 취하는 사이, 
수상한 망토를 걸친 방랑 상인이 나타났다. 
'psst, 이거 하나 드릴게요...' 라며 건네준 것은 
바로 [집중력의 수정구]였다!"
```
