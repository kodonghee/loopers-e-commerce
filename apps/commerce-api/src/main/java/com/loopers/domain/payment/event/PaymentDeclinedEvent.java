package com.loopers.domain.payment.event;

public record PaymentDeclinedEvent(
        String orderId,
        String userId,
        String reason
) {
    public static PaymentDeclinedEvent of(String orderId, String userId, String reason) {
        return new PaymentDeclinedEvent(orderId, userId, reason);
    }
}
