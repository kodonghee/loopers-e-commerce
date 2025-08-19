package com.loopers.infrastructure.payment.pg;

import java.math.BigDecimal;

public final class PgDto {
    private PgDto() {}

    public static record Request(
            String orderId,
            String cardType,
            String cardNo,
            String amount,
            String callbackUrl
    ) {}

    public static record Response(
            String paymentId,
            String orderId,
            String status
    ) {}
}
