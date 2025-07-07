package laoride.lao_ride.admin.service;

import laoride.lao_ride.admin.dto.AdminDashboardDto;
import laoride.lao_ride.member.repository.MemberRepository;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public AdminDashboardDto getDashboardData() {
        LocalDate today = LocalDate.now();

        // 1. 오늘 신규 예약 건수 조회
        long todayNewReservations = reservationRepository.countByCreatedAtAfter(today.atStartOfDay());

        // 2. '확인 대기중' 상태인 예약 건수 조회
        long pendingReservations = reservationRepository.countByStatus("PENDING");

        // 3. 금일 예상 매출 조회 (오늘 시작하는 예약들의 총합)
        BigDecimal todayExpectedSales = reservationRepository.sumTotalPriceBetweenDates(today, today)
                .orElse(BigDecimal.ZERO);

        // 4. 현재 대여 중인 상품 수 조회
        long currentlyRentedCount = reservationRepository.countActiveRentals(today);

        // 5. 처리 대기 예약 목록 조회 (최신 5건)
        List<Reservation> recentPendingEntities = reservationRepository.findTop5ByStatusOrderByIdDesc("PENDING");
        List<AdminDashboardDto.PendingReservation> recentPendingDtos = AdminDashboardDto.from(recentPendingEntities);

        // 6. Builder를 사용해 최종 DTO 생성 및 반환
        return AdminDashboardDto.builder()
                .todayNewReservations(todayNewReservations)
                .pendingReservations(pendingReservations)
                .todayExpectedSales(todayExpectedSales)
                .currentlyRentedCount(currentlyRentedCount)
                .recentPendingReservations(recentPendingDtos)
                .build();
    }

}
