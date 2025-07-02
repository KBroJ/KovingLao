package laoride.lao_ride.product.controller;

import laoride.lao_ride.product.dto.ProductSummaryDto;
import laoride.lao_ride.product.dto.ProductGroupDto;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bikes") // 이 컨트롤러의 모든 API는 /api/bikes 로 시작
public class ProductApiController {

    @GetMapping("/available")
    public List<ProductGroupDto> getAvailableBikes(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // --- 여기부터는 DB 조회 로직을 시뮬레이션하는 임시 데이터입니다. ---

        // 1. 전체 오토바이 목록 (실제로는 DB의 BIKE 테이블)
        List<ProductSummaryDto> allBikes = getAllBikes();

        // 2. 전체 예약 목록 (실제로는 DB의 RESERVATION 테이블)
        List<Reservation> allReservations = getReservations();

        // 3. 요청된 기간(startDate ~ endDate)에 예약된 오토바이의 이름 목록을 찾음
        List<String> unavailableBikeNames = allReservations.stream()
                // 요청된 기간과 겹치는 예약을 필터링
                .filter(res -> !res.getEndDate().isBefore(startDate) && !res.getStartDate().isAfter(endDate))
                .map(Reservation::getBikeName)
                .collect(Collectors.toList());

        // 4. 전체 오토바이에서, 예약된 오토바이를 제외하고, 모델별로 그룹화하여 개수를 셈
        List<ProductGroupDto> availableModels = allBikes.stream()
                .filter(bike -> !unavailableBikeNames.contains(bike.getName())) // 예약 안된 오토바이 필터링
                .collect(Collectors.groupingBy(ProductSummaryDto::getName, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    String modelName = entry.getKey();
                    long count = entry.getValue();
                    String imageUrl = allBikes.stream()
                            .filter(b -> b.getName().equals(modelName))
                            .findFirst().map(ProductSummaryDto::getImageUrl)
                            .orElse("/images/product/default-bike.png");
                    return new ProductGroupDto(modelName, imageUrl, count);
                })
                .collect(Collectors.toList());

        return availableModels;
    }

    // --- 임시 데이터 생성 메서드 (나중에 DB 로직으로 대체) ---
    private List<ProductSummaryDto> getAllBikes() {
        List<ProductSummaryDto> bikes = new ArrayList<>();
        bikes.add(new ProductSummaryDto("E-Bike Alpha", "AVAILABLE", "/images/product/e-bike-alpha.png"));
        bikes.add(new ProductSummaryDto("E-Bike Alpha", "AVAILABLE", "/images/product/e-bike-alpha.png"));
        bikes.add(new ProductSummaryDto("E-Bike Bravo", "AVAILABLE", "/images/product/e-bike-bravo.png"));
        // ... 등등 모든 오토바이 개체
        return bikes;
    }

    private List<Reservation> getReservations() {
        List<Reservation> reservations = new ArrayList<>();
        // 예시: 알파 모델 하나는 7월 5일 ~ 7월 7일 예약됨
        reservations.add(new Reservation("E-Bike Alpha", LocalDate.of(2025, 7, 5), LocalDate.of(2025, 7, 7)));
        // 예시: 브라보 모델 하나는 7월 6일 ~ 7월 8일 예약됨
        reservations.add(new Reservation("E-Bike Bravo", LocalDate.of(2025, 7, 6), LocalDate.of(2025, 7, 8)));
        return reservations;
    }

    // 임시 예약을 위한 내부 클래스
    private static class Reservation {
        private String bikeName;
        private LocalDate startDate;
        private LocalDate endDate;

        public Reservation(String bikeName, LocalDate startDate, LocalDate endDate) {
            this.bikeName = bikeName;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getBikeName() { return bikeName; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
    }
}