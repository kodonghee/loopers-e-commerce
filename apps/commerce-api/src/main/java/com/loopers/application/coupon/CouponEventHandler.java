package com.loopers.application.coupon;

import com.loopers.domain.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CouponEventHandler {

    private final CouponUseCase couponUseCase;
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        couponUseCase.use(event.couponId());
    }
}
