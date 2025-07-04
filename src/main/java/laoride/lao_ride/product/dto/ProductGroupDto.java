package laoride.lao_ride.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ProductGroupDto {

    private final Long id; // [추가] 상품 ID
    private final String name;
    private final String imageUrl;
    private final long availableCount;

    public ProductGroupDto(Long id, String name, String imageUrl, long availableCount) {
        this.id = id; // [추가]
        this.name = name;
        this.imageUrl = imageUrl;
        this.availableCount = availableCount;
    }

}
