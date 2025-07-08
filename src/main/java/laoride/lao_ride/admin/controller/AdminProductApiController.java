package laoride.lao_ride.admin.controller;

import laoride.lao_ride.product.dto.AdminProductListDto;
import laoride.lao_ride.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductApiController {

    private final ProductService productService;

    @GetMapping
    public List<AdminProductListDto> getAllProducts() {
        return productService.getProductModelSummaries();
    }

}
