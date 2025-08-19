package com.loopers.infrastructure.payment.pg;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "pgClient", url = "${pg.base-url}")
public interface PgClient {

    @PostMapping("/api/v1/payments")
    PgDto.Response requestPayment(@RequestHeader("X-USER-ID") String userId,
                                     @RequestBody PgDto.Request request);

    @GetMapping("/api/v1/payments")
    PgDto.Response findByOrderId(@RequestHeader("X-USER-ID") String userId,
                                    @RequestParam("orderId") String orderId);

    @GetMapping("/api/v1/payments/{paymentId}")
    PgDto.Response findByPaymentId(@RequestHeader("X-USER-ID") String userId,
                                   @PathVariable String paymentId);

}
