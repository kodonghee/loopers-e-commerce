package com.loopers.infrastructure.like;

import com.loopers.domain.like.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeCountJpaRepository extends JpaRepository<Like, Long> {
    int countByProductId(Long productId);
}
