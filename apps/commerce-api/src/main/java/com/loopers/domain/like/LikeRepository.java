package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    Optional<Like> findByUserIdAndProductId(String userId, Long productId);
    void save(Like like);
    void delete(Like like);
    List<Like> findAllByUserId(String userId);
    long countByProductId(Long productId);
}
