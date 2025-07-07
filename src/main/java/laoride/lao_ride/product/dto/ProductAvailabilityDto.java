package laoride.lao_ride.product.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductAvailabilityDto {

    private final long availableCount;
    private final BigDecimal price;
    private final BigDecimal deposit;

    public ProductAvailabilityDto(long availableCount, BigDecimal price, BigDecimal deposit) {
        this.availableCount = availableCount;
        this.price = price;
        this.deposit = deposit;
    }

}
