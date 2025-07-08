package laoride.lao_ride.product.service;

import laoride.lao_ride.content.domain.ContentImage;
import laoride.lao_ride.content.repository.ContentImageRepository;
import laoride.lao_ride.product.domain.InventoryItem;
import laoride.lao_ride.product.domain.InventoryItemStatus;
import laoride.lao_ride.product.domain.ProductModel;
import laoride.lao_ride.product.dto.*;
import laoride.lao_ride.product.repository.InventoryItemRepository;
import laoride.lao_ride.product.repository.ProductModelRepository;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor        // 생성자 주입을 위한 어노테이션
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정
public class ProductService {

    private final ProductModelRepository productModelRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ReservationRepository reservationRepository;
    private final ContentImageRepository contentImageRepository;

    /**
     * 관리자 페이지의 '상품 모델 목록'에 필요한 데이터를 조회하는 메서드
     * @return 각 상품 모델의 요약 정보 리스트
     */
    public List<AdminProductListDto> getProductModelSummaries() {
        // 1. 모든 상품 모델을 조회합니다.
        List<ProductModel> models = productModelRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        // 2. 각 모델별로 총 보유 대수와 대여 가능 대수를 계산하여 DTO 리스트로 변환합니다.
        // TODO: 향후 데이터가 많아지면 N+1 문제가 발생할 수 있으므로, 성능 최적화가 필요합니다.
        return models.stream().map(model -> {
            long totalQuantity = inventoryItemRepository.countByProductModel(model);
            long availableQuantity = inventoryItemRepository.countByProductModelAndStatus(model, InventoryItemStatus.AVAILABLE);
            return new AdminProductListDto(model, totalQuantity, availableQuantity);
        }).collect(Collectors.toList());
    }


    // 특정 기간에 예약이 잡혀있지 않은 상품들을 모델별로 그룹화하여 반환
    public List<ProductGroupDto> findAvailableProducts(LocalDate startDate, LocalDate endDate) {
        // 1. 해당 기간에 예약되어 이용 불가능한 개별 재고(item) ID 목록을 가져옴
        List<Long> unavailableItemIds = reservationRepository.findUnavailableItemIds(startDate, endDate);

        // 2. 이용 가능한 개별 재고 목록을 가져옴
        List<InventoryItem> availableItems;
        if (unavailableItemIds.isEmpty()) {
            availableItems = inventoryItemRepository.findByStatus(InventoryItemStatus.AVAILABLE);
        } else {
            availableItems = inventoryItemRepository.findByStatusAndIdNotIn(InventoryItemStatus.AVAILABLE, unavailableItemIds);
        }

        // 3. 이용 가능한 재고들을 ProductModel 별로 그룹화하고, 각 모델의 수량을 계산
        return availableItems.stream()
                .collect(Collectors.groupingBy(InventoryItem::getProductModel, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    ProductModel model = entry.getKey();
                    long count = entry.getValue();
                    return new ProductGroupDto(model.getId(), model.getName(), model.getImageUrl(), count);
                })
                .collect(Collectors.toList());
    }

    // 상품 상세 페이지용: 상품 모델 ID로 상세 정보 조회
    public ProductDetailDto findProductDetailsById(Long modelId) {
        ProductModel model = productModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("상품 모델을 찾을 수 없습니다: " + modelId));

        String imageGroupKey = "MODEL_" + modelId + "_IMAGES";
        List<ContentImage> images = contentImageRepository.findByContentGroup_GroupKeyOrderByDisplayOrderAsc(imageGroupKey);

        return ProductDetailDto.from(model, images);
    }

    // 상품 상세 페이지용: 특정 상품의 특정 날짜 재고 및 가격 조회
    public ProductAvailabilityDto findProductAvailability(Long modelId, LocalDate date) {
        ProductModel model = productModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("상품 모델을 찾을 수 없습니다: " + modelId));

        // 1. 해당 모델의 총 재고 수량 계산
        long totalStock = inventoryItemRepository.countByProductModel(model);

        // 2. 해당 날짜에 예약된 수량 계산
        long reservedCount = reservationRepository.countByProductModelAndDate(model, date);

        // 3. 이용 가능 수량 계산
        long availableCount = totalStock - reservedCount;

        // 4. 가격 및 보증금 정보는 ProductModel에서 직접 가져옴
        return new ProductAvailabilityDto(availableCount, model.getDailyRate(), model.getDeposit());
    }

    // 관리자 페이지용: 모든 상품 목록 조회
    public List<ProductModel> findAllProducts() {
        return productModelRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * [추가] 폼으로부터 받은 DTO로 새로운 상품 모델을 생성하고 저장합니다.
     * @param dto 상품 모델 폼 데이터
     * @return 저장된 ProductModel 엔티티
     */
    @Transactional // 데이터를 저장하므로 @Transactional 어노테이션을 붙여줍니다.
    public ProductModel createProductModel(ProductModelFormDto dto) {
        // 빌더 패턴을 사용하여 DTO로부터 엔티티를 생성합니다.
        ProductModel newModel = ProductModel.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .dailyRate(dto.getDailyRate())
                .monthlyRate(dto.getMonthlyRate())
                .deposit(dto.getDeposit())
                .includedItems(dto.getIncludedItems())
                .notIncludedItems(dto.getNotIncludedItems())
                .usageGuide(dto.getUsageGuide())
                .cancellationPolicy(dto.getCancellationPolicy())
                .isActive(dto.getIsActive())
                .build();

        return productModelRepository.save(newModel);
    }



}
