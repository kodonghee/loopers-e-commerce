package com.loopers.domain.coupon;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        assertThatThrownBy(() -> coupon.calculateDiscount(new BigDecimal("30000")))
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

    @DisplayName("주문 금액에 정액 쿠폰을 적용하면 해당 금액만큼 할인된다.")
    @Test
    void fixedCouponAppliesCorrectly() {
        // arrange
        Coupon coupon = new Coupon("cookie95", CouponType.FIXED, new BigDecimal("10000"));
        BigDecimal orderAmount = new BigDecimal("30000");

        // act
        BigDecimal discountAmount = coupon.calculateDiscount(orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        // assert
        assertThat(finalAmount).isEqualByComparingTo("20000");
    }

    @DisplayName("주문 금액에 정률 쿠폰을 적용하면 해당 금액만큼 할인된다.")
    @Test
    void rateCouponAppliesCorrectly() {
        // arrange
        Coupon coupon = new Coupon("cookie95", CouponType.RATE, new BigDecimal("10"));
        BigDecimal orderAmount = new BigDecimal("30000");

        // act
        BigDecimal discountAmount = coupon.calculateDiscount(orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        // assert
        assertThat(finalAmount).isEqualByComparingTo("27000");
    }

    @ParameterizedTest(name = "couponAmount={0}, orderAmount={1} -> 결제 금액은 0원")
    @CsvSource({
            "50000, 30000",
            "1000, 1",
            "10000, 10000"
    })
    @DisplayName("쿠폰 할인 금액이 주문 금액보다 큰 경우, 할인된 최종 결제 금액은 최소 0원이다.")
    void finalAmountIsMinZero_whenCouponGreaterThanOrderAmount(String couponStr, String orderStr) {
        // arrange
        Coupon coupon = new Coupon("cookie95", CouponType.FIXED, new BigDecimal(couponStr));
        BigDecimal orderAmount = new BigDecimal(orderStr);

        // act
        BigDecimal discountAmount = coupon.calculateDiscount(orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        // assert
        assertThat(finalAmount).isEqualByComparingTo("0");
    }

    @DisplayName("정률 쿠폰의 할인 금액은 소수점 이하를 버림하여 계산한다.")
    @Test
    void discountAmountIsRoundedDownToInteger_whenApplyRateCoupon() {
        // arrange
        Coupon coupon = new Coupon("cookie95", CouponType.RATE, new BigDecimal("33"));
        BigDecimal orderAmount = new BigDecimal("3960");

        // act
        BigDecimal discountAmount = coupon.calculateDiscount(orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        // assert
        assertThat(finalAmount).isEqualByComparingTo("2654");
    }
}
