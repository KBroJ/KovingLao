package laoride.lao_ride.admin.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DashboardSummaryDto {

    private final long todayReservations;    // 오늘 신규 예약 건수
    private final long pendingReservations;  // 처리해야 할 '확인 대기중' 예약 건수
    private final BigDecimal monthlySales;   // 이번 달 총 매출
    private final long totalMembers;         // 전체 관리자/직원 수

    public DashboardSummaryDto(long todayReservations, long pendingReservations, BigDecimal monthlySales, long totalMembers) {
        this.todayReservations = todayReservations;
        this.pendingReservations = pendingReservations;
        this.monthlySales = monthlySales;
        this.totalMembers = totalMembers;
    }

}
