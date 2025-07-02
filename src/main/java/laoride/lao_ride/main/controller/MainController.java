package laoride.lao_ride.main.controller;

import laoride.lao_ride.product.dto.ProductSummaryDto;
import laoride.lao_ride.product.dto.ProductGroupDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @GetMapping("/") // HTTP GET 요청이 루트 경로("/")로 올 때 이 메서드를 실행합니다.
    public String mainPage(Model model) {

        // Thymeleaf에 전달할 오토바이 데이터 (임시 데이터)
        // --- 임시 데이터 생성 (DB에서 모든 오토바이 정보를 가져왔다고 가정) ---
        List<ProductSummaryDto> allBikes = new ArrayList<>();

        // 모델 A (총 5대 중 3대 가능)
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "예약 가능", "/images/e-bike-alpha.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "예약 가능", "/images/e-bike-alpha.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "예약 가능", "/images/e-bike-alpha.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "대여 중", "/images/e-bike-alpha.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Alpha", "대여 중", "/images/e-bike-alpha.png"));
        // 모델 B (총 3대 중 2대 가능)
        allBikes.add(new ProductSummaryDto("E-Bike Bravo", "예약 가능", "/images/e-bike-bravo.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Bravo", "예약 가능", "/images/e-bike-bravo.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Bravo", "대여 중", "/images/e-bike-bravo.png"));
        // 모델 C (총 2대 모두 대여 중)
        allBikes.add(new ProductSummaryDto("E-Bike Charlie", "대여 중", "/images/e-bike-charlie.png"));
        allBikes.add(new ProductSummaryDto("E-Bike Charlie", "대여 중", "/images/e-bike-charlie.png"));
        // 모델 D (총 1대 가능)
        allBikes.add(new ProductSummaryDto("E-Bike Delta", "예약 가능", "/images/e-bike-delta.png"));



        // 예약 가능한 모델만, 이름으로 그룹화하여 개수 세기 ---
        List<ProductGroupDto> availableModels = allBikes.stream()
                // 1. "예약 가능" 상태인 오토바이만 필터링
                .filter(bike -> "예약 가능".equals(bike.getStatus()))
                // 2. 오토바이 이름(모델명)으로 그룹화하고, 각 그룹의 개수를 셈
                .collect(Collectors.groupingBy(ProductSummaryDto::getName, Collectors.counting()))
                // 3. 그룹화된 Map을 BikeModelDto 리스트로 변환
                .entrySet().stream()
                .map(entry -> {
                    String modelName = entry.getKey();
                    long count = entry.getValue();
                    // 대표 이미지 찾기 (첫 번째 매칭되는 오토바이의 이미지 사용)
                    String imageUrl = allBikes.stream()
                            .filter(b -> b.getName().equals(modelName))
                            .findFirst()
                            .map(ProductSummaryDto::getImageUrl)
                            .orElse("/images/default-bike.png");
                    return new ProductGroupDto(modelName, imageUrl, count);
                })
                .collect(Collectors.toList());


        model.addAttribute("bikeModels", availableModels);

        return "main";
    }

}
