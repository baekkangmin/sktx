package com.skala.sktx.dto.event;

// 좌석 보류 요청 이벤트 DTO

public record SeatHoldRequestedEvent(
        Long scheduleId,
        String seatNo,
        String waitingToken
) {}
