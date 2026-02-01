package com.skala.sktx.exception;

import lombok.Getter;

// 에러 코드/메시지 표준 정의

@Getter
public enum ErrorCode {
    INVALID_REQUEST(1000, "INVALID_REQUEST"),
    NOT_FOUND(1001, "NOT_FOUND"),
    WAITING_TOKEN_NOT_FOUND(2000, "WAITING_TOKEN_NOT_FOUND"),
    NOT_ADMITTED(2001, "NOT_ADMITTED"),
    SEAT_NOT_AVAILABLE(3000, "SEAT_NOT_AVAILABLE"),
    RESERVATION_EXPIRED(3001, "RESERVATION_EXPIRED"),
    PAYMENT_ALREADY_DONE(4000, "PAYMENT_ALREADY_DONE"),
    SYSTEM_ERROR(9000, "SYSTEM_ERROR");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
