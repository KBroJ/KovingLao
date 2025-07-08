package laoride.lao_ride.admin.dto;

import laoride.lao_ride.reservation.domain.Reservation;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AdminReservationListDto {

    private final Long id;
    private final String reservationCode;
    private final String customerName;
    private final String customerPhone;
    private final String productName;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String status;

    public AdminReservationListDto(Reservation reservation) {
        this.id = reservation.getId();
        this.reservationCode = reservation.getReservationCode();
        this.customerName = reservation.getCustomerName();
        this.customerPhone = reservation.getCustomerPhone();
        this.productName = reservation.getProduct().getName(); // LAZY 로딩 주의
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
        this.status = reservation.getStatus();
    }

}
