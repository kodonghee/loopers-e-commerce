package com.loopers.events.like;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record LikeChangedEvent (
        String eventId,
        Instant occurredAt,
        Long productId,
        String userId,
        boolean liked
){
    @JsonCreator
    public LikeChangedEvent(
            @JsonProperty("productId") Long productId,
            @JsonProperty("userId") String userId,
            @JsonProperty("liked") boolean liked
    ) {
        this(UUID.randomUUID().toString(), Instant.now(), productId, userId, liked);
    }
}
