package com.flow.backend.exception;

public class ExtensionException extends RuntimeException {

    private final ErrorCode errorCode;

    public ExtensionException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
