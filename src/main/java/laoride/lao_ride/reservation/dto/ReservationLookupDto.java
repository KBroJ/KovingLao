package laoride.lao_ride.reservation.dto;

import lombok.Getter;
import lombok.Setter;

// 사용자 - 예약내역조회
@Getter
@Setter
public class ReservationLookupDto {

    private String reservationCode;
    private String customerEmail;

}
