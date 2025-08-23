package com.loopers.application.payment;

import com.loopers.application.order.OrderService;
import com.loopers.application.payment.port.PaymentGateway;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentGateway gateway;
    private final PointRepository pointRepository;
    private final OrderService orderService;

    @Value("${pg.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    public PaymentResult requestCardPayment(PaymentCriteria criteria) {
        BigDecimal finalAmount = orderService.prepareForPayment(Long.valueOf(criteria.orderId()), criteria.couponId());

        var req = new PaymentGateway.Request(
                criteria.userId(), criteria.pgOrderId(),
                criteria.cardType(), criteria.cardNo(),
                finalAmount, callbackUrl
        );

        var result = gateway.request(req);

        return new PaymentResult(criteria.pgOrderId(), result.paymentId(), result.status());
    }

    @Transactional
    public PaymentResult processPointPayment(PaymentCriteria criteria) {
        try {
            BigDecimal finalAmount = orderService.prepareForPayment(Long.valueOf(criteria.orderId()), criteria.couponId());

            var point = pointRepository.findByUserIdForUpdate(criteria.userId())
                    .orElseThrow();
            point.use(finalAmount);

            orderService.confirmPayment(Long.valueOf(criteria.orderId()), criteria.couponId());
            return PaymentResult.success(criteria.orderId());
        } catch (Exception e) {
            orderService.markOrderFailed(Long.valueOf(criteria.orderId()));
            throw e;
        }
    }

    @Transactional
    public void handlePgCallback(PaymentCallback callback) {
        var order = orderService.getOrderDetail(Long.valueOf(callback.orderId()));

        if (order.totalAmount().compareTo(callback.amount()) != 0) {
            orderService.markOrderFailed(Long.valueOf(callback.orderId()));
            throw new IllegalStateException("결제 금액 불일치: 주문 금액=" + order.totalAmount() + ", PG 금액=" + callback.amount());
        }

        if ("SUCCESS".equals(callback.status())) {
            orderService.confirmPayment(
                    Long.valueOf(callback.orderId()),
                    callback.couponId()
            );

        } else {
            orderService.markOrderFailed(Long.valueOf(callback.orderId()));
        }
    }

    public PaymentResult findByOrderId(String userId, String orderId) {
        var result = gateway.findByOrderId(userId, orderId);

        if (result == null) {
            return new PaymentResult(orderId, null, "PENDING");
        }

        return new PaymentResult(result.orderId(), result.paymentId(), result.status());
    }
}
