package com.loopers.domain.coupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {
    @DisplayName("쿠폰은 특정 사용자가 소유하고 있어야 한다.")
    @Test
    void couponHasOwner() {
        // arrange
        String userId = "cookie95";
        Coupon coupon = new Coupon(userId, CouponType.FIXED, new BigDecimal("10000"));

        // act
        String owner = coupon.getUserId();

        // assert
        assertThat(owner).isEqualTo(userId);
    }

    @DisplayName("이미 사용된 쿠폰은 사용할 수 없다.")
    @Test
    void cannotUseCoupon_whenCouponIsUsed() {
        // arrange
        Coupon coupon = new Coupon("cookie95", CouponType.FIXED, new BigDecimal("10000"));
        coupon.markAsUsed();

        // act & assert
        assertThatThrownBy(() -> coupon.applyCoupon(new BigDecimal("30000")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 사용된 쿠폰입니다.");
    }

    @DisplayName("쿠폰은 한 번만 사용 처리할 수 있다.")
    @Test
    void cannotMarkCouponAsUsedTwice() {
        // arrange
        Coupon coupon = new Coupon("cookie95", CouponType.FIXED, new BigDecimal("10000"));
        coupon.markAsUsed();

        // act & assert
        assertThatThrownBy(coupon::markAsUsed)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이미 사용된 쿠폰입니다.");
    }
}
