package org.son.monitor.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;

    public static <T> ApiResponse<T> of(ResponseCode code, T data) {
        return new ApiResponse<>(code.getStatus(), code.getMessage(), data);
    }

    public static <T> ApiResponse<T> of(ResponseCode code) {
        return new ApiResponse<>(code.getStatus(), code.getMessage(), null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(ResponseCode.OK, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(ResponseCode.CREATED, data);
    }

    public static ApiResponse<Void> noContent() {
        return of(ResponseCode.NO_CONTENT);
    }

    public static ApiResponse<Void> error(ResponseCode code, String message) {
        return new ApiResponse<>(code.getStatus(), message, null);
    }
}
