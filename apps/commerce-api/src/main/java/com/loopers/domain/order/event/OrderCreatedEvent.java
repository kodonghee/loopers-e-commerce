package com.loopers.domain.order.event;

import com.loopers.domain.order.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItem> items
) {
    public static OrderCreatedEvent of(String orderId, String userId, BigDecimal totalAmount, List<OrderItem> items) {
        return new OrderCreatedEvent(orderId, userId, totalAmount, items);
    }
}
