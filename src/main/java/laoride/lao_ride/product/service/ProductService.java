package laoride.lao_ride.product.service;

import laoride.lao_ride.content.domain.ContentImage;
import laoride.lao_ride.content.repository.ContentImageRepository;
import laoride.lao_ride.product.domain.Product;
import laoride.lao_ride.product.dto.ProductAvailabilityDto;
import laoride.lao_ride.product.dto.ProductDetailDto;
import laoride.lao_ride.product.dto.ProductGroupDto;
import laoride.lao_ride.product.repository.ProductPriceRepository;
import laoride.lao_ride.product.repository.ProductRepository;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 생성자 주입을 위한 어노테이션
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정
public class ProductService {

    private final ProductRepository productRepository;
    private final ReservationRepository reservationRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ContentImageRepository contentImageRepository;

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

        // 상품들을 모델별로 그룹화할 때, 대표 ID도 함께 찾아서 DTO에 담아줍니다.
        return availableProducts.stream()
                .collect(Collectors.groupingBy(Product::getName))
                .entrySet().stream()
                .map(entry -> {
                    String modelName = entry.getKey();
                    List<Product> productsInGroup = entry.getValue();
                    long count = productsInGroup.size();

                    // 그룹의 대표 상품 정보 (첫 번째 상품 기준)
                    Product representative = productsInGroup.get(0);

                    return new ProductGroupDto(
                            representative.getId(), // [수정] ID 추가
                            modelName,
                            representative.getImageUrl(),
                            count
                    );
                })
                .collect(Collectors.toList());
    }

    // 상품 상세 페이지용: 특정 상품의 모든 상세 정보 조회
    public ProductDetailDto findProductDetailsById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        // 상품 이미지 그룹 키 생성 (예: PRODUCT_1_IMAGES)
        String imageGroupKey = "PRODUCT_" + productId + "_IMAGES";
        List<ContentImage> images = contentImageRepository.findByContentGroup_GroupKeyOrderByDisplayOrderAsc(imageGroupKey);

        return ProductDetailDto.from(product, images);
    }

    // 상품 상세 페이지용: 특정 상품의 특정 날짜 재고 및 가격 조회
    public ProductAvailabilityDto findProductAvailability(Long productId, LocalDate date) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        // 1. 총 재고 수량 계산 (이름이 같은 모든 상품의 수)
        long totalStock = productRepository.countByName(product.getName());

        // 2. 해당 날짜에 예약된 수량 계산
        long reservedCount = reservationRepository.countByProductIdAndDate(productId, date);

        // 3. 이용 가능 수량 계산
        long availableCount = totalStock - reservedCount;

        // 4. 가격 조회
        BigDecimal price = productPriceRepository.findFirstByProductIdOrderByEffectiveDateDesc(productId)
                .map(p -> p.getDailyRate())
                .orElse(BigDecimal.ZERO);

        return new ProductAvailabilityDto(availableCount, price);
    }

}
