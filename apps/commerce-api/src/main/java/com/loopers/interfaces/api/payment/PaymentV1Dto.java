package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentResult;
import io.swagger.v3.oas.annotations.media.Schema;

public class PaymentV1Dto {

    public record PaymentRequest(
            @Schema(example = "1351039135") String orderId,
            @Schema(example = "SAMSUNG") String cardType,
            @Schema(example = "1234-5678-9814-1451") String cardNo,
            @Schema(example = "5000") String amount
    ) {}

    public record PaymentResponse(
            String orderId,
            String paymentId,
            String status
    ) {
        public static PaymentResponse from(PaymentResult result) {
            return new PaymentResponse(
                    result.orderId(),
                    result.paymentId(),
                    result.status()
            );
        }
    }
}
