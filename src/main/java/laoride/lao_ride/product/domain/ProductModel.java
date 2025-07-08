package laoride.lao_ride.product.domain;

import jakarta.persistence.*;
import jakarta.persistence.*;
import laoride.lao_ride.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "product_model") // 테이블명을 명확히 지정
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요하므로 NoArgsConstructor 추가
public class ProductModel extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "model_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob // 대용량 텍스트
    @Column(columnDefinition = "TEXT") // DB 타입을 TEXT로 명시
    private String description;

    private String imageUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate; // 일일 요금

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRate; // 월별 요금

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deposit; // 보증금

    // --- 향후 확장을 위한 필드 ---
    private Integer maxRange; // 최대 운행 거리 (km)
    private String genderType; // 'MALE', 'FEMALE', 'UNISEX'


    @Lob
    @Column(columnDefinition = "TEXT", name = "included_items")
    private String includedItems;

    @Lob
    @Column(columnDefinition = "TEXT", name = "not_included_items")
    private String notIncludedItems;

    @Lob
    @Column(columnDefinition = "TEXT", name = "usage_guide")
    private String usageGuide;

    @Lob
    @Column(columnDefinition = "TEXT", name = "cancellation_policy")
    private String cancellationPolicy;

    @Column(nullable = false)
    private boolean isActive; // 모델 판매 여부

}
