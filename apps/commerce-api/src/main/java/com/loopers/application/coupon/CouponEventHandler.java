package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CouponEventHandler {

    private final CouponRepository couponRepository;
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        if (event.couponId() != null) {
            couponRepository.findByIdForUpdate(event.couponId())
                    .ifPresent(Coupon::markAsUsed);
        }
    }
}
