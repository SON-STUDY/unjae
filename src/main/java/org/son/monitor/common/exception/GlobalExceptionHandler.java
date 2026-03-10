package org.son.monitor.common.exception;

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
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("[BusinessException] {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
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
        return ResponseEntity
                .status(ResponseCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.BAD_REQUEST, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);
        return ResponseEntity
                .status(ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, ResponseCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}