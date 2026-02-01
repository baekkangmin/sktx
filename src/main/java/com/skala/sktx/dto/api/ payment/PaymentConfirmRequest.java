package com.skala.sktx.dto.api.payment;

import jakarta.validation.constraints.NotNull;

// 결제 승인 요청 DTO

public record PaymentConfirmRequest(
        @NotNull(message = "reservationId는 필수입니다.")
        Long reservationId
) {}
