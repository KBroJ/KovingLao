package laoride.lao_ride.reservation.service;

import laoride.lao_ride.product.domain.Product;
import laoride.lao_ride.product.domain.Reservation;
import laoride.lao_ride.product.repository.ProductRepository;
import laoride.lao_ride.product.repository.ReservationRepository;
import laoride.lao_ride.reservation.dto.ReservationRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;

    @Transactional
    public Reservation createReservation(ReservationRequestDto dto) {
        // 상품 조회
        Product product = productRepository.findByName(dto.getModelName())
                .orElseThrow(() -> new IllegalArgumentException("해당 모델을 찾을 수 없습니다."));

        // 가격 계산
        long days = ChronoUnit.DAYS.between(LocalDate.parse(dto.getStartDate()), LocalDate.parse(dto.getEndDate())) + 1;
        BigDecimal price = calculatePrice(product.getId(), days);

        // 예약 엔티티 생성 및 저장
        Reservation reservation = Reservation.builder()
                .product(product)
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getEmail())
                .customerPhone(dto.getPhone())
                .startDate(LocalDate.parse(dto.getStartDate()))
                .endDate(LocalDate.parse(dto.getEndDate()))
                .totalPrice(price)
                .status("PENDING") // 초기 상태는 '확정 대기'
                .build();

        return reservationRepository.save(reservation);
    }

    // 이 메서드는 예시이며, 실제로는 더 복잡한 가격 정책이 필요할 수 있습니다.
    private BigDecimal calculatePrice(Long productId, long days) {
        return productPriceRepository.findFirstByProductIdOrderByEffectiveDateDesc(productId)
                .map(priceInfo -> priceInfo.getDailyRate().multiply(BigDecimal.valueOf(days)))
                .orElse(BigDecimal.ZERO);
    }

}
