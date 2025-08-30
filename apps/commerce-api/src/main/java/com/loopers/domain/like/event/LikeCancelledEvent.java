package com.loopers.domain.like.event;

public record LikeCancelledEvent(String userId, Long productId) {
    public static LikeCancelledEvent of(String userId, Long productId) {
        return new LikeCancelledEvent(userId, productId);
    }
}
