package com.loopers.application.payment;

import com.loopers.application.payment.port.PaymentGateway;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderPaymentProcessor;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.payment.pg.PgServerUnstableException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentGateway gateway;
    private final PointRepository pointRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final OrderPaymentProcessor paymentProcessor;

    @Value("${pg.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    public PaymentResult requestCardPayment(PaymentCriteria criteria) {

        Order order = orderRepository.findByOrderId(criteria.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        var coupon = criteria.couponId() != null
                ? couponRepository.findById(criteria.couponId()).orElse(null)
                : null;

        BigDecimal finalAmount = paymentProcessor.prepareForPayment(order, coupon);

        Payment payment = Payment.newForOrder(
                criteria.orderId(),
                criteria.userId(),
                finalAmount
        );

        paymentRepository.save(payment);

        try {
            var req = new PaymentGateway.Request(
                    criteria.userId(), criteria.orderId(),
                    criteria.cardType(), criteria.cardNo(),
                    finalAmount, callbackUrl
            );

            var result = gateway.request(req);

            if ("ERROR".equals(result.status())) {
                payment.updateStatus(null, PaymentStatus.ERROR, result.reason());
                order.errorPayment();
                return PaymentResult.failed(order.getOrderId());
            }

            payment.markRequested(result.paymentId());
            return PaymentResult.pending(order.getOrderId(), result.paymentId());
        } catch (FeignException.BadRequest e) {
            payment.updateStatus(null, PaymentStatus.DECLINED, "잘못된 요청: " + e.getMessage()); // 결제 상태 변경
            order.declinePayment(); // 주문 상태 변경
            throw e;
        } catch (PgServerUnstableException e) {
            payment.updateStatus(null, PaymentStatus.ERROR, "PG 서버 오류: " + e.getMessage());
            order.errorPayment();
            throw e;
        } catch (feign.RetryableException e) {
            payment.updateStatus(null, PaymentStatus.ERROR, "네트워크 오류: " + e.getMessage());
            order.errorPayment();
            throw e;
        } catch (Exception e) {
            payment.updateStatus(null, PaymentStatus.ERROR, "알 수 없는 오류: " + e.getMessage());
            order.errorPayment();
            throw e;
        }
    }

    @Transactional
    public PaymentResult processPointPayment(PaymentCriteria criteria) {
        Order order = orderRepository.findByOrderId(criteria.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        var coupon = criteria.couponId() != null
                ? couponRepository.findById(criteria.couponId()).orElse(null)
                : null;

        Payment payment = Payment.newForOrder(order.getOrderId(), order.getUserId(), order.getFinalAmount());
        paymentRepository.save(payment);

        try {
            BigDecimal finalAmount = paymentProcessor.prepareForPayment(order, coupon);

            var point = pointRepository.findByUserIdForUpdate(criteria.userId())
                    .orElseThrow();
            point.use(finalAmount);

            List<Product> products = productRepository.findByIdForUpdate(
                    order.getOrderItems().stream().map(OrderItem::getProductId).toList()
            );

            paymentProcessor.confirmPayment(order, products, coupon);
            payment.updateStatus(null, PaymentStatus.SUCCESS, "포인트 결제 성공");
            order.paid();
            return PaymentResult.success(order.getOrderId());
        } catch (IllegalArgumentException e) {
            payment.updateStatus(null, PaymentStatus.DECLINED, e.getMessage());
            order.declinePayment();
            throw e;
        } catch (Exception e) {
            payment.updateStatus(null, PaymentStatus.ERROR, "시스템 오류: " + e.getMessage());
            order.errorPayment();
            throw e;
        }
    }

    @Transactional
    public void handlePgCallback(PaymentCallback callback) {
        Order order = orderRepository.findByOrderId(callback.orderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        Payment payment = paymentRepository.findByOrderId(callback.orderId())
                .orElseThrow(() -> new IllegalArgumentException("결제 정보가 존재하지 않습니다."));

        if (!order.isAwaitingPayment() || payment.isFinalized()) {
            log.warn("이미 처리된 결제 콜백입니다: {}", callback.orderId());
            return;
        }

        if (order.getFinalAmount().compareTo(callback.amount()) != 0) {
            payment.updateStatus(callback.paymentId(), PaymentStatus.ERROR, "금액 불일치");
            order.errorPayment();
            throw new IllegalStateException("결제 금액 불일치: 주문 금액=" + order.getFinalAmount() + ", PG 금액=" + callback.amount());
        }

        PaymentStatus newStatus = PaymentStatus.fromPgStatus(callback.status());
        payment.updateStatus(callback.paymentId(), newStatus, callback.reason());

        if (newStatus.isSuccess()) {
            List<Product> products = productRepository.findByIdForUpdate(
                    order.getOrderItems().stream().map(OrderItem::getProductId).toList()
            );
            Coupon coupon = callback.couponId() != null
                    ? couponRepository.findById(callback.couponId()).orElse(null)
                    : null;

            paymentProcessor.confirmPayment(order, products, coupon);
            order.paid();
        } else if (newStatus == PaymentStatus.DECLINED) {
            order.declinePayment();
        } else {
            order.errorPayment();
        }
    }

    public PaymentResult findByOrderId(String userId, String orderId) {
        var result = gateway.findByOrderId(userId, orderId);

        if (result == null) {
            return PaymentResult.pending(orderId, null);
        }

        return new PaymentResult(result.orderId(), result.paymentId(), PaymentStatus.valueOf(result.status()));
    }
}
