package com.skala.sktx.dto.api.reservation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 좌석 임시 보류 요청 DTO

public record SeatHoldRequest(
        @NotNull(message = "scheduleId는 필수입니다.")
        Long scheduleId,

        @NotBlank(message = "seatNo는 필수입니다.")
        String seatNo,

        @NotBlank(message = "waitingToken은 필수입니다.")
        String waitingToken
) {}
