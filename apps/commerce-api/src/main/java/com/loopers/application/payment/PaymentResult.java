package com.loopers.application.payment;

public record PaymentResult(
        String orderId,
        String paymentId,
        String status
) {
    public static PaymentResult success(String orderId) {
        return new PaymentResult(orderId, null, "SUCCESS");
    }

    public static PaymentResult failed(String orderId) {
        return new PaymentResult(orderId, null, "FAILED");
    }

    public static PaymentResult pending(String orderId, String paymentId) {
        return new PaymentResult(orderId, paymentId, "PENDING");
    }
}
