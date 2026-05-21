# 06. GTD ↔ RPG 매핑

## 6.1 핵심 개념 매핑 테이블

| GTD 개념 | D&D/RPG 개념 | 앱 구현 | 책의 근거 |
|----------|-------------|---------|----------|
| **Inbox (수집함)** | 미지의 던전 입구 / 랜덤 인카운터 | InboxScreen | "life is a series of random encounters" |
| **Capture (수집)** | 주사위 굴림 의식 | QuickCaptureSheet | 불확실성을 기록으로 변환 |
| **Clarify (명료화)** | 퀘스트 수락 여부 판단 | ClarifySheet 결정트리 | DM의 "Yes, and..." |
| **Next Action (다음 행동)** | 퀘스트 (Quest) | QuestCard | 모험의 최소 단위 |
| **Project (프로젝트)** | 캠페인 / 대형 던전 | ProjectDetailScreen | 여러 퀘스트로 구성된 장기 모험 |
| **Context (@태그)** | 지역 / 바이옴 | ContextTag | 어디서 싸울 수 있는지 |
| **Someday/Maybe** | 전설 속 던전 (소문) | SomedayList | 언젠가의 모험 |
| **Reference** | 마법사의 도서관 | ReferenceList | 지식 창고 |
| **Waiting For** | NPC에게 위임한 퀘스트 | WaitingForList | 파티원의 역할 |
| **Weekly Review** | DM 세션 / 캠페인 브리핑 | WeeklyReviewScreen | "DM of your life" |
| **2-Minute Rule** | Bonus Action (보너스 행동) | QuickDone 처리 | 즉각 행동 |
| **Delegate (위임)** | NPC에게 퀘스트 배정 | NpcInteractionScreen | 파티 협력 |
| **Complete (완료)** | 몬스터 처치 | D20RollSheet → CombatResult | 전투 해결 |
| **Priority (우선순위)** | CR (Challenge Rating) | CRBadge 색상 | 몬스터 강도 |
| **Deadline (마감일)** | 퀘스트 만료 타이머 | DeadlineTimer | 위기 상황 |
| **Energy Level** | HP (Hit Points) | HPBar | 모험가의 체력 |
| **Focus Time** | 전투 라운드 | FocusTimer (Monk) | 집중 전투 |
| **Habit/Routine** | 데일리 퀘스트 / 루틴 | DailyQuestList | 반복 모험 |
| **Life Areas** | 세계관 지역 | LifeAreaTag | 탐험 구역 |
| **Goals** | Epic Quest / 장기 캠페인 | EpicQuestScreen | 대서사시 |
| **Productivity** | 레벨 (Level) | CharacterLevel | 성장 척도 |
| **Skills** | 능력치 (Ability Scores) | AbilityScores | 캐릭터의 특기 |
| **Work Style** | 클래스 (Class) | ClassType | 모험가 유형 |
| **Personality** | 종족 + 클래스 | CharacterBackground | 개성 |
| **Team** | 파티 (Party) | (v2) Guild Feature | 동료 모험가 |
| **Manager** | DM (Dungeon Master) | WeeklyReview DM Mode | 리더십 |
| **Inbox Zero** | 던전 클리어 | InboxZeroReward | 완전 정복 |
| **Review** | 퀘스트 로그 검토 | JournalScreen | 모험 일지 |
| **Processing** | 전투 준비 | ClarifySheet | 전략 수립 |

---

## 6.2 3가지 플레이 기둥 (책 챕터 2) ↔ GTD 매핑

### 탐험 (Exploration) → GTD의 새 프로젝트/학습

```
책의 정의:
"Exploration is driven by needs and goals—searching for knowledge,
 embarking on physical, mental, and emotional journeys."

GTD 매핑:
  탐험 = 새로운 분야 학습, 새 프로젝트 시작, 습관 형성
  
앱 구현:
  - 새 컨텍스트 태그 첫 사용 → "탐험가!" 알림
  - 새 생활 영역 퀘스트 → 탐험 보너스 XP
  - Ranger 클래스: Natural Explorer 특성
  - 생활 영역 태그: 🌍 탐험 구역으로 시각화
```

### 사회적 상호작용 (Social Interaction) → GTD의 위임/협업

```
책의 정의:
"Connect with NPCs to alter the course of evil's path.
 Charisma-based skills: self-awareness, relationship management."

GTD 매핑:
  사회 = 타인과의 협력, 위임, 발표, 협상
  
앱 구현:
  - @사람 컨텍스트 태그 = NPC 연동
  - NPC 위임 기능 = 파티원에게 퀘스트 이전
  - Bard/Paladin 클래스: CHA 기반 위임 보너스
  - 클래스 궁합 시스템 (책 챕터 5 기반)
  - Charisma 체크: 소통 효과 판정
```

### 전투/갈등 해결 (Combat/Conflict Resolution) → GTD의 실행

```
책의 정의:
"Combat is reframed as conflict resolution—defeating monsters
 is solving problems and completing tasks."

GTD 매핑:
  전투 = 할 일 완료, 장애물 극복, 문제 해결
  
앱 구현:
  - 퀘스트 완료 = 몬스터 처치 (D20 전투)
  - CR = 문제의 난이도
  - 크리티컬 히트 = 최상의 퀘스트 완료 경험
  - 크리티컬 미스 = 실수, 하지만 이야기의 일부
  - "Yes, and..." = 실패도 XP로 기록
```

---

## 6.3 D20 시스템 (책 챕터 3) ↔ GTD 불확실성

```
책의 철학:
"The d20 represents life's unpredictability. 
 Critical hits and misses are both part of the story.
 Even a skilled character can fail on a bad roll."

GTD 적용:
  할 일 완료 = 능력치 + 환경 + 운(D20)의 조합
  완벽하게 준비해도 외부 요인으로 실패할 수 있다
  실패도 XP와 경험으로 전환된다

앱 구현:
  D20 결과 해석:
  1      → 크리티컬 미스: "오늘은 운이 없었지만, 내일은 다르다"
  2-9    → 미스: HP 손실, 퀘스트 복귀
  10-14  → 낮은 명중: 최소 XP만 획득
  15-19  → 명중: 정상 XP 획득
  20     → 크리티컬 히트: 2배 XP + 아이템 드롭

"Yes, and..." 구현:
  크리티컬 미스도 유머러스하게 처리
  HP 손실은 있지만 자정에 회복 (Long Rest)
  "실패는 이야기의 일부입니다" 메시지
```

---

## 6.4 클래스 시스템 (책 챕터 4) ↔ GTD 성격 유형

```
책의 철학:
"A class is an adventurer's calling. It's about mindset
 (how you approach challenges) and role (in relation to the world)."

GTD 적용:
  클래스 = 개인의 생산성 스타일과 강점
  
매핑:
BARBARIAN  → 즉각 행동형, 마감 위기에 강함
BARD       → 관계/협업형, 위임과 소통에 강함
CLERIC     → 지원형, 묵은 과제 처리와 회복에 강함
DRUID      → 균형형, 루틴과 장기 지속에 강함
FIGHTER    → 전략형, 다양한 상황에 다재다능
MONK       → 집중형, 딥 워크와 루틴 최강
PALADIN    → 약속형, 마감 이행과 장기 목표 유지
RANGER     → 자립형, 새 분야 탐험과 이동 중 생산성
ROGUE      → 효율형, 즉흥 처리와 2분 규칙 마스터
SORCERER   → 에너지형, 폭발적 집중과 유연한 조정
WARLOCK    → 헌신형, 장기 목표 집착과 AI 협력
WIZARD     → 계획형, 분석/연구/기획 최강

사용자가 클래스 퀴즈로 자신의 GTD 성격 유형 발견
(책의 "Class Quiz"를 앱 온보딩에 직접 구현)
```

---

## 6.5 클래스 궁합 시스템 (책 챕터 5) ↔ NPC 시스템

```
책의 12x12 호환성 매트릭스:
  ★★ (매우 호환)  → 협업 최고, 위임 성공률 높음
  ★  (호환)       → 일반적 협업
  ×  (비호환)     → 소통 어려움, 마찰 가능성

앱 구현:
  NPC 등록 시 추정 클래스 선택
  내 클래스 ↔ NPC 클래스 궁합 자동 계산
  
  궁합 효과:
  매우 호환: 위임 성공률 +30%, 소통 XP +20%
  호환:      위임 성공률 기본값 (70%)
  비호환:    위임 성공률 -20%, 주의 아이콘 표시

주요 궁합 관계 (책 기반):
  Barbarian  ← 최상 →  Ranger, Sorcerer
  Bard       ← 최상 →  Paladin, Fighter
  Cleric     ← 최상 →  Monk, Rogue
  Druid      ← 최상 →  Ranger, Monk
  Fighter    ← 최상 →  Bard, Paladin
  Monk       ← 최상 →  Cleric, Druid
  Paladin    ← 최상 →  Bard, Fighter
  Ranger     ← 최상 →  Barbarian, Druid
  Rogue      ← 최상 →  Cleric, Wizard
  Sorcerer   ← 최상 →  Barbarian, Warlock
  Warlock    ← 최상 →  Sorcerer, Wizard
  Wizard     ← 최상 →  Rogue, Warlock
```

---

## 6.6 DM of Your Life (책 챕터 6) ↔ Weekly Review

```
책의 철학:
"In a way, everyone is Dungeon Master of their own life.
 The DM creates the worlds and stories within which the PCs play."

DM 역할:
  1. Referee (심판): 규칙 집행 → GTD 원칙 지키기
  2. Narrator (내레이터): 스토리 창조 → 자신의 삶 해석
  3. Player of NPCs: 다른 역할 → 다양한 관점 취하기

GTD Weekly Review = DM 세션:
  "자신의 삶을 DM 시점에서 조망"
  캐릭터(나)와 DM(나)을 분리하여 객관적 검토

앱 구현:
  Weekly Review = "DM 모드" 활성화
  - UI가 DM 시점으로 전환 (3인칭 내러티브)
  - "당신의 캐릭터 Aria는 이번 주 어떠했나요?"
  - Claude AI가 DM의 목소리로 요약 제공
  - 6단계 체크리스트 = DM의 캠페인 관리 루틴
  
DM 보상 (완료 시):
  +200 XP + "현명한 DM" 칭호
  연속 4주 완료: "전설적인 DM" 전설 칭호
```

---

## 6.7 XP 시스템 (책 챕터 10) ↔ 어드벤처 저널

```
책의 철학:
"You can gain XP for defeating monsters, achieving goals,
 or just working hard at learning a new skill.
 When you have enough experience, you can level up."

어드벤처 저널:
  책은 실생활 성장을 추적하는 저널 제안
  앱의 JournalScreen이 이를 디지털화

매핑:
  완료된 퀘스트 = XP
  극복한 장애물 = 크리티컬 히트 보너스
  새 스킬 학습 = 학습 영역 XP + 레벨업
  실패와 교훈 = 크리티컬 미스 기록 (이야기로 남음)

책이 언급한 앱들과 차별화:
  Habitica: 게임화만 있고 GTD 구조 없음
  Zombies Run!: 운동에 특화, 할 일 관리 없음
  Hero's Journal: 종이 저널, 디지털화 미흡

QuestLog의 차별점:
  GTD 5단계 완전 통합
  D&D 5e SRD 기반 정교한 게임 메커니즘
  Claude AI 동적 내러티브
```

---

## 6.8 Adventurer Types (책 챕터 9) ↔ 플레이 스타일

```
책의 2가지 어드벤처 유형:

Solo Adventuring (단독 모험):
  자신의 목표, 자신의 페이스
  GTD의 기본: 개인 생산성 관리
  앱의 기본 모드

Party Adventuring (파티 모험):
  팀과 협력, 역할 분담
  GTD의 팀 버전: 공유 프로젝트
  앱 v2.0 계획

앱 구현 (v1.0):
  Solo: 현재 개발 범위
  - 혼자 모든 퀘스트 관리
  - NPC는 위임/협력자로만 존재 (앱 밖의 사람)

앱 구현 (v2.0):
  Party: 길드 기능
  - 공유 프로젝트 (캠페인)
  - 역할 분담 (각자 다른 클래스)
  - 파티원의 진행도 확인
  - 파티 XP 공유 시스템
```
