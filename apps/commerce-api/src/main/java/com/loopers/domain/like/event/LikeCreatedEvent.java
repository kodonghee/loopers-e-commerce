package com.loopers.domain.like.event;

public record LikeCreatedEvent(String userId, Long productId) {
    public static LikeCreatedEvent of(String userId, Long productId) {
        return new LikeCreatedEvent(userId, productId);
    }
}
