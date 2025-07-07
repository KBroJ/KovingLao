package laoride.lao_ride.admin.dto;

import laoride.lao_ride.reservation.domain.Reservation;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
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

    // 오늘의 스케줄(오늘 하루 동안 처리해야 할 오프라인 업무를 시간순으로 정리한) 목록 데이터
    private final List<TodayScheduleItem> todaySchedule;

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

    // 오늘의 스케줄 항목을 위한 DTO
    @Getter
    public static class TodayScheduleItem {
        private final String type; // "출고" 또는 "반납"
        private final LocalTime time; // 출고 또는 반납 시간
        private final String customerName;
        private final String productName;
        private final Long reservationId;

        public TodayScheduleItem(Reservation reservation, String type) {
            this.type = type;
            this.time = "출고".equals(type) ? reservation.getPickupTime() : reservation.getReturnTime();
            this.customerName = reservation.getCustomerName();
            this.productName = reservation.getProduct().getName();
            this.reservationId = reservation.getId();
        }
    }

    // 정적 팩토리 메서드: Reservation 엔티티 리스트를 DTO 리스트로 변환
    public static List<PendingReservation> from(List<Reservation> reservations) {
        return reservations.stream()            // 1. Reservation 리스트를 스트림(stream)으로 변환 (물 흐르듯이 처리하기 위해)
                .map(PendingReservation::new)   // 2. 리스트의 각 Reservation 객체를 PendingReservation 객체로 변환
                .collect(Collectors.toList());  // 3. 변환된 PendingReservation 객체들을 다시 새로운 List로 수집
    }

}
