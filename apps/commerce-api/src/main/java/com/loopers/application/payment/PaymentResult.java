package com.loopers.application.payment;

public record PaymentResult(
        String orderId,
        String paymentId,
        String status
) { }
