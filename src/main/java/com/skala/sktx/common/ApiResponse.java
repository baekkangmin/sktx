package com.skala.sktx.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final int result;   // 0 success, 1 fail
    private final int code;     // error code
    private final String message;
    private final T body;

    private ApiResponse(int result, int code, String message, T body) {
        this.result = result;
        this.code = code;
        this.message = message;
        this.body = body;
    }

    public static <T> ApiResponse<T> ok(T body) {
        return new ApiResponse<>(0, 0, "success", body);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(1, code, message, null);
    }
}
