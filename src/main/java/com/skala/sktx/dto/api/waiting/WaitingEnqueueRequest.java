package com.skala.sktx.dto.api.waiting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 대기열 등록 요청 DTO

public record WaitingEnqueueRequest(
        @NotNull(message = "scheduleId는 필수입니다.")
        Long scheduleId,

        @NotBlank(message = "userKey는 필수입니다.")
        String userKey
) {}
