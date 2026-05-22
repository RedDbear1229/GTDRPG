# QuestLog - GTD × D&D Android App

> "당신의 할 일이 곧 모험이다"

GTD(Getting Things Done) 방법론과 D&D(Dungeons & Dragons) TRPG 시스템을 결합한 안드로이드 생산성 앱.
할 일을 완료할 때마다 전투가 벌어지고, 캐릭터가 성장하며, 스토리가 전개된다.

## 문서 목차

### 진입점

| 문서 | 설명 |
|------|------|
| **[PRD.md](PRD.md)** | **제품 요구사항 정의 (PRD)** — 범위·완료기준·Non-Goals 단일 진입점 |

### 상세 설계

| 문서 | 설명 |
|------|------|
| [01. 앱 개요 및 철학](docs/01_overview.md) | 프로젝트 비전, 핵심 가치, 타겟 사용자 |
| [02. 화면 설계 (UX/UI)](docs/02_screens.md) | 전체 화면 목록, 네비게이션 맵, 각 화면 상세 명세 |
| [03. GTD 시스템 설계](docs/03_gtd_system.md) | GTD 5단계 플로우, 각 단계별 기능 상세 |
| [04. 게임 메커니즘](docs/04_game_mechanics.md) | 전투 시스템, XP/레벨업, 클래스, 아이템, 몬스터 |
| [05. 데이터 모델](docs/05_data_model.md) | Entity 설계, Room DB 스키마, Repository 인터페이스 |
| [06. GTD ↔ RPG 매핑](docs/06_gtd_rpg_mapping.md) | 두 시스템의 1:1 개념 대응표 |
| [07. Claude API 연동](docs/07_claude_api.md) | AI 내러티브 생성 시나리오, 프롬프트 설계 |
| [08. 기술 스택](docs/08_tech_stack.md) | 아키텍처, 의존성, 패키지 구조 |
| [09. 개발 로드맵](docs/09_roadmap.md) | Phase별 작업 목록, 마일스톤, 우선순위 |
| [10. 디자인 시스템](docs/10_design_system.md) | 색상, 타이포그래피, 컴포넌트 가이드 |

## 빠른 시작

```bash
# 프로젝트 클론 후
cd gtdrpg
./gradlew assembleDebug
```

## 핵심 루프

```
Inbox 수집 → Clarify 명료화 → Quest Board 배치
    ↓ 완료 체크
D20 주사위 굴림 (자동 전투)
    ↓
XP 획득 → 레벨업 → 클래스 특수 능력 해금
    ↓
Claude AI 전투 내러티브 생성
    ↓
다음 퀘스트로
```

## 기반 자료

- 책: *Dungeons & Dragons: How to Be More D&D* (책의 D&D 철학과 GTD 매핑)
- GTD 방법론: David Allen의 *Getting Things Done*
- D&D 5e System Reference Document (SRD)
