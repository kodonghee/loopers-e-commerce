package com.loopers.interfaces.api.payment;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment V1 API", description = "결제 관련 API 입니다.")
public interface PaymentV1ApiSpec {

    @Operation(summary = "결제 요청")
    @GetMapping("/payments")
    ApiResponse<PaymentV1Dto.PaymentResponse> requestPayment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PaymentV1Dto.PaymentRequest request
    );

    @Operation(summary = "주문 ID로 결제 조회")
    @GetMapping("/payments/{orderId}")
    ApiResponse<PaymentV1Dto.PaymentResponse> findByOrderId(
            @RequestHeader("X-USER-ID") String userId,
            @PathVariable("paymentId") String orderId
    );

    @Operation(summary = "callback 요청")
    @PostMapping("/payments/callback")
    void handleCallback(
            @RequestBody PaymentV1Dto.PaymentCallbackRequest request
    );
}
