package com.skala.sktx.config;

import com.skala.sktx.entity.SeatInventory;
import com.skala.sktx.entity.TrainSchedule;
import com.skala.sktx.repository.SeatInventoryRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EntityManager entityManager;
    private final SeatInventoryRepository seatInventoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // TrainSchedule 데이터가 이미 있는지 확인
        Long count = entityManager.createQuery(
                "SELECT COUNT(t) FROM TrainSchedule t WHERE t.id = 1", Long.class)
                .getSingleResult();

        if (count > 0) {
            log.info("초기 데이터가 이미 존재합니다. 데이터 초기화를 건너뜁니다.");
            return;
        }

        log.info("초기 데이터를 생성합니다...");

        // TrainSchedule 생성
        TrainSchedule schedule = new TrainSchedule();
        entityManager.createNativeQuery(
                "INSERT INTO train_schedule (id, train_no, depart_station, arrive_station, depart_at, arrive_at) " +
                "VALUES (1, 'KTX001', 'SEOUL', 'BUSAN', :departAt, :arriveAt)")
                .setParameter("departAt", LocalDateTime.now().plusDays(1))
                .setParameter("arriveAt", LocalDateTime.now().plusDays(1).plusHours(2))
                .executeUpdate();

        // SeatInventory 생성 (1번부터 20번까지)
        for (int i = 1; i <= 20; i++) {
            entityManager.createNativeQuery(
                    "INSERT INTO seat_inventory (id, schedule_id, seat_no, status, reservation_id) " +
                    "VALUES (:id, 1, :seatNo, 'AVAILABLE', NULL)")
                    .setParameter("id", (long) i)
                    .setParameter("seatNo", "A" + i)
                    .executeUpdate();
        }

        log.info("초기 데이터 생성 완료!");
    }
}
