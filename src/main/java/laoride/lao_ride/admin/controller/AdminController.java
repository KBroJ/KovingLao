package laoride.lao_ride.admin.controller;

import laoride.lao_ride.admin.dto.DashboardSummaryDto;
import laoride.lao_ride.admin.service.DashboardService;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DashboardService dashboardService;
    private final ReservationService reservationService;

    /**
     * 관리자 로그인 페이지를 보여줍니다.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login"; // templates/admin/login.html 파일을 찾음
    }

    /**
     * 로그인 성공 후 보여줄 관리자 대시보드(메인) 페이지입니다.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        DashboardSummaryDto summary = dashboardService.getDashboardSummary();

        model.addAttribute("summary", summary);

        return "admin/dashboard"; // 콘텐츠 뷰 이름 반환
    }

    /**
     * 전체 예약 목록 페이지를 보여줍니다.
     */
    @GetMapping("/reservations")
    public String reservationListPage(Model model) {
        List<Reservation> reservations = reservationService.findAllReservations();
        model.addAttribute("reservations", reservations);
        return "admin/reservations";
    }

}
