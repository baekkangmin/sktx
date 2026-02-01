package com.skala.sktx.service;

import com.skala.sktx.config.KafkaConfig;
import com.skala.sktx.dto.api.reservation.SeatHoldRequest;
import com.skala.sktx.dto.event.SeatHoldRequestedEvent;
import com.skala.sktx.exception.BusinessException;
import com.skala.sktx.exception.ErrorCode;
import org.springframework.stereotype.Service;

// hold api 요청 처리 서비스
// 토근이 ADMITTED 상태인지 확인하고 Kafka에 seat.hold.requested 이벤트 발행

@Service
public class ReservationService {

    private final WaitingTokenService tokenService;
    private final EventPublisher publisher;

    public ReservationService(WaitingTokenService tokenService, EventPublisher publisher) {
        this.tokenService = tokenService;
        this.publisher = publisher;
    }

    public void requestHold(SeatHoldRequest req) {
        if (tokenService.getStatus(req.waitingToken()) != WaitingTokenService.WaitingStatus.ADMITTED) {
            throw new BusinessException(ErrorCode.NOT_ADMITTED);
        }

        publisher.publish(
                KafkaConfig.TOPIC_SEAT_HOLD_REQUESTED,
                req.scheduleId().toString(),
                new SeatHoldRequestedEvent(req.scheduleId(), req.seatNo(), req.waitingToken())
        );
    }
}
