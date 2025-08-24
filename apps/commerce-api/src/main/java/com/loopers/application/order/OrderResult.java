package com.loopers.application.order;

import com.loopers.domain.order.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderResult(
        String orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItemResult> items,
        OrderStatus status
) {
    public record OrderItemResult(Long productId, int quantity, BigDecimal price) {}
}
