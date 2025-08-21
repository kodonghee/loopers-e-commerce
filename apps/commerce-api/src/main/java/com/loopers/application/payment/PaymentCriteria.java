package com.loopers.application.payment;

import java.math.BigDecimal;

public record PaymentCriteria(
    String userId,
    String orderId,
    String cardType,
    String cardNo,
    BigDecimal amount,
    Long couponId

    ) { }
