package laoride.lao_ride.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationRequestDto {

    private String modelName;
    private String startDate;
    private String endDate;
    private String pickupTime;
    private String returnTime;

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String passportNumber;

}
