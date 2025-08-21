package com.loopers.application.payment;

import java.math.BigDecimal;

public record PaymentCallback(
        String orderId,
        String paymentId,
        String status,
        String userId,
        BigDecimal amount,
        Long couponId
) { }

