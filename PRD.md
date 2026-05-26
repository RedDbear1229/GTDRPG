# QuestLog PRD (Product Requirements Document)

> **이 문서의 위치**: 프로젝트의 단일 진입점. 의사결정의 근거와 범위를 정의한다.
> **상세 설계는** `docs/01_overview.md` ~ `docs/10_design_system.md` 참조.
> **대상 독자**: 개발자(혼자) + Claude Code (작업 시 컨텍스트 자동 로드).

---

## 1. 프로젝트 개요

### 무엇을 만드는가

**QuestLog** — GTD(Getting Things Done) 방법론과 D&D 5e 기반 TRPG 시스템을 결합한 Android 할 일 관리 앱.

- 실생활 할 일을 "퀘스트"로, 완료를 "전투"로, 누적 완수를 "캐릭터 레벨업"으로 매핑
- 자동 전투형 — 퀘스트 완료 체크 시 D20 주사위 자동 굴림 → XP/HP 갱신 → 결과 화면
- Claude AI가 (사용자 동의 시) 전투·주간 리뷰·랜덤 인카운터의 서사적 내러티브 생성
- 로컬 우선(local-first) — 백엔드·로그인·계정 없음

### 왜 만드는가 (Why)

| 동기 | 설명 |
|------|------|
| 개인의 GTD 실천 보조 | 기존 GTD 앱(Things, Todoist)은 게임적 동기부여 부족 — 매일 인박스 정리가 의무로 느껴짐 |
| RPG 메커니즘으로 행동 강화 | "오늘 인박스 비웠다 = 던전 클리어 = 보상" 구조로 즉각적 보상 루프 형성 |
| 책 *"How to Be More D&D"* 철학 구현 | "DM of Your Life" — 자신의 인생을 캠페인처럼 바라보는 관점을 디지털로 |
| 학습/실험 프로젝트 | Jetpack Compose, Hilt, Room, Claude API 등 최신 Android 스택 실전 적용 |

### 배포 범위

- **혼자 사용** — Play Store 출시 없음, ADB sideload 또는 GitHub Actions APK artifact로 직접 설치
- **확장 가능 구조** — 미래에 KMP(iOS)·Supabase 동기화·다중 사용자로 확장 가능하도록 Domain 레이어를 순수 Kotlin으로 유지

---

## 2. 타겟 사용자

### Primary (혼자 사용자 — 본인)

- **연령/직군**: 25-40세, 지식 근로자 (개발자/기획자/디자이너 등)
- **GTD 경험**: GTD 방법론을 알고 있으며 인박스 0을 추구
- **TRPG 친숙도**: D&D 5e 기본 규칙(클래스, 능력치, D20)을 안다
- **디바이스**: Android (Pixel 또는 Galaxy), 매일 사용
- **언어**: 한국어 단일

### 사용 시나리오

```
출근길 (08:00):
  - 알림으로 오늘 마감 퀘스트 3개 확인
  - 위젯으로 갑작스러운 아이디어를 Inbox에 즉시 캡처

업무 중 (10:00-18:00):
  - 짧은 할 일은 2분 룰 → 즉시 완료 (QuickDone)
  - 큰 할 일은 ClarifySheet에서 다음 행동 정의 → QuestBoard 배치
  - 완료할 때마다 D20 굴림 애니메이션 + XP 획득 → 도파민 보상

저녁 (21:00):
  - HP 위기 알림 (오늘 완료 0개 + HP 25% 이하)
  - 마지막 한 개라도 완료해서 스트릭 유지

주말 (토 10:00):
  - WeeklyReview 알림 → "DM 모드"로 한 주 회고
  - Claude AI 요약 + 다음 주 조언
```

---

## 3. 핵심 기능 목록 (MVP — v1.0)

> 상세 화면 명세: `docs/02_screens.md` · 게임 메커니즘: `docs/04_game_mechanics.md` · GTD 매핑: `docs/06_gtd_rpg_mapping.md`

### 3.1 GTD 5단계 플로우

| 단계 | 기능 | 화면 |
|------|------|------|
| Capture | 텍스트/위젯/공유로 Inbox에 즉시 캡처 *(음성/STT는 Phase 4 F4.5 — F4.0 동의 인프라 선행)* | InboxScreen, QuickCaptureSheet |
| Clarify | 6단계 결정트리(행동 가능? 2분 이내? 위임?)로 분류 | ClarifySheet |
| Organize | Task/Project/Someday/Reference/Waiting/Delete 자동 분류 | QuestBoard (칸반) |
| Reflect | 일일/주간/월간 리뷰 + 통계 | JournalScreen, WeeklyReviewScreen |
| Engage | 컨텍스트·에너지·시간 기반 추천 | QuestBoard 우선순위 정렬 |

### 3.2 캐릭터 시스템

- 12 D&D 클래스 중 선택 (온보딩 시 클래스 퀴즈)
- 6 능력치 (STR/DEX/CON/INT/WIS/CHA) — 4d6 drop lowest 굴림
- HP/XP/Level (D&D 5e 표준 XP 임계값)
- 클래스별 특수 능력 (Lv1 1개 — Rage, Bardic Inspiration 등)

### 3.3 D20 자동 전투 (핵심 게임 루프)

```
퀘스트 완료 체크
  → CR 자동 계산 (urgency × 0.4 + complexity × 0.3 + time × 0.3)
  → D20 SecureRandom 굴림
  → 공격 굴림 (D20 + 능력치 수정 + 숙련 + 장비) vs 몬스터 AC
  → 결과:
    20      → 크리티컬 히트 (2배 XP + 아이템 드롭)
    15-19   → 명중 (기본 XP)
    10-14   → 낮은 명중 (최소 XP)
    2-9     → 미스 (HP 손실 + Inbox 복귀)
    1       → 크리티컬 미스 (HP 손실 + 유머러스 메시지)
```

**원자성 보장 (필수)**: 단일 Room `@Transaction`으로 Task 상태 + CombatLog + Character 업데이트.
조건부 UPDATE (`WHERE status='ACTIVE'`)로 더블탭·재시도 시 중복 보상 차단.
상세: `docs/08_tech_stack.md` 8.5절.

### 3.4 HP / 스트릭 시스템

- HP: 클래스별 Hit Die + CON 수정 — Miss 시 손실
- Long Rest: 자정 자동 HP 전체 회복 (WorkManager)
- Short Rest: 연속 3회 완료 시 HP 25% 회복
- 스트릭: 매일 1개 이상 완료 시 +1일, 미완료 시 리셋 (보호 토큰 사용 가능)

### 3.5 아이템 / 인벤토리

- 5등급 (Common/Uncommon/Rare/Very Rare/Legendary)
- 크리티컬 히트 시 CR 기반 드롭률 적용
- 5 슬롯 장비 (Weapon/Armor/Ring/Necklace/Misc)
- 장비 효과: 공격 보너스, XP 배율, HP 보너스, 특수 효과

### 3.6 NPC / 위임

- 안드로이드 연락처 연동 (선택)
- NPC에 추정 클래스 부여 → 12×12 호환성 매트릭스로 위임 성공률 계산
- 위임된 Task는 WaitingFor 목록에서 추적

### 3.7 Claude AI 내러티브 (선택 기능)

7가지 시나리오: 전투 내러티브, 크리티컬 히트 특별, 주간 리뷰 요약, Clarify 제안, 레벨업 축하, 랜덤 인카운터, 크리티컬 미스 위로.

**개인정보 보호 원칙 (필수)**:
- 기본값 OFF — 명시적 동의(ConsentDialog) 없이 API 호출 금지
- PromptSanitizer로 제목 50자 제한, 메모·첨부·NPC 이름 제외
- 동의 없으면 로컬 폴백 템플릿 사용
- 상세: `docs/07_claude_api.md` 7.0절

### 3.8 WeeklyReview (DM 모드)

- 토요일 10:00 알림
- 6단계 체크리스트 (Inbox/Projects/Waiting/Someday/Reflection/Next Week)
- Claude AI가 DM의 목소리로 한 주 요약 + 다음 주 조언
- 완료 시 +200 XP

### 3.9 Memory of the Day (솔로 RPG 회고)

- **컨셉**: 하루 1회, 그날 가장 의미 있었던 완료 1개를 골라 짧은 메모(≤500자)를 남기는 일기 모드
- **하루 1엔트리 강제** — DB 레벨 `entryDate UNIQUE` 제약. 다음 날이 되면 "어제의 기억"은 잠금
- **즉시 정산 부담 없음** — 퀘스트 완료 직후가 아닌, 사용자가 원할 때 (저녁/하루 끝) 작성
- **5상태 UI**: NoCompletions / Selecting / Writing / Saved / Expired
- **선택적 Claude AI 윤색** — 동의 + 활성화 시에만, 4종 톤(StrongHit/WeakHit/Miss/None) 분기. 원본/윤색본 토글 가능
- **Weekly Review 통합** — 이번 주 7일 메모 미니카드 표시
- **리마인더**: 기본 21:00, 오늘 완료 ≥1 AND 미작성 시에만 알림 (스팸 방지)

> 솔로 RPG 패러다임(Ironsworn/Mythic GME) 연구 후 "즉시 정산" 대신 "지연 정산 — 하루 1회" 채택. 글쓰기 부담 최소화 + 회고 자연 유도.

---

## 4. 향후 기능 목록 (Phase 2+)

> v1.0에는 들어가지 않는다. 아키텍처는 확장 가능하도록 설계되어 있다.

### v1.5 (안정화)

- [ ] Firebase Crashlytics (개인 사용도 크래시 로그 필요)
- [ ] 위젯 고도화 — Glance API 마이그레이션 / 인라인 캡처(RemoteViews 텍스트 입력) / 오늘 마감 카운트 표시 *(v1 위젯은 탭→앱 캡처 시트 오픈만 지원)*
- [ ] 다크/라이트 테마 토글 (현재는 다크만)

### v2.0 (확장)

- [ ] **Kotlin Multiplatform** — iOS 클라이언트 추가 (Domain 레이어 재사용)
- [ ] **Supabase 동기화** — 다기기 데이터 동기화 (선택 기능)
- [ ] **Party 모드** — 여러 사용자가 공유 프로젝트(캠페인)에서 협력
- [ ] **Guild 기능** — 길드원 진행도 공유, XP 공유 시스템
- [ ] **Class Lv2+ 특수 능력** — 현재는 Lv1만, 향후 Lv20까지 확장

### v3.0+ (실험)

- [ ] Wear OS 컴패니언 앱 (워치에서 빠른 완료 체크)
- [ ] 음성 우선 인터페이스 (운전 중 Capture)
- [ ] 캐릭터 시각화 (3D 아바타 + 장비 표시)

---

## 5. 기술 스택 (결정 사항)

> 선택 근거 전문: `docs/08_tech_stack.md` 8.4절 (확정 기술 스택 결정 사항)

### 5.1 핵심 스택

| 영역 | 선택 | 핵심 이유 |
|------|------|----------|
| 언어 | **Kotlin 2.1** | Android 표준, null safety, coroutines, KMP 가능 |
| UI | **Jetpack Compose** (BOM 2024.12) | 선언형 UI, Material3, Compose Navigation 2.8+ type-safe |
| 아키텍처 | **MVVM + Clean Architecture** | Domain 레이어 순수 Kotlin → KMP 확장 가능 |
| DI | **Hilt 2.53** | 컴파일 타임 검증, WorkManager/ViewModel 통합 |
| 로컬 DB | **Room 2.6** | Flow 네이티브, `@Transaction` 원자 쓰기 |
| 직렬화 | **kotlinx.serialization 1.7** (단독) | null-safe, KMP 대응 — **Gson 사용 안 함** |
| 네트워크 | **Retrofit 2.11 + kotlinx.serialization 컨버터** | Gson 컨버터 대체 |
| 비동기 | **Coroutines 1.9 + Flow** | Android 표준 |
| 백그라운드 | **WorkManager 2.10** | 자정 HP 리셋, 일일 알림, 랜덤 인카운터 |
| 보안 | **EncryptedSharedPreferences** | Claude API Key 저장 |
| 설정 | **DataStore Preferences 1.1** | SharedPreferences 대체 |

### 5.2 UI 보조 라이브러리

| 영역 | 선택 | 용도 |
|------|------|------|
| 애니메이션 | **Lottie Compose 6.6** | D20 굴림, 레벨업, 크리티컬 히트 이펙트 |
| 차트 | **Vico 2.0** | StatisticsScreen 라인/바/파이 차트 |
| 이미지 | **Coil 2.7** | 아바타, 아이템 아이콘 |
| 로깅 | **Timber 5.0** | 디버그 로그 |

### 5.3 테스트 스택

| 영역 | 선택 | 용도 |
|------|------|------|
| 단위 테스트 | **JUnit 5 (Jupiter)** | 파라미터화 테스트, 빠른 JVM 실행 |
| 모킹 | **MockK 1.13** | Kotlin 네이티브 모킹, coroutine 지원 |
| Flow 테스트 | **Turbine 1.2** | `awaitItem()` 체인 검증 |
| 스크린샷 테스트 | **Paparazzi 1.3** | 에뮬레이터 불필요, Compose 컴포넌트 회귀 방지 |
| DB 테스트 | **Robolectric 4.14** + Room In-Memory | JVM에서 Room DAO 테스트 |
| UI 테스트 | **Compose UI Test** (최소화) | 핵심 플로우 1-2개만 |

### 5.4 빌드 / CI

| 영역 | 선택 | 용도 |
|------|------|------|
| 빌드 | **Gradle Kotlin DSL + Version Catalog** | `libs.versions.toml` 단일 파일 관리 |
| AGP | **8.7** | JDK 17, KSP 지원 |
| CI | **GitHub Actions** | PR마다 testDebugUnitTest + verifyPaparazziDebug |
| 배포 | **GitHub Actions APK artifact + ADB sideload** | Play Store 불필요 |

### 5.5 절대 사용하지 않을 것

| 제외 | 이유 |
|------|------|
| ❌ Gson | Kotlin null safety 불완전, kotlinx.serialization으로 통일 |
| ❌ JUnit 4 (unit test) | JUnit 5의 파라미터화·중첩·확장 활용 |
| ❌ Mockito | MockK가 Kotlin final class, suspend 함수에 더 적합 |
| ❌ RxJava | Coroutines + Flow로 완전 대체 가능 |
| ❌ Realm | 락인 심각, MongoDB 인수 후 불투명 |
| ❌ Fragment | Compose 패러다임과 충돌 |
| ❌ Dagger 직접 | Hilt 래핑으로 충분, 보일러플레이트 극심 |
| ❌ 자체 백엔드 서버 | 혼자 사용에 과도, 필요 시 Supabase BaaS 사용 |
| ❌ Flutter/RN 전환 | 이미 Compose 설계 완료, 전환 비용 극심 |

---

## 6. 데이터 모델 (개요)

> **단일 진실 공급원 (SSOT)**: `docs/05_data_model.md`
> - §5.1.1 — 현재 구현 (Room v2, 3 엔티티 — F1.1)
> - §5.1.2 — 계획 최종 (Room v12, 15 엔티티 — F6.1 완료 시점)
> - §5.6.1 — 마이그레이션 SSOT (v2가 fresh install 첫 활성 스키마, 수동 마이그레이션은 v3→v4 / v6→v7만)
> - §5.6.4 — 현재 v2 DatabaseModule (코드와 1:1 일치)
> - §5.6.5 — 계획 v12 DatabaseModule 최종 형태
>
> PRD는 엔티티 목록·필드·관계를 직접 복제하지 않는다. 변경 시 SSOT 한 곳만 수정하여 drift 방지.

### 6.1 엔티티 그룹 (Phase별)

| Phase | Room ver | 신규 엔티티 | 비고 |
|-------|---------|------------|------|
| F1.1 | **v2** (구현됨) | `InboxItem`, `Task`, `Project` | 캡처·명료화 — fresh install 첫 활성 스키마 |
| F2.1 | v3 | `Character` | 캐릭터 코어 |
| F3.1 | v4 | `CombatLog` | D20 전투 (+ `TaskEntity` 컬럼 추가) |
| F4.1 | v5 | `Item`, `CharacterItem` | 인벤토리 |
| F4.2 | v6 | `Npc` | 협력자 |
| F4.4 | v7 | `EncounterLog`, `XpAward` | 랜덤 인카운터 + 보상 원장 |
| F4.0 | v8 | `ConsentRecord` | 프라이버시 동의 이력 |
| F5.x | v9 ~ v11 | `WeeklyReview`, `Achievement`, `CharacterAchievement` | 주간 리뷰·업적 |
| F6.1 | v12 | `MemoryEntry` | 하루 1엔트리, `entryDate UNIQUE` |

**현재 구현된 엔티티 3개 (v2)**, **계획 최종 15개 (v12)**. 상세 스키마·DAO·FK 관계는 `docs/05_data_model.md §5.6.1` 참조.

### 6.2 데이터 무결성 계약 (핵심 5개)

코드/문서 어디서든 위반 시 PR 리뷰의 최우선 차단 사유:

1. **Task 완료는 원자적** — `CompletionDao.commitCompletion()` 단일 `@Transaction` (CLAUDE.md "원자성 계약")
2. **CombatLog는 불변** — `OnConflictStrategy.IGNORE`로 재실행 시 중복 INSERT 차단
3. **상태 전이 가드** — `WHERE status='ACTIVE'` 조건부 UPDATE, 0 rows면 `AlreadyCompleted` 반환
4. **인카운터 보상 멱등성** — `PENDING → CLAIMED` 조건부 UPDATE로 중복 수령 차단
5. **MemoryEntry 하루 1엔트리** — `entryDate UNIQUE` + `OnConflictStrategy.ABORT`. UPSERT/REPLACE 금지 (기존 메모 손실)
6. **소프트 삭제** — Task는 `status='DELETED'` 마킹, 물리 삭제 금지 (저널 보존)

---

## 7. API 설계

### 7.1 외부 API — Claude API (선택 기능)

> 상세: `docs/07_claude_api.md`

**Base URL**: `https://api.anthropic.com/`
**인증**: `x-api-key` 헤더 (사용자가 직접 입력, EncryptedSharedPreferences 저장)
**모델**: `claude-sonnet-4-6` (기본) / `claude-haiku-4-5-20251001` (빈도 높은 호출)

| 엔드포인트 | 메서드 | 용도 |
|----------|--------|------|
| `/v1/messages` | POST | 7가지 시나리오 모두 단일 엔드포인트 사용 (System/User 프롬프트만 변경) |

**호출 전 필수 조건**:
```kotlin
suspend fun canCallApi(): Boolean =
    appSettings.aiConsentGiven        // 명시적 동의
    && appSettings.claudeApiEnabled   // 기능 토글 ON
    && secureStorage.getApiKey() != null
```

**호출 빈도 제한**:
- 무료 사용자: 일 3회
- 우선순위: 레벨업 > 전투 완료 > 주간 리뷰 > 퀘스트 제안 > 랜덤 인카운터

**데이터 최소화 (PromptSanitizer)**:
- 제목 50자 제한, 메모/첨부/NPC 이름 전송 제외
- 주간 통계는 개수만 전송, 미완료 퀘스트 제목 제외

### 7.2 내부 API — Repository 인터페이스

> Domain 레이어의 추상화, 구현은 Data 레이어. 상세: `docs/05_data_model.md` 5.4절

| Repository | 주요 메서드 |
|------------|------------|
| **TaskRepository** | `getInboxItems(): Flow<List>`, `getActiveTasks(): Flow<List>`, `completeTask()`, `returnToInbox()` |
| **CharacterRepository** | `getCharacter(): Flow<Character?>`, `gainXP(Long)`, `takeDamage(Int)`, `updateStreak(Boolean)` |
| **CombatRepository** | `resolveCombat(Task, Character): CombatResult`, `getCombatLogByTaskId(String)` |
| **ClaudeRepository** | `generateCombatNarrative()`, `generateWeeklySummary()`, `getFallback()` |
| **ItemRepository** | `addItemToInventory()`, `equipItem(slot)`, `getEquippedItems(): Flow` |
| **NpcRepository** | `getNpcs(): Flow`, `delegateTask(taskId, npcId)`, `calculateCompatibility()` |

### 7.3 백그라운드 작업 — WorkManager

| Worker | 스케줄 | 작업 |
|--------|--------|------|
| **HPResetAndStreakWorker** | 매일 00:00 | HP 전체 회복, 스트릭 증감 처리 |
| **DailyReminderWorker** | 매일 08:00 | 오늘 마감 퀘스트 3개 알림 |
| **WeeklyReviewReminderWorker** | 토요일 10:00 | 주간 리뷰 알림 |
| **RandomEncounterWorker** | 하루 1-2회 (랜덤) | 랜덤 인카운터 생성 |
| **EncounterExpirationWorker** | 매시간 | PENDING 인카운터 48시간 후 EXPIRED |

---

## 8. 완료 기준 (Definition of Done)

각 기능이 "완성"되었다고 판단하는 기준. 모두 충족하지 않으면 다음 Phase로 진행 금지.

### 8.1 공통 완료 기준 (모든 기능)

- [ ] 단위 테스트 작성 (UseCase 90%+, Repository 75%+, ViewModel 80%+)
- [ ] 핵심 Composable에 Paparazzi 스크린샷 테스트 작성
- [ ] GitHub Actions CI 그린 (`./gradlew testDebugUnitTest verifyPaparazziDebug` 통과)
- [ ] 실기기에서 수동 테스트 1회 (Pixel 또는 Galaxy)
- [ ] 크래시 없이 30분 연속 사용 가능

### 8.2 기능별 완료 기준

#### GTD MVP (Phase 1)
- [ ] Inbox 항목 추가 → Clarify → QuestBoard 배치 → 완료까지 전체 플로우 작동
- [ ] 앱 재시작 후 데이터 유지 (Room 영속화 확인)
- [ ] **위젯 설치 검증** — 홈 화면 위젯 추가 가능, 위젯 탭 시 앱 캡처 시트가 즉시 열리고 입력 → Inbox 저장 동작 (`InboxWidgetProvider` + `EXTRA_OPEN_CAPTURE` PendingIntent 경로)
- [ ] 공유 시트 캡처 작동 (`ACTION_SEND` 텍스트 → Inbox 즉시 저장)

> ⚠️ 음성 입력(STT)·`RECORD_AUDIO`는 Phase 1 범위 밖이다. F4.0 프라이버시 동의 인프라 → F4.5 STT 순으로만 진입한다 (`IMPLEMENTATION_PLAN.md` F1.2 / F4.5, CLAUDE.md 컴팩션 보존 항목 #3).

#### 캐릭터 시스템 (Phase 2)
- [ ] 12 클래스 모두 선택 가능, 클래스 퀴즈 작동
- [ ] 4d6 drop lowest 능력치 굴림, 재굴림 1회 가능
- [ ] 레벨업 시퀀스 (Lv1→Lv2 임계값 300 XP 도달 시 LevelUpScreen 표시)

#### D20 전투 (Phase 3) — 핵심
- [ ] 퀘스트 완료 체크 → D20 애니메이션 → 결과 표시 → XP 획득까지 **3초 이내** 완결
- [ ] 크리티컬 히트 (D20=20) 시 황금 파티클 + 2배 XP + 아이템 드롭
- [ ] 크리티컬 미스 (D20=1) 시 유머러스 메시지 + HP 손실
- [ ] **더블탭 시 중복 보상 없음** (CompletionDao 트랜잭션 단위 테스트로 검증)
- [ ] **앱 크래시 후 재시작 시 중복 보상 없음** (상태 전이 가드 검증)
- [ ] 자정 HP 자동 회복 작동 (WorkManager 실기기 검증)

#### 아이템 / NPC (Phase 4)
- [ ] 크리티컬 히트 시 등급별 드롭률대로 아이템 드롭
- [ ] 5 슬롯 장비 착용/해제 작동, XP 배율/공격 보너스 실제 반영
- [ ] NPC 등록 → 클래스 부여 → 위임 → 호환성 매트릭스 기반 성공률 계산
- [ ] 랜덤 인카운터 알림 수신 후 PENDING → CLAIMED 정상 전이 (중복 수령 차단 확인)

#### Claude AI (Phase 5)
- [ ] **동의 다이얼로그 표시 전에는 API 호출 0회** (단위 테스트로 검증)
- [ ] PromptSanitizer로 메모/NPC 이름 미전송 확인
- [ ] 오프라인 시 로컬 폴백 정상 작동
- [ ] 7가지 시나리오 모두 정상 응답 + 로컬 폴백 모두 작동
- [ ] 일일 사용량 카운터 정상 동작 (4회째 호출 시 폴백)

#### Weekly Review (Phase 5)
- [ ] 토요일 10:00 알림 수신
- [ ] 6 step 모두 체크 시 +200 XP 지급
- [ ] AI 요약 생성 (동의 시) 또는 로컬 폴백
- [ ] 4주 연속 완료 시 "전설적인 DM" 칭호 지급

---

## 9. 하지 않을 것 (Non-Goals)

> 명시적으로 v1.0 범위 밖. 요구사항 추가 시 이 목록을 먼저 확인할 것.

### 9.1 사용자/계정

- ❌ **로그인 / 회원가입** — 단일 사용자, 로컬 전용
- ❌ **소셜 기능** — 친구, 팔로우, 리더보드 없음
- ❌ **다중 사용자 / Party 모드** — v2.0으로 연기
- ❌ **다중 캐릭터** — 한 사용자 = 한 캐릭터

### 9.2 비즈니스

- ❌ **Play Store 출시** — 혼자 사용, sideload만
- ❌ **수익화 (광고, 인앱결제, 구독)** — 무료 + 광고 없음
- ❌ **프리미엄 티어** — 모든 기능 무료
- ❌ **마케팅, 분석(GA, Mixpanel)** — 본인 사용량만 자체 통계

### 9.3 플랫폼

- ❌ **iOS 앱** — v2.0 KMP 확장 시 고려
- ❌ **웹 앱 / 데스크탑** — Android 단독
- ❌ **Wear OS** — v3.0+
- ❌ **태블릿 최적화** — 폰 세로 모드 위주 (태블릿 호환은 하되 전용 레이아웃 없음)

### 9.4 백엔드 / 동기화

- ❌ **자체 서버** — 운영 비용/복잡도 과도
- ❌ **클라우드 동기화** — v2.0 Supabase로 검토
- ❌ **백업 / 복원** — 사용자가 직접 `adb backup` 또는 Room DB 파일 복사
- ❌ **푸시 알림 서버** — 로컬 WorkManager만 사용

### 9.5 국제화 / 접근성

- ❌ **다국어 (i18n)** — 한국어 단일 (영어 대응 안 함)
- ❌ **다크/라이트 테마 토글** — 다크만 (v1.5)
- ❌ **글꼴 크기 조정** — 시스템 기본 설정 따름
- ❌ **TalkBack 완전 지원** — 기본 접근성만, 전용 최적화 안 함

### 9.6 콘텐츠

- ❌ **Lv2+ 클래스 특수 능력** — Lv1 1개씩만 (확장 여지 남김)
- ❌ **종족 (Race) 시스템** — 클래스만 사용
- ❌ **마법 주문 (Spell) 카드** — 추상화된 능력만
- ❌ **던전 맵 / 시각적 던전** — 텍스트 내러티브만
- ❌ **전체 D&D 5e 룰북** — GTD에 필요한 부분만 차용
- ❌ **전면 솔로 RPG 모드** (Mythic GME / Ironsworn 전체 룰셋) — Memory of the Day(§3.9) 1엔트리/일 형태로만 흡수. Chaos Factor, Fate Chart, Move 목록 등은 v1.0 범위 밖
- ❌ **하루 다중 메모 / 자유 일기** — UNIQUE 제약으로 하루 1엔트리만. 자유 일기를 원하면 외부 앱 사용

### 9.7 기술

- ❌ **반응형/멀티 윈도우 UI** — 단일 폰 세로 레이아웃
- ❌ **위젯 인터랙티브 컨트롤** — v1은 RemoteViews 탭→앱 캡처 시트 오픈만 지원. 위젯 안 인라인 입력 / Glance API / 오늘 마감 카운트 표시는 v1.5
- ❌ **Compose Multiplatform UI** — Android Compose만
- ❌ **GraphQL** — Retrofit REST만
- ❌ **WebView 기반 화면** — 모두 네이티브 Compose

---

## 부록 A — 참조 문서 인덱스

| 파일 | 내용 |
|------|------|
| `docs/01_overview.md` | 앱 비전, 5 디자인 원칙, 경쟁 분석 |
| `docs/02_screens.md` | 27개 화면/시트 ASCII 목업 + 인터랙션 명세 |
| `docs/03_gtd_system.md` | ClarifySheet 결정트리, CR 자동 계산 알고리즘, Context 태그 |
| `docs/04_game_mechanics.md` | D20 전투, XP 공식, HP, 12 클래스 능력, 아이템 카탈로그, 몬스터 표 |
| `docs/05_data_model.md` | 15 Room 엔티티 (F6.1 완료 시점) + DAO + TypeConverter + Migration SSOT (§5.6.1) |
| `docs/06_gtd_rpg_mapping.md` | GTD ↔ D&D 25행 매핑 테이블, 3 플레이 기둥, 클래스 궁합 |
| `docs/07_claude_api.md` | **7.0 개인정보 보호 원칙**, 7가지 AI 시나리오, 폴백 템플릿 |
| `docs/08_tech_stack.md` | 아키텍처, 모듈, 의존성, **8.5 원자적 완료 처리**, NetworkModule, CI/CD |
| `docs/09_roadmap.md` | 7-Phase 325h 개발 계획 (Memory of the Day 포함, Play Store 출시 없음) — 단일 진실은 IMPLEMENTATION_PLAN.md 요약표 |
| `docs/10_design_system.md` | 컬러 팔레트, 타이포그래피, 핵심 Composable, 애니메이션, 사운드 |

## 부록 B — 의사결정 이력

| 날짜 | 결정 | 근거 |
|------|------|------|
| 초기 | TRPG 스타일: **자동 전투형** | 빠른 보상 루프 (3초 이내) |
| 초기 | 책 기반: *How to Be More D&D* | "DM of Your Life" 철학을 GTD에 매핑 |
| 2026-05 | Codex 적대적 리뷰 #1 | 3대 이슈 (원자성, 프라이버시, 누락 엔티티) 수정 |
| 2026-05 | 기술 스택 확정 | Gson 제거 → kotlinx.serialization 단독, JUnit 5, Paparazzi 추가 |
| 2026-05 | 배포 범위 | Play Store 미출시, GitHub Actions APK + ADB sideload |
| 2026-05-23 | Phase 6 Memory of the Day 신설 | 솔로 RPG 패러다임 연구 후 "즉시 정산" 대신 "하루 1엔트리 지연 정산" 채택. 전면 솔로 RPG 모드는 §9.6에 제외. Phase 6(베타+배포) → Phase 7로 시프트, 총 290h → 325h |
