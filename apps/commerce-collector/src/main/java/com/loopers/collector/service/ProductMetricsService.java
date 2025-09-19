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
        int updated = isLike
                ? productMetricsRepository.incrementLike(productId, date)
                : productMetricsRepository.decrementLike(productId, date);

        if (updated == 0) {
            ProductMetrics metrics = new ProductMetrics(productId, date);
            if (isLike) metrics.increaseLikes();
            else metrics.decreaseLikes();
            productMetricsRepository.save(metrics);
        }
    }

    @Transactional
    public void handleOrder(Long productId, LocalDate date, long quantity) {
        int updated = productMetricsRepository.incrementOrder(productId, date, quantity);
        if (updated == 0) {
            ProductMetrics metrics = new ProductMetrics(productId, date);
            metrics.increaseOrder(quantity);
            productMetricsRepository.save(metrics);
        }
    }

    @Transactional
    public void handleView(Long productId, LocalDate date) {
        int updated = productMetricsRepository.incrementView(productId, date);
        if (updated == 0) {
            ProductMetrics metrics = new ProductMetrics(productId, date);
            metrics.increaseViews();
            productMetricsRepository.save(metrics);
        }
    }
}
