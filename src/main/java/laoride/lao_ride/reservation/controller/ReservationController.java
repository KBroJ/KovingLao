package laoride.lao_ride.reservation.controller;

import laoride.lao_ride.product.service.ProductService;
import laoride.lao_ride.reservation.dto.ReservationRequestDto;
import laoride.lao_ride.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ProductService productService;
    private final ReservationService reservationService;

    @GetMapping("/reserve")
    public String reservationPage(
            @RequestParam(value = "modelName", required = false) String modelName,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {

        // JavaScript가 초기 상태를 설정할 수 있도록 파라미터를 그대로 View에 전달합니다.
        if (startDate != null) model.addAttribute("startDate", startDate.toString());
        if (endDate != null) model.addAttribute("endDate", endDate.toString());
        model.addAttribute("selectedModelName", modelName);

        // th:object 바인딩을 위한 빈 DTO 전달
        model.addAttribute("reservationRequest", new ReservationRequestDto());

        // 이제 reservation.html이 새로운 디자인의 페이지가 됩니다.
        return "reservation/reservation";
    }

    @PostMapping("/reserve")
    public String submitReservation(
            @ModelAttribute ReservationRequestDto reservationRequest,
            RedirectAttributes redirectAttributes
    ) {
        try {

            reservationService.createReservation(reservationRequest);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 접수되었습니다!");
            return "redirect:/reservation-success";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 처리 중 오류가 발생했습니다: " + e.getMessage());
            // 올바른 리다이렉트 경로로 변경
            return "redirect:/reserve";
        }
    }

    @GetMapping("/reservation-success")
    public String reservationSuccess() {
        return "reservation/reservation-success";
    }

}