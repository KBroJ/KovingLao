package laoride.lao_ride.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class ProductGroupDto {

    private final Long id;
    private final String name;
    private final String thumbnailUrl;
    private final long availableCount;

    public ProductGroupDto(Long id, String name, String thumbnailUrl, long availableCount) {
        this.id = id;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.availableCount = availableCount;
    }

}
