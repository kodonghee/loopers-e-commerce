package com.loopers.batch.writer;

import com.loopers.batch.entity.AggregatedRanking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class WeeklyHistoryRankingWriter implements ItemWriter<AggregatedRanking> {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void write(Chunk<? extends AggregatedRanking> items) {
        String yearWeek = DateTimeFormatter.ofPattern("yyyy-'W'ww")
                .format(LocalDateTime.now());

        for (AggregatedRanking item : items) {
            entityManager.createNativeQuery("""
                INSERT IGNORE INTO history_product_rank_weekly
                  (product_id, like_count, order_count, order_quantity, view_count, year_week, created_at)
                VALUES (:productId, :likeCount, :orderCount, :orderQuantity, :viewCount, :yearWeek, NOW())
            """)
                    .setParameter("productId", item.getProductId())
                    .setParameter("likeCount", item.getLikeCount())
                    .setParameter("orderCount", item.getOrderCount())
                    .setParameter("orderQuantity", item.getOrderQuantity())
                    .setParameter("viewCount", item.getViewCount())
                    .setParameter("yearWeek", yearWeek)
                    .executeUpdate();
        }
    }
}
