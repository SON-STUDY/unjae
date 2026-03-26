---
name: monitor
description: 로깅과 모니터링을 위한 Observability 스택을 완성한다. AOP 기반 구조화 로깅, Loki로 로그 전송, Prometheus RED 메트릭 수집, Grafana 대시보드 구성을 포함한다.
argument-hint: <구현 내용>
disable-model-invocation: true
allowed-tools: Bash
---
# Observability 구현 프롬프트

아래 내용을 그대로 Claude Code에 붙여넣기.

---

## 프롬프트 시작

---

이 프로젝트는 유저/댓글/게시판 기능을 가진 Spring Boot (Kotlin) 프로젝트야.
현재 Grafana, Prometheus, Loki가 Docker로 구성되어 있어.

아래 목표에 맞게 **Observability 스택을 완성**해줘.
구현 전에 반드시 프로젝트 구조 전체를 파악하고 시작해.

---

## 목표

배포 후 확신을 줄 수 있는 관측 가능성 구현.
구체적으로:
1. AOP 기반 구조화 로깅 (MDC traceId 포함)
2. Loki로 로그 전송
3. Prometheus RED 메트릭 수집
4. Grafana 대시보드 구성

---

## 구현 사항

### 1. 의존성 추가 (build.gradle.kts)

아래 의존성이 없으면 추가해줘.

```
- net.logstash.logback:logstash-logback-encoder (JSON 구조화 로그)
- com.github.loki4j:loki-logback-appender (Loki 전송)
- io.micrometer:micrometer-registry-prometheus (Prometheus 메트릭)
- org.springframework.boot:spring-boot-starter-actuator
- org.springframework.boot:spring-boot-starter-aop
```

---

### 2. MDC 기반 TraceId Filter

`OncePerRequestFilter` 구현.

조건:
- 요청마다 UUID 앞 8자리를 traceId로 생성
- MDC에 아래 필드 주입
    - `traceId`: UUID 앞 8자리
    - `requestUri`: request.requestURI
    - `method`: HTTP method
- SecurityContext에서 인증된 유저가 있으면 `userId`도 MDC에 추가
- 요청 끝나면 반드시 `MDC.clear()`
- Filter 등록 시 `Ordered.HIGHEST_PRECEDENCE`로 설정

---

### 3. AOP 기반 LoggingAspect

조건:
- `@Logging` 커스텀 어노테이션 생성
- `@annotation(Logging)` pointcut 기반
- Around advice로 구현
- 로그 찍을 내용:
    - START: 메서드명, 파라미터 (단, 파라미터 중 민감 필드는 `[REDACTED]` 처리)
    - END: 메서드명, 실행 시간 (ms)
    - FAIL: 메서드명, 예외 클래스, 메시지
- 민감 필드 목록: `password`, `token`, `accessToken`, `refreshToken`
- `@Logging` 어노테이션을 유저/댓글/게시판 Service 클래스에 붙여줘

---

### 4. logback-spring.xml 구성

조건:

**local 프로파일**
- ConsoleAppender
- 패턴: `[%X{traceId}] %d{HH:mm:ss} %-5level %logger{36} - %msg%n`
- 레벨: DEBUG

**prod 프로파일**
- ConsoleAppender: LogstashEncoder로 JSON 출력
- Loki4jAppender: Loki로 전송
    - url: `http://loki:3100/loki/api/v1/push`
    - label: `app=board,level=%level`
    - format: JSON
- 레벨: INFO

JSON 출력 필드에 MDC 필드 포함:
- `traceId`, `userId`, `requestUri`, `method`

---

### 5. application.yml Actuator / Metrics 설정

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, metrics, prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    tags:
      application: board
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

---

### 6. 민감 정보 마스킹

Request body 로깅 시 아래 필드 `[REDACTED]` 처리:
```
password, token, accessToken, refreshToken,
authorization, secret, apiKey
```

JSON 재귀 파싱으로 중첩 구조도 처리.

---

### 7. GlobalExceptionHandler 로깅 보강

기존 GlobalExceptionHandler가 있으면 아래를 확인하고 없으면 추가:
- 예외 발생 시 `logger.error`로 찍되 MDC traceId가 포함되도록
- 4xx는 WARN, 5xx는 ERROR 레벨로 구분

---

## 검증 조건

구현 완료 후 아래를 확인해줘.

1. `GET /actuator/prometheus` 접근 시 메트릭 노출되는지
2. `GET /actuator/health` 응답에 DB 상태 포함되는지
3. 로컬에서 API 호출 시 콘솔에 `[traceId]` 포함된 로그 출력되는지
4. 같은 요청의 로그가 동일한 traceId로 묶이는지

---

## 금지 사항

- XML 설정보다 YAML 선호하되, logback은 xml로
- 기존 코드 구조 변경 최소화
- 테스트 코드는 건드리지 않음
- 불필요한 의존성 추가 금지

---

## 프롬프트 끝
