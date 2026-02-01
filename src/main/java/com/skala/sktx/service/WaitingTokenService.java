package com.skala.sktx.service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.skala.sktx.exception.BusinessException;
import com.skala.sktx.exception.ErrorCode;

@Service
public class WaitingTokenService {

    public enum WaitingStatus { WAITING, ADMITTED, EXPIRED }

    private final StringRedisTemplate redis;
    private final long waitingTtlSeconds;
    private final long admittedTtlSeconds;

    public WaitingTokenService(
            StringRedisTemplate redis,
            @Value("${sktx.waiting.token-ttl-seconds}") long waitingTtlSeconds,
            @Value("${sktx.waiting.admitted-ttl-seconds}") long admittedTtlSeconds
    ) {
        this.redis = redis;
        this.waitingTtlSeconds = waitingTtlSeconds;
        this.admittedTtlSeconds = admittedTtlSeconds;
    }

    // ====== Key Helpers ======
    private String tokenKey(String token) {
        return "wait:token:" + token;
    }

    private String scheduleSetKey() {
        return "wait:schedules";
    }

    private String queueKey(long scheduleId) {
        return "wait:queue:" + scheduleId;
    }

    // ====== Token ======
    public String createWaitingToken(Long scheduleId, String userKey) {
        String token = UUID.randomUUID().toString();
        String value = scheduleId + "|" + userKey + "|" + WaitingStatus.WAITING.name();
        redis.opsForValue().set(tokenKey(token), value, Duration.ofSeconds(waitingTtlSeconds));
        return token;
    }

    public WaitingStatus getStatus(String token) {
        String v = redis.opsForValue().get(tokenKey(token));
        if (v == null) throw new BusinessException(ErrorCode.WAITING_TOKEN_NOT_FOUND);
        return WaitingStatus.valueOf(v.split("\\|")[2]);
    }

    public Long getScheduleId(String token) {
        String v = redis.opsForValue().get(tokenKey(token));
        if (v == null) throw new BusinessException(ErrorCode.WAITING_TOKEN_NOT_FOUND);
        return Long.valueOf(v.split("\\|")[0]);
    }

    public void markAdmitted(String token) {
        String v = redis.opsForValue().get(tokenKey(token));
        if (v == null) throw new BusinessException(ErrorCode.WAITING_TOKEN_NOT_FOUND);

        String[] p = v.split("\\|");
        String newValue = p[0] + "|" + p[1] + "|" + WaitingStatus.ADMITTED.name();
        redis.opsForValue().set(tokenKey(token), newValue, Duration.ofSeconds(admittedTtlSeconds));
    }

    public void markExpired(String token) {
        String v = redis.opsForValue().get(tokenKey(token));
        if (v == null) return;

        String[] p = v.split("\\|");
        String newValue = p[0] + "|" + p[1] + "|" + WaitingStatus.EXPIRED.name();
        redis.opsForValue().set(tokenKey(token), newValue, Duration.ofSeconds(60));
    }

    // ====== Queue ======
    /** Kafka wait.enqueue 수신 시: 스케줄 활성화 + 대기열에 토큰 적재 */
    public void enqueueToQueue(long scheduleId, String token) {
        redis.opsForSet().add(scheduleSetKey(), String.valueOf(scheduleId));
        redis.opsForList().rightPush(queueKey(scheduleId), token);
    }

    /** 스케줄러가 처리할 “활성 스케줄 목록” */
    public Set<String> getActiveSchedules() {
        return redis.opsForSet().members(scheduleSetKey());
    }

    /**
     * 스케줄별 대기열에서 1개 pop (없으면 null)
     * - 단순 루프 기반 작업 처리용
     */
    public String popOne(long scheduleId) {
        return redis.opsForList().leftPop(queueKey(scheduleId));
    }

    /** 대기열이 비었으면 스케줄 set에서 제거(정리) */
    public void cleanupScheduleIfEmpty(long scheduleId) {
        Long size = redis.opsForList().size(queueKey(scheduleId));
        if (size != null && size == 0) {
            redis.opsForSet().remove(scheduleSetKey(), String.valueOf(scheduleId));
        }
    }
}
