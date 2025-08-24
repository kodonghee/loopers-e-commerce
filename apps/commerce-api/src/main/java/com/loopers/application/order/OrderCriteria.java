package com.loopers.application.order;

import com.loopers.domain.order.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public record OrderCriteria(
        String userId,
        List<OrderLine> items,
        Long couponId,
        PaymentMethod paymentMethod
) {
    public record OrderLine(Long productId, int quantity, BigDecimal price) {}
}
