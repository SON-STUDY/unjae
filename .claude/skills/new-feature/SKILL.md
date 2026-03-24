---
name: new-feature
description: 새 도메인 모듈(Entity/Repository/Service/Controller/DTO)을 추가한다. 도메인명과 요구사항을 받아 architecture.md의 레퍼런스 모듈 기준으로 전체 레이어를 생성한다.
argument-hint: <도메인명> <요구사항>
allowed-tools: Read, Grep, Glob, Edit, Write
---

# new-feature: $ARGUMENTS

코드 작성 전 아래 체크리스트를 먼저 출력한다.

```
## 새 도메인 모듈 체크리스트: {도메인명}

### 분석
- [ ] 1. `CLAUDE.md` 읽기 — 베이스 패키지, 레퍼런스 모듈 확인
- [ ] 2. `.claude/architecture.md` 읽기
- [ ] 3. `.claude/context/` 파일 존재 시 전부 읽기 (erd.md, api-spec.md, business-rules.md)
- [ ] 4. 레퍼런스 모듈 전체 파일 읽기
- [ ] 5. `common/exception/ErrorCode.java` 읽기
- [ ] 6. 생성할 파일 목록 확정

### 도메인 레이어
- [ ] 7. `{domain}/domain/{Domain}.java` — Entity 생성

### 인프라 레이어
- [ ] 8. `{domain}/infrastructure/{Domain}Repository.java` — Repository 생성

### 애플리케이션 레이어
- [ ] 9. `ErrorCode.java` — {DOMAIN}_NOT_FOUND 등 에러코드 추가
- [ ] 10. `{domain}/application/{Domain}Service.java` — Service 생성

### 프레젠테이션 레이어
- [ ] 11. `dto/{Domain}CreateRequest.java`
- [ ] 12. `dto/{Domain}UpdateRequest.java`
- [ ] 13. `dto/{Domain}Response.java`
- [ ] 14. `{domain}/presentation/{Domain}Controller.java`

### 검증
- [ ] 15. 모든 import 경로 확인 (`{베이스패키지}.{domain}.*`)
- [ ] 16. `ApiResponse<T>` 반환 타입 확인
- [ ] 17. `@Transactional` 어노테이션 확인
- [ ] 18. 로깅 포맷 확인 (`log.info("event.name key=value")`)
```

## 강제 규칙
- 레퍼런스 모듈(`CLAUDE.md`의 "레퍼런스 모듈" 항목)과 동일한 패턴 유지
- 베이스 패키지는 `CLAUDE.md`의 "베이스 패키지" 항목에서 읽는다. 하드코딩 금지
- Record DTO + static `from()` + `toEntity()` 패턴 유지
- `.claude/architecture.md` 패턴과 충돌하는 코드 작성 금지