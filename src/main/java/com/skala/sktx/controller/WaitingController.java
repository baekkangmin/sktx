package com.skala.sktx.controller;

import com.skala.sktx.common.ApiResponse;
import com.skala.sktx.dto.api.waiting.WaitingEnqueueRequest;
import com.skala.sktx.dto.api.waiting.WaitingEnqueueResponse;
import com.skala.sktx.service.WaitingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// 대기열 등록 API: POST /api/waiting/enqueue
// 결과로 waitingToken 반환

@RestController
@RequestMapping("/api/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/enqueue")
    public ApiResponse<WaitingEnqueueResponse> enqueue(@Valid @RequestBody WaitingEnqueueRequest req) {
        return ApiResponse.ok(waitingService.enqueue(req));
    }
}
