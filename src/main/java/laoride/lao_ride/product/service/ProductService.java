package laoride.lao_ride.product.service;

import laoride.lao_ride.product.domain.Product;
import laoride.lao_ride.product.dto.ProductGroupDto;
import laoride.lao_ride.product.repository.ProductRepository;
import laoride.lao_ride.product.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 생성자 주입을 위한 어노테이션
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정
public class ProductService {

    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;

    // 특정 기간에 예약이 잡혀있지 않은 상품들을 모델별로 그룹화하여 반환
    public List<ProductGroupDto> findAvailableProducts(LocalDate startDate, LocalDate endDate) {
        // 1. 해당 기간에 예약이 잡혀있어 이용 불가능한 상품들의 ID 목록을 가져옴
        List<Long> unavailableProductIds = reservationRepository.findUnavailableProductIds(startDate, endDate);

        // 2. 이용 가능한 상품 목록을 가져옴
        List<Product> availableProducts;
        if (unavailableProductIds.isEmpty()) {
            // 예약된 상품이 없으면 모든 ACTIVE 상품을 가져옴
            availableProducts = productRepository.findByStatus("ACTIVE");
        } else {
            // 예약된 상품이 있으면, 그 ID들을 제외한 ACTIVE 상품들을 가져옴
            availableProducts = productRepository.findByStatusAndIdNotIn("ACTIVE", unavailableProductIds);
        }

        // 3. 상품들을 모델별로 그룹화하고 DTO로 변환하여 반환
        return availableProducts.stream()
                .collect(Collectors.groupingBy(Product::getName, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    String modelName = entry.getKey();
                    long count = entry.getValue();
                    String imageUrl = availableProducts.stream()
                            .filter(p -> p.getName().equals(modelName))
                            .findFirst().map(Product::getImageUrl)
                            .orElse("/images/product/default-bike.png");
                    return new ProductGroupDto(modelName, imageUrl, count);
                })
                .collect(Collectors.toList());
    }

}
