package laoride.lao_ride.product.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import laoride.lao_ride.global.entity.BaseTimeEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor // JPA는 기본 생성자가 필요하므로 NoArgsConstructor 추가
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob // 대용량 텍스트
    @Column(columnDefinition = "TEXT") // DB 타입을 TEXT로 명시
    private String description;

    private String imageUrl;

    @Column(nullable = false)
    private String status;

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

}
