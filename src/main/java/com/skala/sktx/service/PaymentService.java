package com.skala.sktx.service;

import com.skala.sktx.entity.Payment;
import com.skala.sktx.entity.Reservation;
import com.skala.sktx.exception.BusinessException;
import com.skala.sktx.exception.ErrorCode;
import com.skala.sktx.repository.PaymentRepository;
import com.skala.sktx.repository.ReservationRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 결제 확정 처리
// 예약이 유효한지 확인하고 결제 상태를 PAID로 변경

@Service
public class PaymentService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(ReservationRepository reservationRepository, PaymentRepository paymentRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void confirmPayment(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (r.getStatus() == Reservation.Status.PAID) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_DONE);
        }
        if (r.isExpired(LocalDateTime.now()) || r.getStatus() == Reservation.Status.EXPIRED) {
            r.markExpired();
            throw new BusinessException(ErrorCode.RESERVATION_EXPIRED);
        }

        Payment p = paymentRepository.findByReservationId(reservationId)
                .orElseGet(() -> paymentRepository.save(Payment.requested(reservationId)));

        p.confirm();
        r.markPaid();
    }
}
