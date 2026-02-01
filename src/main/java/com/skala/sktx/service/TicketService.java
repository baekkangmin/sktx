package com.skala.sktx.service;

import com.skala.sktx.entity.Reservation;
import com.skala.sktx.entity.Ticket;
import com.skala.sktx.exception.BusinessException;
import com.skala.sktx.exception.ErrorCode;
import com.skala.sktx.repository.ReservationRepository;
import com.skala.sktx.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 티켓 발급 서비스
// 예약이 PAID 상태인지 확인하고 티켓 발급

@Service
public class TicketService {

    private final ReservationRepository reservationRepository;
    private final TicketRepository ticketRepository;

    public TicketService(ReservationRepository reservationRepository, TicketRepository ticketRepository) {
        this.reservationRepository = reservationRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket issueIfPaid(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (r.getStatus() != Reservation.Status.PAID) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "reservation is not PAID");
        }

        return ticketRepository.findByReservationId(reservationId)
                .orElseGet(() -> ticketRepository.save(Ticket.issued(r.getId(), r.getScheduleId(), r.getSeatNo())));
    }
}
