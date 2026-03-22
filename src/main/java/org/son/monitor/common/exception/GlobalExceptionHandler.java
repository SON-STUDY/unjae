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

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MeterRegistry meterRegistry;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[BusinessException] {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();

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