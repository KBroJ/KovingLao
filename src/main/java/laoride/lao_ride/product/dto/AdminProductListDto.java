package laoride.lao_ride.product.dto;

import laoride.lao_ride.product.domain.ProductModel;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminProductListDto {

    private final Long id;
    private final String name;
    private final String status;
    private final String imageUrl;
    private final LocalDateTime createdAt;

    public AdminProductListDto(ProductModel productModel) {
        this.id = productModel.getId();
        this.name = productModel.getName();
        this.status = productModel.getStatus();
        this.imageUrl = productModel.getImageUrl();
        this.createdAt = productModel.getCreatedAt();
    }

}
