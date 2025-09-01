package com.loopers.infrastructure.payment.pg;

import com.loopers.application.payment.port.PaymentGateway;
import com.loopers.domain.payment.PaymentStatus;
import feign.FeignException;
import feign.Request;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@Slf4j
public class PgClientAdapter implements PaymentGateway {

    private final PgClient pgClient;

    @Override
    @CircuitBreaker(name = "pgClient")
    @Retry(name = "pgClient", fallbackMethod = "fallbackPayment")
    public Response request(Request request) {
        var pgRequest = new PgDto.Request(request.orderId(), request.cardType(), request.cardNo(), request.amount(), request.callbackUrl());
        log.info(">>> PG Request: {}", pgRequest);
        var pgResponse = pgClient.requestPayment(request.userId(), pgRequest);
        log.info(">>> PG Raw Response: {}", pgResponse);

        if ("FAIL".equals(pgResponse.meta().result())) {
            if ("Internal Server Error".equals(pgResponse.meta().errorCode())) {
                throw new PgServerUnstableException(pgResponse.meta().message());
            }
            return new Response(
                    request.orderId(),
                    null,
                    "FAILED",
                    pgResponse.meta().message()
            );
        }
        return new Response(
                request.orderId(),
                pgResponse.data().transactionKey(),
                pgResponse.data().status(),
                null);
    }

    @Override
    public Response findByOrderId(String userId, String orderId) {
        var pgResponse = pgClient.findByOrderId(userId, orderId);
        if ("FAIL".equals(pgResponse.meta().result())) {
            return new Response(
                    orderId,
                    null,
                    "FAILED",
                    pgResponse.meta().message()
            );
        }
        return new Response(
                orderId,
                pgResponse.data().transactionKey(),
                pgResponse.data().status(),
                null);
    }

    private PaymentGateway.Response fallbackPayment(PaymentGateway.Request req, Throwable t) {
        return new PaymentGateway.Response(
                req.orderId(),
                null,
                PaymentStatus.ERROR.name(),
                "Fallback due to: " + t.getMessage());
    }
}
