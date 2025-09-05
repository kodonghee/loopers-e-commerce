package com.loopers.collector.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "product_metrics",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_date", columnNames = {"product_id", "date"})
        }
)
@Getter
@NoArgsConstructor
public class ProductMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private long likeCount = 0;

    @Column(nullable = false)
    private long salesCount = 0;

    @Column(nullable = false)
    private long viewCount = 0;

    public ProductMetrics(Long productId, LocalDate date) {
        this.productId = productId;
        this.date = date;
    }

    public void increaseLikes() { this.likeCount++; }

    public void increaseSales() { this.salesCount++; }

    public void increaseViews() { this.viewCount++; }
}
