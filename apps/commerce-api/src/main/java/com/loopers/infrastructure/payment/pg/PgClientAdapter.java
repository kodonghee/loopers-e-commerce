package com.loopers.infrastructure.payment.pg;

import com.loopers.application.payment.port.PaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PgClientAdapter implements PaymentGateway {

    private final PgClient pgClient;

    @Override
    public Response request(Request request) {
        var pgRequest = new PgDto.Request(request.orderId(), request.cardType(), request.cardNo(), request.amount(), request.callbackUrl());
        var pgResponse = pgClient.requestPayment(request.userId(), pgRequest);
        return new Response(pgResponse.orderId(), pgResponse.paymentId(), pgResponse.status());
    }

    @Override
    public Response findByOrderId(String userId, String orderId) {
        var pgResponse = pgClient.findByOrderId(userId, orderId);
        return new Response(pgResponse.orderId(), pgResponse.paymentId(), pgResponse.status());
    }
}
