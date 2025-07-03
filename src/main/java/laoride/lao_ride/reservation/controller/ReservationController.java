package laoride.lao_ride.reservation.controller;

import laoride.lao_ride.product.dto.ProductGroupDto;
import laoride.lao_ride.product.dto.ProductSummaryDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ReservationController {

    @GetMapping("/reserve")
    public String reservePage(@RequestParam(value = "model", required = false) String selectedModel, Model model) {
        
        // 임시 데이터 - 실제로는 서비스 레이어에서 가져와야 함
        List<ProductSummaryDto> allBikes = createBikeData();
        
        // 선택된 모델 정보
        if (selectedModel != null) {
            ProductGroupDto selectedBikeModel = getBikeModelByName(allBikes, selectedModel);
            model.addAttribute("selectedModel", selectedBikeModel);
        }
        
        // 모든 이용 가능한 모델 목록
        List<ProductGroupDto> availableModels = getAvailableModels(allBikes);
        model.addAttribute("availableModels", availableModels);
        
        return "reservation";
    }
    
    private List<ProductSummaryDto> createBikeData() {
        List<ProductSummaryDto> allBikes = new ArrayList<>();
        
        // 모델 A (총 5대 중 3대 가능)
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "예약 가능", "/images/product/e-bike-alpha.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "예약 가능", "/images/product/e-bike-alpha.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "예약 가능", "/images/product/e-bike-alpha.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "대여 중", "/images/product/e-bike-alpha.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "대여 중", "/images/product/e-bike-alpha.png"));
        
        // 모델 B (총 3대 중 2대 가능)
        allBikes.add(new ProductSummaryDto("E-Bike Bravo", "예약 가능", "/images/product/e-bike-bravo.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Bravo", "예약 가능", "/images/product/e-bike-bravo.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Bravo", "대여 중", "/images/product/e-bike-bravo.png"));
        
        // 모델 C (총 2대 모두 대여 중)
        allBikes.add(new ProductSummaryDto("E-Bike Charlie", "대여 중", "/images/product/e-bike-charlie.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Charlie", "대여 중", "/images/product/e-bike-charlie.png"));
        
        // 모델 D (총 1대 가능)
        allBikes.add(new ProductSummaryDto("E-Bike Delta", "예약 가능", "/images/product/e-bike-delta.png"));
        
        return allBikes;
    }
    
    private ProductGroupDto getBikeModelByName(List<ProductSummaryDto> allBikes, String modelName) {
        long availableCount = allBikes.stream()
                .filter(bike -> bike.getName().equals(modelName) && "예약 가능".equals(bike.getStatus()))
                .count();
        
        String imageUrl = allBikes.stream()
                .filter(bike -> bike.getName().equals(modelName))
                .findFirst()
                .map(ProductSummaryDto::getImageUrl)
                .orElse("/images/default-bike.png");
        
        return new ProductGroupDto(modelName, imageUrl, availableCount);
    }
    
    private List<ProductGroupDto> getAvailableModels(List<ProductSummaryDto> allBikes) {
        return allBikes.stream()
                .filter(bike -> "예약 가능".equals(bike.getStatus()))
                .collect(Collectors.groupingBy(ProductSummaryDto::getName, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    String modelName = entry.getKey();
                    long count = entry.getValue();
                    String imageUrl = allBikes.stream()
                            .filter(b -> b.getName().equals(modelName))
                            .findFirst()
                            .map(ProductSummaryDto::getImageUrl)
                            .orElse("/images/default-bike.png");
                    return new ProductGroupDto(modelName, imageUrl, count);
                })
                .collect(Collectors.toList());
    }
    
    @PostMapping("/submit-reservation")
    public String submitReservation(
            @RequestParam("selectedModel") String selectedModel,
            @RequestParam("rentalDate") String rentalDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("duration") String duration,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("passportNumber") String passportNumber,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 실제로는 데이터베이스에 저장하거나 이메일 발송 등의 로직이 들어갑니다
            System.out.println("=== 예약 정보 ===");
            System.out.println("선택 모델: " + selectedModel);
            System.out.println("대여 날짜: " + rentalDate);
            System.out.println("대여 시간: " + startTime);
            System.out.println("대여 기간: " + duration + "시간");
            System.out.println("예약자: " + firstName + " " + lastName);
            System.out.println("이메일: " + email);
            System.out.println("연락처: " + phone);
            System.out.println("여권번호: " + passportNumber);
            System.out.println("================");
            
            // 예약 성공 메시지
            redirectAttributes.addFlashAttribute("successMessage", 
                "예약이 성공적으로 접수되었습니다! 곧 확인 이메일을 받으실 수 있습니다.");
            redirectAttributes.addFlashAttribute("reservationInfo", 
                String.format("%s | %s %s | %s시간", selectedModel, rentalDate, startTime, duration));
            
            return "redirect:/reservation-success";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "예약 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/reserve";
        }
    }
    
    @GetMapping("/reservation-success")
    public String reservationSuccess() {
        return "reservation-success";
    }
} 