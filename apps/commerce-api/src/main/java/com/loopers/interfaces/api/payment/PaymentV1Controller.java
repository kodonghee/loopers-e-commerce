package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentService;
import com.loopers.application.payment.PaymentResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentV1Controller implements PaymentV1ApiSpec {

    private final PaymentService paymentService;

    @PostMapping
    @Override
    public ApiResponse<PaymentV1Dto.PaymentResponse> requestPayment(
            @RequestHeader("X-USER-ID") String userId,
            @RequestBody PaymentV1Dto.PaymentRequest request
    ) {
        PaymentCriteria criteria = new PaymentCriteria(
                userId,
                request.orderId(),
                request.cardType(),
                request.cardNo(),
                request.amount()
        );

        PaymentResult result = paymentService.request(criteria);
        return ApiResponse.success(PaymentV1Dto.PaymentResponse.from(result));
    }

    @GetMapping("/{orderId}")
    @Override
    public ApiResponse<PaymentV1Dto.PaymentResponse> findByOrderId(
            @RequestHeader("X-USER-ID") String userId,
            @PathVariable String orderId
    ) {
        PaymentResult result = paymentService.findByOrderId(userId, orderId);
        return ApiResponse.success(PaymentV1Dto.PaymentResponse.from(result));
    }
}
