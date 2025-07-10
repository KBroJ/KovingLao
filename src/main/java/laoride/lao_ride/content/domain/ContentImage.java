package laoride.lao_ride.content.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private ContentGroup contentGroup;

    @Column(nullable = false)
    private String imageUrl;

    private String linkUrl;

    @Column(nullable = false)
    private int displayOrder;

    /**
     * 빌더 패턴을 위한 생성자
     */
    @Builder
    public ContentImage(ContentGroup contentGroup, String imageUrl, String linkUrl, int displayOrder) {
        this.contentGroup = contentGroup;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.displayOrder = displayOrder;
    }

    /**
     * 이미지의 표시 순서를 업데이트하는 메서드
     * @param displayOrder 새로운 표시 순서
     */
    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

}
