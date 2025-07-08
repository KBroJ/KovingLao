package laoride.lao_ride.admin.controller;

import laoride.lao_ride.admin.dto.AdminDashboardDto;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

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
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard"; // 콘텐츠 뷰 이름 반환
    }

    /**
     * 전체 예약 목록 페이지를 보여줍니다.
     */
    @GetMapping("/reservations")
    public String reservationListPage(Model model) {
        model.addAttribute("activeMenu", "reservations");
        return "admin/reservations";
    }

    // 상품 관리 페이지를 보여주는 메서드
    @GetMapping("/products")
    public String productListPage(Model model) {
        model.addAttribute("activeMenu", "products");
        return "admin/products";
    }

}
