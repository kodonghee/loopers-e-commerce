package com.loopers.application.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderResult(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItemResult> items
) {
    public record OrderItemResult(Long productId, int quantity, BigDecimal price) {}
}
