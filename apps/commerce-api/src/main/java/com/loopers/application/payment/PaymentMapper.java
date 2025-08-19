package com.loopers.application.payment;

import com.loopers.infrastructure.payment.pg.PgDto;

public class PaymentMapper {
    private PaymentMapper() {}

    static PgDto.Request toPgRequest(PaymentCriteria criteria, String callbackUrl) {
        return new PgDto.Request(criteria.orderId(), criteria.cardType(), criteria.cardNo(), criteria.amount(), callbackUrl);
    }

    static PaymentResult toResult(PgDto.Response response) {
        return new PaymentResult(response.orderId(), response.paymentId(), response.status());
    }
}
