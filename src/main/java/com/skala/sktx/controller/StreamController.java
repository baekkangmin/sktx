package com.skala.sktx.controller;

import com.skala.sktx.common.ApiResponse;
import com.skala.sktx.service.SseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

// SSE 스트림 연결용 컨트롤러
// 역할: 클라이언트의 /api/stream 요청을 받아 SseEmitter를 반환

@RestController
public class StreamController {

    private final SseService sseService;

    public StreamController(SseService sseService) {
        this.sseService = sseService;
    }

    @GetMapping("/api/stream")
    public SseEmitter stream(@RequestParam String waitingToken) {
        return sseService.connect(waitingToken);
    }
}
