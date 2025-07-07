package laoride.lao_ride.admin.service;

import laoride.lao_ride.admin.dto.DashboardSummaryDto;
import laoride.lao_ride.member.repository.MemberRepository;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;

    public DashboardSummaryDto getDashboardSummary() {
        // 오늘 신규 예약 건수 조회
        long todayReservations = reservationRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay());

        // '확인 대기중' 상태인 예약 건수 조회
        long pendingReservations = reservationRepository.countByStatus("PENDING");

        // 이번 달 매출 조회
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        BigDecimal monthlySales = reservationRepository.sumTotalPriceBetweenDates(startOfMonth, endOfMonth)
                .orElse(BigDecimal.ZERO);

        // 전체 회원(관리자/직원) 수 조회
        long totalMembers = memberRepository.count();

        return new DashboardSummaryDto(todayReservations, pendingReservations, monthlySales, totalMembers);
    }

}
