---
name: pr
description: 코드 검토 후 PR을 생성한다. 중심 이슈 번호를 인수로 받아 아키텍처 검토 → PR 제목/본문 작성 → gh pr create 순으로 실행한다.
argument-hint: <이슈번호>
disable-model-invocation: true
allowed-tools: Bash
---

# pr: #$ARGUMENTS

## 체크리스트

```
## PR 생성 체크리스트

- [ ] 1. `git log` 로 현재 브랜치 커밋 목록 확인
- [ ] 2. `git diff {base}...HEAD` 로 변경된 파일 및 내용 파악
- [ ] 3. 중심 이슈 번호 확인: #$ARGUMENTS

### 코드 검토 (PR 전 게이트)
- [ ] 4. 레이어 책임 확인 (비즈니스 로직이 Service에만 있는가)
- [ ] 5. Entity: `@NoArgsConstructor(access = PROTECTED)`, `extends BaseEntity`
- [ ] 6. Service: 클래스 레벨 `@Transactional(readOnly = true)`, 쓰기 메서드 `@Transactional`
- [ ] 7. Controller: 반환 타입 `ApiResponse<T>`, `@Valid @RequestBody`
- [ ] 8. 예외: `BusinessException(ErrorCode.XXX)` 패턴
- [ ] 9. 로깅: `log.info("event.name key=value")` 구조화 포맷
- [ ] 10. N+1 가능성 (Lazy 로딩 + 루프)

### PR 생성
- [ ] 11. PR 제목 작성
- [ ] 12. PR 본문 작성 (3개 섹션)
- [ ] 13. gh pr create 실행
- [ ] 14. PR URL 반환
```

## 제목 컨벤션
```
[#{이슈번호}] {type}: {내용}

예)
[#32] refactor: 나라, 자격증 enum으로 관리
[#15] feat: 상품 CRUD API 추가
[#41] fix: 게시글 조회 시 N+1 문제 해결
```
- 이슈 제목의 type, 내용을 그대로 가져오고 앞에 `[#번호]` 만 추가

## PR 본문 템플릿

```markdown
closes #$ARGUMENTS

---

## 구현 기능
<!-- 변경된 내용을 bullet로 나열 -->
-

**실행 예시**
<!-- 실제 요청/응답 또는 동작 결과 예시 -->
```
// 요청
POST /api/...
{
  "field": "value"
}

// 응답
{
  "status": 201,
  "message": "Created",
  "data": { ... }
}
```

---

## 참고사항

```

## gh 명령어
```bash
gh pr create --title "[#$ARGUMENTS] {제목}" --body "$(cat <<'EOF'
{본문}
EOF
)"
```