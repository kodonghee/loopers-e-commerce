package com.loopers.infrastructure.payment.pg;

import java.math.BigDecimal;

public final class PgDto {
    private PgDto() {}

    public static record Request(
            String orderId,
            String cardType,
            String cardNo,
            BigDecimal amount,
            String callbackUrl
    ) {}

    public static record ResponseWrapper(
            Meta meta,
            Data data
    ) {
        public static record Meta(String result, String errorCode, String message) {}
        public static record Data(String transactionKey, String status) {}
    }

    public static record Response(
            String transactionKey,
            String status
    ) {}
}
