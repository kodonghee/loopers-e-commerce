package com.loopers.domain.like;

import com.loopers.domain.user.UserId;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {
    Optional<Like> findByUserIdAndProductId(UserId userId, Long productId);
    void save(Like like);
    void delete(Like like);
    List<Like> findAllByUserId(UserId userId);
    long countByProductId(Long productId);
}
