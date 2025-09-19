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
public class WeeklyMetricsReader {

    private final EntityManagerFactory entityManagerFactory;

    public WeeklyMetricsReader(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public JpaPagingItemReader<ProductMetrics> weeklyMetricsItemReader() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        return new JpaPagingItemReaderBuilder<ProductMetrics>()
                .name("weeklyMetricsItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(100)
                .queryString("SELECT m FROM ProductMetrics m WHERE m.date BETWEEN :start AND :end")
                .parameterValues(Map.of("start", sevenDaysAgo, "end", today))
                .build();
    }
}
