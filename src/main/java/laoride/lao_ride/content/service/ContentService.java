package laoride.lao_ride.content.service;

import laoride.lao_ride.content.dto.ContentImageDto;
import laoride.lao_ride.content.repository.ContentImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

    private final ContentImageRepository contentImageRepository;

    public List<ContentImageDto> findImagesByGroup(String groupKey) {

        // 1. DB에서 이미지 목록 조회
        List<ContentImageDto> images = contentImageRepository.findByContentGroup_GroupKeyOrderByDisplayOrderAsc(groupKey)
                .stream()
                .map(ContentImageDto::new)
                .collect(Collectors.toList());

        log.info("Found {} images from DB for groupKey: {}", images.size(), groupKey);

        // 2. 조회된 목록이 비어있는지 확인
        if (images.isEmpty()) {
            log.warn("No images found for groupKey: {}. Returning default image.", groupKey);

            // 3. 비어있다면, 기본 이미지 DTO를 생성해서 목록에 추가
            if ("MAIN_BANNER".equals(groupKey)) {
                images.add(new ContentImageDto("/images/layout/default-banner.png", null));
            } else if ("STORE_INTRO".equals(groupKey)) {
                images.add(new ContentImageDto("/images/layout/default-store.png", null));
            }
        }

        // 4. 이미지가 하나라도 포함된 목록을 반환
        return images;
    }

}
