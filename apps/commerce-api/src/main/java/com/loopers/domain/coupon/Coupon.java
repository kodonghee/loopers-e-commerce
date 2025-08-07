package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "member_coupon")
public class Coupon extends BaseEntity {

    private String userId;

    @Enumerated(EnumType.STRING)
    private CouponType type;

    private BigDecimal amount;

    private boolean used;

    protected Coupon() {}

    public Coupon(String userId, CouponType type, BigDecimal amount) {
        validateUserId(userId);
        validateCouponAmount(amount);
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.used = false;
    }

    public String getUserId() { return userId; }

    public boolean isUsed() { return used; }

    public BigDecimal getAmount() { return amount; }

    public void checkOwner(String requestUserId) {
        if (!this.userId.equals(requestUserId)) {
            throw new IllegalArgumentException("쿠폰 소유자가 아닙니다.");
        }
    }
    public BigDecimal applyCoupon(BigDecimal totalAmount) {
        if (used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        BigDecimal discount = switch (type) {
            case FIXED -> amount.min(totalAmount);
            case RATE -> totalAmount.multiply(amount).divide(new BigDecimal("100"), 0, RoundingMode.FLOOR);
        };

        return totalAmount.subtract(discount);
    }

    public void markAsUsed() {
        if (used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }

        this.used = true;
    }

    // =============================
    // 🔒 Validation methods
    // =============================

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("ID는 빈 값이 될 수 없습니다.");
        }

        if (!userId.matches("^[a-zA-Z0-9]{1,10}$")) {
            throw new IllegalArgumentException("ID는 영문 및 숫자 10자 이내여야 합니다.");
        }
    }

    private void validateCouponAmount(BigDecimal couponAmount) {
        if (couponAmount == null || couponAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("쿠폰 금액은 0보다 커야 합니다.");
        }
    }
}
