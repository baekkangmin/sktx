package com.skala.sktx.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.skala.sktx.config.KafkaConfig;
import com.skala.sktx.dto.event.WaitEnqueueEvent;
import com.skala.sktx.service.WaitingTokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WaitEnqueueConsumer {

    private final WaitingTokenService tokenService;

    public WaitEnqueueConsumer(WaitingTokenService tokenService) {
        this.tokenService = tokenService;
    }

    // wait.enqueue 이벤트를 소비해서 "Redis 대기열"에만 쌓는다.
    // Admit(승인)은 스케줄러가 초당 N개씩 처리한다.
    @KafkaListener(topics = KafkaConfig.TOPIC_WAIT_ENQUEUE, groupId = "sktx-wait")
    public void onEnqueue(WaitEnqueueEvent event) {
        // 로그 목적: kafaka consumer 동작 확인용
        log.info("[KAFKA] consumed wait.enqueue scheduleId={}, token={}", event.scheduleId(), event.waitingToken());
        tokenService.enqueueToQueue(event.scheduleId(), event.waitingToken());
    }
}
