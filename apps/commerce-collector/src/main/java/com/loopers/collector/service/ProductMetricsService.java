package com.loopers.collector.service;

import com.loopers.collector.entity.ProductMetrics;
import com.loopers.collector.repository.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProductMetricsService {

    private final ProductMetricsRepository productMetricsRepository;

    @Transactional
    public void handleLike(Long productId, LocalDate date, boolean isLike) {
        ProductMetrics metrics = getOrCreate(productId, date);
        if (isLike) {
            metrics.increaseLikes();
        } else {
            metrics.decreaseLikes();
        }
        productMetricsRepository.save(metrics);
    }

    @Transactional
    public void handleOrder(Long productId, LocalDate date, long quantity) {
        ProductMetrics metrics = getOrCreate(productId, date);
        metrics.increaseOrder(quantity);
        productMetricsRepository.save(metrics);
    }

    @Transactional
    public void handleView(Long productId, LocalDate date) {
        ProductMetrics metrics = getOrCreate(productId, date);
        metrics.increaseViews();
        productMetricsRepository.save(metrics);
    }

    private ProductMetrics getOrCreate(Long productId, LocalDate date) {
        return productMetricsRepository
                .findByProductIdAndDate(productId, date)
                .orElseGet(() -> productMetricsRepository.save(
                        new ProductMetrics(productId, date)
                ));
    }
}
