package com.loopers.infrastructure.like;

import com.loopers.domain.like.ProductLikeSummary;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductLikeSummaryJpaRepository extends JpaRepository<ProductLikeSummary, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProductLikeSummary s where s.productId = :productId")
    Optional<ProductLikeSummary> findByProductIdForUpdate(@Param("productId") Long productId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        INSERT INTO product_like_summary (product_id, brand_id, like_count)
        VALUES (:productId, :brandId, 0)
        ON DUPLICATE KEY UPDATE brand_id = VALUES(brand_id)
        """, nativeQuery = true)
    void ensureRow(@Param("productId") Long productId, @Param("brandId") Long brandId);
}
