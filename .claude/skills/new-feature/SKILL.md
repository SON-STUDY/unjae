---
name: new-feature
description: 새 도메인 모듈(Entity/Repository/Service/Controller/DTO)을 추가한다. 도메인명과 요구사항을 받아 post 모듈을 레퍼런스로 전체 레이어를 생성한다.
argument-hint: <도메인명> <요구사항>
allowed-tools: Read, Grep, Glob, Edit, Write
---

# new-feature: $ARGUMENTS

코드 작성 전 아래 체크리스트를 먼저 출력한다.

```
## 새 도메인 모듈 체크리스트: {도메인명}

### 분석
- [ ] 1. `.claude/architecture.md` 읽기
- [ ] 2. `post` 모듈 전체 파일 읽기 (레퍼런스)
- [ ] 3. `common/exception/ErrorCode.java` 읽기
- [ ] 4. 필요한 파일 목록 확정

### 도메인 레이어
- [ ] 5. `{domain}/domain/{Domain}.java` — Entity 생성

### 인프라 레이어
- [ ] 6. `{domain}/infrastructure/{Domain}Repository.java` — Repository 생성

### 애플리케이션 레이어
- [ ] 7. `ErrorCode.java` — {DOMAIN}_NOT_FOUND 등 에러코드 추가
- [ ] 8. `{domain}/application/{Domain}Service.java` — Service 생성

### 프레젠테이션 레이어
- [ ] 9. `dto/{Domain}CreateRequest.java`
- [ ] 10. `dto/{Domain}UpdateRequest.java`
- [ ] 11. `dto/{Domain}Response.java`
- [ ] 12. `{domain}/presentation/{Domain}Controller.java`

### 검증
- [ ] 13. 모든 import 경로 확인 (`org.son.monitor.{domain}.*`)
- [ ] 14. `ApiResponse<T>` 반환 타입 확인
- [ ] 15. `@Transactional` 어노테이션 확인
- [ ] 16. 로깅 포맷 확인 (`log.info("event.name key=value")`)
```

## 강제 규칙
- `post` 모듈과 동일한 패턴 유지
- 메트릭 Counter 필요 시 생성자 직접 작성 (MeterRegistry 주입)
- Record DTO + static `from()` + `toEntity()` 패턴 유지
- `.claude/architecture.md` 패턴과 충돌하는 코드 작성 금지