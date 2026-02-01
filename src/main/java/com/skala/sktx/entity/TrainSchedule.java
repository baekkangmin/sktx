package com.skala.sktx.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

// KTX 열차 시간표(출발/도착/시간) 엔티티

@Getter
@NoArgsConstructor
@Entity
@Table(name = "train_schedule")
public class TrainSchedule {
    @Id
    private Long id;

    @Column(nullable = false)
    private String trainNo;

    @Column(nullable = false)
    private String departStation;

    @Column(nullable = false)
    private String arriveStation;

    @Column(nullable = false)
    private LocalDateTime departAt;

    @Column(nullable = false)
    private LocalDateTime arriveAt;
}
