package laoride.lao_ride.content.repository;

import laoride.lao_ride.content.domain.ContentGroup;
import laoride.lao_ride.content.domain.ContentImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {

    /**
     * GroupKey로 이미지를 찾고, displayOrder 오름차순으로 정렬하는 메서드
     */
    List<ContentImage> findByContentGroup_GroupKeyOrderByDisplayOrderAsc(String groupKey);

    /**
     * ContentGroup 객체로 이미지를 찾고, displayOrder 오름차순으로 정렬하는 메서드
     */
    List<ContentImage> findByContentGroupOrderByDisplayOrderAsc(ContentGroup contentGroup);

    /**
     * GroupKey와 displayOrder로 대표 이미지 하나만 찾는 메서드
     */
    Optional<ContentImage> findFirstByContentGroup_GroupKeyAndDisplayOrder(String groupKey, int displayOrder);

    /**
     * ContentGroup 객체와 displayOrder로 대표 이미지 하나만 찾는 메서드
     */
    Optional<ContentImage> findFirstByContentGroupAndDisplayOrder(ContentGroup contentGroup, int displayOrder);

    /**
     * 이미지 URL로 ContentImage를 찾는 메서드
     */
    Optional<ContentImage> findByImageUrl(String imageUrl);

}
