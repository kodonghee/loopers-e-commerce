package com.loopers.domain.payment.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        String orderId,
        String userId,
        BigDecimal amount,
        Long couponId
) {
    public static PaymentCompletedEvent of(String orderId, String userId, BigDecimal amount, Long couponId) {
        return new PaymentCompletedEvent(orderId, userId, amount, couponId);
    }
}
