package com.loopers.domain.order.event;

import java.math.BigDecimal;
import java.util.List;

public record OrderCreatedEvent(
        String orderId,
        String userId,
        BigDecimal totalAmount,
        List<Long> productIds
) {
    public static OrderCreatedEvent of(String orderId, String userId, BigDecimal totalAmount, List<Long> productIds) {
        return new OrderCreatedEvent(orderId, userId, totalAmount, productIds);
    }
}
