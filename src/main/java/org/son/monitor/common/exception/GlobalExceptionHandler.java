package org.son.monitor.common.exception;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.son.monitor.common.response.ApiResponse;
import org.son.monitor.common.response.ResponseCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[BusinessException] errorCode={} message={}", errorCode.name(), e.getMessage());

        // 비즈니스 예외 카운터 (error_code 태그로 세분화)
        Counter.builder("app.errors.business")
                .description("Business exception count")
                .tag("error_code", errorCode.name())
                .tag("http_status", String.valueOf(errorCode.getResponseCode().getHttpStatus().value()))
                .register(meterRegistry)
                .increment();

        return ResponseEntity
                .status(errorCode.getResponseCode().getHttpStatus())
                .body(ApiResponse.error(errorCode.getResponseCode(), errorCode.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("[ValidationException] {}", message);

        Counter.builder("app.errors.validation")
                .description("Validation exception count")
                .register(meterRegistry)
                .increment();

        return ResponseEntity
                .status(ResponseCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        String message = "필수 헤더가 누락되었습니다: " + e.getHeaderName();
        log.warn("[MissingRequestHeaderException] {}", message);
        return ResponseEntity
                .status(ResponseCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        // 정적 리소스 없음 (favicon.ico 등) — 로그/메트릭 생략
        return ResponseEntity
                .status(ResponseCode.NOT_FOUND.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Exception]", e);

        // 5xx 서버 에러 카운터
        Counter.builder("app.errors.server")
                .description("Unhandled server error count")
                .tag("exception", e.getClass().getSimpleName())
                .register(meterRegistry)
                .increment();

        return ResponseEntity
                .status(ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}