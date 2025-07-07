package laoride.lao_ride.admin.dto;

import laoride.lao_ride.reservation.domain.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder // 빌더 패턴으로 객체를 생성하도록 변경
public class AdminDashboardDto {

    // KPI(Key Performance Indicatort : 핵심성과지표) 카드 데이터
    private final long todayNewReservations;
    private final long pendingReservations;
    private final BigDecimal todayExpectedSales;
    private final long currentlyRentedCount;

    // 처리 대기 예약 목록 데이터
    private final List<PendingReservation> recentPendingReservations;

    // DTO 내부에 중첩 클래스로 선언하여, 이 DTO에서만 사용할 예약 목록용 객체를 정의
    @Getter
    public static class PendingReservation {
        private final Long id;
        private final String reservationCode;
        private final String customerName;
        private final LocalDate startDate;
        private final LocalDate endDate;

        public PendingReservation(Reservation reservation) {
            this.id = reservation.getId();
            this.reservationCode = reservation.getReservationCode();
            this.customerName = reservation.getCustomerName();
            this.startDate = reservation.getStartDate();
            this.endDate = reservation.getEndDate();
        }
    }

    // 정적 팩토리 메서드: Reservation 엔티티 리스트를 DTO 리스트로 변환
    public static List<PendingReservation> from(List<Reservation> reservations) {
        return reservations.stream()
                .map(PendingReservation::new)
                .collect(Collectors.toList());
    }

}
