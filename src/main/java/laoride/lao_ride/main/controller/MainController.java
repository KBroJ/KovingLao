package laoride.lao_ride.main.controller;

import laoride.lao_ride.content.dto.ContentImageDto;
import laoride.lao_ride.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ContentService contentService;

    // 메인 페이지를 위한 뷰 컨트롤러
    @GetMapping("/")
    public String mainPage(Model model) {

        // 'MAIN_BANNER' 그룹의 이미지 목록 조회
        List<ContentImageDto> bannerImages = contentService.findImagesByGroup("MAIN_BANNER");
        // 'STORE_INTRO' 그룹의 이미지 목록 조회
        List<ContentImageDto> storeImages = contentService.findImagesByGroup("STORE_INTRO");

        model.addAttribute("bannerImages", bannerImages);
        model.addAttribute("storeImages", storeImages);

        return "main";

    }

}
