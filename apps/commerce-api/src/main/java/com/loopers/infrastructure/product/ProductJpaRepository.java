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
          pls.like_count     AS likeCount
        FROM product_like_summary pls
        JOIN product p ON p.id = pls.product_id
        JOIN brand b ON b.id = p.brand_id
        WHERE pls.brand_id = :brandId
        ORDER BY pls.like_count DESC, pls.product_id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListLikesDescByBrand(@Param("brandId") Long brandId,
                                                   @Param("limit") int limit,
                                                   @Param("offset") int offset);

    @Query(value = """
        SELECT
          p.id               AS id,
          p.name             AS name,
          p.stock_value      AS stockValue,
          p.price            AS price,
          b.name             AS brandName,
          pls.like_count     AS likeCount
        FROM product_like_summary pls
        JOIN product p ON p.id = pls.product_id
        JOIN brand b ON b.id = p.brand_id
        ORDER BY pls.like_count DESC, pls.product_id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListLikesDescGlobal(@Param("limit") int limit,
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
        WHERE p.brand_id = :brandId
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListLatestByBrand(@Param("brandId") Long brandId,
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
        ORDER BY p.created_at DESC, p.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListLatestGlobal(@Param("limit") int limit,
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
        WHERE p.brand_id = :brandId
        ORDER BY p.price ASC, p.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListPriceAscByBrand(@Param("brandId") Long brandId,
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
        ORDER BY p.price ASC, p.id DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ProductListView> findListPriceAscGlobal(@Param("limit") int limit,
                                                 @Param("offset") int offset);

}
