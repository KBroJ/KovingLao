package laoride.lao_ride.content.dto;

import laoride.lao_ride.content.domain.ContentImage;
import lombok.Getter;

@Getter
public class ContentImageDto {

    private String imageUrl;
    private String linkUrl;

    public ContentImageDto(ContentImage contentImage) {
        this.imageUrl = contentImage.getImageUrl();
        this.linkUrl = contentImage.getLinkUrl();
    }

    // 서비스에서 기본값을 직접 생성하기 위한 생성자(등록된 이미지가 없을 때)
    public ContentImageDto(String imageUrl, String linkUrl) {
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
    }

}
