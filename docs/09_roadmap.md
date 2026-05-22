# 09. 개발 로드맵

## 전체 타임라인

```
Phase 0  ████░░░░░░░░░░░░░░░░░░░  1주   환경 설정 + 프로젝트 셋업
Phase 1  ░░░░████████████░░░░░░░  3주   GTD 핵심 기능 MVP
Phase 2  ░░░░░░░░░░░░████████░░░  2주   캐릭터 시스템 + 온보딩
Phase 3  ░░░░░░░░░░░░░░░░████████ 2주   전투 시스템 (핵심 게임 루프)
Phase 4  ████████░░░░░░░░░░░░░░░  2주   아이템 + NPC + 클래스 특수 능력
Phase 5  ░░░░████████████░░░░░░░  3주   Claude API + 통계 + 폴리싱
Phase 6  ░░░░░░░░░░░░░░░░████████ 2주   베타 테스트 + 플레이스토어 출시
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
총 개발 기간: 약 15주 (3.5개월)
```

---

## Phase 0: 프로젝트 셋업 (1주)

### 목표
개발 가능한 기반 구축. 첫 빌드 실행.

### 작업 목록

#### 환경 구성
- [ ] Android Studio Ladybug 설치 확인 (AGP 8.7+)
- [ ] Android 프로젝트 생성
  - Package: com.questlog
  - Language: Kotlin
  - Min SDK: 26
  - Build system: Gradle Kotlin DSL
- [ ] Git 저장소 초기화 + .gitignore 설정
- [ ] GitHub 원격 저장소 연결

#### 의존성 설정
- [ ] libs.versions.toml (Version Catalog) 설정
- [ ] Hilt 플러그인 + 기본 설정
- [ ] Room 플러그인 + KSP 설정
- [ ] Compose BOM 설정
- [ ] 기본 의존성 모두 추가

#### 아키텍처 뼈대
- [ ] 패키지 구조 생성 (feature/, core/, worker/)
- [ ] BaseViewModel 작성 (UiState + UiEvent 패턴)
- [ ] Hilt Module 기본 구조 (AppModule, DatabaseModule, NetworkModule)
- [ ] MainActivity + NavGraph 기본 구조

#### 디자인 시스템
- [ ] Color.kt (다크 테마 기본 팔레트)
  - 배경: #0D1117 (어두운 판타지)
  - 주색: #C19A6B (고대 금색)
  - 강조: #7C3AED (마법 보라색)
  - 위험: #EF4444 (드래곤 빨간색)
  - 성공: #10B981 (자연 초록색)
- [ ] Typography.kt (MedievalSharp 제목, Noto Sans KR 본문)
- [ ] Shape.kt (둥근 모서리 기본값)
- [ ] Theme.kt (다크 테마 기본, 라이트 테마 추후)

#### Room DB 기반
- [ ] QuestLogDatabase 클래스 생성
- [ ] 모든 Entity 클래스 작성
- [ ] TypeConverters 작성
- [ ] 기본 DAO 인터페이스 작성 (빈 구현)

### 완료 기준
- `./gradlew assembleDebug` 성공
- 에뮬레이터에서 앱 실행 가능
- Hilt 주입 동작 확인

---

## Phase 1: GTD 핵심 기능 MVP (3주)

### 목표
게임 요소 없이 GTD 5단계 플로우 완성. 실제 할 일 관리 가능한 상태.

### Week 1: Capture + Clarify

#### InboxScreen
- [ ] InboxScreen UI 완성
  - LazyColumn (InboxItem 목록)
  - FilterChip (전체/오늘/긴급/미분류)
  - TopAppBar (카운트 배지)
  - EmptyState (Inbox Zero)
- [ ] InboxItemCard 컴포넌트
  - 스와이프 제스처 (우측: Clarify, 좌측: 삭제)
  - 롱프레스 다중 선택 모드
- [ ] InboxViewModel + GetInboxItemsUseCase
- [ ] InboxItemEntity + InboxItemDao

#### QuickCaptureSheet
- [ ] BottomSheet UI (텍스트 입력)
- [ ] 자동 포커스 + 소프트 키보드 처리
- [ ] 음성 입력 버튼 (SpeechRecognizer 연동)
- [ ] Enter키로 저장 + FAB 저장 버튼
- [ ] CaptureItemUseCase
- [ ] 저장 후 애니메이션 피드백

#### ClarifySheet
- [ ] 6단계 결정 트리 UI
- [ ] Progress 인디케이터 (1/6 ~ 6/6)
- [ ] 각 단계 컴포넌트:
  - Q1: 라디오 버튼 (실행가능/아니오)
  - Q2: 텍스트 입력 (다음 행동)
  - Q3: 라디오 버튼 (2분)
  - Q4: 라디오 버튼 (위임)
  - Q5: 프로젝트 선택 드롭다운
  - Q6: CR 슬라이더, 시간, 마감, 컨텍스트, 영역
- [ ] CR 자동 계산 알고리즘 구현
- [ ] ClarifyItemUseCase

### Week 2: Organize + Engage

#### QuestBoardScreen
- [ ] 탭 레이아웃 (활성/프로젝트/대기중/언젠가)
- [ ] 활성 퀘스트 탭: 날짜별 그룹 LazyColumn
- [ ] QuestCard 컴포넌트 (CR 색상, 아이콘, 정보)
- [ ] 완료 체크박스 (임시: 바로 완료 처리, 전투 없이)
- [ ] 필터/정렬 (컨텍스트, 기간)
- [ ] 프로젝트 탭: 기본 리스트 뷰

#### TaskDetailScreen
- [ ] 퀘스트 상세 정보 표시
- [ ] 편집 기능 (제목, 설명, CR, 마감, 맥락)
- [ ] 삭제 기능
- [ ] 완료 버튼

#### ProjectDetailScreen (기본형)
- [ ] 프로젝트 제목/설명
- [ ] 하위 퀘스트 목록
- [ ] 진행률 표시 (completedCount / totalCount)
- [ ] 퀘스트 추가 버튼

### Week 3: Reflect + 데이터 영속화

#### JournalScreen (기본형)
- [ ] 어드벤처 로그 탭 (완료 퀘스트 타임라인)
- [ ] 날짜별 그룹핑
- [ ] 간단한 완료 기록 카드

#### WeeklyReviewScreen (기본형)
- [ ] 6단계 체크리스트 UI
- [ ] Step 1: 인박스 비우기 연동
- [ ] Step 2: 프로젝트 검토 연동
- [ ] Step 5: 이번 주 통계 (간단)
- [ ] 완료 시 보상 메시지 표시

#### 데이터 영속화
- [ ] DataStore AppSettings 구현
- [ ] Repository 구현체 완성 (Task, Project, InboxItem)
- [ ] 앱 재시작 시 데이터 유지 확인
- [ ] 백그라운드에서 데이터 변경 시 UI 실시간 반영 (Flow)

### 완료 기준
- Inbox에 항목 추가 → Clarify → QuestBoard 배치 → 완료까지 전체 플로우 작동
- 앱 재시작 후 데이터 유지
- 기본 통계 표시
- 내부 테스트 빌드 APK 배포

---

## Phase 2: 캐릭터 시스템 + 온보딩 (2주)

### Week 4: 온보딩 플로우

- [ ] WelcomeScreen (Lottie 애니메이션)
- [ ] ClassSelectionScreen
  - 12 클래스 카드 그리드
  - 클래스 상세 패널 (BottomSheet)
  - 클래스 퀴즈 기능
- [ ] AbilityRollScreen
  - D20 3D 애니메이션 (Lottie)
  - 4d6 drop lowest 알고리즘
  - 클래스별 능력치 보너스 표시
  - 재굴림 1회 기능
- [ ] CharacterNamingScreen
  - 클래스별 랜덤 이름 생성
  - 아바타 선택 (12개 기본 아바타)
- [ ] GTDTutorialScreen
  - 5페이지 스와이프 튜토리얼
  - GTD ↔ RPG 개념 매핑 시각화
- [ ] OnboardingViewModel
- [ ] 온보딩 완료 후 DataStore 저장

### Week 5: 캐릭터 시스템

- [ ] CharacterSheetScreen 완성
  - 5개 탭 (스탯/장비/특수능력/업적/NPC)
  - 캐릭터 초상화 + 기본 정보
  - 능력치 6개 표시 (수정치 포함)
  - HP 바 + XP 바 애니메이션
  - 생활 영역별 전투력 표시
- [ ] CharacterEntity + CharacterDao 완성
- [ ] 능력치 수정치 계산 유틸 (abilityModifier)
- [ ] 최대 HP 계산 (클래스별 Hit Die + CON)
- [ ] 숙련 보너스 계산 (레벨 기반)
- [ ] XP 시스템
  - XP 바 애니메이션 (획득 시)
  - 레벨업 임계값 상수 정의
  - GainXPUseCase
  - CheckLevelUpUseCase
- [ ] LevelUpScreen 기본형 (애니메이션, 스탯 변화 표시)
- [ ] 클래스 특수 능력 데이터 정의 (Enum + 설명)

### 완료 기준
- 온보딩 완료 후 캐릭터 생성
- 캐릭터 시트에서 모든 정보 확인
- XP 획득 → 레벨업 시퀀스 작동

---

## Phase 3: 전투 시스템 (2주) ← 핵심 게임 루프

### Week 6: D20 전투 엔진

- [ ] ResolveCombatUseCase (핵심)
  - D20 SecureRandom 굴림
  - 공격 굴림 계산 (능력치 + 숙련 + 장비)
  - 몬스터 AC 계산 (CR 기반)
  - 명중/미스/크리티컬 판정
  - 결과 타입별 반환 (sealed class)
- [ ] XP 계산 공식 완성
  - 기본 XP (CR × 25)
  - 보너스 계수들 (크리티컬, 마감, 스트릭, 클래스, 장비)
- [ ] HP 손실 계산 (미스 시)
- [ ] CompleteTaskUseCase (전체 통합, **원자적 트랜잭션** — 08_tech_stack.md 8.5절 설계 준수)
  - CompletionDao `@Transaction` 구현
  - 상태 전이 가드 (`WHERE status='ACTIVE'`)
  - CombatLog 멱등성 (`OnConflictStrategy.IGNORE`)
  - 캐릭터 단일 UPDATE (XP/HP/Streak 한 번에)
  - 단위 테스트: 더블탭 시 AlreadyCompleted 반환 확인
- [ ] D20RollSheet UI
  - Lottie 주사위 3D 회전 애니메이션 (500ms)
  - 결과 숫자 대형 표시
  - 계산 과정 단계별 표시
  - 명중/미스 판정 표시
  - XP 획득 명세 표시
- [ ] 크리티컬 히트 특별 UI (황금 파티클)
- [ ] 크리티컬 미스 특별 UI (유머러스한 메시지)

### Week 7: 전투 결과 + HP 관리 + 프라이버시 기반

> **중요**: AI 연동(Phase 5 Week 10)보다 **먼저** 프라이버시 동의 UI와 차단 로직을 완성해야 한다.

#### 프라이버시 기반 (AI 연동 전 필수 완료)
- [ ] `AppSettings.aiConsentGiven` DataStore 필드 추가
- [ ] `ConsentDialog` UI 구현 — 전송 데이터·제외 데이터 목록 명시 (07_claude_api.md 7.0절)
- [ ] `PromptSanitizer` 구현 — 제목 50자 제한, 메모·첨부·NPC 이름 제외
- [ ] `ClaudeRepository.canCallApi()` 동의+활성화 이중 확인
- [ ] 단위 테스트: 동의 없으면 API 호출 0회, 폴백 반환 확인

#### 전투 결과 화면
- [ ] CombatResultScreen
  - 승리 Lottie 애니메이션
  - 전투 결과 명세 (D20, 공격, AC)
  - XP 바 증가 애니메이션
  - AI 내러티브 텍스트 (임시: 로컬 폴백)
  - 레벨업 트리거 연결
- [ ] CombatScreen (전투 현황)
  - 현재 "전투 중" 퀘스트 카드
  - 최근 전투 기록 목록
  - 연속 승리 스트릭 표시
- [ ] HP 시스템 완성
  - HPBar 컴포넌트 (색상 변화: 초록→노랑→빨강)
  - HP 상태별 UI 변화 (HEALTHY/TIRED/WOUNDED/CRITICAL)
  - 자정 HP 리셋 WorkManager
- [ ] 스트릭 시스템
  - 스트릭 카운터 증가/리셋 로직
  - 스트릭 마일스톤 보상
  - 스트릭 보호 토큰 시스템
- [ ] CombatLog 저장 + 조회
- [ ] LevelUpScreen 완성 (능력치 선택 UI)

### 완료 기준
- 퀘스트 완료 체크 → D20 애니메이션 → 결과 → XP 획득 전체 3초 이내 완결
- 크리티컬 히트/미스 특수 처리
- 레벨업 시퀀스 완성
- 스트릭 카운터 정상 작동

---

## Phase 4: 아이템 + NPC + 클래스 특수 능력 (2주)

### Week 8: 아이템 시스템

- [ ] 아이템 카탈로그 데이터 정의 (30+ 아이템)
- [ ] ItemEntity + CharacterItemEntity + CharacterItemDao (05_data_model.md 5.2절 스펙 준수)
- [ ] ItemRepository
- [ ] 크리티컬 히트 아이템 드롭 로직
  - 등급별 드롭률 구현
  - CR 기반 드롭 등급 결정
- [ ] 랜덤 아이템 선택 알고리즘
- [ ] ItemDetailScreen (아이템 정보 + 효과 설명)
- [ ] 장비 슬롯 UI (CharacterSheet 장비 탭)
  - 슬롯별 아이템 표시
  - 착용/해제 인터랙션
- [ ] 장비 효과 적용 로직
  - XP 배율 아이템
  - 공격 보너스 아이템
  - HP 보너스 아이템
  - 특수 효과 아이템 (코드별 처리)
- [ ] D20RollSheet에 아이템 드롭 표시

### Week 9: NPC + 클래스 특수 능력 + 랜덤 인카운터

#### NPC 시스템
- [ ] NpcEntity + NpcDao + NpcRepository
- [ ] NpcScreen UI
  - NPC 목록 + 프로필
  - 추정 클래스 선택
  - 위임 퀘스트 목록
  - 마지막 연락 일시
- [ ] ClassCompatibilitySheet
  - 12x12 호환성 매트릭스 데이터
  - 내 클래스 ↔ NPC 클래스 시각화
  - 궁합 설명 텍스트
- [ ] 위임 기능 (Task → NPC 배정 → WaitingFor 목록)

#### 클래스 특수 능력 (최우선 3개/클래스)
- [ ] ClassAbilityEngine 구현
- [ ] 각 클래스 Lv1 능력 구현 (12개):
  - BARBARIAN: Rage
  - BARD: Bardic Inspiration
  - CLERIC: Channel Divinity
  - DRUID: Wild Shape
  - FIGHTER: Action Surge
  - MONK: Stunning Strike (집중 모드)
  - PALADIN: Divine Smite
  - RANGER: Favored Enemy
  - ROGUE: Sneak Attack + Cunning Action
  - SORCERER: Metamagic
  - WARLOCK: Patron Pact
  - WIZARD: Spellbook (템플릿 저장)
- [ ] CharacterSheet 특수능력 탭 UI

#### 랜덤 인카운터
- [ ] EncounterLogEntity + EncounterLogDao (05_data_model.md 5.2절 스펙 준수)
  - `PENDING → CLAIMED` 조건부 업데이트 (중복 보상 방지)
  - 48시간 만료 처리 WorkManager 태스크
- [ ] EncounterLogScreen
- [ ] RandomEncounterWorker 구현
- [ ] 로컬 인카운터 템플릿 (5가지 유형 × 10개 = 50개)

### 완료 기준
- 아이템 드롭, 장착, 효과 적용 작동
- NPC 등록, 위임, 궁합 확인 작동
- 클래스 특수 능력 발동 확인
- 랜덤 인카운터 알림 수신

---

## Phase 5: Claude API + 통계 + 폴리싱 (3주)

### Week 10: Claude API 연동

> **전제 조건**: Week 7의 프라이버시 기반 작업(ConsentDialog, PromptSanitizer, 차단 테스트)이 완료된 상태에서만 시작한다.

- [ ] Retrofit + OkHttp 설정
- [ ] ClaudeApiService 구현
- [ ] ClaudeRepository 구현
  - `canCallApi()` (동의+활성화 이중 확인) 검증 후 호출
  - PromptSanitizer 적용 확인 (모든 7가지 시나리오)
  - 7가지 시나리오 모두 구현
  - 오프라인 폴백 로직
  - 오류 처리
- [ ] EncryptedSharedPreferences API Key 저장
- [ ] AI 설정 화면 (SettingsScreen 내)
  - API Key 입력/저장
  - 기능별 토글
  - 일일 사용량 표시
  - ConsentDialog 재표시 버튼 (동의 철회 포함)
- [ ] 전투 내러티브 연동 (CombatResultScreen)
- [ ] Weekly Review 요약 연동
- [ ] Clarify AI 제안 연동
- [ ] 레벨업 메시지 연동
- [ ] 랜덤 인카운터 AI 생성 연동

### Week 11: 통계 + 알림 시스템

#### 통계 화면
- [ ] StatisticsScreen 완성
  - 주간/월간 완료 퀘스트 BarChart (vico)
  - XP 획득 추이 LineChart
  - 생활 영역 분포 PieChart (커스텀 또는 vico)
  - 스트릭 캘린더 (GitHub 잔디 스타일)
  - 클래스 능력치 레이더 차트
  - D20 분포도 (크리티컬/미스/명중 비율)
- [ ] WeeklyReviewScreen 통계 강화

#### 알림 시스템
- [ ] 알림 채널 설정 (Android 8.0+)
- [ ] DailyReminderWorker (매일 08:00)
  - 오늘 마감 퀘스트 3개 표시
  - 알림 탭 시 앱 QuestBoard로 딥링크
- [ ] WeeklyReviewReminderWorker (토요일 10:00)
- [ ] 마감 D-1 알림 (해당 마감일 전날 09:00)
- [ ] HP 위기 알림 (HP 25% 이하 + 완료 퀘스트 없을 때)
- [ ] 스트릭 위기 알림 (저녁 21:00, 당일 미완료 시)
- [ ] NotificationSettingsScreen

#### 업적 시스템
- [ ] AchievementEntity + DAO
- [ ] 30개 업적 정의
  - 인박스 제로 달성
  - 연속 7일/30일/100일
  - 크리티컬 히트 10회/50회/100회
  - 레벨 5/10/15/20 달성
  - 모든 클래스 경험 (멀티클래스, v2)
  - Weekly Review 4주 연속
  - 특정 생활 영역 50/100퀘스트
  - 등등
- [ ] 업적 달성 시 알림 + 팝업 UI
- [ ] AchievementScreen (업적 목록)

### Week 12: 폴리싱 + QA

#### 애니메이션 완성
- [ ] 화면 전환 Enter/Exit 트랜지션 (Compose Animation)
- [ ] QuestCard 완료 시 애니메이션 (사라지는 효과)
- [ ] HP/XP 바 애니메이션 (증가/감소)
- [ ] 모든 Lottie 애니메이션 파일 확인 및 교체
- [ ] 크리티컬 히트 파티클 이펙트 완성
- [ ] 레벨업 시퀀스 완성

#### UX 개선
- [ ] 모든 로딩 상태 처리 (Shimmer 또는 Skeleton)
- [ ] 모든 에러 상태 처리 (EmptyState, ErrorState)
- [ ] 네트워크 오류 처리 (Snackbar)
- [ ] 폼 입력 유효성 검사
- [ ] Haptic 피드백 (진동)

#### 접근성
- [ ] TalkBack 지원 (contentDescription)
- [ ] 폰트 크기 대응 (sp 단위 사용 확인)
- [ ] 색상 대비 확인 (WCAG 2.1 AA)

#### 퍼포먼스
- [ ] LazyList 최적화 (key 설정)
- [ ] 불필요한 리컴포지션 제거
- [ ] 이미지 캐싱 (Coil)
- [ ] 앱 시작 시간 측정 및 최적화

#### 테스트
- [ ] UseCase 단위 테스트 (CompleteTaskUseCase 중점)
- [ ] ViewModel 단위 테스트
- [ ] Room DB 통합 테스트
- [ ] 주요 플로우 UI 테스트

### 완료 기준
- 모든 시나리오에서 크래시 없음
- 퀘스트 완료 → 결과까지 3초 이내 (D20 애니메이션 포함)
- Claude API 정상 작동 (설정 후)
- 통계 차트 정상 표시

---

## Phase 6: 베타 + 플레이스토어 출시 (2주)

### Week 13: 내부 베타 테스트

#### 플레이스토어 준비
- [ ] 앱 아이콘 디자인 (512×512 PNG)
- [ ] Feature Graphic (1024×500)
- [ ] 스크린샷 (Phone: 5개, Tablet: 선택)
- [ ] 앱 이름: "QuestLog - GTD×D&D 퀘스트 관리"
- [ ] 짧은 설명 (80자)
- [ ] 긴 설명 (4000자, 한국어/영어)
- [ ] 개인정보처리방침 작성 + 호스팅
- [ ] 콘텐츠 등급 신청 (Google Play 설문)

#### 내부 테스트 (20명)
- [ ] Internal Test Track 업로드
- [ ] 테스터 초대 (Gmail 계정)
- [ ] 피드백 수집 (Google Form)
- [ ] Firebase Crashlytics 연동 (크래시 리포팅)

#### 중요 버그 수정
- [ ] 테스터 피드백 기반 버그 수정
- [ ] 크래시 리포트 분석 + 수정
- [ ] UX 개선 (즉각적인 것만)

### Week 14: 출시

- [ ] Production APK 빌드 (proguard 적용)
- [ ] 앱 서명 키 관리 (Google Play App Signing 권장)
- [ ] Play Console Production Track 제출
- [ ] 심사 통과 확인 (보통 1-3일)
- [ ] 출시 발표 (선택: SNS, 커뮤니티)

#### v1.1 계획 (출시 후 2주)
- [ ] 베타 테스터 피드백 기반 개선
- [ ] 성능 이슈 수정
- [ ] 사용성 개선 (데이터 기반)
- [ ] 누락된 기능 소규모 추가

---

## 향후 버전 계획

### v1.5 (출시 후 2개월)
- Freemium 모델 도입 (결제 시스템)
- 클라우드 백업 (Firebase Firestore)
- 홈화면 위젯 (Glance API)
- 다크/라이트 테마 전환
- 잔여 클래스 특수 능력 (Lv2-Lv10)
- 커스텀 몬스터 이름 입력
- Google Calendar 연동 (단방향 읽기)

### v2.0 (출시 후 6개월)
- 파티 시스템 (길드)
  - 친구 초대 + 공유 캠페인
  - 역할 분담 (클래스 협력)
  - 파티 XP 공유
- Wear OS 컴패니언
- 멀티클래스 (20레벨 이후)
- Apple Watch 지원 (iOS 버전 검토)

### v3.0 (출시 후 1년)
- AI DM 모드 (Claude AI가 맞춤형 퀘스트 생성)
- 세계관 확장 (새 지역/몬스터 팩)
- 커뮤니티 퀘스트 공유
- 전설 장비 크래프팅

---

## 우선순위 결정 기준

```
P0 (출시 전 필수):
  - 전체 GTD 5단계 플로우
  - D20 전투 시스템
  - 캐릭터 레벨/XP 시스템
  - 클래스 선택 (최소 4개 구현)
  - 기본 알림 (일일 리마인더)

P1 (출시 전 권장):
  - 모든 12개 클래스
  - Claude API 기본 연동
  - 아이템 드롭 시스템
  - 통계 차트
  - 업적 시스템

P2 (v1.5):
  - NPC 위임 고급 기능
  - 클래스 궁합 UI
  - 홈화면 위젯
  - 클라우드 백업

P3 (v2.0+):
  - 파티 시스템
  - Wear OS
  - AI DM 모드
```
