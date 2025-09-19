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
    private long likeCount;

    @Column(nullable = false)
    private long orderCount;

    @Column(nullable = false)
    private long orderQuantity;

    @Column(nullable = false)
    private long viewCount;

    public ProductMetrics(Long productId, LocalDate date) {
        this.productId = productId;
        this.date = date;
        this.likeCount = 0L;
        this.orderCount = 0L;
        this.orderQuantity = 0L;
        this.viewCount = 0L;
    }

    public void increaseLikes() { this.likeCount++; }

    public void decreaseLikes() {
        if (likeCount > 0) this.likeCount--;
    }

    public void increaseOrder(long quantity) {
        this.orderCount++;
        this.orderQuantity += quantity;
    }

    public void increaseViews() { this.viewCount++; }
}
