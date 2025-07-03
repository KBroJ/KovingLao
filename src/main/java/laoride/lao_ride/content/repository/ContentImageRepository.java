package laoride.lao_ride.content.repository;

import laoride.lao_ride.content.domain.ContentImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentImageRepository extends JpaRepository<ContentImage, Long> {

    // GroupKey로 이미지를 찾고, displayOrder 순으로 정렬하는 메서드
    List<ContentImage> findByContentGroup_GroupKeyOrderByDisplayOrderAsc(String groupKey);

}
