package com.loopers.application.order;

import com.loopers.application.event.MessagePublisher;
import com.loopers.domain.like.event.LikeCreatedEvent;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.events.like.LikeChangedEvent;
import com.loopers.events.order.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExternalEventHandler {

    private final MessagePublisher messagePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OrderCreatedEvent event) {

        var itemDtos = event.items().stream()
                .map(i -> new OrderPlacedEvent.OrderItemDto(i.getProductId(), i.getQuantity(), i.getPrice()))
                .toList();

        OrderPlacedEvent externalEvent = OrderPlacedEvent.of(
                event.orderId(),
                event.userId(),
                event.totalAmount(),
                itemDtos
        );

        log.info("Publishing OrderPlacedEvent: {}", externalEvent);
        messagePublisher.publish(externalEvent);
    }
}
