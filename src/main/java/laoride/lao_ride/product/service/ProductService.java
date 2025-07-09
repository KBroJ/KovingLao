package laoride.lao_ride.product.service;

import laoride.lao_ride.content.domain.ContentGroup;
import laoride.lao_ride.content.domain.ContentImage;
import laoride.lao_ride.content.repository.ContentGroupRepository;
import laoride.lao_ride.content.repository.ContentImageRepository;
import laoride.lao_ride.global.util.FileStorageService;
import laoride.lao_ride.product.domain.InventoryItem;
import laoride.lao_ride.product.domain.InventoryItemStatus;
import laoride.lao_ride.product.domain.ProductModel;
import laoride.lao_ride.product.dto.*;
import laoride.lao_ride.product.repository.InventoryItemRepository;
import laoride.lao_ride.product.repository.ProductModelRepository;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor        // 생성자 주입을 위한 어노테이션
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정
public class ProductService {

    private final ProductModelRepository productModelRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final ReservationRepository reservationRepository;
    private final ContentImageRepository contentImageRepository;
    private final ContentGroupRepository contentGroupRepository;
    private final FileStorageService fileStorageService;

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


    /**
     * 특정 기간에 예약 가능한 상품 모델과 '대표 이미지'를 반환합니다.
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 예약 가능한 상품 그룹 정보 DTO 리스트
     */
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

                    // 모델의 대표 이미지(displayOrder=0) URL을 Content 시스템을 통해 조회
                    String representativeImageUrl = contentImageRepository
                            .findFirstByContentGroup_GroupKeyAndDisplayOrder("MODEL_" + model.getId() + "_IMAGES", 0)
                            .map(ContentImage::getImageUrl)
                            .orElse("/images/product/default-bike.png"); // 이미지가 없으면 기본 이미지


                    return new ProductGroupDto(model.getId(), model.getName(), representativeImageUrl, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 상품 모델 ID로 상세 정보와 '모든 이미지'를 조회합니다.
     * @param modelId 상품 모델 ID
     * @return 상품 상세 정보 DTO
     */
    public ProductDetailDto findProductDetailsById(Long modelId) {
        ProductModel model = productModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("상품 모델을 찾을 수 없습니다: " + modelId));

        // Content 시스템을 통해 이 모델에 속한 모든 이미지를 조회
        String imageGroupKey = "MODEL_" + modelId + "_IMAGES";
        List<ContentImage> images = contentImageRepository.findByContentGroup_GroupKeyOrderByDisplayOrderAsc(imageGroupKey);

        return ProductDetailDto.from(model, images);
    }

    /**
     * 특정 모델의 특정 날짜 재고 및 가격을 조회합니다.
     * @param modelId 상품 모델 ID
     * @param date 조회할 날짜
     * @return 상품 이용 가능 정보 DTO
     */
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
     * 폼으로부터 받은 DTO로 새로운 상품 모델을 생성하고 저장합니다.
     * @param dto 상품 모델 폼 데이터
     * @return 저장된 ProductModel 엔티티
     */
    @Transactional // 데이터를 저장하므로 @Transactional 어노테이션을 붙여줍니다.
    public ProductModel createProductModel(ProductModelFormDto dto, List<MultipartFile> imageFiles) {
        // 1. 먼저 ProductModel 엔티티를 생성하고 저장합니다.
        ProductModel newModel = ProductModel.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .dailyRate(dto.getDailyRate())
                .monthlyRate(dto.getMonthlyRate())
                .deposit(dto.getDeposit())
                .includedItems(dto.getIncludedItems())
                .notIncludedItems(dto.getNotIncludedItems())
                .usageGuide(dto.getUsageGuide())
                .cancellationPolicy(dto.getCancellationPolicy())
                .isActive(dto.getIsActive())
                .build();
        productModelRepository.save(newModel);

        // 2. 이 모델에 대한 ContentGroup 생성
        ContentGroup imageGroup = new ContentGroup("MODEL_" + newModel.getId() + "_IMAGES", newModel.getName() + " 상세 이미지");
        log.info("ProductService|createProductModel|ContentGroup 생성 : Id : {}, GroupKey : {}, Description : {} ", imageGroup.getId(), imageGroup.getGroupKey(), imageGroup.getDescription());
        contentGroupRepository.save(imageGroup);

        // 3. 업로드된 이미지 파일들을 저장하고 ContentImage 엔티티 생성
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile file = imageFiles.get(i);

                // 새로운 storeFile 메서드 호출
                String storedFilePath = fileStorageService.storeFile(
                        file,
                        "product", // 도메인은 "product"
                        newModel.getId().toString() // 도메인 ID는 방금 생성된 모델의 ID
                );

                if (storedFilePath != null) {
                    // 대표 이미지로 지정된 파일은 displayOrder를 0으로, 나머지는 1, 2, ... 순으로 저장
                    int displayOrder = (file.getOriginalFilename().equals(dto.getRepresentativeImageName())) ? 0 : i + 1;

                    ContentImage contentImage = ContentImage.builder()
                            .contentGroup(imageGroup)
                            .imageUrl(storedFilePath)
                            .displayOrder(displayOrder)
                            .build();
                    contentImageRepository.save(contentImage);
                }
            }
        }


        // 4. 초기 재고(InventoryItem)를 생성하고 저장합니다.
        log.info("ProductService|createProductModel|InventoryItem 생성 여부|dto.getInitialQuantity() > 0 : {}", dto.getInitialQuantity());
        if (dto.getInitialQuantity() > 0) {
            // 관리 코드 생성을 위한 약어 (예: "K-Bike Standard" -> "KBS")
            String prefix = createManagementCodePrefix(newModel);
            log.info("ProductService|createProductModel|InventoryItem 생성|prefix : {}", prefix);

            for (int i = 1; i <= dto.getInitialQuantity(); i++) {
                // 3자리 숫자로 포맷팅 (예: 1 -> "001")
                String managementCode = String.format("%s-%03d", prefix, i);
                log.info("ProductService|createProductModel|managementCode : {}", managementCode); // ex) KBS_1-001

                InventoryItem newItem = InventoryItem.builder()
                        .productModel(newModel)
                        .managementCode(managementCode)
                        .status(InventoryItemStatus.AVAILABLE) // 초기 상태는 '대여 가능'
                        .build();

                inventoryItemRepository.save(newItem);
            }
        }

        return newModel;
    }

    /**
     * 모델명으로부터 관리 코드의 접두사를 생성하는 헬퍼 메서드
     * 예: "K-Bike Standard" -> "KBS"
     */
    private String createManagementCodePrefix(ProductModel model) {
        StringBuilder prefix = new StringBuilder();
        String[] words = model.getName().split("\\s+"); // 공백으로 단어 분리
        for (String word : words) {
            if (!word.isEmpty()) {
                prefix.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        prefix.append("_").append(model.getId());

        return prefix.toString();
    }

}
