package laoride.lao_ride.product.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductAvailabilityDto {

    private final long availableCount;
    private final BigDecimal price;

    public ProductAvailabilityDto(long availableCount, BigDecimal price) {
        this.availableCount = availableCount;
        this.price = price;
    }

}
