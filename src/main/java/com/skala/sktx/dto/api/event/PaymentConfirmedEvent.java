package com.skala.sktx.dto.event;

// 결제 승인 완료 이벤트 DTO

public record PaymentConfirmedEvent(
        Long reservationId,
        Long ticketId
) {}
