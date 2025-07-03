package laoride.lao_ride.product.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deposit;

    @Column(nullable = false)
    private LocalDate effectiveDate;

    @Builder
    public ProductPrice(Product product, BigDecimal dailyRate, BigDecimal deposit, LocalDate effectiveDate) {
        this.product = product;
        this.dailyRate = dailyRate;
        this.deposit = deposit;
        this.effectiveDate = effectiveDate;
    }

}
