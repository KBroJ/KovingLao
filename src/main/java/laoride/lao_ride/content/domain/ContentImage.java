package laoride.lao_ride.content.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
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

    private int displayOrder;

    @Builder
    public ContentImage(ContentGroup contentGroup, String imageUrl, String linkUrl, int displayOrder ) {
        this.contentGroup = contentGroup;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.displayOrder = displayOrder;
    }

}
