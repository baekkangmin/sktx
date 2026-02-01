package com.skala.sktx.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 좌석 재고 테이블 엔티티
// 상태 : AVAILABLE(판매 가능), HELD(임시 보류), SOLD(판매 완료)

@Getter
@NoArgsConstructor
@Entity
@Table(
        name = "seat_inventory",
        uniqueConstraints = @UniqueConstraint(name = "uk_schedule_seat", columnNames = {"schedule_id", "seat_no"})
)
public class SeatInventory {

    public enum Status { AVAILABLE, HELD, SOLD }

    @Id
    private Long id;

    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;

    @Column(name = "seat_no", nullable = false)
    private String seatNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "reservation_id")
    private Long reservationId;

    public void hold(Long reservationId) {
        this.status = Status.HELD;
        this.reservationId = reservationId;
    }

    public void release() {
        this.status = Status.AVAILABLE;
        this.reservationId = null;
    }

    public void sell() {
        this.status = Status.SOLD;
    }
}
