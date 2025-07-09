package laoride.lao_ride.content.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String groupKey;

    private String description;

    @Builder
    public ContentGroup(String groupKey, String description) {
        this.groupKey = groupKey;
        this.description = description;
    }

}
