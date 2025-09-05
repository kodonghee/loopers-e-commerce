package com.loopers.application.order;

import com.loopers.domain.payment.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventHandler {

    private final OrderService orderService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        log.info("주문 {} → 결제 성공(PAID)", event.orderId());
        orderService.pay(event.orderId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    public void handle(PaymentDeclinedEvent event) {
        log.info("주문 {} → 결제 거절(PAYMENT_DECLINED), reason={}", event.orderId(), event.reason());
        orderService.decline(event.orderId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handle(PaymentDeclinedRollbackEvent event) {
        log.info("주문 {} → 결제 거절(PAYMENT_DECLINED), reason={}", event.orderId(), event.reason());
        orderService.decline(event.orderId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @EventListener
    public void handle(PaymentErrorEvent event) {
        log.warn("주문 {} → 결제 오류(PAYMENT_ERROR), reason={}", event.orderId(), event.reason());
        orderService.error(event.orderId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handle(PaymentErrorRollbackEvent event) {
        log.warn("주문 {} → 결제 오류(PAYMENT_ERROR), reason={}", event.orderId(), event.reason());
        orderService.error(event.orderId());
    }
}
