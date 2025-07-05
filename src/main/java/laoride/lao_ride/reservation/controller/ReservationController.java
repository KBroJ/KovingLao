package laoride.lao_ride.reservation.controller;

import laoride.lao_ride.product.service.ProductService;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.reservation.dto.ReservationRequestDto;
import laoride.lao_ride.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
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

    /**
     * 예약 폼 제출을 처리합니다.
     * @param reservationRequest 폼 데이터가 바인딩된 DTO
     * @param redirectAttributes 리다이렉트 시 메시지를 전달하기 위한 객체
     * @return 성공 또는 실패에 따른 리다이렉트 경로
     */
    @PostMapping("/reserve")
    public String submitReservation(
            @ModelAttribute ReservationRequestDto reservationRequest,
            RedirectAttributes redirectAttributes
    ) {

        log.info("Received reservation request for: {}", reservationRequest.getEmail());

        try {

            // 서비스 호출 후, 저장된 Reservation 객체를 받아옵니다.
            Reservation savedReservation = reservationService.createReservation(reservationRequest);

            // 대여일수 계산
            long rentalDays = ChronoUnit.DAYS.between(savedReservation.getStartDate(), savedReservation.getEndDate()) + 1;

            // 성공 메시지와 함께, 화면에 표시할 상세 정보들을 각각 담아줍니다.
            redirectAttributes.addFlashAttribute("successMessage", "예약 접수가 성공적으로 완료되었습니다. 확인 이메일을 곧 보내드리겠습니다.");
            redirectAttributes.addFlashAttribute("reservedCode", savedReservation.getReservationCode());
            redirectAttributes.addFlashAttribute("reservedCustomer", savedReservation.getCustomerName());
            redirectAttributes.addFlashAttribute("reservedModel", savedReservation.getProduct().getName());
            redirectAttributes.addFlashAttribute("reservedPeriod",
                    savedReservation.getStartDate().toString() + " ~ " + savedReservation.getEndDate().toString());
            redirectAttributes.addFlashAttribute("rentalDays", rentalDays); // 계산된 대여일수
            redirectAttributes.addFlashAttribute("reservedPickupTime",
                    savedReservation.getPickupTime().format(DateTimeFormatter.ofPattern("HH:mm")));
            redirectAttributes.addFlashAttribute("totalPrice", savedReservation.getTotalPrice());

            return "redirect:/reservation-success";

        } catch (Exception e) {
            log.error("Error during reservation process", e);

            // 실패 시, 에러 메시지를 담아 리다이렉트합니다.
            redirectAttributes.addFlashAttribute("errorMessage", "예약 처리 중 오류가 발생했습니다. 다시 시도해주세요.");

            return "redirect:/reserve";
        }
    }

    /**
     * 예약 완료 페이지를 보여줍니다.
     */
    @GetMapping("/reservation-success")
    public String reservationSuccessPage() {
        return "reservation/reservation-success";
    }


    /**
     * 예약 조회 페이지를 보여주는 메서드
     */
    @GetMapping("/check-reservation")
    public String checkReservationPage() {
        return "reservation/check-reservation";
    }



}