package com.skala.sktx.service;

import com.skala.sktx.config.KafkaConfig;
import com.skala.sktx.dto.api.waiting.WaitingEnqueueRequest;
import com.skala.sktx.dto.api.waiting.WaitingEnqueueResponse;
import com.skala.sktx.dto.event.WaitEnqueueEvent;
import org.springframework.stereotype.Service;

// enqueue 요청 처리 서비스
// waitingToken 만들고 Kafka에 wait.enqueue 이벤트 발행

@Service
public class WaitingService {

    private final WaitingTokenService tokenService;
    private final EventPublisher publisher;

    public WaitingService(WaitingTokenService tokenService, EventPublisher publisher) {
        this.tokenService = tokenService;
        this.publisher = publisher;
    }

    public WaitingEnqueueResponse enqueue(WaitingEnqueueRequest req) {
        String token = tokenService.createWaitingToken(req.scheduleId(), req.userKey());
        publisher.publish(
                KafkaConfig.TOPIC_WAIT_ENQUEUE,
                req.scheduleId().toString(),
                new WaitEnqueueEvent(req.scheduleId(), token)
        );
        return new WaitingEnqueueResponse(token);
    }
}
