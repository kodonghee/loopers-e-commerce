package com.loopers.infrastructure.payment.pg;

import com.loopers.application.payment.port.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PgClientAdapter implements PaymentGateway {

    private final PgClient pgClient;

    @Override
    public Response request(Request request) {
        var pgRequest = new PgDto.Request(request.orderId(), request.cardType(), request.cardNo(), request.amount(), request.callbackUrl());
        log.info(">>> PG Request: {}", pgRequest);
        var pgResponse = pgClient.requestPayment(request.userId(), pgRequest);
        log.info(">>> PG Raw Response: {}", pgResponse);
        return new Response(request.orderId(), pgResponse.data().transactionKey(), pgResponse.data().status());
    }

    @Override
    public Response findByOrderId(String userId, String orderId) {
        var pgResponse = pgClient.findByOrderId(userId, orderId);
        return new Response(orderId, pgResponse.data().transactionKey(), pgResponse.data().status());
    }
}
