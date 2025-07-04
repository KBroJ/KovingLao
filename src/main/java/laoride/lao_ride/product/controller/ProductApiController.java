package laoride.lao_ride.product.controller;

import laoride.lao_ride.product.dto.ProductAvailabilityDto;
import laoride.lao_ride.product.dto.ProductDetailDto;
import laoride.lao_ride.product.dto.ProductSummaryDto;
import laoride.lao_ride.product.dto.ProductGroupDto;
import laoride.lao_ride.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/products") // 이 컨트롤러의 모든 API는 /api/bikes 로 시작
@RequiredArgsConstructor // 생성자 주입을 위한 어노테이션
public class ProductApiController {

    private final ProductService productService;

    // 예약가능한 상품 목록 조회 API
    @GetMapping("/available")
    public List<ProductGroupDto> getAvailableProducts(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return productService.findAvailableProducts(startDate, endDate);
    }

    // 상품 상세 정보 API
    @GetMapping("/{productId}")
    public ProductDetailDto getProductDetails(
            @PathVariable("productId") Long productId
    ) {
        return productService.findProductDetailsById(productId);
    }

    // 특정 날짜의 재고/가격 조회 API
    @GetMapping("/{productId}/availability")
    public ProductAvailabilityDto getProductAvailability(
            @PathVariable("productId") Long productId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return productService.findProductAvailability(productId, date);
    }
}