package laoride.lao_ride.site_content.repository;

import laoride.lao_ride.product.domain.SiteContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteContentRepository extends JpaRepository<SiteContent, Long> {

    // 여러 contentKey에 해당하는 데이터를 한 번의 쿼리로 조회
    List<SiteContent> findByContentKeyIn(List<String> keys);

}
