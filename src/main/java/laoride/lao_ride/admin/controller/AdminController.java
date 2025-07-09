package laoride.lao_ride.admin.controller;

import laoride.lao_ride.admin.dto.AdminDashboardDto;
import laoride.lao_ride.product.domain.ProductModel;
import laoride.lao_ride.product.dto.ProductModelFormDto;
import laoride.lao_ride.product.service.ProductService;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ReservationService reservationService;
    private final ProductService productService;

    /**
     * 관리자 로그인 페이지를 보여줍니다.
     */
    @GetMapping("/login")
    public String loginPage() {
        return "admin/login"; // templates/admin/login.html 파일을 찾음
    }

    /**
     * 로그인 성공 후 보여줄 관리자 대시보드(메인) 페이지입니다.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("activeMenu", "dashboard");
        return "admin/dashboard"; // 콘텐츠 뷰 이름 반환
    }

    /**
     * 전체 예약 목록 페이지를 보여줍니다.
     */
    @GetMapping("/reservations")
    public String reservationListPage(Model model) {
        model.addAttribute("activeMenu", "reservations");
        return "admin/reservations";
    }

    // 상품 관리 페이지를 보여주는 메서드
    @GetMapping("/products")
    public String productListPage(Model model) {
        model.addAttribute("activeMenu", "products");
        return "admin/products";
    }

    /**
     * 새로운 상품 모델을 등록하는 폼 페이지를 보여줍니다.
     * @param model View에 데이터를 전달하기 위한 Model 객체
     * @return 상품 모델 등록 폼 템플릿 경로
     */
    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        // Thymeleaf의 th:object와 데이터 바인딩을 위해 빈 DTO 객체를 모델에 담아 전달합니다.
        model.addAttribute("productModelForm", new ProductModelFormDto());
        model.addAttribute("activeMenu", "products"); // 사이드바 활성화
        return "admin/product-form"; // templates/admin/product-form.html 파일을 찾음
    }

    /**
     * 기존 상품 모델의 정보를 수정하는 폼 페이지를 보여줍니다.
     * @param id 수정할 상품 모델의 ID
     * @param model View에 데이터를 전달하기 위한 Model 객체
     * @return 상품 모델 등록/수정 폼 템플릿 경로
     */
    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable("id") Long id, Model model) {
        // 1. 서비스로 기존 모델 데이터를 조회합니다.
        ProductModel productModel = productService.findModelById(id);

        // 2. 조회한 엔티티의 데이터를 폼 DTO로 옮겨 담습니다.
        ProductModelFormDto formDto = new ProductModelFormDto();

        formDto.setName(productModel.getName());
        formDto.setDescription(productModel.getDescription());
        formDto.setDailyRate(productModel.getDailyRate());
        formDto.setMonthlyRate(productModel.getMonthlyRate());
        formDto.setDeposit(productModel.getDeposit());
        formDto.setIncludedItems(productModel.getIncludedItems());
        formDto.setNotIncludedItems(productModel.getNotIncludedItems());
        formDto.setUsageGuide(productModel.getUsageGuide());
        formDto.setCancellationPolicy(productModel.getCancellationPolicy());

        formDto.setIsActive(productModel.isActive());
        // 초기 재고 수량은 수정 시에는 입력받지 않으므로 설정하지 않습니다.

        // 3. DTO를 모델에 담아 뷰에 전달합니다.
        model.addAttribute("productModelForm", formDto);
        model.addAttribute("activeMenu", "products");
        return "admin/product-form";
    }

}
