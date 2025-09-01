package com.loopers.application.like;

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
public class LikeSummaryEventHandler {
    private final LikeFacade likeFacade;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(LikeCreatedEvent event) {
        try {
            likeFacade.increaseLikeCounts(event.productId(), event.userId());
        } catch (Exception e) {
            log.error("좋아요 집계 실패 (productId={}, userId={}) — 좋아요는 커밋됨",
                    event.productId(), event.userId(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(LikeCancelledEvent event) {
        try {
            likeFacade.decreaseLikeCounts(event.productId(), event.userId());
        } catch (Exception e) {
            log.error("좋아요 취소 집계 실패 (productId={}, userId={}) — 좋아요 취소는 커밋됨",
                    event.productId(), event.userId(), e);
        }
    }
}
