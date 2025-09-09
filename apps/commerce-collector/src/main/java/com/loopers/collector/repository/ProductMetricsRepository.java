package com.loopers.collector.repository;

import com.loopers.collector.entity.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, Long> {
    Optional<ProductMetrics> findByProductIdAndDate(Long productId, LocalDate date);
}
