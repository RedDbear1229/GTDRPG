# CLAUDE.md

QuestLog — GTD × D&D Android 앱 (Kotlin + Compose, 혼자 사용, local-first).
범위·완료기준·Non-Goals는 [PRD.md](PRD.md) 참조. 이 파일은 작업 시 항상 로드되는 핵심 규칙만 담는다.

---

## 자주 쓰는 명령어

```bash
# 빌드 & 설치
./gradlew assembleDebug                    # APK 생성
./gradlew installDebug                     # USB 기기 직접 설치

# 테스트 (CI 게이트 — 모두 통과해야 머지)
./gradlew testDebugUnitTest                # 단위 테스트 (JVM, 빠름)
./gradlew verifyPaparazziDebug             # 스크린샷 회귀 검증
./gradlew recordPaparazziDebug             # 스크린샷 기준 갱신 (UI 변경 시)
./gradlew test --tests "*.CompleteTaskUseCaseTest"   # 특정 테스트

# Room 스키마
./gradlew :app:kspDebugKotlin              # KSP 재생성 (DAO/Entity 변경 후)
# 마이그레이션: app/schemas/ 의 JSON 비교 → MIGRATION_N_M 작성
# Entity 변경 후 마이그레이션 없이 버전만 올리면 데이터 손실!

# 정적 분석
./gradlew lintDebug
./gradlew dependencyUpdates                # 버전 업데이트 확인
```

---

## 디렉토리 구조

```
com.questlog/
├── core/
│   ├── data/{db, datastore, remote, repository, di}/    # Room, Retrofit, Hilt 모듈
│   ├── domain/{model, repository, usecase}/             # 순수 Kotlin (Android import 금지)
│   └── ui/{theme, components}/                          # 공통 Composable
├── feature/                                              # 화면별 (inbox, clarify, questboard, combat, journal, character, npc, settings, onboarding)
│   └── {feature}/{Screen, ViewModel, components}/
└── worker/                                               # WorkManager (HPReset, DailyReminder, etc.)
```

**원칙**: `core/domain`에는 `android.*` import 절대 금지 → 미래 KMP 확장 위해.

---

## 코딩 컨벤션

### 네이밍
- **Entity**: `TaskEntity`, `CharacterItemEntity` (Room) — 항상 `Entity` 접미사
- **Domain Model**: `Task`, `Character` (Entity와 분리, Mapper로 변환)
- **UseCase**: `동사 + 명사 + UseCase` — `CompleteTaskUseCase`, `GainXPUseCase`
  - 단순 위임(1줄 `repo.getById(id)`)이면 UseCase 만들지 말고 ViewModel에서 Repository 직접 호출
- **ViewModel**: `{Feature}ViewModel`, `UiState`는 동일 파일 내 `data class`, `UiEvent`는 `sealed class`
- **Composable**: PascalCase 동사 없음 (`QuestCard`, `HPBar`), 미리보기는 `@Preview {Composable}Preview`
- **테스트**: 한국어 backtick — `` `더블탭 시 AlreadyCompleted 반환`() ``

### 에러 처리
- **도메인 결과는 `sealed class`로 반환** (예외 throw 지양):
  ```kotlin
  sealed class CompleteTaskResult {
      data class Success(...) : CompleteTaskResult()
      data class Failed(...) : CompleteTaskResult()
      data class AlreadyCompleted(val log: CombatLog) : CompleteTaskResult()
      data class Error(val message: String) : CompleteTaskResult()
  }
  ```
- **외부 API 호출**: `runCatching { ... }.getOrElse { fallback }` 패턴 (Claude API는 항상 폴백 보유)
- **DB 트랜잭션**: `@Transaction` 메서드는 throw하지 말고 `Boolean` 반환 (멱등성 신호)
- **Flow 에러**: `.catch { e -> _uiState.update { it.copy(error = e.message) } }`
- **로그**: `Timber.e(throwable, "context")` — `println` / `Log.e` 금지

### 비동기
- `suspend` + `Flow`만 사용 (RxJava 금지)
- ViewModel은 `viewModelScope.launch { }`, Repository는 호출자가 dispatcher 결정 (`withContext(Dispatchers.IO)` Repository 내부에)
- UI 일회성 이벤트는 `SharedFlow`, 지속 상태는 `StateFlow`

---

## 절대 하면 안 되는 것

### 데이터 무결성
- ❌ **퀘스트 완료를 여러 DB 쓰기로 쪼개기** — 반드시 `CompletionDao.commitCompletion()` 단일 `@Transaction` 사용
- ❌ **상태 가드 없이 UPDATE** — `WHERE status='ACTIVE'` 조건 필수, 0 rows면 `AlreadyCompleted` 반환
- ❌ **CombatLog 수정** — 한번 INSERT된 전투 기록은 불변 (`OnConflictStrategy.IGNORE`)
- ❌ **Entity 변경 시 마이그레이션 누락** — `app/schemas/` JSON 차이를 보고 `MIGRATION_N_M` 작성
- ❌ **`MemoryEntryEntity` `entryDate UNIQUE` 우회** — 하루 1엔트리는 DB 레벨 제약. UseCase에서 `LocalDate.now()`를 트랜잭션 시작 시점에 캐시하여 자정 race 차단. 충돌 시 사용자에게 명확한 메시지 표시

### 프라이버시
- ❌ **`canCallApi()` 확인 없이 Claude API 호출** — 동의 + 활성화 이중 확인 필수
- ❌ **`PromptSanitizer` 거치지 않고 프롬프트 전송** — 메모/첨부/NPC 이름은 항상 제외
- ❌ **API Key를 평문 SharedPreferences에 저장** — 반드시 `EncryptedSharedPreferences`
- ❌ **Memory 윤색 호출 시 `body` 외 필드 전송** — `taskTitleSnapshot`, `outcomeType`, `entryDate`, `characterId` 모두 제외. Sanitizer가 본문만 통과시키도록 보장

### 기술 스택
- ❌ **Gson 사용** — `kotlinx.serialization` 단독 (`@Serializable` 어노테이션 필수)
- ❌ **JUnit 4 단위 테스트** — JUnit 5 (`@Test`는 `org.junit.jupiter.api.Test`)
- ❌ **Mockito 사용** — MockK만 사용
- ❌ **RxJava 추가** — Coroutines + Flow로 충분
- ❌ **Fragment 추가** — Compose 단독, Activity는 `MainActivity` 하나만
- ❌ **`core/domain`에 Android import** — KMP 확장 차단

### 코딩 스타일
- ❌ **`!!` (non-null assertion)** — `?:` 또는 `requireNotNull(x) { "message" }`
- ❌ **`println` / `Log.d`** — `Timber` 사용
- ❌ **`runBlocking`** — 테스트 외 사용 금지, 프로덕션은 `suspend` 또는 `viewModelScope`
- ❌ **`GlobalScope.launch`** — 항상 구조화된 동시성
- ❌ **단순 위임 UseCase 작성** — 오버엔지니어링, ViewModel → Repository 직접 호출 허용

### 작업 프로세스
- ❌ **테스트 없이 머지** — `testDebugUnitTest` + `verifyPaparazziDebug` 모두 그린
- ❌ **사용자 확인 없이 git push / force push** — local commit까지만, 푸시는 사용자가
- ❌ **destructive 명령 (`git reset --hard`, `rm -rf`)** — 사용자 확인 필수

---

## 컴팩션 시 반드시 보존할 정보

다음은 코드/문서로 복원 불가능한 결정 사항이다. 대화 압축 시에도 유지할 것:

1. **앱 정체성**: QuestLog = GTD × D&D Android 앱, **자동 전투형** TRPG, 혼자 사용, local-first
2. **원자성 계약**: 퀘스트 완료는 `CompletionDao.commitCompletion()` 단일 트랜잭션 + `WHERE status='ACTIVE'` 가드 + `OnConflictStrategy.IGNORE` (8.5절). 이 계약은 PR 리뷰의 최우선 검증 항목
3. **프라이버시 계약**: Claude API 호출 전 `canCallApi()` (동의 + 활성화) + `PromptSanitizer` 필수. 기본값 OFF. 위반 시 사용자 신뢰 즉시 손실 (07_claude_api.md 7.0절)
4. **확장 경계**: Domain 레이어는 순수 Kotlin (Android 무의존) — 미래 KMP iOS 확장의 유일한 통로
5. **스택 확정 사항**: Kotlin 2.1, Compose BOM 2024.12, Hilt 2.53, Room 2.6, kotlinx.serialization 단독(Gson 금지), JUnit 5(JUnit 4 금지), MockK(Mockito 금지), Paparazzi 스크린샷 테스트, GitHub Actions CI
6. **DoD (Definition of Done)**: 단위 테스트 90%+ (UseCase) / 80%+ (ViewModel) / 75%+ (Repository), Paparazzi 핵심 컴포넌트 100%, 실기기 30분 무크래시
7. **Non-Goals**: Play Store 출시·다중 사용자·자체 백엔드·다국어·iOS·전면 솔로 RPG 모드 — v1.0 범위 밖. 사용자가 요청해도 PRD §9 확인 후 재논의
8. **의사결정 이력**: Codex 적대적 리뷰(2026-05)로 원자성/프라이버시/누락 엔티티 3건 수정 완료 — 같은 실수 반복 금지
9. **Memory of the Day 계약** (Phase 6, 2026-05-23): 솔로 RPG 패러다임을 "하루 1엔트리" 형태로만 흡수. `MemoryEntryEntity.entryDate UNIQUE` + 저장 시점 local tz 고정 + 자정 경계 트랜잭션 race 차단. Claude 윤색은 F4.0 동의 + F5 API + PromptSanitizer 통과 후에만(`body`만 전송). 전면 솔로 RPG 모드(Mythic GME, Ironsworn 전체)는 PRD §9.6 제외 항목 — 요청 와도 거부
