package com.skala.sktx.consumer;

import com.skala.sktx.config.KafkaConfig;
import com.skala.sktx.dto.event.PaymentConfirmedEvent;
import com.skala.sktx.entity.Reservation;
import com.skala.sktx.repository.ReservationRepository;
import com.skala.sktx.service.EventPublisher;
import com.skala.sktx.service.SseService;
import java.util.Map;
import org.springframework.stereotype.Component;

// 결제 승인 완료 이벤트를 처리하는 Kafka Consumer
// PaymentController가 결제 성공 후 호출 → SSE payment 푸시 + Kafka payment.confirmed 발행
// 역할: SSE 전송 + Kafka 이벤트 발행

@Component
public class PaymentConsumer {

    private final ReservationRepository reservationRepository;
    private final EventPublisher publisher;
    private final SseService sseService;

    public PaymentConsumer(
            ReservationRepository reservationRepository,
            EventPublisher publisher,
            SseService sseService
    ) {
        this.reservationRepository = reservationRepository;
        this.publisher = publisher;
        this.sseService = sseService;
    }

    public void onPaymentConfirmed(Long reservationId, Long ticketId) {
        Reservation r = reservationRepository.findById(reservationId).orElse(null);
        if (r == null) return;

        // SSE
        sseService.send(r.getWaitingToken(), "payment", Map.of(
                "reservationId", reservationId,
                "ticketId", ticketId,
                "status", "CONFIRMED"
        ));

        // Kafka 이벤트
        publisher.publish(
                KafkaConfig.TOPIC_PAYMENT_CONFIRMED,
                reservationId.toString(),
                new PaymentConfirmedEvent(reservationId, ticketId)
        );
    }
}
