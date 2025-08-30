package com.loopers.domain.payment.event;

public record PaymentErrorEvent(
        String orderId,
        String userId,
        String reason
) {
    public static PaymentErrorEvent of(String orderId, String userId, String reason) {
        return new PaymentErrorEvent(orderId, userId, reason);
    }
}
