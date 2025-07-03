package laoride.lao_ride.product.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import laoride.lao_ride.global.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자가 필요하므로 NoArgsConstructor 추가
public class SiteContent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "content_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String contentKey;

    @Lob // 긴 텍스트나 URL을 저장하기 위해 @Lob 사용
    @Column(columnDefinition = "TEXT", nullable = false, name = "content_value") // DB 타입을 TEXT로 명시
    private String contentValue;

    private String description;

    @Builder
    public SiteContent(String contentKey, String contentValue, String description) {
        this.contentKey = contentKey;
        this.contentValue = contentValue;
        this.description = description;
    }

}
