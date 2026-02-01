package com.skala.sktx.repository;

import com.skala.sktx.entity.Ticket;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Ticket 엔티티에 대한 JPA 리포지토리
// reservationId로 티켓 조회/발급 메서드 제공

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByReservationId(Long reservationId);
}
