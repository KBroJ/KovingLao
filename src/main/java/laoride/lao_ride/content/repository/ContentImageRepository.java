package laoride.lao_ride.content.repository;

import laoride.lao_ride.content.domain.ContentImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {

    /**
     * GroupKey로 이미지를 찾고, displayOrder 순으로 정렬하는 메서드
     */
    List<ContentImage> findByContentGroup_GroupKeyOrderByDisplayOrderAsc(String groupKey);

    /**
     * GroupKey와 displayOrder로 대표 이미지 하나만 찾는 메서드
     * @param groupKey 콘텐츠 그룹 키
     * @param displayOrder 표시 순서 (대표 이미지는 0)
     * @return 찾은 이미지 (없을 수도 있음)
     */
    Optional<ContentImage> findFirstByContentGroup_GroupKeyAndDisplayOrder(String groupKey, int displayOrder);

}
