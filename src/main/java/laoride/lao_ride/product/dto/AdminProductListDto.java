package laoride.lao_ride.product.dto;

import laoride.lao_ride.product.domain.ProductModel;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class AdminProductListDto {

    private final Long id;
    private final String name;
    private final String thumbnailUrl;
    private final BigDecimal dailyRate;
    private final BigDecimal monthlyRate;
    private final long totalQuantity;       // 보유 대수
    private final long availableQuantity;   // 대여 가능 대수
    private final boolean isActive;         // 모델 판매 여부

    // 서비스 레이어에서 계산된 값을 받아 DTO를 생성
    public AdminProductListDto(ProductModel model, long totalQuantity, long availableQuantity) {
        this.id = model.getId();
        this.name = model.getName();
        this.thumbnailUrl = model.getThumbnailUrl();
        this.dailyRate = model.getDailyRate();
        this.monthlyRate = model.getMonthlyRate();
        this.isActive = model.isActive();
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }

}
