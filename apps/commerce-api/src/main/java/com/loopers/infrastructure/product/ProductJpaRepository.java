package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductListView;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByBrandId(Long brandId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids order by p.id")
    List<Product> findByIdForUpdate(Collection<Long> ids);

    @Query(value = """
        SELECT
          p.id               AS id,
          p.name             AS name,
          p.stock_value      AS stockValue,
          p.price            AS price,
          b.name             AS brandName,
          COALESCE(pls.like_count, 0) AS likeCount
        FROM product p
        JOIN brand b ON b.id = p.brand_id
        LEFT JOIN product_like_summary pls ON pls.product_id = p.id
        WHERE (:brandId IS NULL OR p.brand_id = :brandId)
        ORDER BY COALESCE(pls.like_count, 0) DESC, p.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListLikesDesc(@Param("brandId") Long brandId,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset);

    @Query(value = """
        SELECT
          p.id               AS id,
          p.name             AS name,
          p.stock_value      AS stockValue,
          p.price            AS price,
          b.name             AS brandName,
          COALESCE(pls.like_count, 0) AS likeCount
        FROM product p
        JOIN brand b ON b.id = p.brand_id
        LEFT JOIN product_like_summary pls ON pls.product_id = p.id
        WHERE (:brandId IS NULL OR p.brand_id = :brandId)
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListLatest(@Param("brandId") Long brandId,
                                         @Param("limit") int limit,
                                         @Param("offset") int offset);

    @Query(value = """
        SELECT
          p.id               AS id,
          p.name             AS name,
          p.stock_value      AS stockValue,
          p.price            AS price,
          b.name             AS brandName,
          COALESCE(pls.like_count, 0) AS likeCount
        FROM product p
        JOIN brand b ON b.id = p.brand_id
        LEFT JOIN product_like_summary pls ON pls.product_id = p.id
        WHERE (:brandId IS NULL OR p.brand_id = :brandId)
        ORDER BY p.price ASC, p.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListPriceAsc(@Param("brandId") Long brandId,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

}
