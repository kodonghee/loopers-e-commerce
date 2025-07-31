package com.loopers.infrastructure.like;

public interface LikeJpaRepository extends JpaRepository<Like, Long> {
    int countByProductId(Long productId);
}
