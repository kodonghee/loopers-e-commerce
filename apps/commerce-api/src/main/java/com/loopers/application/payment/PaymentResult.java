package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentStatus;

public record PaymentResult(
        String orderId,
        String paymentId,
        PaymentStatus status
) {
    public static PaymentResult success(String orderId) {
        return new PaymentResult(orderId, null, PaymentStatus.SUCCESS);
    }

    public static PaymentResult failed(String orderId) {
        return new PaymentResult(orderId, null, PaymentStatus.ERROR);
    }

    public static PaymentResult declined(String orderId) {
        return new PaymentResult(orderId, null, PaymentStatus.DECLINED);
    }

    public static PaymentResult pending(String orderId, String paymentId) {
        return new PaymentResult(orderId, paymentId, PaymentStatus.PENDING);
    }
}
