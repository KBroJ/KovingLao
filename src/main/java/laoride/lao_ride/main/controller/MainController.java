package laoride.lao_ride.main.controller;

import laoride.lao_ride.site_content.service.SiteContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final SiteContentService siteContentService;

    // 메인 페이지를 위한 뷰 컨트롤러
    @GetMapping("/")
    public String mainPage(Model model) {

        // 1. 메인 페이지에 필요한 콘텐츠 키 목록 정의
        List<String> contentKeys = List.of("MAIN_BANNER_IMG", "STORE_INTRO_IMG");

        // 2. 서비스 계층을 통해 데이터 조회
        Map<String, String> contents = siteContentService.findContentsByKeys(contentKeys);

        // 3. Model에 담아서 View로 전달
        model.addAttribute("mainBannerUrl", contents.getOrDefault("MAIN_BANNER_IMG", "/images/layout/default-banner.png"));
        model.addAttribute("storeIntroUrl", contents.getOrDefault("STORE_INTRO_IMG", "/images/layout/default-store.png"));

        return "main";
    }

}
