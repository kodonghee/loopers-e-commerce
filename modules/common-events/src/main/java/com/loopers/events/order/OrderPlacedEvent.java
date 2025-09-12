package com.loopers.events.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderPlacedEvent(
        String orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItemDto> items
) {
    public record OrderItemDto(Long productId, int quantity, BigDecimal price) {}

    public static OrderPlacedEvent of(String orderId, String userId, BigDecimal totalAmount, List<OrderItemDto> items) {
        return new OrderPlacedEvent(orderId, userId, totalAmount, items);
    }
}
