package com.skala.sktx.repository;

import com.skala.sktx.entity.Reservation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Reservation 엔티티에 대한 JPA 리포지토리
// 예약(홀드) 정보를 DB에서 조회/저장

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findTopByWaitingTokenOrderByIdDesc(String waitingToken);
}
