package com.loopers.application.payment;

import com.loopers.application.order.OrderService;
import com.loopers.application.payment.port.PaymentGateway;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.PageableOpenAPIConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PaymentService {

    private final PaymentGateway gateway;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final PointRepository pointRepository;
    private final OrderService orderService;

    @Value("${pg.callback-url:http://localhost:8080/api/v1/pg/callback}")
    private String callbackUrl;

    public PaymentResult requestCardPayment(PaymentCriteria criteria) {
        var req = new PaymentGateway.Request(
                criteria.userId(), criteria.orderId(),
                criteria.cardType(), criteria.cardNo(),
                criteria.amount(), callbackUrl
        );
        var result = gateway.request(req);
        return new PaymentResult(result.orderId(), result.paymentId(), result.status());
    }

    @Transactional
    public PaymentResult processPointPayment(PaymentCriteria criteria) {
        var point = pointRepository.findByUserIdForUpdate(criteria.userId())
                .orElseThrow();
        point.use(criteria.amount());

        orderService.markOrderPaid(Long.valueOf(criteria.orderId()));
        return PaymentResult.success(criteria.orderId());
    }

    @Transactional
    public void handlePgCallback(PaymentCallbackDto callback) {
        if ("SUCCESS".equals(callback.status())) {
            orderService.markOrderPaid(Long.valueOf(callback.orderId()));

            productRepository.decreaseStock(callback.items());

            if (callback.couponId() != null) {
                var coupon = couponRepository.findByIdForUpdate(callback.couponId()).orElseThrow();
                coupon.checkOwner(callback.userId());
                coupon.markAsUsed();
            }

            if (callback.usePoint()) {
                var point = pointRepository.findByUserIdForUpdate(callback.userId()).orElseThrow();
                point.use(callback.amount());
            }
        } else {
            orderService.markOrderFailed(Long.valueOf(callback.orderId()));
        }
    }

    public PaymentResult findByOrderId(String userId, String orderId) {
        var result = gateway.findByOrderId(userId, orderId);
        return new PaymentResult(result.orderId(), result.paymentId(), result.status());
    }
}
