package org.son.monitor.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.son.monitor.common.response.ResponseCode;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // User
    USER_NOT_FOUND(ResponseCode.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(ResponseCode.CONFLICT, "이미 사용 중인 이메일입니다."),

    // Post
    POST_NOT_FOUND(ResponseCode.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    POST_FORBIDDEN(ResponseCode.FORBIDDEN, "게시글에 대한 권한이 없습니다."),

    // Comment
    COMMENT_NOT_FOUND(ResponseCode.NOT_FOUND, "댓글을 찾을 수 없습니다."),

    // Common
    INVALID_INPUT(ResponseCode.BAD_REQUEST, "입력값이 올바르지 않습니다.");

    private final ResponseCode responseCode;
    private final String message;
}