package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CouponUseCase {

    private final CouponRepository couponRepository;

    public Long createCoupon(CouponCriteria criteria) {
        Coupon coupon = new Coupon(
                criteria.userId(),
                criteria.type(),
                criteria.amount()
        );

        couponRepository.save(coupon);
        return coupon.getId();
    }
}
