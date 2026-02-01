package com.skala.sktx.consumer;

import com.skala.sktx.config.KafkaConfig;
import com.skala.sktx.dto.event.SeatHeldEvent;
import com.skala.sktx.dto.event.SeatHoldFailedEvent;
import com.skala.sktx.dto.event.SeatHoldRequestedEvent;
import com.skala.sktx.entity.Reservation;
import com.skala.sktx.entity.SeatInventory;
import com.skala.sktx.repository.ReservationRepository;
import com.skala.sktx.repository.SeatInventoryRepository;
import com.skala.sktx.service.SseService;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 좌석 보류 요청을 처리하는 Kafka Consumer
// DB에서 좌석 락 걸고 AVAILABLE이면 HELD로 변경 + Reservation 생성
// Redis에 TTL 키 생성 → 만료 시 자동 해제
// SSE로 hold 이벤트(성공/실패) 푸시

@Component
public class SeatHoldConsumer {

    private final SeatInventoryRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final StringRedisTemplate redis;
    private final SseService sseService;
    private final long holdSeconds;

    public SeatHoldConsumer(
            SeatInventoryRepository seatRepository,
            ReservationRepository reservationRepository,
            StringRedisTemplate redis,
            SseService sseService,
            @Value("${sktx.reservation.hold-seconds}") long holdSeconds
    ) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.redis = redis;
        this.sseService = sseService;
        this.holdSeconds = holdSeconds;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_SEAT_HOLD_REQUESTED, groupId = "sktx-hold")
    @Transactional
    public void onHoldRequested(SeatHoldRequestedEvent event) {
        SeatInventory seat = seatRepository
                .findByScheduleIdAndSeatNoForUpdate(event.scheduleId(), event.seatNo())
                .orElse(null);

        if (seat == null) {
            sseService.send(event.waitingToken(), "hold",
                    new SeatHoldFailedEvent(event.scheduleId(), event.seatNo(), event.waitingToken(), "SEAT_NOT_FOUND"));
            return;
        }

        if (seat.getStatus() != SeatInventory.Status.AVAILABLE) {
            sseService.send(event.waitingToken(), "hold",
                    new SeatHoldFailedEvent(event.scheduleId(), event.seatNo(), event.waitingToken(), "SEAT_NOT_AVAILABLE"));
            return;
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(holdSeconds);
        Reservation r = reservationRepository.save(
                Reservation.newHold(event.scheduleId(), event.seatNo(), event.waitingToken(), expiresAt)
        );

        seat.hold(r.getId());

        // Redis TTL 키 생성 → 만료 이벤트로 자동 해제
        String holdKey = "hold:reservation:" + r.getId();
        redis.opsForValue().set(holdKey, "1", Duration.ofSeconds(holdSeconds));

        sseService.send(event.waitingToken(), "hold",
                new SeatHeldEvent(r.getId(), event.scheduleId(), event.seatNo(), event.waitingToken(), expiresAt));
    }
}
