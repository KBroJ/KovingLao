package laoride.lao_ride.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductGroupDto {

    private String name;
    private String imageUrl;
    private long availableCount; // 이용 가능한 대수

}
