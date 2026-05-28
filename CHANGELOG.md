# Changelog

모든 주요 변경사항을 이 파일에 기록합니다.
형식은 [Keep a Changelog](https://keepachangelog.com/ko/1.0.0/) 기반,
버전 관리는 [Semantic Versioning](https://semver.org/lang/ko/) 준수.

---

## [Unreleased]

---

## [0.1.0-alpha] - 2026-05-28

### 추가
- **GTD 핵심 플로우**: Inbox 수집 → Clarify 명료화 → Quest Board 배치 → 완료
- **D20 전투 시스템**: 퀘스트 완료 시 자동 주사위 굴림, 크리티컬 히트/미스 처리
- **캐릭터 시스템**: 12클래스, XP/레벨업, 능력치 6종, HP 관리
- **아이템 시스템**: 크리티컬 히트 드롭, 장비 슬롯, XP 배율 효과
- **NPC 시스템**: 연락처 연동, 클래스 호환성 매트릭스, 위임 게이트
- **클래스 특수 능력**: 12클래스 Lv1 버프 시스템
- **랜덤 인카운터**: 50개 템플릿, 12시간 주기 WorkManager, 보상 원자성 처리
- **음성 캡처 (STT)**: RECORD_AUDIO 동의 게이트, SpeechRecognizer 연동
- **Claude AI 연동**: 전투 내러티브, 주간 리뷰 요약, Clarify 제안, 레벨업 메시지
- **통계 화면**: 6종 차트 (Vico BarChart/LineChart, Canvas PieChart/RadarChart/StreakGrid, D20 분포)
- **알림 시스템**: 5채널 (일일 리마인더, 기한 임박, HP 위기, 스트릭 위기, 주간 리뷰)
- **주간 리뷰**: 6단계 GTD 체크리스트, 200 XP 보상, AI 요약
- **프라이버시**: 동의 게이트(ConsentManager), PromptSanitizer, EncryptedSharedPreferences API Key 저장
- **Compose 애니메이션**: 화면 전환 슬라이드/페이드, D20 스핀, 레벨업 축하 연출, LazyColumn animateItem
- **홈 화면 위젯**: InboxWidgetProvider, 탭 시 캡처 시트 오픈

### 기술
- Kotlin 2.1, Compose BOM 2024.12, Hilt 2.53, Room 2.6 (v9 마이그레이션)
- kotlinx.serialization 단독 (Gson 미사용)
- JUnit 5 + MockK 단위 테스트
- WorkManager 8개 워커 (HP 리셋, 인카운터, 알림 5종, 주간 리뷰)
