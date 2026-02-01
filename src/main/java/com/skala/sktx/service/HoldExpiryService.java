package com.skala.sktx.service;

import com.skala.sktx.dto.event.HoldExpiredEvent;
import com.skala.sktx.entity.Reservation;
import com.skala.sktx.entity.SeatInventory;
import com.skala.sktx.repository.ReservationRepository;
import com.skala.sktx.repository.SeatInventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 좌석 보류 만료 처리 서비스
// Redis 만료 이벤트 수신, 예약 상태 만료 처리, 좌석 반환, SSE 알림

@Service
public class HoldExpiryService {

    private final ReservationRepository reservationRepository;
    private final SeatInventoryRepository seatRepository;
    private final SseService sseService;
    private final WaitingTokenService tokenService;

    public HoldExpiryService(
            ReservationRepository reservationRepository,
            SeatInventoryRepository seatRepository,
            SseService sseService,
            WaitingTokenService tokenService
    ) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
        this.sseService = sseService;
        this.tokenService = tokenService;
    }

    /**
     * Redis 만료 이벤트 수신 메서드 (MessageListenerAdapter가 호출)
     * @param expiredKey Redis에서 만료된 key 문자열
     */
    public void onMessage(String expiredKey) {
        // hold:reservation:{id} 형태만 처리
        if (expiredKey == null || !expiredKey.startsWith("hold:reservation:")) {
            return;
        }
        Long reservationId = parseReservationId(expiredKey);
        if (reservationId == null) return;

        expireReservation(reservationId);
    }

    private Long parseReservationId(String key) {
        try {
            String[] p = key.split(":");
            return Long.valueOf(p[2]);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void expireReservation(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId).orElse(null);
        if (r == null) return;

        // 이미 결제됐거나 이미 만료면 무시
        if (r.getStatus() != Reservation.Status.HELD) return;

        // 좌석 반환
        seatRepository.findByScheduleIdAndSeatNoForUpdate(r.getScheduleId(), r.getSeatNo())
                .ifPresent(seat -> {
                    if (seat.getStatus() == SeatInventory.Status.HELD
                            && reservationId.equals(seat.getReservationId())) {
                        seat.release();
                    }
                });

        r.markExpired();
        tokenService.markExpired(r.getWaitingToken());

        // SSE 알림
        sseService.send(r.getWaitingToken(), "hold_expired",
                new HoldExpiredEvent(r.getId(), r.getWaitingToken(), "PAYMENT_TIMEOUT"));
    }
}
