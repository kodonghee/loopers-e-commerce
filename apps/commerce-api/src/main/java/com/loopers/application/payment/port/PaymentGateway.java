package com.loopers.application.payment.port;

import java.math.BigDecimal;

public interface PaymentGateway {
    Response request(Request request);
    Response findByOrderId(String userId, String orderId);
    record Request(String userId, String orderId, String cardType, String cardNo, BigDecimal amount, String callbackUrl) {}
    record Response(String orderId, String paymentId, String status) {}
}
