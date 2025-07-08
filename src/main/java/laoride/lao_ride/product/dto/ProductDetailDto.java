package laoride.lao_ride.product.dto;

import laoride.lao_ride.content.domain.ContentImage;
import laoride.lao_ride.product.domain.ProductModel;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

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

    // private 생성자
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

    // 정적 팩토리 메서드
    public static ProductDetailDto from(ProductModel model, List<ContentImage> images) {
        return new ProductDetailDto(model, images);
    }

}
