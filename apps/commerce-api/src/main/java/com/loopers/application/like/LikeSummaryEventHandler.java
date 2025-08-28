package com.loopers.application.like;

import com.loopers.domain.like.ProductLikeSummaryRepository;
import com.loopers.domain.like.event.LikeCancelledEvent;
import com.loopers.domain.like.event.LikeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeSummaryEventHandler {
    private final ProductLikeSummaryRepository productLikeSummaryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(LikeCreatedEvent event) {
        productLikeSummaryRepository.findByProductIdForUpdate(event.productId())
                .ifPresent(summary -> {
                    summary.increment();
                    productLikeSummaryRepository.save(summary);
                    log.info("상품 {} 좋아요 수 +1 (userId={})", event.productId(), event.userId());
                });
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(LikeCancelledEvent event) {
        productLikeSummaryRepository.findByProductIdForUpdate(event.productId())
                .ifPresent(summary -> {
                    summary.decrement();
                    productLikeSummaryRepository.save(summary);
                    log.info("상품 {} 좋아요 수 -1 (userId={})", event.productId(), event.userId());
                });
    }
}
