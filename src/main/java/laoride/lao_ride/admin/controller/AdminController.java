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
    public ModelAndView dashboard() {
        // ModelAndView를 사용하면 view 이름과 model 객체를 한번에 관리하기 편합니다.
        ModelAndView mav = new ModelAndView();

        // 1. Model 데이터 추가
        DashboardSummaryDto summary = dashboardService.getDashboardSummary();
        mav.addObject("summary", summary);

        // 2. 콘텐츠 페이지 경로를 Model에 추가
        mav.addObject("page", "admin/dashboard-content"); // 실제 콘텐츠 파일 경로

        // 3. 뷰 이름으로 레이아웃 파일 경로 지정
        mav.setViewName("admin/fragments/admin-layout"); // 레이아웃을 직접 렌더링

        return mav;
    }

    /**
     * 전체 예약 목록 페이지를 보여줍니다.
     */
    @GetMapping("/reservations")
    public String reservationListPage(Model model) {
        List<Reservation> reservations = reservationService.findAllReservations();
        model.addAttribute("reservations", reservations);
        return "admin/reservations"; // templates/admin/reservations.html
    }

}
