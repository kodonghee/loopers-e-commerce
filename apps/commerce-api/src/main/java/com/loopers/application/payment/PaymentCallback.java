package com.loopers.application.payment;

import java.math.BigDecimal;

public record PaymentCallback(
        String orderId,
        String status,
        String userId,
        List<PaymentItem> items,
        Long couponId,
        boolean usePoint,
        BigDecimal amount;
) { }

