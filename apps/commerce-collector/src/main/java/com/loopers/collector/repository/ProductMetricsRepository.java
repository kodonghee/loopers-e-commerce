package com.loopers.collector.repository;

import com.loopers.collector.entity.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, Long> {
}
