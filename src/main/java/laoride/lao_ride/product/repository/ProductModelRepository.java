package laoride.lao_ride.product.repository;

import laoride.lao_ride.product.domain.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {

    // 상품 상태가 'ACTIVE'이고, ID가 특정 목록에 포함되지 않은 상품들을 조회
    List<ProductModel> findByIsActiveAndIdNotIn(boolean isActive, List<Long> ids);

    List<ProductModel> findByIsActive(boolean isActive);

    // 상품 이름으로 상품을 조회하는 메서드
    Optional<ProductModel> findByName(String name);

}
