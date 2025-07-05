package laoride.lao_ride.reservation.repository;

import laoride.lao_ride.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 특정 기간과 겹치는 예약을 가진 상품들의 ID 목록을 조회 (중복 제거)
    @Query("SELECT DISTINCT r.product.id FROM Reservation r " +
            "WHERE r.status IN ('PENDING', 'CONFIRMED') " +
            "AND r.endDate >= :startDate AND r.startDate <= :endDate")
    List<Long> findUnavailableProductIds(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT count(r) FROM Reservation r " +
            "WHERE r.product.id = :productId " +
            "AND :date BETWEEN r.startDate AND r.endDate")
    long countByProductIdAndDate(@Param("productId") Long productId, @Param("date") LocalDate date);

    // 예약 코드로 예약을 조회
    Optional<Reservation> findByReservationCode(String reservationCode);

}
