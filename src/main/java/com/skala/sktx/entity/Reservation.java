package com.skala.sktx.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

// KTX 예약(임시 좌석 확보) 엔티티
// 예약 상태: HELD(임시 확보), PAID(결제 완료), EXPIRED(만료)

@Getter
@NoArgsConstructor
@Entity
@Table(name = "reservation")
public class Reservation {

    public enum Status { HELD, PAID, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long scheduleId;

    @Column(nullable = false)
    private String seatNo;

    @Column(nullable = false)
    private String waitingToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Reservation newHold(Long scheduleId, String seatNo, String waitingToken, LocalDateTime expiresAt) {
        Reservation r = new Reservation();
        r.scheduleId = scheduleId;
        r.seatNo = seatNo;
        r.waitingToken = waitingToken;
        r.status = Status.HELD;
        r.expiresAt = expiresAt;
        r.createdAt = LocalDateTime.now();
        return r;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public void markPaid() { this.status = Status.PAID; }
    public void markExpired() { this.status = Status.EXPIRED; }
}
