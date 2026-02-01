package com.skala.sktx.repository;

import com.skala.sktx.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Payment 엔티티에 대한 JPA 리포지토리
// 결제 저장/조회 기능 제공

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReservationId(Long reservationId);
}
