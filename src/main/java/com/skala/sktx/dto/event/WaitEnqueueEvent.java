package com.skala.sktx.dto.event;

// 대기열 등록 이벤트 DTO

public record WaitEnqueueEvent(
        Long scheduleId,
        String waitingToken
) {}
