package laoride.lao_ride.admin.service;

import laoride.lao_ride.admin.dto.AdminDashboardDto;
import laoride.lao_ride.member.repository.MemberRepository;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    /**
     *  월(가로축)/월별(세로축) 예약 건수를 관리하기 위해 Map<String, Object> 사용
     *
     */
    // 차트 데이터를 가공하는 메서드
    public Map<String, Object> getMonthlyReservationChartData() {
        // 1. 6개월 전 날짜 계산
        LocalDateTime startDate = LocalDateTime.now().minusMonths(6); // N개월로 수정가능

        /*
            2. DB에서 월별 예약 통계 데이터 조회

            List<Object[]>는 무엇인가?
                GROUP BY로 집계한 결과는 특정 엔티티와 모양이 다르기 때문에, JPA는 각 행(row)을 Object 배열로 묶어서 반환합니다.
                예를 들어, results 안에는 ["2025-06", 15L], ["2025-07", 22L] 와 같은 Object 배열들이 List에 담겨 있습니다.

            문제점: 이 results 리스트에는 예약이 1건도 없었던 달의 정보는 아예 포함되지 않습니다. (예: 5월에 예약이 없었다면, "2025-05" 데이터 자체가 없습니다.)

            * reservationRepository에서 COUNT(r.id)를 사용한 집계 함수의 반환 타입은 기본적으로 Long
                * JPQL(JPA Query Language)에서 COUNT 집계 함수는 기본적으로 Long 타입을 반환하도록 JPA 명세(Specification)에 정의되어 있습니다.
        */
        List<Object[]> results = reservationRepository.countMonthlyReservations(startDate);

        /*
            3. Chart.js가 사용할 형식으로 데이터 가공(위 문제점을 해결하기 위함)
                * 순서를 보장하기 위해 LinkedHashMap 사용(HashMap은 데이터를 저장할 때 순서를 보장하지 않음)

                3-1) "데이터 없는 달"을 위해 0으로 초기화
                    "5개월 전, 4개월 전, ..., 현재 달"까지 총 6개의 달에 대해 yyyy-MM 형식의 이름표를 만들고
                    모든 달의 예약 건수를 일단 0으로 초기화해서 LinkedHashMap에 순서대로 담습니다.

                3-2) DB로부터 가져온 results 리스트를 순회하며, 실제 예약 건수가 있는 달의 데이터를 덮어씁니다.
                    * Map의 put 메서드는 동일한 Key가 들어오면 기존의 Value를 새로운 Value로 덮어씁니다.


        */
        Map<String, Long> monthlyCounts = new LinkedHashMap<>();

        // 3-1) 지난 6개월의 모든 월을 'YYYY-MM' 형식의 키로, 값은 0으로 초기화
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            monthlyCounts.put(LocalDateTime.now().minusMonths(i).format(formatter), 0L);
        }

        // 3-2) DB에서 가져온 결과로 실제 예약 건수 업데이트
        for (Object[] result : results) {
            String month = (String) result[0];  // 월(Key : 2025-06)
            Long count = (Long) result[1];      // 월별 예약건수(Value: 15L)
            monthlyCounts.put(month, count);    // Map의 put 메서드는 동일한 Key가 들어오면 기존의 Value를 새로운 Value로 덮어씁니다.
        }

        /*
            4. 최종적으로 labels와 data로 분리하여 Map에 담아 반환

            monthlyCounts.keySet()은 Map의 Key("yyyy-MM" 문자열)들만 순서대로 뽑아서 리스트를 생성
            monthlyCounts.values()는 Value(예약 건수)들만 순서대로 뽑아서 리스트 생성
        */
        Map<String, Object> chartData = new LinkedHashMap<>();
        chartData.put("labels", monthlyCounts.keySet());
        chartData.put("data", monthlyCounts.values());

        return chartData;
    }

}
