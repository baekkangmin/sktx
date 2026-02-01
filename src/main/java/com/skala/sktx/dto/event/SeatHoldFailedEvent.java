package com.skala.sktx.dto.event;

// 좌석 보류 실패 이벤트 DTO

public record SeatHoldFailedEvent(
        Long scheduleId,
        String seatNo,
        String waitingToken,
        String reason
) {}
