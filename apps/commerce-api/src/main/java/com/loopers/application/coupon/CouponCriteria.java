package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponType;

import java.math.BigDecimal;

public record CouponCriteria (
    String userId,
    CouponType type,
    BigDecimal amount
) {}
