package laoride.lao_ride.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 상품 모델 생성 및 수정을 위한 폼 데이터를 담는 DTO
 */
@Getter
@Setter
public class ProductModelFormDto {

    private String name;
    private String description;
    private BigDecimal dailyRate;
    private BigDecimal monthlyRate;
    private BigDecimal deposit;
    private Integer maxRange;
    private String genderType;
    private String includedItems;
    private String notIncludedItems;
    private String usageGuide;
    private String cancellationPolicy;
    private Boolean isActive = true;    // 기본값을 '판매중'으로 설정
    private int initialQuantity;        // 모델 신규 등록 시 함께 생성할 초기 재고 수량
    private String representativeImageName; // 사용자가 선택한 대표 이미지의 원본 파일명

}
