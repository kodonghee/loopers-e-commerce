package com.loopers.application.payment;

import com.loopers.infrastructure.payment.pg.PgDto;

public class PaymentMapper {
    private PaymentMapper() {}

    static PgDto.Request toPgRequest(PaymentCriteria criteria, String callbackUrl) {
        return new PgDto.Request(criteria.orderId(), criteria.cardType(), criteria.cardNo(), criteria.amount(), callbackUrl);
    }

    static PaymentResult toResult(String orderId, PgDto.Response response) {
        return new PaymentResult(orderId, response.transactionKey(), response.status());
    }
}
