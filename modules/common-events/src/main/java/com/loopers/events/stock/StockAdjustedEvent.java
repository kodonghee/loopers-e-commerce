package com.loopers.events.stock;

import java.time.Instant;
import java.util.UUID;

public record StockAdjustedEvent(
        String eventId,
        Instant occurredAt,
        Long productId,
        int newStock
) {
    public StockAdjustedEvent(Long productId, int newStock) {
        this(UUID.randomUUID().toString(), Instant.now(), productId, newStock);
    }
}
