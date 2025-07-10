package laoride.lao_ride.admin.controller;

import laoride.lao_ride.product.domain.ProductModel;
import laoride.lao_ride.product.dto.AdminProductListDto;
import laoride.lao_ride.product.dto.ProductModelFormDto;
import laoride.lao_ride.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductApiController {

    private final ProductService productService;

    /**
     *
     */
    @GetMapping
    public List<AdminProductListDto> getAllProducts() {
        return productService.getProductModelSummaries();
    }

    /**
     * 새로운 상품 모델을 생성하는 API(관리자>상품관리>새 상품 모델 등록)
     * @param formDto 폼에서 전송된 JSON 데이터와 이미지 파일을 받음
     * @return 생성된 모델의 ID와 성공 메시지를 담은 ResponseEntity
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestPart("formDto") ProductModelFormDto formDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles
    ) {
        ProductModel savedModel = productService.createProductModel(formDto, imageFiles);

        Map<String, Object> response = Map.of(
                "message", "상품 모델이 성공적으로 등록되었습니다.",
                "modelId", savedModel.getId()
        );

        // RESTful API에서는 리소스가 성공적으로 생성되었을 때 '201 Created' 상태 코드를 반환하는 것이 표준입니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 기존 상품 모델을 수정하는 API
     * @param id 수정할 모델의 ID
     * @param formDto 폼에서 전송된 JSON 데이터
     * @return 성공 메시지를 담은 ResponseEntity
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable("id") Long id,
            @RequestPart("formDto") ProductModelFormDto formDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles
    ) {

        productService.updateProductModel(id, formDto, imageFiles);

        Map<String, Object> response = Map.of("message", "상품 모델이 성공적으로 수정되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * 상품 모델을 삭제하는 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProductModel(id);
        return ResponseEntity.ok(Map.of("message", "상품 모델이 성공적으로 삭제되었습니다."));
    }

}
