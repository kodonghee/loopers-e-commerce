package com.loopers.application.coupon;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CouponUseCase {

    private final CouponRepository couponRepository;

    @Transactional
    public Long createCoupon(CouponCriteria criteria) {
        Coupon coupon = new Coupon(
                criteria.userId(),
                criteria.type(),
                criteria.amount()
        );

        couponRepository.save(coupon);
        return coupon.getId();
    }

    @Transactional
    public void use(Long couponId) {
        Optional<Coupon> mayCoupon = couponRepository.findByIdForUpdate(couponId);
        if(mayCoupon.isEmpty()){
            return;
        }
        mayCoupon.get().markAsUsed();
    }

}
