package com.loopers.batch.reader;

import com.loopers.batch.entity.AggregatedRanking;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class WeeklyMetricsReader {

    private final EntityManagerFactory entityManagerFactory;

    public WeeklyMetricsReader(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public JpaPagingItemReader<AggregatedRanking> weeklyRankingItemReader() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        return new JpaPagingItemReaderBuilder<AggregatedRanking>()
                .name("weeklyRankingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("""
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
            """)
                .parameterValues(Map.of(
                        "start", sevenDaysAgo,
                        "end", today
                ))
                .build();
    }
}
