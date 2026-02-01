package com.skala.sktx.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 결제 테이블 엔티티
// 상태 : REQUESTED(결제 요청), CONFIRMED(결제 완료)

@Getter
@NoArgsConstructor
@Entity
@Table(name = "payment")
public class Payment {

    public enum Status { REQUESTED, CONFIRMED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Payment requested(Long reservationId) {
        Payment p = new Payment();
        p.reservationId = reservationId;
        p.status = Status.REQUESTED;
        p.createdAt = LocalDateTime.now();
        return p;
    }

    public void confirm() { this.status = Status.CONFIRMED; }
}
