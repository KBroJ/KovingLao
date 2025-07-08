package laoride.lao_ride.admin.controller;

import laoride.lao_ride.admin.dto.AdminDashboardDto;
import laoride.lao_ride.admin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController // 데이터를 반환하는 API 컨트롤러임을 명시(모든 메서드는 반환하는 객체를 자동으로 JSON 데이터로 변환하여 응답합니다.)
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminDashboardApiController {

    private final DashboardService dashboardService;

    // 대시보드 전체 데이터를 JSON으로 반환하는 API
    @GetMapping("/dashboard")
    public AdminDashboardDto getDashboardData() {
        return dashboardService.getDashboardData();
    }

    // 차트 데이터를 JSON으로 반환하는 API
    @GetMapping("/stats/monthly-reservations")
    public Map<String, Object> getMonthlyReservations() {
        return dashboardService.getMonthlyReservationChartData();
    }

}
