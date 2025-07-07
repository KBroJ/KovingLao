package laoride.lao_ride.admin.service;

import laoride.lao_ride.admin.dto.AdminDashboardDto;
import laoride.lao_ride.member.repository.MemberRepository;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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

        // 6. 오늘의 스케줄 목록 생성
        // 오늘 출고 건 조회 ('확정' 또는 '대기' 상태 모두)
        List<Reservation> pickups = reservationRepository.findByStartDateAndStatusIn(today, List.of("CONFIRMED", "PENDING"));
        // 오늘 반납 건 조회 ('확정' 상태만)
        List<Reservation> returns = reservationRepository.findByEndDateAndStatusIn(today, List.of("CONFIRMED"));

        // DTO 리스트로 변환
        List<AdminDashboardDto.TodayScheduleItem> scheduleItems = new ArrayList<>();
        pickups.forEach(r -> scheduleItems.add(new AdminDashboardDto.TodayScheduleItem(r, "출고")));
        returns.forEach(r -> scheduleItems.add(new AdminDashboardDto.TodayScheduleItem(r, "반납")));

        // 시간순으로 정렬
        scheduleItems.sort(Comparator.comparing(AdminDashboardDto.TodayScheduleItem::getTime));

        // 7. 최종 DTO 생성 및 반환
        return AdminDashboardDto.builder()
                .todayNewReservations(todayNewReservations)
                .pendingReservations(pendingReservations)
                .todayExpectedSales(todayExpectedSales)
                .currentlyRentedCount(currentlyRentedCount)
                .recentPendingReservations(recentPendingDtos)
                .todaySchedule(scheduleItems)
                .build();
    }

}
