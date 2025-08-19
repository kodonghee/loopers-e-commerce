package com.loopers.application.payment;

public record PaymentCriteria(
    String userId,
    String orderId,
    String cardType,
    String cardNo,
    String amount

    ) { }
