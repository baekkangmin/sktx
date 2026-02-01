package com.skala.sktx.exception;

import lombok.Getter;

// 비즈니스 예외를 나타내는 커스텀 런타임 예외 클래스
// 컨트롤러/서비스에서 throw new BusinessException(ErrorCode.NOT_ADMITTED) 이런 식으로 사용

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }
}
