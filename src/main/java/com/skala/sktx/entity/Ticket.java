package com.skala.sktx.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 티켓 테이블 엔티티
// 발급된 티켓 정보를 저장
// reservationId 기준으로 1장 발급

@Getter
@NoArgsConstructor
@Entity
@Table(name = "ticket")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private String seatNo;

    @Column(nullable = false)
    private LocalDateTime issuedAt;

    public static Ticket issued(Long reservationId, Long scheduleId, String seatNo) {
        Ticket t = new Ticket();
        t.reservationId = reservationId;
        t.scheduleId = scheduleId;
        t.seatNo = seatNo;
        t.issuedAt = LocalDateTime.now();
        return t;
    }
}
