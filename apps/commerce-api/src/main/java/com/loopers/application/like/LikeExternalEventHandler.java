package com.loopers.application.like;

import com.loopers.application.event.MessagePublisher;
import com.loopers.events.like.LikeChangedEvent;
import com.loopers.domain.like.event.LikeCancelledEvent;
import com.loopers.domain.like.event.LikeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeExternalEventHandler {

    private final MessagePublisher messagePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(LikeCreatedEvent event) {
        messagePublisher.publish(new LikeChangedEvent(
                event.productId(),
                event.userId(),
                true
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(LikeCancelledEvent event) {
        messagePublisher.publish(new LikeChangedEvent(
                event.productId(),
                event.userId(),
                false
        ));
    }
}
