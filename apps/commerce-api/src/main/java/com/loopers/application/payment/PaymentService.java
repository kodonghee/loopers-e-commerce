package com.loopers.application.payment;

import com.loopers.application.order.OrderService;
import com.loopers.application.payment.port.PaymentGateway;
import com.loopers.domain.point.PointRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentGateway gateway;
    private final PointRepository pointRepository;
    private final OrderService orderService;

    @PersistenceContext
    private final EntityManager em;

    @Value("${pg.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    public PaymentResult requestCardPayment(PaymentCriteria criteria) {
        BigDecimal finalAmount = orderService.prepareForPayment(criteria.orderId(), criteria.couponId());

        //Payment payment = Payment.newForOrder(
        //        Long.valueOf(criteria.orderId()),

        //)

        var req = new PaymentGateway.Request(
                criteria.userId(), criteria.orderId(),
                criteria.cardType(), criteria.cardNo(),
                finalAmount, callbackUrl
        );

        var result = gateway.request(req);

        return new PaymentResult(criteria.orderId(), result.paymentId(), result.status());
    }

    @Transactional
    public PaymentResult processPointPayment(PaymentCriteria criteria) {
        try {
            BigDecimal finalAmount = orderService.prepareForPayment(criteria.orderId(), criteria.couponId());

            var point = pointRepository.findByUserIdForUpdate(criteria.userId())
                    .orElseThrow();
            point.use(finalAmount);

            orderService.confirmPayment(criteria.orderId(), criteria.couponId());
            return PaymentResult.success(criteria.orderId());
        } catch (Exception e) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                        orderService.markOrderFailed(criteria.orderId());
                    }
                }
            });
            throw e;
        }
    }

    @Transactional
    public void handlePgCallback(PaymentCallback callback) {
        var order = orderService.getOrderDetail(callback.orderId());

        if (order.totalAmount().compareTo(callback.amount()) != 0) {
            orderService.markOrderFailed(callback.orderId());
            throw new IllegalStateException("결제 금액 불일치: 주문 금액=" + order.totalAmount() + ", PG 금액=" + callback.amount());
        }

        if ("SUCCESS".equals(callback.status())) {
            orderService.confirmPayment(
                    callback.orderId(),
                    callback.couponId()
            );

        } else {
            orderService.markOrderFailed(callback.orderId());
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
