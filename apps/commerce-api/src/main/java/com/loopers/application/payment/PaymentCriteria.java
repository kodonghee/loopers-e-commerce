package com.loopers.application.payment;

import java.math.BigDecimal;

public record PaymentCriteria(
    String userId,
    String orderId,
    String cardType,
    String cardNo,
    BigDecimal amount,
    Long couponId

    ) {
    public PaymentCriteria withCardNo(String newCardNo) {
        return new PaymentCriteria(userId, orderId, cardType, newCardNo, amount, couponId);
    }
}
