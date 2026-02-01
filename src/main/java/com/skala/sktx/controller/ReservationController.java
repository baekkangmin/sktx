package com.skala.sktx.controller;

import com.skala.sktx.common.ApiResponse;
import com.skala.sktx.dto.api.reservation.SeatHoldRequest;
import com.skala.sktx.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

// 좌석 홀드 요청 API: POST /api/reservations/hold
// 성공/실패 결과는 비동기이기 때문에 SSE로 받게 됨

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/hold")
    public ApiResponse<Void> hold(@Valid @RequestBody SeatHoldRequest req) {
        reservationService.requestHold(req);
        return ApiResponse.ok(null);
    }
}
