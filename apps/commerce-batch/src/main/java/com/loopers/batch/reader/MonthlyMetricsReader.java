package com.loopers.batch.reader;

import com.loopers.collector.entity.ProductMetrics;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
public class MonthlyMetricsReader {

    private final EntityManagerFactory entityManagerFactory;

    public MonthlyMetricsReader(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
    @Bean
    public JpaPagingItemReader<ProductMetrics> monthlyMetricsItemReader() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29);

        return new JpaPagingItemReaderBuilder<ProductMetrics>()
                .name("monthlyMetricsItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("SELECT m FROM ProductMetrics m WHERE m.date BETWEEN :start AND :end")
                .parameterValues(Map.of("start", thirtyDaysAgo, "end", today))
                .build();
    }
}
