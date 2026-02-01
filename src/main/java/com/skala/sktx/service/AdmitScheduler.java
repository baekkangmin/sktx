package com.skala.sktx.service;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.skala.sktx.config.KafkaConfig;
import com.skala.sktx.dto.event.WaitAdmitEvent;
import com.skala.sktx.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

// AdmitScheduler는 Redis 대기열(queue)에 들어 있는 토큰을 하나씩 꺼내서, 그 토큰의 상태를 ADMITTED로 바꾼다

@Slf4j
@Component
public class AdmitScheduler {

    private final WaitingTokenService tokenService;
    private final SseService sseService;
    private final EventPublisher publisher;
    private final int perSecond;

    public AdmitScheduler(
            WaitingTokenService tokenService,
            SseService sseService,
            EventPublisher publisher,
            @Value("${sktx.admit.per-second}") int perSecond
    ) {
        this.tokenService = tokenService;
        this.sseService = sseService;
        this.publisher = publisher;
        this.perSecond = perSecond;
    }

    /**
     * 1초마다 실행:
     * - 활성 schedule들을 훑고
     * - schedule별로 최대 perSecond 만큼 토큰을 pop 해서 ADMITTED 처리
     */
    @Scheduled(fixedRate = 1000)
    public void admitLoop() {
        Set<String> schedules = tokenService.getActiveSchedules();

        if (schedules == null || schedules.isEmpty()) return;

        for (String s : schedules) {
            long scheduleId;
            try {
                scheduleId = Long.parseLong(s);
            } catch (NumberFormatException e) {
                continue;
            }

            int admitted = 0;

            while (admitted < perSecond) {
                String token = tokenService.popOne(scheduleId);

                if (token == null) break;

                try {
                    tokenService.markAdmitted(token);
                    log.info("[ADMIT] admitted token={}", token); // for debug
                } catch (BusinessException ex) {
                    // 토큰이 이미 만료/삭제된 경우 등: 그냥 스킵
                    continue;
                }

                // Kafka에도 admit 이벤트 남겨서 관측 가능하게
                publisher.publish(
                        KafkaConfig.TOPIC_WAIT_ADMIT,
                        String.valueOf(scheduleId),
                        new WaitAdmitEvent(scheduleId, token)
                );

                // SSE로 승인 상태 즉시 푸시
                sseService.send(token, "waiting", Map.of(
                        "waitingToken", token,
                        "status", "ADMITTED",
                        "scheduleId", scheduleId
                ));

                admitted++;
            }

            tokenService.cleanupScheduleIfEmpty(scheduleId);
        }
    }
}
