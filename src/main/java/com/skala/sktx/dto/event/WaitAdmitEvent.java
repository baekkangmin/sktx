package com.skala.sktx.dto.event;

// 대기열 승인 이벤트 DTO

public record WaitAdmitEvent(
        Long scheduleId,
        String waitingToken
) {}
