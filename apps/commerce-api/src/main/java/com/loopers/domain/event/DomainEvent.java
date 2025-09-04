package com.loopers.domain.event;

import java.time.Instant;

public interface DomainEvent {
    String eventId();
    Instant occurredAt();
}
