package laoride.lao_ride.reservation.controller;

import laoride.lao_ride.reservation.domain.Reservation;
import laoride.lao_ride.reservation.dto.ReservationDetailDto;
import laoride.lao_ride.reservation.dto.ReservationLookupDto;
import laoride.lao_ride.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;

    // 예약 정보를 조회하는 API
    @PostMapping("/lookup")
    public ReservationDetailDto lookupReservation(@RequestBody ReservationLookupDto lookupDto) {
        Reservation reservation = reservationService.findReservationByCodeAndEmail(lookupDto);

        // Reservation 엔티티를 그대로 반환하기보다, 필요한 정보만 담은 DTO를 만들어 반환하는 것이 좋음
        return new ReservationDetailDto(reservation);
    }

}
