package com.loopers.application.payment;

import com.loopers.application.order.OrderService;
import com.loopers.application.payment.port.PaymentGateway;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.PointRepository;
import feign.FeignException;
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
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Value("${pg.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    public PaymentResult requestCardPayment(PaymentCriteria criteria) {
        BigDecimal finalAmount = orderService.prepareForPayment(criteria.orderId(), criteria.couponId());

        Payment payment = Payment.newForOrder(
                criteria.orderId(),
                criteria.userId(),
                criteria.amount()
        );

        paymentRepository.save(payment);

        try {
            var req = new PaymentGateway.Request(
                    criteria.userId(), criteria.orderId(),
                    criteria.cardType(), criteria.cardNo(),
                    finalAmount, callbackUrl
            );

            var result = gateway.request(req);

            payment.applyCallback(result.paymentId(), PaymentStatus.PENDING, "PG 결제 대기중");
            return new PaymentResult(criteria.orderId(), result.paymentId(), "PENDING");
        } catch (FeignException.BadRequest e) {
            payment.applyCallback(null, PaymentStatus.DECLINED, "잘못된 요청: " + e.getMessage()); // 결제 상태 변경
            orderService.getOrderEntity(criteria.orderId()).declinePayment(); // 주문 상태 변경
            throw e;
        } catch (FeignException.InternalServerError e) {
            payment.applyCallback(null, PaymentStatus.ERROR, "PG 서버 오류: " + e.getMessage());
            orderService.getOrderEntity(criteria.orderId()).errorPayment();
            throw e;
        } catch (feign.RetryableException e) {
            payment.applyCallback(null, PaymentStatus.ERROR, "네트워크 오류: " + e.getMessage());
            orderService.getOrderEntity(criteria.orderId()).errorPayment();
            throw e;
        } catch (Exception e) {
            payment.applyCallback(null, PaymentStatus.ERROR, "알 수 없는 오류: " + e.getMessage());
            orderService.getOrderEntity(criteria.orderId()).errorPayment();
            throw e;
        }
    }

    @Transactional
    public PaymentResult processPointPayment(PaymentCriteria criteria) {
        Payment payment = Payment.newForOrder(criteria.orderId(), criteria.userId(), criteria.amount());
        paymentRepository.save(payment);

        try {
            BigDecimal finalAmount = orderService.prepareForPayment(criteria.orderId(), criteria.couponId());

            var point = pointRepository.findByUserIdForUpdate(criteria.userId())
                    .orElseThrow();
            point.use(finalAmount);

            orderService.confirmPayment(criteria.orderId(), criteria.couponId());
            payment.applyCallback(null, PaymentStatus.SUCCESS, "포인트 결제 성공");
            return PaymentResult.success(criteria.orderId());
        } catch (IllegalArgumentException e) {
            payment.applyCallback(null, PaymentStatus.DECLINED, e.getMessage());
            orderService.markOrderFailed(criteria.orderId(), true);
            throw e;
        } catch (Exception e) {
            payment.applyCallback(null, PaymentStatus.ERROR, "시스템 오류: " + e.getMessage());
            orderService.markOrderFailed(criteria.orderId(), false);
            throw e;
        }
    }

    @Transactional
    public void handlePgCallback(PaymentCallback callback) {
        var order = orderService.getOrderDetail(callback.orderId());

        if (order.totalAmount().compareTo(callback.amount()) != 0) {
            orderService.markOrderFailed(callback.orderId(), false);
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
