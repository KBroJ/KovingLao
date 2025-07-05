package laoride.lao_ride.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import laoride.lao_ride.global.entity.BaseTimeEntity;
import laoride.lao_ride.product.domain.Product;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String reservationCode; // 고유 예약 코드(내역확인용)

    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩으로 성능 최적화
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String customerPhone;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime pickupTime;

    @Column(nullable = false)
    private LocalTime returnTime;

    @Column(nullable = false, precision = 10, scale = 2) // 금액을 다룰 때는 BigDecimal
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String status; // 향후 Enum 타입으로 변경하는 것을 추천

    @Builder
    public Reservation(
            String reservationCode, Product product, String customerName, String customerEmail, String customerPhone,
            LocalDate startDate, LocalDate endDate, LocalTime pickupTime, LocalTime returnTime,
            BigDecimal totalPrice, String status
    ) {
        this.reservationCode = reservationCode;
        this.product = product;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.startDate = startDate;
        this.endDate = endDate;
        this.pickupTime = pickupTime;
        this.returnTime = returnTime;
        this.totalPrice = totalPrice;
        this.status = status;
    }

}
