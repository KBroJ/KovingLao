package laoride.lao_ride.main.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BikeModelDto {

    private String name;
    private String imageUrl;
    private long availableCount; // 이용 가능한 대수

}
