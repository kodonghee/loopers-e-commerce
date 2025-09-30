package com.loopers.domain.ranking;

import com.loopers.application.product.ProductResult;

import java.util.List;

public interface PeriodicRankingRepository {
    List<ProductResult> getWeeklyTopProducts(int page, int size);
    List<ProductResult> getMonthlyTopProducts(int page, int size);
}
