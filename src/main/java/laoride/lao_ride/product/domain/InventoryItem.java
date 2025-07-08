package laoride.lao_ride.product.domain;

import jakarta.persistence.*;
import laoride.lao_ride.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private ProductModel productModel;

    @Column(unique = true, nullable = false)
    private String managementCode; // 관리 번호 (예: KBA-001)

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열로 저장
    @Column(nullable = false)
    private InventoryItemStatus status;

    private String note; // 비고

    /**
     * 빌더 패턴을 위한 생성자
     */
    @Builder
    public InventoryItem(ProductModel productModel, String managementCode, InventoryItemStatus status, String note) {
        this.productModel = productModel;
        this.managementCode = managementCode;
        this.status = status;
        this.note = note;
    }

}
