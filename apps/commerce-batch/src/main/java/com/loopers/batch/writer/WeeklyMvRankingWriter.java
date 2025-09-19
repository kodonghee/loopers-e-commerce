package com.loopers.batch.writer;

import com.loopers.batch.entity.AggregatedRanking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class WeeklyMvRankingWriter implements ItemWriter<AggregatedRanking> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void write(Chunk<? extends AggregatedRanking> items) {
        for (AggregatedRanking item : items) {
            entityManager.createNativeQuery("""
                INSERT INTO mv_product_rank_weekly 
                  (product_id, like_count, order_count, order_quantity, view_count, updated_at)
                VALUES (:productId, :likeCount, :orderCount, :orderQuantity, :viewCount, NOW())
                ON DUPLICATE KEY UPDATE
                  like_count = VALUES(like_count),
                  order_count = VALUES(order_count),
                  order_quantity = VALUES(order_quantity),
                  view_count = VALUES(view_count),
                  updated_at = NOW()
            """)
                    .setParameter("productId", item.getProductId())
                    .setParameter("likeCount", item.getLikeCount())
                    .setParameter("orderCount", item.getOrderCount())
                    .setParameter("orderQuantity", item.getOrderQuantity())
                    .setParameter("viewCount", item.getViewCount())
                    .executeUpdate();
        }
    }
}
