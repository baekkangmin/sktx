package com.skala.sktx.repository;

import com.skala.sktx.entity.SeatInventory;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 좌석 재고 관련 DB 접근 레포지토리
// 좌석은 동시성 경쟁이 심하므로 PESSIMISTIC_WRITE(비관적 락) 으로 조회

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select s from SeatInventory s
        where s.scheduleId = :scheduleId
          and s.seatNo = :seatNo
        """)
    Optional<SeatInventory> findByScheduleIdAndSeatNoForUpdate(
            @Param("scheduleId") Long scheduleId,
            @Param("seatNo") String seatNo
    );
}
