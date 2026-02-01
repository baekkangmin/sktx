package com.skala.sktx.dto.event;

// 좌석 보류 시간 만료 이벤트 DTO

public record HoldExpiredEvent(
        Long reservationId,
        String waitingToken,
        String reason
) {}
