package com.swapcampus.common.exception;

public enum ErrorCode {
    OK(0, "ok"),
    BAD_REQUEST(40000, "bad request"),
    UNAUTHORIZED(40100, "unauthorized"),
    FORBIDDEN(40300, "forbidden"),
    NOT_FOUND(40400, "resource not found"),
    VALIDATION_FAILED(42200, "validation failed"),
    INTERNAL_ERROR(50000, "internal server error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
