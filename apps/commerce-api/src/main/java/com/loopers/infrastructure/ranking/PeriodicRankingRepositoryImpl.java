package com.loopers.infrastructure.ranking;

import com.loopers.application.product.ProductResult;
import com.loopers.domain.ranking.PeriodicRankingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PeriodicRankingRepositoryImpl implements PeriodicRankingRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProductResult> getWeeklyTopProducts(int page, int size) {
        return entityManager.createQuery("""
            SELECT new com.loopers.application.product.ProductResult(
                r.productId, p.name, p.stock.value, p.price.amount,
                b.name, r.likeCount, null
            )
            FROM mv_product_rank_weekly r
            JOIN Product p ON r.productId = p.id
            JOIN Brand b ON p.brandId = b.id
            ORDER BY (r.likeCount + r.orderCount + r.orderQuantity + r.viewCount) DESC
            """, ProductResult.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }

    @Override
    public List<ProductResult> getMonthlyTopProducts(int page, int size) {
        return entityManager.createQuery("""
            SELECT new com.loopers.application.product.ProductResult(
                r.productId, p.name, p.stock.value, p.price.amount,
                b.name, r.likeCount, null
            )
            FROM mv_product_rank_monthly r
            JOIN Product p ON r.productId = p.id
            JOIN Brand b ON p.brandId = b.id
            ORDER BY (r.likeCount + r.orderCount + r.orderQuantity + r.viewCount) DESC
            """, ProductResult.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }
}
