package laoride.lao_ride.product.dto;

import laoride.lao_ride.content.domain.ContentImage;
import laoride.lao_ride.product.domain.ProductModel;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자에게 보여줄 상품 모델의 모든 상세 정보를 담는 DTO
 */
@Getter
public class ProductDetailDto {

    private final Long id;
    private final String name;
    private final String description;
    private final List<String> imageUrls; // 상품 이미지 목록
    private final String includedItems;
    private final String notIncludedItems;
    private final String usageGuide;
    private final String cancellationPolicy;

    /**
     * ProductModel 엔티티와 ContentImage 엔티티 리스트를 받아 DTO의 필드를 초기화합니다.
     * 이 생성자는 private으로 선언하여, 항상 from() 메서드를 통해서만 객체가 생성되도록 유도합니다.
     * @param model 상품 모델 엔티티
     * @param images 해당 모델에 속한 이미지 엔티티 리스트
     */
    private ProductDetailDto(ProductModel model, List<ContentImage> images) {
        this.id = model.getId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.imageUrls = images.stream()
                .map(ContentImage::getImageUrl)
                .collect(Collectors.toList());
        this.includedItems = model.getIncludedItems();
        this.notIncludedItems = model.getNotIncludedItems();
        this.usageGuide = model.getUsageGuide();
        this.cancellationPolicy = model.getCancellationPolicy();
    }

    /**
     * 엔티티 객체들을 DTO로 변환하는 정적 팩토리 메서드(static factory method)입니다.
     * 서비스 로직 등 외부에서는 이 메서드를 호출하여 DTO 객체를 생성합니다.
     * @param model 상품 모델 엔티티
     * @param images 해당 모델에 속한 이미지 엔티티 리스트
     * @return 변환된 ProductDetailDto 객체
     */
    public static ProductDetailDto from(ProductModel model, List<ContentImage> images) {
        return new ProductDetailDto(model, images);
    }

}
