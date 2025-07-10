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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
                .sorted(Comparator.comparing(item -> item.getProductModel().getId()))
                .collect(Collectors.groupingBy(
                        InventoryItem::getProductModel,
                        LinkedHashMap::new, // 순서를 보장하는 LinkedHashMap 사용을 명시합니다.
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    ProductModel model = entry.getKey();
                    long count = entry.getValue();

                    return new ProductGroupDto(model.getId(), model.getName(), model.getThumbnailUrl(), count);
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
                log.info("ProductService|createProductModel|storedFilePath|이미지 저장 경로 : {}", storedFilePath);

                if (storedFilePath != null) {
                    // 업로드 순서(i)를 그대로 displayOrder로 사용
                    ContentImage contentImage = ContentImage.builder()
                            .contentGroup(imageGroup)
                            .imageUrl(storedFilePath)
                            .displayOrder(i) // 0번째가 대표, 1번째가 추가이미지1, ...
                            .build();
                    contentImageRepository.save(contentImage);

                    // 첫 번째 이미지의 URL을 thumbnailUrl 필드에 업데이트
                    if (i == 0) {
                        newModel.updateThumbnailUrl(storedFilePath);
                    }
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

    /**
     * ID로 상품 모델 엔티티를 조회합니다.(관리자>상품관리>수정)
     * @param modelId 상품 모델 ID
     * @return 찾은 ProductModel 엔티티
     */
    public ProductModel findModelById(Long modelId) {
        return productModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("상품 모델을 찾을 수 없습니다: " + modelId));
    }

    /**
     * 기존 상품 모델의 정보를 수정합니다.
     * @param modelId 수정할 모델의 ID
     * @param dto 폼에서 받은 새로운 데이터
     * @return 수정된 ProductModel 엔티티
     */
    @Transactional
    public ProductModel updateProductModel(Long modelId, ProductModelFormDto dto, List<MultipartFile> newImageFiles) {
        // 1. ID로 기존 모델을 조회합니다.
        ProductModel modelToUpdate = productModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("상품 모델을 찾을 수 없습니다: " + modelId));

        // 2. DTO의 내용으로 엔티티의 필드를 업데이트합니다.
        // (주의: @Transactional 덕분에, 엔티티의 필드를 변경하는 것만으로 DB에 update 쿼리가 실행됩니다.)
        modelToUpdate.updateDetails(
                dto.getName(), dto.getDescription(),
                dto.getDailyRate(), dto.getMonthlyRate(), dto.getDeposit(),
                dto.getIncludedItems(), dto.getNotIncludedItems(),
                dto.getUsageGuide(), dto.getCancellationPolicy(),
                dto.getIsActive()
        );

        // 3. 이미지 그룹 찾기
        ContentGroup imageGroup = contentGroupRepository.findByGroupKey("MODEL_" + modelId + "_IMAGES")
                .orElseThrow(() -> new IllegalStateException("이미지 그룹을 찾을 수 없습니다."));

        // 4. 기존에 DB에 저장된 이미지 목록 조회
        List<ContentImage> existingImages = contentImageRepository.findByContentGroupOrderByDisplayOrderAsc(imageGroup);

        // 4. 삭제해야 할 이미지 처리
        List<String> finalImageUrls = new ArrayList<>();        // 최종 이미지 URL 목록을 담을 변수
        if(dto.getExistingImageUrls() != null) {
            finalImageUrls.addAll(dto.getExistingImageUrls());
        }

        List<ContentImage> imagesToDelete = new ArrayList<>();
        for (ContentImage existingImage : existingImages) {

            // 기존 이미지 URL이 최종 이미지 목록에 없다면 삭제 대상에 추가
            if (!finalImageUrls.contains(existingImage.getImageUrl())) {
                imagesToDelete.add(existingImage);

                // 실제 파일도 삭제
                 fileStorageService.deleteFile(existingImage.getImageUrl());
            }

        }

        // 5. 삭제 대상 이미지들을 DB에서 삭제합니다.
        contentImageRepository.deleteAll(imagesToDelete);

        // 6. 남아있는 기존 이미지들의 순서를 업데이트 (for-each -> indexed for loop)
        for (int i = 0; i < finalImageUrls.size(); i++) {

            final int displayOrder = i;
            String imageUrl = finalImageUrls.get(i); // 최종 이미지 URL 목록을 담은 변수에서 i순번의 이미지 URL을 가져옵니다.

            // imageUrl로 DB에서 ContentImage 엔티티를 찾고, 있다면(ifPresent) 아래 로직을 실행합니다.
            // contentImageRepository.findByImageUrl의 반환객체는 Optional<ContentImage>이므로 람다식에서 image는 ContentImage 타입입니다.
            contentImageRepository.findByImageUrl(imageUrl).ifPresent(image -> {
                image.updateDisplayOrder(displayOrder); // @Transactional이 자동으로 UPDATE 쿼리를 실행 ★★★
            });
        }

        // 7. 새로 제출된 이미지 파일들을 저장합니다.
        int nextDisplayOrder = finalImageUrls.size();
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            for (MultipartFile file : newImageFiles) {
                String storedFilePath = fileStorageService.storeFile(file, "product", modelId.toString());
                if (storedFilePath != null) {
                    ContentImage contentImage = ContentImage.builder()
                            .contentGroup(imageGroup)
                            .imageUrl(storedFilePath)
                            .displayOrder(nextDisplayOrder)
                            .build();
                    contentImageRepository.save(contentImage);
                    nextDisplayOrder++;
                }
            }
        }

        // 8. 대표 이미지 URL 업데이트
        contentImageRepository.findFirstByContentGroupAndDisplayOrder(imageGroup, 0)
                .ifPresentOrElse(
                        image -> modelToUpdate.updateThumbnailUrl(image.getImageUrl()),
                        () -> modelToUpdate.updateThumbnailUrl(null)
                );


        return modelToUpdate;
    }

    /**
     * 상품 모델과 관련된 모든 데이터를 삭제합니다.
     * @param modelId 삭제할 상품 모델의 ID
     */
    @Transactional
    public void deleteProductModel(Long modelId) {
        // 1. 삭제할 ProductModel 엔티티를 찾습니다.
        ProductModel modelToDelete = productModelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("상품 모델을 찾을 수 없습니다: " + modelId));

        // 2. 이 모델에 대한 예약이 하나라도 존재하면 삭제를 막습니다.
        if (reservationRepository.existsByInventoryItem_ProductModel(modelToDelete)) {
            throw new IllegalStateException("이 상품 모델에 대한 예약이 존재하여 삭제할 수 없습니다.");
        }

        // 3. 연결된 이미지 그룹과 이미지 파일들을 삭제합니다.
        contentGroupRepository.findByGroupKey("MODEL_" + modelId + "_IMAGES").ifPresent(group -> {
            List<ContentImage> images = contentImageRepository.findByContentGroupOrderByDisplayOrderAsc(group);
            for (ContentImage image : images) {
                fileStorageService.deleteFile(image.getImageUrl()); // 실제 파일 삭제
            }
            contentImageRepository.deleteAll(images); // DB 기록 삭제
            contentGroupRepository.delete(group); // 그룹 삭제
        });

        // 4. 연결된 모든 재고(InventoryItem)를 삭제합니다.
        List<InventoryItem> itemsToDelete = inventoryItemRepository.findByProductModel(modelToDelete);
        inventoryItemRepository.deleteAll(itemsToDelete);

        // 5. 마지막으로 ProductModel을 삭제합니다.
        productModelRepository.delete(modelToDelete);
    }

}
