package laoride.lao_ride.product.repository;

import laoride.lao_ride.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 상품 상태가 'ACTIVE'이고, ID가 특정 목록에 포함되지 않은 상품들을 조회
    List<Product> findByStatusAndIdNotIn(String status, List<Long> ids);

    List<Product> findByStatus(String status);

}
