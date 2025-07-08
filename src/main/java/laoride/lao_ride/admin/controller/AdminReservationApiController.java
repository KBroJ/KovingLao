package laoride.lao_ride.admin.controller;

import laoride.lao_ride.admin.dto.AdminReservationListDto;
import laoride.lao_ride.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationApiController {

    private final ReservationService reservationService;

    @GetMapping
    public List<AdminReservationListDto> getAllReservations() {

        /*
            1. .stream() : 재료 투입
                reservationService.findAllReservations()를 통해 List<Reservation>이라는 DB 엔티티 묶음을 가져옵니다.
                이 엔티티 묶음을 '변환용 컨베이어 벨트'에 Reservation 객체 하나하나씩 올리는 역할

            2. .map(AdminReservationListDto::new) : 가공
                2-1) .map() :
                    컨베이어 벨트를 지나가는 내용물을 다른 형태로 변환(매핑) 하는 기계
                2-2) AdminReservationListDto::new :
                    '메서드 참조(Method Reference)' 라는 Java 8 문법으로,
                    reservation -> new AdminReservationListDto(reservation) 코드의 축약형

                즉, 컨베이어 벨트를 지나가는 Reservation 객체 각각을 AdminReservationListDto의 생성자에 넣어
                    새로운 AdminReservationListDto 객체로 변환하라는 의미

            3. .collect(Collectors.toList()) : 포장
                3-1) .collect() :
                    컨베이어 벨트 끝에서 가공이 완료된 결과물들을 모으는 '최종 처리(Terminal Operation)' 단계
                3-2) Collectors.toList() :
                    "모든 결과물을 새로운 List에 담아서 포장해줘" 라는 의미

                결과적으로, 변환이 완료된 AdminReservationListDto 객체들이 담긴 새로운 List가 최종적으로 반환됩니다.

          ====================================================================================================
              다른 방식(for-each)

                // 1. 서비스에서 Reservation 엔티티 리스트를 가져옵니다.
                List<Reservation> reservationEntities = reservationService.findAllReservations();

                // 2. DTO를 담을 빈 리스트를 새로 만듭니다.
                List<AdminReservationListDto> dtoList = new ArrayList<>();

                // 3. 가져온 엔티티 리스트를 하나씩 순회합니다.
                for (Reservation reservation : reservationEntities) {
                    // 4. 각 엔티티를 사용해 DTO 객체를 새로 만듭니다.
                    AdminReservationListDto dto = new AdminReservationListDto(reservation);
                    // 5. 새로 만든 DTO를 빈 리스트에 추가합니다.
                    dtoList.add(dto);
                }

                // 6. 모든 변환이 끝난 리스트를 반환합니다.
                return dtoList;
        */
        return reservationService.findAllReservations().stream()
                .map(AdminReservationListDto::new)
                .collect(Collectors.toList());
    }

}
