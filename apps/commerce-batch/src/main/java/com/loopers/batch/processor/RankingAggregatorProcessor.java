package com.loopers.batch.processor;

import com.loopers.batch.entity.AggregatedRanking;
import com.loopers.collector.entity.ProductMetrics;
import org.springframework.batch.item.ItemProcessor;

public class RankingAggregatorProcessor implements ItemProcessor<ProductMetrics, AggregatedRanking> {
    @Override
    public AggregatedRanking process(ProductMetrics metrics) {
        return new AggregatedRanking(
                metrics.getProductId(),
                metrics.getLikeCount(),
                metrics.getOrderCount(),
                metrics.getOrderQuantity(),
                metrics.getViewCount()
        );
    }
}
