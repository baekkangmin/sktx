package com.skala.sktx.consumer;

import com.skala.sktx.config.KafkaConfig;
import com.skala.sktx.dto.event.WaitEnqueueEvent;
import com.skala.sktx.service.SseService;
import com.skala.sktx.service.WaitingTokenService;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// wait.enqueue 이벤트를 소비하는 Kafka Consumer
// 초당 N명만 대기열 승인(ADMITTED) 처리하고 나머지는 WAITING 상태 유지

@Component
public class WaitEnqueueConsumer {

    private final WaitingTokenService tokenService;
    private final SseService sseService;
    private final int perSecond;

    private volatile long currentSecond = System.currentTimeMillis() / 1000;
    private final AtomicLong admittedThisSecond = new AtomicLong(0);

    public WaitEnqueueConsumer(
            WaitingTokenService tokenService,
            SseService sseService,
            @Value("${sktx.admit.per-second}") int perSecond
    ) {
        this.tokenService = tokenService;
        this.sseService = sseService;
        this.perSecond = perSecond;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_WAIT_ENQUEUE, groupId = "sktx-wait")
    public void onEnqueue(WaitEnqueueEvent event) {
        long nowSec = System.currentTimeMillis() / 1000;
        if (nowSec != currentSecond) {
            currentSecond = nowSec;
            admittedThisSecond.set(0);
        }

        // 초당 N명만 admit
        if (admittedThisSecond.incrementAndGet() <= perSecond) {
            tokenService.markAdmitted(event.waitingToken());

            sseService.send(event.waitingToken(), "waiting", Map.of(
                    "waitingToken", event.waitingToken(),
                    "status", "ADMITTED",
                    "scheduleId", event.scheduleId()
            ));
        } else {
            admittedThisSecond.decrementAndGet();
            // WAITING 유지. 필요하면 SSE로 "still waiting"도 보내도 되지만 과제에선 생략.
        }
    }
}
