package laoride.lao_ride.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 상품 모델 생성 및 수정을 위한 폼 데이터를 담는 DTO
 */
@Getter
@Setter
public class ProductModelFormDto {

    private Long id;
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
    private Boolean isActive = true;        // 기본값을 '판매중'으로 설정
    private int initialQuantity;            // 모델 신규 등록 시 함께 생성할 초기 재고 수량
    private List<String> existingImageUrls; // 수정 시, 최종적으로 화면에 남아있는 '기존' 이미지들의 URL 목록.

}
