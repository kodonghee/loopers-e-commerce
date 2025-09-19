package com.loopers.collector.repository;

import com.loopers.collector.entity.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricsRepository extends JpaRepository<ProductMetrics, Long> {
    Optional<ProductMetrics> findByProductIdAndDate(Long productId, LocalDate date);

    @Modifying
    @Query("update ProductMetrics pm " +
            "set pm.likeCount = pm.likeCount + 1 " +
            "where pm.productId = :productId and pm.date = :date")
    int incrementLike(@Param("productId") Long productId,
                      @Param("date") LocalDate date);

    @Modifying
    @Query("update ProductMetrics pm " +
            "set pm.likeCount = case when pm.likeCount > 0 then pm.likeCount - 1 else 0 end " +
            "where pm.productId = :productId and pm.date = :date")
    int decrementLike(@Param("productId") Long productId,
                      @Param("date") LocalDate date);

    @Modifying
    @Query("update ProductMetrics pm " +
            "set pm.orderCount = pm.orderCount + 1, " +
            "    pm.orderQuantity = pm.orderQuantity + :quantity " +
            "where pm.productId = :productId and pm.date = :date")
    int incrementOrder(@Param("productId") Long productId,
                       @Param("date") LocalDate date,
                       @Param("quantity") long quantity);

    @Modifying
    @Query("update ProductMetrics pm " +
            "set pm.viewCount = pm.viewCount + 1 " +
            "where pm.productId = :productId and pm.date = :date")
    int incrementView(@Param("productId") Long productId,
                      @Param("date") LocalDate date);
}
