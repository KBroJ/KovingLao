package laoride.lao_ride.admin.controller;

import laoride.lao_ride.product.domain.ProductModel;
import laoride.lao_ride.product.dto.AdminProductListDto;
import laoride.lao_ride.product.dto.ProductModelFormDto;
import laoride.lao_ride.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    /**
     * [추가] 새로운 상품 모델을 생성하는 API
     * @param formDto 폼에서 전송된 JSON 데이터
     * @return 생성된 모델의 ID와 성공 메시지를 담은 ResponseEntity
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestBody ProductModelFormDto formDto) {
        ProductModel savedModel = productService.createProductModel(formDto);

        Map<String, Object> response = Map.of(
                "message", "상품 모델이 성공적으로 등록되었습니다.",
                "modelId", savedModel.getId()
        );

        // RESTful API에서는 리소스가 성공적으로 생성되었을 때 '201 Created' 상태 코드를 반환하는 것이 표준입니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
