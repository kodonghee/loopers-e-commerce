package com.loopers.events.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderPlacedEvent(
        String eventId,
        Instant occurredAt,
        String orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItemDto> items
) {
    public record OrderItemDto(Long productId, int quantity, BigDecimal price) {}

    public static OrderPlacedEvent of(String orderId, String userId, BigDecimal totalAmount, List<OrderItemDto> items) {
        return new OrderPlacedEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                orderId,
                userId,
                totalAmount,
                items);
    }
}
