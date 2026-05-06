package com.flow.backend.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    DUPLICATE_EXTENSION(HttpStatus.CONFLICT, "이미 존재하는 확장자입니다."),
    INVALID_EXTENSION(HttpStatus.BAD_REQUEST, "확장자는 영문과 숫자만 1자 이상 20자 이하로 입력할 수 있습니다."),
    CUSTOM_EXTENSION_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "커스텀 확장자는 최대 200개까지 등록할 수 있습니다."),
    EXTENSION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 확장자 id입니다."),
    FIXED_EXTENSION_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "고정 확장자는 삭제할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
