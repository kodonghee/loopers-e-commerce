package com.loopers.application.dataplatform;

import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import com.loopers.domain.payment.event.PaymentDeclinedEvent;
import com.loopers.domain.payment.event.PaymentErrorEvent;
import com.loopers.infrastructure.dataplatform.DataPlatformClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformEventHandler {

    private final DataPlatformClient client;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCreatedEvent event) {
        client.sendOrderData(event);
        log.info("[DataPlatform] 주문 이벤트 전송 완료: orderId={}", event.orderId());
    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        client.sendPaymentData(event);
        log.info("데이터 플랫폼으로 결제 성공 데이터 전송 완료: orderId={}", event.orderId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentDeclinedEvent event) {
        client.sendPaymentData(event);
        log.info("데이터 플랫폼으로 결제 거절 데이터 전송 완료: orderId={}, reason={}", event.orderId(), event.reason());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentErrorEvent event) {
        client.sendPaymentData(event);
        log.warn("데이터 플랫폼으로 결제 오류 데이터 전송 완료: orderId={}, reason={}", event.orderId(), event.reason());
    }
}
