package com.loopers.domain.event.like;

import com.loopers.domain.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record LikeChangedEvent (
        String eventId,
        Instant occurredAt,
        Long productId,
        String userId,
        boolean liked
) implements DomainEvent {

    public LikeChangedEvent(Long productId, String userId, boolean liked) {
        this(UUID.randomUUID().toString(), Instant.now(), productId, userId, liked);
    }
}
