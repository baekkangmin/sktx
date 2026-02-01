package com.skala.sktx.service;

import com.skala.sktx.exception.BusinessException;
import com.skala.sktx.exception.ErrorCode;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// SSE 연결/이벤트 전송을 관리하는 서비스
// waitingToken을 키로 해서 SseEmitter를 보관/관리함

@Service
public class SseService {

    // waitingToken -> emitter
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final WaitingTokenService tokenService;

    public SseService(WaitingTokenService tokenService) {
        this.tokenService = tokenService;
    }

    public SseEmitter connect(String waitingToken) {
        // token 유효성 확인(없으면 400)
        tokenService.getStatus(waitingToken);

        SseEmitter emitter = new SseEmitter(0L); // timeout 없음(로컬/과제용). 실무면 적절히 설정
        emitters.put(waitingToken, emitter);

        emitter.onCompletion(() -> emitters.remove(waitingToken));
        emitter.onTimeout(() -> emitters.remove(waitingToken));
        emitter.onError(e -> emitters.remove(waitingToken));

        // 연결 즉시 현재 상태 1회 푸시(초기 동기화)
        send(waitingToken, "waiting", Map.of(
                "waitingToken", waitingToken,
                "status", tokenService.getStatus(waitingToken).name(),
                "scheduleId", tokenService.getScheduleId(waitingToken)
        ));

        return emitter;
    }

    public void send(String waitingToken, String eventName, Object data) {
        SseEmitter emitter = emitters.get(waitingToken);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException e) {
            emitters.remove(waitingToken);
        }
    }

    public void close(String waitingToken) {
        SseEmitter emitter = emitters.remove(waitingToken);
        if (emitter != null) emitter.complete();
    }
}
