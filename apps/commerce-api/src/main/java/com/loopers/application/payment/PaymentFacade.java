package com.loopers.application.payment;

import com.loopers.application.payment.port.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PaymentFacade {

    private final PaymentGateway gateway;

    @Value("${pg.callback-url:http://localhost:8080/api/v1/pg/callback}")
    private String callbackUrl;

    public PaymentResult request(PaymentCriteria criteria) {
        var req = new PaymentGateway.Request(criteria.userId(), criteria.orderId(), criteria.cardType(), criteria.cardNo(), criteria.amount(), callbackUrl);
        var result = gateway.request(req);
        return new PaymentResult(result.orderId(), result.paymentId(), result.status());
    }

    public PaymentResult findByOrderId(String userId, String orderId) {
        var result = gateway.findByOrderId(userId, orderId);
        return new PaymentResult(result.orderId(), result.paymentId(), result.status());
    }
}
