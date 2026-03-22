# 아키텍처 컨텍스트

> 이 파일은 언제든 교체 가능하다. 변경 시 이 파일만 수정하면 된다.

---

## 패키지 구조

```
org.son.monitor
├── {domain}/
│   ├── presentation/
│   │   ├── {Domain}Controller.java
│   │   └── dto/
│   │       ├── {Domain}CreateRequest.java
│   │       ├── {Domain}UpdateRequest.java
│   │       └── {Domain}Response.java
│   ├── application/
│   │   └── {Domain}Service.java
│   ├── infrastructure/
│   │   └── {Domain}Repository.java
│   └── domain/
│       └── {Domain}.java          ← Entity
└── common/
    ├── aop/LoggingAspect.java
    ├── config/
    ├── entity/BaseEntity.java
    ├── exception/
    │   ├── BusinessException.java
    │   ├── ErrorCode.java
    │   └── GlobalExceptionHandler.java
    ├── filter/
    ├── response/
    │   ├── ApiResponse.java
    │   └── ResponseCode.java
    └── MonitorApplication.java
```

---

## 레이어별 책임 & 규칙

### presentation (Controller + DTO)
- `@RestController`, `@RequestMapping("/api/{domain}s")`
- `@RequiredArgsConstructor`
- 반환 타입은 항상 `ApiResponse<T>`
- userId는 `@RequestHeader("X-User-Id") Long userId` 로 받는다 (현재 JWT 미적용)
- `@Valid @RequestBody` 필수

### application (Service)
- `@Service`, 클래스 레벨 `@Transactional(readOnly = true)`
- 쓰기 메서드에만 `@Transactional` 개별 추가
- MeterRegistry 주입이 필요한 경우 → 생성자 직접 작성 (Lombok 사용 불가)
- MeterRegistry 불필요한 경우 → `@RequiredArgsConstructor`
- 엔티티 조회 헬퍼: `get{Domain}(Long id)` — `orElseThrow(() -> new BusinessException(ErrorCode.XXX))`
- 로깅 포맷: `log.info("event.name key=value key=value")` — 구조화 로그, 문장형 금지

### infrastructure (Repository)
- `JpaRepository<Entity, Long>` 상속
- QueryDSL 커스텀 쿼리 필요 시 별도 `{Domain}RepositoryCustom` 인터페이스 + `Impl` 클래스

### domain (Entity)
- `@Entity`, `@Table(name = "{domain}s")`
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + `@Builder` private 생성자
- `extends BaseEntity` (createdAt, updatedAt 자동 관리)
- ID: `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- 비즈니스 변경 메서드: `public void update(...)` — setter 금지

---

## 공통 패턴

### 응답 래퍼
```java
ApiResponse.ok(data)        // 200
ApiResponse.created(data)   // 201
ApiResponse.noContent()     // 204
ApiResponse.error(code, msg)
```

### 예외 처리
1. `ErrorCode`에 enum 항목 추가 (ResponseCode + 메시지)
2. `throw new BusinessException(ErrorCode.XXX)` 로 사용
3. `GlobalExceptionHandler`가 자동 처리

### Response DTO
- Java record 사용
- static factory: `public static {Domain}Response from({Domain} entity)`

### Request DTO
- Java record 사용
- `@NotBlank`, `@NotNull` 등 validation 어노테이션 필수
- 엔티티 변환: `public {Domain} toEntity(...)` 메서드

---

## 메트릭 (Prometheus)
```java
// 생성자에서 등록
this.xxxCounter = Counter.builder("business.{domain}.{action}.total")
        .description("설명")
        .register(registry);

// 사용
xxxCounter.increment();
```
네이밍: `business.{domain}.{action}.total`

---

## 모니터링 스택
- Prometheus → 메트릭 수집
- Grafana → 시각화
- Loki → 로그 수집 (Logstash JSON 포맷)
- Dozzle → 컨테이너 로그 뷰어
- MDC → traceId 자동 주입 (TSID 기반)