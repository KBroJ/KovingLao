package laoride.lao_ride.product.repository;

import laoride.lao_ride.product.domain.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

    // 상품 ID로 가장 최근에 적용된 가격 정보 1개를 조회
    Optional<ProductPrice> findFirstByProductIdOrderByEffectiveDateDesc(Long productId);

}
