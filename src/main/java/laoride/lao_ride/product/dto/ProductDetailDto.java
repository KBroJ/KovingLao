package laoride.lao_ride.product.dto;

import laoride.lao_ride.content.domain.ContentImage;
import laoride.lao_ride.product.domain.Product;
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
    private ProductDetailDto(Product product, List<ContentImage> images) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.imageUrls = images.stream()
                .map(ContentImage::getImageUrl)
                .collect(Collectors.toList());
        this.includedItems = product.getIncludedItems();
        this.notIncludedItems = product.getNotIncludedItems();
        this.usageGuide = product.getUsageGuide();
        this.cancellationPolicy = product.getCancellationPolicy();
    }

    // 정적 팩토리 메서드: 엔티티 객체들을 받아 DTO를 생성하는 역할
    public static ProductDetailDto from(Product product, List<ContentImage> images) {
        return new ProductDetailDto(product, images);
    }

}
