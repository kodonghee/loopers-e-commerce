package com.loopers.domain.payment.event;

import com.loopers.domain.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        String orderId,
        String userId,
        BigDecimal amount,
        Long couponId,
        PaymentMethod paymentMethod
) {
    public static PaymentCompletedEvent of(String orderId, String userId, BigDecimal amount, Long couponId, PaymentMethod paymentMethod) {
        return new PaymentCompletedEvent(orderId, userId, amount, couponId, paymentMethod);
    }
}
