package com.skala.sktx.service;

import com.skala.sktx.exception.BusinessException;
import com.skala.sktx.exception.ErrorCode;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

// Redis에서 waitingToken의 상태/TTL을 관리하는 서비스

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

    private String key(String token) {
        return "wait:token:" + token;
    }

    public String createWaitingToken(Long scheduleId, String userKey) {
        String token = UUID.randomUUID().toString();
        String value = scheduleId + "|" + userKey + "|" + WaitingStatus.WAITING.name();
        redis.opsForValue().set(key(token), value, Duration.ofSeconds(waitingTtlSeconds));
        return token;
    }

    public WaitingStatus getStatus(String token) {
        String v = redis.opsForValue().get(key(token));
        if (v == null) throw new BusinessException(ErrorCode.WAITING_TOKEN_NOT_FOUND);
        return WaitingStatus.valueOf(v.split("\\|")[2]);
    }

    public Long getScheduleId(String token) {
        String v = redis.opsForValue().get(key(token));
        if (v == null) throw new BusinessException(ErrorCode.WAITING_TOKEN_NOT_FOUND);
        return Long.valueOf(v.split("\\|")[0]);
    }

    public void markAdmitted(String token) {
        String v = redis.opsForValue().get(key(token));
        if (v == null) throw new BusinessException(ErrorCode.WAITING_TOKEN_NOT_FOUND);
        String[] p = v.split("\\|");
        String newValue = p[0] + "|" + p[1] + "|" + WaitingStatus.ADMITTED.name();
        redis.opsForValue().set(key(token), newValue, Duration.ofSeconds(admittedTtlSeconds));
    }

    public void markExpired(String token) {
        String v = redis.opsForValue().get(key(token));
        if (v == null) return;
        String[] p = v.split("\\|");
        String newValue = p[0] + "|" + p[1] + "|" + WaitingStatus.EXPIRED.name();
        // 만료 상태도 잠깐만 유지
        redis.opsForValue().set(key(token), newValue, Duration.ofSeconds(60));
    }
}
