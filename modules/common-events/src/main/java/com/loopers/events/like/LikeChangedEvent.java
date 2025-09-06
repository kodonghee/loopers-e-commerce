package com.loopers.events.like;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record LikeChangedEvent(
        String eventId,
        Instant occurredAt,
        Long productId,
        String userId,
        boolean liked
) {
    @JsonCreator
    public LikeChangedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("productId") Long productId,
            @JsonProperty("userId") String userId,
            @JsonProperty("liked") boolean liked
    ) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.productId = productId;
        this.userId = userId;
        this.liked = liked;
    }

    public static LikeChangedEvent of(Long productId, String userId, boolean liked) {
        return new LikeChangedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                productId,
                userId,
                liked
        );
    }
}
