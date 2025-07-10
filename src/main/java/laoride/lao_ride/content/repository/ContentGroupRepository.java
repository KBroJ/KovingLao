package laoride.lao_ride.content.repository;

import laoride.lao_ride.content.domain.ContentGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContentGroupRepository extends JpaRepository<ContentGroup, Long> {

    /**
     * 고유한 groupKey를 사용하여 ContentGroup 엔티티를 조회합니다.
     * @param groupKey 조회할 콘텐츠 그룹의 키
     * @return 조회된 ContentGroup (Optional)
     */
    Optional<ContentGroup> findByGroupKey(String groupKey);

}
