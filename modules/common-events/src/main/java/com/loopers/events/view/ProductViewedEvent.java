package com.loopers.events.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ProductViewedEvent(
        String eventId,
        Instant occurredAt,
        Long productId,
        String userId
) {
    @JsonCreator
    public ProductViewedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("productId") Long productId,
            @JsonProperty("userId") String userId
    ) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.productId = productId;
        this.userId = userId;
    }

    public static ProductViewedEvent of(Long productId, String userId) {
        return new ProductViewedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                productId,
                userId
        );
    }
}
