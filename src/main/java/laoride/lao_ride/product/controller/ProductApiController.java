package laoride.lao_ride.product.controller;

import laoride.lao_ride.product.dto.ProductSummaryDto;
import laoride.lao_ride.product.dto.ProductGroupDto;
import laoride.lao_ride.product.service.ProductService;
import lombok.RequiredArgsConstructor;
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
}