package laoride.lao_ride.product.repository;

import laoride.lao_ride.product.domain.InventoryItem;
import laoride.lao_ride.product.domain.InventoryItemStatus;
import laoride.lao_ride.product.domain.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // ProductModel을 기준으로 재고 개수를 세는 쿼리 메서드
    long countByProductModel(ProductModel productModel);

    // ProductModel과 Status를 기준으로 재고 개수를 세는 쿼리 메서드
    long countByProductModelAndStatus(ProductModel productModel, InventoryItemStatus status);

    // 특정 모델에 속하면서, ID 목록에 포함되지 않고, 상태가 'AVAILABLE'인 재고를 하나 찾음
    Optional<InventoryItem> findFirstByProductModelAndIdNotInAndStatus(
            ProductModel model, List<Long> unavailableIds, InventoryItemStatus status);

    // ID 목록이 비어있을 경우를 위한 메서드
    Optional<InventoryItem> findFirstByProductModelAndStatus(
            ProductModel model, InventoryItemStatus status);

    // 사용 불가능한 ID 목록을 제외하고, 상태가 AVAILABLE인 모든 재고를 조회
    List<InventoryItem> findByStatusAndIdNotIn(InventoryItemStatus status, List<Long> unavailableIds);

    // 상태가 AVAILABLE인 모든 재고를 조회 (예약이 하나도 없을 경우 사용)
    List<InventoryItem> findByStatus(InventoryItemStatus status);

}
