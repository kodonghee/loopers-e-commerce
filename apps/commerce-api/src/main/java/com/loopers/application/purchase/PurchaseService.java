package com.loopers.application.purchase;

import com.loopers.application.order.OrderCriteria;
import com.loopers.application.order.OrderResult;
import com.loopers.application.order.OrderService;
import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentService;
import com.loopers.domain.order.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PurchaseService {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional
    public PurchaseResult purchase(OrderCriteria orderCriteria, PaymentCriteria paymentCriteria) {

        // 1. 주문 생성
        OrderResult order = orderService.createPendingOrder(orderCriteria);

        if (orderCriteria.paymentMethod() == PaymentMethod.POINTS) {
            var result = paymentService.processPointPayment(paymentCriteria);
            return new PurchaseResult(order, result);
        } else {
            var result = paymentService.requestCardPayment(paymentCriteria);
            return new PurchaseResult(order, result);
        }
    }
}
