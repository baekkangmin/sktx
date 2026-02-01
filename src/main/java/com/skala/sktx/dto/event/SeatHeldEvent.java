package com.skala.sktx.dto.event;

import java.time.LocalDateTime;

// 좌석 보류 완료 이벤트 DTO

public record SeatHeldEvent(
        Long reservationId,
        Long scheduleId,
        String seatNo,
        String waitingToken,
        LocalDateTime expiresAt
) {}
