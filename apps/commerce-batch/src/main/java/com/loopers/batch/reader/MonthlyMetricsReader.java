package com.loopers.batch.reader;

import com.loopers.batch.entity.AggregatedRanking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.batch.item.ItemReader;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

public class MonthlyMetricsReader implements ItemReader<AggregatedRanking> {

    @PersistenceContext
    private EntityManager entityManager;

    private Iterator<AggregatedRanking> iterator;

    @Override
    public AggregatedRanking read() {
        if (iterator == null) {
            LocalDate today = LocalDate.now();
            LocalDate thirtyDaysAgo = today.minusDays(29);

            List<AggregatedRanking> results = entityManager.createQuery("""
                SELECT new com.loopers.batch.entity.AggregatedRanking(
                    m.productId,
                    SUM(m.likeCount),
                    SUM(m.orderCount),
                    SUM(m.orderQuantity),
                    SUM(m.viewCount)
                )
                FROM ProductMetrics m
                WHERE m.date BETWEEN :start AND :end
                GROUP BY m.productId
            """, AggregatedRanking.class)
                    .setParameter("start", thirtyDaysAgo)
                    .setParameter("end", today)
                    .getResultList();

            iterator = results.iterator();
        }

        return iterator != null && iterator.hasNext() ? iterator.next() : null;
    }
}
