package com.loopers.events.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ProductViewedEvent(
        String eventId,
        Instant occurredAt,
        Long productId
) {
    @JsonCreator
    public ProductViewedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("productId") Long productId
    ) {
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.productId = productId;
    }

    public static ProductViewedEvent of(Long productId) {
        return new ProductViewedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                productId
        );
    }
}
