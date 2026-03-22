# Monitor — Claude Code 지시서

## 핵심 행동 규칙 (항상 따를 것)

### 규칙 1: 원자 체크리스트 — 구현 전 필수
모든 구현 요청 시, 코드를 작성하기 **전에** 반드시 아래 형식으로 체크리스트를 먼저 출력한다.

```
## 작업 체크리스트
- [ ] 1. ...
- [ ] 2. ...
- [ ] 3. ...
```

각 항목은 완료 즉시 `[x]`로 표시한다. 이전 항목이 완료되지 않으면 다음으로 넘어가지 않는다.
체크리스트 없이 바로 코드부터 작성하는 것은 금지다.

### 규칙 2: 할루시네이션 방지
- 코드를 수정하기 전 **반드시** 해당 파일을 먼저 읽는다.
- 존재 여부가 불확실한 클래스·메서드·어노테이션은 Grep으로 확인한 후 사용한다.
- 기억이나 추측으로 코드를 작성하지 않는다. 항상 현재 코드베이스를 기준으로 한다.
- 불확실하면 구현 전에 사용자에게 질문한다.

### 규칙 3: 아키텍처 준수
- 모든 코드는 `.claude/architecture.md`의 패턴을 따른다.
- 새 도메인 추가 시 `post` 모듈을 레퍼런스 템플릿으로 사용한다.
- 아키텍처 파일과 충돌하는 코드는 작성하지 않는다.

### 규칙 4: 최소 변경 원칙
- 요청된 것만 변경한다. 요청 범위 밖의 리팩터링·주석 추가·코드 정리는 금지다.
- 기존 코드 스타일과 다를 경우 변경 전에 사용자에게 알린다.

---

## 프로젝트 개요

- **프레임워크**: Spring Boot 3.5.3, Java 21
- **빌드**: Gradle
- **패키지**: `org.son.monitor`
- **아키텍처 상세**: `.claude/architecture.md` 참조

## 기술 스택 요약

| 분류 | 기술 |
|------|------|
| 웹 | Spring Web, Validation, AOP |
| 보안 | Spring Security, JWT (jjwt 0.12.3) |
| DB | JPA + Hibernate, QueryDSL 5.1, MySQL |
| 모니터링 | Actuator, Micrometer Prometheus, Loki (Logstash encoder) |
| 유틸 | Lombok, Swagger (springdoc 2.8.9), TSID |
| 테스트 | JUnit5, H2 (테스트 전용) |

---

## Skills

| 커맨드 | 파일 | 설명 |
|--------|------|------|
| `/new-feature` | `.claude/skills/new-feature/SKILLS.md` | 새 도메인 모듈 추가 |
| `/pr` | `.claude/skills/pr/SKILLS.md` | PR 생성 |
| `/issue` | `.claude/skills/issue/SKILLS.md` | GitHub 이슈 생성 |

---

## 아키텍처 컨텍스트 교체 방법

아키텍처가 변경되면 `.claude/architecture.md`만 수정하면 된다.
CLAUDE.md는 건드리지 않아도 된다.

## Skill 추가 방법

`.claude/skills/{skill-name}/SKILLS.md` 파일을 추가하고 위 Skills 표에 등록한다.
