package com.loopers.domain.payment.event;

public record PaymentDeclinedRollbackEvent(
        String orderId,
        String userId,
        String reason
) {
    public static PaymentDeclinedRollbackEvent of(String orderId, String userId, String reason) {
        return new PaymentDeclinedRollbackEvent(orderId, userId, reason);
    }
}
