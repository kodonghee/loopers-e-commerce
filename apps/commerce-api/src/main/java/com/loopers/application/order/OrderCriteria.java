package com.loopers.application.order;

import java.math.BigDecimal;
import java.util.List;

public record OrderCriteria(
        String userId,
        List<OrderLine> items,
        Long couponId
) {
    public record OrderLine(Long productId, int quantity, BigDecimal price) {}
}
