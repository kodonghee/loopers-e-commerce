package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.PaymentCallback;
import com.loopers.application.payment.PaymentResult;
import com.loopers.domain.order.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class PaymentV1Dto {

    public record PaymentRequest(
            @Schema(example = "1L") String orderId,
            @Schema(example = "1234567") String pgOrderId,
            @Schema(example = "POINTS or CARD") PaymentMethod paymentMethod,
            @Schema(example = "SAMSUNG") String cardType,
            @Schema(example = "1234-5678-9814-1451") String cardNo,
            @Schema(example = "5000") BigDecimal amount,
            @Schema(example = "123") Long couponId
    ) {
        public boolean isCard() {
            return paymentMethod == PaymentMethod.CARD;
        }

        public boolean isPoints() {
            return paymentMethod == PaymentMethod.POINTS;
        }

        public void validate() {
            if (isCard()) {
                if (cardType == null || cardNo == null) {
                    throw new IllegalArgumentException("카드 결제 시 카드 정보는 필수입니다.");
                }
            }
            if (isPoints()) {
                if (cardType != null || cardNo != null) {
                    throw new IllegalArgumentException("포인트 결제 시 카드 정보는 허용 되지 않습니다.");
                }
            }
        }
    }


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

    public record PaymentCallbackRequest(
            String orderId,
            String paymentId,
            String status,
            String userId,
            BigDecimal amount,
            Long couponId
    ) {
        public PaymentCallback toCallback() {
            return new PaymentCallback(
                    orderId, paymentId, status, userId, amount, couponId

            );
        }
    }
}
