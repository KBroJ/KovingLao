package laoride.lao_ride.reservation.service;

import laoride.lao_ride.product.domain.InventoryItem;
import laoride.lao_ride.product.domain.InventoryItemStatus;
import laoride.lao_ride.product.domain.ProductModel;
import laoride.lao_ride.product.repository.InventoryItemRepository;
import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.product.repository.ProductModelRepository;
import laoride.lao_ride.reservation.dto.ReservationLookupDto;
import laoride.lao_ride.reservation.repository.ReservationRepository;
import laoride.lao_ride.reservation.dto.ReservationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ProductModelRepository productModelRepository;
    private final InventoryItemRepository inventoryItemRepository;


    /**
     * 예약 요청 DTO를 받아 예약을 생성하고 데이터베이스에 저장합니다.
     * @param dto 예약 폼에서 전송된 데이터
     * @return 저장된 Reservation 엔티티
     */
    @Transactional // 데이터 변경이 일어나므로 @Transactional 어노테이션을 붙입니다.
    public Reservation createReservation(ReservationRequestDto dto) {

        LocalDate startDate = LocalDate.parse(dto.getStartDate());
        LocalDate endDate = LocalDate.parse(dto.getEndDate());

        log.info("Creating reservation for model: {}", dto.getModelName());

        // 1. 상품 이름으로 Product 엔티티를 조회합니다.
        ProductModel productModel = productModelRepository.findByName(dto.getModelName())
                .orElseThrow(() -> new IllegalArgumentException("해당 모델을 찾을 수 없습니다: " + dto.getModelName()));

        // 2. [신규] 해당 기간에 대여 가능한 '개별 재고(InventoryItem)'를 찾습니다.
        // 2-1. 먼저 해당 기간에 이미 예약된 재고들의 ID 목록을 가져옵니다.
        List<Long> unavailableItemIds = reservationRepository.findUnavailableItemIds(startDate, endDate);

        // 2-2. 예약된 재고들을 제외하고, 'AVAILABLE' 상태인 재고를 하나 찾아옵니다.
        Optional<InventoryItem> availableItemOpt;
        if (unavailableItemIds.isEmpty()) {
            availableItemOpt = inventoryItemRepository.findFirstByProductModelAndStatus(
                    productModel, InventoryItemStatus.AVAILABLE);
        } else {
            availableItemOpt = inventoryItemRepository.findFirstByProductModelAndIdNotInAndStatus(
                    productModel, unavailableItemIds, InventoryItemStatus.AVAILABLE);
        }

        // 2-3. 만약 가능한 재고가 없다면 에러를 발생시킵니다.
        InventoryItem itemToReserve = availableItemOpt
                .orElseThrow(() -> new IllegalStateException("선택하신 날짜에 이용 가능한 재고가 없습니다."));


        // 3. 요금을 계산합니다. (일수 * 모델의 일일 요금)
        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal totalPrice = productModel.getDailyRate().multiply(BigDecimal.valueOf(rentalDays));

        // 4. 예약 코드 생성
        // 예: "LR" + 8자리 영문 대문자/숫자 조합 -> "LR-A4B1C8D2"
        String randomChars = RandomStringUtils.randomAlphanumeric(8).toUpperCase();
        String reservationCode = "LR-" + randomChars;

        log.info("Creating reservation| startDate : {}, endDate : {}, rentalDays : {}, totalPrice : {}, reservationCode : {}, ",
                startDate, endDate, rentalDays, totalPrice, reservationCode
        );

        // 5. 전달받은 모든 정보를 사용하여 Reservation 엔티티를 생성합니다.
        Reservation reservation = Reservation.builder()
                .reservationCode(reservationCode)
                .inventoryItem(itemToReserve)
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

        // 6. 생성된 예약 엔티티를 DB에 저장하고 반환합니다.
        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", savedReservation.getId());

        // TODO: 예약 확정 이메일 발송 로직 추가

        return savedReservation;
    }

    // 예약내역조회
    public Reservation findReservationByCodeAndEmail(ReservationLookupDto dto) {
        // 1. 예약 코드로 예약을 찾는다.
        Reservation reservation = reservationRepository.findByReservationCode(dto.getReservationCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 예약 번호를 찾을 수 없습니다."));

        // 2. 찾은 예약의 이메일과 입력된 이메일이 일치하는지 확인한다. (보안 핵심)
        if (!reservation.getCustomerEmail().equalsIgnoreCase(dto.getCustomerEmail())) {
            throw new IllegalArgumentException("예약자 정보가 일치하지 않습니다.");
        }

        return reservation;
    }

    /**
     * 모든 예약 목록을 최신순으로 조회합니다.
     * @return 모든 Reservation 엔티티 리스트
     */
    public List<Reservation> findAllReservations() {
        // ID 역순으로 정렬하여 최신 예약이 위로 오도록 함
        return reservationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

}
