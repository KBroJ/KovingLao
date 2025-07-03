package laoride.lao_ride.site_content.service;

import laoride.lao_ride.product.domain.SiteContent;
import laoride.lao_ride.site_content.repository.SiteContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SiteContentService {

    private final SiteContentRepository siteContentRepository;

    public Map<String, String> findContentsByKeys(List<String> keys) {
        // key 목록으로 DB에서 데이터를 조회한 후, Map<Key, Value> 형태로 변환하여 반환
        return siteContentRepository.findByContentKeyIn(keys).stream()
                .collect(Collectors.toMap(SiteContent::getContentKey, SiteContent::getContentValue));
    }

}
