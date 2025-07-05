package laoride.lao_ride.reservation.dto;

import laoride.lao_ride.reservation.domain.Reservation;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class ReservationDetailDto {

    private final String reservationCode;
    private final String customerName;
    private final String productName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime pickupTime;
    private final LocalTime returnTime;
    private final String status;
    private final BigDecimal totalPrice;

    // Reservation 엔티티를 받아서 필요한 데이터만 뽑아 DTO를 생성하는 생성자
    public ReservationDetailDto(Reservation reservation) {
        this.reservationCode = reservation.getReservationCode();
        this.customerName = reservation.getCustomerName();
        this.productName = reservation.getProduct().getName(); // 연관된 상품의 이름
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
        this.pickupTime = reservation.getPickupTime();
        this.returnTime = reservation.getReturnTime();
        this.status = reservation.getStatus();
        this.totalPrice = reservation.getTotalPrice();
    }

}
