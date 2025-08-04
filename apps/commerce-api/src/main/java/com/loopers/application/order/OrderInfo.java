package com.loopers.application.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderInfo(
        Long orderId,
        String userId,
        BigDecimal totalAmount,
        List<OrderItemInfo> items
) {
    public record OrderItemInfo(Long productId, int quantity, BigDecimal price) {}
}
