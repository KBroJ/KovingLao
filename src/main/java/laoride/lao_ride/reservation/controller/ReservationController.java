package laoride.lao_ride.reservation.controller;

import laoride.lao_ride.product.dto.ProductGroupDto;
import laoride.lao_ride.product.dto.ProductSummaryDto;
import laoride.lao_ride.product.service.ProductService;
import laoride.lao_ride.reservation.dto.ReservationRequestDto;
import laoride.lao_ride.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ProductService productService;
    private final ReservationService reservationService;

    @GetMapping("/reserve")
    public String reservePage(
        @RequestParam String modelName,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate endDate,
        Model model
    ) {

        // 날짜 정보가 없으면 오늘 날짜를 기본값으로 설정
        LocalDate searchStartDate = (startDate != null) ? startDate : LocalDate.now();
        LocalDate searchEndDate = (endDate != null) ? endDate : LocalDate.now();

        // 1. 해당 날짜에 이용 가능한 모든 모델 목록을 조회 (왼쪽 패널용)
        List<ProductGroupDto> availableModels = productService.findAvailableProducts(searchStartDate, searchEndDate);
        model.addAttribute("availableModels", availableModels);

        // 2. 파라미터로 특정 모델이 넘어온 경우, 해당 모델을 '선택된 모델'로 지정 (오른쪽 폼용)
        if (modelName != null) {
            availableModels.stream()
                    .filter(m -> m.getName().equals(modelName))
                    .findFirst()
                    .ifPresent(selected -> model.addAttribute("selectedModel", selected));
        }

        // 3. 날짜 정보도 모델에 담아 전달
        model.addAttribute("startDate", searchStartDate);
        model.addAttribute("endDate", searchEndDate);

        return "reservation";
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
            redirectAttributes.addFlashAttribute("errorMessage", "예약 처리 중 오류가 발생했습니다.");
            return "redirect:/reserve"; // 에러 발생 시 원래 파라미터를 담아 리다이렉트 필요 (추후 개선)
        }
    }

    @GetMapping("/reservation-success")
    public String reservationSuccess() {
        return "reservation-success";
    }

}