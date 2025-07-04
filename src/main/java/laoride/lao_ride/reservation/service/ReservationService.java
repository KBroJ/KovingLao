package laoride.lao_ride.reservation.service;

import laoride.lao_ride.product.domain.Product;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.product.repository.ProductPriceRepository;
import laoride.lao_ride.product.repository.ProductRepository;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import laoride.lao_ride.reservation.dto.ReservationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;

    /**
     * 예약 요청 DTO를 받아 예약을 생성하고 데이터베이스에 저장합니다.
     * @param dto 예약 폼에서 전송된 데이터
     * @return 저장된 Reservation 엔티티
     */
    @Transactional // 데이터 변경이 일어나므로 @Transactional 어노테이션을 붙입니다.
    public Reservation createReservation(ReservationRequestDto dto) {

        log.info("Creating reservation for model: {}", dto.getModelName());

        // 1. 상품 이름으로 Product 엔티티를 조회합니다.
        Product product = productRepository.findByName(dto.getModelName())
                .orElseThrow(() -> new IllegalArgumentException("해당 모델을 찾을 수 없습니다: " + dto.getModelName()));

        // 2. 대여 기간(일)을 계산하고, 이를 바탕으로 총 가격을 계산합니다.
        LocalDate startDate = LocalDate.parse(dto.getStartDate());
        LocalDate endDate = LocalDate.parse(dto.getEndDate());
        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal totalPrice = calculatePrice(product.getId(), rentalDays);

        // 3. 전달받은 모든 정보를 사용하여 Reservation 엔티티를 생성합니다.
        Reservation reservation = Reservation.builder()
                .product(product)
                .customerName(dto.getLastName() + " " + dto.getFirstName())
                .customerEmail(dto.getEmail())
                .customerPhone(dto.getPhone())
                .startDate(startDate)
                .endDate(endDate)
                .pickupTime(LocalTime.parse(dto.getPickupTime()))
                .returnTime(LocalTime.parse(dto.getReturnTime()))
                .totalPrice(totalPrice)
                .status("PENDING") // 초기 상태는 '확정 대기'
                .build();

        // 4. 생성된 예약 엔티티를 DB에 저장하고 반환합니다.
        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", savedReservation.getId());

        // TODO: 예약 확정 이메일 발송 로직 추가

        return savedReservation;
    }

    /**
     * 상품 ID와 대여일수를 바탕으로 총 가격을 계산합니다.
     * @param productId 상품 ID
     * @param days 대여일수
     * @return 계산된 총 가격
     */
    private BigDecimal calculatePrice(Long productId, long days) {
        // 가장 최근에 적용된 가격 정보를 찾아, (1일 대여료 * 대여일수)를 계산합니다.
        return productPriceRepository.findFirstByProductIdOrderByEffectiveDateDesc(productId)
                .map(priceInfo -> priceInfo.getDailyRate().multiply(BigDecimal.valueOf(days)))
                .orElse(BigDecimal.ZERO); // 가격 정보가 없으면 0을 반환
    }

}
