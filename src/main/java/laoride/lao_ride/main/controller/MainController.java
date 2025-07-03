package laoride.lao_ride.main.controller;

import laoride.lao_ride.product.dto.ProductSummaryDto;
import laoride.lao_ride.product.dto.ProductGroupDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MainController {

    // 메인 페이지를 위한 뷰 컨트롤러
    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

}
