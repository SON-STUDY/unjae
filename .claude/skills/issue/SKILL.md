---
name: issue
description: GitHub 이슈를 생성한다. 이슈 내용을 인수로 받아 컨벤션에 맞는 제목과 본문을 작성하고 gh issue create를 실행한다.
argument-hint: <이슈 내용>
disable-model-invocation: true
allowed-tools: Bash
---

# issue: $ARGUMENTS

## 체크리스트

```
- [ ] 1. 이슈 타입 확인 (feat / fix / refactor / chore / docs)
- [ ] 2. 제목 작성
- [ ] 3. 각 섹션 내용 작성 (없는 내용은 섹션만 남기고 비워둠)
- [ ] 4. gh issue create 실행
- [ ] 5. 이슈 URL 반환
```

## 제목 컨벤션
```
{type}: {내용}

예)
feat: 상품 CRUD API 추가
fix: 게시글 조회 시 N+1 문제 해결
refactor: 의존 관계 조정 및 아키텍처 개선
chore: 빌드 설정 정리
```

## 이슈 본문 템플릿

> 구현 내용과 기대 결과는 Claude Code가 이슈를 읽고 바로 구현할 수 있도록
> 파일명, 클래스명, 메서드명, 변경 방향을 구체적으로 작성한다.
> 필요 없는 섹션은 내용을 비우고 제목만 남긴다.

```markdown
## 총 목적


## 구현 내용
<!-- 어떤 파일의 무엇을 어떻게 변경하는지 명시 -->
<!-- 예)
- `PostService`: `UserService` 직접 의존 제거 → `UserRepository`로 교체
- `ErrorCode`: `USER_NOT_FOUND` 추가
-->


## 기대 결과
<!-- 구현 후 달라지는 동작, 구조, 성능 등 -->


## 참고사항

```

## gh 명령어
```bash
gh issue create --title "{제목}" --body "$(cat <<'EOF'
{본문}
EOF
)"
```