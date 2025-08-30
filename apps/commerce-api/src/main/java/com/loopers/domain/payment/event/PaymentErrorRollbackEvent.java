package com.loopers.domain.payment.event;

public record PaymentErrorRollbackEvent(
        String orderId,
        String userId,
        String reason
) {
    public static PaymentErrorRollbackEvent of(String orderId, String userId, String reason) {
        return new PaymentErrorRollbackEvent(orderId, userId, reason);
    }
}
